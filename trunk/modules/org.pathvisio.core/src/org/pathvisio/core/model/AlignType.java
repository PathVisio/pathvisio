// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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

/** possible values for the (horizontal) text alignment property */
public enum AlignType 
{ 
	LEFT("Left"), CENTER("Center"), RIGHT("Right");

	private final String gpmlName;
	private static Map<String, AlignType> byGpmlName = new HashMap<String, AlignType>();
	
	static {
		for (AlignType t : values()) byGpmlName.put (t.gpmlName, t);
	}
	
	private AlignType(String gpmlName)
	{
		this.gpmlName = gpmlName;
	}
	
	public static AlignType fromGpmlName(String value)
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

}