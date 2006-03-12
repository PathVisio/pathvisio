//~ import java.awt.Graphics;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import java.awt.geom.Point2D;

import javax.swing.JComponent;


//~ abstract class GmmlDrawingObject extends JComponent 
abstract class GmmlDrawingObject
{
	boolean isSelected;
	
	/**
	 * Draws the GmmlDrawingObject object on the GmmlDrawing
	 * it is part of
	 * @param g - the Graphics object to use for drawing
	 */
	abstract void draw(PaintEvent e);
	
	/**
	 * Determines wheter a GmmlGraphics object contains
	 * the point specified
	 * @param point - the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	abstract boolean isContain(Point2D point);
	
	/**
	 * Moves GmmlGraphics object by specified increments
	 * @param dx - the value of x-increment
	 * @param dy - the value of y-increment
	 */
	void moveBy(double dx, double dy){}
	
}
