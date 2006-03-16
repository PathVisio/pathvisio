import java.io.File;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.*;

public class TestDbPerformance extends ApplicationWindow {
	String nl;
	
	String runstring = "Test";
	String abortstring = "Abort";
	
	Display display;
	Label timeLabel;
	Button startButton;
	Text resultText;
	TestInputGroup testInput;
	
	TestDb testDb;
	
	TestThread testThread;
	
	int nrTests;
	
	public TestDbPerformance() {
		super(null);
		testDb = new TestDb();
    }
	
	protected Control createContents(Composite parent) {
		display = getShell().getDisplay();
    	getShell().setText("GenMAPP DB Performance");
    	parent.setLayout(new GridLayout(2,false));
    	
    	resultText = new Text(parent,SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
    	resultText.setEditable(false);
    	
    	nl = resultText.getLineDelimiter();
    	
    	GridData textLayout = new GridData(GridData.FILL_BOTH);
    	textLayout.widthHint = 300;
    	textLayout.heightHint = 100;
    	textLayout.horizontalSpan = 2;
    	textLayout.grabExcessVerticalSpace = true;
    	resultText.setLayoutData(textLayout);
    	
    	testInput = new TestInputGroup(parent);
    	GridData groupLayout = new GridData(GridData.FILL_HORIZONTAL);
    	groupLayout.grabExcessHorizontalSpace = true;
    	testInput.setLayoutData(groupLayout);
    	
		startButton = new Button(parent, SWT.PUSH);
    	startButton.setText(runstring);
    	startButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING 
    										| GridData.VERTICAL_ALIGN_END));
    	startButton.addSelectionListener(new startButtonAdapter());
    	
    	parent.pack();
    	return parent;
	}
	
    public static void main(String[] args) {
    	
    	TestDbPerformance tdb = new TestDbPerformance();
    	tdb.setBlockOnOpen(true);
    	
    	tdb.open();
    	if(tdb.testThread != null) {
    		tdb.testThread.interrupt();
    	}
    	Display.getCurrent().dispose();
    }
	
    public void invertStartButtonLabel() {
    	display.asyncExec(new Runnable() {
    		public void run() {
    			if(startButton.getText().equals(runstring)) {
    				startButton.setText(abortstring);
    			} else {
    				startButton.setText(runstring);
    			}
    		}
    	});
    }
    
    public void errorMessage(int eNr) {
    	final int errorNr = eNr;
    	display.asyncExec(new Runnable() {
    		public void run() {
    			MessageBox messageBox = new MessageBox(getShell(),SWT.ICON_WARNING | SWT.OK);
    			switch(errorNr) {
    			case 1: // File missing
    				messageBox.setMessage("Input files missing!");
        			break;
    			case 2: // Number of tests not a number
    				messageBox.setMessage("Number of tests specified is not a valid number");
    				break;
    			default: // General error
    				messageBox.setMessage("Error");
    			}
    			messageBox.open();
    		}
    	});
    }
    
    public void doTests() {
    	// Connect to the database
    	testDb.connect();
    	// Recreate the tables
    	testDb.createTables();
    	// Create arrays for the test results
    	long[] gdbTimes = new long[nrTests];
    	long[] gexTimes = new long[nrTests];
    	long[] mappTimes = new long[nrTests];
    	// perform all tests
    	for(int i = 0; i < nrTests; i++) {
    		gdbTimes[i] = testDb.loadGdbTest();
    		gexTimes[i] = testDb.loadGexTest();
    		mappTimes[i] = testDb.loadMappTest();
    		// Check if test is aborted
    		if(gdbTimes[i] > -1 & gexTimes[i] > -1 & mappTimes[i] > -1) {
        		// Create output strings
        		String thisResult = "Results from test " + (i+1) + ":" + nl;
        		String gdbString = "> Loading Gene database took:" + gdbTimes[i] + " ms" + nl;
        		String gexString = "> Loading Expression data took:" + gexTimes[i] + " ms" + nl;
        		String mappString = "> Loading Pathway map took:" + mappTimes[i] + " ms" + nl;
        		updateTestResults(thisResult + gdbString + gexString + mappString);
    		} else {
    			updateTestResults("Test aborted!" + nl);
    			testDb.close();
    			invertStartButtonLabel();
    			return;
    		}
    	}
    	// Close the connection
    	testDb.close();
    	// Calculate average results
    	long gdbAvg = calculateAverage(gdbTimes);
    	long gexAvg = calculateAverage(gexTimes);
    	long mappAvg = calculateAverage(mappTimes);
		// Create output strings
		String thisResult = "Average results over " + nrTests + " tests:" + nl;
		String gdbString = "> Loading Gene database:" + gdbAvg + " ms" + nl;
		String gexString = "> Loading Expression data:" + gexAvg + " ms" + nl;
		String mappString = "> Loading Pathway map:" + mappAvg + " ms" + nl;
		updateTestResults(thisResult + gdbString + gexString + mappString);
    	// Invert the startButton label
    	invertStartButtonLabel();
	}
    
    public long calculateAverage(long[] array) {
    	long sum = 0;
    	for(int i = 0; i < array.length; i++) {
    		sum += array[i];
    	}
    	return sum / array.length;
    }
	
    public void updateTestResults(String resultString) {
    	final String result = resultString;
    	// update user interface
    	display.asyncExec(new Runnable() {
    		public void run() {
    			resultText.append(result);
    		}
    	});
    }
    
  	class startButtonAdapter extends SelectionAdapter {
    	public startButtonAdapter() {
    		super();
    	}
		
    	public void widgetSelected(SelectionEvent e) {
    		if(startButton.getText().equals(runstring)) {
    			// Clicked run, start the test
    			String gdb = testInput.gdbText.getText();
        		String gex = testInput.gexText.getText();
        		String mapp = testInput.mappText.getText();
    			// Check for required files
    			if(!gdb.equals("") && !gex.equals("")) {
    				testDb.gdbFile = new File(gdb);
    				testDb.gexFile = new File(gex);
    				if(!mapp.equals("")) {
    					testDb.mappFile = new File(mapp);
    				}
    				// Try to set the number of tests specified
    				nrTests = 1;
    				try {
    					nrTests = Integer.parseInt(testInput.nrTestText.getText());
    				} catch(NumberFormatException ne) {
    					errorMessage(2);
    				}
    				// Invert the startButton label
    				invertStartButtonLabel();
    				resultText.append("Starting database tests" + nl);
    				// Do the tests in a seperate thread
    				testThread = new TestThread();
    				testThread.start();
    			} else {
    				errorMessage(1);
    			}
    		} else {
    			// Clicked abort, interrupt the thread
    			testThread.interrupt();
    		}				
		}    	
    }
	
  	public class TestThread extends Thread {
		public void run() {
			doTests();
		}
		public void interrupt() {
			testDb.isInterrupted = true;
		}
	}
}