package colorSet;
import graphics.GmmlGeneProduct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;

import data.GmmlGex.Sample;

public class GmmlColorGradient extends GmmlColorSetObject {
	public static final int DATA_COL_ALL = -1;
	public static final int DATA_COL_NO = -2;
	private int dataColumn;	
	public ArrayList<ColorValuePair> colorValuePairs;
	
	public GmmlColorGradient(GmmlColorSet parent, String name)
	{
		super(parent, name);
		colorValuePairs = new ArrayList<ColorValuePair>();
		dataColumn = DATA_COL_NO;
	}
	
	public GmmlColorGradient(GmmlColorSet parent, String name, String criterion)
	{
		super(parent, name, criterion);
	}
	
	public void setDataColumn(int dataColumn)
	{
		useSamples = new ArrayList<Integer>();
		this.dataColumn = dataColumn;
		if(dataColumn > -1)
		{
			useSamples.add(dataColumn);
		}
		else
		{
			for(Sample s : parent.useSamples)
			{
				useSamples.add(s.idSample);  
			}
		}
	}
	
	public int getDataColumn()
	{
		return dataColumn;
	}
	
	public RGB getColor(double value)
	{
		double[] minmax = getMinMax();
		double valueStart = 0;
		double valueEnd = 0;
		RGB colorStart = null;
		RGB colorEnd = null;
		Collections.sort(colorValuePairs);
		//Find what colors the value is in between
		boolean found = false;
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
				found = true;
				break;
			}
		}
		if(!found)
		{
//			return null;
			if(value < minmax[0]) value = minmax[0]; else value = minmax[1];
		}
		//TEMP
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
				found = true;
				break;
			}
		}
		//TEMP
		if(colorStart == null || colorEnd == null ) { return null; }
		double alpha = (value - valueStart) / (valueEnd - valueStart);
		double red = colorStart.red + alpha*(colorEnd.red - colorStart.red);
		double green = colorStart.green + alpha*(colorEnd.green - colorStart.green);
		double blue = colorStart.blue + alpha*(colorEnd.blue - colorStart.blue);
		RGB rgb = null;
		
//		System.out.println("Finding color for: " + value);
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
		int useSample = dataColumn;
		if(dataColumn == -1)
		{
			useSample = idSample;
		} else {
			if(useSample != idSample) return null;
		}
		try {
			double value = (Double)data.get(useSample);
			return getColor(value);
		} catch(NullPointerException ne) {
//			System.out.println("GmmlColorGradient:getColor:Error: No data to calculate color");
		} catch(ClassCastException ce) {
//			System.out.println("GmmlColorGradient:getColor:Error: Data is not of type double");
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public GmmlColorSet getParent()
	{
		return parent;
	}
	
	public String getCriterionString()
	{
		String sep = "|";
		StringBuilder criterion = new StringBuilder("GRADIENT" + sep);
		criterion.append(
				dataColumn + sep);
		for(ColorValuePair cvp : colorValuePairs)
		{
			criterion.append(
					cvp.value + sep +
					getColorString(cvp.color) + sep);
		}
		System.out.println(criterion.toString());
		return criterion.toString();
	}
	
	void parseCriterionString(String criterion)
	{
		colorValuePairs = new ArrayList<ColorValuePair>();
		String[] s = criterion.split("\\|");
//		System.out.println(criterion);
//		System.out.println(s[0] + "," + s[1] + "," + s[2] + "," + s[3] + "," + s[4] + "," + s[5]);
		try
		{
			setDataColumn(Integer.parseInt(s[1]));
			for(int i = 2; i < s.length - 1; i+=2)
			{
				colorValuePairs.add(new ColorValuePair(
						parseColorString(s[i+1]),
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
