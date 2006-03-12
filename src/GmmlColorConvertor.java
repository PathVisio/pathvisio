import org.eclipse.swt.graphics.*;
//~ import java.awt.Color;
import java.util.Arrays;
import java.util.List;

class GmmlColorConvertor
{
	public static final List colorMappings = Arrays.asList(new String[]{
		"Aqua", "Black", "Blue", "Fuchsia", "Gray", "Green", "Lime",
		"Maroon", "Navy", "Olive", "Purple", "Red", "Silver", "Teal",
		"White", "Yellow"
	});
	
	public static final List rgbMappings = Arrays.asList(new double[][] {
		{0, 1, 1},		// aqua 
		{0, 0, 0},	 	// black
		{0, 0, 1}, 		// blue
		{1, 0, 1},		// fuchsia
		{.5, .5, .5,},	// gray
		{0, .5, 0}, 	// green
		{0, 1, 0},		// lime
		{.5, 0, 0},		// maroon
		{0, 0, .5},		// navy
		{.5, .5, 0},	// olive
		{.5, 0, .5},	// purple
		{1, 0, 0}, 		// red
		{.75, .75, .75},// silver
		{0, .5, .5}, 	// teal
		{1, 1, 1}		// white
	});
	
	private static final char[] hexadecimalMappings = {'1', '2', '3', '4', '5',
		'6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	/**
	 * Constructor for this class
	 */
	public GmmlColorConvertor()
	{
	}	
	
	/**
	 * Check the format of String specified and then calls the 
	 * correct method to decode it
	 * @param strColor	- the String to convert to a color
	 * @return	a Color object
	 */
	public static RGB string2Color(String strColor)
	{
		RGB color = new RGB(0, 0, 0);
		if(strColor.length() == 6)
		{
			boolean strColorIsHex = true;
			boolean found = false;
						
			for (int j = 0; (j < strColor.length()) && !found; j ++)
			{
				char x = strColor.charAt(j);
				found = false;
				for (int i = 0; (i < 16) && strColorIsHex; i ++)
				{
					if(x == hexadecimalMappings[i])
					{
						found = true;
					}
				}
				if (!found)
				{
					strColorIsHex = false;
				}
			}
			if (strColorIsHex)
			{
				int r = Integer.parseInt(strColor.substring(0, 2), 16);
				int g = Integer.parseInt(strColor.substring(2, 4), 16);
				int b = Integer.parseInt(strColor.substring(4, 6), 16);
								
				color = new RGB(r, g, b);
			}
		}
		
		if(strColor.startsWith("java.awt.Color[r="))
		{
			int first 	= strColor.indexOf("=") + 1;
			int second	= strColor.indexOf(",");
			
			int r = (int)Double.parseDouble(strColor.substring(first, second));
			
			first	= second + 3; 
			second 	= strColor.lastIndexOf(",");
			
			int g = (int)Double.parseDouble(strColor.substring(first, second));
			
			first 	= strColor.lastIndexOf("=") + 1;
			second	= strColor.lastIndexOf("]");
			
			int b = (int)Double.parseDouble(strColor.substring(first, second));
			
			color = new RGB(r, g, b);
		}
		
		else {
			int index = colorMappings.indexOf(strColor);
			if (index > -1)
			{
				double[] c = (double[]) rgbMappings.get(index);
				color = new RGB((int)c[0] * 255, (int)c[1] * 255, (int)c[2] * 255);			
			}
			else 
			{
				color = new RGB(0,0,0);
			}
		}
		return color;
	}
}