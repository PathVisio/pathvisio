package graphics;

import gmmlVision.GmmlVision;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
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
 * This class implements a brace and provides 
 * methods to resize and draw it
 */
public class GmmlBrace extends GmmlGraphics
{
	private static final long serialVersionUID = 1L;
	
	public static final int INITIAL_PPO = 10;
	public static final int ORIENTATION_TOP		= 0;
	public static final int ORIENTATION_RIGHT	= 1;
	public static final int ORIENTATION_BOTTOM	= 2;
	public static final int ORIENTATION_LEFT	= 3;
	
	public final List attributes = Arrays.asList(new String[] {
			"CenterX", "CenterY", "Width", "PicPointOffset", "Orientation", "Color",
			"Notes"
	});

	double centerx;
	double centery;
	double width;
	double ppo;
	
	int orientation; //orientation: 0=top, 1=right, 2=bottom, 3=left
	RGB color;
	
	String notes = "";
	
	Element jdomElement;
	
	GmmlHandle handlecenter;
	GmmlHandle handlewidth;
	
	// Some mappings to Gmml
	private final List orientationMappings = Arrays.asList(new String[] {
			"top", "right", "bottom", "left"
	});
		
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this brace will be part of
	 */
	public GmmlBrace(GmmlDrawing canvas)
	{
		drawingOrder = GmmlDrawing.DRAW_ORDER_BRACE;
		
		this.canvas = canvas;

		handlecenter = new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this, canvas);
		handlewidth	= new GmmlHandle(GmmlHandle.HANDLETYPE_WIDTH, this, canvas);		
		canvas.addElement(handlecenter);
		canvas.addElement(handlewidth);
	}
	
	/**
	 * Constructor for this class
	 * @param centerX - center x coordinate
	 * @param centerY - center y coordinate
	 * @param width - width
	 * @param ppo - picpoint ofset
	 * @param orientation - orientation (0 for top, 1 for right, 2 for bottom, 3 for left)
	 * @param color - the color this brace will be painted
	 * @param canvas - the GmmlDrawing this brace will be part of
	 */
	public GmmlBrace(double centerX, double centerY, double width, double ppo, int orientation, RGB color, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.centerx = centerX;
		this.centery = centerY;
		this.width = width;
		this.ppo = ppo;
		this.orientation = orientation;
		this.color = color;

		setHandleLocation();
		
		createJdomElement(doc);

	}

	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlBrace
	 * @param canvas - the GmmlDrawing this GmmlBrace will be part of
	 */
	public GmmlBrace(Element e, GmmlDrawing canvas) {
		this(canvas);
		
		this.jdomElement = e;
		
		mapAttributes(e);
	}

	/**
	 * Sets the brace at the location specified
	 * @param centerX - the x coordinate
	 * @param centerY - the y coordinate
	 */
	public void setLocation(double centerX, double centerY)
	{
		this.centerx = centerX;
		this.centery = centerY;
		
		
	}
	
	/**
	 * Updates the JDom representation of this arc
	 */
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("Notes", notes);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("CenterX", Integer.toString((int)centerx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Integer.toString((int)centery * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString((int)width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("PicPointOffset", Double.toString(ppo));
				jdomGraphics.setAttribute("Orientation", (String)orientationMappings.get(orientation));
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
			}
		}
	}
	
	protected void createJdomElement(Document doc) {
		if(jdomElement == null) {
			jdomElement = new Element("Brace");
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
		centerx	*= factor;
		centery	*= factor;
		width	*= factor;
		ppo		*= factor;
	}

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
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
		buffer.setLineWidth (2);
		
		int cx = (int)centerx;
		int cy = (int)centery;
		int w = (int)width;
		int d = (int)ppo;

		if (orientation == ORIENTATION_TOP)
		{
			buffer.drawLine (cx + d/2, cy, cx + w/2 - d/2, cy); //line on the right
			buffer.drawLine (cx - d/2, cy, cx - w/2 + d/2, cy); //line on the left
			
			buffer.drawArc (cx - w/2, cy, d, d, -180, -90); //arc on the left
			buffer.drawArc (cx - d, cy - d,	d, d, -90, 90); //left arc in the middle
			buffer.drawArc (cx, cy - d, d, d, -90, -90); //right arc in the middle
			buffer.drawArc (cx + w/2 - d, cy, d, d, 0, 90); //arc on the right
		}
		
		else if (orientation == ORIENTATION_RIGHT)
		{
			buffer.drawLine (cx, cy + d/2, cx, cy + w/2 - d/2); //line on the bottom
			buffer.drawLine (cx, cy - d/2, cx, cy - w/2 + d/2); //line on the top
			
			buffer.drawArc (cx - d,cy - w/2, d, d, 0, 90); //arc on the top
			buffer.drawArc (cx, cy - d, d, d, -90, -90); //upper arc in the middle
			buffer.drawArc (cx, cy, d, d, 90, 90); //lower arc in the middle
			buffer.drawArc (cx - d, cy + w/2 - d, d, d, 0, -90); //arc on the bottom

		}
		
		else if (orientation == ORIENTATION_BOTTOM)
		{ 
			buffer.drawLine (cx + d/2, cy, cx + w/2 - d/2, cy); //line on the right
			buffer.drawLine (cx - d/2, cy, cx - w/2 + d/2, cy); //line on the left
			
			buffer.drawArc (cx - w/2, cy - d, d, d, -180, 90); //arc on the left
			buffer.drawArc (cx - d, cy, d, d, 90, -90); //left arc in the middle
			buffer.drawArc (cx, cy, d, d, 90, 90); //right arc in the middle
			buffer.drawArc (cx + w/2 - d, cy - d, d, d, 0, -90); //arc on the right

		}
		
		else if (orientation == ORIENTATION_LEFT)
		{
			buffer.drawLine (cx, cy + d/2, cx, cy + w/2 - d/2); //line on the bottom
			buffer.drawLine (cx, cy - d/2, cx, cy - w/2 + d/2); //line on the top
			
			buffer.drawArc (cx, cy - w/2, d, d, -180, -90); //arc on the top
			buffer.drawArc (cx - d, cy - d, d, d, -90, 90); //upper arc in the middle
			buffer.drawArc (cx - d, cy, d, d, 90, -90); //lower arc in the middle
			buffer.drawArc (cx, cy + w/2 - d, d, d, -90, -90); //arc on the bottom

		}
		
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
		Shape outline = getOutline();
		
		return outline.contains(p);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#intersects(java.awt.geom.Rectangle2D.Double)
	 */
	protected boolean intersects(Rectangle2D.Double r)
	{
		Shape outline = getOutline();
		return outline.intersects(r.x, r.y, r.width, r.height);
	}
	
	protected Rectangle getBounds()
	{
		return getOutline().getBounds();
	}
	
	protected Shape getOutline()
	{
		Line2D l = new Line2D.Double();
		if (orientation == ORIENTATION_TOP)
		{
			l = new Line2D.Double(centerx - width/2, centery, centerx + width/2, centery);
		}
		else if (orientation == ORIENTATION_RIGHT)
		{
			l = new Line2D.Double(centerx, centery - width/2, centerx, centery + width/2);
		}
		else if (orientation == ORIENTATION_BOTTOM)
		{
			l = new Line2D.Double(centerx - width/2, centery, centerx + width/2, centery);
		}
		else if (orientation == ORIENTATION_LEFT)
		{
			l = new Line2D.Double(centerx, centery - width/2, centerx, centery + width/2);
		}
		BasicStroke stroke = new BasicStroke(10);
		return stroke.createStrokedShape(l);
	}
	
	public Vector<GmmlHandle> getHandles()
	{
		Vector<GmmlHandle> v = new Vector<GmmlHandle>();
		v.add(handlecenter);
		v.add(handlewidth);
		return v;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#moveBy(double, double)
	 */
	protected void moveBy(double dx, double dy)
	{
		markDirty();
		setLocation(centerx + dx, centery + dy);
		markDirty();
		setHandleLocation();
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
	protected void resizeY(double dy){}
	
	public List getAttributes() {
		return attributes;
	}
	
	public void updateToPropItems()
	{
		if (propItems == null)
		{
			propItems = new Hashtable();
		}
		
		Object[] values = new Object[] {centerx, centery,
				width, ppo, orientation, color, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		markDirty();	
		
		centerx		= (Double)propItems.get(attributes.get(0));
		centery		= (Double)propItems.get(attributes.get(1));
		width		= (Double)propItems.get(attributes.get(2));
		ppo			= (Double)propItems.get(attributes.get(3));
		orientation	= (Integer)propItems.get(attributes.get(4));
		color 		= (RGB)propItems.get(attributes.get(5));
		notes		= (String)propItems.get(attributes.get(6));
		
		markDirty();
		setHandleLocation();
		canvas.redrawDirtyRect();
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
						this.centerx = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 1: // CenterY
						this.centery = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 2: // Width
						this.width = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 3: // PicPointOffset
						this.ppo = Double.parseDouble(value) / GmmlData.GMMLZOOM; break;
					case 4: // Orientation
						if(orientationMappings.indexOf(value) > -1)
							this.orientation = orientationMappings.indexOf(value);
						break;
					case 5: // Color
						this.color = ColorConverter.gmmlString2Color(value); break;
					case 6: // Notes
						this.notes = value;
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
	
	private void setHandleLocation()
	{
		handlecenter.setLocation(centerx, centery);
		if (orientation == ORIENTATION_TOP)
		{
			handlewidth.setLocation(centerx + width/2, centery);
		}
		else if (orientation == ORIENTATION_RIGHT)
		{
			handlewidth.setLocation(centerx, centery + width/2);
		}
		else if (orientation == ORIENTATION_BOTTOM)
		{
			handlewidth.setLocation(centerx + width/2, centery);	
		}
		else if (orientation == ORIENTATION_LEFT)
		{
			handlewidth.setLocation(centerx, centery + width/2);
		}
	}
	
	
} //end of GmmlBrace