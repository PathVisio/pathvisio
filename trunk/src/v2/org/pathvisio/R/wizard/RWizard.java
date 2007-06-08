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
package org.pathvisio.R.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import org.pathvisio.gui.Engine;
import org.pathvisio.util.SwtUtils.SimpleRunnableWithProgress;
import org.pathvisio.R.RDataIn;
import org.pathvisio.R.RCommands.RException;
import org.pathvisio.R.RCommands.RObjectContainer;


public class RWizard extends Wizard {
	public static RObjectContainer usedRObjects;
	
	public RWizard() {
		super();
		
		setWindowTitle("Pathway statistics");
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		addPage(new PageData());
		addPage(new PageStats());
	}
	
	public boolean performFinish() {		
		PageStats ps = (PageStats)getPage("PageStats");
		ps.finishText.setText("");
		boolean ok = false;
		try {						
			SimpleRunnableWithProgress srwp = 
				new SimpleRunnableWithProgress(ps.getClass(), "performFinish", new Class[] { });
			srwp.setArgs(new Object[] { });
			srwp.setInstance(ps);
			SimpleRunnableWithProgress.setMonitorInfo("", IProgressMonitor.UNKNOWN);
			getContainer().run(true, true, srwp);
					
			RDataIn.displayResults(RDataIn.getResultSets(ps.getResultVar()), ps.function);
			ok = true;
			
		} catch(InvocationTargetException e) {
			if(e.getCause() instanceof InterruptedException) return true;
			MessageDialog.openError(getShell(), "Error while applying function", e.getCause().getMessage());
			Engine.log.error("Unable to perform pathway statistics", e);
		} catch(RException re) {
			MessageDialog.openError(getShell(), "Error while loading results", re.getMessage());
			Engine.log.error("Unable to perform pathway statistics", re);
		} catch(InterruptedException ie) {
			return true; //Closes the wizard (needed because R process is killed (at least in linux)
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(!ok) ps.showConfig();
		}
		return ok;
	}
		
	public static class RWizardDialog extends WizardDialog {
		public RWizardDialog(Shell parent, IWizard wizard) {
			super(parent, wizard);
		}
			
		protected void nextPressed() {
			IWizardPage page = getCurrentPage();
			if		(page instanceof PageData) {
				boolean ok = ((PageData)page).performFinish();
				if(!ok) return;
				((PageStats)getWizard().getNextPage(page)).init();
			}
			super.nextPressed();
		}
	}
}
