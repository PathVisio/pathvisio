package colorSet;
import graphics.GmmlGeneProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

public class GmmlColorSet {
	public static RGB COLOR_NO_CRITERIA_MET = new RGB(200, 200, 200);
	public static RGB COLOR_NO_GENE_FOUND = new RGB(255, 255, 255);
	public RGB color_no_criteria_met = COLOR_NO_CRITERIA_MET;
	public RGB color_gene_not_found = COLOR_NO_GENE_FOUND;
	
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
		RGB rgb = color_no_criteria_met;
		Iterator it = colorSetObjects.iterator();
		while(it.hasNext())
		{
			GmmlColorSetObject gc = (GmmlColorSetObject)it.next();
			RGB gcRgb = gc.getColor(data);
			if(gcRgb != null)
			{
				return gcRgb;
			}
		}
		return rgb;
	}
}
