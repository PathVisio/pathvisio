package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import preferences.GmmlPreferences;
import util.Utils;
import util.tableviewer.PathwayTable;
import R.RCommands.RException;
import R.RDataIn.ResultSet;

public class StatsResultTable extends PathwayTable {
	List<ResultSet> results;
	
	public StatsResultTable(Composite parent, int style) {
		super(parent, style);
		results = new ArrayList<ResultSet>();
	}
	
	protected void createContents() {
		setLayout(new GridLayout(1, false));
		
		createStatsComposite();

		Composite tableComposite = new Composite(this, SWT.NULL);
		tableComposite.setLayout(new FillLayout());
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		initTable(tableComposite);
	}
	
	Combo resultCombo;
	private Composite createStatsComposite() {
		Composite saveComp = new Composite(this, SWT.NULL);
		saveComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		saveComp.setLayout(new GridLayout(2, false));
		
		createResultCombo(saveComp);
		
		//Save as R file
		createSaveButton(saveComp);
		
		//Save as tab delimited text file
		//TODO: implement
		
		return saveComp;
	}

	private void createResultCombo(Composite parent) {
		Label comLabel = new Label(parent, SWT.NULL);
		comLabel.setText("Results: ");
		resultCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY);
		resultCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		resultCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ResultSet rs = results.get(resultCombo.getSelectionIndex());
				setTableData(rs);
			}
		});
	}
	
	private void createSaveButton(Composite parent) {
		Button saveAsR = new Button(parent, SWT.PUSH);
		saveAsR.setText("Save results");
		saveAsR.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
				fd.setFilterPath(GmmlVision.getPreferences().getString(GmmlPreferences.PREF_DIR_RDATA));
				String fn = fd.open();
				if(fn == null) return;
				try {
					saveResults(new File(fn));
				} catch(RException re) {
					MessageDialog.openError(getShell(), "Unable to save results", re.getMessage());
				}
			}
		});
	}
	
	public void setResults(List<ResultSet> results) {
		this.results = results;
		String[] resultNames = new String[results.size()];
		for(int i = 0; i < results.size(); i++) resultNames[i] = results.get(i).getName();
		resultCombo.setItems(resultNames);
		resultCombo.select(0);
		setTableData(results.get(0));
	}
	
	public void saveResults(File saveTo) throws RException {
		String[] resultVars = new String[results.size()];
		for(int i = 0; i < results.size(); i++) 
			resultVars[i] = results.get(i).getVarName();
		RCommands.eval("save(list = c(" + Utils.array2String(resultVars, "'", ",") + 
							"), file = '" + RCommands.fileToString(saveTo) + "')");
	}
	
}
