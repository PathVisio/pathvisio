package colorSet;
import graphics.GmmlGeneProduct;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

public class GmmlColorSet {
	public final static RGB COLOR_NO_CRITERIA_MET = new RGB(128, 128, 128);
	
	public String name;
	
	public Vector colorSetObjects;
	
	public GmmlColorSet(String name)
	{
		this.name = name;
		colorSetObjects = new Vector();
	}
	
	public Vector getColorSetObjects()
	{
		return colorSetObjects;
	}
	
	public void addObject(GmmlColorSetObject o)
	{
		colorSetObjects.add(o);
	}
	
	public Object getParent()
	{
		return null;
	}
	
	public RGB getColor(HashMap data)
	{
		RGB rgb = COLOR_NO_CRITERIA_MET;
		Iterator it = colorSetObjects.iterator();
		while(it.hasNext())
		{
			GmmlColorSetObject gc = (GmmlColorSetObject)it.next();
			System.out.println("Getting color for " + gc.name);
			RGB gcRgb = gc.getColor(data);
			if(gcRgb != null)
			{
				return gcRgb;
			}
		}
		return rgb;
	}
}
