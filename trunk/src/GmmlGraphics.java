import java.awt.geom.Rectangle2D;

import javax.swing.JTable;


/**
 * This class is a parent class for all graphics
 * that can be added to a GmmlDrawing.
 */
abstract class GmmlGraphics extends GmmlDrawingObject
{
	
	
	/**
	 * Resizes GmmlGraphics in x-direction
	 * @param dx - the value with wich to resize the object
	 */
	void resizeX(double dx){}
	
	/**
	 * Resizes GmmlGraphics in y-direction
	 * @param dx - the value with wich to resize the object
	 */
	void resizeY(double dy){}
	
	/**
	 * Moves the start of a line by numbers specified
	 * @param dx - the value of x-increment
	 * @param dy - the value of y-increment
	 */
	void moveLineStart(double dx, double dy){}
	
	/**
	 * Moves the start of a line by numbers specified
	 * @param dx - the value of x-increment
	 * @param dy - the value of y-increment
	 */	
	void moveLineEnd(double dx, double dy){}
	
	/**
	 * Adjusts the GmmlGraphics object to the zoom
	 * specified in the drawing it is part of
	 * @param factor - the factor to scale the objects coordinates and measures with
	 */
	abstract void adjustToZoom(double factor);

	/**
	 * Updates GmmlGraphics object properties from the 
	 * table specified.
	 * @param t - the table to get the properties from
	 */
	abstract void updateFromPropertyTable(JTable t);	
	
	/**
	 * Determines whether a GmmlGraphics object intersects 
	 * the rectangle specified
	 * @param r - the rectangle to check
	 * @return True if the object intersects the rectangle, false otherwise
	 */
	abstract boolean intersects(Rectangle2D.Double r);
	
	/**
	 * Gets the GmmlGraphics object properties and returns them
	 * in a table
	 * @return a table containing the objects properties
	 */
	abstract JTable getPropertyTable();
	
}