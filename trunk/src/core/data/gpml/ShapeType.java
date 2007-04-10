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
package data.gpml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ShapeType 
{
	
	RECTANGLE ("Rectangle", "Rectangle"),
	OVAL ("Oval", "Oval"),
	ARC ("Arc", "Arc"),
	CELLA ("CellA", "CellA"),
	RIBOSOME ("Ribosome", "Ribosome"),
	ORGANA ("OrganA", "OrganA"),
	ORGANB ("OrganB", "OrganB"),
	ORGANC ("OrganC", "OrganC"),
	PROTEINB ("ProteinB", "ProteinComplex"),
	TRIANGLE ("Poly", "Triangle"), // poly in MAPP
	VESICLE ("Vesicle", "Vesicle"),
	PENTAGON ("Poly", "Pentagon"), // poly in MAPP
	HEXAGON ("Poly", "Hexagon"), // poly in MAPP
	BRACE ("Brace", "Brace");
	
	private static final Map<String, ShapeType> mappMappings = initMappMappings();
	private static final Map<String, ShapeType> gpmlMappings = initGpmlMappings();	
	private String gpmlName;
	private String mappName;
	
	ShapeType(String _mappName, String _gpmlName)
	{
		mappName = _mappName;
		gpmlName = _gpmlName;
	}
	
	static Map<String, ShapeType> initMappMappings()
	{
		Map<String, ShapeType> result = new HashMap<String, ShapeType>();
		
		for (ShapeType s : ShapeType.values())
		{
			result.put(s.mappName, s);
		}
		return result;
	}

	static Map<String, ShapeType> initGpmlMappings()
	{
		Map<String, ShapeType> result = new HashMap<String, ShapeType>();
		
		for (ShapeType s : ShapeType.values())
		{
			result.put(s.gpmlName, s);
		}
		return result;
	}


	/*
	 * Warning when using fromMappName: in case value == Poly, 
	 * this will return Triangle. The caller needs to check for 
	 * this special
	 * case.
	 */
	public static ShapeType fromMappName (String value)
	{
		return mappMappings.get(value);
	}
	
	public static String toMappName (ShapeType value)
	{
		return value.mappName;
	}

	public static ShapeType fromGpmlName (String value)
	{
		return gpmlMappings.get(value);
	}
	
	public static String toGpmlName (ShapeType value)
	{
		return value.gpmlName;
	}
	
	static public String[] getNames()
	{
		List<String> result = new ArrayList<String>();		
		for (ShapeType s : ShapeType.values())
		{
			result.add("" + s.gpmlName);
		}
		String [] resultArray = new String [result.size()];
		return result.toArray(resultArray);
	}

}

