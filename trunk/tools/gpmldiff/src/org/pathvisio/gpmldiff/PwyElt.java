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

import java.util.*;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;

/**
   A single element in a pathway, which can be a line, shape, datanode, etc.
*/
class PwyElt
{
	PathwayElement elt;
	public PathwayElement getElement () { return elt; }
	
	Map<String, String> contents = new HashMap<String, String>();
	
	public Map<String, String> getContents() { return contents; }
	
	PwyElt(PathwayElement _elt)
	{
		assert (_elt != null);
		elt = _elt;
		
		copyContents();
	}

	private void copyContents()
	{
		for (PropertyType prop : elt.getAttributes(true))
		{
			String attr = prop.tag();
			String val = "" + elt.getProperty (prop);
			contents.put (attr, val);
		}
	}
			
	String summary()
	{
		String result = "[" + elt.getObjectType();
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
		result += "]";
		return result;
	}

	/**
	   Show detailed modifications compared to another elt
	   call on oldElt.
	 */
	void writeModifications (PwyElt newElt, DiffOutputter outputter)
	{
		boolean opened = false; // indicates if modifyStart has been
								// sent already for current PwyElt.
		for (String key : contents.keySet())
		{
			if (newElt.contents.containsKey(key))
			{
				if (!contents.get(key).equals(newElt.contents.get(key)))
				{
					if (!opened)
					{
						outputter.modifyStart (this, newElt);
						opened = true;
					}
					outputter.modifyAttr (key, contents.get(key), newElt.contents.get(key));
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