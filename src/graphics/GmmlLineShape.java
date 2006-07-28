package graphics;

import gmmlVision.GmmlVision;

import java.awt.Polygon;
import java.awt.Rectangle;
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
 * This class implements a Gmml lineshape and provides 
 * methods to resize and draw it.
 */
public class GmmlLineShape extends GmmlGraphics
{
	private static final long serialVersionUID = 1L;

	public final List attributes = Arrays.asList(new String[] {
			"StartX", "StartY", "EndX", "EndY",	"Type", "Color", "Notes"
	});
	
	public static final int TYPE_TBAR 				= 0;
	public static final int TYPE_RECEPTOR_ROUND		= 1;
	public static final int TYPE_LIGAND_ROUND		= 2;
	public static final int TYPE_RECEPTOR_SQUARE	= 3;
	public static final int TYPE_LIGAND_SQUARE		= 4;
	
	double startx;
	double starty;
	double endx;
	double endy;
	
	int type;
	
	String notes = "";

	RGB color;
	
	private final List typeMappings = Arrays.asList(new String[] {
			"Tbar", "ReceptorRound", "LigandRound", 
			"ReceptorSquare", "LigandSquare"
	});	
	Element jdomElement;

	GmmlHandle handlecenter;
	GmmlHandle handleStart;
	GmmlHandle handleEnd;
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this lineshape will be part of
	 */
	public GmmlLineShape(GmmlDrawing canvas)
	{
		drawingOrder = GmmlDrawing.DRAW_ORDER_LINESHAPE;
		
		this.canvas = canvas;
		handlecenter	= new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this, canvas);
		handleStart	= new GmmlHandle(GmmlHandle.HANDLETYPE_LINE_START, this, canvas);
		handleEnd	= new GmmlHandle(GmmlHandle.HANDLETYPE_LINE_END, this, canvas);
		canvas.addElement(handlecenter);
		canvas.addElement(handleStart);
		canvas.addElement(handleEnd);
	}
	
	/**
	 * Constructor for this class
	 * @param startx - x coordinate of the starting point
	 * @param starty - x coordinate of the starting point
	 * @param end x - x coordinate of the end point 
	 * @param end y - y coordinate of the end point
	 * @param type - this lineshapes type (0 for tbar, 1 for receptor round, 
	 * 2 for ligand round, 3 for receptro square, 4 for ligandsquare)
	 * @param color - the color this lineshape will be painted
	 * @param canvas - the GmmlDrawing this geneproduct will be part of
	 */	
	public GmmlLineShape(double startx, double starty, double endx, double endy, int type, RGB color, GmmlDrawing canvas, Document doc)
	{
		this(canvas);
		
		this.startx = startx;
		this.starty = starty;
		this.endx 	= endx;
		this.endy 	= endy;
		this.type 	= type;
		this.color 	= color;
		
		createJdomElement(doc);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmLineShape
	 * @param canvas - the GmmlDrawing this GmmlLineShape will be part of
	 */
	public GmmlLineShape(Element e, GmmlDrawing canvas) {
		this(canvas);
		
		this.jdomElement = e;
		mapAttributes(e);
	}

	/**
	 * Sets lineshape at the location specified
	 * <BR>
	 * <DL><B>Parameters</B>
	 * <DD>Double x1	- the new start x position
	 * <DD>Double y1	- the new start y position
	 * <DD>Double x2	- the new end x position
	 * <DD>Double y2	- the new end y position
	 * <DL>
	 */	
	public void setLocation(double x1, double y1, double x2, double y2)
	{
		startx = x1;
		starty = y1;
		endx	 = x2;
		endy	 = y2;		
	}
	
	/**
	 * Updates the JDom representation of this lineshape
	 */
	public void updateJdomElement() {
		if(jdomElement != null) {
			jdomElement.setAttribute("Notes", notes);
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
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
			jdomElement = new Element("LineShape");
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
		endx	*= factor;
		endy	*= factor;
		setHandleLocation();
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
		buffer.setBackground (c);
		buffer.setLineStyle (SWT.LINE_SOLID);
		buffer.setLineWidth (1);
			
		double s = Math.sqrt(((endx-startx)*(endx-startx)) + ((endy - starty)*(endy - starty)));
		
		if (type == TYPE_TBAR)
		{
			s /= 8;

			double capx1 = ((-endy + starty)/s) + endx;
			double capy1 = (( endx - startx)/s) + endy;
			double capx2 = (( endy - starty)/s) + endx;
			double capy2 = ((-endx + startx)/s) + endy;

			buffer.drawLine ((int)startx, (int)starty, (int)endx, (int)endy);
			buffer.drawLine ((int)capx1, (int)capy1, (int)capx2, (int)capy2);
		}
		else if (type == TYPE_LIGAND_ROUND)
		{
			double dx = (endx - startx)/s;
			double dy = (endy - starty)/s;
						
			buffer.drawLine ((int)startx, (int)starty, (int)(endx - 6 * dx), (int)(endy - 6 * dy));
			buffer.drawOval ((int)endx - 5, (int)endy - 5, 10, 10);
			buffer.fillOval ((int)endx - 5, (int)endy - 5, 10, 10);
		}
		
		else if (type == TYPE_RECEPTOR_ROUND)
		{
			// TODO: this code is not safe for division by zero!
			double theta 	= Math.toDegrees(Math.atan((endx - startx)/(endy - starty)));
			double dx 		= (endx - startx)/s;
			double dy 		= (endy - starty)/s;	
			
			buffer.drawLine ((int)startx, (int)starty, (int)(endx - (8*dx)), (int)(endy - (8*dy)));
			buffer.drawArc ((int)endx - 8, (int)endy - 8, 16, 16, (int)theta + 180, -180);			
		}
		else if (type == TYPE_RECEPTOR_SQUARE)
		{
			s /= 8;
			
			double x3 		= endx - ((endx - startx)/s);
			double y3 		= endy - ((endy - starty)/s);
			double capx1 	= ((-endy + starty)/s) + x3;
			double capy1 	= (( endx - startx)/s) + y3;
			double capx2 	= (( endy - starty)/s) + x3;
			double capy2 	= ((-endx + startx)/s) + y3;			
			double rx1		= capx1 + 1.5*(endx - startx)/s;
			double ry1 		= capy1 + 1.5*(endy - starty)/s;
			double rx2 		= capx2 + 1.5*(endx - startx)/s;
			double ry2 		= capy2 + 1.5*(endy - starty)/s;
		
			buffer.drawLine ((int)startx, (int)starty, (int)x3, (int)y3);
			buffer.drawLine ((int)capx1, (int)capy1, (int)capx2, (int)capy2);
			buffer.drawLine ((int)capx1, (int)capy1, (int)rx1, (int)ry1);
			buffer.drawLine ((int)capx2, (int)capy2, (int)rx2, (int)ry2);
		}
		else if (type == TYPE_LIGAND_SQUARE)
		{
			s /= 6;
			double x3 		= endx - ((endx - startx)/s);
			double y3 		= endy - ((endy - starty)/s);

			int[] points = new int[4 * 2];
			
			points[0] = (int) (((-endy + starty)/s) + x3);
			points[1] = (int) ((( endx - startx)/s) + y3);
			points[2] = (int) ((( endy - starty)/s) + x3);
			points[3] = (int) (((-endx + startx)/s) + y3);

			points[4] = (int) (points[2] + 1.5*(endx - startx)/s);
			points[5] = (int) (points[3] + 1.5*(endy - starty)/s);
			points[6] = (int) (points[0] + 1.5*(endx - startx)/s);
			points[7] = (int) (points[1] + 1.5*(endy - starty)/s);
			
			buffer.drawLine ((int)startx, (int)starty, (int)x3, (int)y3);
			buffer.drawPolygon(points);
			buffer.fillPolygon(points);
		}
		else
		{
			buffer.drawLine ((int)startx, (int)starty, (int)endx, (int)endy);
		}
		
		c.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D point)
	{
		Polygon p = getOutline();
		return p.contains(point);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#intersects(java.awt.geom.Rectangle2D.Double)
	 */
	protected boolean intersects(Rectangle2D.Double r)
	{			
		Polygon p = getOutline();
		return p.intersects(r.x, r.y, r.width, r.height);
	}
	
	protected Rectangle getBounds()
	{
		return getOutline().getBounds();
	}
	
	protected Polygon getOutline()
	{
		double s  = Math.sqrt(((endx-startx)*(endx-startx)) + ((endy-starty)*(endy-starty)))/8;
		
		int[] x = new int[4];
		int[] y = new int[4];
			
		x[0] = (int)(((-endy + starty)/s) + endx);
		y[0] = (int)((( endx - startx)/s) + endy);
		x[1] = (int)((( endy - starty)/s) + endx);
		y[1] = (int)(((-endx + startx)/s) + endy);
		x[2] = (int)((( endy - starty)/s) + startx);
		y[2] = (int)(((-endx + startx)/s) + starty);
		x[3] = (int)(((-endy + starty)/s) + startx);
		y[3] = (int)((( endx - startx)/s) + starty);
		return new Polygon(x, y, 4);
	}

	public Vector<GmmlHandle> getHandles()
	{
		Vector<GmmlHandle> v = new Vector<GmmlHandle>();
		v.add(handlecenter);
		v.add(handleStart);
		v.add(handleEnd);
		return v;
	}
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#moveBy(double, double)
	 */
	protected void moveBy(double dx, double dy)
	{
		markDirty();
		setLocation(startx + dx, starty + dy, endx + dx, endy + dy);
		markDirty();
		setHandleLocation();
	}
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#moveLineStart(double, double)
	 */
	protected void moveLineStart(double dx, double dy)
	{
		markDirty();
		startx += dx;
		starty += dy;	
//		constructLine();
		markDirty();
		setHandleLocation();
	}
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#moveLineEnd(double, double)
	 */
	protected void moveLineEnd(double dx, double dy)
	{
		markDirty();
		endx += dx;
		endy += dy;	
		markDirty();
//		constructLine();
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
				 endx, endy, type, color, notes};
		
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
		type		= (Integer)propItems.get(attributes.get(4));
		color 		= (RGB)propItems.get(attributes.get(5));
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
					case 2: // EndX
						this.endx = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 3: // EndY
						this.endy = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 4: // Type
						if(typeMappings.indexOf(value) > -1)
							this.type = typeMappings.indexOf(value);
						break;
					case 5: // Color
						this.color = ColorConverter.gmmlString2Color(value); break;
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

	/**
	 * Sets this class handles at the correct position 
	 */
	private void setHandleLocation()
	{
		handlecenter.setLocation((startx + endx)/2, (starty + endy)/2);
		handleStart.setLocation(startx, starty);
		handleEnd.setLocation(endx, endy);
	}
}