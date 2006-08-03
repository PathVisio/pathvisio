package colorSet;
import gmmlVision.GmmlVision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

import preferences.GmmlPreferences;
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

	public RGB color_no_criteria_met = GmmlPreferences.getColorProperty("colors.no_criteria_met");
	public RGB color_no_gene_found = GmmlPreferences.getColorProperty("colors.no_gene_found");
	public RGB color_no_data_found = GmmlPreferences.getColorProperty("colors.no_data_found");
	
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
	 */
	public GmmlColorSet(String name)
	{
		this.name = name;
		colorSetObjects = new Vector<GmmlColorSetObject>();
		useSamples = new ArrayList<Sample>();
		sampleTypes = new ArrayList<Integer>();
	}
	
	/**
	 * Constructor of this class
	 * @param name			name of the colorset
	 * @param criterion		string containing information to generate the colorset as stored
	 * in the expression database
	 */
	public GmmlColorSet(String name, String criterion)
	{
		this(name);
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
		// color_no_criteria_met | color_no_gene_found | useSamples | sampleTypes | color_no_data_found 
		// r, g, b			     | r, g, b		       | s1,...,sn  | t1,...,tn | r, g, b	
		String sep = "|";
		StringBuilder criterion = new StringBuilder();
		criterion.append(
				getColorString(color_no_criteria_met) + sep +
				getColorString(color_no_gene_found) + sep +
				getArrayListString(useSamples) + sep +
				getArrayListString(sampleTypes) + sep +
				getColorString(color_no_data_found)
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
		String[] s = criterion.split("\\|", 5);
		try
		{
			color_no_criteria_met = parseColorString(s[0]);
			color_no_gene_found= parseColorString(s[1]);
			
			if(s.length > 2)
			{
				useSamples = parseSampleArrayList(s[2]);
				sampleTypes = parseIntegerArrayList(s[3]);
				color_no_data_found = parseColorString(s[4]);
			}
		}
		catch (Exception e)
		{
			GmmlVision.log.error("Unable to parse colorset data stored in " +
					"expression database: " + criterion, e);
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
			GmmlVision.log.error("Unable to parse color '" + colorString + 
					"'stored in expression database", e);
			return new RGB(0,0,0);
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
				a.add(GmmlGex.getSamples().get(Integer.parseInt(s[i])));
			} catch (Exception e) { 
				GmmlVision.log.error("Unable to parse arraylist as stored in " +
						"expression database: " + arrayListString, e);
			}
		}
		return a;
	}
}
