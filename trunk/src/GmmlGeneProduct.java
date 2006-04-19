import java.util.*;
import java.util.List;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*;

import javax.swing.JTable;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * This class implements a geneproduct and 
 * provides methods to resize and draw it.
 */
public class GmmlGeneProduct extends GmmlGraphics
{
	private static final long serialVersionUID = 1L;
	private static final int INITIAL_FONTSIZE = 10;
	
	public final List attributes = Arrays.asList(new String[] {
			"CenterX", "CenterY", "Width", "Height",
			"GeneID", "Xref", "Color"
	});
	
	double centerx;
	double centery;
	double width;
	double height;
	double fontSizeDouble;
	int fontSize;

	RGB color = new RGB (0,0,0);
	RGB fillColor = new RGB (255,255,255);
	
	GmmlDrawing canvas;
	
	Element jdomElement;
	
	String geneLabel;
	String xref;

	GmmlHandle handlecenter	= new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this);
	GmmlHandle handlex		= new GmmlHandle(GmmlHandle.HANDLETYPE_WIDTH, this);
	GmmlHandle handley		= new GmmlHandle(GmmlHandle.HANDLETYPE_HEIGHT, this);
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this geneproduct will be part of
	 */
	public GmmlGeneProduct(GmmlDrawing canvas)
	{
		this.canvas = canvas;

		canvas.addElement(handlecenter);
		canvas.addElement(handlex);
		canvas.addElement(handley);
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
	public GmmlGeneProduct(double x, double y, double width, double height, String geneLabel, String xref, RGB color, GmmlDrawing canvas){
		this.centerx = x;
		this.centery = y;
		this.width = width;
		this.height = height;
		this.geneLabel = geneLabel;
		this.xref = xref;
		this.color = color;
		this.canvas = canvas;
		this.fontSize = INITIAL_FONTSIZE;
		this.fontSizeDouble = this.fontSize;
		
		updateJdomGraphics();
		canvas.addElement(handlecenter);
		canvas.addElement(handlex);
		canvas.addElement(handley);
		setHandleLocation();
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlGeneProduct
	 * @param canvas - the GmmlDrawing this GmmlAGmmlGeneProductrc will be part of
	 */
	public GmmlGeneProduct(Element e, GmmlDrawing canvas) {
		this.jdomElement = e;
		// List the attributes
		mapAttributes(e);
		this.fontSize = INITIAL_FONTSIZE;
		this.fontSizeDouble = this.fontSize;
		this.canvas = canvas;
		
		updateJdomGraphics();
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
		
		updateJdomGraphics();
	}
	
	/**
	 * Updates the JDom representation of this geneproduct
	 */
	public void updateJdomGraphics() {
		if(jdomElement != null) {
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("CenterX", Integer.toString((int)centerx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString((int)centery * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString((int)width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Integer.toString((int)height * GmmlData.GMMLZOOM));
			}
		}
	}
	
	/**
	 * Fetches the gene identifier from the Jdom representation
	 *
	 */
	public String getGeneId() {
		if(jdomElement != null) {
			return jdomElement.getAttribute("Name").getValue();
		} else {
			return "";
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#adjustToZoom()
	 */
	protected void adjustToZoom(double factor)
	{
		centerx	*= factor;
		centery	*= factor;
		width	*= factor;
		height	*= factor;
		fontSizeDouble *= factor;
		fontSize = (int)fontSizeDouble;
		
		updateJdomGraphics();
	}

	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e)
	{
		Font f = new Font(e.display, "ARIAL", fontSize, SWT.NONE);
		
		e.gc.setFont (f);
		
		Point textSize = e.gc.textExtent (geneLabel);
		
		Color c;
		if (isSelected)
		{
			c = new Color (e.display, 255, 0, 0);
		}
		else 
		{
			c = new Color (e.display, this.color);
		}
		
		Color cFill = new Color(e.display, this.fillColor);
		
		e.gc.setForeground (c);
		e.gc.setBackground (cFill);
		e.gc.setLineStyle (SWT.LINE_SOLID);
		
		e.gc.fillRectangle(
				(int)(centerx - width / 2),
				(int)(centery - height / 2),
				(int)width,
				(int)height
			);
		
		e.gc.drawRectangle (
			(int)(centerx - width / 2),
			(int)(centery - height / 2),
			(int)width,
			(int)height
		);
		
		e.gc.drawString (geneLabel, 
			(int) centerx - (textSize.x / 2) , 
			(int) centery - (textSize.y / 2));
		
		f.dispose();			
		
		setHandleLocation();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D point)
	{
		Rectangle2D rect = new Rectangle2D.Double(
			centerx - width/2, centery - height/2, width, height);
		
		isSelected = rect.contains(point);
		return isSelected;
	}	

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#intersects(java.awt.geom.Rectangle2D.Double)
	 */
	protected boolean intersects(Rectangle2D.Double r)
	{
		isSelected = r.intersects(centerx - width/2, centery - height/2, width, height);
		return isSelected;
	}

	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#getPropertyTable()
	 */
	protected JTable getPropertyTable()
	{
		Object[][] data = new Object[][] {{new Double(centerx), new Double(centery), 
			new Double(width), new Double(height), geneLabel, xref, color}};
		
		Object[] cols = new Object[] {"CenterX", "CenterY", "Width",
				"Height", "GeneID", "Xref", "Color"};
		
		return new JTable(data, cols);
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
		width += dx;
		updateJdomGraphics();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#resizeY(double)
	 */
	protected void resizeY(double dy)
	{
		height 	-= dy;
		updateJdomGraphics();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#updateFromPropertyTable(javax.swing.JTable)
	 */
	protected void updateFromPropertyTable(JTable t)
	{			
		centerx		= Double.parseDouble(t.getValueAt(0, 0).toString());
		centery		= Double.parseDouble(t.getValueAt(0, 1).toString());
		width		= Double.parseDouble(t.getValueAt(0, 2).toString());
		height		= Double.parseDouble(t.getValueAt(0, 3).toString());
		geneLabel		= t.getValueAt(0, 4).toString();
		xref		= t.getValueAt(0, 5).toString();
		color 		= GmmlColorConvertor.string2Color(t.getValueAt(0, 6).toString());
		
		updateJdomGraphics();
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
					case 0: // CenterX
						this.centerx = Integer.parseInt(value) / GmmlData.GMMLZOOM ; break;
					case 1: // CenterY
						this.centery = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 2: // Width
						this.width = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 3:	// Height
						this.height = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 4: // GeneLabel
						this.geneLabel = value; break;
					case 5: // Xref
						this.xref = value; break;
					case 6: // Color
						this.color = GmmlColorConvertor.string2Color(value); break;
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