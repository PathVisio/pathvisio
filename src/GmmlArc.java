import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.jdom.Attribute;
import org.jdom.Element;

import java.awt.geom.Point2D;
import java.awt.BasicStroke;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*;

import javax.swing.JTable;

/**
 * This class implements an arc and provides 
 * methods to resize and draw it
 */
public class GmmlArc extends GmmlGraphics
{
	private static final long serialVersionUID = 1L;

	public final List attributes = Arrays.asList(new String[] {
			"StartX", "StartY", "Width",
			"Height","Color","Rotation"
	});
	
	double startx;
	double starty;
	double width;
	double height;
	double rotation;
	
	RGB color;
	GmmlDrawing canvas;
	
	Element jdomElement;
	
	GmmlHandle handlecenter	= new GmmlHandle(GmmlHandle.HANDLETYPE_CENTER, this);
	GmmlHandle handlex		= new GmmlHandle(GmmlHandle.HANDLETYPE_WIDTH, this);
	GmmlHandle handley		= new GmmlHandle(GmmlHandle.HANDLETYPE_HEIGHT, this);
		
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this arc will be part of
	 */
	public GmmlArc(GmmlDrawing canvas)
	{
		this.canvas = canvas;
		
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
	public GmmlArc(double startx, double starty, double width, double height, RGB color, double rotation, GmmlDrawing canvas)
	{
		this.startx 	= startx;
		this.starty 	= starty;
		this.width 		= width;
		this.height		= height;
		this.color 		= color;
		this.rotation 	= Math.toDegrees(rotation);
		this.canvas 	= canvas;
				
		setHandleLocation();
		
		canvas.addElement(handlecenter);
		canvas.addElement(handlex);
		canvas.addElement(handley);
	}
	
	/**
	 * Constructor for mapping a JDOM Element.
	 * @param e	- the GMML element which will be loaded as a GmmlArc
	 * @param canvas - the GmmlDrawing this GmmlArc will be part of
	 */
	public GmmlArc(Element e, GmmlDrawing canvas) {
		// List the attributes

		mapAttributes(e);
				
		this.canvas = canvas;
		
		setHandleLocation();
		
		canvas.addElement(handlecenter);
		canvas.addElement(handlex);
		canvas.addElement(handley);
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
		
		updateJdomGraphics();
	}

	/**
	 * Updates the JDom representation of this arc
	 */
	public void updateJdomGraphics() {
		if(jdomElement != null) {
			Element jdomGraphics = jdomElement.getChild("Graphics");
			if(jdomGraphics !=null) {
				jdomGraphics.setAttribute("StartX", Integer.toString((int)startx * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("StartY", Integer.toString((int)starty * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Width", Integer.toString((int)width * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Integer.toString((int)height * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Rotation", Double.toString(rotation));
			}
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
		
		updateJdomGraphics();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e)
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
		e.gc.setLineStyle (SWT.LINE_SOLID);
		e.gc.setLineWidth (2);
		
		e.gc.drawArc((int)(startx-width), (int)(starty-height),
			(int)(2*width), (int)(2*height),
			(int)(180-rotation), 180
		);
		
		setHandleLocation();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D p)
	{
		Arc2D arc = new Arc2D.Double(startx-width, starty-height, 2*width, 2*height, 180-rotation, 180, 0);

		isSelected =  arc.contains(p);
		return isSelected;
	}

	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#intersects(java.awt.geom.Rectangle2D.Double)
	 */
	protected boolean intersects(Rectangle2D.Double r)
	{
		Arc2D arc = new Arc2D.Double(startx-width, starty-height, 2*width, 2*height, 180-rotation, 180, 0);

		isSelected = arc.intersects(r.x, r.y, r.width, r.height);
		return isSelected;
	
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#getPropertyTable()
	 */
	protected JTable getPropertyTable()
	{
		Object[][] data = new Object[][] {{new Double(startx), new Double(starty),
				new Double(width), new Double(height), color, new Double(rotation)}};
		
		Object[] cols = new Object[] {"Start X", "Start Y", "Width", "Height", 
				"Color", "Rotation"};
		
		return new JTable(data, cols);
	}
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#moveBy(double, double)
	 */
	protected void moveBy(double dx, double dy)
	{
		setLocation(startx + dx, starty + dy);
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
		height += dy;
		updateJdomGraphics();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlGraphics#updateFromPropertyTable(javax.swing.JTable)
	 */
	protected void updateFromPropertyTable(JTable t)
	{
		startx		= Double.parseDouble(t.getValueAt(0, 0).toString());
		starty		= Double.parseDouble(t.getValueAt(0, 1).toString());
		width		= Double.parseDouble(t.getValueAt(0, 2).toString());
		height		= Double.parseDouble(t.getValueAt(0, 3).toString());
		color 		= GmmlColorConvertor.string2Color(t.getValueAt(0, 4).toString());
		rotation	= Double.parseDouble(t.getValueAt(0, 5).toString());
		
		updateJdomGraphics();
	}

	/**
	 * Maps attributes to internal variables.
	 * @param e - the element to map to a GmmlArc
	 */
	private void mapAttributes (Element e) {
		this.jdomElement = e;
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
					case 2: // Width
						this.width = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 3: // Height
						this.height = Integer.parseInt(value) / GmmlData.GMMLZOOM; break;
					case 4: // Color
						this.color = GmmlColorConvertor.string2Color(value); break;
					case 5: // Rotation
						this.rotation = Double.parseDouble(value); break;
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

} //end of GmmlArc
