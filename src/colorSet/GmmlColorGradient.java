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
	 * Contains the colors and corresponding values used in this gradient as {@link ColorValuePair}
	 */
	private ArrayList<ColorValuePair> colorValuePairs;
	/**
	 * Get the the colors and corresponding values used in this gradient as {@link ColorValuePair}
	 * @return ArrayList containing the ColorValuePairs
	 */
	public ArrayList<ColorValuePair> getColorValuePairs() 
	{ 
		if(colorValuePairs == null) colorValuePairs = new ArrayList<ColorValuePair>();
		return colorValuePairs;
	}
	/**
	 * Add a {@link ColorValuePair} to this gradient
	 */
	public void addColorValuePair(ColorValuePair cvp)
	{
		if(colorValuePairs == null) colorValuePairs = new ArrayList<ColorValuePair>();
		colorValuePairs.add(cvp);
	}
	/**
	 * Remove a {@link ColorValuePair} from this gradient
	 */
	public void removeColorValuePair(ColorValuePair cvp)
	{
		if(colorValuePairs == null || !colorValuePairs.contains(cvp)) return;
		colorValuePairs.remove(cvp);
	}
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * @param name 			name of the gradient
	 */
	public GmmlColorGradient(GmmlColorSet parent, String name)
	{
		super(parent, name);
		getColorValuePairs();
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
		getColorValuePairs();
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
		if(value < minmax[0]) value = minmax[0]; else if(value > minmax[1]) value = minmax[1];
		
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
		if(useSample == USE_SAMPLE_ALL) //Check if this gradient applies to all samples
		{
			applySample = idSample; //Apply the gradient on the given sample
		} else { //Does the gradient apply to the given sample?
			if(useSample != idSample) return null;
		}
		try {
			double value = (Double)data.get(applySample); //Try to get the data
			return getColor(value);
		} catch(NullPointerException ne) { //No data available
			System.out.println("GmmlColorGradient:getColor:Error: No data to calculate color");
		} catch(ClassCastException ce) { //Data not of type double
		} catch(Exception e) { //Any other exception
			e.printStackTrace();
		}
		return null; //If anything goes wrong, return null
	}
	
	public String getCriterionString()
	{
		//GRADIENT | useSample | value1 | color1 | ... | valueN | colorN |
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
		return criterion.toString();
	}
	
	public void parseCriterionString(String criterion)
	{
		colorValuePairs = new ArrayList<ColorValuePair>();
		String[] s = criterion.split("\\|");
		try
		{
			useSample = Integer.parseInt(s[1]);
			for(int i = 2; i < s.length - 1; i+=2)
			{
				colorValuePairs.add(new ColorValuePair(
						GmmlColorSet.parseColorString(s[i+1]),
						Double.parseDouble(s[i])));
			}
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
