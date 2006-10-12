
package R.wizard;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import R.RDataIn;
import R.RCommands.RException;
import R.wizard.RFunctionLoader.RFunction;

public class PageStats extends WizardPage {
	Combo comboFunc;
	Composite compSettings;
	
	public PageStats() {
		super("PageStats");
		
		setTitle("Apply a statistical function");
		setDescription("Choose a function to apply to the selected pathways and expression data");
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		content.setLayout(new GridLayout(2, false));
		
		Label flbl = new Label(content, SWT.NULL);
		flbl.setText("Choose a function to apply:");
		comboFunc = new Combo(content, SWT.BORDER | SWT.READ_ONLY);
		comboFunc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboFunc.setItems(RFunctionLoader.getFunctionNames());
		
		compSettings = new Composite(content, SWT.NULL);
		GridData span2Cols = new GridData(GridData.FILL_BOTH);
		span2Cols.horizontalSpan = 2;
		compSettings.setLayoutData(span2Cols);
		
		compSettings.setLayout(new StackLayout());
		for(String fn : comboFunc.getItems()) {
			RFunctionLoader.getFunction(fn).getConfigComposite(compSettings);
		}
		
		addListeners();
		setControl(content);
	}
	
	public void addListeners() {
		comboFunc.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RFunction rfnc = RFunctionLoader.getFunction(comboFunc.getText());
				((StackLayout)compSettings.getLayout()).topControl = rfnc.getConfigComposite();
				compSettings.layout();
				checkPageComplete();
			}
		});
	}
	
	public void performFinish() throws RException {
		RFunctionLoader.getFunction(comboFunc.getText()).run();
	}
	
	private void checkPageComplete() {
		setErrorMessage(null);
		setPageComplete(true);
	}
}
