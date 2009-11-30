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
package org.pathvisio.gpmldiff;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;

/**
   Utility class for pathway element methods related to gpmldiff.
*/
class PwyElt
{
	static String summary(PathwayElement elt)
	{
		if (elt == null) return "null"; // TODO, why is this necessary?
		String result = "[" + elt.getObjectType().getTag();
		Set<PropertyType> props = elt.getStaticPropertyKeys();
		if (props.contains(PropertyType.TEXTLABEL))
			result += ",lbl=" + elt.getStaticProperty(PropertyType.TEXTLABEL);
		if (props.contains(PropertyType.WIDTH))
			result += ",w=" + elt.getStaticProperty(PropertyType.WIDTH);
		if (props.contains(PropertyType.HEIGHT))
			result += ",h=" + elt.getStaticProperty(PropertyType.HEIGHT);
		if (props.contains(PropertyType.CENTERX))
			result += ",cx=" + elt.getStaticProperty(PropertyType.CENTERX);
		if (props.contains(PropertyType.CENTERY))
			result += ",cy=" + elt.getStaticProperty(PropertyType.CENTERY);
		if (props.contains(PropertyType.STARTX))
			result += ",x1=" + elt.getStaticProperty(PropertyType.STARTX);
		if (props.contains(PropertyType.STARTY))
			result += ",y1=" + elt.getStaticProperty(PropertyType.STARTY);
		if (props.contains(PropertyType.ENDX))
			result += ",x2=" + elt.getStaticProperty(PropertyType.ENDX);
		if (props.contains(PropertyType.ENDY))
			result += ",y2=" + elt.getStaticProperty(PropertyType.ENDY);
		if (props.contains(PropertyType.GRAPHID))
			result += ",id=" + elt.getStaticProperty(PropertyType.GRAPHID);
		if (props.contains(PropertyType.STARTGRAPHREF))
			result += ",startref=" + elt.getStaticProperty(PropertyType.STARTGRAPHREF);
		if (props.contains(PropertyType.ENDGRAPHREF))
			result += ",endref=" + elt.getStaticProperty(PropertyType.ENDGRAPHREF);
		if (props.contains(PropertyType.MAPINFONAME))
			result += ",title=" + elt.getStaticProperty(PropertyType.MAPINFONAME);
		if (props.contains(PropertyType.AUTHOR))
			result += ",author=" + elt.getStaticProperty(PropertyType.AUTHOR);
		result += "]";
		return result;
	}

	static Map<String, String> getContents(PathwayElement elt)
	{
		Map<String, String> result = new HashMap<String, String>();

		for (PropertyType prop : elt.getStaticPropertyKeys())
		{
			String attr = prop.tag();
			String val = "" + elt.getStaticProperty (prop);
			result.put (attr, val);
		}
		return result;
	}
	/**
	   Show detailed modifications compared to another elt
	   call on oldElt.
	 */
	static void writeModifications (PathwayElement oldElt, PathwayElement newElt, DiffOutputter outputter)
	{
		Map<String, String> oldContents = getContents (oldElt);
		Map<String, String> newContents = getContents (newElt);

		boolean opened = false; // indicates if modifyStart has been
								// sent already for current PwyElt.
		for (String key : oldContents.keySet())
		{
			if (key.equals ("BoardWidth") || key.equals ("BoardHeight"))
			{
				// ignore board width and height
				continue;
			}
			if (newContents.containsKey(key))
			{
				if (!oldContents.get(key).equals(newContents.get(key)))
				{
					if (!opened)
					{
						outputter.modifyStart (oldElt, newElt);
						opened = true;
					}
					outputter.modifyAttr (key, oldContents.get(key), newContents.get(key));
				}
			}
		}
		if (opened)
		{
			outputter.modifyEnd();
			opened = false;
		}
	}
}