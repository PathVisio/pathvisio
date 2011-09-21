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

/**
   Possible values for PathwayElement.getObjectType(), such as "DataNode" or "Shape"
 */
public enum ObjectType
{
	/** any shape with width and height */
	SHAPE ("Shape"),
	
	/** a rectangle that contains a link to an online biological database */
	DATANODE ("DataNode"),
	
	/** a piece of text */
	LABEL ("Label"),

	/** a connector. Can be straight, or can consist of multiple line segments */
	LINE ("Line"),
	
	/** Zero or one per pathway. Placeholder object to let visualization plugins draw a legend */
	LEGEND ("Legend"),
	
	/** One per pathway. TODO: unused. */
	INFOBOX ("InfoBox"),
	
	/** The pathway description, one per pathway. In GPML this is the root tag */
	MAPPINFO ("Pathway"),
	
	/** a grouping of pathway elements */
	GROUP ("Group"),
	
	/** a pool of BioPAX definitions */
	BIOPAX ("Biopax"),
	
	/** similar to DataNode, but State is always
	 * attached to - and specified relative to - another DataNode */
	STATE ("State");

	private String tag;
	static private final Map<String, ObjectType> TAG_MAP = new HashMap<String, ObjectType>();
	static
	{
		for (ObjectType o : ObjectType.values())
		{
			TAG_MAP.put (o.tag, o);
		}
	}

	/**
	 * @param aTag tag used in Gpml for this object type.
	 */
	private ObjectType (String aTag)
	{
		tag = aTag;
	}

	/**
	 * return the ObjectType that corresponds to a certain tag.
	 * Returns null if no such ObjectType exists.
	 */
	public static ObjectType getTagMapping(String value)
	{
		if (TAG_MAP.containsKey(value))
		{
			return TAG_MAP.get (value);
		}
		else
		{
			return null;
		}
	}

	/**
	 * returns the GPML tag corresponding to this object type,
	 * can also function as a human-readable description.
	 */
	public String getTag()
	{
		return tag;
	}
}
