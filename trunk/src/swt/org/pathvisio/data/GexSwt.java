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
package org.pathvisio.data;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.data.GexImportWizard.ImportPage;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.swt.SwtProgressKeeper;
import org.pathvisio.model.Xref;

public class GexSwt {
	
	public static DBConnectorSwt getDBConnector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return SwtEngine.getCurrent().getSwtDbConnector(DBConnectorSwt.TYPE_GEX);
	}
	
	public static class ProgressWizardDialog extends WizardDialog {
		ProgressKeeper progress;
		public ProgressWizardDialog(Shell shell, IWizard wizard) {
			super(shell, wizard);
		}
		
		public void run(boolean fork, boolean cancellable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			//Add another selection listener to cancel ProgressKeeper
			//Overriding cancelPressed() doesn't work when using progress monitor
			getButton(WizardDialog.CANCEL).addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(progress != null) progress.cancel();
				}
			});
			if(runnable instanceof ProgressKeeper) {
				progress = (ProgressKeeper)runnable;
			}
			super.run(fork, cancellable, runnable);
		}
	}
	
	public static class CacheProgressKeeper extends SwtProgressKeeper implements IRunnableWithProgress {
		List<Xref> refs;
		
		public CacheProgressKeeper(List<Xref> refs) 
		{
			super(refs.size());
			this.refs = refs;
		}
		
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			super.run(monitor);
			monitor.beginTask("Loading data", getTotalWork());
			Gex.cacheData(refs, this);
		}
	}
	
	public static class ImportProgressKeeper extends SwtProgressKeeper {
		ImportPage page;
		ImportInformation info;
		
		public ImportProgressKeeper(ImportPage page, ImportInformation info) {
			super((int)1E6);
			this.page = page;
			this.info = info;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			super.run(monitor);
			monitor.beginTask("Importing data", getTotalWork());
			GexTxtImporter.importFromTxt(info, this);
		}
	}	
}
