package colorSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

import data.GmmlGex;
import data.GmmlGex.Sample;

/**
 * This class represents a colorset, a set of criteria that can be evaluated and 
 * results in a color given a collection of data
 */
public class GmmlColorSet {
	/**
	 * Types of samples that can be specified to choose custom visualization
	 */
	public static String[] SAMPLE_TYPES = {"undefined", "transcriptomics", "proteomics"};
	/**
	 * Index of sample type 'undefined', visualization is filled square
	 */
	public static final int SAMPLE_TYPE_UNDEF = 0;
	/**
	 * Index of sample type 'transcriptomics', visualization is mRNA image
	 */
	public static final int SAMPLE_TYPE_TRANS = 1;
	/**
	 * Index of sample type 'proteomics', visualization is Protein image
	 */
	public static final int SAMPLE_TYPE_PROT	= 2;
	
	/**
	 * Possible ways to display a gene that has multiple data
	 */
	public static String[] MULT_DATA_DISPLAY_METHODS = 
	{"average (and mark gene with red border)", "divide gene in horizontal bars"};
	/**
	 * Field value indicating that multiple data for a gene should be averaged
	 */
	public static final int MULT_DATA_AVG = 0;
	/**
	 * Field value indicating that multiple data for a gene should be displayed in seperate
	 * horizontal bars
	 */
	public static final int MULT_DATA_DIV = 1;
	/**
	 * Standard color for when the colorset returns no valid color (no criteria met)
	 */
	public static RGB COLOR_NO_CRITERIA_MET = new RGB(200, 200, 200);
	/**
	 * Standard color for when the gene is not found in the database or expression data
	 */
	public static RGB COLOR_NO_GENE_FOUND = new RGB(255, 255, 255);
	public RGB color_no_criteria_met = COLOR_NO_CRITERIA_MET;
	public RGB color_gene_not_found = COLOR_NO_GENE_FOUND;
	
	private int multipleDataDisplay;
	/**
	 * Sets how this colorset has to display genes with multiple data values
	 * @param type one of the field constants of this class starting with MULT_DATA
	 */
	public void setMultipleDataDisplay(int type) 
	{ 
		if(type < 0 || type > MULT_DATA_DISPLAY_METHODS.length) return;
		this.multipleDataDisplay = type;
	}
	/**
	 * Get the way this colorset displays genes with multiple data values
	 * @return
	 */
	public int getMultipleDataDisplay() { return multipleDataDisplay; }
	
	public String name;
	
	public Vector<GmmlColorSetObject> colorSetObjects;
	
	public GmmlGex gmmlGex;
	
	/**
	 * Samples selected for visualization
	 */
	public ArrayList<Sample> useSamples;
	/**
	 * Type of samples selected for visualization (one of the SAMPLE_TYPE field constants)
	 */
	public ArrayList<Integer> sampleTypes;
	
	/**
	 * Constructor of this class
	 * @param name		name of the colorset
	 * @param gmmlGex	reference to {@link GmmlGex} object containing the expression data
	 */
	public GmmlColorSet(String name, GmmlGex gmmlGex)
	{
		this.name = name;
		this.gmmlGex = gmmlGex;
		colorSetObjects = new Vector<GmmlColorSetObject>();
		useSamples = new ArrayList<Sample>();
		sampleTypes = new ArrayList<Integer>();
	}
	
	/**
	 * Constructor of this class
	 * @param name			name of the colorset
	 * @param criterion		string containing information to generate the colorset as stored
	 * in the expression database
	 * @param gmmlGex 		reference to {@link GmmlGex} object containing the expression data
	 */
	public GmmlColorSet(String name, String criterion, GmmlGex gmmlGex)
	{
		this(name, gmmlGex);
		parseCriterionString(criterion);
	}
		
	/**
	 * Adds a new {@link GmmlColorSetObject} to this colorset
	 * @param o the {@link GmmlColorSetObject} to add
	 */
	public void addObject(GmmlColorSetObject o)
	{
		colorSetObjects.add(o);
	}
	
	/**
	 * Adds a {@link Sample} to the samples that are used for visualization and
	 * sets type to SAMPLE_TYPE_UNDEF
	 * @param s	{@link Sample} to add
	 */
	public void addUseSample(Sample s)
	{
		useSamples.add(s);
		sampleTypes.add(SAMPLE_TYPE_UNDEF);
	}
	
	/**
	 * Get the color for the given expression data by evaluating all colorset objects
	 * @param data		the expression data to get the color for
	 * @param sampleId	the id of the sample that will be visualized
	 * @return	an {@link RGB} object representing the color for the given data
	 */
	public RGB getColor(HashMap<Integer, Object> data, int sampleId)
	{
		RGB rgb = color_no_criteria_met; //The color to return
		Iterator it = colorSetObjects.iterator();
		//Evaluate all colorset objects, return when a valid color is found
		while(it.hasNext())
		{
			GmmlColorSetObject gc = (GmmlColorSetObject)it.next();
			RGB gcRgb = gc.getColor(data, sampleId);
			System.out.println("Colorset evaluates " + gc + " which returned " + gcRgb);
			if(gcRgb != null)
			{
				return gcRgb;
			}
		}
		return rgb;
	}
	
	/**
	 * Create a string containing the information to re-create the colorset for
	 * storage in the expression database
	 * @return string containing information to re-create the colorset
	 */
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
	
	/**
	 * Parses a string containing the colorset information
	 * @param criterion string containing the colorset information as stored in the 
	 * expression database
	 */
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
	
	/**
	 * Creates a string representing a {@link RGB} object which is parsable by {@link parseColorString}
	 * @param rgb the {@link RGB} object to create a string from
	 * @return the string representing the {@link RGB} object
	 */
	public static String getColorString(RGB rgb)
	{
		return rgb.red + "," + rgb.green + "," + rgb.blue;
	}
	
	/**
	 * Parses a string representing a {@link RGB} object created with {@link getColorString}
	 * @param colorString the string to be parsed
	 * @return the {@link RGB} object this string represented
	 */
	public static RGB parseColorString(String colorString)
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
	
	/**
	 * Creates a string representing a {@link ArrayList} object, parsable by either
	 * {@link parseIntegerArrayList} or {@link parseSampleArrayList}
	 * @param a the {@link ArrayList} to be parsed
	 * @return the string representing the {@link ArrayList}
	 */
	public static String getArrayListString(ArrayList a)
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
	
	/**
	 * Parses a string representing a {@link ArrayList}<Integer> object 
	 * created with {@link getArrayListString}
	 * @param arrayListString the string to be parsed
	 * @return the {@link ArrayList<Integer>} object this string represented
	 */
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
	
	/**
	 * Parses a string representing a {@link ArrayList}<Sample> object 
	 * created with {@link getArrayListString}
	 * @param arrayListString the string to be parsed
	 * @return the {@link ArrayList<Sample>} object this string represented
	 */
	public ArrayList<Sample> parseSampleArrayList(String arrayListString)
	{
		String[] s = arrayListString.split(",");
		ArrayList<Sample> a = new ArrayList<Sample>();
		for(int i = 0; i < s.length; i++)
		{
			try { 
				a.add(gmmlGex.getSamples().get(Integer.parseInt(s[i])));
			} catch (Exception e) { e.printStackTrace();}
		}
		return a;
	}
}
