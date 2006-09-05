package colorSet;
import java.util.HashMap;

import org.eclipse.swt.graphics.RGB;

/**
 * This class represent any object that can be present in a colorset
 * e.g. a gradient or boolean expression.
 */
public abstract class GmmlColorSetObject {
	
	/**
	 * The parent colorset, that this colorSetObject is a part of.
	 */
	private GmmlColorSet parent;
	
	/**
	 * The display name of this colorSetObject
	 */
	private String name;
	
	/**
	 * getter for name, the name of this colorSetObject
	 */
	public void setName(String _name) 
	{
		this.name = _name; 
	}
	
	/**
	 * setter for name, the name of this colorSetObject
	 */
	public String getName() { return name; }
	
	/**
	 * Apply to all samples
	 */
	public static final int USE_SAMPLE_ALL = -1;
	/**
	 * Apply to no samples (initial value)
	 */
	public static final int USE_SAMPLE_NO = -2;
	
	/**
	 * Sample to use for this color gradient (index of element in 
	 * {@GmmlColorSet.useSamples}, DATA_COL_ALL or DATA_COL_NO
	 */
	public int useSample;
	
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * @param name 			name of the gradient
	 */
	public GmmlColorSetObject(GmmlColorSet parent, String name) 
	{	
		this.parent = parent;
		this.name = name;
		useSample = USE_SAMPLE_NO;
	}
	
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * @param name			name of the gradient	
	 * @param criterion		string containing information to generate the gradient as stored
	 * in the expression database
	 */
	public GmmlColorSetObject(GmmlColorSet parent, String name, String criterion)
	{ 
		this(parent, name);
		parseCriterionString(criterion);
	}
	
	/**
	 * get the color defined by the colorset object for the given data
	 * @param data {@link HashMap}<Integer, Object> containing data (String or double) for every sampleId 
	 * @param sample id of the sample that is visualized using this color
	 * @return {@link RGB} with the color returned by the colorset object after evaluating the input data,
	 * null if the input data doesn't result in a valid color
	 */
	abstract RGB getColor(HashMap<Integer, Object> data, int idSample);
	
	/**
	 * Returns the parent colorset
	 * @return
	 */
	public GmmlColorSet getParent()
	{
		return parent;
	}
	
	/**
	 * Parses a string containing the colorset object information
	 * @param criterion string containing the colorset object information as stored in the 
	 * expression database
	 */
	abstract void parseCriterionString(String criterion);
	
	/**
	 * Create a string containing the information to re-create the colorset object for
	 * storage in the expression database
	 * @return string containing information to re-create the colorset object
	 */
	public abstract String getCriterionString();
	
}
