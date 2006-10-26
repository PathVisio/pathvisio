package util;

import gmmlVision.GmmlVision;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Utils {
	
	public static final int OS_UNSUPPORTED = -1;
	public static final int OS_WINDOWS = 0;
	public static final int OS_LINUX = 1;
	
	/**
	 * Get the OS
	 * @returns 
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
	 * Checks the version of the Gene database or Expression dataset to be opened
	 */
	public static void checkDbVersion(Connection con, int compat_version) throws Exception
	{
		boolean check = false;
		try {
			ResultSet r = con.createStatement().executeQuery("SELECT version FROM info");
			if(r.next()) check = r.getInt("version") == compat_version;
		} catch (Exception e) {
			GmmlVision.log.error("Database compatibility version number could not be read", e);
		}
		if(check) return;
		throw new Exception("Incompatible version of database schema");
	}
}
