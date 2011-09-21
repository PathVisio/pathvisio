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
package org.pathvisio.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This PropertyManager handles the registration/lookups of everything related to TypedProperties.
 * <p>
 * Plugins with custom Properties or PropertyTypes should register them here.
 *
 * @author Mark Woon
 */
public class PropertyManager {
	private static final Map<String, PropertyType> PROPERTY_TYPES = new HashMap<String, PropertyType>();
	private static final Map<String, Property> PROPERTIES = new HashMap<String, Property>();


	/**
	 * Private constructor - not meant to be instantiated.
	 */
	private PropertyManager() {
	}


	/**
	 * Registers a property type.  Must have unique IDs.
	 *
	 * @throws IllegalArgumentException if there is an existing type that uses the same ID
	 */
	public static void registerPropertyType(PropertyType type) {

		if (type != null) {
			if (PROPERTY_TYPES.containsKey(type.getId())) {
				throw new IllegalArgumentException("Duplicate Id: an existing type already uses the id '" + type.getId() + "'");
			}
			PROPERTY_TYPES.put(type.getId(), type);
		}
	}

	/**
	 * Gets the property type matching the given ID.
	 *
	 * @return the property type matching the given ID or null if none exists
	 */
	public static PropertyType getPropertyType(String id) {
		return PROPERTY_TYPES.get(id);
	}


	/**
	 * Registers a property.  Properties must have unique IDs.
	 *
	 * @throws IllegalArgumentException if there is an existing property that uses the same ID
	 */
	public static void registerProperty(Property prop) {

		if (prop != null) {
			if (PROPERTIES.containsKey(prop.getId())) {
				throw new IllegalArgumentException("Duplicate Id: an existing property already uses the id '" + prop.getId() + "'");
			}
			PROPERTIES.put(prop.getId(), prop);
		}
	}

	/**
	 * Gets the property matching the given ID.
	 *
	 * @return the property matching the given ID or null if none exists
	 */
	public static Property getProperty(String id) {
		return PROPERTIES.get(id);
	}
}
