package org.pathvisio.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AnchorType implements Comparable<AnchorType> {
	private static final Map<String, AnchorType> nameMappings = new HashMap<String, AnchorType>();
	private static final Set<AnchorType> values = new TreeSet<AnchorType>();
	
	public static final AnchorType NONE = new AnchorType("None");
	public static final AnchorType CIRCLE = new AnchorType("Circle");
	
	private String name;

	private AnchorType (String name)
	{
		if (name == null) { throw new NullPointerException(); }
		
		this.name  = name;
		values.add(this);
		nameMappings.put (name, this);
	}

	/**
	   Create an object and add it to the list.

	   For extending the enum.
	 */
	public static AnchorType create (String name)
	{
		return new AnchorType(name);
	}

	/**
	   looks up the AnchorType corresponding to that name.
	 */
	public static AnchorType fromName (String value)
	{
		return nameMappings.get(value);
	}

	/**
	   Stable identifier for this AnchorType.
	 */
	public String getName ()
	{
		return name;
	}

	static public AnchorType[] getValues()
	{
		return values.toArray(new AnchorType[0]);
	}

	public String toString()
	{
		return name;
	}

	public int compareTo(AnchorType o) {
		return toString().compareTo(o.toString());
	}
}
