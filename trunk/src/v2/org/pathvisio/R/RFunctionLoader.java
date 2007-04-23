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
package org.pathvisio.R;

import org.pathvisio.gui.Engine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.pathvisio.util.JarUtils;
import org.pathvisio.util.Utils;
import org.pathvisio.util.SwtUtils.SimpleRunnableWithProgress;
import org.pathvisio.visualization.colorset.Criterion;
import org.pathvisio.visualization.colorset.CriterionComposite;
import org.pathvisio.R.RCommands.RException;
import org.pathvisio.R.wizard.RWizard;


public class RFunctionLoader {
	static final String FUN_DIR = "R/functions";
	static final String FUN_CLASS = "VisioFunction";
	
	static final HashMap<String, RFunction> functions = new HashMap<String, RFunction>();
	
	
	public static void loadFunctions() throws IOException, RException {
		URL url = Engine.getResourceURL(FUN_DIR);
		
		String protocol = url.getProtocol();
		if(protocol.equals("jar")) {
			JarURLConnection conn = (JarURLConnection)url.openConnection();

			JarFile jfile = conn.getJarFile();
			Enumeration e = jfile.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)e.nextElement();
				String entryname = entry.getName();
				if (	entryname.startsWith(FUN_DIR) && 
						(entryname.endsWith(".R") || entryname.endsWith(".r"))) {
					File tmp = JarUtils.resourceToNamedTempFile(entryname, new File(entryname).getName());
					loadFunction(tmp);
				}
			}
		} else if(protocol.equals("file")) {
			File dir = new File(url.getFile());
			File[] rfiles = dir.listFiles(new FilenameFilter() {
				public boolean accept(File f, String name) {
					if(name.endsWith("R") || name.endsWith("r")) return true;
					return false;
				}
				
			});
			for(File f : rfiles) loadFunction(f);
		} else throw new IOException("Unable to find R functions for statistical tests");
		initFunctions();
	}
	
	private static void loadFunction(File funFile) {
    	try {
    		Engine.log.trace("Loading R function: " + funFile);
    		RCommands.eval("source('" + RCommands.fileToString(funFile) + "')");
    	} catch(RException re) {
    		RController.openError("Unable to load functions in " + funFile.toString(), re);
    	}
	}
	
	public static String[] getFunctionNames() {
		List<String> fns = new ArrayList<String>(functions.keySet());
		Collections.sort(fns);
		return fns.toArray(new String[fns.size()]);
	}
	
	public static RFunction getFunction(String name) {
		return functions.get(name);
	}
		
	public static void initFunctions() throws RException {		
		String[] names = RCommands.ls(FUN_CLASS);
		for(String name : names) {
			if(name.equals("zscore")) functions.put(name, new ZScore(name)); //Think of something to load the RFunction subclass (if exitst) for a function
			else functions.put(name, new RFunction(name));
		}
	}
	
	public static List<RFunction> getFunctions() {
		return new ArrayList<RFunction>(functions.values());
	}
	
	public static class RFunction {
		String varName, name, description;
		String[] args, argDefaults, argDescriptions, argClasses;
		HashMap<String, String> argValues;
		Composite configComp;
		Combo[] valueCombos;
	
		
		public RFunction(String varName) throws RException {
			this.varName = varName;
			argValues = new HashMap<String, String>();
			
			name = RCommands.eval("name(" + varName + ")", true).asString();
			description = RCommands.eval("description(" + varName + ")", true).asString();
			args = RCommands.eval("getArgs(" + varName + ")", true).asStringArray();
			argDefaults = RCommands.eval("getDefaults(" + varName + ")", true).asStringArray();
			argDescriptions = RCommands.eval("arg_descr(" + varName + ")" , true).asStringArray();
			argClasses = RCommands.eval("arg_class(" + varName + ")", true).asStringArray();
			
			valueCombos = new Combo[args.length];
		}
		
		public String[] getArgs() { return args; }
		public String[] getArgDefaults() { return argDefaults; }
		public String[] getArgDescriptions() { return argDescriptions; }
		public String[] getArgClasses() { return argClasses; }
		public String getName() { return name; }
		public String getDescription() { return description; }
		public String getVarName() { return varName; }
		
		protected Composite getArgsComposite(Composite parent) {			
			Composite argComp = new Composite(parent, SWT.NULL);
			argComp.setLayout(new GridLayout());
			
			Label nmLabel = new Label(argComp, SWT.FLAT | SWT.WRAP);
			nmLabel.setText(name + ":\n" + description);
			Group argC = new Group(argComp, SWT.NULL);
			argC.setText("Arguments");
			argC.setLayout(new GridLayout());
			argC.setLayoutData(new GridData(GridData.FILL_BOTH));
			for(int i = 0; i < args.length; i++) setArgGroup(i, argC);
			return argComp;
		}
		
		protected void updateArgGroup(String arg) throws RException {
			int ai = -1;
			for(int i = 0; i < args.length; i++) 
				if(args[i].equals(arg)) { ai = i; break; }
			updateItem(ai);
		}
		
		public Composite getConfigComposite(Composite parent) {
			if(configComp != null && !configComp.isDisposed()) configComp.dispose();
						
			configComp = getArgsComposite(parent);

			return configComp;
		}
		
		protected void setArgGroup(final int index, Composite parent) {
			Group argG = createArgGroup(parent);
			//Description
			createArgDescr(index, argG);
			//Combo box for value
			createArgCombo(index, argG);
		}
		
		protected Group createArgGroup(Composite parent) {
			Group argG = new Group(parent, SWT.NULL);
			argG.setLayout(new GridLayout(1, false));
			argG.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			return argG;
		}
		
		protected Composite createArgDescr(final int index, Composite parent) {
			Composite descrComp = new Composite(parent, SWT.NULL);
			descrComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			descrComp.setLayout(new GridLayout(2, false));
			Label descr = new Label(descrComp, SWT.NULL);
			descr.setText(args[index] + ": ");
			Label argDescr = new Label(descrComp, SWT.FLAT | SWT.WRAP);
			argDescr.setText(argDescriptions[index]);
			argDescr.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			return descrComp;
		}
		
		protected Combo createArgCombo(final int index, Composite parent) {
			final Combo argT = new Combo(parent, SWT.SINGLE | SWT.BORDER);
			argT.setText(argDefaults[index]);
			argT.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			argT.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					argValues.put(args[index], argT.getText());
				}
			});
			valueCombos[index] = argT;
			return argT;
		}
		
		public void initValueComboItems() throws RException {
			for(int i = 0; i < args.length; i++) {
				updateItem(i);
			}
		}
		
		protected void updateItem(int i) throws RException {
			valueCombos[i].setItems(RCommands.ls(argClasses[i]));
			if(!argDefaults[i].equals("")) valueCombos[i].setText(argDefaults[i]);
			else valueCombos[i].select(0);
		}
		
		public Composite getConfigComposite() { return configComp; }
		
		public void run(String resultVar) throws RException {
			RCommands.eval(getRunCommand(resultVar));			
		}
		
		public String getRunCommand(String resultVar) {
			StringBuilder runStr = new StringBuilder(resultVar + "=" + varName + "(");
			for(String arg : args) {
				String value = argValues.get(arg);
				if(value == null || value.equals("")) continue;
				runStr.append(arg + "=" + value + ",");
			}
			int comma = runStr.lastIndexOf(",");
			if(comma > -1) runStr.replace(comma, runStr.length(), "");
			runStr.append(")");
			return runStr.toString();
		}
	}
	
	//This class will later be stored in a jar together with R file...
	public static class ZScore extends RFunction {
		static final String SETS_ARG = "sets";
		
		public ZScore(String varName) throws RException {
			super(varName);
		}
			
		org.eclipse.swt.widgets.List setList;
		protected void setArgGroup(final int i, Composite parent) {
			if(args[i].equals(SETS_ARG)) {
				Group argG = createArgGroup(parent);
				argG.setLayoutData(new GridData(GridData.FILL_BOTH));
				createArgDescr(i, argG);
				
				//List instead of combo
				Composite listComp = new Composite(argG, SWT.NULL);
				listComp.setLayoutData(new GridData(GridData.FILL_BOTH));
				listComp.setLayout(new GridLayout(2, false));
				setList = new org.eclipse.swt.widgets.List(listComp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
				setList.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						putSetValues();
					}
				});
				setList.setLayoutData(new GridData(GridData.FILL_BOTH));
				//Buttons to add/remove sets
				createSetButtons(listComp);
				
			} else super.setArgGroup(i, parent);
		}
		
		private void putSetValues() {
			String cmd = "cbind(" + Utils.array2String(setList.getSelection(), "", ",") + ")";
			argValues.put(SETS_ARG, cmd);
		}
		
		private void createSetButtons(Composite parent) {
			Composite buttonComp = new Composite(parent, SWT.NULL);
			buttonComp.setLayout(new RowLayout(SWT.VERTICAL));
			Button setButton = new Button(buttonComp, SWT.PUSH);
			setButton.setText("Add a set");
			setButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						new SetConfigDialog(Display.getCurrent().getActiveShell()).open();
						updateArgGroup(SETS_ARG);
					} catch(Exception ex) {
						MessageDialog.openError(e.display.getActiveShell(), "Error", ex.getMessage());
						Engine.log.error("", ex);
					}
				}
			});
			Button removeButton = new Button(buttonComp, SWT.PUSH);
			removeButton.setText("Remove selected sets");
			removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						for(String setName : setList.getSelection()) {
							RWizard.usedRObjects.removeObject(setName, true);
						}
						RWizard.usedRObjects.save();
						updateArgGroup(SETS_ARG);
					} catch(Exception ex) {
						MessageDialog.openError(e.display.getActiveShell(), "Error", ex.getMessage());
						Engine.log.error("", ex);
					}
				}
			});
		}
		
		protected void updateItem(int i) throws RException {
			if(args[i].equals(SETS_ARG)) {
				setList.setItems(RCommands.ls(argClasses[i]));
				setList.selectAll();
				putSetValues();
			} else super.updateItem(i);
		}
		
		public class SetConfigDialog extends ApplicationWindow {
			String[] dataSets;
			Combo dataSetCombo;
			HashMap<String, String[]> availableSymbols;
			Criterion criterion;
			String setVar;
			
			public SetConfigDialog(Shell shell) throws Exception {
				super(shell);
				setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
				setBlockOnOpen(true);
				availableSymbols = new HashMap<String, String[]>();
				criterion = new Criterion();
				dataSets = RDataIn.listDataSets();
				if(dataSets == null || dataSets.length == 0)
					throw new Exception("No objects of class DataSet available in R");
				for(String ds : dataSets) availableSymbols.put(ds, RCommands.colnames(ds));
				
			}
			
			private boolean saveSet() {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				SimpleRunnableWithProgress rwp = new SimpleRunnableWithProgress(
						this.getClass(), "doSaveSet", new Class[] { }, new Object[] { }, this);
				SimpleRunnableWithProgress.setMonitorInfo(
						"Exporting set to R", IProgressMonitor.UNKNOWN);
				rwp.setRunAsSyncExec(true);
				try {
					dialog.run(true, true, rwp); 
				} catch(InvocationTargetException ex) {
					MessageDialog.openError(getShell(), "Unable to export set", ex.getCause().getMessage());
					Engine.log.error("Unable to export set to R", ex);
					return false;
				} catch(InterruptedException ie) { return false; }
				return true;
			}
			
			public void doSaveSet() throws Exception {
				if(setVar == null || setVar.equals("")) 
					throw new Exception("No R variable name specified");
				setVar = RCommands.format(setVar);
				RDataOut.createSetVector(criterion, dataSetCombo.getText(), setVar);
				RWizard.usedRObjects.addObject(setVar);
				RWizard.usedRObjects.save();
			}
			
			protected Control createContents(Composite parent) {
				Composite contents = new Composite(parent, SWT.NULL);
				contents.setLayout(new GridLayout(2, false));
				
				Label dataSetLabel = new Label(contents, SWT.NULL);
				dataSetLabel.setText("Apply to data set:");
				
				dataSetCombo = new Combo(contents, SWT.SINGLE | SWT.READ_ONLY);
				dataSetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				dataSetCombo.setItems(dataSets);
				dataSetCombo.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						criterion.getConfigComposite().setAvailableSymbols
						(availableSymbols.get(dataSetCombo.getText()));
					}
				});
							
				final CriterionComposite critConf = criterion.createConfigComposite(contents);
				GridData critGrid = new GridData(GridData.FILL_BOTH);
				critGrid.horizontalSpan = 2;
				critConf.setLayoutData(critGrid);
									
				GridData span2cols = new GridData(GridData.FILL_HORIZONTAL);
				span2cols.horizontalSpan = 2;
				
				Composite saveComp = new Composite(contents, SWT.NULL);
				saveComp.setLayoutData(span2cols);
				saveComp.setLayout(new GridLayout(3, false));
				
				Label varLabel = new Label(saveComp, SWT.CENTER);
				varLabel.setText("Variable name in R:");
				
				final Text setVarText = new Text(saveComp, SWT.SINGLE | SWT.BORDER);
				setVarText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				setVarText.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						setVar = RCommands.format(setVarText.getText());
					}
				});
				
				Button saveCrit = new Button(saveComp, SWT.PUSH);
				saveCrit.setText("Export set to R");
				saveCrit.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						saveSet();
					}
				});
				
				GridData okGrid = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
				okGrid.horizontalSpan = 2;
				
				Composite okComp = new Composite(contents, SWT.NULL);
				okComp.setLayoutData(okGrid);
				okComp.setLayout(new GridLayout(2, false));
				
				Button ok = new Button(okComp, SWT.PUSH);
				ok.setText(" Ok ");
				ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				ok.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						boolean exists = false;
						try {
							exists = RCommands.exists(setVar);
						} catch(RException re) {}
						
						if(exists) close(); //Prevent the set from beeing exported twice
						else if (saveSet()) close();
					}
				});
				ok.setFocus();
				
				Button cancel = new Button(okComp, SWT.PUSH);
				cancel.setText("  Cancel  ");
				cancel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				cancel.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						close();
					}
				});
				
				dataSetCombo.select(0);
				return contents;
			}
		}
	}
}
