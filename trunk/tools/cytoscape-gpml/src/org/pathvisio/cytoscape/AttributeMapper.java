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
package org.pathvisio.cytoscape;

import cytoscape.data.CyAttributes;

import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.StaticProperty;

/**
 * Interface for classes that keep track of mapping from
 * Cytoscape attributes to GPML properties.
 */
public interface AttributeMapper {
	/**
	 * Set a default value that will be used when no mapping is available
	 */
	public void setDefaultValue(StaticProperty prop, Object value);

	public Object getDefaultValue(StaticProperty prop);

	/**
	 * Sets a two-way mapping, should be equivalent to:<br>
	 * <code>
	 * setAttributeToPropertyMapping(attr, prop);
	 * setPropertyToAttributeMapping(prop, attr);
	 * </code>
	 */
	public void setMapping(String attr, StaticProperty prop);
	/**
	 * Set a one-way mapping, from attribute to property
	 */
	public void setAttributeToPropertyMapping(String attr, StaticProperty prop);
	/**
	 * Set a one-way mapping, from property to attribute
	 */
	public void setPropertyToAttributeMapping(StaticProperty prop, String attr);
	public StaticProperty getMapping(String attr);
	public String getMapping(StaticProperty prop);
	public void attributesToProperties(String id, PathwayElement elm, CyAttributes attr);
	public void propertiesToAttributes(String id, PathwayElement elm, CyAttributes attr);

	/**
	 * Check whether a property is protected. If a property is protected, no attributes should
	 * be mapped to and from this property.
	 */
	public boolean isProtected(StaticProperty prop);
	
	/**
	 * Protect a property. If a property is protected, no attributes should
	 * be mapped to and from this property.
	 */
	public void protect(StaticProperty prop);

	/**
	 * Unprotect a property. If a property is unprotected, the attributemapper
	 * will map attribute value from and to this property
	 */
	public void unprotect(StaticProperty prop);
	
	/**
	 * Check whether a prop is to be a hidden attribute. If a property is to be hidden, the its 
	 * associated attribute should be set to isVisible(false).
	 */
	public boolean isHidden(StaticProperty prop);
	
}
