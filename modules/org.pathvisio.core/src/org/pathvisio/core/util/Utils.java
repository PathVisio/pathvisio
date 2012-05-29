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
package org.pathvisio.core.util;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Various utility functions
 */
public class Utils {

	public static final int OS_UNSUPPORTED = -1;
	public static final int OS_WINDOWS = 0;
	public static final int OS_LINUX = 1;
	public static final int OS_MAC = 2;

	/**
	 * Get the OS
	 */
	public static int getOS() {
		String os = System.getProperty("os.name");
		if			(os.startsWith("Win"))
			return OS_WINDOWS;
		else if		(os.startsWith("Lin"))
			return OS_LINUX;
		else if		(os.startsWith("Mac"))
			return OS_MAC;
		else
			return OS_UNSUPPORTED;
	}

	/**
	 * Converts a list to a string
	 * @param list The list to convert to a string
	 * @param quote A quote character to use
	 * @param sep The separator to use
	 * @return a String representing the list with given seperator and quote (no parentheses)
	 */
	public static String collection2String(Collection<?> list, String quote, String sep) {
		StringBuilder strb = new StringBuilder();
		for(Object o : list) {
			strb.append(quote + o.toString() + quote + sep);
		}
		int last = strb.lastIndexOf(String.valueOf(sep));
		if(last >= 0) strb.delete(last, strb.length());

		return strb.toString();
	}

	public static InputStream stringToInputStream(String str){
		if(str==null) return null;
		InputStream in = null;
		try{
		in = new java.io.ByteArrayInputStream(str.getBytes("UTF-8"));
		}catch(Exception ex){
		}
		return in;
	}

	/**
	 * Encodes a font to a string that can be converted back
	 * into a font object using Font.decode(String)
	 */
	public static String encodeFont(Font f) {
		String style = "PLAIN";
		if(f.isBold() && f.isItalic()) style = "BOLDITALIC";
		else if (f.isBold()) style = "BOLD";
		else if (f.isItalic()) style = "ITALIC";
		String fs = f.getName() + "-" + style + "-" + f.getSize();
		return fs;
	}

	/**
	 * Helper function to print the contents of maps or collections,
	 * or maps of maps, collections of maps, collections of collections etc.
	 * Useful for debugging.
	 * Similar in idea to the perl Data::Dumper module
	 * @param indent is for recursive use, e.g. to prefix each line with "\t"
	 */
	public static void printx (PrintStream out, String indent, Object o)
	{
		if (o instanceof Map)
		{
			Map<?, ?> map = (Map<?, ?>)o;
			for (Object key : map.keySet())
			{
				printx (out, indent, key);
				out.println (indent + "=>");
				Object value = map.get (key);
				printx (out, indent + "\t", value);
			}
		}
		else if (o instanceof Collection)
		{
			Collection<?> col = (Collection<?>)o;
			out.println (indent + "(");
			for (Object item : col)
			{
				printx (out, indent + "\t", item);
			}
			out.println (indent + ")");
		}
		else
		{
			out.println (indent + o.toString());
		}
	}

	/**
	 * Useful if you want to use one item from a set, and you don't care which one.
	 * @param set a set that you want one element out of
	 * @return null if the set is empty or null, or an element from the set otherwise.
	 */
	static public <T> T oneOf (Set<T> set)
	{
		if (set == null || set.size() == 0)
		{
			return null;
		}
		else
		{
			return set.iterator().next();
		}
	}

	/**
	 * Create a new Set of the given value(s)
	 */
	static public <T> Set<T> setOf (T val)
	{
		Set<T> result = new HashSet<T>();
		result.add (val);
		return result;
	}

	/**
	 * For safely adding a value to a multimap (i.e. a map of sets)   
	 */
	static public <U, V> void multimapPut (Map <U, Set<V> > map, U key, V val)
	{
		Set<V> x;
		if (map.containsKey(key))
			x = map.get(key);
		else
		{
			x = new HashSet<V>();
			map.put (key, x);
		}
		x.add(val);
	}

	/**
	 * returns a set of all unique values of a multimap (i.e. a map of sets).
	 */
	public static <U, V> Set<V> multimapValues (Map<U, Set<V>> map)
	{
		Set <V> result = new HashSet<V>();
		for (Set<V> set : map.values()) result.addAll(set);
		return result;
	}

    /**
     * Compares two Strings, returning <code>true</code> if they are equal.
     *
     * @see java.lang.String#equals(Object)
     * @param str1  the first String, may be null
     * @param str2  the second String, may be null
     * @return <code>true</code> if the Strings are equal, case sensitive, or
     *  both <code>null</code>
     */
    public static boolean stringEquals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    /**
	 * Get the direction of the line on the x axis
	 * @param start The start point of the line
	 * @param end The end point of the line
	 * @return 1 if the direction is positive (from left to right),
	 * -1 if the direction is negative (from right to left)
	 */
	public static int getDirectionX(Point2D start, Point2D end) {
		return (int)Math.signum(end.getX() - start.getX());
	}

	/**
	 * Get the direction of the line on the y axis
	 * @param start The start point of the line
	 * @param end The end point of the line
	 * @return 1 if the direction is positive (from top to bottom),
	 * -1 if the direction is negative (from bottom to top)
	 */
	public static int getDirectionY(Point2D start, Point2D end) {
		return (int)Math.signum(end.getY() - start.getY());
	}


	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

}
