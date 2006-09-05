package org.rosuda.JRI;

import java.lang.*;

/** Rengine class is the interface between an instance of R and the Java VM. Due to the fact that R has no threading support, you can run only one instance of R withing a multi-threaded application. There are two ways to use R from Java: individual call and full event loop. See the Rengine {@link #Rengine constructor} for details. <p> <u>Important note:</u> All methods starting with <code>rni</code> (R Native Interface) are low-level native methods that should be avoided if a high-level methods exists. They do NOT attempt any synchronization, so it is the duty of the calling program to ensure that the invocation is safe (see {@link getRsync()} for details). At some point in the future when the high-level API is complete they should become private. However, currently this high-level layer is not complete, so they are available for now.<p>All <code>rni</code> methods use <code>long</code> type to reference <code>SEXP</code>s on R side. Those reference should never be modified or used in arithmetics - the only reason for not using an extra interface class to wrap those references is that <code>rni</code> methods are all <i>native</i> methods and therefore it would be too expensive to handle the unwrapping on the C side.<p><code>jri</code> methods are called internally by R and invoke the corresponding method from the even loop handler. Those methods should usualy not be called directly. */
public class Rengine extends Thread {
    static {
        try {
            System.loadLibrary("jri");
        } catch (UnsatisfiedLinkError e) {
			System.err.println("Cannot find JRI native library!\nPlease make sure that the JRI native library is in a directory listed in java.library.path.\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

	/**	API version of the Rengine itself; see also rniGetVersion() for binary version. It's a good idea for the calling program to check the versions of both and abort if they don't match. This should be done using {@link #versionCheck}
		@return version number as <code>long</code> in the form <code>0xMMmm</code> */
    public static long getVersion() {
        return 0x0105;
    }

    /** check API version of this class and the native binary. This is usually a good idea to ensure consistency.
	@return <code>true</code> if the API version of the Java code and the native library matches, <code>false</code> otherwise */
	public static boolean versionCheck() {
		return (getVersion()==rniGetVersion());
	}
	
    /** debug flag. Set to value &gt;0 to enable debugging messages. The verbosity increases with increasing number */
    public static int DEBUG=0;
	
	/** this value specifies the time (in ms) to spend sleeping between checks for R shutdown requests if R event loop is not used. The default is 200ms. Higher values lower the CPU usage but may make R less responsive to shutdown attempts (in theory it should not matter because {@link #stop()} uses interrupt to awake from the idle sleep immediately, but some implementation may not honor that).
	@since JRI 0.3
	*/
	public int idleDelay = 200;
	
    /** main engine. Since there can be only one instance of R, this is also the only instance. */
    static Rengine mainEngine=null;
    
    /** return the current main R engine instance. Since there can be only one true R instance at a time, this is also the only instance. This may not be true for future versions, though.
	@return current instance of the R engine or <code>null</code> if no R engine was started yet. */
    public static Rengine getMainEngine() { return mainEngine; }

    boolean died, alive, runLoop, loopRunning;
    /** arguments used to initialize R, set by the constructor */
	String[] args;
	/** synchronization mutex */
	Mutex Rsync;
	/** callback handler */
    RMainLoopCallbacks callback;
	
    /** create and start a new instance of R. 
	@param args arguments to be passed to R. Please note that R requires the presence of certain arguments (e.g. <code>--save</code> or <code>--no-save</code> or equivalents), so passing an empty list usually doesn't work.
	@param runMainLoop if set to <code>true</code> the the event loop will be started as soon as possible, otherwise no event loop is started. Running loop requires <code>initialCallbacks</code> to be set correspondingly as well.
	@param initialCallbacks an instance implementing the {@link org.rosuda.JRI.RMainLoopCallbacks RMainLoopCallbacks} interface that provides methods to be called by R
    */
    public Rengine(String[] args, boolean runMainLoop, RMainLoopCallbacks initialCallbacks) {
        super();
        Rsync=new Mutex();
        died=false;
        alive=false;
        runLoop=runMainLoop;
        loopRunning=false;
        this.args=args;
        callback=initialCallbacks;
        mainEngine=this;
        start();
        while (!alive && !died) yield();
    }

    /** RNI: setup R with supplied parameters (should <b>not</b> be used directly!).
	@param args arguments
	@return result code
     */
    native int rniSetupR(String[] args);
    
    synchronized int setupR() {
        return setupR(null);
    }
    
    synchronized int setupR(String[] args) {
        int r=rniSetupR(args);
        if (r==0) {
            alive=true; died=false;
        } else {
            alive=false; died=true;
        }
        return r;
    }
    
    /** RNI: parses a string into R expressions (do NOT use directly unless you know exactly what you're doing, where possible use {@link #eval} instead). Note that no synchronization is performed!
	@param s string to parse
	@param parts number of expressions contained in the string
	@return reference to the resulting list of expressions */
    public synchronized native long rniParse(String s, int parts);
    /** RNI: evaluate R expression (do NOT use directly unless you know exactly what you're doing, where possible use {@link #eval} instead). Note that no synchronization is performed!
	@param exp reference to the expression to evaluate
	@param rho environment to use for evaluation (or 0 for global environemnt)
	@return result of the evaluation */
    public synchronized native long rniEval(long exp, long rho);

	/** RNI: protect an R object (c.f. PROTECT macro in C)
		@since API 1.5, JRI 0.3
		@param exp reference to protect */
	public synchronized native void rniProtect(long exp);
	/** RNI: unprotect last <code>count></code> references (c.f. UNPROTECT in C)
		@since API 1.5, JRI 0.3
		@param count number of references to unprotect */
	public synchronized native void rniUnprotect(int count);

    /** RNI: get the contents of the first entry of a character vector
	@param exp reference to STRSXP
	@return contents or <code>null</code> if the reference is not STRSXP */
    public synchronized native String rniGetString(long exp);
    /** RNI: get the contents of a character vector
	@param exp reference to STRSXP
	@return contents or <code>null</code> if the reference is not STRSXP */
    public synchronized native String[] rniGetStringArray(long exp);
    /** RNI: get the contents of an integer vector
	@param exp reference to INTSXP
	@return contents or <code>null</code> if the reference is not INTSXP */
    public synchronized native int[] rniGetIntArray(long exp);
    /** RNI: get the contents of a numeric vector
	@param exp reference to REALSXP
	@return contents or <code>null</code> if the reference is not REALSXP */
    public synchronized native double[] rniGetDoubleArray(long exp);
    /** RNI: get the contents of a generic vector (aka list)
	@param exp reference to VECSXP
	@return contents as an array of references or <code>null</code> if the reference is not VECSXP */
    public synchronized native long[] rniGetVector(long exp);

    /** RNI: create a character vector of the length 1
	@param s initial contents of the first entry
	@return reference to the resulting STRSXP */
    public synchronized native long rniPutString(String s);
    /** RNI: create a character vector
	@param a initial contents of the vector
	@return reference to the resulting STRSXP */
    public synchronized native long rniPutStringArray(String[] a);
    /** RNI: create an integer vector
	@param a initial contents of the vector
	@return reference to the resulting INTSXP */
    public synchronized native long rniPutIntArray(int [] a);
    /** RNI: create a numeric vector
	@param a initial contents of the vector
	@return reference to the resulting REALSXP */
    public synchronized native long rniPutDoubleArray(double[] a);
    /** RNI: create a generic vector (aka a list)
	@param exps initial contents of the vector consisiting of an array of references
	@return reference to the resulting VECSXP */
    public synchronized native long rniPutVector(long[] exps);
    
    /** RNI: get an attribute
	@param exp reference to the object whose attribute is requested
	@param name name of the attribute
	@return reference to the attribute or 0 if there is none */
    public synchronized native long rniGetAttr(long exp, String name);
    /** RNI: set an attribute
	@param exp reference to the object whose attribute is to be modified
	@param name attribute name
	@param attr reference to the object to be used as teh contents of the attribute */
    public synchronized native void rniSetAttr(long exp, String name, long attr);

	/** RNI: determines whether an R object instance inherits from a specific class (S3 for now)
		@since API 1.5, JRI 0.3
		@param exp reference to an object
		@param cName name of the class to check
		@return <code>true</code> if <code>cName</code> inherits from class <code>cName</code> (see <code>inherits</code> in R) */ 
	public synchronized native boolean rniInherits(long exp, String cName);

    /** RNI: create a dotted-pair list (LISTSXP)
	@param head CAR
	@param tail CDR (must be a reference to LISTSXP or 0)
	@return reference to the newly created LISTSXP */
    public synchronized native long rniCons(long head, long tail);
    /** RNI: get CAR of a dotted-pair list (LISTSXP)
	@param exp reference to the list
	@return reference to CAR of the list (head) */
    public synchronized native long rniCAR(long exp);
    /** RNI: get CDR of a dotted-pair list (LISTSXP)
	@param exp reference to the list
	@return reference to CDR of the list (tail) */
    public synchronized native long rniCDR(long exp);
    /** RNI: get TAG of a dotted-pair list (LISTSXP)
		@param exp reference to the list
		@return reference to TAG of the list (tail) */
    public synchronized native long rniTAG(long exp);
    /** RNI: create a dotted-pair list (LISTSXP)
		@since API 1.5, JRI 0.3
		@param cont contents as an array of references
		@return reference to the newly created LISTSXP */
    public synchronized native long rniPutList(long[] cont);
    /** RNI: retrieve CAR part of a dotted-part list recursively as an array of references
	@param exp reference to a dotted-pair list (LISTSXP)
	@return contents of the list as an array of references */
    public synchronized native long[] rniGetList(long exp);
	/** RNI: retrieve name of a symbol (c.f. PRINTNAME)
		@since API 1.5, JRI 0.3
		@param sym reference to a symbol
		@return name of the symbol or <code>null</code> on error or if exp is no symbol */
	public synchronized native String rniGetSymbolName(long sym);
	/** RNI: install a symbol name
		@since API 1.5, JRI 0.3
		@param sym symbol name
		@return reference to SYMSXP referencing the symbol */
	public synchronized native long rniInstallSymbol(String sym);

	//--- was API 1.4 but it only caused portability problems, so we got rid of it
    //public static native void rniSetEnv(String key, String val);
    //public static native String rniGetEnv(String key);
	//--- end API 1.4

	/** RNI: convert Java object to EXTPTRSEXP
		@param o arbitrary Java object
		@return new EXTPTRSEXP pointing to the Java object
		@since API 1.5, JRI 0.3
		*/
	synchronized native long rniJavaToXref(Object o);
	/** RNI: convert EXTPTRSEXP to Java object - make sure the pointer is really what you expect, otherwise you'll crash the JVM!
		@param exp reference to EXTPTRSEXP pointing to a Java object
		@return resulting Java object
		@since API 1.5, JRI 0.3
		*/
	synchronized native Object rniXrefToJava(long exp);
	
    /** RNI: return the API version of the native library
		@return API version of the native library */
    public static native long rniGetVersion();
    
    /** RNI: interrupt the R process (if possible)
	@param flag currently ignored, must be set to 0
	@return result code (currently unspecified) */
    public native int rniStop(int flag);
    
    /** RNI: assign a value to an environment
	@param name name
	@param exp value
	@param rho environment (use 0 for the global environment) */
    public synchronized native void rniAssign(String name, long exp, long rho);
    
    /** RNI: get the SEXP type
	@param exp reference to a SEXP
	@return type of the expression (see xxxSEXP constants) */
    public synchronized native int rniExpType(long exp);
    /** RNI: run the main loop.<br> <i>Note:</i> this is an internal method and it doesn't return until the loop exits. Don't use directly! */
    public native void rniRunMainLoop();
        
    /**RNI: custom main loop (test) **/
    public native void rniCustomLoop();
    
    /** RNI: run other event handlers in R */
    public synchronized native void rniIdle();

    /** Add a handler for R callbacks. The current implementation supports only one handler at a time, so call to this function implicitly removes any previous handlers */
    public void addMainLoopCallbacks(RMainLoopCallbacks c)
    {
        // we don't really "add", we just replace ... (so far)
        callback = c;
    }
		
    /** if Rengine was initialized with <code>runMainLoop=false</code> then this method can be used to start the main loop at a later point. It has no effect if the loop is running already. This method returns immediately but the loop will be started once the engine is ready. Please note that there is currently no way of stopping the R thread if the R event loop is running other than using <code>quit</code> command in R which closes the entire application. */
    public void startMainLoop() {
		runLoop=true;
    }
    
    //============ R callback methods =========

    /** JRI: R_WriteConsole call-back from R
	@param text text to disply */
    public void jriWriteConsole(String text)
    {
        if (callback!=null) callback.rWriteConsole(this, text);
    }

    /** JRI: R_Busy call-back from R
	@param which state */
    public void jriBusy(int which)
    {
        if (callback!=null) callback.rBusy(this, which);
    }

    /** JRI: R_ReadConsole call-back from R.
	@param prompt prompt to display before waiting for the input.<br><i>Note:</i> implementations should block for input. Returning immediately is usually a bad idea, because the loop will be cycling.
	@param addToHistory flag specifying whether the entered contents should be added to history
	@return content entered by the user. Returning <code>null</code> corresponds to an EOF and usually causes R to exit (as in <code>q()</doce>). */
    public String jriReadConsole(String prompt, int addToHistory)
    {
		if (DEBUG>1)
			System.out.println("Rengine.jreReadConsole BEGIN "+Thread.currentThread());
        Rsync.unlock();
        String s=(callback==null)?null:callback.rReadConsole(this, prompt, addToHistory);
        if (!Rsync.safeLock()) {
            String es="\n>>JRI Warning: jriReadConsole detected a possible deadlock ["+Rsync+"]["+Thread.currentThread()+"]. Proceeding without lock, but this is inherently unsafe.\n";
            jriWriteConsole(es);
            System.err.print(es);
        }
		if (DEBUG>1)
			System.out.println("Rengine.jreReadConsole END "+Thread.currentThread());
        return s;
    }

    /** JRI: R_ShowMessage call-back from R
	@param message message */
    public void jriShowMessage(String message)
    {
        if (callback!=null) callback.rShowMessage(this, message);
    }
    
    /** JRI: R_loadhistory call-back from R
	@param filename name of the history file */
    public void jriLoadHistory(String filename)
    {
        if (callback!=null) callback.rLoadHistory(this, filename);
    }

    /** JRI: R_savehistory call-back from R
	@param filename name of the history file */
    public void jriSaveHistory(String filename)
    {
        if (callback!=null) callback.rSaveHistory(this, filename);
    }
	
    /** JRI: R_ChooseFile call-back from R
	@param newFile flag specifying whether an existing or new file is requested
	@return name of the selected file or <code>null</code> if cancelled */
    public String jriChooseFile(int newFile)
    {
        if (callback!=null) return callback.rChooseFile(this, newFile);
		return null;
    }
	
    /** JRI: R_FlushConsole call-back from R */	
    public void jriFlushConsole()
    {
        if (callback!=null) callback.rFlushConsole(this);
    }
	
    
    //============ "official" API =============


    /** Parses and evaluates an R expression and returns the result. Has the same effect as calling <code>eval(s, true)</code>.
	@param s expression (as string) to parse and evaluate
	@return resulting expression or <code>null</code> if something wnet wrong */
    public synchronized REXP eval(String s) {
		return eval(s, true);
	}
	
    /** Parses and evaluates an R expression and returns the result.
		@since JRI 0.3
		@param s expression (as string) to parse and evaluate
		@param convert if set to <code>true</code> the resulting REXP will contain native representation of the contents, otherwise an empty REXP will be returned. Depending on the back-end an empty REXP may or may not be used to convert the result at a later point.
		@return resulting expression or <code>null</code> if something wnet wrong */
    public synchronized REXP eval(String s, boolean convert) {
		if (DEBUG>0)
			System.out.println("Rengine.eval("+s+"): BEGIN "+Thread.currentThread());
        boolean obtainedLock=Rsync.safeLock();
        try {
            /* --- so far, we ignore this, because it can happen when a callback needs an eval which is ok ...
            if (!obtainedLock) {
                String es="\n>>JRI Warning: eval(\""+s+"\") detected a possible deadlock ["+Rsync+"]["+Thread.currentThread()+"]. Proceeding without lock, but this is inherently unsafe.\n";
                jriWriteConsole(es);
                System.err.print(es);
            }
             */
            long pr=rniParse(s, 1);
            if (pr>0) {
                long er=rniEval(pr, 0);
                if (er>0) {
                    REXP x=new REXP(this, er, convert);
                    if (DEBUG>0) System.out.println("Rengine.eval("+s+"): END (OK)"+Thread.currentThread());
                    return x;
                }
            }
        } finally {
            if (obtainedLock) Rsync.unlock();
        }
        if (DEBUG>0) System.out.println("Rengine.eval("+s+"): END (ERR)"+Thread.currentThread());
        return null;
    }
        
    /** This method is very much like {@link #eval(String)}, except that it is non-blocking and returns <code>null</code> if the engine is busy.
        @param s string to evaluate
        @return result of the evaluation or <code>null</code> if the engine is busy
        */
    public synchronized REXP idleEval(String s) {
		return idleEval(s, true);
	}

    /** This method is very much like {@link #eval(String,boolean)}, except that it is non-blocking and returns <code>null</code> if the engine is busy.
		@since JRI 0.3
        @param s string to evaluate
		@param convert flag denoting whether an empty or fully-converted REXP should be returned (see {@link #eval(String,boolean)} for details)
        @return result of the evaluation or <code>null</code> if the engine is busy
        */
    public synchronized REXP idleEval(String s, boolean convert) {
        int lockStatus=Rsync.tryLock();
        if (lockStatus==1) return null; // 1=locked by someone else
        boolean obtainedLock=(lockStatus==0);
        try {
            long pr=rniParse(s, 1);
            if (pr>0) {
                long er=rniEval(pr, 0);
                if (er>0) {
                    REXP x=new REXP(this, er, convert);
                    return x;
                }
            }
        } finally {
            if (obtainedLock) Rsync.unlock();
        }
        return null;
    }
    
	/** returns the synchronization mutex for this engine. If an external code needs to use RNI calls, it should do so only in properly protected environment secured by this mutex. Usually the procedure should be as follows:<pre>
	boolean obtainedLock = e.getRsync().safeLock();
	try {
		// use RNI here ...
	} finally {
		if (obtainedLock) e.getRsync().unlock();
	}
	</pre>
		@return synchronization mutex
		@since JRI 0.3
		*/
	public Mutex getRsync() {
		return Rsync;
	}
	
    /** check the state of R
		@return <code>true</code> if R is alive and <code>false</code> if R died or exitted */
    public synchronized boolean waitForR() {
        return alive;
    }

    /** attempt to shut down R. This method is asynchronous. */
    public void end() {
        alive=false;
        interrupt();
    }
    
    /** The implementation of the R thread. This method should not be called directly. */	
    public void run() {
		if (DEBUG>0)
			System.out.println("Starting R...");
        if (setupR(args)==0) {
            while (alive) {
                try {
                    if (runLoop) {                        
						if (DEBUG>0)
							System.out.println("***> launching main loop:");
                        loopRunning=true;
                        //runReplLoop();
                        rniCustomLoop();
						// actually R never returns from runMainLoop ...
                        loopRunning=false;
						if (DEBUG>0)
							System.out.println("***> main loop finished:");
                        runLoop=false;
						died=true;
						return;
                    }
                    sleep(idleDelay);
                    if (runLoop) rniIdle();
                } catch (InterruptedException ie) {
                    interrupted();
                }
            }
            died=true;
			if (DEBUG>0)
				System.out.println("Terminating R thread.");
        } else {
			System.err.println("Unable to start R");
        }
    }
	
	/** assign a string value to a symbol in R. The symbol is created if it doesn't exist already.
        @param sym symbol name.  The symbol name is used as-is, i.e. as if it was quoted in R code (for example assigning to "foo$bar" has the same effect as `foo$bar`&lt;- and NOT foo$bar&lt;-).
		@param ct contents
		@since JRI 0.3
        */
    public void assign(String sym, String ct) {
       	long x1 = rniPutString(ct);
       	rniAssign(sym,x1,0);
    }

    /** assign a content of a REXP to a symbol in R. The symbol is created if it doesn't exist already.
        @param sym symbol name. The symbol name is used as-is, i.e. as if it was quoted in R code (for example assigning to "foo$bar" has the same effect as `foo$bar`&lt;- and NOT foo$bar&lt;-).
        @param r contents as <code>REXP</code>. currently only raw references and basic types (int, double, int[], double[]) are supported.
		@since JRI 0.3
        */
    public void assign(String sym, REXP r) {
    	if (r.Xt == REXP.XT_INT || r.Xt == REXP.XT_ARRAY_INT) {
    		int[] cont = r.rtype == REXP.XT_INT?new int[]{((Integer)r.cont).intValue()}:(int[])r.cont;
    		long x1 = rniPutIntArray(cont);
    		rniAssign(sym,x1,0);
    	}
    	if (r.Xt == REXP.XT_DOUBLE || r.Xt == REXP.XT_ARRAY_DOUBLE) {
    		double[] cont = r.rtype == REXP.XT_DOUBLE?new double[]{((Double)r.cont).intValue()}:(double[])r.cont;
    		long x1 = rniPutDoubleArray(cont);
    		rniAssign(sym,x1,0);
    	}
		if (r.Xt == REXP.XT_STR || r.Xt == REXP.XT_ARRAY_STR) {
			String[] cont = r.rtype == REXP.XT_STR?new String[]{(String)r.cont}:(String[])r.cont;
			long x1 = rniPutStringArray(cont);
			rniAssign(sym,x1,0);
		}
    }

    /** assign values of an array of doubles to a symbol in R (creating an integer vector).<br>
        equals to calling {@link #assign(String, REXP)}.
		@param sym symbol name
		@param val double array to assign
		@since JRI 0.3
	*/
    public void assign(String sym, double[] val)  {
        assign(sym,new REXP(val));
    }

    /** assign values of an array of integers to a symbol in R (creating a numeric vector).<br>
        equals to calling {@link #assign(String, REXP)}.
		@param sym symbol name
		@param val integer array to assign
		@since JRI 0.3
		*/
	public void assign(String sym, int[] val) {
        assign(sym,new REXP(val));
    }

    /** assign values of an array of strings to a symbol in R (creating a character vector).<br>
        equals to calling {@link #assign(String, REXP)}.
		@param sym symbol name
		@param val string array to assign
		@since JRI 0.3
		*/
	public void assign(String sym, String[] val) {
        assign(sym,new REXP(val));
    }
}
