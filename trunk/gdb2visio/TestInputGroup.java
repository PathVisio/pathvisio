import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class TestInputGroup extends Composite {
	final int GDB = 0;
	final int GEX = 1;
	final int MAPP = 2;
	
	Group group;
	Button gdbButton;
	Button gexButton;
	Button mappButton;
	Label gdbLabel;
	Label gexLabel;
	Label mappLabel;
	Text gdbText;
	Text gexText;
	Text mappText;
	
	File gdbFile;
	File gexFile;
	File mappFile;
	
	public TestInputGroup(Composite parent) {
		super(parent, SWT.NONE);
		
		group = new Group(this, SWT.SHADOW_ETCHED_IN);
		group.setText("Test data");
		
		group.setLayout(new GridLayout(3,false));
		
		GridData textLayout = new GridData(GridData.FILL_HORIZONTAL);
		textLayout.grabExcessHorizontalSpace = true;
		textLayout.widthHint = 300;
		
		gdbLabel = new Label(group, SWT.CENTER);
		gdbLabel.setText("Gene Database:");
		gdbText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gdbText.setLayoutData(textLayout);
		gdbButton = new Button(group, SWT.PUSH);
		gdbButton.setText("Browse");
		gdbButton.addListener(SWT.Selection, new browseListener(GDB));
		
		gexLabel = new Label(group, SWT.CENTER);
		gexLabel.setText("Expressionset:");
		gexText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gexText.setLayoutData(textLayout);
		gexButton = new Button(group, SWT.PUSH);
		gexButton.setText("Browse");
		
		
		mappLabel = new Label(group, SWT.CENTER);
		mappLabel.setText("Pathway map:");
		mappText = new Text(group, SWT.SINGLE | SWT.BORDER);
		mappText.setLayoutData(textLayout);
		mappButton = new Button(group, SWT.PUSH);
		mappButton.setText("Browse");
		
		group.pack();
	}
	
	public class browseListener implements Listener {
		private int type;
		
		public browseListener(int type) {
			this.type = type;
		}
		
		public void handleEvent(Event e) {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			switch(type) {
			case GDB:
				fileDialog.setFilterExtensions(new String[] {"*.gdb"});
				fileDialog.setFilterNames(new String[] {"Gene Database"});
				gdbFile = new File(fileDialog.open());
				break;
			case GEX:
				fileDialog.setFilterExtensions(new String[] {"*.gex"});
				fileDialog.setFilterNames(new String[] {"Expression Dataset"});
				gexFile = new File(fileDialog.open());
				break;
			case MAPP:
				fileDialog.setFilterExtensions(new String[] {"*.mapp"});
				fileDialog.setFilterNames(new String[] {"GenMAPP pathway"});
				gdbFile = new File(fileDialog.open());
				break;
			}
		}
	}
}
