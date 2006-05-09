package graphics;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTable;

import org.eclipse.swt.widgets.Table;


/**
 * This class is a parent class for all graphics
 * that can be added to a GmmlDrawing.
 */
public abstract class GmmlGraphics extends GmmlDrawingObject
{

	/**
	 * Resizes GmmlGraphics in x-direction
	 * @param dx - the value with wich to resize the object
	 */
	protected void resizeX(double dx){}
	
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

	abstract void updateToPropItems();
	
	public abstract void updateFromPropItems();
	
	abstract void updateJdomElement();
	
	public abstract List getAttributes();
	
	public List attributes;
	public Hashtable propItems;
	
}