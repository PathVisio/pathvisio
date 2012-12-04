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
package org.pathvisio.core.biopax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.jdom.Element;
import org.pathvisio.core.biopax.reflect.BiopaxProperty;
import org.pathvisio.core.biopax.reflect.Namespaces;
import org.pathvisio.core.biopax.reflect.PropertyType;
import org.pathvisio.core.biopax.reflect.PublicationXref;
import org.pathvisio.core.model.GpmlFormat;

/**
 * Represents a fragment of the embedded biopax of a pathway.
 */
public class BiopaxNode
{
	private Element wrapped;
	
	private Set<PropertyType> validProperties;
	private List<BiopaxProperty> properties;

	public BiopaxNode() {
		wrapped = new Element("unnamed");
		wrapped.setNamespace(GpmlFormat.BIOPAX);
		validProperties = new HashSet<PropertyType>();
		properties = new ArrayList<BiopaxProperty>();
	}

	protected void setValidProperties(PropertyType[] valid) {
		validProperties = new HashSet<PropertyType>();
		for(PropertyType pt : valid) validProperties.add(pt);
	}

	public void addProperty(BiopaxProperty p) {
		//Check if property is valid for any subclass of BiopaxElement
		if (this.getClass() != BiopaxNode.class)
		{
			PropertyType pt = PropertyType.byName(p.getName());
			if(!validProperties.contains(pt)) {
				throw new IllegalArgumentException("Property " + p.getName() + " is not valid for " + this);
			}
		}
		List<BiopaxProperty> existingProps = getProperties(p.getName());
		if(p.getMaxCardinality() != BiopaxProperty.UNBOUND &&
				existingProps.size() >= p.getMaxCardinality()) {
			//Replace the first occuring property
			int first = getFirstPropertyIndex(p.getName());
			properties.remove(first);
			properties.add(first, p);
		} else {
			properties.add(p);
		}
		wrapped.addContent(p);
	}

	public void removeProperty(BiopaxProperty p) {
		BiopaxProperty existing = properties.get(properties.indexOf(p));
		if(existing != null) {
			properties.remove(p);
			wrapped.removeContent(p);
		}
	}

	private int getFirstPropertyIndex(String name) {
		int i = 0;
		for(BiopaxProperty p : properties) {
			if(p.getName().equals(name)) break;
			i++;
		}
		return i;
	}

	/**
	 * Gets all the property objects by name
	 * @param name
	 * @return
	 */
	public List<BiopaxProperty> getProperties(String name) {
		List<BiopaxProperty> props = new ArrayList<BiopaxProperty>();
		for(BiopaxProperty p : properties) {
			if(p.getName().equals(name)) {
				props.add(p);
			}
		}
		return props;
	}

	/**
	 * Returns the first property with the given name
	 * @param name
	 * @return
	 */
	public BiopaxProperty getProperty(String name) {
		for(BiopaxProperty p : properties) {
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public String getId() {
		return wrapped.getAttributeValue("id", Namespaces.RDF);
	}

	public void setId(String id) {
		wrapped.setAttribute("id", id, Namespaces.RDF);
	}

	public static BiopaxNode fromXML(Element xml) 
	{
		String className = xml.getName();
		BiopaxNode result = null;
		if ("PublicationXref".equalsIgnoreCase(className))
		{
			// compatibility hack, see bug #1022
			xml.setName("PublicationXref");
			
			result = new PublicationXref();
		}
		else
		{
			result = new BiopaxNode();
		}
		result.loadXML(xml);
		return result;
	}

	void loadXML(Element xml) {
		wrapped.setName(xml.getName());
		wrapped.setNamespace(xml.getNamespace());
		String id = xml.getAttributeValue("id", Namespaces.RDF);
		if (id != null) setId(id);
		for(Object child : xml.getChildren()) {
			if(child instanceof Element) {
				addProperty(new BiopaxProperty((Element)child));
			}
		}
	}

	/**
	 * Check if this element equals the given element by comparing the properties.
	 * @param e
	 * @return
	 */
	public boolean propertyEquals(BiopaxNode e) {
		return propertyEquals(e, null);
	}

	/**
	 * Check if this element equals the given element by comparing the properties.
	 * Properties that are in the ignore collection are not taken into account.
	 * @param e
	 * @param ignore
	 * @return
	 */
	public boolean propertyEquals(BiopaxNode e, Collection<PropertyType> ignore) {
		for(PropertyType p : PropertyType.values()) {
			//Continue if property is in ignore list
			if(ignore != null && ignore.contains(p)) continue;

			//Get the properties for this property type
			List<BiopaxProperty> pv1 = getProperties(p.name());
			List<BiopaxProperty> pv2 = e.getProperties(p.name());
			//Compare the property list by the property values
			ListIterator<BiopaxProperty> e1 = pv1.listIterator();
			ListIterator<BiopaxProperty> e2 = pv2.listIterator();
			while(e1.hasNext() && e2.hasNext()) {
			    BiopaxProperty o1 = e1.next();
			    BiopaxProperty o2 = e2.next();
			    if(o1 == null || o2 == null) {
			    	if(!(o1 == null && o2 == null)) {
			    		return false;
			    	}
			    } else {
			    	if(o1.getValue() != null) {
			    		if(!o1.getValue().equals(o2.getValue())) {
			    			return false;
			    		}
			    	}
			    }
			}
		}
		return true;
	}

	/** @deprecated warning: internal, don't use. */
	public Element getWrapped()
	{
		return wrapped;
	}

	protected void setName(String string)
	{
		wrapped.setName(string);
	}


	public String getName()
	{
		return wrapped.getName();
	}
}
