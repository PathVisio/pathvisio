package org.pathvisio.model;

import java.util.ArrayList;
import java.util.List;

public enum DataNodeType {
	UNKOWN("Unknown"),
	RNA("Rna"),
	PROTEIN("Protein"),
	COMPLEX("Complex"),
	GENEPRODUCT("GeneProduct"),
	METABOLITE("Metabolite");
	
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
}
