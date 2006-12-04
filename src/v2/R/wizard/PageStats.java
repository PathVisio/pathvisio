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

package R.wizard;

import gmmlVision.GmmlVision;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import util.SwtUtils.SimpleRunnableWithProgress;
import R.RController;
import R.RFunctionLoader;
import R.RCommands.RException;
import R.RFunctionLoader.RFunction;

public class PageStats extends WizardPage {
	String resultVar;
	
	String function;
	Combo comboFunc;
	Composite compSettings;
	
	Text finishText;
	
	public PageStats() {
		super("PageStats");		
		setTitle("Apply a statistical function");
		setDescription("Choose a function to apply to the selected pathways and expression data");
		setPageComplete(false);
	}

	public String getResultVar() { return resultVar; }
	
	public void createControl(Composite parent) {
		
		Composite content = new Composite(parent, SWT.NULL);
		StackLayout topStack = new StackLayout();
		content.setLayout(topStack);
		

		Composite statsComp = new Composite(content, SWT.NULL);
		statsComp.setLayout(new GridLayout(2, false));
				
		Label flbl = new Label(statsComp, SWT.NULL);
		flbl.setText("Choose a function to apply:");
		comboFunc = new Combo(statsComp, SWT.BORDER | SWT.READ_ONLY);
		comboFunc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboFunc.setItems(RFunctionLoader.getFunctionNames());
		
		compSettings = new Composite(statsComp, SWT.NULL);
		GridData span2Cols = new GridData(GridData.FILL_BOTH);
		span2Cols.horizontalSpan = 2;
		compSettings.setLayoutData(span2Cols);
		
		compSettings.setLayout(new StackLayout());
		
		for(String fn : comboFunc.getItems()) {
			RFunctionLoader.getFunction(fn).getConfigComposite(compSettings);
		}
				
		//Composite for showing progress info
		Composite finishComp = new Composite(content, SWT.NULL);
		finishComp.setLayout(new FillLayout());
		finishText = new Text(finishComp, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		
		topStack.topControl = statsComp;
		
		addListeners();
		setControl(content);
	}
	
	public void init() {
		for(String fn : comboFunc.getItems()) {
			try { 
				RFunctionLoader.getFunction(fn).initValueComboItems(); 
			} catch(RException e) { GmmlVision.log.error("Unable to initialize values for function", e); }
		}
	}
	
	public void addListeners() {
		comboFunc.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				function = comboFunc.getText();
				RFunction rfnc = RFunctionLoader.getFunction(function);
				((StackLayout)compSettings.getLayout()).topControl = rfnc.getConfigComposite();
				compSettings.layout();
				checkPageComplete();
			}
		});
	}
	
	private void updateFinishText(final String txt) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				finishText.append(txt);
			}
		});	
	}
	
	private void doCancel() {
		RController.interruptRProcess();
	}
	
	public void showFinish() {
		doSetTopToParent(finishText);
	}
	
	public void showConfig() {
		doSetTopToParent(comboFunc);
	}
	
	protected void doSetTopToParent(final Control top) {
		GmmlVision.getWindow().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				((StackLayout)((Composite)getControl()).getLayout()).topControl = top.getParent();
				((Composite)getControl()).layout();
			}});
	}
	
	public void performFinish() throws RException {
		showFinish();
		
		Thread thr = new Thread() {
			volatile boolean stop = false;
			public void run() {
				try {
					while(!stop) {
						String rout = RController.getNewOutput();
						if(rout != null) updateFinishText(rout + "\n");
						
						try { sleep(250); } catch(InterruptedException e) { 
							return;
						}
						if(SimpleRunnableWithProgress.isCancelled()) {
							doCancel();
							return;
						}
					}
				} catch(Exception re) { re.printStackTrace(); }
			}
			public void interrupt() {
				stop = true;
			}
		};
		
		thr.start();
		RFunction fun = RFunctionLoader.getFunction(function);
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		SimpleDateFormat sdf =  new java.text.SimpleDateFormat("yyMMdd_HHmmss");
		resultVar = "result_" + fun.getVarName() + sdf.format(cal.getTime());
		fun.run(resultVar);
		thr.interrupt();
	}
		
	private void checkPageComplete() {
		setErrorMessage(null);
		setPageComplete(true);
	}
}
