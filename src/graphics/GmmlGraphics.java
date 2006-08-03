package graphics;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

import preferences.GmmlPreferences;


/**
 * This class is a parent class for all graphics
 * that can be added to a GmmlDrawing.
 */
public abstract class GmmlGraphics extends GmmlDrawingObject
{
	public static RGB selectColor = GmmlPreferences.getColorProperty("colors.selectColor");
	public static RGB highlightColor = GmmlPreferences.getColorProperty("colors.highlightColor");
		
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
	
	public void select()
	{
		super.select();
		for (GmmlHandle h : getHandles())
		{
			h.show();
		}
	}
	
	public void deselect()
	{
		super.deselect();
		for (GmmlHandle h : getHandles())
		{
			h.hide();
		}
	}
	
	abstract Vector<GmmlHandle> getHandles();
	
	abstract void updateToPropItems();
	
	public abstract void updateFromPropItems();
	
	abstract void updateJdomElement();
	
	public abstract List getAttributes();
	
	public List attributes;
	public Hashtable propItems;
	
}