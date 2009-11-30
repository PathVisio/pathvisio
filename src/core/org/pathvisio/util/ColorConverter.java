// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import org.jdom.Element;

/**
 * Methods for writing and parsing colors in different ways.
 * There are two methods for exchanging RGB triplets in a 255,255,255 string format,
 * and two methods for exchanging RGB triplets as
 * a <color red="255" green="255" blue="255"/> JDom element
 */
public abstract class ColorConverter
{
	/**
	 * Returns a string representing a {@link Color} object.
	 * @param c The {@link Color} to be converted to a string
	 * @return a string representing the {@link Color} c
	 */
	public static String getRgbString(Color c) {
		return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
	}

	/**
	 * Parses a string representing a {@link Color} object created with {@link #getRgbString(Color)}
	 * @param rgbString the string to be parsed
	 * @return the {@link Color} object this string represented
	 */
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

	/**
	 * Create a JDom Element to store this RGB triplet
	 */
	public static Element createColorElement(String name, Color rgb) {
    	Element elm = new Element(XML_ELEMENT_COLOR);
    	elm.setName(name);
    	elm.setAttribute(XML_COLOR_R, Integer.toString(rgb.getRed()));
    	elm.setAttribute(XML_COLOR_G, Integer.toString(rgb.getGreen()));
    	elm.setAttribute(XML_COLOR_B, Integer.toString(rgb.getBlue()));

    	return elm;
    }

	/**
	 * obtain the RGB triplet from a JDom element.
	 */
    public static Color parseColorElement(Element xml) {
    	int r = Integer.parseInt(xml.getAttributeValue(XML_COLOR_R));
    	int g = Integer.parseInt(xml.getAttributeValue(XML_COLOR_G));
    	int b = Integer.parseInt(xml.getAttributeValue(XML_COLOR_B));
    	return new Color(r,g,b);
    }
}
