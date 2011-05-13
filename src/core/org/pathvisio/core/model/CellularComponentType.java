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
package org.pathvisio.core.model;

import java.util.HashMap;
import java.util.Map;

/** Possible values for the cellular component property. 
 * These values describe the biological meaning of an element, it does
 * not have to correlate with a particular appearance.
 */
public enum CellularComponentType 
{ 
	NONE("None"), CELL("Cell"), NUCLEUS("Nucleus"), MITOCHONDRIA("Mitochondria"), 
		GOLGIAPPARATUS("Golgi Apparatus"), ENDOPLASMICRETICULUM("Endoplasmic Reticulum"), SARCOPLASMICRETICULUM("Sarcoplasmic Reticulum"), VESICLE("Vesicle"), 
		ORGANELLE("Organelle"), NUCLEOLUS("Nucleolus"), VACUOLE("Vacuole"), LYSOSOME("Lysosome"), CYTOSOL("Cytosol region"), 
		EXTRACELLULAR("Extracellular region"), MEMBRANE("Membrane region");

	private final String gpmlName;
	private static Map<String, CellularComponentType> byGpmlName = new HashMap<String, CellularComponentType>();
	
	//Temporary Dynamic Property for cellular component
	//TODO: refactor as Static Property with next GPML update
	public final static String CELL_COMPONENT_KEY = "org.pathvisio.CellularComponentProperty";
	
	public static final PropertyType CELL_COMPONENT_TYPE = new PropertyType()
	{
		public String getId()
		{
			return "core.CellularComponentType";
		}
	};
	
	public static final Property CELL_COMPONENT_PROPERTY = new Property () {
		public String getId() {
			return CELL_COMPONENT_KEY;
		}
		
		public String getDescription() {
			return "This property associates Shapes with cellular component terms";
		}
		
		public String getName() {
			return "Cellular Component";
		}
		
		public PropertyType getType() {
			return CELL_COMPONENT_TYPE;
		}
		
		public boolean isCollection() {
			return false;
		}
	};

	static {
		for (CellularComponentType t : values()) byGpmlName.put (t.gpmlName, t);
	}
	
	private CellularComponentType(String gpmlName)
	{
		this.gpmlName = gpmlName;
	}
	
	public static CellularComponentType fromGpmlName(String value)
	{
		return byGpmlName.get(value);
	}
	
	public String getGpmlName()
	{
		return gpmlName;
	}
	
	public static String[] getNames() 
	{ 
		String[] result = new String[values().length];
		for (int i = 0; i < values().length; ++i) result[i] = values()[i].gpmlName;
		return result;
	}
	
	public String toString()
	{
		return gpmlName;
	}

}