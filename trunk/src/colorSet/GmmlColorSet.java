package colorSet;
import graphics.GmmlGeneProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

import data.GmmlGex;
import data.GmmlGex.Sample;

public class GmmlColorSet {
	public static String[] SAMPLE_TYPES = {"undefined", "transcriptomics", "proteomics", "p-value"};
	public static int SAMPLE_TYPE_UNDEF = 0;
	public static int SAMPLE_TYPE_TRANS = 1;
	public static int SAMPLE_TYPE_PROT	= 2;
	public static int SAMPLE_TYPE_PVALUE= 3;
	
	public static RGB COLOR_NO_CRITERIA_MET = new RGB(200, 200, 200);
	public static RGB COLOR_NO_GENE_FOUND = new RGB(255, 255, 255);
	public RGB color_no_criteria_met = COLOR_NO_CRITERIA_MET;
	public RGB color_gene_not_found = COLOR_NO_GENE_FOUND;
	
	public String name;
	
	public Vector colorSetObjects;
	
	public GmmlGex gmmlGex;
	
	public ArrayList<Sample> useSamples;	
	public ArrayList<Integer> sampleTypes;
	
	public GmmlColorSet(String name, GmmlGex parent)
	{
		this.name = name;
		gmmlGex = parent;
		colorSetObjects = new Vector();
		useSamples = new ArrayList<Sample>();
		sampleTypes = new ArrayList<Integer>();
	}
	
	public GmmlColorSet(String name, String criterion, GmmlGex parent)
	{
		this(name, parent);
		parseCriterionString(criterion);
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
	
	public RGB getColor(HashMap data, int sampleId)
	{
		RGB rgb = color_no_criteria_met;
		Iterator it = colorSetObjects.iterator();
		while(it.hasNext())
		{
			GmmlColorSetObject gc = (GmmlColorSetObject)it.next();
			RGB gcRgb = gc.getColor(data, sampleId);
			if(gcRgb != null)
			{
				return gcRgb;
			}
		}
		return rgb;
	}
	
	public String getCriterionString()
	{
		// color_no_criteria_met | color_gene_not_found | useSamples | sampleTypes
		// r, g, b			     | r, g, b		        | s1,...,sn  | t1,...,tn
		String sep = "|";
		StringBuilder criterion = new StringBuilder();
		criterion.append(
				getColorString(color_no_criteria_met) + sep +
				getColorString(color_gene_not_found) + sep +
				getArrayListString(useSamples) + sep +
				getArrayListString(sampleTypes)
				);
		return criterion.toString();
	}
	
	void parseCriterionString(String criterion)
	{
		String[] s = criterion.split("\\|");
//		System.out.println(criterion);
//		System.out.println(s[0] + "," + s[1] + "," + s[2] + "," + s[3]);
		try
		{
			color_no_criteria_met = parseColorString(s[0]);
			color_gene_not_found= parseColorString(s[1]);
			if(s.length > 2)
			{
				useSamples = parseSampleArrayList(s[2]);
				sampleTypes = parseIntegerArrayList(s[3]);
			}
//			System.out.println(color_no_criteria_met + "," + color_gene_not_found + "," + useSamples + "," +
//					sampleTypes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String getColorString(RGB rgb)
	{
		return rgb.red + "," + rgb.green + "," + rgb.blue;
	}
	
	public RGB parseColorString(String colorString)
	{
		String[] s = colorString.split(",");
		try 
		{
			return new RGB(
					Integer.parseInt(s[0]), 
					Integer.parseInt(s[1]), 
					Integer.parseInt(s[2]));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public String getArrayListString(ArrayList a)
	{
		if(a.size() > 0)
		{
			StringBuilder s = new StringBuilder();
			for(Object o : a)
			{
				s.append(o.toString() + ",");
			}
			return s.toString().substring(0, s.lastIndexOf(","));
		}
		return "";
	}
	
	public ArrayList<Integer> parseIntegerArrayList(String arrayListString)
	{
		String[] s = arrayListString.split(",");
		ArrayList<Integer> a = new ArrayList<Integer>();
		for(int i = 0; i < s.length; i++)
		{
			a.add(Integer.parseInt(s[i]));
		}
		return a;
	}
	
	public ArrayList<Sample> parseSampleArrayList(String arrayListString)
	{
		String[] s = arrayListString.split(",");
		ArrayList<Sample> a = new ArrayList<Sample>();
		for(int i = 0; i < s.length; i++)
		{
			a.add(gmmlGex.samples.get(Integer.parseInt(s[i])));
		}
		return a;
	}
}
