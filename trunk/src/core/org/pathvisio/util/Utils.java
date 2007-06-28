// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pathvisio.gui.swt.Engine;

public class Utils {
	
	public static final int OS_UNSUPPORTED = -1;
	public static final int OS_WINDOWS = 0;
	public static final int OS_LINUX = 1;
	
	/**
	 * Get the OS
	 */
	public static int getOS() {
		String os = System.getProperty("os.name");
		if			(os.startsWith("Win"))
			return OS_WINDOWS;			
		else if		(os.startsWith("Lin"))
			return OS_LINUX;
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
	public static String collection2String(Collection list, String quote, String sep) {
		StringBuilder strb = new StringBuilder();
		for(Object o : list) {
			strb.append(quote + o.toString() + quote + sep);
		}
		int last = strb.lastIndexOf(String.valueOf(sep));
		if(last >= 0) strb.delete(last, strb.length());
		
		return strb.toString();
	}
	
	/**
	 * Converts an array to a string
	 * @param array The array to convert to a string
	 * @param quote A quote character to use
	 * @param sep The separator to use
	 * @return a String representing the array with given seperator and quote (no parentheses)
	 */
	public static String array2String(Object[] array, String quote, String sep) {
		return collection2String(Arrays.asList(array), quote, sep);
	}
	
    /**
     * Moves an element in a {@link List}
     * @param 	l the list the object is in
     * @param 	o the object that has to be moved
     * @param 	newIndex the index to move the object to
     */
    public static <T> void moveElement(List<T> l, T o, int newIndex)
    {
    	l.remove(o);
    	l.add(newIndex, o);
    }
    
    /**
     * Switches the index of the element with the one before the element
     * @see #changeOrder(List, Object, int)
     */
	public static final int ORDER_UP = 1;
	/**
	 * Switches the index of the element with the one after the element
	 * @see #changeOrder(List, Object, int)
	 */
	public static final int ORDER_DOWN = -1;
	/**
	 * Puts the element at the start of the list
	 */
	public static final int ORDER_FIRST = 2;
	/**
	 * Puts the element at the end of the list
	 */
	public static final int ORDER_LAST = -2;
	
	/**
	 * Change the order of the element in the given {@link List}
	 * @param <T>
	 * @param l The list containing the element to change the order for
	 * @param o The element of which the order has to be changed
	 * @param order The order constant (one of {@link #ORDER_UP}, {@link #ORDER_DOWN},
	 * {@link #ORDER_FIRST}, {@link #ORDER_LAST})
	 */
	public static <T> void changeOrder(List<T> l, T o, int order) {
		int index = l.indexOf(o);
		switch(order) {
		case ORDER_UP:
			if(index == 0) break;
			l.remove(index);
			l.add(index - 1, o);
			break;
		case ORDER_DOWN:
			if(index == l.size() - 1) break;
			l.remove(index);
			l.add(index + 1, o);
			break;
		case ORDER_FIRST:
			l.remove(index);
			l.add(0, o);
			break;
		case ORDER_LAST:
			l.remove(index);
			l.add(o);
			break;
		}
	}
	
	/**
	 * Checks the version of the Gene database or Expression dataset to be opened
	 */
	public static void checkDbVersion(Connection con, int compat_version) throws Exception
	{
		boolean check = false;
		try {
			ResultSet r = con.createStatement().executeQuery("SELECT version FROM info");
			if(r.next()) check = r.getInt("version") == compat_version;
		} catch (Exception e) {
			Engine.log.error("Database compatibility version number could not be read", e);
		}
		if(check) return;
		throw new Exception("Incompatible version of database schema");
	}
	
	/**
	 * Check if the given class implements the given interface
	 * @param c the class
	 * @param ifName the name of the interface
	 * @return true if the class implements the interface, false if not
	 */
	public static boolean implementsInterface(Class c, String ifName) {
		Class[] interfaces = c.getInterfaces();
		for(Class i : interfaces) { 
			if(ifName.equals(i.getCanonicalName())) return true; 
		}
		return false;
	}
	
	/**
	 * Check whether the given class is a subclass of the given super-class
	 * @param c the class
	 * @param superClass the super-class
	 * @return true if the class is a sub-class of superClass, false if not
	 */
	public static boolean isSubClass(Class c, Class superClass) {
		Class sc = c;
		while((sc = sc.getSuperclass()) != null) {
			Engine.log.trace("\t\t>" + c + " with superclass: " + superClass);
			if(sc.equals(superClass)) return true;
		}
		return false;
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
}
