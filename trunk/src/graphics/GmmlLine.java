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
 * This class implements and handles a line
 */
public class GmmlLine extends GmmlGraphicsLine
{
	private static final long serialVersionUID = 1L;
	
	public static final int STYLE_SOLID		= 0;
	public static final int STYLE_DASHED	= 1;
	
	public static final int TYPE_LINE	= 0;
	public static final int TYPE_ARROW	= 1;
	
	public final List attributes = Arrays.asList(new String[] {
			"StartX", "StartY", "EndX", "EndY", "Color", "Style", "Type", "Notes"
	});
		
	int style;
	int type;
	RGB color;
	String notes = "";
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this line will be part of
	 */
	public GmmlLine(GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_LINE;
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
				
		setHandleLocation();
	}

	/**
	 * Updates the JDom representation of this label
	 */	
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("Notes", notes);
			jdomElement.setAttribute("Type", type == TYPE_LINE ? "Line" : "Arrow");
			jdomElement.setAttribute("Style", style == STYLE_SOLID ? "Solid" : "Broken");
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics != null) {
				jdomGraphics.setAttribute("StartX", Integer.toString((int)startx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("StartY", Integer.toString((int)starty * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("EndX", Integer.toString((int)endx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("EndY", Integer.toString((int)endy * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(color));
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
	
	protected void draw(PaintEvent e, GC buffer)
	{
		Line2D line = getLine();
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
		buffer.setBackground (c);
		
		buffer.setLineWidth (1);
		if (style == STYLE_SOLID)
		{
			buffer.setLineStyle (SWT.LINE_SOLID);
		}
		else if (style == STYLE_DASHED)
		{ 
			buffer.setLineStyle (SWT.LINE_DASH);
		}
		
		buffer.drawLine ((int)line.getX1(), (int)line.getY1(), (int)line.getX2(), (int)line.getY2());
		
		if (type == TYPE_ARROW)
		{
			drawArrowhead(buffer);
		}
		c.dispose();
		
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	protected boolean isContain(Point2D p)
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(getLine());
		return outline.contains(p);
	}

	protected boolean intersects(Rectangle2D.Double r)
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(getLine());
		
		return outline.intersects(r);
	}
	
	protected Rectangle getBounds()
	{
		BasicStroke stroke = new BasicStroke(10);
		Shape outline = stroke.createStrokedShape(getLine());
		return outline.getBounds();
	}
	
	/**
	 * If the line type is arrow, this method draws the arrowhead
	 */
	private void drawArrowhead(GC buffer) //TODO! clean up this mess.....
	{
		double angle = 25.0;
		double theta = Math.toRadians(180 - angle);
		double[] rot = new double[2];
		double[] p = new double[2];
		double[] q = new double[2];
		double a, b, norm;
		
		rot[0] = Math.cos(theta);
		rot[1] = Math.sin(theta);
		
		buffer.setLineStyle (SWT.LINE_SOLID);
		
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
		
		buffer.drawPolygon (points);
		buffer.fillPolygon (points);
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
					case 2: // EndX
						this.endx = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 3: // EndY
						this.endy = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 4: // Color
						this.color = ColorConverter.gmmlString2Color(value); break;
					case 5: // Style
						this.style = value.equalsIgnoreCase("Solid") ? STYLE_SOLID : STYLE_DASHED;
						break;
					case 6: // Type
						this.type = value.equalsIgnoreCase("Line") ? TYPE_LINE : TYPE_ARROW;
						break;
					case 7: //Notes
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
		markDirty();
		
		startx		= (Double)propItems.get(attributes.get(0));
		starty		= (Double)propItems.get(attributes.get(1));
		endx		= (Double)propItems.get(attributes.get(2));
		endy		= (Double)propItems.get(attributes.get(3));
		color 		= (RGB)propItems.get(attributes.get(4));
		style		= (Integer)propItems.get(attributes.get(5));
		type		= (Integer)propItems.get(attributes.get(6));
		notes		= (String)propItems.get(attributes.get(7));
				
		markDirty();
		setHandleLocation();
		
	}
}
