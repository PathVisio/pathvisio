package util;

import java.util.List;

public class Utils {
	
	/**
	 * Converts a list to a string
	 * @param list The list to convert to a string
	 * @param quote A quote character to use
	 * @param sep The separator to use
	 * @return a String representing the list with given seperator and quote (no parentheses)
	 */
	public static String list2String(List list, char quote, char sep) {
		StringBuilder strb = new StringBuilder();
		for(Object o : list) strb.append(quote + o.toString() + quote + sep);
		strb.delete(strb.lastIndexOf(String.valueOf(sep)), strb.length());
		
		return strb.toString();
	}
}
