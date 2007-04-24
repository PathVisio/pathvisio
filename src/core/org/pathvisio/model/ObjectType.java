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
package org.pathvisio.model;

import java.util.Arrays;
import java.util.List;

public class ObjectType {
	 
	//TODO: enum
	public static final int MIN_VALID = 0; // lowest valid value
	public static final int SHAPE = 0;
	public static final int DATANODE = 1;
	public static final int LABEL = 2;
	public static final int LINE = 3;
	public static final int LEGEND = 4;
	public static final int INFOBOX = 5;
	public static final int MAPPINFO = 6;
	public static final int MAX_VALID = 6;
	 
	// Some mappings to Gpml TAGS
	 // TODO: is this actually used?
	private static final List<String> tagMappings = Arrays.asList(new String[] {
		"Shape", "DataNode", "Label", "Line", "Legend", "InfoBox", 
		"Pathway"
	});

	public static int getTagMapping(String value)
	{
		return tagMappings.indexOf(value);
	}
	
	public static String getTagMapping(int value)
	{
		return (String)tagMappings.get(value);
	}
}
