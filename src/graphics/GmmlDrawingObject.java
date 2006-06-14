package graphics;
//~ import java.awt.Graphics;
import java.awt.Rectangle;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;


//~ abstract class GmmlDrawingObject extends JComponent 
abstract class GmmlDrawingObject implements Comparable
{	
	/** 
	 * mark the area currently occupied by this object for redraw 
	 */
	public void markDirty()
	{
		canvas.addDirtyRect(this);
	}
	
	private boolean isSelected;
	
	/**
	 * Besides setting isSelected, this accomplishes this:
	 * - marking the area dirty, so the object has a chance to redraw itself in selected state
	 */
	public void select()
	{
		if (!isSelected)
		{
			isSelected = true;
			markDirty();			
		}
	}
	
	/**
	 * Besides resetting isSelected, this accomplishes this:
	 * - marking the area dirty, so the object has a chance to redraw itself in unselected state
	 */
	public void deselect()
	{
		if (isSelected)
		{
			isSelected = false;
			markDirty();			
		}
	}
	
	public boolean isSelected()
	{
		return isSelected;
	}
	
	int drawingOrder = GmmlDrawing.DRAW_ORDER_DEFAULT;
	
	protected GmmlDrawing canvas;
	
	/**
	 * Draws the GmmlDrawingObject object on the GmmlDrawing
	 * it is part of
	 * @param g - the Graphics object to use for drawing
	 */
	protected abstract void draw(PaintEvent e, GC buffer);
	protected abstract void draw(PaintEvent e);
	/**
	 * Determines wheter a GmmlGraphics object contains
	 * the point specified
	 * @param point - the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected abstract boolean isContain(Point2D point);
	
	/**
	 * Determines whether a GmmlGraphics object intersects 
	 * the rectangle specified
	 * @param r - the rectangle to check
	 * @return True if the object intersects the rectangle, false otherwise
	 */
	protected abstract boolean intersects(Rectangle2D.Double r);
	
	protected abstract Rectangle getBounds();
	/**
	 * Moves GmmlGraphics object by specified increments
	 * @param dx - the value of x-increment
	 * @param dy - the value of y-increment
	 */
	protected void moveBy(double dx, double dy) {}
	
	/**
	 * Orders GmmlDrawingObjects by their drawingOrder.
	 * The comparison is consistent with "equals", i.e. it doesn't return 0 if
	 * the objects are different, even if their drawing order is the same.
	 * 
	 * @param o
	 * @return
	 * @throws ClassCastException
	 */
	public int compareTo(Object o) throws ClassCastException
	{
		if(!(o instanceof GmmlDrawingObject))
		{
			throw new ClassCastException("Object is not of type GmmlDrawingObject");
		}
		GmmlDrawingObject d = ((GmmlDrawingObject)o);
		
		// same object? easy...
		if (d == this)
			return 0;
		
		int az, bz;
		az = drawingOrder;
		bz = d.drawingOrder;
		
		if(isSelected())
		{
			az = GmmlDrawing.DRAW_ORDER_SELECTED;
		}
		if(d.isSelected())
		{
			bz = GmmlDrawing.DRAW_ORDER_SELECTED;
		}
		
		// note, if the drawing order is equal, that doesn't mean the objects are equal
		// the construct with hashcodes give objects a defined sort order, even if their
		// drawing orders are equal.		
		if (az == bz)
		{
			az = hashCode();
			bz = d.hashCode();		
		}
		// there is still a remote possibility that although the objects are not the same,
		// the hashcode is the same. Even still, we shouldn't return 0.
		if (az != bz) 
			return bz - az; 
		else
			return -1;				
		
	}
	
}
