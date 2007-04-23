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
package org.pathvisio.search;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.pathvisio.gui.Engine;
import org.pathvisio.gui.MainWindow;
import org.pathvisio.preferences.Preferences;
import org.pathvisio.search.SearchMethods.SearchException;
import org.pathvisio.util.SwtUtils.SimpleRunnableWithProgress;
import org.pathvisio.data.DataSources;

public class PathwaySearchComposite extends Composite {

	HashMap<String, SearchOptionComposite> searchControls;
	
	MainWindow gmmlVision;
	
	public PathwaySearchComposite(Composite parent, int style, MainWindow gmmlVision) {
		super(parent, style);
		this.gmmlVision = gmmlVision;
				
		setLayout(new GridLayout());
		initSearchComposite();
		initSearchResultTable();
		
	}
	
	StackLayout pathwaySearchStack;
	SearchResultTable searchResultTable;
	
	private void initSearchComposite() {
		searchControls = new HashMap<String, SearchOptionComposite>();

		String[] soLabels = new String[] { "gene id", "gene symbol" };
		final HashMap<String, String> labelMappings = new HashMap<String, String>();
		labelMappings.put(soLabels[0], "pathwaysContainingGene");
		labelMappings.put(soLabels[1], "pathwaysContainingGeneSymbol");
		
		final Group group = new Group(this, SWT.SHADOW_ETCHED_IN);
		group.setText("Search");
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		group.setLayout(new GridLayout(2, false));
		Label label = new Label(group, SWT.CENTER);
		label.setText("Search by:");
		final Combo combo = new Combo(group, SWT.READ_ONLY);
		combo.setItems(soLabels);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridData span2cols = new GridData(GridData.FILL_HORIZONTAL);
		span2cols.horizontalSpan = 2;
		
		final Group sGroup = new Group(group, SWT.NULL);
		pathwaySearchStack = new StackLayout();
		sGroup.setLayout(pathwaySearchStack);
		sGroup.setLayoutData(span2cols);
				
		//Add search options composites to stacklayout
		final Composite[] searchOptionControls = new Composite[2];
		searchOptionControls[0] = pathwaysContainingGene(sGroup);
		searchOptionControls[1] = pathwaysContainingGeneSymbol(sGroup);
		
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selection = combo.getSelectionIndex();
				if(selection > -1)
					searchControls.get(labelMappings.get(combo.getText())).select();
				sGroup.layout();
			}
		});
		
		//Set initial selection
		//set symbol search as default
		pathwaySearchStack.topControl = searchOptionControls[1];
		combo.select(1);
	}
	
	private void initSearchResultTable() {
		Group group = new Group(this, SWT.SHADOW_ETCHED_IN);
		group.setText("Results");
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setLayout(new FillLayout());
		searchResultTable = new SearchResultTable(group, SWT.NULL);
		
	}
	
	public Composite pathwaysContainingGeneSymbol(Composite parent) {
		SearchOptionComposite comp = new SearchOptionComposite(parent, SWT.NULL)
		{
			void select() {
				pathwaySearchStack.topControl = this;
				pack();
			}
			
			public Composite createContents(Composite parent) {
				setLayout(new GridLayout(3, false));
				
				GridData span2cols = new GridData(GridData.FILL_HORIZONTAL);
				span2cols.horizontalSpan = 2;
				
				Label symLabel = new Label(parent, SWT.CENTER);
				symLabel.setText("Gene symbol:");
				final Text symText = new Text(parent, SWT.SINGLE | SWT.BORDER);
				symText.setLayoutData(span2cols);
								
				Label dirLabel = new Label(parent, SWT.CENTER);
				dirLabel.setText("Directory to search:");
				
				final Text dirText = createDirText(parent);
				createDirButton(parent, dirText);
				
				Button searchButton = new Button(parent, SWT.PUSH);
				searchButton.setText("Search");
				GridData span3cols = new GridData(GridData.HORIZONTAL_ALIGN_END);
				span3cols.horizontalSpan = 3;
				searchButton.setLayoutData(span3cols);
				searchButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						String id = symText.getText();
						String folder = dirText.getText();
						if(id.equals("") || folder.equals("")) {
							MessageDialog.openError(getShell(), "error", "please specify id and pathway folder"); 
							return;
						}
						
						SearchRunnableWithProgress srwp = new SearchRunnableWithProgress(
								"pathwaysContainingGeneSymbol", 
								new Class[] { String.class, File.class, 
										SearchResultTable.class, SearchRunnableWithProgress.class });
						srwp.setArgs(new Object[] {id, new File(folder), searchResultTable, srwp });
						ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
						try { dialog.run(true, true, srwp); } catch(Exception ex) 
						{ 
							MessageDialog.openError(getShell(), "Error", "Unable to perform search: " + ex.getMessage());
							return;
						}
					}
				});
				
				//Add controls to hash to enable preset values
				name2Control.put("idText", symText);
				name2Control.put("dirText", dirText);
				return parent;
			}
		};
		searchControls.put("pathwaysContainingGeneSymbol", comp); //Add to available search options
		return comp;
	}
	
	public Composite pathwaysContainingGene(Composite parent) {
		SearchOptionComposite comp = new SearchOptionComposite(parent, SWT.NULL)
		{
			void select() {
				pathwaySearchStack.topControl = this;
			}
			
			public Composite createContents(Composite parent) {
				setLayout(new GridLayout(3, false));
				
				GridData span2cols = new GridData(GridData.FILL_HORIZONTAL);
				span2cols.horizontalSpan = 2;
				
				Label idLabel = new Label(parent, SWT.CENTER);
				idLabel.setText("Gene id:");
				final Text idText = new Text(parent, SWT.SINGLE | SWT.BORDER);
				idText.setLayoutData(span2cols);
				
				Label systemLabel = new Label(parent, SWT.CENTER);
				systemLabel.setText("Id system:");
				final Combo systemCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY);
				systemCombo.setItems(DataSources.dataSources);
				systemCombo.setLayoutData(span2cols);
				
				Label dirLabel = new Label(parent, SWT.CENTER);
				dirLabel.setText("Directory to search:");
				
				final Text dirText = createDirText(parent);
				createDirButton(parent, dirText);
				
				Button searchButton = new Button(parent, SWT.PUSH);
				searchButton.setText("Search");
				GridData span3cols = new GridData(GridData.HORIZONTAL_ALIGN_END);
				span3cols.horizontalSpan = 3;
				searchButton.setLayoutData(span3cols);
				searchButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						String id = idText.getText();
						int codeIndex = systemCombo.getSelectionIndex();
						String code =  codeIndex == -1 ? "" : DataSources.systemCodes[codeIndex];
						String folder = dirText.getText();
						if(id.equals("") || code.equals("") || folder.equals("")) {
							MessageDialog.openError(getShell(), "error", "please specify id, code and pathway folder"); 
							return;
						}
						
						SearchRunnableWithProgress srwp = new SearchRunnableWithProgress(
								"pathwaysContainingGeneID", 
								new Class[] { String.class, String.class, File.class, 
										SearchResultTable.class, SearchRunnableWithProgress.class });
						SearchRunnableWithProgress.setMonitorInfo("Searching", (int)SearchMethods.TOTAL_WORK);
						srwp.setArgs(new Object[] {id, code, new File(folder), searchResultTable, srwp });
						ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
						try { dialog.run(true, true, srwp); } catch(Exception ex) 
						{ 
							MessageDialog.openError(getShell(), "Error", "Unable to perform search: " + ex.getMessage());
							return;
						}
					}
				});
				
				//Add controls to hash to enable preset values
				name2Control.put("idText", idText);
				name2Control.put("systemCombo", systemCombo);
				name2Control.put("dirText", dirText);
				return parent;
			}
		};
		searchControls.put("pathwaysContainingGene", comp); //Add to available search options
		return comp;
	}
	
	private Text createDirText(Composite parent) {
		Text t = new Text(parent, SWT.SINGLE | SWT.BORDER);
		t.setText(Engine.getPreferences().getString(Preferences.PREF_DIR_PWFILES));
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return t;
	}
	
	private Button createDirButton(Composite parent, final Text dirText) {
		Button b = new Button(parent, SWT.PUSH);
		b.setText("Browse");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd = new DirectoryDialog(getShell());
				dd.setFilterPath(dirText.getText());
				String dirName = dd.open();
				if(dirName != null) dirText.setText(dirName);
			}
		});
		return b;
	}
	
	public class SearchRunnableWithProgress extends SimpleRunnableWithProgress {

		public SearchRunnableWithProgress(String method, Class[] parameters) {
			super(SearchMethods.class, method, parameters);
		}
		
		public void run(IProgressMonitor monitor) {
			try {
				super.run(monitor);
			} catch (InterruptedException e) {
				openMessageDialog("error", e.getMessage());
				Engine.log.error("Unable to start search", e);
			} catch (InvocationTargetException e) {
				if(e.getCause() instanceof SearchException)
					openMessageDialog("", e.getCause().getMessage());
				else {
					openMessageDialog("error", "Cause: " + e.getCause().getMessage());
					Engine.log.error("while searching", e);
				}
			}
		}
		
	}
	
	public abstract class SearchOptionComposite extends Composite {
		HashMap<String, Control> name2Control;
		
		public SearchOptionComposite(Composite parent, int style) {
			super(parent, style);
			name2Control = new HashMap<String,Control>();
			createContents(this);
		}
		
		public void setContents(HashMap<String, String> name2Value) {
			for(String key : name2Control.keySet()) {
				if(name2Value.containsKey(key)) {
					Control c = name2Control.get(key);
					if(c instanceof Text) 		((Text)c).setText(name2Value.get(key));
					else if(c instanceof Combo) ((Combo)c).setText(name2Value.get(key));
				}
			}
		}
		
		abstract void select();
		public abstract Composite createContents(Composite parent);
	}
}
