import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TestDbPerformance extends ApplicationWindow {
	String nl; // new line character for resultText
	
	String runstring = "Test"; // label for startButton
	String abortstring = "Abort"; // label for startButton when running test
	
	Display display;
	Shell shell;
	Label timeLabel;
	Button startButton;
	Text resultText;
	TestInputGroup testInput;
	ProgressIndicator indicator;
	IProgressMonitor progressMonitor;
	
	TestDbEngines testDbEngines;
	
	TestThread testThread;
	ProgressThread progressThread;
	
	volatile int nrTests;
	volatile double workDone;
	
	public TestDbPerformance() {
		super(null);
		testDbEngines = new TestDbEngines(this);
	}
	
	protected Control createContents(Composite parent) {    	
		shell = getShell();
		shell.setText("GenMAPP DB Performance");
		shell.setSize(800, 600);
		shell.setLocation(100, 100);
		display = shell.getDisplay();
		parent.setLayout(new GridLayout(2,false));
		
		resultText = new Text(parent,SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		resultText.setEditable(false);
		
		nl = resultText.getLineDelimiter();
		
		GridData textLayout = new GridData(GridData.FILL_BOTH);
		textLayout.widthHint = 300;
		textLayout.heightHint = 300;
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
		
		indicator = new ProgressIndicator(parent);
		
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
	
	public long[] calculateStats(long[] array) {
		long sum = 0;
		long dsum = 0;
		for(int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		long avg = sum / array.length;
		for(int i = 0; i < array.length; i++) {
			dsum += Math.abs(avg - array[i]);
		}
		return new long[] { sum / array.length, dsum / array.length };
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
				int testType = testInput.selectTest.getSelectionIndex();
				// Check for required files
				if(!gdb.equals("") && (!gex.equals("") | (testType !=4))) {
					testDbEngines.gdbFile = new File(gdb);
					testDbEngines.gexFile = new File(gex);
					if(!mapp.equals("")) {
						testDbEngines.mappFile = new File(mapp);
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
					// Do the tests in a seperate thread
					testThread = new TestThread(testType);
					progressThread = new ProgressThread();
					testThread.start();
					progressThread.start();
				} else {
					errorMessage(1);
				}
			} else {
				// Clicked abort, interrupt the thread
				testThread.interrupt();
			}				
		}    	
	}
	
	public class ProgressThread extends Thread {
		volatile boolean interrupted;
		Object watch;
		public ProgressThread() {
			super();
			interrupted = false;
			indicator.beginTask(100);
		}
		public void run() {
			// Update progress indicator when needed
			workDone = 0;
			double oldProgress = 0;
			testDbEngines.progress = 0;
			while(!testThread.isInterrupted() & !interrupted) {
				// Check for new progress
				if((int)oldProgress != (int)testDbEngines.progress) {
					workDone = (testDbEngines.progress - oldProgress) / nrTests;
					oldProgress = testDbEngines.progress;
					display.asyncExec(new Runnable() {
						public void run() {
							indicator.worked(workDone);
						}
					});
					try {
						sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			setIndicatorDone();
		}  		
		public void interrupt() {
			interrupted = true;
			setIndicatorDone();
		}
		public void setIndicatorDone() {
			display.asyncExec(new Runnable() {
				public void run() {
					indicator.done();
				}
			});
		}
	}
	
	public class TestThread extends Thread {
		int testType;
		
		public TestThread(int testType) {
			this.testType = testType;
		}
		
		public void run() {
			switch(testType) {
			case 0: // load gdb (using hsqldb)
				updateTestResults(nl + "Starting 'Load Gene Database with hsqldb' test"+ nl);
				loadGdbTest(TestDbEngines.HDB);
				break;
			case 1: // load gdb (using Daffodil)
				updateTestResults(nl + "Starting 'Load Gene Database with Daffodil' test"+ nl);
				loadGdbTest(TestDbEngines.DAF);
				break;
			case 2: // load gdb (using Derby)
				updateTestResults(nl + "Starting 'Load Gene Database with Derby' test"+ nl);
				loadGdbTest(TestDbEngines.DERBY);
				break;
			case 3: // load gdb (using McKoi)
				updateTestResults(nl + "Starting 'Load Gene Database with McKoi' test"+ nl);
				loadGdbTest(TestDbEngines.MCKOI);
				break;
			case 4: // Do all tests
				updateTestResults(nl + "Starting all database tests" + nl);
				doAllTests();
				break;
			case 5: // load gdb using TEXT table
				updateTestResults(nl + "Starting 'Load Gene Database (TEXT table)' test"+ nl);
				loadGdbTextTest();
				break;
			default:
				doAllTests();
			}
			progressThread.interrupt();
		}
		public void interrupt() {
			updateTestResults("Starting all database tests" + nl);
			testDbEngines.isInterrupted = true;
		}
		public boolean isInterrupted() {
			return testDbEngines.isInterrupted;
		}
	}
	
	public void loadGdbTextTest() {
		// Connect to the database
		testDbEngines.connectGdb();
		// Create arrays for the test results
		long[] gdbTimes = new long[nrTests];
		long[] connTimes = new long[nrTests];
		// Perform tests
		for(int i = 0; i < nrTests; i++) {
			connTimes[i] = testDbEngines.connectHdb();
			// Remove and recreate the tables
			testDbEngines.createTablesHdb();
			// Modify the gdb table to TEXT
			testDbEngines.createGdbTextTable();
			gdbTimes[i] = testDbEngines.loadGdbTest(TestDbEngines.HDB);
			testDbEngines.close();
			// Check if test is aborted
			if(gdbTimes[i] > -1) {
				// Create output strings
				String thisResult = "Results from test " + (i+1) + ":" + nl;
				String connString = "> Connecting to database: " + connTimes[i] + " ms" + nl;
				String gdbString = "> Loading Gene database: " + gdbTimes[i] + " ms" + nl;
				updateTestResults(thisResult + connString + gdbString);
			} else {
				updateTestResults("Test aborted!" + nl);
				invertStartButtonLabel();
				return;
			}
		}
		if(nrTests > 1) {
			// Calculate average results
			long[] gdbStat = calculateStats(gdbTimes);
			long[] connStat = calculateStats(connTimes);
			// Create output strings
			String thisResult = "Average results over " + nrTests + " tests:" + nl;
			String connString = "> Connecting to database: " + connStat[0] + " (+/- "
			+ connStat[1] + ") ms" + nl;
			String gdbString = "> Connecting to database: " + gdbStat[0] + " (+/- "
			+ gdbStat[1] + ") ms" + nl;
			updateTestResults(thisResult + connString + gdbString);
		}
		// Set progress to 100 (just in case)
		workDone = 100;
		// Invert the startButton label
		invertStartButtonLabel();
		
	}
	public void loadGdbTest(int db) {
		// Create arrays for the test results
		long[] gdbTimes = new long[nrTests];
		long[] connTimes = new long[nrTests];
		// Perform tests
		for(int i = 0; i < nrTests; i++) {
			// Connect to the genmapp database
			testDbEngines.connectGdb();
			// Connect and recreate the tables
			switch(db) {
			case TestDbEngines.HDB: //hsqldb
				connTimes[i] = testDbEngines.connectHdb();
				testDbEngines.createTablesHdb();
				break;
			case TestDbEngines.DAF: //daf
				connTimes[i] = testDbEngines.connectDaf();
				testDbEngines.createTables(testDbEngines.conDaf);
				break;
			case TestDbEngines.DERBY: //derby
				connTimes[i] = testDbEngines.connectDerby();
				testDbEngines.createTables(testDbEngines.conDer);
				break;
			case TestDbEngines.MCKOI: //McKoi
				connTimes[i] = testDbEngines.connectMck();
				testDbEngines.createTables(testDbEngines.conMck);
				break;
			}
			gdbTimes[i] = testDbEngines.loadGdbTest(db);
			// Close the connection
			testDbEngines.close();
			// Check if test is aborted
			if(gdbTimes[i] > -1) {
				// Create output strings
				String thisResult = "Results from test " + (i+1) + ":" + nl;
				String connString = "> Connecting to database: " + connTimes[i] + " ms" + nl;
				String gdbString = "> Loading Gene database: " + gdbTimes[i] + " ms" + nl;
				updateTestResults(thisResult + connString + gdbString);
			} else {
				updateTestResults("Test aborted!" + nl);
				invertStartButtonLabel();
				return;
			}
		}
		if(nrTests > 1) {
			// Calculate average results
			long[] gdbStat = calculateStats(gdbTimes);
			long[] connStat = calculateStats(connTimes);
			// Create output strings
			String thisResult = "Average results over " + nrTests + " tests:" + nl;
			String connString = "> Connecting to database: " + connStat[0] + " (+/- "
			+ connStat[1] + ") ms" + nl;
			String gdbString = "> Connecting to database: " + gdbStat[0] + " (+/- "
			+ gdbStat[1] + ") ms" + nl;
			updateTestResults(thisResult + connString + gdbString);
		}
		// Set progress to 100 (just in case)
		workDone = 100;
		// Invert the startButton label
		invertStartButtonLabel();
	}
	
	public void doAllTests() {
		// Connect to the database
		testDbEngines.connectGdb();
		testDbEngines.connectGex();
		testDbEngines.connectHdb();
		// Create arrays for the test results
		long[] gdbTimes = new long[nrTests];
		long[] gexTimes = new long[nrTests];
		long[] mappTimes = new long[nrTests];
		
		// perform all tests
		for(int i = 0; i < nrTests; i++) {
			// Remove and recreate the tables
			testDbEngines.createTablesHdb();
			gdbTimes[i] = testDbEngines.loadGdbTest(TestDbEngines.HDB);
			gexTimes[i] = testDbEngines.loadGexTest();
			mappTimes[i] = testDbEngines.loadMappTest();
			// Check if test is aborted
			if(gdbTimes[i] > -1 & gexTimes[i] > -1 & mappTimes[i] > -1) {
				// Create output strings
				String thisResult = "Results from test " + (i+1) + ":" + nl;
				String gdbString = "> Loading Gene database: " + gdbTimes[i] + " ms" + nl;
				String gexString = "> Loading Expression data: " + gexTimes[i] + " ms" + nl;
				String mappString = "> Loading Pathway map: " + mappTimes[i] + " ms" + nl;
				updateTestResults(thisResult + gdbString + gexString + mappString);
			} else {
				updateTestResults("Test aborted!" + nl);
				testDbEngines.close();
				invertStartButtonLabel();
				return;
			}
		}
		// Close the connection
		testDbEngines.close();
		if(nrTests > 1) {
			// Calculate average results
			long[] gdbStat = calculateStats(gdbTimes);
			long[] gexStat = calculateStats(gexTimes);
			long[] mappStat = calculateStats(mappTimes);
			// Create output strings
			String thisResult = "Average results over " + nrTests + " tests:" + nl;
			String gdbString = "> Loading Gene database:" + gdbStat[0] + " ms" + nl;
			String gexString = "> Loading Expression data:" + gexStat[0] + " ms" + nl;
			String mappString = "> Loading Pathway map:" + mappStat[0] + " ms" + nl;
			updateTestResults(thisResult + gdbString + gexString + mappString);
		}
		// Set progress to 100 (just in case)
		workDone = 100;
		// Invert the startButton label
		invertStartButtonLabel();
	}
}


