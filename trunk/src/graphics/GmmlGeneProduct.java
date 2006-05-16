package graphics;

import java.util.*;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*;

import javax.swing.JTable;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import util.GmmlColorConvertor;

import data.GmmlData;

/**
 * This class implements a geneproduct and 
 * provides methods to resize and draw it.
 */
public class GmmlGeneProduct extends GmmlGraphics
{
	private static final long serialVersionUID = 1L;
	private static final int INITIAL_FONTSIZE = 10;
	public static final int INITIAL_WIDTH = 80;
	public static final int INITIAL_HEIGHT = 20;
	public static final RGB INITIAL_FILL_COLOR = new RGB(255, 255, 255);
	
	public static final List attributes = Arrays.asList(new String[] {
			"Name", "GeneProduct-Data-Source", "GeneID", 
			"CenterX", "CenterY", "Width", "Height", "Color", 
			"Xref", "BackpageHead","Type", "Notes" 
	});
	
	public static final List dataSources = Arrays.asList(new String[] {
			"FlyBase", "GenBank", "GenBank", 
			"InterPro", "LocusLink", "MGI", "RefSeq", "RGD", 
			"SGD", "SwissProt", "TAIR", "UniGene", "UniProt",
			"WormBase", "Affy", "ZFIN"
	});
	
	double centerx;
	double centery;
	double width;
	double height;
	double fontSizeDouble;
	int fontSize;

	RGB color = new RGB (0,0,0);
	RGB fillColor = INITIAL_FILL_COLOR;
	
	GmmlDrawing canvas;
	
	Element jdomElement;
	
	String geneID;
	String xref;
	String name = "GeneID";
	String backpageHead = "";
	String type = "unknown";
	String notes = "";
	String geneProductDataSource = "";
	
