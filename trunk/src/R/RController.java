package R;

import gmmlVision.GmmlVision;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.rosuda.JRI.Rengine;

import R.RCommands.RException;

public abstract class RController {
	static final String importGmmlR = "library(GmmlR)";
	
	private static Rengine re;
	
	public static Rengine getRengine() throws RException { 
		if(re != null && re.isAlive()) return re;
		throw new RException(re, "R is not started");
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
					re = new Rengine(rArgs, false, null);
					m.done();
				}
			});
		} catch(InterruptedException ie) { 
		} catch(Exception e) {
			startError(e);
			return false;
		}
		if(!re.waitForR()) { //Unable to load if waitForR() is true
			startError(new Exception("Rengine.waitForR() is true"));
			return false;
		}
		
		try {
			importLibraries();
		} catch(RException re) {
			startError(new Exception("Unable to load required libraries: " + re.getMessage()));
			return false;
		}
		return true;
	}
	
	private static void importLibraries() throws RException {
		RCommands.evalE(re, importGmmlR); //GmmlR package, don't continue without it
	}
	
	public static void endR() {
		if(re != null && re.isAlive()) {
			re.interrupt();
			while(re.isAlive()) {} //Wait for R to shutdown
		}
	}

	private static void startError(Exception e) {
		MessageDialog.openError(GmmlVision.getWindow().getShell(), 
				"Unable to load R-engine", e.getClass() + ": " + e.getMessage());
		GmmlVision.log.error("Unable to load R-engine", e);
	}
}
