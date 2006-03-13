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
    
    private long gdbTime;
    private long gexTime;
    private long mappTime;
    
    public void doTests() {
    	// Connect to the database
    	testDb.connect();
    	// Recreate the tables
    	testDb.createTables();
		// perform all tests
    	gdbTime = testDb.loadGdbTest();
    	gexTime = testDb.loadGexTest();
    	mappTime = testDb.loadMappTest();
    	// Close the connection
    	testDb.close();
    	// update user interface
    	display.asyncExec(new Runnable() {
    		public void run() {
    			if(gdbTime > -1) {
    				resultText.append("> Loading Gene database took " + gdbTime + " ms" + nl);
    			} else {
    				resultText.append("> Loading Gene database was aborted" + nl);
    			}
    			if(gexTime > -1) {
    				resultText.append("> Loading Expression data took " + gexTime + " ms" + nl);
    			} else {
    				resultText.append("> Loading Expression data was aborted" + nl);
    			}
    			if(mappTime > -1) {
    				resultText.append("> Loading Pathway map took " + mappTime + " ms" + nl);
    			} else {
    				resultText.append("> Loading Pathway map was aborted" + nl);
    			}
    		}
    	});
    	// Invert the startButton label
    	invertStartButtonLabel();
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
    				// Invert the startButton label
    				invertStartButtonLabel();
    				resultText.append("Starting database tests" + nl);
    				// Do the tests in a seperate thread
    				testThread = new TestThread();
    				testThread.start();
    			} else {
    				MessageBox fileMissing = new MessageBox(getShell(),SWT.ICON_WARNING | SWT.OK);
    				fileMissing.setMessage("Input files missing!");
    				fileMissing.open();
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