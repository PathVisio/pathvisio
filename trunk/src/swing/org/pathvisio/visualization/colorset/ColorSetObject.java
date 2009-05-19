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

import org.jdom.Element;
import org.pathvisio.gex.ReporterData;
import org.pathvisio.gex.Sample;
import org.pathvisio.visualization.colorset.Criterion.CriterionException;

/**
 * This class represent any object that can be present in a colorset
 * e.g. a gradient or boolean expression.
 */
public abstract class ColorSetObject {
	
	/**
	 * The parent colorset, that this colorSetObject is a part of.
	 */
	private ColorSet parent;
	
	/**
	 * The display name of this colorSetObject
	 */
	private String name;
	
	/**
	 * getter for name, the name of this colorSetObject
	 * the Name does not need to be unique
	 */
	public void setName(String aName) 
	{
		this.name = aName; 
	}
	
	/**
	 * setter for name, the name of this colorSetObject
	 * The name does not need to be unique.
	 */
	public String getName() { return name; }
	
	public ColorSet getColorSet() { return parent; }
	
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * @param name 			name of the gradient
	 */
	public ColorSetObject(ColorSet parent, String name) 
	{	
		this.parent = parent;
		this.name = name;
	}
	
	public ColorSetObject(ColorSet parent, Element xml) {
		this.parent = parent;
		loadXML(xml);
	}
				
	/**
	 * get the color defined by the colorset object for the given data
	 * @param data {@link HashMap}<Integer, Object> containing data (String or double) for every sampleId 
	 * @param sample id of the sample that is visualized using this color
	 * @return {@link Color} with the color returned by the colorset object after evaluating the input data,
	 * null if the input data doesn't result in a valid color
	 * @throws Exception 
	 */
	abstract Color getColor(ReporterData data, Sample key) throws CriterionException;
	
	/**
	 * Returns the parent colorset
	 */
	public ColorSet getParent()
	{
		return parent;
	}

	public abstract void paintPreview(Graphics2D g, Rectangle bounds);
	
	/**
	 * let ColorSetManager fire an event to indicate this ColorSet has
	 * changed. 
	 */
	protected void fireModifiedEvent() {
		if(parent != null && parent.getColorSetManager() != null) 
			parent.getColorSetManager().fireColorSetEvent(
				new ColorSetEvent (this, ColorSetEvent.COLORSET_MODIFIED));
	}
	
	abstract String getXmlElementName();
	
	static final String XML_ATTR_NAME = "name";
	
	public Element toXML() {
		Element elm = new Element(getXmlElementName());
		elm.setAttribute(XML_ATTR_NAME, name);
		return elm;
	}
	
	protected void loadXML(Element xml) {
		name = xml.getAttributeValue(XML_ATTR_NAME);
	}
				
}
