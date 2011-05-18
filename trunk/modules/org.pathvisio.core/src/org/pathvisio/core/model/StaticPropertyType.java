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

package org.pathvisio.core.model;

/**
 * The properties in {@link StaticProperty} define properties of different types,
 * all the possible types are defined here.
 */
public enum StaticPropertyType implements PropertyType
{
	BOOLEAN,
	DOUBLE,
	INTEGER,
	DATASOURCE,
	LINESTYLE,
	COLOR,
	STRING,
	ORIENTATION,
	SHAPETYPE,
	LINETYPE,
	OUTLINETYPE,
	GENETYPE,
	FONT,
	ANGLE,
	ORGANISM,
	DB_ID,
	DB_SYMBOL,
	BIOPAXREF,
	COMMENTS,
	GROUPSTYLETYPE,
	ALIGNTYPE,
	VALIGNTYPE;

	private String id;


	private StaticPropertyType() {
		id = "core." + name();
		PropertyManager.registerPropertyType(this);
	}


	public String getId() {
		return id;
	}
}
