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

import java.util.*;

public enum LineType 
{
	LINE ("Line", "Line"),
	ARROW ("Arrow", "Arrow"),
	TBAR ("TBar", "TBar"),
	RECEPTOR ("Receptor", "Receptor"),
	LIGAND_SQUARE ("LigandSq", "LigandSquare"),
	RECEPTOR_SQUARE ("ReceptorSq", "ReceptorSquare"),
	LIGAND_ROUND ("LigandRd", "LigandRound"),
	RECEPTOR_ROUND ("ReceptorRd", "ReceptorRound");
	
	private LineType (String _mappName, String _gpmlName)
	{
		mappName = _mappName; 
		gpmlName = _gpmlName;
	}
	
	private String mappName;
	private String gpmlName;
	
	String getMappName() { return mappName; }
	String getGpmlName() { return gpmlName; }
	
	static private Map<String, LineType> gpmlMapping = initGpmlMapping();
	
	static private Map<String, LineType> initGpmlMapping()
	{
		Map<String, LineType> result = new HashMap<String, LineType>();
		for (LineType l : LineType.values())
		{
			result.put (l.getGpmlName(), l);
		}
		return result;
	}
	
	static LineType getByGpmlName(String value)
	{
		return gpmlMapping.get (value);
	}
}
