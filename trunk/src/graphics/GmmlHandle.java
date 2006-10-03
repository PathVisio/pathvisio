package graphics;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;

import util.LinAlg;
import util.LinAlg.Point;

/**
 * This class implements and handles handles for 
 * objects on the drawing which are used to 
 * resize them or change their location.
 */
class GmmlHandle extends GmmlDrawingObject
{
	private static final long serialVersionUID = 1L;
	
	/** 
	 * because isSelected really doesn't make sense for GmmlHandles, 
	 * I added this variable isVisible. It should be set automatically by its parent
	 * through calls of show() and hide()
	 */
	private boolean isVisible = false;
	
	//The direction this handle is allowed to move in
	int direction;
	public static final int DIRECTION_FREE = 0;
	public static final int DIRECTION_X	 = 1;
	public static final int DIRECTION_Y  = 2; 
	public static final int DIRECTION_ROT = 3;
	public static final int DIRECTION_XY = 4;
	public static final int DIRECTION_MINXY = 5;
	
	public static final int WIDTH 	= 8;
	public static final int HEIGHT	= 8;
	
	GmmlDrawingObject parent;
	
	double centerx;
	double centery;
	
	double rotation;
	
	boolean visible;
	
	/**
	 * Constructor for this class, creates a handle given the parent, direction and canvas
	 * @param direction	Direction this handle can be moved in (one of DIRECTION_*)
	 * @param parent	The object this handle belongs to
	 * @param canvas	The {@link GmmlDrawing} to draw this handle on
	 */
	public GmmlHandle(int direction, GmmlDrawingObject parent, GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_HANDLE;
		
		this.direction = direction;
		this.parent = parent;
	}
	
	/**
	 * Get the direction this handle is allowed to move in
	 * @return one of DIRECTION_*
	 */
	public int getDirection() { return direction; }
	
	public void setDirection(int direction) { this.direction = direction; }
	
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
	
	/**
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
	
	/**
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
		if (!isVisible) return;
		
		if(direction == DIRECTION_ROT) {
			buffer.setLineWidth (1);
			buffer.setLineStyle(SWT.LINE_SOLID);
			buffer.setBackground (e.display.getSystemColor (SWT.COLOR_GREEN));
			buffer.setForeground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.fillOval(
					(int)(centerx - WIDTH/2), 
					(int)(centery - HEIGHT/2), 
					(int)WIDTH, 
					(int)HEIGHT);
			buffer.drawOval(
					(int)(centerx - WIDTH/2), 
					(int)(centery - HEIGHT/2), 
					(int)WIDTH, 
					(int)HEIGHT);
		} else {			
			buffer.setLineWidth (1);
			buffer.setLineStyle(SWT.LINE_SOLID);
			buffer.setBackground (e.display.getSystemColor (SWT.COLOR_YELLOW));
			buffer.setForeground (e.display.getSystemColor (SWT.COLOR_BLACK));
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
		return getOutline().contains(p);
	}
		
	/**
	 * Moves this handle by the specified increments and
	 * adjusts the {@link GmmlDrawingObject} to the new position
	 */
	public void moveBy(double dx, double dy)
	{	
		markDirty();

		if(direction != DIRECTION_FREE && direction != DIRECTION_ROT) {
			Point v = new Point(0,0);
			double xtraRot = 0;
			if		(direction == DIRECTION_X) {
				v = new Point(1,0);
			}
			else if	(direction == DIRECTION_Y) {
				v = new Point(0,1);
			}
			else if (direction == DIRECTION_XY) {
				Rectangle b = parent.getBounds();
				v = new Point(b.width + 1, b.height + 1);
			}
			else if (direction == DIRECTION_MINXY) {
				xtraRot = Math.PI/2;
				Rectangle b = parent.getBounds();
				v = new Point(b.height + 1, b.width + 1);
			}
			Point yr = LinAlg.rotate(v, rotation + xtraRot);
			Point prj = LinAlg.project(new Point(dx, dy), yr);
			dx = prj.x; dy= prj.y;
		}
		
		centerx += dx;
		centery += dy;
		
		parent.adjustToHandle(this);
		markDirty();
	}
	
	protected boolean intersects(Rectangle2D.Double r)
	{	
		return getOutline().intersects(r);
	}
	
	protected Rectangle getBounds()
	{
		
		return getOutline().getBounds();
	}
	
	public Shape getOutline() {
		return constructRectangle();
	}
	
	private Rectangle2D.Double constructRectangle()
	{
		return new Rectangle2D.Double(centerx - WIDTH/2, centery - HEIGHT/2, WIDTH, HEIGHT);
	}
	
	public String toString() { 
		return 	"Handle with parent: " + parent.toString() +
		" and direction " + direction; 
	}
	
	public void adjustToZoom(double factor) { }
	
} // end of class


