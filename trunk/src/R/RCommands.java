package R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import util.SwtUtils.SimpleRunnableWithProgress;
import R.RDataOut.RObject;

/**
 * This class contains static wrappers for commands to be evaluated in R
 * @author thomas
 */
public class RCommands {	
	/**
	 * Remove an object from the current R workspace
	 * @param symbol The name of the object to remove
	 */
	public static void rm(String symbol) throws RException {
		eval("rm('" + symbol + "')");
	}
	
	/**
	 * Remove a list of objects from the current R workspace
	 * @param symbols	The names of the objects (in R) to remove
	 * @throws RException
	 */
	public static void rm(List<String> symbols) throws RException {
		rm(symbols.toArray(new String[symbols.size()]));
//		eval("try(rm(list = list(" + Utils.list2String(symbols, "\"", ",") + ")))");
	}
	
	/**
	 * Remove a list of objects from the current R workspace
	 * @param symbols	The names of the objects (in R) to remove
	 * @throws RException
	 */
	public static void rm(String[] symbols) throws RException {
		assign("tmp", symbols);
		eval("rm(list = tmp)");
		eval("rm(tmp)");
	}
	
    /**
     * Wrapper for {@link Rengine#eval(String s)}, throws {@link RException}
     * Equivalent to eval(String s, false)
     * @throws InterruptedException 
    */
    public static REXP eval(String s) throws RException {
    	return eval(s, false);
    }
    
    /**
     * Wrapper for {@link Rengine#eval(String)}, evaluates the string
     * and returns an empty REXP (does not convert the evaluation result to a Java object)
     * @param re
     * @param s
     * @throws RException
     */
    public static REXP eval(String s, boolean convert) throws RException {
    	Rengine re = RController.getR();
    	checkCancelled();
    	REXP rexp = re.eval(s, convert);
    	if(rexp == null) throw new REvalException(re, s);
    	return rexp;
    }
        
