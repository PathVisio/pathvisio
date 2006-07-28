package graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;

/**
 * This class implements and handles handles for 
 * other GmmlGraphics objects, which are used to 
 * resize them or change their location.
 */
class GmmlHandle extends GmmlDrawingObject
{
	private static final long serialVersionUID = 1L;
	
	/** because isSelected really doesn't make sense for GmmlHandles, 
	 * I added this variable isVisible. It should be set automatically by its parent
	 * through calls of show() and hide()
	 */
	private boolean isVisible = false;
	
	int type = 0;
	// possible types:
	public static final int HANDLETYPE_CENTER		= 0;
	public static final int HANDLETYPE_WIDTH		= 1;
	public static final int HANDLETYPE_HEIGHT		= 2;
	public static final int HANDLETYPE_LINE_START	= 3;
	public static final int HANDLETYPE_LINE_END		= 4;
	
	public static final int WIDTH 	= 8;
	public static final int HEIGHT	= 8;
	
	GmmlGraphics parent;
	
	double centerx;
	double centery;
		
	Rectangle2D rect;
	boolean visible;
	
	public GmmlHandle(int type, GmmlGraphics parent, GmmlDrawing canvas)
	{
		drawingOrder = GmmlDrawing.DRAW_ORDER_HANDLE;
		
		this.type = type;
		this.parent = parent;
		this.canvas = canvas;

		constructRectangle();
	}
	
	public Point2D getCenterPoint()
	{
		Point2D p = new Point2D.Double(centerx, centery);
		return p;
	}

	public void setLocation(double x, double y)
	{
		markDirty();
		centerx = x;
		centery = y;
		markDirty();
	}
	
	/**
	 * returns the visibility of this handle
	 * @see hide(), show()
	 */
	public boolean isVisible()
	{
		return isVisible;
	}
	
	/*
	 * call show() to cause this handle to show up and mark its area dirty 
	 * A handle should show itself only if it's parent object is active / selected
	 * @see hide(), isvisible()
	 */
	public void show()
	{
		if (!isVisible)
		{
			isVisible = true;
			markDirty();
		}
	}
	
	/*
	 * hide handle, and also mark its area dirty
	 * @see show(), isvisible()
	 */
	public void hide()
	{
		if (isVisible)
		{
			isVisible = false;
			markDirty();
		}
	}
	
	/**
	 * draws itself, but only if isVisible() is true, there is 
	 * no need for a check for isVisible() before calling draw().
	 */
	protected void draw(PaintEvent e, GC buffer)
	{
		if (isVisible)
		{
			constructRectangle();
			buffer.setLineWidth (1);
			buffer.setLineStyle(SWT.LINE_SOLID);
			buffer.setBackground (e.display.getSystemColor (SWT.COLOR_YELLOW));
			buffer.setForeground (e.display.getSystemColor (SWT.COLOR_BLUE));
			buffer.fillRectangle (
					(int)(centerx - WIDTH/2), 
					(int)(centery - HEIGHT/2), 
					(int)WIDTH, 
					(int)HEIGHT);	
			buffer.drawRectangle (
				(int)(centerx - WIDTH/2), 
				(int)(centery - HEIGHT/2), 
				(int)WIDTH, 
				(int)HEIGHT);		
		}
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}

	protected boolean isContain(Point2D p)
	{
		return rect.contains(p);
	}
	
	/**
	 * Unlike moveBy methods of most other GmmlDrawingObjects, not the object itself
	 * but its parent is moved, and the movement may affect position, width or height of the
	 * parent object depending on the handle type.
	 */
	protected void moveBy(double dx, double dy)
	{
		if (type == HANDLETYPE_CENTER)
		{
			parent.moveBy(dx, dy);
		}
		if (type == HANDLETYPE_WIDTH)
		{
			parent.resizeX(dx);
		}
		if (type == HANDLETYPE_HEIGHT)
		{
			parent.resizeY(dy);
		}
		if (type == HANDLETYPE_LINE_START)
		{
			parent.moveLineStart(dx, dy);
		}
		if (type == HANDLETYPE_LINE_END)
		{
			parent.moveLineEnd(dx, dy);
		}
	}
	
	// TODO: Check this method, it doesn't make much sense ~ Martijn 
	protected boolean intersects(Rectangle2D.Double r)
	{	
		return parent.isSelected();
	}
	
	protected Rectangle getBounds()
	{
		constructRectangle();
		rect.add(parent.getBounds());
		return rect.getBounds();
	}
	
	private void constructRectangle()
	{
		rect = new Rectangle2D.Double(centerx - WIDTH/2, centery - HEIGHT/2, WIDTH, HEIGHT);
	}
	
	public String toString() { return "Handle with parent: " + parent.toString(); }

} // end of class


