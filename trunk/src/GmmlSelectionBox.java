import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	
	Rectangle2D.Double r;

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
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e)
	{
		
		if(canvas.isSelecting)
		{
			e.gc.setForeground (e.display.getSystemColor (SWT.COLOR_BLUE));
			e.gc.setBackground (e.display.getSystemColor (SWT.COLOR_BLUE));
			e.gc.setLineStyle (SWT.LINE_DASH);
			e.gc.drawRectangle (x1, y1, x2-x1, y2-y1);

		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlDrawingObject#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D point)
	{
		return false;
	}
	    
} // end of class