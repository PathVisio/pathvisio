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
package org.pathvisio.gpmldiff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.model.ObjectType;
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
		String result = "[" + ObjectType.getTagMapping (elt.getObjectType());
		String tmp;
		List<PropertyType> props = elt.getAttributes(true);
		if (props.contains(PropertyType.TEXTLABEL))
			result += ",lbl=" + elt.getProperty(PropertyType.TEXTLABEL);
		if (props.contains(PropertyType.WIDTH))
			result += ",w=" + elt.getProperty(PropertyType.WIDTH);
		if (props.contains(PropertyType.HEIGHT))
			result += ",h=" + elt.getProperty(PropertyType.HEIGHT);
		if (props.contains(PropertyType.CENTERX))
			result += ",cx=" + elt.getProperty(PropertyType.CENTERX);
		if (props.contains(PropertyType.CENTERY))
			result += ",cy=" + elt.getProperty(PropertyType.CENTERY);
		if (props.contains(PropertyType.STARTX))
			result += ",x1=" + elt.getProperty(PropertyType.STARTX);
		if (props.contains(PropertyType.STARTY))
			result += ",y1=" + elt.getProperty(PropertyType.STARTY);		
		if (props.contains(PropertyType.ENDX))
			result += ",x2=" + elt.getProperty(PropertyType.ENDX);
		if (props.contains(PropertyType.ENDY))
			result += ",y2=" + elt.getProperty(PropertyType.ENDY);		
		if (props.contains(PropertyType.GRAPHID))
			result += ",id=" + elt.getProperty(PropertyType.GRAPHID);
		if (props.contains(PropertyType.MAPINFONAME))
			result += ",name=" + elt.getProperty(PropertyType.MAPINFONAME);
		if (props.contains(PropertyType.AUTHOR))
			result += ",author=" + elt.getProperty(PropertyType.AUTHOR);
		result += "]";
		return result;
	}

	static Map<String, String> getContents(PathwayElement elt)
	{
		Map<String, String> result = new HashMap<String, String>();

		for (PropertyType prop : elt.getAttributes(true))
		{
			String attr = prop.tag();
			String val = "" + elt.getProperty (prop);
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