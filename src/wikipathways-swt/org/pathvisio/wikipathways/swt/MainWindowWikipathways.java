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
package org.pathvisio.wikipathways.swt;

import java.net.MalformedURLException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swt.MainWindow;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.model.Pathway;
import org.pathvisio.wikipathways.WikiPathways;

public class MainWindowWikipathways extends MainWindow {
	WikiPathways wiki;
	
	public MainWindowWikipathways(WikiPathways w) {
		super();
		wiki = w;
	}
		
	void setReadOnly(final boolean readOnly) { 
		threadSave(new Runnable() {
			public void run() {
				((Action)switchEditModeAction).setEnabled(!readOnly);
			}
		});
	}
	
	protected boolean canHandleShellCloseEvent() {
		Pathway p = Engine.getCurrent().getActivePathway();
		if(p != null && p.hasChanged()) {
			Display.getCurrent().syncExec(new Runnable() {
				public void run() {
					boolean doit = MessageDialog.openQuestion(getShell(), "Save pathway?", 
							"Do you want to save the changes to " + wiki.getPwName() + " on " + Globals.SERVER_NAME + "?");
					if(doit) {
						boolean saved = wiki.saveUI();
						MessageDialog.openInformation(getShell(), "Pathway saved", "The pathway is saved to " + Globals.SERVER_NAME + 
							".\n Refresh the pathway page in your browser by pressing F5");
					}
					
				}
			});
		}
		return true;
	}
}
