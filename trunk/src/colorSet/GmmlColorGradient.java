package colorSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.swt.graphics.RGB;

/**
 * This class represent a color gradient used for data visualization
 */
public class GmmlColorGradient extends GmmlColorSetObject {
	/**
	 * Apply to all samples
	 */
	public static final int DATA_COL_ALL = -1;
	/**
	 * Apply to no samples (initial value)
	 */
	public static final int DATA_COL_NO = -2;
	/**
	 * Sample to use for this color gradient (index of element in 
	 * {@GmmlColorSet.useSamples}, DATA_COL_ALL or DATA_COL_NO
	 */
	public int useSample;
	/**
	 * Contains the colors and corresponding values used in this gradient as {@link ColorValuePair}
	 */
	public ArrayList<ColorValuePair> colorValuePairs;
	
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * @param name 			name of the gradient
	 */
	public GmmlColorGradient(GmmlColorSet parent, String name)
	{
		super(parent, name);
		colorValuePairs = new ArrayList<ColorValuePair>();
		useSample = DATA_COL_NO;
	}
	
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * @param name			name of the gradient	
	 * @param criterion		string containing information to generate the gradient as stored
	 * in the expression database
	 */
	public GmmlColorGradient(GmmlColorSet parent, String name, String criterion)
	{
		super(parent, name, criterion);
	}
	
	/**
	 * get the color of the gradient for this value
	 * @param value
	 * @return	{@link RGB} containing the color information for the corresponding value
	 * or null if the value does not have a valid color for this gradient
	 */
	public RGB getColor(double value)
	{
		double[] minmax = getMinMax(); //Get the minimum and maximum values of the gradient
		double valueStart = 0;
		double valueEnd = 0;
		RGB colorStart = null;
		RGB colorEnd = null;
		Collections.sort(colorValuePairs);
		//If value is larger/smaller than max/min then set the value to max/min
		//TODO: make this optional
		if(value < minmax[0]) value = minmax[0]; else value = minmax[1];
		
		//Find what colors the value is in between
		for(int i = 0; i < colorValuePairs.size() - 1; i++)
		{
			ColorValuePair cvp = colorValuePairs.get(i);
			ColorValuePair cvpNext = colorValuePairs.get(i + 1);
			if(value >= cvp.value && value <= cvpNext.value)
			{
				valueStart = cvp.value;
				colorStart = cvp.color;
				valueEnd = cvpNext.value;
				colorEnd = cvpNext.color;
				break;
			}
		}
		if(colorStart == null || colorEnd == null) return null; //Check if the values/colors are found
		// Interpolate to find the color belonging to the given value
		double alpha = (value - valueStart) / (valueEnd - valueStart);
		double red = colorStart.red + alpha*(colorEnd.red - colorStart.red);
		double green = colorStart.green + alpha*(colorEnd.green - colorStart.green);
		double blue = colorStart.blue + alpha*(colorEnd.blue - colorStart.blue);
		RGB rgb = null;
		
//		System.out.println("Finding color for: " + value);
		//Try to create an RGB, if the color values are not valid (outside 0 to 255)
		//This method returns null
		try {
			rgb = new RGB((int)red, (int)green, (int)blue);
//			System.out.println("Found color: " + rgb);
		} catch (Exception e) { 
			System.out.println("GmmlColorGradient:getColor:Error: " + 
					red + "," + green + "," +blue + ", at value " + value);
		}
		return rgb;
	}
	
	public RGB getColor(HashMap<Integer, Object> data, int idSample)
	{
		int applySample = idSample; //The sample to apply the gradient on
		if(useSample == -1) //Check if this gradient applies to all samples
		{
			applySample = idSample; //Apply the gradient on the given sample
		} else { //Does the gradient apply to the given sample?
			if(useSample != idSample) return null;
		}
		try {
			double value = (Double)data.get(applySample); //Try to get the data
			return getColor(value);
		} catch(NullPointerException ne) { //No data available
//			System.out.println("GmmlColorGradient:getColor:Error: No data to calculate color");
		} catch(ClassCastException ce) { //Data is not double
//			System.out.println("GmmlColorGradient:getColor:Error: Data is not of type double");
		} catch(Exception e) { //Any other exception
			e.printStackTrace();
		}
		return null; //If anything goes wrong, return null
	}
	
	public String getCriterionString()
	{
		String sep = "|";
		StringBuilder criterion = new StringBuilder("GRADIENT" + sep);
		criterion.append(
				useSample + sep);
		for(ColorValuePair cvp : colorValuePairs)
		{
			criterion.append(
					cvp.value + sep +
					GmmlColorSet.getColorString(cvp.color) + sep);
		}
		System.out.println(criterion.toString());
		return criterion.toString();
	}
	
	public void parseCriterionString(String criterion)
	{
		colorValuePairs = new ArrayList<ColorValuePair>();
		String[] s = criterion.split("\\|");
//		System.out.println(criterion);
//		System.out.println(s[0] + "," + s[1] + "," + s[2] + "," + s[3] + "," + s[4] + "," + s[5]);
		try
		{
			useSample = Integer.parseInt(s[1]);
			for(int i = 2; i < s.length - 1; i+=2)
			{
				colorValuePairs.add(new ColorValuePair(
						GmmlColorSet.parseColorString(s[i+1]),
						Double.parseDouble(s[i])));
			}
//			System.out.println(dataColumn + "," + valueStart + "," + colorStart + "," +
//					valueEnd + "," + colorEnd);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Find the minimum and maximum values used in this gradient
	 * @return a double[] of length 2 with respecively the minimum and maximum values
	 */
	public double[] getMinMax()
	{
		double[] minmax = new double[] { Double.MAX_VALUE, Double.MIN_VALUE };
		for(ColorValuePair cvp : colorValuePairs)
		{
			minmax[0] = Math.min(cvp.value, minmax[0]);
			minmax[1] = Math.max(cvp.value, minmax[1]);
		}
		return minmax;
	}
	
	/**
	 * This class contains a color and its corresponding value used for the {@link GmmlColorGradient}
	 */
	public class ColorValuePair implements Comparable {
		public RGB color;
		public double value;
		public ColorValuePair(RGB color, double value)
		{
			this.color = color;
			this.value = value;
		}
		
		public int compareTo(Object o) throws ClassCastException
		{
			if(!(o instanceof ColorValuePair)) throw new ClassCastException("Object not of class ColorValuePair");
			return (int)(value - ((ColorValuePair)o).value);
		}
	}
}
