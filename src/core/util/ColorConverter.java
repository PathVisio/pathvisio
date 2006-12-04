// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package util;
import gmmlVision.GmmlVision;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.jdom.Element;

public abstract class ColorConverter
{
	public static final List colorMappings = Arrays.asList(new String[]{
		"Aqua", "Black", "Blue", "Fuchsia", "Gray", "Green", "Lime",
		"Maroon", "Navy", "Olive", "Purple", "Red", "Silver", "Teal",
		"White", "Yellow", "Transparent"
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
		{1, 1, 1},		// white
		{1, 1, 0},		// yellow
		{0, 0, 0}		// transparent (actually irrelevant)
	});
	
	/**
	 * Constructor for this class
	 */
	public ColorConverter()
	{}	
	
	/**
	 * Converts an {@link RGB} object to a hexbinary string
	 * @param color
	 * @return
	 */
	public static String color2HexBin(RGB color)
	{
		String red = padding(Integer.toBinaryString(color.red), 8, '0');
		String green = padding(Integer.toBinaryString(color.green), 8, '0');
		String blue = padding(Integer.toBinaryString(color.blue), 8, '0');
		String hexBinary = Integer.toHexString(Integer.valueOf(red + green + blue, 2));
		return padding(hexBinary, 6, '0');
	}
	
	/**
	 * Converts a string containing either a named color (as specified in gpml) or a hexbinary number
	 * to an {@link RGB} object
	 * @param strColor
	 * @return
	 */
    public static RGB gmmlString2Color(String strColor)
    {
    	if(colorMappings.contains(strColor))
    	{
    		double[] color = (double[])rgbMappings.get(colorMappings.indexOf(strColor));
    		return new RGB((int)(255*color[0]),(int)(255*color[1]),(int)(255*color[2]));
    	}
    	else
    	{
    		try
    		{
    			strColor = padding(strColor, 6, '0');
        		int red = Integer.valueOf(strColor.substring(0,2),16);
        		int green = Integer.valueOf(strColor.substring(2,4),16);
        		int blue = Integer.valueOf(strColor.substring(4,6),16);
        		return new RGB(red,green,blue);
    		}
    		catch (Exception e)
    		{
    			GmmlVision.log.error("while converting color: " +
    					"Color " + strColor + " is not valid, element color is set to black", e);
    		}
    	}
    	return new RGB(0,0,0);
    }
    
    /**
	 * Creates a string representing a {@link RGB} object which is parsable by {@link parseRgbString}
	 * @param rgb the {@link RGB} object to create a string from
	 * @return the string representing the {@link RGB} object
	 */
	public static String getRgbString(RGB rgb)
	{
		return rgb.red + "," + rgb.green + "," + rgb.blue;
	}
	
	/**
	 * Parses a string representing a {@link RGB} object created with {@link getRgbString}
	 * @param rgbString the string to be parsed
	 * @return the {@link RGB} object this string represented
	 */
	public static RGB parseRgbString(String rgbString)
	{
		String[] s = rgbString.split(",");
		try 
		{
			return new RGB(
					Integer.parseInt(s[0]), 
					Integer.parseInt(s[1]), 
					Integer.parseInt(s[2]));
		}
		catch(Exception e)
		{
			GmmlVision.log.error("Unable to parse color '" + rgbString + 
					"'stored in expression database", e);
			return new RGB(0,0,0);
		}
	}
	
    /**
     * Prepends character c x-times to the input string to make it length n
     * @param s	String to pad
     * @param n	Number of characters of the resulting string
     * @param c	character to append
     * @return	string of length n or larger (if given string s > n)
     */
    public static String padding(String s, int n, char c)
    {
    	while(s.length() < n)
    	{
    		s = c + s;
    	}
    	return s;
    }
    
    final static String XML_ELEMENT_COLOR = "color";
	final static String XML_COLOR_R = "red";
	final static String XML_COLOR_G = "green";
	final static String XML_COLOR_B = "blue";
    public static Element createColorElement(String name, RGB rgb) {
    	Element elm = new Element(XML_ELEMENT_COLOR);
    	elm.setName(name);
    	elm.setAttribute(XML_COLOR_R, Integer.toString(rgb.red));
    	elm.setAttribute(XML_COLOR_G, Integer.toString(rgb.green));
    	elm.setAttribute(XML_COLOR_B, Integer.toString(rgb.blue));
    	
    	return elm;
    }
    
    public static RGB parseColorElement(Element xml) {
    	int r = Integer.parseInt(xml.getAttributeValue(XML_COLOR_R));
    	int g = Integer.parseInt(xml.getAttributeValue(XML_COLOR_G));
    	int b = Integer.parseInt(xml.getAttributeValue(XML_COLOR_B));
    	return new RGB(r,g,b);
    }
}
