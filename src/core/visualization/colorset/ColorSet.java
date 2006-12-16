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
package visualization.colorset;
import gmmlVision.GmmlVision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.RGB;
import org.jdom.Element;

import preferences.GmmlPreferences;
import util.ColorConverter;
import visualization.VisualizationManager;
import visualization.VisualizationManager.VisualizationEvent;


/**
 * This class represents a colorset, a set of criteria that can be evaluated and 
 * results in a color given a collection of data
 */
public class ColorSet {	
	public static final int ID_COLOR_NO_CRITERIA_MET = 1;
	public static final int ID_COLOR_NO_GENE_FOUND = 2;
	public static final int ID_COLOR_NO_DATA_FOUND = 3;
	
	RGB color_no_criteria_met = GmmlPreferences.getColorProperty(GmmlPreferences.PREF_COL_NO_CRIT_MET);
	RGB color_no_gene_found = GmmlPreferences.getColorProperty(GmmlPreferences.PREF_COL_NO_GENE_FOUND);
	RGB color_no_data_found = GmmlPreferences.getColorProperty(GmmlPreferences.PREF_COL_NO_DATA_FOUND);
		
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
	
	public void setColor(int id, RGB rgb) {
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
	
	public RGB getColor(int id) {
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
	
	public void removeObject(ColorSetObject o) {
		colorSetObjects.remove(o);
		fireModifiedEvent();
	}
	
	public List<ColorSetObject> getObjects() {
		return colorSetObjects;
	}
	
	public boolean nameExists(String name) {
		for(ColorSetObject o : colorSetObjects) 
			if(o.getName().equalsIgnoreCase(name)) return true;
		return false;
	}
	
	public String getNewName(String prefix) {
		int i = 1;
		String name = prefix;
		while(nameExists(name)) name = prefix + "-" + i++;
		return name;
	}
		
	/**
	 * Get the color for the given expression data by evaluating all colorset objects
	 * @param data		the expression data to get the color for
	 * @param sampleId	the id of the sample that will be visualized
	 * @return	an {@link RGB} object representing the color for the given data
	 */
	public RGB getColor(HashMap<Integer, Object> data, int sampleId)
	{
		if(data == null) return color_no_data_found;
		
		RGB rgb = color_no_criteria_met; //The color to return
		Iterator it = colorSetObjects.iterator();
		//Evaluate all colorset objects, return when a valid color is found
		while(it.hasNext())
		{
			ColorSetObject gc = (ColorSetObject)it.next();
			try{ 
				RGB gcRgb = gc.getColor(data, sampleId);
				if(gcRgb != null) {
					return gcRgb;
				}
			} catch(Exception e) {
				GmmlVision.log.error("ColorSetObject " + gc + " could not evaluate data: " + e.getMessage());
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
				else if(name.equals(ColorCriterion.XML_ELEMENT_NAME))
					cs.addObject(new ColorCriterion(cs, elm));
				else if(name.equals(XML_ELM_COLOR_NCM))
					cs.setColor(ID_COLOR_NO_CRITERIA_MET, ColorConverter.parseColorElement(elm));
				else if(name.equals(XML_ELM_COLOR_NGF))
					cs.setColor(ID_COLOR_NO_GENE_FOUND, ColorConverter.parseColorElement(elm));
				else if(name.equals(XML_ELM_COLOR_NDF))
					cs.setColor(ID_COLOR_NO_DATA_FOUND, ColorConverter.parseColorElement(elm));
			} catch(Exception ex) {
				GmmlVision.log.error("Unable to parse colorset xml", ex);
			}
		}
		return cs;
	}
			
	static void printParseError(String criterion, Exception e) {
		GmmlVision.log.error("Unable to parse colorset data stored in " +
				"expression database: " + criterion, e);
		MessageDialog.openWarning(GmmlVision.getWindow().getShell(), 
					"Warning", "Unable to parse the colorset data in this expression dataset");
	}
	
	void fireModifiedEvent() {
		VisualizationManager.fireVisualizationEvent(
				new VisualizationEvent(this, VisualizationEvent.COLORSET_MODIFIED));
	}
}
