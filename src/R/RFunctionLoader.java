package R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.rosuda.JRI.REXP;

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
			functions.add(new RFunction(name));
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
		
		public Composite getConfigComposite(Composite parent) {
			if(configComp != null && !configComp.isDisposed()) configComp.dispose();
			
			configComp = new Composite(parent, SWT.NULL);
			configComp.setLayout(new GridLayout());
			
			Label nmLabel = new Label(configComp, SWT.FLAT | SWT.WRAP);
			nmLabel.setText(name + ":\n" + description);
			Group argC = new Group(configComp, SWT.NULL);
			argC.setText("Arguments");
			argC.setLayout(new GridLayout());
			argC.setLayoutData(new GridData(GridData.FILL_BOTH));
			for(int i = 0; i < args.length; i++) setArgGroup(i, argC);
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
				valueCombos[i].setItems(RCommands.ls(argClasses[i]));
				if(!argDefaults[i].equals("")) valueCombos[i].setText(argDefaults[i]);
				else valueCombos[i].select(0);
			}
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
}
