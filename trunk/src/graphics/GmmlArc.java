package graphics;

import gmmlVision.GmmlVision;

import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
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
public class GmmlArc extends GmmlGraphics
{
	private static final long serialVersionUID = 1L;

	public final List attributes = Arrays.asList(
		new String[] {
			"StartX", "StartY", "Width",
			"Height","Color","Rotation", "Notes"
		});
	
	double startx;
	double starty;
	double width;
	double height;
	double rotation;
	
	String notes = "";
	
	RGB color;
	
	Element jdomElement;
	
	GmmlHandle handlecenter;
	GmmlHandle handlex;
	GmmlHandle handley;
		
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this arc will be part of
	 */
	public GmmlArc(GmmlDrawing canvas)
	{
		drawingOrder = GmmlDrawing.DRAW_ORDER_ARC;
		
		this.canvas = canvas;
		handlecenter	= new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this, canvas);
		handlex		= new GmmlHandle(GmmlHandle.HANDLETYPE_WIDTH, this, canvas);
		handley		= new GmmlHandle(GmmlHandle.HANDLETYPE_HEIGHT, this, canvas);
		
		canvas.addElement(handlecenter);
		canvas.addElement(handlex);
		canvas.addElement(handley);
	}

	/**
	 * Constructor for this class
	 * @param x	- the arcs upper left x coordinate 	
	 * @param y - the arcs upper left y coordinate
	 * @param width - the arcs widht
	 * @param height - the arcs height
	 * @param color - the color the arc will be painted
	 * @param rotation - the angle at which the arc has to be rotated when drawing it
	 * @param canvas - the GmmlDrawing this arc will be part of
	 */
	public GmmlArc(double startx, double starty, double width, double height, RGB color, double rotation, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.startx 	= startx;
		this.starty 	= starty;
		this.width 		= width;
		this.height		= height;
		this.color 		= color;
		this.rotation 	= Math.toDegrees(rotation);
				
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
	
	public Vector<GmmlHandle> getHandles()
	{
		Vector<GmmlHandle> v = new Vector<GmmlHandle>();
		v.add(handlecenter);
		v.add(handlex);
		v.add(handley);
		return v;
	}
	
	/**
	 * Sets this class's handles at the correct location
	 */
	public void setHandleLocation()
	{
		handlecenter.setLocation(startx - width, starty + height);
		handlex.setLocation(startx + width, starty - height/2);
		handley.setLocation(startx + width/2, starty + height);
	}

	/**
	 * Sets the location of this arc to the coordinate specified
	 * @param x - new x coordinate
	 * @param y - new y coordinate
	 */
	public void setLocation(double x, double y)
	{
		this.startx = x;
		this.starty = y;
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
				jdomGraphics.setAttribute("StartX", Integer.toString((int)startx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("StartY", Integer.toString((int)starty * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString((int)width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Integer.toString((int)height * GmmlData.GMMLZOOM));
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
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#adjustToZoom()
	 */
	protected void adjustToZoom(double factor)
	{
		startx	*= factor;
		starty	*= factor;
		width	*= factor;
		height	*= factor;
		setHandleLocation();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
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
		
		buffer.drawArc((int)(startx-width), (int)(starty-height),
			(int)(2*width), (int)(2*height),
			(int)(180 - (rotation * 180 / Math.PI)), 180
		);
		
		c.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D p)
	{
		Arc2D arc = new Arc2D.Double(startx-width, starty-height, 2*width, 2*height, 180 - (rotation * 180 / Math.PI), 180, 0);

		return arc.contains(p);
	}

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#intersects(java.awt.geom.Rectangle2D.Double)
	 */
	protected boolean intersects(Rectangle2D.Double r)
	{
		Arc2D arc = new Arc2D.Double(startx-width, starty-height, 2*width, 2*height, 180 - (rotation * 180 / Math.PI), 180, 0);

		return arc.intersects(r.x, r.y, r.width, r.height);
	
	}
	
	protected Rectangle getBounds()
	{
		Arc2D arc = new Arc2D.Double(startx-width, starty-height, 2*width, 2*height, 180 - (rotation * 180 / Math.PI), 180, 0);
		return arc.getBounds();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#moveBy(double, double)
	 */
	protected void moveBy(double dx, double dy)
	{
		markDirty();
		setLocation(startx + dx, starty + dy);
		markDirty();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#resizeX(double)
	 */
	protected void resizeX(double dx)
	{
		markDirty();
		width = Math.abs(width + dx);
		markDirty();
		setHandleLocation();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#resizeY(double)
	 */
	protected void resizeY(double dy)
	{
		markDirty();
		height = Math.abs(height + dy);
		markDirty();
		setHandleLocation();		
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
		
		Object[] values = new Object[] {startx, starty,
				width, height, color, rotation, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();
		
		startx		= (Double)propItems.get(attributes.get(0));
		starty		= (Double)propItems.get(attributes.get(1));
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
						this.startx = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 1: // StartY
						this.starty = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 2: // Width
						this.width = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 3: // Height
						this.height = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
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
