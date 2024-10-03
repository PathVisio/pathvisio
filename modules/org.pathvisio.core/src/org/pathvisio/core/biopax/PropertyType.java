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
package org.pathvisio.core.biopax;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties in BioPAX, only the properties
 * that are accessed by PathVisio need to be listed here.
 */
public enum PropertyType {
	TITLE("http://www.w3.org/2001/XMLSchema#string", 1),
	YEAR("http://www.w3.org/2001/XMLSchema#string", 1),
	AUTHORS("http://www.w3.org/2001/XMLSchema#string"),
	ID("http://www.w3.org/2001/XMLSchema#string", 1),
	DB("http://www.w3.org/2001/XMLSchema#string", 1),
	SOURCE("http://www.w3.org/2001/XMLSchema#string", 1),
	;

	String datatype;
	int maxCardinality;

	private static Map<String, PropertyType> lookupMap = new HashMap<String, PropertyType>();
	static {
		for (PropertyType pt : PropertyType.values())
			lookupMap.put(pt.name(), pt);
	}
	
	PropertyType(String datatype) {
		this(datatype, BiopaxProperty.UNBOUND);
	}

	PropertyType(String datatype, int maxCardinality) {
		this.datatype = datatype;
		this.maxCardinality = maxCardinality;
	}

	BiopaxProperty getProperty(String value) {
		return new BiopaxProperty(name(), value, datatype, maxCardinality);
	}
	
	public static PropertyType byName(String name)
	{
		return lookupMap.get(name);
	}
}