    /**
     * Assigns all objects in the {@link List}<{@link RObject}> to a generic vector (list) in R
     * @param symbol	the name of the variable that stores the list in R
     * @param list	the List<RObject> to store in R (all objects in the list must extend RObject)
     * @throws RException
     */
	public static void assign(String symbol, List list) throws RException {
		Rengine re = RController.getR();
			
		//Using extra methods added to JRI - no hassle with lists longer than protection stack (10.000)
		long xpv = re.rniInitVector(list.size());
		re.rniProtect(xpv);
			
		for(int i = 0; i < list.size(); i++) {
			checkCancelled();
			
			RObject ro = (RObject)list.get(i);
			long xpe = ro.getRef();
			re.rniVectorSetElement(xpe, xpv, i);
		}
		
		re.rniAssign(symbol, xpv, 0);
		re.rniUnprotect(1);
				
//		//Using rni methods - faster
//		long[] refs = new long[list.size()];
//		for(int i = 0; i < list.size(); i++) {
//			checkCancelled();
//			
//			RObject ro = (RObject)list.get(i);
//			refs[i] = ro.getRef();
//			re.rniProtect(refs[i]);
//		};
//		long listRef = re.rniPutVector(refs);
//		if(listRef == 0) throw new RniException(re, RniException.CAUSE_XP_ZERO);
//		
//		re.rniAssign(symbol, listRef, 0);
//		
//		re.rniUnprotect(refs.length);
		
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
	
	/**
	 * Wrapper for the function {@link Rengine#assign(String, String[])}
	 * @param symbol	the name of the R variable
	 * @param sa		the String[] to store in R
	 * @throws RException
	 */
	public static void assign(String symbol, String[] sa) throws RException {
		Rengine re = RController.getR();
		checkCancelled();
		
		// Using rni methods - faster
		long r = re.rniPutStringArray(sa);
		if(r == 0) throw new RniException(re, RniException.CAUSE_XP_ZERO);
		re.rniAssign(symbol, r, 0);
		
//		// Using high level API - more stable?
//		evalE(re, symbol + "= character()");
//		for(String s : sa) {
//			evalE(re, symbol + "= append(" + symbol + ", '" + s + "')");
//		}
	}
	
	/**
	 * List all R objects of given class in the current user's R environment
	 * @param objClass	The name of the class of the objects to list
	 * @return	A String[] with the variable names of the objects of class objClass present in R
	 * @throws RException
	 */
	public static String[] ls(String objClass) throws RException {
		eval("tmpls = ls()");
		eval("ofclass = sapply(tmpls, function(x) " +
				"eval(parse(text = paste('class(', x, ') == \"" + objClass + "\"'))))");
		String[] list =  eval("tmpls[ofclass]", true).asStringArray();
		
		rm(new String[] { "tmpls", "ofclass" });
		return list;
	}
	
	/**
	 * Wrapper for the R function 'dim'; Get the dimensions of an R object
	 * @param symbol	the name of the R object
	 * @return the dimensions as int[]
	 * @throws RException
	 */
	public static int[] dim(String symbol) throws RException {
		return eval("dim(" + symbol + ")", true).asIntArray();
	}
	
	/**
	 * Wrapper for the R function 'colnames'; Get the column names of an R object
	 * @param symbol	the name of the R object
	 * @return	the column names as String[]
	 * @throws RException
	 */
	public static String[] colnames(String symbol) throws RException {
		return eval("colnames(" + symbol + ")", true).asStringArray();
	}
	
	/**
	 * Wrapper for the R function 'rownames'; Get the row names of an R object
	 * @param symbol	the name of the R object
	 * @return	the row names as String[]
	 * @throws RException
	 */
	public static String[] rownames(String symbol) throws RException {
		return eval("rownames(" + symbol + ")", true).asStringArray();
	}
	
	/**
	 * Set the dimnames attribute for the SEXP that ref points to
	 * @param ref	a reference to the SEXP object to set the dimnames attribute for
	 * @param rowNames	the row names (dimnames[[1]])
	 * @param colNames	the column names (dimnames[[2]])
	 * @throws RException
	 */
	public static void setDimNames(long ref, String[] rowNames, String[] colNames) throws RException {
		Rengine re = RController.getR();
		
		long dn_ref = re.rniInitVector(2);
		re.rniProtect(dn_ref);
		long rown_ref = re.rniPutStringArray(rowNames);
		re.rniVectorSetElement(rown_ref, dn_ref, 0);
		long coln_ref = re.rniPutStringArray(colNames);
		re.rniVectorSetElement(coln_ref, dn_ref, 1);
		re.rniSetAttr(ref, "dimnames", dn_ref);
		re.rniUnprotect(1);
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
	
	/**
	 * This class manages temporary variables that can be used to temporarily store objects in R
	 * @author thomas
	 */
	public static class RTemp {		
		static final String prefix = "tmp_"; //prefix for the temp variables
		static int MAX_VARS = 500;			 //maximum of temp variables to retain in R
				
		static HashMap<String, String> tmpVars = new HashMap<String, String>();
		static List<String> toProtect = new ArrayList<String>();
		
		/**
		 * Assign the result of the expression 'cmd' to a temp object in R
		 * @param cmd the R command of which the evaluated result will be stored 
		 * in the (protected) temp object
		 * @return the name of the temp object
		 * @throws RException
		 */
		static String assign(String cmd) throws RException {
			return assign(cmd, true);
		}
		
		/**
		 * Assign the result of the expression 'cmd' to a temp object in R
		 * @param cmd the R command of which the evaluated result will be stored as temp object
		 * @param protect protect the temp variable or not (protected means that this variable will not
		 * be removed when the {@link MAX_VARS} threshold is reached)
		 * @return the name of the temp object
		 * @throws RException
		 */
		static String assign(String cmd, boolean protect) throws RException {			
			String tmpVar = getNewVar(protect);
			eval(tmpVar + "= " + cmd);
			return tmpVar;
		}
		
		static int nextNm = 0;
		static String getUniqueSymbol() { return prefix + nextNm++; }
		
		/**
		 * Reserve a new (protected) name for a temporary R object
		 * @return the reserved name
		 * @throws RException
		 */
		static String getNewVar() throws RException {
			return getNewVar(true);
		}
		
		/**
		 * Reserve a new (protected) name for a temporary R object
		 * @param protect protect the temp variable or not (protected means that this variable will not
		 * be removed when the {@link MAX_VARS} threshold is reached)
		 * @return the reserved name
		 * @throws RException
		 */
		static String getNewVar(boolean protect) throws RException {
			check();

			String tmpVar = getUniqueSymbol();
			tmpVars.put(tmpVar, tmpVar);
			if(protect) protect(tmpVar);
			return tmpVar;
		}
		
		/**
		 * Protect this temporary variable (this means that it will NOT be removed the next time
		 * the number of temporary variables exceeds {{@link #MAX_VARS})
		 * @param symbol the name of the variable to protect
		 */
		static void protect(String symbol) {
			if(!isProtected(symbol)) 
				toProtect.add(symbol);
		}
		
		/**
		 * Unprotect this temporary variable (this means that it will be removed the next time
		 * the number of temporary variables exceeds {@link #MAX_VARS})
		 * @param symbol
		 * @throws RException
		 */
		static void unprotect(String symbol) throws RException { 
			toProtect.remove(symbol);
		}
		
		/**
		 * Unprotect the temporary variables (this means that it will be removed the next time
		 * the number of temporary variables exceeds {@link #MAX_VARS})
		 * @param symbols all variables to be unprotected
		 */
		static void unprotect(String[] symbols) throws RException {
			for(String s : symbols) unprotect(s);
		}
		
		/**
		 * Unprotect given symbol and check whether or not the number of temporary
		 * object exceeds {@link MAX_VARS} 
		 * @param symbol
		 * @throws RException
		 */
		static void dispose(String symbol) throws RException {
			unprotect(symbol);
			check();
		}
		
		/**
		 * Unprotect given symbols and check whether or not the number of temporary
		 * object exceeds {@link MAX_VARS} 
		 * @param symbols
		 * @throws RException
		 */
		static void dispose(String[] symbols) throws RException {
			unprotect(symbols);
			check();
		}
		
		/**
		 * Check if a temporary object is protected or not
		 * @param symbol	the variable name of the temporary object
		 * @return	true if the object is protected, false if not
		 */
		static boolean isProtected(String symbol) {
			return toProtect.contains(symbol);
		}
		
		/**
		 * Checks whether the number of temporary objects exceeds the threshold set
		 * by {@link MAX_VARS}
		 * @throws RException
		 */
		static void check() throws RException {
			int inUse = tmpVars.size() - toProtect.size();
			if (inUse > MAX_VARS) flush();
		}
		
		/**
		 * Remove the given temporary object
		 * @param s	the variable name of the object to remove
		 * @throws RException
		 */
		static void remove(String s) throws RException {
			rm(s);
			toProtect.remove(s);
			tmpVars.remove(s);
		}
		
		/**
		 * Remove the given {@link List} of temporary objects
		 * @param symbols	the objects to remove
		 * @throws RException
		 */
		static void remove(List<String> symbols) throws RException {
			rm(symbols);
			for(String s : symbols) {
				toProtect.remove(s);
				tmpVars.remove(s);
			}
		}
		
		/**
		 * Remove all temporary objects (excluding the protected ones)
		 * (equivalent to calling flush(false))
		 * @throws RException
		 */
		static void flush() throws RException { flush(false); }
		
		/**
		 * Remove all temporary objects
		 * @param all	whether the protected variables have to be removed as well
		 * @throws RException
		 */
		static void flush(boolean all) throws RException {
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
		}
	}
	
	/**
	 * Exception for R commands
	 * @author thomas
	 */
	public static class RException extends Exception {
		private static final long serialVersionUID = 1L;
		Rengine re;
		String msg;
		
		/**
		 * Constructor of this class
		 * @param re	the {@link Rengine} used when this exception occured
		 * @param msg	message to specify the error
		 */
		public RException(Rengine re, String msg) 	{ this.msg = msg; this.re = re;}
		public String getMessage() 		{
			if(re == null) return msg;
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
	
	/**
	 * Exception occured during methods that use the native JRI methods
	 * @author thomas
	 *
	 */
	public static class RniException extends RException {
		private static final long serialVersionUID = 1L;
		
		public static final int CAUSE_XP_ZERO = 0;
		private static final String MSG_XP_ZERO = "invalid foreign reference";
		
		int cause;
		
		public RniException(Rengine re, int cause) {
			super(re, "");
			String cm;
			switch(cause) {
			case CAUSE_XP_ZERO: cm = MSG_XP_ZERO; break;
			default: cm = "no type specified";
			}
			
			msg = cm;
			this.cause = cause;
		}
	}
	
	/**
	 * Exception the occured because an R command was interrupted
	 * @author thomas
	 */
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
