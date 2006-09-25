package R;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVision.PropertyEvent;
import gmmlVision.GmmlVision.PropertyListener;

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
					else if(!re.isAlive()) re.run();
					m.done();
				}
			});
		} catch(InterruptedException ie) { 
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
		} catch(RException re) {
			startError(new Exception("Unable to load required libraries: " + re.getMessage()));
			return false;
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
			re.end();
			re.interrupt();
		}
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
