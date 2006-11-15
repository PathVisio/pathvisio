package graphics;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Region;

import preferences.GmmlPreferences;
import data.GmmlDataObject;
import data.GmmlEvent;
import data.GmmlListener;


/**
 * This class is a parent class for all graphics
 * that can be added to a GmmlDrawing.
 */
public abstract class GmmlGraphics extends GmmlDrawingObject implements GmmlListener
{
	public static RGB selectColor = GmmlPreferences.getColorProperty(GmmlPreferences.PREF_COL_SELECTED);
	public static RGB highlightColor = GmmlPreferences.getColorProperty(GmmlPreferences.PREF_COL_HIGHLIGHTED);
	
	protected GmmlDataObject gdata = null;
	
	public GmmlGraphics(GmmlDrawing canvas, GmmlDataObject o) {
		super(canvas);
		o.addListener(this);
		gdata = o;
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
	
	public GmmlDataObject getGmmlData() {
		return gdata;
	}
	
//	public List getAttributes() { return gdata.getAttributes() ;}
	boolean listen = true;
	public void gmmlObjectModified(GmmlEvent e) {	
		if(listen) markDirty(); // mark everything dirty
	}
	
	public Region createVisualizationRegion() {
		Region region = new Region();
		java.awt.Rectangle r = getBounds();
		region.add(r.x, r.y, r.width, r.height);
		return region;
	}
}