// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.R;

import org.pathvisio.gui.Engine;

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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import org.pathvisio.preferences.Preferences;
import org.pathvisio.util.Utils;
import org.pathvisio.util.tableviewer.PathwayTable;
import org.pathvisio.R.RCommands.RException;
import org.pathvisio.R.RDataIn.ResultSet;

public class StatsResultTable extends PathwayTable {
	List<ResultSet> results;
	
	public StatsResultTable(Composite parent, int style) {
		super(parent, style);
		results = new ArrayList<ResultSet>();
	}
	
	protected void createContents() {
		setLayout(new GridLayout(1, false));
		
		createStatsComposite();
		
		createGlobalsComposite();
		
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
	
	Label globalNames;
	Label globalValues;
	private Composite createGlobalsComposite() {
		Group globComp = new Group(this, SWT.NULL);
		globComp.setLayout(new RowLayout());
		globalNames = new Label(globComp, SWT.NULL);
		globalValues = new Label(globComp, SWT.NULL);
		return globComp;
	}

	void setGlobals(ResultSet rs) {
		String[] names = rs.getGlobalNames();
		String[] values = rs.getGlobalValues();
		String nmText = "";
		String vText = "";
		if(names != null) for(String n : names) nmText += n + ":\n";
		if(values != null) for(String v : values) vText += v + "\n";
		globalNames.setText(nmText);
		globalValues.setText(vText);
		layout();
	}
	
	private void createResultCombo(Composite parent) {
		Label comLabel = new Label(parent, SWT.NULL);
		comLabel.setText("Results: ");
		resultCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY);
		resultCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		resultCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ResultSet rs = results.get(resultCombo.getSelectionIndex());
				setInput(rs);
			}
		});
	}
	
	public void setInput(ResultSet rs) {
		setTableData(rs);
		setGlobals(rs);
	}
	
	private void createSaveButton(Composite parent) {
		Button saveAsR = new Button(parent, SWT.PUSH);
		saveAsR.setText("Save results");
		saveAsR.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
				fd.setFilterPath(Engine.getPreferences().getString(Preferences.PREF_DIR_RDATA));
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
		setInput(results.get(0));
	}
	
	public void saveResults(File saveTo) throws RException {
		String[] resultVars = new String[results.size()];
		for(int i = 0; i < results.size(); i++) 
			resultVars[i] = results.get(i).getVarName();
		RCommands.eval("save(list = c(" + Utils.array2String(resultVars, "'", ",") + 
							"), file = '" + RCommands.fileToString(saveTo) + "')");
	}
	
}
