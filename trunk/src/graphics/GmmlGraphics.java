package graphics;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.jdom.Element;

import preferences.GmmlPreferences;


/**
 * This class is a parent class for all graphics
 * that can be added to a GmmlDrawing.
 */
public abstract class GmmlGraphics extends GmmlDrawingObject
{
	public static RGB selectColor = GmmlPreferences.getColorProperty("colors.selectColor");
	public static RGB highlightColor = GmmlPreferences.getColorProperty("colors.highlightColor");
	
	Element jdomElement;
	
	public GmmlGraphics(GmmlDrawing canvas) {
		super(canvas);
	}
	
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
	
	//Methods dealing with property table
	public Hashtable propItems;
	
	abstract void updateToPropItems();
	
	public abstract void updateFromPropItems();
	
	//Methods dealing with the GMML representation
	abstract void updateJdomElement();
	
	public abstract List getAttributes();
	
	public List attributes;
	
}