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

import java.util.HashMap;
import java.util.Map;

/**
 * Different possible outlines for Labels.
 * These can be set/get on the outlineType property of PathwayElements
 */
public enum OutlineType
{
	NONE ("None"),
	RECTANGLE ("Rectangle"),
	ROUNDED_RECTANGLE ("RoundedRectangle");

	private String tag;

	static private Map<String, OutlineType> tags =
		new HashMap<String, OutlineType>();

	static
	{
		for (OutlineType o : values())
		{
			tags.put (o.getTag(), o);
		}
	}

	private OutlineType (String tag)
	{
		this.tag = tag;
	}

	static public OutlineType fromTag (String tag)
	{
		return tags.get (tag);
	}

	static public String[] getTags()
	{
		String[] result = new String[OutlineType.values().length];
		int i = 0;
		for (OutlineType o : OutlineType.values())
		{
			result[i] = o.getTag();
			i++;
		}
		return result;
	}

	public String getTag ()
	{
		return tag;
	}

	public String toString ()
	{
		return tag;
	}
}