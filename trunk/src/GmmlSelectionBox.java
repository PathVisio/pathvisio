import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*;

/**
 * This class implements a selectionbox 
 */ 
class GmmlSelectionBox extends GmmlGraphicsUtils
{
	private static final long serialVersionUID = 1L;

	int x1, y1, x2, y2;
	
	GmmlDrawing canvas;
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this selectionbox will be part of
	 */
	public GmmlSelectionBox(GmmlDrawing canvas)
	{
		this.canvas = canvas;
		canvas.addElement(this);
		resetRectangle();
	}	
	
	/**
	 * resets the selectionbox rectangle position to the upper 
	 * left corner of the screen
	 */
	public void resetRectangle()
	{
		x1 = 0;
		y1 = 0;
		x2 = 0;
		y2 = 0;
	}
	
	public Rectangle2D.Double getRectangle() {
		double width = x2-x1;
		double height = y2-y1;
		double x = x1;
		double y = y1;
		
		if(width < 0)
		{
			width = -width;
			x = x - width;
		}
		if(height < 0)
		{
			height = -height;
			y = y - height;
		}
		return new Rectangle2D.Double(x,y,width,height);
	}
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e)
	{
		
		if(canvas.isSelecting)
		{
			e.gc.setForeground (e.display.getSystemColor (SWT.COLOR_BLACK));
			e.gc.setBackground (e.display.getSystemColor (SWT.COLOR_BLACK));
			e.gc.setLineStyle (SWT.LINE_DOT);
			e.gc.drawRectangle (x1, y1, x2-x1, y2-y1);

		}
	}
	
	protected boolean intersects(Rectangle2D.Double r)
	{	
		return true;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlDrawingObject#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D point)
	{
		return false;
	}
	
	protected Rectangle getBounds()
	{
		return getRectangle().getBounds();
	}
	
	protected ArrayList getSideAreas()
	{
		int w = 4;
		ArrayList rl = new ArrayList();
		Rectangle r = getRectangle().getBounds();
		rl.add(new Rectangle(r.x - w/2, r.y - w/2, r.width + w, w));
		rl.add(new Rectangle(r.x + r.width - w/2, r.y - w/2, w, r.height + w/2));
		rl.add(new Rectangle(r.x - w/2, r.y + r.height - w/2, r.width + w, w));
		rl.add(new Rectangle(r.x - w/2, r.y + w/2, w, r.height + w));
		return rl;
	} 
} // end of class