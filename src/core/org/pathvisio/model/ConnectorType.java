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
package org.pathvisio.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Different Connector types
 *
 * This is an extensible enum type, you can't instantiate ConnectorType directly,
 * you have to use the getInstance() method.
 *
 * default connector types are
 * STRAIGHT -> the shortest path between two points
 * ELBOW -> connects with horizontal or vertical segments and 90-degree angles
 * CURVED -> uses splines to generate a smooth curve while keeping the end-points
 * 	perpendicular to the connecting element.
 */
public class ConnectorType implements Comparable<ConnectorType> {
	private static Map<String, ConnectorType> nameMappings = new HashMap<String, ConnectorType>();
	private static Set<ConnectorType> values = new TreeSet<ConnectorType>();

	public static final ConnectorType STRAIGHT = new ConnectorType ("Straight");
	public static final ConnectorType ELBOW = new ConnectorType ("Elbow");
	public static final ConnectorType CURVED = new ConnectorType ("Curved");
	public static final ConnectorType SEGMENTED = new ConnectorType("Segmented");

	private String name;

	private ConnectorType (String name)
	{
		if (name == null) { throw new NullPointerException(); }

		this.name  = name;
		values.add(this);
		nameMappings.put (name, this);
	}

	/**
	 * returns a connectorType with a given name.
	 * If the connectorType doesn't exist yet, it is created.
	 * for extending the enum.
	 * 
	 * @deprecated Use {@link #fromName(String)} instead.
	 */
	public static ConnectorType getInstance (String name)
	{
		return fromName(name);
	}

	/**
	 * returns a connectorType with a given name.
	 * If the connectorType doesn't exist yet, it is created.
	 * for extending the enum.
	 */
	public static ConnectorType fromName (String name)
	{
		if (nameMappings.containsKey (name))
		{
			return nameMappings.get(name);
		}
		else
			return new ConnectorType(name);
	}

	/**
	   Stable identifier for this ConnectorType.
	 */
	public String getName ()
	{
		return name;
	}

	static public ConnectorType[] getValues()
	{
		return values.toArray(new ConnectorType[0]);
	}

	public String toString()
	{
		return name;
	}

	public int compareTo(ConnectorType o) {
		return toString().compareTo(o.toString());
	}
}
