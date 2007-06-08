// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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
package org.pathvisio.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum GroupStyle {
	STACK("Stack"),
	COMPLEX("Complex"),
	BOX("Box"),
	NONE("None");
//	NAMEDSELECTION("NamedSelection"),
//	METANODE("Metanode");
	
	
	private static final Map<String, GroupStyle> gpmlMappings = initGpmlMappings();	
	private String gpmlName;
	
	static Map<String, GroupStyle> initGpmlMappings()
	{
		Map<String, GroupStyle> result = new HashMap<String, GroupStyle>();
		
		for (GroupStyle s : GroupStyle.values())
		{
			result.put(s.gpmlName, s);
		}
		return result;
	}

	public static GroupStyle fromGpmlName (String value)
	{
		return gpmlMappings.get(value);
	}
	
	public static String toGpmlName (GroupStyle value)
	{
		return value.gpmlName;
	}

	
	private GroupStyle (String gpmlName) {
		this.gpmlName = gpmlName;
	}
	
	String getGpmlName() { return gpmlName; }
	public String toString() { return getGpmlName(); }
	
	static public String[] getNames()
	{
		List<String> result = new ArrayList<String>();		
		for (GroupStyle s : GroupStyle.values())
		{
			result.add("" + s.gpmlName);
		}
		String [] resultArray = new String [result.size()];
		return result.toArray(resultArray);
	}
}
