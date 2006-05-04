import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
//~ import java.awt.Graphics;
//~ import java.awt.Graphics2D;
//~ import java.awt.Color;
import java.awt.geom.Rectangle2D;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*;

/**
 * This class implements and handles handles for 
 * other GmmlGraphics objects, which are used to 
 * resize them or change their location.
 */
class GmmlHandle extends GmmlDrawingObject
{
	private static final long serialVersionUID = 1L;

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
	
	int type = 0;
	// types:
	// 0: center
	// 1: width
	// 2: height
	// 3: line start 
	
	GmmlDrawing canvas;
	

	
	Rectangle2D rect;
	boolean visible;
	
	public GmmlHandle(int type, GmmlGraphics parent)
	{
		this.type = type;
		this.parent = parent;

		constructRectangle();
	}
	
	public Point2D getCenterPoint()
	{
		Point2D p = new Point2D.Double(centerx, centery);
		return p;
	}

	public void setLocation(double x, double y)
	{
		centerx = x;
		centery = y;
	}

	protected void draw(PaintEvent e)
	{
		if (parent.isSelected)
		{
			constructRectangle();
			e.gc.setLineWidth (1);
			e.gc.setBackground (e.display.getSystemColor (SWT.COLOR_YELLOW));
			e.gc.setForeground (e.display.getSystemColor (SWT.COLOR_BLUE));
			e.gc.fillRectangle (
					(int)(centerx - WIDTH/2), 
					(int)(centery - HEIGHT/2), 
					(int)WIDTH, 
					(int)HEIGHT);	
			e.gc.drawRectangle (
				(int)(centerx - WIDTH/2), 
				(int)(centery - HEIGHT/2), 
				(int)WIDTH, 
				(int)HEIGHT);		
		}
	}

	protected boolean isContain(Point2D p)
	{
		return rect.contains(p);
	}
	
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
	
	protected boolean intersects(Rectangle2D.Double r)
	{	
		return parent.isSelected;
	}
	
	protected Rectangle getBounds()
	{
		return parent.getBounds();
	}
	
	private void constructRectangle()
	{
		rect = new Rectangle2D.Double(centerx - WIDTH/2, centery - HEIGHT/2, WIDTH, HEIGHT);
	}

} // end of class


