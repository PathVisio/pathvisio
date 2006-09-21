package R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import util.Utils;
import util.SwtUtils.SimpleRunnableWithProgress;
import R.RData.RObject;

public class RCommands {	
	/**
	 * Remove an object from the workspace
	 * @param symbol The name of the object to remove
	 */
	public static void rm(String symbol) throws RException {
		eval("try(rm(" + symbol + "))");
	}
	
	public static void rm(List<String> symbols) throws RException {
		eval("try(rm(list = list(" + Utils.list2String(symbols, "", ",") + ")))");
	}
	
	public static void rm(String[] symbols) throws RException {
		rm(Arrays.asList(symbols));
	}
	
    /**
     * Wrapper for {@link Rengine#eval(String s)}, throws {@link RException}
     * @throws InterruptedException 
    */
    public static REXP eval(String s) throws RException {
    	return eval(s, false);
    }
    
    /**
     * Wrapper for {@link Rengine#eval(String)}, evaluates the string
     * and returns an empty REXP, throws {@link RException}
     * @param re
     * @param s
     * @throws RException
     */
    public static REXP eval(String s, boolean convert) throws RException {
    	Rengine re = RController.getR();
    	checkCancelled();
    	REXP rexp = re.eval(s, false);
    	if(rexp == null) throw new REvalException(re, s);
    	return rexp;
    }
    
	public static void assign(String symbol, List list) throws RException {
		Rengine re = RController.getR();
		
//		//Using rni methods - faster
		long[] refs = new long[list.size()];
		for(int i = 0; i < list.size(); i++) {
			checkCancelled();
			
			RObject ro = (RObject)list.get(i);
			refs[i] = ro.getRef();
			re.rniProtect(refs[i]);
		};
		long listRef = re.rniPutVector(refs);
		if(listRef == 0) throw new RniException(re, "rniPutVector", "zero reference", listRef);
		re.rniAssign(symbol, listRef, 0);
		
		re.rniUnprotect(refs.length);
		
		//Using high level API methods - more stable?	
//		int i = 0;
//		String[] tmpVars = new String[list.size()];
//		
//		for(Object o : list) {
//			tmpVars[i++] = ((RObject)o).toRTemp(re, true);
//		}
//		
//		String varList = Utils.array2String(tmpVars, "", ",");
//		evalEN(symbol + "= list(" + varList + ")");
//		
//		RTemp.unprotect(tmpVars);
	}
	
	public static void assign(String symbol, String[] sa) throws RException {
		Rengine re = RController.getR();
		checkCancelled();
		
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
	
	/**
	 * Check wether the user pressed cancel (for long running operations
	 * using the {@link SimpleRunnableWithProgress}
	 * @return
	 */
	static void checkCancelled() throws RInterruptedException {
		if(SimpleRunnableWithProgress.isCancelled())
			throw new RInterruptedException();
	}
	
	public static class RTemp {		
		static final String prefix = "tmp_";	
		static int MAX_VARS = 500;
				
		static HashMap<String, String> tmpVars = new HashMap<String, String>();
		static List<String> toProtect = new ArrayList<String>();
		
		static String assign(String cmd) throws RException {
			return assign(cmd, true);
		}
		static String assign(String cmd, boolean protect) throws RException {			
			String tmpVar = getNewVar(protect);
			eval(tmpVar + "= " + cmd);
			return tmpVar;
		}
		
		static int nextNm = 0;
		static String getUniqueSymbol() { return prefix + nextNm++; }
		
		static String getNewVar() throws RException {
			return getNewVar(true);
		}
		
		static String getNewVar(boolean protect) throws RException {
			check();

			String tmpVar = getUniqueSymbol();
			tmpVars.put(tmpVar, tmpVar);
			if(protect) protect(tmpVar);
			return tmpVar;
		}
		
		static void protect(String symbol) {
			if(!isProtected(symbol)) 
				toProtect.add(symbol);
		}
		
		static void unprotect(String symbol) throws RException { 
			toProtect.remove(symbol);
		}
				
		static void unprotect(String[] symbols) throws RException {
			for(String s : symbols) unprotect(s);
		}
		
		static void dispose(String symbol) throws RException {
			unprotect(symbol);
			check();
		}
		
		static void dispose(String[] symbols) throws RException {
			unprotect(symbols);
			check();
		}
		
		static boolean isProtected(String symbol) {
			return toProtect.contains(symbol);
		}
		
		static void check() throws RException {
			int inUse = tmpVars.size() - toProtect.size();
			if (inUse > MAX_VARS) flush();
		}
		
		static void remove(String s) throws RException {
			rm(s);
			toProtect.remove(s);
			tmpVars.remove(s);
		}
		
		static void remove(List<String> symbols) throws RException {
			for(String s : symbols) remove(s);
		}
		
		static void flush() throws RException { flush(false); }
		
		static void flush(boolean all) throws RException {
			System.err.println("RTemp: FLUSHING " + all);
			System.err.println("\tBefore:");
			System.err.println("\t> tmpVars:\t" + tmpVars.size());
			System.err.println("\t> toProtect:\t" + toProtect.size());
			
			List<String> toRemove = new ArrayList<String>();
			if(all) {
				toRemove.addAll(tmpVars.keySet());
				nextNm = 0;
			} else {
				for(String s : tmpVars.keySet()) {
					if(!isProtected(s)) toRemove.add(s);
				}
			}
			remove(toRemove);

			System.err.println("\tAfter:");
			System.err.println("\t> tmpVars:\t" + tmpVars.size());
			System.err.println("\t> toProtect:\t" + toProtect.size());
		}
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
	
	public static class RInterruptedException extends RException {
		private static final long serialVersionUID = 1L;
		static final String MSG_INTERRUPT = "R command was interrupted";
		
		public RInterruptedException(Rengine re) {
			super(re, MSG_INTERRUPT);
		}
		
		public RInterruptedException() {
			this(null);
		}
	}
}
