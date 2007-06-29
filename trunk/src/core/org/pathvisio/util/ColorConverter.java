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
package org.pathvisio.util;

import java.awt.Color;

import org.eclipse.swt.graphics.RGB;
import org.jdom.Element;
import org.pathvisio.Engine;

public abstract class ColorConverter
{		    
    /**
	 * Creates a string representing a {@link RGB} object which is parsable by {@link #parseRgbString(String)}
	 * @param rgb the {@link RGB} object to create a string from
	 * @return the string representing the {@link RGB} object
	 */
	public static String getRgbString(RGB rgb)
	{
		return rgb.red + "," + rgb.green + "," + rgb.blue;
	}
	
	public static String getRgbString(java.awt.Color c) {
		return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
	}
	
	public static RGB toRGB(Color c) {
		return new RGB(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	public static Color fromRGB(RGB rgb) {
		return new Color(rgb.red, rgb.green, rgb.blue);
	}
	
	/**
	 * Parses a string representing a {@link RGB} object created with {@link #getRgbString(RGB)}
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
			Engine.log.error("Unable to parse color '" + rgbString + 
					"'stored in expression database", e);
			return new RGB(0,0,0);
		}
	}
	    
	public static java.awt.Color parseColorString(String colorString)
	{
		String[] s = colorString.split(",");
		try 
		{
			return new java.awt.Color(
					Integer.parseInt(s[0]), 
					Integer.parseInt(s[1]), 
					Integer.parseInt(s[2]));
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Unable to parse color from '" + colorString + "'", e);
		}
	}
	    
    final static String XML_ELEMENT_COLOR = "color";
	final static String XML_COLOR_R = "red";
	final static String XML_COLOR_G = "green";
	final static String XML_COLOR_B = "blue";
    public static Element createColorElement(String name, Color rgb) {
    	Element elm = new Element(XML_ELEMENT_COLOR);
    	elm.setName(name);
    	elm.setAttribute(XML_COLOR_R, Integer.toString(rgb.getRed()));
    	elm.setAttribute(XML_COLOR_G, Integer.toString(rgb.getGreen()));
    	elm.setAttribute(XML_COLOR_B, Integer.toString(rgb.getBlue()));
    	
    	return elm;
    }
    
    public static Color parseColorElement(Element xml) {
    	int r = Integer.parseInt(xml.getAttributeValue(XML_COLOR_R));
    	int g = Integer.parseInt(xml.getAttributeValue(XML_COLOR_G));
    	int b = Integer.parseInt(xml.getAttributeValue(XML_COLOR_B));
    	return new Color(r,g,b);
    }
}
