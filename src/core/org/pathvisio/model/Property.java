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
package org.pathvisio.model;

/**
 * This interface defines a typed property.
 *
 * @author Mark Woon
 */
public interface Property {

	/**
	 * Gets the Id for this property.  Ids must be unique.
	 */
	String getId();

	/**
	 * @returns Name of property, used e.g. as row header in the properties table.
	 */
	String getName();

	/**
	 * Description of property, used e.g. as tooltip text when mousing over
	 * the properties table. Descriptions are optional.
	 * @returns description. May return null.
	 */
	String getDescription();


	/**
	 * Gets the data type for this property.
	 */
	PropertyType getType();


	/**
	 * Gets whether this property has accepts values.
	 */
	boolean isCollection();
}
