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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.RGB;
import org.jdom.Element;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ColorConverter;

/**
 * This class represents a colorset, a set of criteria that can be evaluated and 
 * results in a color given a collection of data
 */
public class ColorSet 
{	
	/** constant to access the special fallback color, 
	 * for when none of the criteria are met. */
	public static final int ID_COLOR_NO_CRITERIA_MET = 1;
	/**
	 * constant to access the special fallback color,
	 * for when this gene could not be found in the gene database
	 */
	public static final int ID_COLOR_NO_GENE_FOUND = 2;
	/**
	 * constant to access the special fallback color,
	 * for when the gene could be found in the gene database,
	 * but no data is available for this gene in the dataset.
	 */
	public static final int ID_COLOR_NO_DATA_FOUND = 3;
	
	Color color_no_criteria_met = PreferenceManager.getCurrent().getColor (GlobalPreference.COLOR_NO_CRIT_MET);
	Color color_no_gene_found = PreferenceManager.getCurrent().getColor (GlobalPreference.COLOR_NO_GENE_FOUND);
	Color color_no_data_found = PreferenceManager.getCurrent().getColor (GlobalPreference.COLOR_NO_DATA_FOUND);
		
	/**
	 * A user can give each colorset a name
	 */
	String name;
	
	public List<ColorSetObject> colorSetObjects;
		
	/**
	 * Constructor of this class
	 * @param name		name of the colorset
	 */
	public ColorSet(String name)
	{
		this.name = name;
		colorSetObjects = new ArrayList<ColorSetObject>();
	}
		
	public String getName() { return name; }
	
	public void setName(String n) { 
		name = n;
		fireModifiedEvent();
	}
	
	public void setColor(int id, Color rgb) {
		switch(id) {
		case ID_COLOR_NO_CRITERIA_MET:
			color_no_criteria_met = rgb;
			break;
		case ID_COLOR_NO_DATA_FOUND:
			color_no_data_found = rgb;
			break;
		case ID_COLOR_NO_GENE_FOUND:
			color_no_gene_found = rgb;
			break;
		}
		fireModifiedEvent();
	}
	
	/**
	 * Get one of the special case colors.
	 * @param id one of the ColorSet.ID_XXX constants
	 * @return
	 */
	public Color getColor(int id) {
		switch(id) {
		case ID_COLOR_NO_CRITERIA_MET:
			return color_no_criteria_met;
		case ID_COLOR_NO_DATA_FOUND:
			return color_no_data_found;
		case ID_COLOR_NO_GENE_FOUND:
			return color_no_gene_found;
		default: return null;
		}
	}
	
	/**
	 * Adds a new {@link ColorSetObject} to this colorset
	 * @param o the {@link ColorSetObject} to add
	 */
	public void addObject(ColorSetObject o)
	{
		colorSetObjects.add(o);
		fireModifiedEvent();
	}
	
	/**
	 * Remove a ColorSetObject from this ColorSet.
	 * @param o
	 */
	public void removeObject(ColorSetObject o) 
	{
		colorSetObjects.remove(o);
		fireModifiedEvent();
	}
	
	/**
	 * Obtain all ColorSetObjects (Rules / Gradients) in this ColorSet.
	 */
	public List<ColorSetObject> getObjects() 
	{
		return colorSetObjects;
	}
	
	/**
	 * Get the color for the given expression data by evaluating all colorset objects
	 * @param data		the expression data to get the color for
	 * @param sampleId	the id of the sample that will be visualized
	 * @return	an {@link RGB} object representing the color for the given data
	 */
	public Color getColor(HashMap<Integer, Object> data, int sampleId)
	{
		if(data == null) return color_no_data_found;
		Object value = data.get(sampleId);
		if(value == null || value.equals(Double.NaN)) return color_no_data_found;
		
		Color rgb = color_no_criteria_met; //The color to return
		Iterator<ColorSetObject> it = colorSetObjects.iterator();
		//Evaluate all ColorSet objects, return when a valid color is found
		while(it.hasNext())
		{
			ColorSetObject gc = it.next();
			try{ 
				Color gcRgb = gc.getColor(data, sampleId);
				if(gcRgb != null) {
					return gcRgb;
				}
			} catch(Exception e) {
				Logger.log.error("ColorSetObject " + gc + " could not evaluate data: " + e.getMessage());
			}
		}
		return rgb;
	}
	
	final static String XML_ELEMENT = "ColorSet";
	final static String XML_ATTR_NAME = "name";
	final static String XML_ELM_COLOR_NCM = "no-criteria-met";
	final static String XML_ELM_COLOR_NGF = "no-gene-found";
	final static String XML_ELM_COLOR_NDF = "no-data-found";
	
	public Element toXML() {
		Element elm = new Element(XML_ELEMENT);
		elm.setAttribute(XML_ATTR_NAME, name);
		
		elm.addContent(ColorConverter.createColorElement(XML_ELM_COLOR_NCM, color_no_criteria_met));
		elm.addContent(ColorConverter.createColorElement(XML_ELM_COLOR_NGF, color_no_gene_found));
		elm.addContent(ColorConverter.createColorElement(XML_ELM_COLOR_NDF, color_no_data_found));
		
		for(ColorSetObject cso : colorSetObjects)
			elm.addContent(cso.toXML());
		return elm;
	}
	
	public static ColorSet fromXML(Element e) {
		ColorSet cs = new ColorSet(e.getAttributeValue(XML_ATTR_NAME));
		for(Object o : e.getChildren()) {
			try {
				Element elm = (Element) o;
				String name = elm.getName();
				if(name.equals(ColorGradient.XML_ELEMENT_NAME))
					cs.addObject(new ColorGradient(cs, elm));
				else if(name.equals(ColorRule.XML_ELEMENT_NAME))
					cs.addObject(new ColorRule(cs, elm));
				else if(name.equals(XML_ELM_COLOR_NCM))
					cs.setColor(ID_COLOR_NO_CRITERIA_MET, ColorConverter.parseColorElement(elm));
				else if(name.equals(XML_ELM_COLOR_NGF))
					cs.setColor(ID_COLOR_NO_GENE_FOUND, ColorConverter.parseColorElement(elm));
				else if(name.equals(XML_ELM_COLOR_NDF))
					cs.setColor(ID_COLOR_NO_DATA_FOUND, ColorConverter.parseColorElement(elm));
			} catch(Exception ex) {
				Logger.log.error("Unable to parse colorset xml", ex);
			}
		}
		return cs;
	}
			
	static void printParseError(String criterion, Exception e) {
		Logger.log.error("Unable to parse colorset data stored in " +
				"expression database: " + criterion, e);
		MessageDialog.openWarning(SwtEngine.getCurrent().getWindow().getShell(), 
					"Warning", "Unable to parse the colorset data in this expression dataset");
	}
	
	void fireModifiedEvent() {
		ColorSetManager.fireColorSetEvent(
				new ColorSetEvent (this, ColorSetEvent.COLORSET_MODIFIED));
	}
}
