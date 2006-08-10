package graphics;

import gmmlVision.GmmlVision;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import util.ColorConverter;
import util.SwtUtils;
import data.GmmlData;

/**
 * This class represents a GMMLShape, which can be a 
 * rectangle or ellips, depending of its type.
 */
public class GmmlShape extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;

	public static final int TYPE_RECTANGLE	= 0;
	public static final int TYPE_OVAL 		= 1;
	
	public final List attributes = Arrays.asList(new String[] {
			"CenterX", "CenterY", "Width", "Height", 
			"Type","Color","Rotation", "Notes"
	});
	
	public static final List typeMappings = Arrays.asList(new String[] {
			"Rectangle","Oval"
	});
		
	String notes = "";
	int type;
	RGB color;
		
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this GmmlShape will be part of
	 */
	public GmmlShape(GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_SHAPE;
	}
		
	/**
	 * Constructor for this class
	 * @param x - the upper left corner x coordinate
	 * @param y - the upper left corner y coordinate
	 * @param width - the width
	 * @param height - the height
	 * @param type - this shapes type (0 for rectangle, 1 for ellipse)
	 * @param color - the color this geneproduct will be painted
	 * @param canvas - the GmmlDrawing this geneproduct will be part of
	 */
	public GmmlShape(double x, double y, double width, double height, int type, RGB color, double rotation, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		centerX = x;
		centerY = y;
		setGmmlWidth(width);
		setGmmlHeight(height);
		this.color 		= color;
		this.type 		= type;
		this.rotation 	= rotation;

		calcStart(x, y);
		setHandleLocation();
		createJdomElement(doc);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlShape
	 * @param canvas - the GmmlDrawing this GmmlShape will be part of
	 */
	public GmmlShape(Element e, GmmlDrawing canvas) {
		this(canvas);
		
		jdomElement = e;
		mapAttributes(e);
		
		calcStart();
		setHandleLocation();
	}
			
	/**
	 * Updates the JDom representation of the GMML file. 
	 */
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("Type", (String)typeMappings.get(type));
			jdomElement.setAttribute("Notes", notes);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("CenterX", Integer.toString(getCenterX() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString(getCenterY() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString(getGmmlWidth()));
				jdomGraphics.setAttribute("Height", Integer.toString(getGmmlHeight()));
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
				jdomGraphics.setAttribute("Rotation", Double.toString(rotation));
			}
		}
	}

	protected void createJdomElement(Document doc) {
		if(jdomElement == null) {
			jdomElement = new Element("Shape");
			jdomElement.setAttribute("Type", (String)typeMappings.get(type));
			jdomElement.addContent(new Element("Graphics"));
			
			doc.getRootElement().addContent(jdomElement);
		}
	}
	
	protected void adjustToZoom(double factor)
	{
		startX *= factor;
		startY *= factor;
		width 	*= factor;
		height	*= factor;
		setHandleLocation();
	}
	
	protected void draw(PaintEvent e, GC buffer)
	{	
		Color c = null;
		if (isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
		}
		else 
		{
			c = SwtUtils.changeColor(c, this.color, e.display);
		}
		buffer.setForeground (c);
		buffer.setLineStyle (SWT.LINE_SOLID);
		buffer.setLineWidth (1);
		
		Transform tr = new Transform(e.display);
		rotateGC(buffer, tr);
		
		if (type == TYPE_RECTANGLE)
		{
			buffer.drawRectangle (
				(int)(startX),
				(int)(startY),
				(int)(width),
				(int)(height)
			);
		}
		else if (type == TYPE_OVAL)
		{
			buffer.drawOval (
				(int)(startX), 
				(int)(startY),
				(int)(width), 
				(int)(height)
			);
		}	
		
		buffer.setTransform(null);
		
		c.dispose();
		tr.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
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
		
		Object[] values = new Object[] {(double)getCenterX(), (double)getCenterY(),
				width, height, type, color, rotation, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();
		centerX = ((Double)propItems.get(attributes.get(0)));
		centerY = ((Double)propItems.get(attributes.get(1)));
		width		= (Double)propItems.get(attributes.get(2));
		height		= (Double)propItems.get(attributes.get(3));
		type		= (Integer)propItems.get(attributes.get(4));
		color 		= (RGB)propItems.get(attributes.get(5));
		rotation	= (Double)propItems.get(attributes.get(6));
		notes		= (String)propItems.get(attributes.get(7));	
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
					case 0: // CenterX
						centerX = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 1: // CenterY
						centerY = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 2: // Width
						setGmmlWidth(Double.parseDouble(value) / GmmlData.GMMLZOOM); break;
					case 3: // Height
						setGmmlHeight(Double.parseDouble(value) / GmmlData.GMMLZOOM); break;
					case 4: // Type
						if(typeMappings.indexOf(value) > -1)
							this.type = typeMappings.indexOf(value);
						break;
					case 5: // Color
						this.color = ColorConverter.gmmlString2Color(value); break;
					case 6: // Rotation
						this.rotation = Double.parseDouble(value); break;
					case 7: // Notes
						this.notes = value; break;
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