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
package org.pathvisio.model;

import java.util.Arrays;
import java.util.List;

/**
   Possible values for PathwayElement.getObjectType(), such as "DataNode" or "Shape"
 */   
public class ObjectType
{	 
	//TODO: enum
	public static final int MIN_VALID = 0; // lowest valid value
	public static final int SHAPE = 0;
	public static final int DATANODE = 1;
	public static final int LABEL = 2;
	public static final int LINE = 3;
	public static final int LEGEND = 4;
	public static final int INFOBOX = 5;
	public static final int MAPPINFO = 6;
	public static final int GROUP = 7;
	public static final int BIOPAX = 8;
	public static final int STATE = 9;
	public static final int MAX_VALID = 9;
	 
	// Some mappings to Gpml TAGS
	private static final List<String> TAG_MAPPINGS = Arrays.asList(new String[] {
		"Shape", "DataNode", "Label", "Line", "Legend", "InfoBox", 
		"Pathway", "Group", "Biopax", "State"
	});

	public static int getTagMapping(String value)
	{
		return TAG_MAPPINGS.indexOf(value);
	}
	
	public static String getTagMapping(int value)
	{
		return (String)TAG_MAPPINGS.get(value);
	}
}
