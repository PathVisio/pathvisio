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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Refers to the biological type of a DataNode, e.g. RNA, PROTEIN etc.
 * Most existing pathways use either METABOLITE or GENEPRODUCT. The use of the other
 * types is not clearly defined.
 */
public enum DataNodeType {
	UNKOWN("Unknown"),
	RNA("Rna"),
	PROTEIN("Protein"),
	COMPLEX("Complex"),
	GENEPRODUCT("GeneProduct"),
	METABOLITE("Metabolite");

	private static final Map<String, DataNodeType> NAME_MAP = new HashMap<String, DataNodeType>();

	static
	{
		for (DataNodeType dnt : DataNodeType.values())
		{
			NAME_MAP.put (dnt.getGpmlName(), dnt);
		}
	}

	private DataNodeType (String gpmlName) {
		this.gpmlName = gpmlName;
	}
	private String gpmlName;

	String getGpmlName() { return gpmlName; }
	public String toString() { return getGpmlName(); }

	static public String[] getNames()
	{
		List<String> result = new ArrayList<String>();
		for (DataNodeType s : DataNodeType.values())
		{
			result.add("" + s.gpmlName);
		}
		String [] resultArray = new String [result.size()];
		return result.toArray(resultArray);
	}

	public static DataNodeType byName(String name)
	{
		return NAME_MAP.get (name);
	}
}
