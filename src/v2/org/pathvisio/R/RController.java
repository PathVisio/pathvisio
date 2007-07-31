// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.R.RCommands.RException;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.util.JarUtils;
import org.pathvisio.util.Utils;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

public class RController implements ApplicationEventListener{	
	private static Rengine re;
	private static BufferedReader rOut;
	
	private static File pkgFile;
	
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
			new ProgressMonitorDialog(SwtEngine.getCurrent().getWindow().getShell()).run(true, true,
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
			//Add a listener to close R on closing PathVisio
			Engine.getCurrent().addApplicationEventListener(new RController());
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
		} catch(Throwable e) {
			throw new InvocationTargetException(e, "Unable to redirect R standard output to file");
		}
		
		try { //Import or install R package and load functions
				installPackage();
				importPackage();
		} catch(Throwable e) {
			throw new InvocationTargetException(e, "Unable to load or install required libraries");
		}
		
		RFunctionLoader.loadFunctions();
	}
	
	private static void extractJRI() throws IOException, UnsatisfiedLinkError, InterruptedException {
		Logger.log.trace("Loading R");
		
		String ext = LIB_JRI_FILE.substring(LIB_JRI_FILE.lastIndexOf('.'));
		String rversion = getRVersion();
		Logger.log.trace("\tDetected R version " + rversion);
		File libFile = null;
		try {
			libFile = JarUtils.resourceToNamedTempFile(LIB_JRI_PATH + "/jri-" + 
					rversion.substring(0, 3) + ext, LIB_JRI_FILE, false);
		} catch(Exception e) {
			throw new IOException(ERR_NO_JRI_VERSION + 
					"\nCurrently installed R version: " + rversion + "\n");
		}
				
		Logger.log.trace("\tExtracted library " + libFile.toString());
		
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
			child = Runtime.getRuntime().exec(locateRExec());
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
	
	private static String locateRExec() {
		final StringBuilder cmd = new StringBuilder();
		SwtEngine.getCurrent().getWindow().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				String exec = Utils.getOS() == Utils.OS_WINDOWS ? "R.exe" : "R";
				InputDialog libDialog = new InputDialog(SwtEngine.getCurrent().getWindow().getShell(),
						"Unable to find R executable",
						"Unable to locate " + exec + "\nPlease install R (" + WWW_R + ") " +
						" or specify location:", "", null);
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
//		Engine.getCurrent().getWindow().getShell().getDisplay().syncExec(new Runnable() {
//			public void run() {
//				String libName = System.mapLibraryName("jri");
//				InputDialog libDialog = new InputDialog(Engine.getCurrent().getWindow().getShell(),
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
	
	private static void importPackage() throws RException {
		RCommands.eval(IMPORT_LIB_PKG); //GmmlR package, don't continue without it
	}

	private static void installPackage() throws FileNotFoundException, IOException, RException, InterruptedException {
		File pkgFile = getPackageFile();
		if(needsPackageUpdate(pkgFile.getName())) {
			Logger.log.info("R package " + PKG_NAME + " is out of date or not installed yet: installing newest version");
			switch(Utils.getOS()) {
			case Utils.OS_WINDOWS:
				String pkgFileName = RCommands.fileToString(pkgFile);
				RCommands.eval("install.packages('" + pkgFileName + "', repos=NULL)");
				break;			
			case Utils.OS_LINUX: //In linux we need to be root to install (is gksudo available in all linux distr?)
			default:
				File srcFile = createInstallScript(pkgFile);
			Process proc = Runtime.getRuntime().exec("gksudo R CMD BATCH " + srcFile.toString());
			proc.waitFor();
			}
		}
	}
	
	static File getPackageFile() throws RException, IOException {
		if(pkgFile == null) {
		
		List<String> dircontent = JarUtils.listResources(PKG_PATH);
		String ext = null;
		switch(Utils.getOS()) {
		case Utils.OS_WINDOWS: 
			ext = "zip";
			break;
		case Utils.OS_LINUX:
		default: ext = "tar.gz";
		}
		Pattern regex = Pattern.compile(PKG_NAME + "_[0-9].[0-9].[0-9]." + ext);
		for(String f : dircontent) {
			Logger.log.trace(f);
			if(regex.matcher(f).find()) {
				pkgFile =  JarUtils.resourceToNamedTempFile(f, new File(f).getName());
				if(pkgFile != null) break;
			}
		}
		if(pkgFile == null) 
			throw new RException(null, "Unable to find " + PKG_NAME + " package");
		}
		return pkgFile;
	}
	
	static boolean needsPackageUpdate(String pkgFileName) throws RException {
		String curr = getCurrentPackageUpdate();
		if(curr == null) {
			return true;
		} else {
			String injar = file2Version(pkgFileName);
			return injar.compareTo(curr) > 0;
		}
	}
	
	static String getCurrentPackageUpdate() throws RException {
		REXP rexp = RController.getR().eval(
				"packageDescription('" + PKG_NAME + "', fields='Version')", true);
		return rexp.asString();
	}
	
	static String file2Version(String libFileName) {
		Pattern regex = Pattern.compile("_[0-9].[0-9].[0-9].");
		Matcher matcher = regex.matcher(libFileName);
		if(matcher.find())
			return libFileName.substring(matcher.start() + 1, matcher.end() - 1);
		else
			return "";
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
			Logger.log.error("Unable to read R output", e);
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
		Logger.log.error(ERR_MSG_PRE, e);
		openError(msg, e);
	}
		
	public static void openError(final String msg, final Throwable e) {
		SwtEngine.getCurrent().getWindow().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(SwtEngine.getCurrent().getWindow().getShell(), 
						ERR_MSG_PRE, (msg == null ? "" : msg + "\n \n Details:\n") + e.getMessage() + 
						" (" + e.getClass().getName() + ")");
			}
		});
	}
		
	public void applicationEvent(ApplicationEvent e) {
		if(e.type == ApplicationEvent.APPLICATION_CLOSE) {
			endR(); //End the R process
			if(rOut != null) { //Close the R output file
				try { 
					rOut.close();
				} catch(Exception ie) { 
					Logger.log.error("Unable to close R output file", ie);
				}
			}
		}
	}
	
	static final String PKG_NAME = "pathVisio";
	static final String PKG_PATH = "R/library";
	
	//Shared libraries
	static final String LIB_R_FILE = System.mapLibraryName("R");
	static final String LIB_JRI_FILE = System.mapLibraryName("jri");
	static final String LIB_JRI_PATH = "JRI-lib";
	
	//Shell commands
	static final String CMD_R_VERSION = "R --version";
	static final String CMD_R_VERSION_REG = 
		"REG QUERY HKLM\\Software\\R-core\\R /v \"Current Version\"";

	//Error messages
	static final String WWW_R = "http://www.r-project.org";
	static final String ERR_NO_JRI_VERSION = 
		"Your version of R is not supported, please install an updated version of R (" + WWW_R + ")"; 
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
	static final String IMPORT_LIB_PKG = "library(" + PKG_NAME + ")";
}
