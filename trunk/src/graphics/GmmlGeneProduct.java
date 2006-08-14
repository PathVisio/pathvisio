package graphics;

import gmmlVision.GmmlVision;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import util.ColorConverter;
import util.SwtUtils;
import data.GmmlData;

/**
 * This class implements a geneproduct and 
 * provides methods to resize and draw it.
 */
public class GmmlGeneProduct extends GmmlGraphicsShape
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
	
	double fontSizeDouble;
	int fontSize;

	RGB color = new RGB (0,0,0);
	RGB fillColor = INITIAL_FILL_COLOR;
		
	String geneID;
	String xref;
	String name = "GeneID";
	String backpageHead = "";
	String type = "unknown";
	String notes = "";
	String geneProductDataSource = "";
		
	GmmlGpColor gpColor;
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this geneproduct will be part of
	 */
	public GmmlGeneProduct(GmmlDrawing canvas)
	{
		super(canvas);
		
		drawingOrder = GmmlDrawing.DRAW_ORDER_GENEPRODUCT;
				
		gpColor = new GmmlGpColor(this);
		
		this.fontSizeDouble = INITIAL_FONTSIZE / canvas.getZoomFactor();
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
		
		centerX = x;
		centerY = y;
		setGmmlWidth(width);
		setGmmlHeight(height);
		this.geneID = geneLabel;
		this.xref = xref;
		this.color = color;

		calcStart();
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
		
		calcStart();
		setHandleLocation();
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
				jdomGraphics.setAttribute("CenterX", Integer.toString(getCenterX() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString(getCenterY() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString((int)width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Integer.toString((int)height * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
			}
		}
	}
	
	/**
	 * Fetches the gene identifier from the Jdom representation
	 */
	public String getId() {
		if(jdomElement != null) {
			return jdomElement.getAttributeValue("Name");
			} else {
			return "";
		}
	}
	
	public void setFontSize(double size) {
		fontSizeDouble = size;
		fontSize = (int)size;
	}
	
	/**
	 * Looks up the systemcode for this gene in {@link GmmlData#sysName2Code}
	 * @param systemName	The system name (as in gmml)
	 * @return	The system code or an empty string if the system is not found
	 */
	public String getSystemCode()
	{
		String systemCode = "";
		if(GmmlData.sysName2Code.containsKey(geneProductDataSource)) 
			systemCode = GmmlData.sysName2Code.get(geneProductDataSource);
		return systemCode;
	}
	
	private Text t;
	public void createTextControl()
	{		
		Color background = canvas.getShell().getDisplay()
		.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		
		Composite textComposite = new Composite(canvas, SWT.NONE);
		textComposite.setLayout(new GridLayout());
		textComposite.setLocation(getCenterX(), getCenterY() - 10);
		textComposite.setBackground(background);
		
		Label label = new Label(textComposite, SWT.CENTER);
		label.setText("Specify gene name:");
		label.setBackground(background);
		t = new Text(textComposite, SWT.SINGLE | SWT.BORDER);
				
		t.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				disposeTextControl();
			}
		});
				
		t.setFocus();
		
		Button b = new Button(textComposite, SWT.PUSH);
		b.setText("OK");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				disposeTextControl();
			}
		});
		
		textComposite.pack();
	}
	
	protected void disposeTextControl()
	{	
		markDirty();
		geneID = t.getText();
		markDirty();
		canvas.updatePropertyTable(this);
		Composite c = t.getParent();
		c.setVisible(false);
		c.dispose();
		
		canvas.redrawDirtyRect();
	}
	
	protected void createJdomElement(Document doc) {
		if(jdomElement == null) {
			jdomElement = new Element("GeneProduct");
			jdomElement.addContent(new Element("Graphics"));
			
			doc.getRootElement().addContent(jdomElement);
		}
	}
		
	public void adjustToZoom(double factor)
	{
		startX	*= factor;
		startY	*= factor;
		width	*= factor;
		height	*= factor;
		fontSizeDouble *= factor;
		fontSize = (int)fontSizeDouble;
		setHandleLocation();
	}

	protected void draw(PaintEvent e, GC buffer)
	{
		Color c = null;
		if(isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
		}
		else 
		{
			c = SwtUtils.changeColor(c, this.color, e.display);
		}
		
		buffer.setForeground(c);
		buffer.setLineStyle (SWT.LINE_SOLID);
		buffer.setLineWidth (1);		
		
		buffer.drawRectangle (
			(int)(startX),
			(int)(startY),
			(int)width,
			(int)height
		);
		
		buffer.setClipping (
				(int)(startX) + 1,
				(int)(startY) + 1,
				(int)width - 1,
				(int)height - 1
			);
		
		gpColor.draw(e, buffer);
		
		Region r = null;
		buffer.setClipping(r);
		
		drawHighlight(e, buffer);
		
		c.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	public void drawHighlight(PaintEvent e, GC buffer)
	{
		if(isHighlighted())
		{
			Color c = null;
			c = SwtUtils.changeColor(c, highlightColor, e.display);
			buffer.setForeground(c);
			buffer.setLineWidth(2);
			buffer.drawRectangle (
					(int)(startX) - 1,
					(int)(startY) - 1,
					(int)width + 3,
					(int)height + 3
				);
			if(c != null) c.dispose();
		}
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
				geneID, (double)getCenterX(), (double)getCenterY(), width, height, color,
				xref, backpageHead, type, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();
		centerX		= (Double)propItems.get(attributes.get(attributes.indexOf("CenterX")));
		centerY		= (Double)propItems.get(attributes.get(attributes.indexOf("CenterY")));
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
		
		calcStart();
		markDirty();
		setHandleLocation();
	}
	
	/**
	 * Maps attributes to internal variables.
	 * @param e - the element to map to a GmmlArc
	 */
	private void mapAttributes (Element e) {
		// Map attributes
		GmmlVision.log.trace("> Mapping element '" + e.getName()+ "'");
		Iterator it = e.getAttributes().iterator();
		while(it.hasNext()) {
			Attribute at = (Attribute)it.next();
			int index = attributes.indexOf(at.getName());
			String value = at.getValue();
			switch(index) {
					case 3:// CenterX
						this.centerX = Integer.parseInt(value) / GmmlData.GMMLZOOM ; break;
					case 4:// CenterY
						this.centerY = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 5:// Width
						setGmmlWidth(Integer.parseInt(value) / GmmlData.GMMLZOOM); break;
					case 6:// Height
						setGmmlHeight(Integer.parseInt(value) / GmmlData.GMMLZOOM); break;
					case 2:// GeneLabel
						this.geneID = value; break;
					case 8:// Xref
						this.xref = value; break;
					case 7:// Color
						this.color = ColorConverter.gmmlString2Color(value); break;
					case 0:// Name
						this.name = value; break;
					case 9:// BackpageHead
						this.backpageHead = value; break;
					case 10: // Type
						this.type = value; break;
					case 11:// Notes
						this.notes = value; break;
					case 1:// GeneProduct-Data-Source
						this.geneProductDataSource = value; 
						break;
					case -1:
						GmmlVision.log.trace("\t> Attribute '" + at.getName() + "' is not recognized");
			}
		}
		
		// Map child's attributes
		it = e.getChildren().iterator();
		while(it.hasNext()) {
			mapAttributes((Element)it.next());
		}
	}
}