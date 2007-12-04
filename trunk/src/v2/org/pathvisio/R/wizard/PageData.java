// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.R.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pathvisio.R.RCommands;
import org.pathvisio.R.RDataIn;
import org.pathvisio.R.RDataOut;
import org.pathvisio.R.RCommands.RInterruptedException;
import org.pathvisio.data.GexManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.swt.SwtPreferences.SwtPreference;

public class PageData extends WizardPage {
	RDataOut rDataOut;
	RDataIn rDataIn;
	
	Text pwDir, exportFile, importFile, pwObj, exprObj;
	Button radioExport, radioImport, exportBrowse, importBrowse, pwBrowse;
	Composite settingsComp, importSettings, exportSettings;
	
	public PageData() {
		super("PageData");
		
		rDataOut = new RDataOut();
		rDataIn =  new RDataIn();
		
		setTitle("Export data to R");
		setDescription("Export expression and pathway data to R for statistical analysis or" +
				" load previously exported data");
		setPageComplete(false);
	}
	
	public void createControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.VERTICAL | SWT.HORIZONTAL);
		
		Composite content = new Composite(sc, SWT.NULL);
		content.setLayout(new GridLayout(1, false));
				
		sc.setContent(content);
		
		radioExport = new Button(content, SWT.RADIO);
		radioExport.setText("Export data to R");
		//Only available when expression data is loaded
		radioExport.setEnabled(GexManager.getCurrentGex().isConnected());
			
		radioImport = new Button(content, SWT.RADIO);
		radioImport.setText("Load previously exported data");
		
		settingsComp = new Composite(content, SWT.NULL);
		settingsComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		StackLayout settingsStack = new StackLayout();
		settingsComp.setLayout(settingsStack);
		
		exportSettings = createExportSettings(settingsComp);
		importSettings = createImportSettings(settingsComp);
		
		setListeners();
		setInitialValues();
		
		content.layout(true);
		sc.setMinSize(content.computeSize(300, SWT.DEFAULT, true));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		setControl(sc);
	}
	
	private void setInitialValues() {
		exprObj.setText(rDataOut.getDataSetName());
		pwObj.setText(rDataOut.getPathwaySetName());
		File pd = rDataOut.getPathwayDir();
		pwDir.setText(pd == null ? "" : pd.toString());
	}
	
	public Composite createExportSettings(Composite parent) {
		Composite settings = new Composite(parent, SWT.NULL);
		settings.setLayout(new GridLayout());
		
		GridData groupGrid = new GridData(GridData.FILL_HORIZONTAL);
		GridData span2Cols = new GridData(GridData.FILL_HORIZONTAL);
		span2Cols.horizontalSpan = 2;
		
		Group pwsGroup = new Group(settings, SWT.NONE);
		pwsGroup.setText("Export pathways");
		pwsGroup.setLayoutData(groupGrid);
		pwsGroup.setLayout(new GridLayout(3, false));
		{
			Label ol = new Label(pwsGroup, SWT.CENTER);
			ol.setText("Pathways variable name in R:");
			pwObj = new Text(pwsGroup, SWT.SINGLE | SWT.BORDER);
			pwObj.setLayoutData(span2Cols);
			
			Label dl = new Label(pwsGroup, SWT.CENTER);
			dl.setText("Include pathways in directory:");
			pwDir = new Text(pwsGroup, SWT.BORDER | SWT.SINGLE);
			pwDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			pwBrowse = new Button(pwsGroup, SWT.PUSH);
			pwBrowse.setText("Browse");
		}
		pwsGroup.layout(true);
		
		Group dataGroup = new Group(settings, SWT.NONE);
		dataGroup.setLayout(new GridLayout(3, false));
		dataGroup.setLayoutData(groupGrid);
		dataGroup.setText("Export expression data");
		{
			Label ol = new Label(dataGroup, SWT.CENTER);
			ol.setText("Dataset variable name in R:");
			exprObj = new Text(dataGroup, SWT.SINGLE | SWT.BORDER);
			exprObj.setLayoutData(span2Cols);
		}
		dataGroup.layout(true);
		
		Composite export = new Composite(settings, SWT.NULL);
		export.setLayoutData(groupGrid);
		export.setLayout(new GridLayout(3, false));
		
		Label exportLabel = new Label(export, SWT.CENTER);
		exportLabel.setText("Save as R data file: ");
		exportFile = new Text(export, SWT.SINGLE | SWT.BORDER);
		exportFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		exportBrowse = new Button(export, SWT.PUSH);
		exportBrowse.setText("Browse");

		return settings;
	}
	
	public Composite createImportSettings(Composite parent) {
		Composite settings = new Composite(parent, SWT.NULL);
		settings.setLayout(new GridLayout(3, false));
		Label lbl = new Label(settings, SWT.FLAT);
		lbl.setText("R data file:");
		importFile = new Text(settings, SWT.BORDER | SWT.SINGLE);
		importFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		importBrowse = new Button(settings, SWT.PUSH);
		importBrowse.setText("Browse");
		return settings;
	}
	
	private void setListeners() {
		
		pwDir.addModifyListener(textListener);
		pwObj.addModifyListener(textListener);
		exprObj.addModifyListener(textListener);
		exportFile.addModifyListener(textListener);
		importFile.addModifyListener(textListener);
		
		pwBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog fd = new DirectoryDialog(getShell());
				String pwTxt = pwDir.getText();
				fd.setFilterPath(pwTxt.equals("") ? 
						SwtPreference.SWT_DIR_PWFILES.getValue() : pwTxt);
				String dir = fd.open();
				if(dir != null) pwDir.setText(dir);
				checkPageComplete();
			}
		});

		exportBrowse.addSelectionListener(browseListener);
		importBrowse.addSelectionListener(browseListener);
		radioExport.addSelectionListener(radioListener);
		radioImport.addSelectionListener(radioListener);
	}
	
	SelectionAdapter browseListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			FileDialog fd = new FileDialog(getShell(), 
					e.widget == exportBrowse ? SWT.SAVE : SWT.OPEN);
			String expTxt = exportFile.getText();
			fd.setFilterPath(expTxt.equals("") ? SwtPreference.SWT_DIR_RDATA.getValue() : expTxt);
			String file = fd.open();
			if(file != null) {
				if		(e.widget == exportBrowse) 	exportFile.setText(file);
				else if	(e.widget == importBrowse) 	importFile.setText(file);
			}
			checkPageComplete();
		}
	};
	
	SelectionAdapter radioListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			((StackLayout)settingsComp.getLayout()).topControl = 
				radioExport.getSelection() ? exportSettings : importSettings;
			settingsComp.layout();
			checkPageComplete();
		}
	};
	
	ModifyListener textListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if		(e.widget == pwDir) 
				rDataOut.setPathwayDir(new File(pwDir.getText()));
			else if	(e.widget == exportFile)
				rDataOut.setExportFile(exportFile.getText());
			else if (e.widget == pwObj)
				rDataOut.setPathwaySetName(RCommands.format(pwObj.getText()));
			else if (e.widget == exprObj)
				rDataOut.setDataSetName(RCommands.format(exprObj.getText()));
			else if (e.widget == importFile)
				rDataIn.setRDataFile(importFile.getText());
			checkPageComplete();
		}
	};

	private void checkPageComplete() {
		try {
			if		(radioExport.getSelection()) rDataOut.checkValid();
			else if (radioImport.getSelection()) rDataIn.checkValid();
			else { setPageComplete(false); return; } //Nothing selected yet
		} catch(Exception e) {
			setErrorMessage(e.getMessage());
			setPageComplete(false);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
	}
	
	public void setPageComplete(boolean complete) {
		super.setPageComplete(complete);
	}
	
	public boolean performFinish() {
		try {
			if(radioExport.getSelection()) { //Export the data
				RWizard.usedRObjects = rDataOut.getUsedObjects();
				rDataOut.doExport();
			} else { //Load the data into R
				RWizard.usedRObjects = rDataIn.getUsedObjects();
				rDataIn.load();
			}
		} catch(Exception e) {
			if(e instanceof RInterruptedException) return false;
			
			String action = radioExport.getSelection() ? "exporting" : "loading";
			String msg = (e instanceof InvocationTargetException) ? e.getCause().getMessage() : e.getMessage();
			
			MessageDialog.openError(getShell(), "Error", "Unable to " + action + " data: " + msg);
			Logger.log.error("Unable to export to R", e);
			return false;
		}
		return true;
	}
}
