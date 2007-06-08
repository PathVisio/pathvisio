// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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

import org.jdom.*;
import java.util.*;

/**
   A single element in a pathway, which can be a line, shape, datanode, etc.
*/
class PwyElt
{
	Element elt;
	Map<String, String> contents = new HashMap<String, String>();
	
	public Map<String, String> getContents() { return contents; }
	
	PwyElt(Element _elt)
	{
		assert (_elt != null);
		elt = _elt;
		
		copyContents(elt, "");
	}

	/**
	   Recursive function that copies all attributes and children of an element
	   to a list of strings.
	*/
	private void copyContents(Element elt, String base)
	{
		String newb = base + "/" + elt.getName();
				
		// copy text value, if there is one
		String txt = elt.getTextTrim();
		if (txt.length() > 0)
		{
			contents.put (newb + ".text()", txt);
		}
		// copy attributes
		for (Attribute a : (List<Attribute>)elt.getAttributes())
		{
			contents.put (newb + "." + a.getName(), a.getValue());
		}
		// recursively copy children
		for (Element child : (List<Element>)elt.getChildren())
		{
			copyContents (child, newb);
		}
	}
		
	String summary()
	{
		String result = "[" + elt.getName();
		String tmp;
		if ((tmp = elt.getAttributeValue("TextLabel")) != null) result += ",lbl=" + tmp;
		if ((tmp = elt.getAttributeValue("ObjectType")) != null) result += ",ot=" + tmp;
		for (Element g : (List<Element>)elt.getChildren("Graphics", elt.getNamespace()))
		{
			if ((tmp = g.getAttributeValue("Width")) != null) result += ",w=" + tmp;
			if ((tmp = g.getAttributeValue("Height")) != null) result += ",h=" + tmp;
			
			int i = 0;
			for (Element p : (List<Element>)g.getChildren("Point", elt.getNamespace()))
			{
				i++;
				result += ",x" + i + "=" + p.getAttributeValue("x");
				result += ",y" + i + "=" + p.getAttributeValue("y");
			}
		}
		if ((tmp = elt.getAttributeValue("CenterX")) != null) result += ",cx=" + tmp;
		if ((tmp = elt.getAttributeValue("CenterY")) != null) result += ",cy=" + tmp;
		result += "]";
		return result;
	}

	void writeModifications (PwyElt other, DiffOutputter outputter)
	{
		//TODO: insertions / deletions
				
		for (String key : contents.keySet())
		{
			if (other.contents.containsKey(key))
			{
				if (!contents.get(key).equals(other.contents.get(key)))
				{
					outputter.modify (this, key, contents.get(key), other.contents.get(key));
				}
			}
		}			
	}
}