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
package org.pathvisio.visualization.colorset;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.jdom.Element;
import org.pathvisio.debug.Logger;
import org.pathvisio.util.ColorConverter;

/**
 * This class represent a color gradient used for data visualization
 */
public class ColorGradient extends ColorSetObject {
	public static final String XML_ELEMENT_NAME = "ColorGradient";

	private ArrayList<ColorValuePair> colorValuePairs = new ArrayList<ColorValuePair>();
	
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * This will generate an empty gradient. Call generateDefault() if you
	 * want to fill the gradient with some default values.
	 */
	public ColorGradient(ColorSet parent)
	{
		super(parent, "gradient");
		getColorValuePairs();
	}
	
	/**
	 * Adds a few default ColorValuePairs to this color gradient:
	 * -1, red
	 * 0, yellow
	 * 1, green
	 */
	public void generateDefault()
	{
		colorValuePairs.add(new ColorValuePair(new Color(0,255,0), -1));
		colorValuePairs.add(new ColorValuePair(new Color(255,255,0), 0));
		colorValuePairs.add(new ColorValuePair(new Color(255,0,0), 1));		
	}
		
	public ColorGradient(ColorSet parent, Element xml) 
	{
		super(parent, xml);
	}
	
	/**
	 * Get the the colors and corresponding values used in this gradient as {@link ColorValuePair}
	 * @return ArrayList containing the ColorValuePairs
	 */
	public ArrayList<ColorValuePair> getColorValuePairs() 
	{
		return colorValuePairs;
	}
	
	/**
	 * Add a {@link ColorValuePair} to this gradient
	 */
	public void addColorValuePair(ColorValuePair cvp)
	{
		colorValuePairs.add(cvp);
		fireModifiedEvent();
	}
	
	/**
	 * Remove a {@link ColorValuePair} from this gradient
	 */
	public void removeColorValuePair(ColorValuePair cvp)
	{
		if(!colorValuePairs.contains(cvp)) return;
		colorValuePairs.remove(cvp);
		fireModifiedEvent();
	}
			
	/**
	 * get the color of the gradient for this value
	 * @param value
	 * @return	{@link RGB} containing the color information for the corresponding value
	 * or null if the value does not have a valid color for this gradient
	 */
	public Color getColor(double value)
	{
		double[] minmax = getMinMax(); //Get the minimum and maximum values of the gradient
		double valueStart = 0;
		double valueEnd = 0;
		Color colorStart = null;
		Color colorEnd = null;
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
				valueStart = cvp.getValue();
				colorStart = cvp.getColor();
				valueEnd = cvpNext.getValue();
				colorEnd = cvpNext.getColor();
			}
		}
		if(colorStart == null || colorEnd == null) return null; //Check if the values/colors are found
		// Interpolate to find the color belonging to the given value
		double alpha = (value - valueStart) / (valueEnd - valueStart);
		double red = colorStart.getRed() + alpha*(colorEnd.getRed() - colorStart.getRed());
		double green = colorStart.getGreen() + alpha*(colorEnd.getGreen() - colorStart.getGreen());
		double blue = colorStart.getBlue() + alpha*(colorEnd.getBlue() - colorStart.getBlue());
		Color rgb = null;
		
		//Try to create an RGB, if the color values are not valid (outside 0 to 255)
		//This method returns null
		try {
			rgb = new Color((int)red, (int)green, (int)blue);
		} catch (Exception e) { 
			Logger.log.error("GmmlColorGradient:getColor: " + 
					red + "," + green + "," +blue + ", for value " + value, e);
		}
		return rgb;
	}
	
	public Color getColor(HashMap<Integer, Object> data, int idSample) throws NumberFormatException
	{
		double value = (Double)data.get(idSample);
		return getColor(value);
	}
	
	String getXmlElementName() {
		return XML_ELEMENT_NAME;
	}
	
	public Element toXML() {
		Element elm = super.toXML();
		for(ColorValuePair cvp : colorValuePairs)
			elm.addContent(cvp.toXML());
		return elm;
	}
	
	protected void loadXML(Element xml) {
		super.loadXML(xml);
		colorValuePairs = new ArrayList<ColorValuePair>();
		for(Object o : xml.getChildren(ColorValuePair.XML_ELEMENT))
			colorValuePairs.add(new ColorValuePair((Element) o));
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
	 * This class contains a color and its corresponding value used for the {@link ColorGradient}
	 */
	public class ColorValuePair implements Comparable<ColorValuePair> 
	{
		static final String XML_ELEMENT = "color-value";
		static final String XML_ATTR_VALUE = "value";
		static final String XML_ELM_COLOR = "color";
		private Color color;
		private double value;
		
		public ColorValuePair(Color color, double value)
		{
			this.color = color;
			this.value = value;
		}
		
		public ColorValuePair(Element xml) {
			Object o = xml.getChildren(XML_ELM_COLOR).get(0);
			color = ColorConverter.parseColorElement((Element)o);
			value = Double.parseDouble(xml.getAttributeValue(XML_ATTR_VALUE));
		}
		
		public Color getColor() { return color; }
		public void setColor(Color rgb) {
			color = rgb;
			fireModifiedEvent();
		}
		
		public double getValue() { return value; }
		public void setValue(double v) {
			value = v;
			fireModifiedEvent();
		}
		
		public int compareTo(ColorValuePair o)
		{
			return (int)(value - o.value);
		}
		
		public Element toXML() {
			Element elm = new Element(XML_ELEMENT);
			elm.setAttribute(XML_ATTR_VALUE, Double.toString(value));
			elm.addContent(ColorConverter.createColorElement(XML_ELM_COLOR, color));
			return elm;
		}
	}	
}
