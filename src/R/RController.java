package R;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVision.PropertyEvent;
import gmmlVision.GmmlVision.PropertyListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.rosuda.JRI.Rengine;

import rversion.GetRVersion;
import util.JarUtils;
import util.Utils;
import R.RCommands.RException;

public class RController implements PropertyListener{
	static final String ERR_MSG_PRE = "Problems when starting R-interface";
	static final String IMPORT_LIB_GmmlR = "library(GmmlR)";
	static final String INSTALL_GmmlR_win = "R/library/GmmlR.zip";
	static final String INSTALL_GmmlR_linux = "R/library/GmmlR.tar.gz";
	
	private static Rengine re;
	private static BufferedReader rOut;
	
	public static Rengine getR() throws RException { 
		if(re == null || !re.isAlive()) startR();
		return re;
	}
	
	static { Rengine.DEBUG = 0; }
	
	//NOTE: commandline arguments don't seem to work...
	static final String[] rArgs = new String[] {
		"--no-save", 			//Don't save .RData on quit
		"--no-restore",			//Don't restore .RData on startup
		"--quiet"				//Don't display copyright message
	};
			
	public static boolean startR() {
		//Start R-engine (with progress monitor)
		try {
			new ProgressMonitorDialog(GmmlVision.getWindow().getShell()).run(true, true,
					new IRunnableWithProgress() {
				public void run(IProgressMonitor m) throws 	InvocationTargetException, 
				InterruptedException 
				{
					try {
						m.beginTask("Starting R engine", IProgressMonitor.UNKNOWN);
						doStartR();
						m.done();
						if(m.isCanceled()) throw new InterruptedException();
					} catch(Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch(Exception e) {
			startError(e);
			return false;
		} finally {
			//Add a listener to close R on closing gmml-visio
			GmmlVision.addPropertyListener(new RController());
		}

		return true;
	}
	
	
	private static boolean doStartR() throws UnsatisfiedLinkError, IOException, RException {
		//Extract the JRI shared library for the OS / R version on this system
		extractJRI();
		
		if(re == null) re = new Rengine(rArgs, false, null);
		if(re.isInterrupted()) re.start();
		
		if(!re.waitForR()) { //R is not started
			startError(new RException(re, "Rengine.waitForR() is false"));
			return false;
		}
		
		try { //Redirect the R standard output to a text file
			if(rOut == null) sink(File.createTempFile("visioR", null, null));
		} catch(Exception e) {
			startError("Unable to redirect R standard output to file", e);
			return false;
		}
		
		try { //Import or install R libraries and load functions
			try { importLibraries(); } catch(RException re) { 
				installLibraries();
				importLibraries();
			}
		} catch(Exception e) {
			startError("Unable to load required libraries or functions", e);
			return false;
		}
		
		RFunctionLoader.loadFunctions();
		
		return true;
	}
	private static void extractJRI() throws IOException, UnsatisfiedLinkError {
		GmmlVision.log.trace("Loading R");
		
		String libFileName = System.mapLibraryName("jri");
		String ext = libFileName.substring(libFileName.lastIndexOf('.'));
		String rversion = GetRVersion.rniGetVersionR(); //e.g. 2.2.1, ignore last digit
		
		GmmlVision.log.trace("\tDetected R version " + rversion);
		File libFile = JarUtils.resourceToNamedTempFile("lib/JRI-lib/jri-" + 
				rversion.substring(0, 3) + ext, libFileName, false);
		GmmlVision.log.trace("\tExtracted library " + libFile.toString());
		
		//Load the library
		loadJRI(libFile);
	}
	
	private static void loadJRI(File libFile) throws UnsatisfiedLinkError, IOException {
//		try {
			System.load(libFile.toString());
		/* Doesn't work
		 * We need to find a way to change LD_LIBRARY_PATH from within
		 * this application
		 */
//		} catch(UnsatisfiedLinkError e) {
//			//Problably libR.so or R.dll wasn't found
//			//Add to LD_LIBRARY_PATH...needs restart...
//			//Or try to load with System.load...?
//			String rLib = locateRLib();
//			if(rLib == null) throw e;
//			Runtime.getRuntime().exec("sh export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + rLib);
//			loadJRI(libFile);
//		}
	}
	
/* Useless, can't point jri to right location from within Java */
//	private static String locateRLib() {
//		final String CANCEL = "C";
//		final StringBuilder value = new StringBuilder(CANCEL);
//		GmmlVision.getWindow().getShell().getDisplay().syncExec(new Runnable() {
//			public void run() {
//				String libName = System.mapLibraryName("jri");
//				InputDialog libDialog = new InputDialog(GmmlVision.getWindow().getShell(),
//					"System couldn't find " + libName, "Please specify location of file " + libName, "", null);
//				if(libDialog.open() == InputDialog.OK) {
//					value.delete(0, CANCEL.length());
//					value.append(libDialog.getValue());
//				}
//			}
//				
//		});
//		String str = value.toString();
//		return str.equals(CANCEL) ? null : str;
//	}
	
	private static void importLibraries() throws RException {
		RCommands.eval(IMPORT_LIB_GmmlR); //GmmlR package, don't continue without it
	}
	
	private static void installLibraries() throws Exception {
		File pkgFile;
		switch(Utils.getOS()) {
		case Utils.OS_WINDOWS:
			pkgFile = JarUtils.resourceToNamedTempFile(INSTALL_GmmlR_win, "GmmlR"); break;			
		case Utils.OS_LINUX:	
		default:
			pkgFile = JarUtils.resourceToNamedTempFile(INSTALL_GmmlR_linux, "GmmlR");
		}
		String pkgFileName = RCommands.fileToString(pkgFile);
		RCommands.eval("install.packages('" + pkgFileName + "', repos=NULL)");
	}
	
	public static void interruptRProcess() {
		if(re != null) {
			try { re.rniStop(0); } catch(Exception e) { e.printStackTrace(); }
		}
	}
	
	public static void endR() {
		if(re != null) {
			re.end();
		}
	}

	public static void sink(File f) throws RException, FileNotFoundException, IOException {
		if(f == null) {
			if(rOut != null) rOut.close();
			RCommands.eval("sink()");
			rOut = null;
		}
		else {
			RCommands.eval("sink('" + RCommands.fileToString(f) + "')");
			rOut = new BufferedReader(new FileReader(f));
		}
	}
		
	public static String getNewOutput() {
		String output = null;
		if(rOut == null) return null;
		try {
			String line = null;
			while((line = rOut.readLine()) != null) 
				output = output == null ? line : output + "\n" + line;
		} catch(IOException e) {
			GmmlVision.log.error("Unable to read R output", e);
		}
		return output;
	}
	
	private static void startError(Throwable e) {
		startError(null, e);
	}
	
	private static void startError(String msg, Throwable e) {
		if(e instanceof InterruptedException) 
			return;
		else if (e instanceof InvocationTargetException)
			e = e.getCause();
		
		GmmlVision.log.error(ERR_MSG_PRE, e);
		openError(msg, e);
	}
	
	public static void openError(final String msg, final Throwable e) {
		GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(GmmlVision.getWindow().getShell(), 
						ERR_MSG_PRE, (msg == null ? "" : msg + "\n") + e.getMessage() + 
						" (" + e.getClass().getName() + ")");
			}
		});
	}
		
	public void propertyChanged(PropertyEvent e) {
		if(e.name == GmmlVision.PROPERTY_CLOSE_APPLICATION) {
			endR(); //End the R process
			if(rOut != null) { //Close the R output file
				try { 
					rOut.close();
				} catch(Exception ie) { 
					GmmlVision.log.error("Unable to close R output file", ie);
				}
			}
		}
	}
}
