package R.wizard;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rosuda.JRI.REXP;

import R.RCommands;
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
			} catch(Exception e) { e.printStackTrace(); }
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
		
		String[] names = RCommands.ls("function");
		for(String name : names) {
			String[] args = RCommands.eval("names(formals(" + name + "))", true).asStringArray();
			functions.add(new RFunction(name, args));
		}
		return functions;
	}
	
	public static class RFunction {
		String name;
		String[] args;
		HashMap<String, String> argValues;
		Composite configComp;
		
		public RFunction(String name, String[] args) throws RException {
			this.name = name;
			this.args = args;
			argValues = new HashMap<String, String>();
		}
		
		public String[] getArgs() { return args; }
		
		public String getName() { return name; }
		
		public Composite getConfigComposite(Composite parent) {
			if(configComp != null && !configComp.isDisposed()) return configComp;
			
			configComp = new Composite(parent, SWT.NULL);
			configComp.setLayout(new GridLayout());
			
			Label nmLabel = new Label(configComp, SWT.FLAT);
			nmLabel.setText("Arguments for " + name + " function");
			
			Composite argC = new Composite(configComp, SWT.NULL);
			argC.setLayout(new GridLayout(2, false));
			argC.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridData textGrid = new GridData(GridData.FILL_HORIZONTAL);
			for(String arg : args) {
				final Label argL = new Label(argC, SWT.FLAT);
				argL.setText(arg);
				final Text argT = new Text(argC, SWT.SINGLE | SWT.BORDER);
				argT.setLayoutData(textGrid);
				argT.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						argValues.put(argL.getText(), argT.getText());
					}
				});
			}
			return configComp;
		}
		
		public Composite getConfigComposite() { return configComp; }
		
		public void run() throws RException {
			REXP res = RCommands.eval(getRunCommand(), true);
			System.out.println(res.asStringArray());
		}
		
		public String getRunCommand() {
			StringBuilder runStr = new StringBuilder(name + "(");
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
