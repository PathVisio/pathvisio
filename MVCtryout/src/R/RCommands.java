package R;

import java.util.List;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import R.RData.RObject;

public class RCommands {

	/**
	 * Remove an object from the workspace
	 * @param symbol The name of the object to remove
	 */
	public static void rm(Rengine re, String symbol) throws RException {
		evalE(re, "rm(" + symbol + ")");
	}
	
	public static void rm(Rengine re, String[] symbols) throws RException {
		for(String s : symbols) rm(re, s);
	}
	
    /**
     * Wrapper for {@link Rengine#eval(String s)}, throws {@link REvalException}
    */
    public static REXP evalE(Rengine re, String s) throws REvalException {
    	REXP rexp = re.eval(s);
    	if(rexp == null) throw new REvalException(s);
    	return rexp;
    }
    
	public static void assign(Rengine re, String symbol, List list) throws 	RException,
																			ClassCastException {
		//Using rni methods
//		long[] refs = new long[list.size()];
//		for(int i = 0; i < list.size(); i++) {
//			RObject ro = (RObject)list.get(i);
//			refs[i] = ro.getRef(re);
//		};
//		long listRef = re.rniPutVector(refs);
//		re.rniAssign(symbol, listRef, 0);
		
		//Using high level API methods
		evalE(re, symbol + "= list()");

		for(Object o : list) {
			((RObject)o).toR(re, "tmpobj");
			evalE(re, symbol + "= append(" + symbol + ", list(tmpobj))");
		}
		
		rm(re, "tmpobj");
	}
	
	public static void assign(Rengine re, String symbol, String[] sa) throws RException {
		// Using rni methods
//		long r = re.rniPutStringArray(sa);
//		if(r == 0) throw new RniException("rniPutStringArray", r);
//		re.rniAssign(symbol, r, 0);
		
		// Using high level API
		evalE(re, symbol + "= character()");
		for(String s : sa) {
			evalE(re, symbol + "= append(" + symbol + ", '" + s + "')");
		}
	}
	
	public static class RException extends Exception {
		String msg;

		public RException(String msg) 	{ this.msg = msg; }
		public String getMessage() 		{ return msg; }
	}
	
	public static class REvalException extends RException {
		String cmd;
		
		public REvalException(String cmd) {
			super("R was unable to evaluate the command '" + cmd + "'");
			this.cmd = cmd;
		}
		
		public String getCmd() { return cmd; }
	}
	
	public static class RniException extends RException {
		long ref;
		String method;
		
		public RniException(String method, long ref) { 
			super("R was unable to process rni method '" + method + "'");
			this.method = method;
			this.ref = ref;
		}
		
		public long getRef() { return ref; }
	}
}
