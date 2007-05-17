package org.pathvisio.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum GroupStyle {
	STACK("Stack"),
	COMPLEX("Complex"),
	BOX("Box"),
	NONE("None");
//	NAMEDSELECTION("NamedSelection"),
//	METANODE("Metanode");
	
	
	private static final Map<String, GroupStyle> gpmlMappings = initGpmlMappings();	
	private String gpmlName;
	
	static Map<String, GroupStyle> initGpmlMappings()
	{
		Map<String, GroupStyle> result = new HashMap<String, GroupStyle>();
		
		for (GroupStyle s : GroupStyle.values())
		{
			result.put(s.gpmlName, s);
		}
		return result;
	}

	public static GroupStyle fromGpmlName (String value)
	{
		return gpmlMappings.get(value);
	}
	
	public static String toGpmlName (GroupStyle value)
	{
		return value.gpmlName;
	}

	
	private GroupStyle (String gpmlName) {
		this.gpmlName = gpmlName;
	}
	
	String getGpmlName() { return gpmlName; }
	public String toString() { return getGpmlName(); }
	
	static public String[] getNames()
	{
		List<String> result = new ArrayList<String>();		
		for (GroupStyle s : GroupStyle.values())
		{
			result.add("" + s.gpmlName);
		}
		String [] resultArray = new String [result.size()];
		return result.toArray(resultArray);
	}
}
