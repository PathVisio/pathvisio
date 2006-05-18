package colorSet;
import graphics.GmmlGeneProduct;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;

public class GmmlColorGradient extends GmmlColorSetObject {
	private int dataColumn;
	public RGB colorStart;
	public RGB colorEnd;
	public double valueStart;
	public double valueEnd;
	
	public GmmlColorGradient(GmmlColorSet parent, String name)
	{
		super(parent, name);
		colorStart = new RGB(0, 255, 0);
		colorEnd = new RGB(255, 0, 0);
		valueStart = -1;
		valueEnd = 1;
	}
	
	public GmmlColorGradient(GmmlColorSet parent, String name, String criterion)
	{
		super(parent, name, criterion);
	}
	
	public void setDataColumn(int dataColumn)
	{
		useSamples = new ArrayList<Integer>();
		this.dataColumn = dataColumn;
		useSamples.add(dataColumn);
	}
	
	public int getDataColumn()
	{
		return dataColumn;
	}
	
	public RGB getColor(double value)
	{
		if(value < valueStart || value > valueEnd)
		{
			return null;
		}
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
			System.out.println("GmmlColorGradient:getColor:Error: " + red + "," + green + "," +blue);
		}
		return rgb;
	}
	
	public RGB getColor(HashMap<Integer, Object> data)
	{
		try {
			double value = (Double)data.get(dataColumn);
			return getColor(value);
		} catch(NullPointerException ne) {
			System.out.println("GmmlColorGradient:getColor:Error: No data to calculate color");
		} catch(ClassCastException ce) {
			System.out.println("GmmlColorGradient:getColor:Error: Data is not of type double");
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
				dataColumn + sep +
				valueStart + sep +
				getColorString(colorStart) + sep +
				valueEnd   + sep +
				getColorString(colorEnd));
		return criterion.toString();
	}
	
	void parseCriterionString(String criterion)
	{
		String[] s = criterion.split("\\|");
//		System.out.println(criterion);
//		System.out.println(s[0] + "," + s[1] + "," + s[2] + "," + s[3] + "," + s[4] + "," + s[5]);
		try
		{
			setDataColumn(Integer.parseInt(s[1]));
			valueStart = Double.parseDouble(s[2]);
			colorStart = parseColorString(s[3]);
			valueEnd = Double.parseDouble(s[4]);
			colorEnd = parseColorString(s[5]);
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
}
