package graphics;

import gmmlVision.GmmlVision;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
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
 * This class implements an arc and provides 
 * methods to resize and draw it
 */
public class GmmlArc extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;

	public final List attributes = Arrays.asList(
		new String[] {
			"StartX", "StartY", "Width",
			"Height","Color","Rotation", "Notes"
		});
	
	String notes = "";
	RGB color;
				
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this arc will be part of
	 */
	public GmmlArc(GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_ARC;
	}

	/**
	 * Constructor for this class
	 * @param startx	- the arcs upper left x coordinate 	
	 * @param starty - the arcs upper left y coordinate
	 * @param width - the arcs widht
	 * @param height - the arcs height
	 * @param color - the color the arc will be painted
	 * @param rotation - the angle at which the arc has to be rotated when drawing it
	 * @param canvas - the GmmlDrawing this arc will be part of
	 */
	public GmmlArc(double startx, double starty, double width, double height, RGB color, double rotation, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.startX 	= startx;
		this.startY 	= starty;
		setGmmlWidth(width);
		setGmmlHeight(height);
		this.color 		= color;
		this.rotation 	= rotation;
				
		setHandleLocation();
		createJdomElement(doc);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlArc
	 * @param canvas - the GmmlDrawing this GmmlArc will be part of
	 */
	public GmmlArc(Element e, GmmlDrawing canvas) {
		this(canvas);
		this.jdomElement = e;
		
		mapAttributes(e);
				
		setHandleLocation();
	}
	
	/**
	 * Updates the JDom representation of this arc
	 */
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("Notes", notes);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics != null) {
				jdomGraphics.setAttribute("StartX", Integer.toString((int)startX * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("StartY", Integer.toString((int)startY * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString(getGmmlWidth()));
				jdomGraphics.setAttribute("Height", Integer.toString(getGmmlHeight()));
				jdomGraphics.setAttribute("Rotation", Double.toString(rotation));
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
			}
		}
	}

	protected void createJdomElement(Document doc) {
		if(jdomElement == null) {
			jdomElement = new Element("Arc");
			jdomElement.addContent(new Element("Graphics"));
			
			doc.getRootElement().addContent(jdomElement);
		}
	}
	
	protected void adjustToZoom(double factor)
	{
		startX	*= factor;
		startY	*= factor;
		width	*= factor;
		height	*= factor;
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
		buffer.setForeground (c);
		buffer.setLineStyle (SWT.LINE_SOLID);
		buffer.setLineWidth (2);
		
		Transform tr = new Transform(e.display);
		rotateGC(buffer, tr);
		
		buffer.drawArc((int)(startX), (int)(startY),
			(int)(width), (int)(height),
			 0, -180
		);
		
		buffer.setTransform(null);
		
		tr.dispose();
		c.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	protected Rectangle rotate(Rectangle r, double angle) {
		AffineTransform t = AffineTransform.getRotateInstance(angle);
		return (Rectangle)t.createTransformedShape(r);
	}
	
	public void negativeHeight(GmmlHandle h) {
		height = -height;
		rotation += Math.PI;
		if(!(h == handleN || h == handleS)) {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_X);
			canvas.setPressedObject(h);
		}
	}
	
	protected Arc2D.Double getArc() {
		return new Arc2D.Double(startX-width, startY-height, 2*width, 2*height, 180 - (rotation * 180 / Math.PI), 180, 0);
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
		
		Object[] values = new Object[] {startX, startY,
				width, height, color, rotation, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();
		
		startX		= (Double)propItems.get(attributes.get(0));
		startY		= (Double)propItems.get(attributes.get(1));
		width		= (Double)propItems.get(attributes.get(2));
		height		= (Double)propItems.get(attributes.get(3));
		color 		= (RGB)propItems.get(attributes.get(4));
		rotation	= (Double)propItems.get(attributes.get(5));
		notes		= (String)propItems.get(attributes.get(6));
		
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
					case 0: // StartX
						this.startX = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 1: // StartY
						this.startY = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 2: // Width
						setGmmlWidth(Integer.parseInt(value) / GmmlData.GMMLZOOM); break;
					case 3: // Height
						setGmmlHeight(Integer.parseInt(value) / GmmlData.GMMLZOOM); break;
					case 4: // Color
						this.color = ColorConverter.gmmlString2Color(value); break;
					case 5: // Rotation
						this.rotation = Double.parseDouble(value); 
						break;
					case 6: // Notes
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

} //end of GmmlArc
