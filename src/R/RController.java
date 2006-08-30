package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

public class RController extends ApplicationWindow implements RMainLoopCallbacks {
	Rengine re;
	Text rOutput;
	Text rInput;
	boolean inputReady;
	
	static { Rengine.DEBUG = 1; }
	
	//NOTE: commandline arguments don't seem to work...
	static final String[] rArgs = new String[] {
		"--no-save", 			//Don't save .RData on quit
		"--no-restore",			//Don't restore .RData on startup
		"--quiet"				//Don't display copyright message
	};
	static final String ln = Text.DELIMITER; 
	
	public RController(Shell shell) {		
		super(shell);
	}
	
	public Composite createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		content.setLayout(new GridLayout());
		
		rOutput = new Text(content, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData outGrid = new GridData(GridData.FILL_BOTH);
		outGrid.heightHint = 200;
		outGrid.widthHint = 300;
		rOutput.setLayoutData(outGrid);
		
		rInput = new Text(content, SWT.SINGLE | SWT.BORDER);
		rInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rInput.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR) inputReady = true;
				if(e.keyCode == SWT.ALT) { //TEST
					RData rdata = new RData(new File(
						"/home/thomas/afstuderen/visio_data/pathways/test"), true);
					rdata.doTest(re);
				}
			}
		});
		rInput.setFocus();
		
		return content;
	}
	
	public int open() {
		int o = super.open();
		
		rInput.setEnabled(false);
		re.addMainLoopCallbacks(this);
		re.startMainLoop();
		rInput.setEnabled(true);
		return o;
	}
	
	public boolean close() {
		endR();
		return super.close();
	}
	
	public boolean startR() {
		//Start R-engine (with progress monitor)
		try {
			new ProgressMonitorDialog(getShell()).run(true, true,
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
		return true;
	}
	
	public void endR() {
		if(re != null && re.isAlive()) {
			setInput("q(save='no');");
			while(re.isAlive()) {} //Wait for R to shutdown
		}
	}
	
	private void printConsole(final String line) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if(rOutput == null || rOutput.isDisposed()) return;
				rOutput.append(line);
			}
		});
	}
	
	private void setInput(String input) {
		altInput = input; 	//Results getInput() to return altInput
		while(rBusy) { } 	//Wait until R processed the command
	}
	
	String altInput;
	synchronized private String getInput() {
		final StringBuilder input = new StringBuilder();
		while(!inputReady) { 
			// Wait until user pressed RC key
			if(re.isInterrupted()) { return ""; } //return if thread is interrupted
			if(altInput != null) { //process input passed programatically
				String toReturn = new String(altInput);
				altInput = null;
				return toReturn;
			}
		}
		
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				input.append(rInput.getText());
				rInput.setText("");
			}
		});
		return input.toString();
	}
	
	private void startError(Exception e) {
		MessageDialog.openError(getShell(), "Unable to load R-engine", e.getMessage());
		GmmlVision.log.error("Unable to load R-engine", e);
	}
	
	//LoopCallBack implementation
	public void rWriteConsole(Rengine re, String msg) { 
		GmmlVision.log.trace("R> " + msg);
		printConsole(msg);
	}
	
	boolean rBusy = false;
	public void rBusy(Rengine re, int which) { 
		rBusy = which == 1;
		String msg = which == 0 ? "" : "Busy...\n";
		printConsole(msg);
	}
	
	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		if(rInput == null || rInput.isDisposed()) return "";
		
		printConsole(prompt);
		inputReady = false;
		
		return getInput();
	}
	
	public void rShowMessage(Rengine re, String msg) {
		MessageDialog.openInformation(getShell(), "R message", msg);
	}
	
	public String rChooseFile(Rengine re, int newFile) {	return null; }
	
	public void rFlushConsole(Rengine re) { }
	
	public void rSaveHistory(Rengine re, String filename) { }
	
	public void rLoadHistory(Rengine re, String filename) { }
	
}