	GmmlHandle handlecenter	= new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this);
	GmmlHandle handlex		= new GmmlHandle(GmmlHandle.HANDLETYPE_WIDTH, this);
	GmmlHandle handley		= new GmmlHandle(GmmlHandle.HANDLETYPE_HEIGHT, this);
	
	GmmlGpColor gpColor;
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this geneproduct will be part of
	 */
	public GmmlGeneProduct(GmmlDrawing canvas)
	{
		drawingOrder = GmmlDrawing.DRAW_ORDER_GENEPRODUCT;
		
		this.canvas = canvas;
		canvas.addElement(handlecenter);
		canvas.addElement(handlex);
		canvas.addElement(handley);
		
		gpColor = new GmmlGpColor(this);
		
		this.fontSizeDouble = INITIAL_FONTSIZE / canvas.zoomFactor;
		this.fontSize = (int)this.fontSizeDouble;
	}
	
	/**
	 * Constructor for this class
	 * @param x - the upper left corner x coordinate
	 * @param y - the upper left corner y coordinate
	 * @param width - the width
	 * @param height - the height
	 * @param geneLabel - the gene label as it will be shown as a label
	 * @param xref - 
	 * @param color - the color this geneproduct will be painted
	 * @param canvas - the GmmlDrawing this geneproduct will be part of
	 */
	public GmmlGeneProduct(double x, double y, double width, double height, String geneLabel, String xref, RGB color, GmmlDrawing canvas, Document doc){
		this(canvas);
		
		this.centerx = x;
		this.centery = y;
		this.width = width;
		this.height = height;
		this.geneID = geneLabel;
		this.xref = xref;
		this.color = color;

		setHandleLocation();
		
		createJdomElement(doc);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlGeneProduct
	 * @param canvas - the GmmlDrawing this GmmlAGmmlGeneProductrc will be part of
	 */
	public GmmlGeneProduct(Element e, GmmlDrawing canvas) {
		this(canvas);
		
		this.jdomElement = e;
		mapAttributes(e);
		
		
		setHandleLocation();
	}

	/**
	 * Set the geneproduct at the location specified
	 * @param x - new x coordinate
	 * @param y - new y coordinate
	 */
	public void setLocation(double x, double y)
	{
		centerx = x;
		centery = y;
	}
	
	public Vector<GmmlHandle> getHandles()
	{
		Vector<GmmlHandle> v = new Vector<GmmlHandle>();
		v.add(handlecenter);
		v.add(handlex);
		v.add(handley);
		return v;
	}
	
	/**
	 * Updates the JDom representation of this geneproduct
	 */
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("GeneID", geneID);
			jdomElement.setAttribute("Xref", xref);
			jdomElement.setAttribute("Type", type);
			jdomElement.setAttribute("Name", name);
			jdomElement.setAttribute("BackpageHead", backpageHead);
			jdomElement.setAttribute("Notes", notes);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("CenterX", Integer.toString((int)centerx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString((int)centery * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString((int)width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Integer.toString((int)height * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Color", GmmlColorConvertor.color2String(color));
			}
		}
	}
	
	/**
	 * Fetches the gene identifier from the Jdom representation
	 *
	 */
	public String getGeneId() {
		if(jdomElement != null) {
			return jdomElement.getAttributeValue("Name");
			} else {
			return "";
		}
	}
	
	private Text t;
	public void createTextControl()
	{
		t = new Text(canvas, SWT.SINGLE | SWT.BORDER);
		t.setLocation((int)centerx, (int)centery - 10);
		t.setSize(100,20);
		t.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				disposeTextControl();
			}
			public void focusGained(FocusEvent e) {}
		});
		t.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR)
				{
					disposeTextControl();
				}
			}
			public void keyReleased(KeyEvent e) {}
		});
		t.setFocus();
		t.setVisible(true);
	}
	
	protected void disposeTextControl()
	{
		Rectangle rp = getBounds();
		
		geneID = t.getText();
		canvas.updatePropertyTable(this);
		t.setVisible(false);
		t.dispose();

		Rectangle r = getBounds();
		r.add(rp);
		r.grow(5,5);
		canvas.redraw(r.x, r.y, r.width, r.height, false);
	}
	
	protected void createJdomElement(Document doc) {
		if(jdomElement == null) {
			jdomElement = new Element("GeneProduct");
			jdomElement.addContent(new Element("Graphics"));
			
			doc.getRootElement().addContent(jdomElement);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#adjustToZoom()
	 */
	public void adjustToZoom(double factor)
	{
		centerx	*= factor;
		centery	*= factor;
		width	*= factor;
		height	*= factor;
		fontSizeDouble *= factor;
		fontSize = (int)fontSizeDouble;
	}

	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e, GC buffer)
	{
		Font f = new Font(e.display, "ARIAL", fontSize, SWT.NONE);
		
		buffer.setFont (f);
		
		Point textSize = buffer.textExtent (geneID);
		
		Color c;
		if (isSelected)
		{
			c = new Color (e.display, 255, 0, 0);
		}
		else 
		{
			c = new Color (e.display, this.color);
		}
		
//		Color cFill = new Color(e.display, this.fillColor);
//		if(canvas.colorSetIndex > -1)
//		{
//			RGB rgb = canvas.gmmlVision.gmmlGex.getGeneColor(name, canvas.colorSetIndex);
//			if(rgb != null)
//			{
//				System.out.println("Drawing in color: " + rgb);
//				cFill = new Color(e.display, rgb);
//			}
//		}
//
//		buffer.setBackground (cFill);
//		
//		buffer.fillRectangle(
//				(int)(centerx - width / 2),
//				(int)(centery - height / 2),
//				(int)width,
//				(int)height
//			);
		
		buffer.setForeground(c);
		buffer.setLineStyle (SWT.LINE_SOLID);
		buffer.setLineWidth (1);
		
		gpColor.draw(e, buffer);

		buffer.drawRectangle (
			(int)(centerx - width / 2),
			(int)(centery - height / 2),
			(int)width,
			(int)height
		);
		
		buffer.setClipping (
				(int)(centerx - width / 2) - 1,
				(int)(centery - height / 2) - 1,
				(int)width - 1,
				(int)height - 1
			);
		buffer.setForeground(c);
		buffer.drawString (geneID, 
			(int) centerx - (textSize.x / 2) , 
			(int) centery - (textSize.y / 2), true);
		
		Region r = null;
		buffer.setClipping(r);
		
		f.dispose();
		c.dispose();
//		cFill.dispose();
		
		setHandleLocation();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D point)
	{
		Rectangle2D rect = new Rectangle2D.Double(
			centerx - width/2, centery - height/2, width, height);
		
		return rect.contains(point);
	}	

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#intersects(java.awt.geom.Rectangle2D.Double)
	 */
	protected boolean intersects(Rectangle2D.Double r)
	{
		return r.intersects(centerx - width/2, centery - height/2, width, height);
	}
	
	protected Rectangle getBounds()
	{
		Rectangle2D rect = new Rectangle2D.Double(
				centerx - width/2, centery - height/2, width, height);
		return rect.getBounds();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#moveBy(double, double)
	 */
	protected void moveBy(double dx, double dy)
	{
		setLocation(centerx + dx, centery + dy);

		// TODO
		//~ BasicStroke stroke = new BasicStroke(20);
		//~ Shape s = stroke.createStrokedShape(rect);

		//~ Iterator it = canvas.lineHandles.iterator();

		//~ while (it.hasNext())
		//~ {
			//~ GmmlHandle h = (GmmlHandle) it.next();
			//~ Point2D p = h.getCenterPoint();
			//~ if (s.contains(p))
			//~ {
				//~ h.moveBy(dx, dy);
			//~ }
		//~ }
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#resizeX(double)
	 */
	protected void resizeX(double dx)
	{
		width = Math.abs(width + dx);
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#resizeY(double)
	 */
	protected void resizeY(double dy)
	{
		height = Math.abs(height - dy);
		
	}

	public List getAttributes() {
		return attributes;
	}
	
	public void updateToPropItems()
	{
		if (propItems == null)
		{
			propItems = new Hashtable();
		}

		Object[] values = new Object[] {name, geneProductDataSource,
				geneID, centerx, centery, width, height, color,
				xref, backpageHead, type, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		Rectangle rp = getBounds();
		
		centerx		= (Double)propItems.get(attributes.get(attributes.indexOf("CenterX")));
		centery		= (Double)propItems.get(attributes.get(attributes.indexOf("CenterY")));
		width		= (Double)propItems.get(attributes.get(attributes.indexOf("Width")));
		height		= (Double)propItems.get(attributes.get(attributes.indexOf("Height")));
		geneID		= (String)propItems.get(attributes.get(attributes.indexOf("GeneID")));
		xref		= (String)propItems.get(attributes.get(attributes.indexOf("Xref")));
		color		= (RGB)propItems.get(attributes.get(attributes.indexOf("Color")));
		name		= (String)propItems.get(attributes.get(attributes.indexOf("Name")));
		backpageHead	= (String)propItems.get(attributes.get(attributes.indexOf("BackpageHead")));
		type		= (String)propItems.get(attributes.get(attributes.indexOf("Type")));
		notes		= (String)propItems.get(attributes.get(attributes.indexOf("Notes")));
		geneProductDataSource = (String)propItems.get(attributes.get(attributes.indexOf("GeneProduct-Data-Source")));

		// Update jdom element to store gene id
		jdomElement.setAttribute("Name", name);
		
		Rectangle r = getBounds();
		r.add(rp);
		r.grow(5,5);
		canvas.redraw(r.x, r.y, r.width, r.height, false);
	}

	/**
	 * Maps attributes to internal variables.
	 * @param e - the element to map to a GmmlArc
	 */
	private void mapAttributes (Element e) {
		// Map attributes
		System.out.println("> Mapping element '" + e.getName()+ "'");
		Iterator it = e.getAttributes().iterator();
		while(it.hasNext()) {
			Attribute at = (Attribute)it.next();
			int index = attributes.indexOf(at.getName());
			String value = at.getValue();
			switch(index) {
					case 3:// CenterX
						this.centerx = Integer.parseInt(value) / GmmlData.GMMLZOOM ; break;
					case 4:// CenterY
						this.centery = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 5:// Width
						this.width = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 6:// Height
						this.height = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 2:// GeneLabel
						this.geneID = value; break;
					case 8:// Xref
						this.xref = value; break;
					case 7:// Color
						this.color = GmmlColorConvertor.string2Color(value); break;
					case 0:// Name
						this.name = value; break;
					case 9:// BackpageHead
						this.backpageHead = value; break;
					case 10: // Type
						this.type = value; break;
					case 11:// Notes
						this.notes = value; break;
					case 1:// GeneProduct-Data-Source
						this.geneProductDataSource = value; break;
					case -1:
						System.out.println("\t> Attribute '" + at.getName() + "' is not recognized");
			}
		}
		// Map child's attributes
		it = e.getChildren().iterator();
		while(it.hasNext()) {
			mapAttributes((Element)it.next());
		}
	}

	/**
	 * Sets this class's handles at the correct location
	 */
	private void setHandleLocation()
	{
		handlecenter.setLocation(centerx, centery);
		handlex.setLocation(centerx + width/2, centery);
		handley.setLocation(centerx, centery - height/2);
	}
} //end of GmmlGeneProduct