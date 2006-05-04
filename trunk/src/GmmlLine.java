import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Polygon;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
 
/**
 * This class implements and handles a line
 */
public class GmmlLine extends GmmlGraphics
{
	private static final long serialVersionUID = 1L;
	
	public static final int STYLE_SOLID		= 0;
	public static final int STYLE_DASHED	= 1;
	
	public static final int TYPE_LINE	= 0;
	public static final int TYPE_ARROW	= 1;
	
	public final List attributes = Arrays.asList(new String[] {
			"StartX", "StartY", "EndX", "EndY", "Color", "Style", "Type", "Notes"
	});
	
	double startx;
	double starty;
	double endx;
	double endy;
	double mx;
	double my;
	
	int style;
	int type;
	
	RGB color;
	
	String notes = "";
	
	GmmlDrawing canvas;
	Line2D line;
	
	Element jdomElement;
	
	GmmlHandle handlecenter	= new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this);
	GmmlHandle handleStart	= new GmmlHandle(GmmlHandle.HANDLETYPE_LINE_START, this);
	GmmlHandle handleEnd	= new GmmlHandle(GmmlHandle.HANDLETYPE_LINE_END, this);
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this line will be part of
	 */
	public GmmlLine(GmmlDrawing canvas)
	{
		this.canvas = canvas;
		
		canvas.addElement(handlecenter);
		canvas.addElement(handleStart);
		canvas.addElement(handleEnd);
	}
	
	/**
	 * Constructor for this class
	 * @param startx - start x coordinate
	 * @param starty - start y coordinate
	 * @param endx - end x coordinate
	 * @param endy - end y coordinate
	 * @param color - color this line will be painted
	 * @param canvas - the GmmlDrawing this line will be part of
	 */
	public GmmlLine(double startx, double starty, double endx, double endy, RGB color, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.startx = startx;
		this.starty = starty;
		this.endx 	= endx;
		this.endy 	= endy;
		
		this.color = color;
		
		line = new Line2D.Double(startx, starty, endx, endy);

		setHandleLocation();
		
		createJdomElement(doc);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlLine
	 * @param canvas - the GmmlDrawing this GmmlLine will be part of
	 */
	public GmmlLine (Element e, GmmlDrawing canvas) {
		this(canvas);
		
		this.jdomElement = e;

		mapAttributes(e);
		
		line = new Line2D.Double(startx, starty, endx, endy);
		
		setHandleLocation();
	}

	/**
	 * Constructs the internal line in this class
	 */
	public void constructLine()
	{
		line = new Line2D.Double(startx, starty, endx, endy);
	}
	
	/**
	 * Sets the line start and end to the coordinates specified
	 * <DL><B>Parameters</B>
	 * <DD>Double x1	- new startx 
	 * <DD>Double y1	- new starty
	 * <DD>Double x2	- new endx
	 * <DD>Double y2	- new endy
	 */
	public void setLine(double x1, double y1, double x2, double y2)
	{
		startx = x1;
		starty = y1;
		endx   = x2;
		endy   = y2;
		
		constructLine();
		
	}

	/**
	 * Sets the line start and en to the points specified
	 * <DL><B>Parameters</B>
	 * <DD>Point2D start	- new start point 
	 * <DD>Point2D end		- new end point
	 * <DL>
	 */
	public void setLine(Point2D start, Point2D end)
	{
		startx = start.getX();
		starty = start.getY();
		endx   = end.getX();
		endy   = end.getY();
		
		constructLine();
	}

	/**
	 * Updates the JDom representation of this label
	 */	
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("Notes", notes);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics != null) {
				jdomGraphics.setAttribute("StartX", Integer.toString((int)startx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("StartY", Integer.toString((int)starty * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("EndX", Integer.toString((int)endx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("EndY", Integer.toString((int)endy * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Color", GmmlColorConvertor.color2String(color));
			}
		}
	}
	
	protected void createJdomElement(Document doc) {
		if(jdomElement == null) {
			jdomElement = new Element("Line");
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
		endx 	*= factor;
		endy	*= factor;
		
		constructLine();
	}

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e)
	{
		if(line!=null)
		{
			Color c;
			if (isSelected)
			{
				c = new Color (e.display, 255, 0, 0);
			}
			else 
			{
				c = new Color (e.display, this.color);
			}
			e.gc.setForeground (c);
			e.gc.setBackground (c);
			
			if (style == STYLE_SOLID)
			{
				e.gc.setLineStyle (SWT.LINE_SOLID);
			}
			else if (style == STYLE_DASHED)
			{ 
				e.gc.setLineStyle (SWT.LINE_DASH);
			}
			
			e.gc.drawLine ((int)line.getX1(), (int)line.getY1(), (int)line.getX2(), (int)line.getY2());
			
			if (type == TYPE_ARROW)
			{
				drawArrowhead(e);
			}
			setHandleLocation();
			c.dispose();
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D p)
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(line);
		return outline.contains(p);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#intersects(java.awt.geom.Rectangle2D.Double)
	 */
	protected boolean intersects(Rectangle2D.Double r)
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(line);
		
		return outline.intersects(r);
	}
	
	protected Rectangle getBounds()
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(line);
		return outline.getBounds();
	}
	
	/*
 	 *  (non-Javadoc)
 	 * @see GmmlGraphics#moveBy(double, double)
 	 */
	protected void moveBy(double dx, double dy)
	{
		setLine(startx + dx, starty + dy, endx + dx, endy + dy);
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#moveLineStart(double, double)
	 */
	protected void moveLineStart(double dx, double dy)
	{
		startx += dx;
		starty += dy;
		constructLine();
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#moveLineEnd(double, double)
	 */
	protected void moveLineEnd(double dx, double dy)
	{
		endx += dx;
		endy += dy;
		constructLine();
		
	}
	
	/**
	 * If the line type is arrow, this method draws the arrowhead
	 */
	private void drawArrowhead(PaintEvent e) //TODO! clean up this mess.....
	{
		double angle = 25.0;
		double theta = Math.toRadians(180 - angle);
		double[] rot = new double[2];
		double[] p = new double[2];
		double[] q = new double[2];
		double a, b, norm;
		
		rot[0] = Math.cos(theta);
		rot[1] = Math.sin(theta);
		
		e.gc.setLineStyle (SWT.LINE_SOLID);
		
		a = endx-startx;
		b = endy-starty;
		norm = 8/(Math.sqrt((a*a)+(b*b)));				
		p[0] = ( a*rot[0] + b*rot[1] ) * norm + endx;
		p[1] = (-a*rot[1] + b*rot[0] ) * norm + endy;
		q[0] = ( a*rot[0] - b*rot[1] ) * norm + endx;
		q[1] = ( a*rot[1] + b*rot[0] ) * norm + endy;
		int[] points = {
			(int)endx, (int)endy,
			(int)(p[0]), (int)(p[1]),
			(int)(q[0]), (int)(q[1])
		};
		
		e.gc.drawPolygon (points);
		e.gc.fillPolygon (points);
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
					case 0: // StartX
						this.startx = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 1: // StartY
						this.starty = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 2: // EndX
						this.endx = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 3: // EndY
						this.endy = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 4: // Color
						this.color = GmmlColorConvertor.string2Color(value); break;
					case 5: // Style
						List styleMappings = Arrays.asList(new String[] {
								"Solid", "Broken"
						});
						if(styleMappings.indexOf(value) > -1)
							this.type = styleMappings.indexOf(value);
						break;
					case 6: // Type
						List typeMappings = Arrays.asList(new String[] {
								"Line", "Arrow"
						});
						if(typeMappings.indexOf(value) > -1)
							this.type = typeMappings.indexOf(value);
						break;
					case 7: //Notes
						this.notes = value; break;
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
				 endx, endy, color, style, type, notes};
		
		for (int i = 0; i < attributes.size(); i++)
		{
			propItems.put(attributes.get(i), values[i]);
		}
	}
	
	public void updateFromPropItems()
	{
		Rectangle rp = getBounds();
		
		startx		= (Double)propItems.get(attributes.get(0));
		starty		= (Double)propItems.get(attributes.get(1));
		endx		= (Double)propItems.get(attributes.get(2));
		endy		= (Double)propItems.get(attributes.get(3));
		color 		= (RGB)propItems.get(attributes.get(4));
		style		= (Integer)propItems.get(attributes.get(5));
		type		= (Integer)propItems.get(attributes.get(6));
		notes		= (String)propItems.get(attributes.get(7));
		
		constructLine();
		
		Rectangle r = getBounds();
		r.add(rp);
		r.grow(5,5);
		canvas.redraw(r.x, r.y, r.width, r.height, false);
	}

	/**
	 * Sets the handles in this class at the correct location
	 */
	private void setHandleLocation()
	{
		handlecenter.setLocation((startx + endx)/2, (starty + endy)/2);
		handleStart.setLocation(startx, starty);
		handleEnd.setLocation(endx, endy);
	}

} // end of classdsw
