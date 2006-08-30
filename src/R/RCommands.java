package R;

import gmmlVision.GmmlVision;

import java.util.List;

import org.rosuda.JRI.Rengine;

import R.RData.Pathway;
import R.RData.RObject;

public class RCommands {

	/**
	 * Remove an object from the workspace
	 * @param symbol The name of the object to remove
	 */
	public static void rm(Rengine re, String symbol) {
		re.eval("rm(" + symbol + ")");
	}
	
	public static void rm(Rengine re, String[] symbols) {
		for(String s : symbols) rm(re, s);
	}
	
	public static void assign(Rengine re, String symbol, List list) throws ClassCastException {
		long[] refs = new long[list.size()];
		for(int i = 0; i < list.size(); i++) {
			RObject ro = (RObject)list.get(i);
			refs[i] = ro.getRef(re);
		}
		long listRef = re.rniPutVector(refs);
		re.rniAssign(symbol, listRef, 0);
	}
	
	public static void assign(Rengine re, String symbol, String[] sa) {
		long r = re.rniPutStringArray(sa);
		re.rniAssign(symbol, r, 0);
	}
}
