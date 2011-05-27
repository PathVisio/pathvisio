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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Extensible enum type for Group Styles.
 *
 * Groups can have different biological meanings (e.g. protein Complex), and
 * can be rendered in different ways based on that.
 */
public class GroupStyle implements Comparable<GroupStyle> {
	private static Map<String, GroupStyle> nameMappings = new HashMap<String, GroupStyle>();
	private static Set<GroupStyle> values = new TreeSet<GroupStyle>();
	
	public static final double DEFAULT_M_MARGIN = 8; //Make the bounds slightly
	  //larger than the summed bounds
	  //of the containing elements
	public static final double COMPLEX_M_MARGIN = 12;
	
	public static final GroupStyle NONE = new GroupStyle ("None");

	/**
	 * Style used to group objects for drawing convenience.
	 */
	public static final GroupStyle GROUP = new GroupStyle ("Group");

	/**
	 * Style used to represent a group of objects that belong to a complex.
	 */
	public static final GroupStyle COMPLEX = new GroupStyle ("Complex", false, COMPLEX_M_MARGIN);

	/**
	 * Style used to represent a group of objects that belong to a pathway.
	 */
	public static final GroupStyle PATHWAY = new GroupStyle ("Pathway");
	
	private String name;
    private boolean disallowLinks;
    private double mMargin;
    
	private GroupStyle (String name)
    {
        this(name, false, DEFAULT_M_MARGIN);
    }    

    private GroupStyle (String name, boolean disallowLinks)
	{
    	this (name, disallowLinks, DEFAULT_M_MARGIN);
	}

    private GroupStyle (String name, boolean disallowLinks, double mMargin)
	{
		if (name == null) { throw new NullPointerException(); }

		this.name  = name;
        this.disallowLinks = disallowLinks;
        this.mMargin = mMargin;
        values.add(this);
		nameMappings.put (name, this);
	}

	/**
	   Create an object and add it to the list.

	   For extending the enum.
	 */
	public static GroupStyle create (String name)
	{
		return new GroupStyle(name);
	}

    /**
       Create an object and add it to the list.

       For extending the enum.
     */
    public static GroupStyle create (String name, boolean disallowLinks)
    {
        return new GroupStyle(name, disallowLinks);
    }


    /**
	   looks up the ConnectorType corresponding to that name.
	 */
	public static GroupStyle fromName (String value)
	{
		return nameMappings.get(value);
	}

	/**
	   looks up the ConnectorType corresponding to its GPML name.
	   @deprecated use {@link #fromName(String)} instead.
	 */
	public static GroupStyle fromGpmlName (String value) {
		return nameMappings.get(value);
	}

	/**
	 * Get the gpml name of the given GroupStyle.
	 * @deprecated use {@link #getName()} instead.
	 */
	public static String toGpmlName(GroupStyle style) {
		return style.getName();
	}

	/**
	   Stable identifier for this ConnectorType.
	 */
	public String getName ()
	{
		return name;
	}

    public boolean isDisallowLinks() {
        return disallowLinks;
    }

    static public GroupStyle[] getValues()
	{
		return values.toArray(new GroupStyle[0]);
	}

	public static String[] getNames() {
		return nameMappings.keySet().toArray(new String[nameMappings.size()]);
	}

	public String toString()
	{
		return name;
	}

	public int compareTo(GroupStyle o) {
		return toString().compareTo(o.toString());
	}

	/** Margin of group bounding-box around contained elements */
	public double getMMargin() 
	{
		return mMargin;
	}
}
