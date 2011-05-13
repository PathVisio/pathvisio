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
package org.pathvisio.core.gpmldiff;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.StaticProperty;

/**
   Utility class for pathway element methods related to gpmldiff.
*/
class PwyElt
{
	static String summary(PathwayElement elt)
	{
		if (elt == null) return "null"; // TODO, why is this necessary?
		String result = "[" + elt.getObjectType().getTag();
		Set<StaticProperty> props = elt.getStaticPropertyKeys();
		if (props.contains(StaticProperty.TEXTLABEL))
			result += ",lbl=" + elt.getStaticProperty(StaticProperty.TEXTLABEL);
		if (props.contains(StaticProperty.WIDTH))
			result += ",w=" + elt.getStaticProperty(StaticProperty.WIDTH);
		if (props.contains(StaticProperty.HEIGHT))
			result += ",h=" + elt.getStaticProperty(StaticProperty.HEIGHT);
		if (props.contains(StaticProperty.CENTERX))
			result += ",cx=" + elt.getStaticProperty(StaticProperty.CENTERX);
		if (props.contains(StaticProperty.CENTERY))
			result += ",cy=" + elt.getStaticProperty(StaticProperty.CENTERY);
		if (props.contains(StaticProperty.STARTX))
			result += ",x1=" + elt.getStaticProperty(StaticProperty.STARTX);
		if (props.contains(StaticProperty.STARTY))
			result += ",y1=" + elt.getStaticProperty(StaticProperty.STARTY);
		if (props.contains(StaticProperty.ENDX))
			result += ",x2=" + elt.getStaticProperty(StaticProperty.ENDX);
		if (props.contains(StaticProperty.ENDY))
			result += ",y2=" + elt.getStaticProperty(StaticProperty.ENDY);
		if (props.contains(StaticProperty.GRAPHID))
			result += ",id=" + elt.getStaticProperty(StaticProperty.GRAPHID);
		if (props.contains(StaticProperty.STARTGRAPHREF))
			result += ",startref=" + elt.getStaticProperty(StaticProperty.STARTGRAPHREF);
		if (props.contains(StaticProperty.ENDGRAPHREF))
			result += ",endref=" + elt.getStaticProperty(StaticProperty.ENDGRAPHREF);
		if (props.contains(StaticProperty.MAPINFONAME))
			result += ",title=" + elt.getStaticProperty(StaticProperty.MAPINFONAME);
		if (props.contains(StaticProperty.AUTHOR))
			result += ",author=" + elt.getStaticProperty(StaticProperty.AUTHOR);
		result += "]";
		return result;
	}

	static Map<String, String> getContents(PathwayElement elt)
	{
		Map<String, String> result = new HashMap<String, String>();

		for (StaticProperty prop : elt.getStaticPropertyKeys())
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