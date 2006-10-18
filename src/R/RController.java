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

import R.RCommands.RException;

public class RController implements PropertyListener{
	static final String importGmmlR = "library(GmmlR)";
	
	private static Rengine re;
	
	private static final File rOutFile = new File("rout.txt");
	private static BufferedReader rOut;
	
	public static Rengine getR() throws RException { 
		if(re != null && re.isAlive()) return re;
		throw new RException(re, "R is thread not started yet");
	}
	
	static { Rengine.DEBUG = 1; }
	
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
					m.beginTask("Starting R engine", IProgressMonitor.UNKNOWN);
					if(re == null) re = new Rengine(rArgs, false, null);
					if(!re.isAlive()) re.run();
					m.done();
					if(m.isCanceled()) throw new InterruptedException();
				}
			});
		} catch(InterruptedException ie) { 
			return false;
		} catch(Exception e) {
			startError(e);
			return false;
		}
		if(!re.waitForR()) { //Unable to load if waitForR() is true
			startError(new RException(re, "Rengine.waitForR() is true"));
			return false;
		}
		
		try {
			importLibraries();
			RFunctionLoader.loadFunctions();
		} catch(Exception re) {
			startError(new Exception("Unable to load required libraries and functions: " + re.getMessage()));
			return false;
		}
		try {
			sink(rOutFile);
		} catch(Exception e) {
			startError(e);
		}
		//Add a listener to close R on closing gmml-visio
		GmmlVision.addPropertyListener(new RController());
		
		return true;
	}
	
	private static void importLibraries() throws RException {
		RCommands.eval(importGmmlR); //GmmlR package, don't continue without it
	}
	
	public static void endR() {
		if(re != null) {
			try { re.rniStop(0); } catch(Exception e) { e.printStackTrace(); }
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
			RCommands.eval("sink('" + f.toString() + "')");
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
	
	private static void startError(Exception e) {
		MessageDialog.openError(GmmlVision.getWindow().getShell(), 
				"Unable to load R-engine", e.getClass() + ": " + e.getMessage());
		GmmlVision.log.error("Unable to load R-engine", e);
	}
	
	public void propertyChanged(PropertyEvent e) {
		if(e.name == GmmlVision.PROPERTY_CLOSE_APPLICATION) {
			endR();
		}
	}
}
