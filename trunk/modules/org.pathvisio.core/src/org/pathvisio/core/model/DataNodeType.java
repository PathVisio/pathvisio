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
package org.pathvisio.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
   Extensible enum
 */
public class DataNodeType {
	private static final Map<String, DataNodeType> NAME_MAP = new HashMap<String, DataNodeType>();
	private static List<DataNodeType> values = new ArrayList<DataNodeType>();

	public static final DataNodeType UNKOWN = new DataNodeType("Unknown");
	public static final DataNodeType RNA = new DataNodeType("Rna");
	public static final DataNodeType PROTEIN = new DataNodeType("Protein");
	@Deprecated
	public static final DataNodeType COMPLEX = new DataNodeType("Complex");
	public static final DataNodeType GENEPRODUCT = new DataNodeType("GeneProduct");
	public static final DataNodeType METABOLITE = new DataNodeType("Metabolite");
	public static final DataNodeType PATHWAY = new DataNodeType("Pathway");

	private String name;

	/**
	   The constructor is private so we have to use the "create"
	   method to add new ShapeTypes. In the create method we make sure
	   that the same object can't get added twice.
	   <p>
	   Note that mappName may be null for Shapes that are not supported by GenMAPP.
	 */
	private DataNodeType(String name)
	{
		NAME_MAP.put (name, this);
		this.name = name;
		// and add it to the array list.
		values.add (this);
	}

	/**
	   Create an object and add it to the list.

	   For extending the enum.
	 */
	public static DataNodeType create (String name)
	{
		if (NAME_MAP.containsKey (name))
		{
			return NAME_MAP.get (name);
		}
		else
		{
			return new DataNodeType(name);
		}
	}

	/**
	 *  @param value the name of the DataNodeType to be returned
     *  @return the DataNodeType corresponding to that name.
	 */
	public static DataNodeType byName (String value)
	{
		return NAME_MAP.get(value);
	}

	/**
	   @return Stable identifier for this DataNodeType.
	 */
	public String getName ()
	{
		return name;
	}

	/**
	   @return the names of all registered DataNode types, in such a way that the index
	   is equal to it's ordinal value.
	   <p>
	   i.e. DataNodeType.fromName(DataNodeType.getNames[n]).getOrdinal() == n
	 */
	static public String[] getNames()
	{
		String[] result = new String[values.size()];

		for (int i = 0; i < values.size(); ++i)
		{
			result[i] = values.get(i).getName();
		}
		return result;
	}

	static public DataNodeType[] getValues()
	{
		return values.toArray(new DataNodeType[0]);
	}

	public String toString()
	{
		return name;
	}
}
