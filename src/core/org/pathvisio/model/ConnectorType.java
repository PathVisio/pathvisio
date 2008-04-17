package org.pathvisio.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ConnectorType implements Comparable<ConnectorType> {
	private static final Map<String, ConnectorType> nameMappings = new HashMap<String, ConnectorType>();
	private static final Set<ConnectorType> values = new TreeSet<ConnectorType>();

	public static final ConnectorType STRAIGHT = new ConnectorType ("Straight");
	public static final ConnectorType ELBOW = new ConnectorType ("Elbow");
	public static final ConnectorType CURVED = new ConnectorType ("Curved");
	
	private String name;

	private ConnectorType (String name)
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
	public static ConnectorType create (String name)
	{
		return new ConnectorType(name);
	}

	/**
	   looks up the ConnectorType corresponding to that name.
	 */
	public static ConnectorType fromName (String value)
	{
		return nameMappings.get(value);
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
