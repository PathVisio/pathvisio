package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rosuda.JRI.REXP;

import colorSet.Criterion;
import colorSet.CriterionComposite;

import R.RCommands.RException;


public class RFunctionLoader {
	static final String funDir = "RFunctions";
	
	static final HashMap<String, RFunction> functions = new HashMap<String, RFunction>();
	
	
	public static void loadFunctions() {
		//Look for functions in directory functions
		for(File f : new File(funDir).listFiles(new FilenameFilter() {
			public boolean accept(File f, String name) {
				return	name.endsWith(".R") ||
						name.endsWith(".r");
			}})) {
			try {
				for(RFunction rf : getFunctions(f.toString()))
					functions.put(rf.getName(), rf);
			} catch(RException e) { 
				MessageDialog.openError(Display.getCurrent().getActiveShell(), 
						"Unable to load function", e.getMessage());
			}
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
	
	public static List<RFunction> getFunctions(String rFileName) throws RException {
		RCommands.eval("source('" + rFileName + "')");
		List<RFunction> functions = new ArrayList<RFunction>();
		
		String[] names = RCommands.ls("GmmlFunction");
		for(String name : names) {
			if(name.equals("zscore")) functions.add(new ZScore(name)); //Think of something to load the RFunction subclass (if exitst) for a function
			else functions.add(new RFunction(name));
		}
		return functions;
	}
	
	public static class RFunction {
		String varName;
		String name;
		String description;
		String[] args;
		String[] argDefaults;
		String[] argDescriptions;
		String[] argClasses;
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
			updateValueComboItems(ai);
		}
		
		public Composite getConfigComposite(Composite parent) {
			if(configComp != null && !configComp.isDisposed()) configComp.dispose();
			
			configComp = getArgsComposite(parent);
			
			return configComp;
		}
		
		private void setArgGroup(final int index, Composite parent) {
			Group argG = new Group(parent, SWT.NULL);
			argG.setLayout(new GridLayout(2, false));
			argG.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			//Description
			Label descr = new Label(argG, SWT.NULL);
			descr.setText(args[index] + ": ");
			Label argDescr = new Label(argG, SWT.FLAT | SWT.WRAP);
			argDescr.setText(argDescriptions[index]);
			argDescr.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			//Combo box for value
			final Combo argT = new Combo(argG, SWT.SINGLE | SWT.BORDER);
			argT.setText(argDefaults[index]);
			GridData comboGrid = new GridData(GridData.FILL_HORIZONTAL);
			comboGrid.horizontalSpan = 2;
			argT.setLayoutData(comboGrid);
			argT.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					argValues.put(args[index], argT.getText());
				}
			});
			valueCombos[index] = argT;
		}
		
		public void initValueComboItems() throws RException {
			for(int i = 0; i < args.length; i++) {
				updateValueComboItems(i);
			}
		}
		
		protected void updateValueComboItems(int i) throws RException {
			valueCombos[i].setItems(RCommands.ls(argClasses[i]));
			if(!argDefaults[i].equals("")) valueCombos[i].setText(argDefaults[i]);
			else valueCombos[i].select(0);
		}
		
		public Composite getConfigComposite() { return configComp; }
		
		public void run(String resultVar) throws RException {
			REXP res = RCommands.eval(getRunCommand(resultVar), true);
			System.out.println(res.asStringArray());
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
		
		public ZScore(String varName) throws RException {
			super(varName);
		}
		
		public Composite getConfigComposite(Composite parent) {
			if(configComp != null && !configComp.isDisposed()) configComp.dispose();
			
			configComp = new Composite(parent, SWT.NULL);
			configComp.setLayout(new GridLayout());
			getArgsComposite(configComp);
			Button setButton = new Button(configComp, SWT.PUSH);
			setButton.setText("Add a set");
			setButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						new SetConfigDialog(Display.getCurrent().getActiveShell()).open();
						updateArgGroup("set");
					} catch(Exception ex) {
						MessageDialog.openError(e.display.getActiveShell(), "Error", ex.getMessage());
						GmmlVision.log.error("", ex);
					}
				}
			});
			
			return configComp;
		}
				
		private class SetConfigDialog extends ApplicationWindow {
			String[] dataSets;
			Combo dataSetCombo;
			HashMap<String, String[]> availableSymbols;
			Criterion criterion;
			String setVar;
			
			public SetConfigDialog(Shell shell) throws Exception {
				super(shell);
				setShellStyle(SWT.RESIZE);
				setBlockOnOpen(true);
				availableSymbols = new HashMap<String, String[]>();
				criterion = new Criterion();
				dataSets = RDataIn.getDataSets();
				if(dataSets == null || dataSets.length == 0)
					throw new Exception("No objects of class DataSet available in R");
				for(String ds : dataSets) availableSymbols.put(ds, RCommands.colnames(ds));
				
			}
			
			private void saveSet() {
				try {
					if(setVar.equals("")) throw new Error("No R variable name specified");
					criterion.getConfigComposite().saveToCriterion();
					RDataOut.createSetVector(criterion, dataSetCombo.getText(), setVar);
				} catch(Exception ex) {
					MessageDialog.openError(getShell(), "Unable to save set", ex.getMessage());
					GmmlVision.log.error("Unable to save set to R", ex);
				}
			}

			protected Control createContents(Composite parent) {
				Composite contents = new Composite(parent, SWT.NULL);
				contents.setLayout(new GridLayout());
				
				dataSetCombo = new Combo(contents, SWT.SINGLE | SWT.READ_ONLY);
				dataSetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				dataSetCombo.setItems(dataSets);
				dataSetCombo.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						criterion.getConfigComposite().setAvailableSymbols
						(availableSymbols.get(dataSetCombo.getText()));
					}
				});
							
				final CriterionComposite critConf = criterion.getConfigComposite(contents);
				critConf.setLayoutData(new GridData(GridData.FILL_BOTH));
												
				Composite saveComp = new Composite(contents, SWT.NULL);
				saveComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				saveComp.setLayout(new GridLayout(3, false));
				
				Label varLabel = new Label(saveComp, SWT.CENTER);
				varLabel.setText("Variable name in R:");
				
				final Text setVarText = new Text(saveComp, SWT.SINGLE | SWT.BORDER);
				setVarText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				setVarText.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						setVar = setVarText.getText();
					}
				});
				
				Button saveCrit = new Button(saveComp, SWT.PUSH);
				saveCrit.setText("Save set to R");
				saveCrit.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						saveSet();
					}
				});
				
				Button ok = new Button(contents, SWT.PUSH);
				ok.setText("  Ok  ");
				ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				ok.addSelectionListener(new SelectionAdapter() {
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
