package R;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVision.PropertyEvent;
import gmmlVision.GmmlVision.PropertyListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.rosuda.JRI.Rengine;

import util.JarUtils;
import util.Utils;
import R.RCommands.RException;

public class RController implements PropertyListener{	
	private static Rengine re;
	private static BufferedReader rOut;
	
	public static Rengine getR() throws RException { 
		if(re == null || !re.isAlive()) startR();
		return re;
	}
	
	static { Rengine.DEBUG = 0; }
	
	//NOTE: commandline arguments don't seem to work...
	static final String[] rArgs = new String[] {
		"--vanilla", //No messages at all, no save, no restore
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
						if(e instanceof InvocationTargetException) 
							throw (InvocationTargetException)e;
						else throw new InvocationTargetException(e);
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
	
	
	private static void doStartR() throws UnsatisfiedLinkError, IOException, RException, InvocationTargetException, InterruptedException {
		//Extract the JRI shared library for the OS / R version on this system
		extractJRI();
		
		if(re == null) re = new Rengine(rArgs, false, null);
		if(re.isInterrupted()) re.start();
		
		if(!re.waitForR()) { //R is not started
			throw new RException(re, "Rengine.waitForR() is false");
		}
		
		try { //Redirect the R standard output to a text file
			if(rOut == null) sink(File.createTempFile("visioR", null, null));
		} catch(Exception e) {
			throw new InvocationTargetException(e, "Unable to redirect R standard output to file");
		}
		
		try { //Import or install R libraries and load functions
			try { importLibraries(); } catch(RException re) { 
				installLibraries();
				importLibraries();
			}
		} catch(Exception e) {
			throw new InvocationTargetException(e, "Unable to load or install required libraries");
		}
		
		RFunctionLoader.loadFunctions();
	}
	
	private static void extractJRI() throws IOException, UnsatisfiedLinkError, InterruptedException {
		GmmlVision.log.trace("Loading R");
		
		String ext = LIB_JRI_FILE.substring(LIB_JRI_FILE.lastIndexOf('.'));
		String rversion = getRVersion();
		GmmlVision.log.trace("\tDetected R version " + rversion);
		File libFile = null;
		try {
			libFile = JarUtils.resourceToNamedTempFile("lib/JRI-lib/jri-" + 
					rversion.substring(0, 3) + ext, LIB_JRI_FILE, false);
		} catch(Exception e) {
			throw new IOException(ERR_NO_JRI_VERSION + 
					"\nCurrently installed R version: " + rversion + "\n");
		}
				
		GmmlVision.log.trace("\tExtracted library " + libFile.toString());
		
		//Load the library
		loadJRI(libFile);
	}
	
	/**
	 * 
	 * @return R_MAJOR.R_MINOR
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private static String getRVersion() throws IOException, InterruptedException {
		switch(Utils.getOS()) {
		default:
		case Utils.OS_LINUX:
			//Assume that R is in path
			return getRVersionCmd();
		case Utils.OS_WINDOWS:
			//Location should be in registry
			return getRVersionReg();
		}
	}

	private static String getRVersionReg() throws IOException, InterruptedException {
		Process child = Runtime.getRuntime().exec(CMD_R_VERSION_REG);
		//If registry key can't be found exit code will be 1: try via R version command
		if(child.waitFor() != 0) return getRVersionCmd();
		
		InputStream in = child.getInputStream();
		StringBuilder output = new StringBuilder();
		int c;
		while ((c = in.read()) != -1) output.append((char)c);
		in.close();

		//Match 'REG_SZ' -> output will be "REG_SZ x.x.x"
		Pattern regex = Pattern.compile("REG_SZ");
		Matcher matcher = regex.matcher(output.toString());
		if(matcher.find())
			return output.substring(matcher.end() + 1, matcher.end() + 6);
		else
			return null;
	}
	
	private static String getRVersionCmd() throws IOException, InterruptedException {
		Process child = null;
		try {
			child = Runtime.getRuntime().exec(CMD_R_VERSION);
		} catch(IOException e) {
			child = Runtime.getRuntime().exec(specifyRVersionCmd());
		}
		child.waitFor();
		InputStream in = child.getInputStream();
		StringBuilder output = new StringBuilder();
		int c;
		while ((c = in.read()) != -1) output.append((char)c);
		in.close();

		//Match 'R version x.x.x'
		String prefix = "R version ";
		Pattern regex = Pattern.compile(prefix + "[0-9].[0-9].[0-9]");
		Matcher matcher = regex.matcher(output.toString());
		if(matcher.find())
			return output.substring(matcher.start() + prefix.length(), matcher.end());
		else
			return null;
	}
	
	private static String specifyRVersionCmd() {
		final StringBuilder cmd = new StringBuilder();
		GmmlVision.getWindow().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				String exec = Utils.getOS() == Utils.OS_WINDOWS ? "R.exe" : "R";
				InputDialog libDialog = new InputDialog(GmmlVision.getWindow().getShell(),
						"Unable to find R executable",
						"System can't find " + exec + "\nPlease specify location:", "", null);
				if(libDialog.open() == InputDialog.OK) {
					cmd.append(new File(libDialog.getValue()).getPath() + "/" + CMD_R_VERSION);
				}
			}
		});
		return cmd.toString();
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
	
	private static void installLibraries() throws FileNotFoundException, IOException, RException, InterruptedException {
		File pkgFile;
		switch(Utils.getOS()) {
		case Utils.OS_WINDOWS:
			pkgFile = JarUtils.resourceToNamedTempFile(INSTALL_GmmlR_win, "GmmlR");
			String pkgFileName = RCommands.fileToString(pkgFile);
			RCommands.eval("install.packages('" + pkgFileName + "', repos=NULL)");
			break;			
		case Utils.OS_LINUX: //In linux we need to be root to install (is gksudo available in all linux distr?)
		default:
			pkgFile = JarUtils.resourceToNamedTempFile(INSTALL_GmmlR_linux, "GmmlR");
			File srcFile = createInstallScript(pkgFile);
			Process proc = Runtime.getRuntime().exec("gksudo R CMD BATCH " + srcFile.toString());
			proc.waitFor();
		}
	}
	
	public static File createInstallScript(File pkgFile) throws IOException {
		File srcFile = File.createTempFile("install", null);
		FileWriter out = new FileWriter(srcFile);
		out.append("install.packages('" + RCommands.fileToString(pkgFile) + "', repos=NULL)");
		out.close();
		return srcFile;
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
		if (e instanceof InvocationTargetException)
			e = e.getCause();
		if(e instanceof UnsatisfiedLinkError) {
			if(e.getMessage().contains(LIB_R_FILE)) msg = ERR_MSG_NOR;	
			else msg = ERR_MSG_NOR;
		}
		GmmlVision.log.error(ERR_MSG_PRE, e);
		openError(msg, e);
	}
		
	public static void openError(final String msg, final Throwable e) {
		GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(GmmlVision.getWindow().getShell(), 
						ERR_MSG_PRE, (msg == null ? "" : msg + "\n \n Details:\n") + e.getMessage() + 
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
	
	//Shared libraries
	static final String LIB_R_FILE = System.mapLibraryName("R");
	static final String LIB_JRI_FILE = System.mapLibraryName("jri");
	
	//Shell commands
	static final String CMD_R_VERSION = "R --version";
	static final String CMD_R_VERSION_REG = 
		"REG QUERY HKLM\\Software\\R-core\\R /v \"Current Version\"";

	//Error messages
	static final String WWW_R = "http://www.r-project.org";
	static final String ERR_NO_JRI_VERSION = 
		"Your version of R is not supported, pleases install an updated version of R (" + WWW_R + ")"; 
	static final String ERR_MSG_PRE = "Problems when starting R-interface";
	static final String ERR_MSG_NOR_LINUX = 
		" and that environment variable LD_LIBRARY_PATH " +
		"is set to the directory containing " + LIB_R_FILE + "\n";
	static final String ERR_MSG_NOR = 
		"Unable to find the R shared library '" + LIB_R_FILE +
		"'.\nMake sure you have installed R (" + WWW_R + ") and added the " +
				" directory containing " + LIB_R_FILE + " to your PATH environment variable" +
		(Utils.getOS() == Utils.OS_LINUX ? ERR_MSG_NOR_LINUX : "");
	
	//R scripts/libraries/commands
	static final String IMPORT_LIB_GmmlR = "library(GmmlR)";
	static final String INSTALL_GmmlR_win = "R/library/GmmlR.zip";
	static final String INSTALL_GmmlR_linux = "R/library/GmmlR.tar.gz";
	static final String INSTALL_SCRIPT = "R/library/install.R";
}
