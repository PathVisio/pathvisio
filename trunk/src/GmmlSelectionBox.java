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

	double x;
	double y;

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
	}	
	
	/**
	 * resets the selectionbox rectangle position to the upper 
	 * left corner of the screen
	 */
	public void resetRectangle()
	{
		r = new Rectangle2D.Double(0, 0, 0, 0);
	}

	/**
	 * Resize the selectionbox to the specified widht and height
	 * @param width
	 * @param height
	 */
	public void resize(double width, double height)
	{
		r = new Rectangle2D.Double(x, y, width, height);
	}
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e)
	{

		//~ if(r != null && canvas.isSelecting)
		//~ {
			//~ Graphics2D g2D = (Graphics2D) g;			
				
			//~ setDrawableRectangle();

			//~ g2D.setColor(new Color(0f, 0f, 0.8f, 0.5f));
			//~ g2D.fill(r);
			
			//~ g2D.setStroke(new BasicStroke(2.0f));
			//~ g2D.setColor(new Color(0f, 0f, 0.5f));
			//~ g2D.draw(r);

		//~ }
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlDrawingObject#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D point)
	{
		return false;
	}
	
	/**
	 * Sets the selectionbox rectangle so that it can be drawn
	 */
	private void setDrawableRectangle()
	{
		double width  = r.width;
		double height = r.height;
		
		double x = r.x;
		double y = r.y;
		
		boolean changed = false;
		
      //Make sure rectangle width and height are positive.
      if (width < 0)
      {
	     	changed = true;
	      width = 0 - width;
         x = x - width + 1;
         if (x < 0)
         {
         	width += x;
            x = 0;
      	}
      }
      if (height < 0)
      {
      	changed = true;
	      height = 0 - height;
         y = y - height + 1;
         if (y < 0)
         {
         	height += y;
            y = 0;
         }
      }

      if (changed)
      {
      	r = new Rectangle2D.Double(x, y, width, height);
	   }
	}
    
} // end of class