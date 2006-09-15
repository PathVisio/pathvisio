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
		evalE(re, "try(rm(" + symbol + "))");
	}
	
	public static void rm(Rengine re, String[] symbols) throws RException {
		for(String s : symbols) rm(re, s);
	}
	
    /**
     * Wrapper for {@link Rengine#eval(String s)}, throws {@link REvalException}
    */
    public static REXP evalE(Rengine re, String s) throws REvalException {
    	REXP rexp = re.eval(s);
    	if(rexp == null) throw new REvalException(re, s);
    	return rexp;
    }
    
	public static void assign(Rengine re, String symbol, List list) throws 	RException,
																			ClassCastException {
		//Using rni methods - faster
		long[] refs = new long[list.size()];
		for(int i = 0; i < list.size(); i++) {
			RObject ro = (RObject)list.get(i);
			refs[i] = ro.getRef(re);
		};
		long listRef = re.rniPutVector(refs);
		if(listRef == 0) throw new RniException(re, "rniPutVector", "zero reference", listRef);
		re.rniAssign(symbol, listRef, 0);
		
		//Using high level API methods - more stable?
//		evalE(re, symbol + "= list()");
//
//		for(Object o : list) {
//			((RObject)o).toR(re, "tmpobj");
//			evalE(re, symbol + "= append(" + symbol + ", list(tmpobj))");
//		}
//		rm(re, "tmpobj");
	}
	
	public static void assign(Rengine re, String symbol, String[] sa) throws RException {
		// Using rni methods - faster
		long r = re.rniPutStringArray(sa);
		if(r == 0) throw new RniException(re, "rniPutStringArray", "zero reference", r);
		re.rniAssign(symbol, r, 0);
		
//		// Using high level API - more stable?
//		evalE(re, symbol + "= character()");
//		for(String s : sa) {
//			evalE(re, symbol + "= append(" + symbol + ", '" + s + "')");
//		}
	}
	
	public static class RException extends Exception {
		private static final long serialVersionUID = 1L;
		Rengine re;
		String msg;

		public RException(Rengine re, String msg) 	{ this.msg = msg; this.re = re;}
		public String getMessage() 		{
			REXP err = re.eval("geterrmessage()");
			return err == null ? msg + "\n R> no error message" : msg + "\nR> " + err.getContent();
		}
	}
	
	public static class REvalException extends RException {
		private static final long serialVersionUID = 1L;
		String cmd;
		
		public REvalException(Rengine re, String cmd) {
			super(re, "R was unable to evaluate the command '" + cmd + "'");
			this.cmd = cmd;
		}
		
		public String getCmd() { return cmd; }
	}
	
	public static class RniException extends RException {
		private static final long serialVersionUID = 1L;
		long ref;
		String method;
		String cause;
		
		public RniException(Rengine re, String method, String cause, long ref) { 
			super(re, "R was unable to process rni method '" + method + "; " + cause);
			this.method = method;
			this.cause = cause;
			this.ref = ref;
		}
		
		public long getRef() { return ref; }
	}
}
