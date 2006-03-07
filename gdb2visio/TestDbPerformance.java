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
	
	Label timeLabel;
	Button startButton;
	Text resultText;
	TestInputGroup testInput;
	
	TestData testData;
	
	public TestDbPerformance() {
		super(null);
		testData = new TestData();
    }
	
	protected Control createContents(Composite parent) { 
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
    	startButton.setText("test!");
    	startButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING 
    										| GridData.VERTICAL_ALIGN_END));
    	startButton.addSelectionListener(new SelectionAdapter() {
    		public void widgetSelected(SelectionEvent e) {
    			// Check for required files
    			if(testInput.gdbFile != null 
    					&& testInput.gexFile != null 
    					&& testInput.mappFile != null) {
    				long elapsedTime = testData.doTest();
    				resultText.setText("Loading the database took "+Long.toString(elapsedTime)+" ms");
    			} else {
    				MessageBox fileMissing = new MessageBox(getShell(),SWT.ICON_WARNING | SWT.OK);
    				fileMissing.setMessage("Input files missing!");
    				fileMissing.open();
    			}				
    		}
    	});
    	
    	parent.pack();
    	return parent;
	}
	
    public static void main(String[] args) {
    	
    	TestDbPerformance tdb = new TestDbPerformance();
    	tdb.setBlockOnOpen(true);
    	
    	tdb.open();
    	Display.getCurrent().dispose();
    }
}