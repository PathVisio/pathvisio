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
package org.pathvisio.visualization.colorset;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom.Element;
import org.pathvisio.debug.Logger;
import org.pathvisio.gex.ReporterData;
import org.pathvisio.gex.Sample;
import org.pathvisio.util.ColorConverter;

/**
 * This class represent a color gradient used for data visualization
 */
public class ColorGradient extends ColorSetObject {
	public static final String XML_ELEMENT_NAME = "ColorGradient";

	private List<ColorValuePair> colorValuePairs;

	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * This will generate an empty gradient. Call generateDefault() if you
	 * want to fill the gradient with some default values.
	 */
	public ColorGradient()
	{
		super("gradient");
		colorValuePairs = new ArrayList<ColorValuePair>();
	}

	public static List<ColorGradient> createDefaultGradients() {
		List<ColorGradient> gradients = new ArrayList<ColorGradient>();
		ColorGradient g = new ColorGradient();
		g.addColorValuePair(new ColorValuePair(Color.BLUE, -1));
		g.addColorValuePair(new ColorValuePair(Color.YELLOW, 1));
		gradients.add(g);
		g = new ColorGradient();
		g.addColorValuePair(new ColorValuePair(Color.GREEN, -1));
		g.addColorValuePair(new ColorValuePair(Color.YELLOW, 0));
		g.addColorValuePair(new ColorValuePair(Color.RED, 1));
		gradients.add(g);
		g = new ColorGradient();
		g.addColorValuePair(new ColorValuePair(Color.GREEN, -1));
		g.addColorValuePair(new ColorValuePair(Color.WHITE, 0));
		g.addColorValuePair(new ColorValuePair(Color.RED, 1));
		gradients.add(g);
		g = new ColorGradient();
		g.addColorValuePair(new ColorValuePair(Color.BLUE, -1));
		g.addColorValuePair(new ColorValuePair(Color.RED, 1));
		gradients.add(g);
		g = new ColorGradient();
		g.addColorValuePair(new ColorValuePair(Color.BLUE, -1));
		g.addColorValuePair(new ColorValuePair(Color.WHITE, 0));
		g.addColorValuePair(new ColorValuePair(Color.RED, 1));
		gradients.add(g);
		return gradients;
	}

	public ColorGradient(Element xml)
	{
		super(xml);
	}

	/**
	 * Get the the colors and corresponding values used in this gradient as {@link ColorValuePair}
	 * @return ArrayList containing the ColorValuePairs
	 */
	public List<ColorValuePair> getColorValuePairs()
	{
		return colorValuePairs;
	}

	/**
	 * Add a {@link ColorValuePair} to this gradient
	 */
	public void addColorValuePair(ColorValuePair cvp)
	{
		colorValuePairs.add(cvp);
		cvp.setParent(this);
		fireModifiedEvent();
	}

	/**
	 * Remove a {@link ColorValuePair} from this gradient
	 */
	public void removeColorValuePair(ColorValuePair cvp)
	{
		if(!colorValuePairs.contains(cvp)) return;
		colorValuePairs.remove(cvp);
		cvp.setParent(null);
		fireModifiedEvent();
	}

	public void paintPreview(Graphics2D g, Rectangle bounds) {
		paintPreview(g, bounds, false);
	}

	public void paintPreview(Graphics2D g, Rectangle bounds, boolean text) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double[] mm = getMinMax();
		for(int i = 0; i < bounds.width; i++) {
			Color c = getColor(mm[0] + (double)i * (mm[1] - mm[0])/ bounds.width);
			g.setColor(c);
			g.fillRect(bounds.x + i, bounds.y, 1, bounds.height);
		}

		if(text) {
			g.setColor(Color.BLACK);
			int margin = 30; //Border spacing
			int x = bounds.x + margin / 2;
			int w = bounds.width - margin;
			for(int i = 0; i < colorValuePairs.size(); i++) {
				String value = "" + colorValuePairs.get(i).getValue();
				Rectangle2D fb = g.getFontMetrics().getStringBounds(value, g);
				g.drawString(
						value,
						x - (int)(fb.getWidth() / 2),
						bounds.y + bounds.height / 2 + (int)(fb.getHeight() / 2)
				);
				x += w / (colorValuePairs.size() - 1);
			}
		}
	}

	/**
	 * get the color of the gradient for this value
	 * @param value
	 * @return	Color containing the color information for the corresponding value
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

	@Override public Color getColor(ReporterData data, Sample key)
	{
		Object o = data.getSampleData(key);
		double value;
		if (o instanceof Double) value = (Double)o;
		else value = Double.NaN;
		return getColor(value);
	}

	/**
	 * Compares two color gradients by the order of the colors.
	 * @return True if they have the same colors in the same order.
	 */
	public boolean equalsPreset(ColorGradient g) {
		if(g.colorValuePairs.size() == colorValuePairs.size()) {
			for(int i = 0; i < colorValuePairs.size(); i++) {
				if(!g.colorValuePairs.get(i).color.equals(colorValuePairs.get(i).color)) {
					return false;
				}
			}
			return true;
		}
		return false;
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
	public static class ColorValuePair implements Comparable<ColorValuePair>
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
			if (parent != null) parent.fireModifiedEvent();
		}

		public double getValue() { return value; }
		public void setValue(double v) {
			value = v;
			if (parent != null) parent.fireModifiedEvent();
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
		
		
		private ColorGradient parent = null;

		/**
		 * Must be called by ColorGradient.add
		 */
		private void setParent(ColorGradient parent)
		{
			this.parent = parent;
		}
	}
}
