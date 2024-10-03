/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.gpmldiff;

import java.util.HashMap;
import java.util.Map;

import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.StaticProperty;

/**
   Utility class for pathway element methods related to gpmldiff.
*/
class PwyElt
{
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
