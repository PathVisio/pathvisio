package org.pathvisio.wikipathways.swt;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
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
					boolean doit = MessageDialog.openQuestion(SwtEngine.getCurrent().getWindow().getShell(), "Save pathway?", 
							"Do you want to save the changes to " + wiki.getPwName() + " on " + Globals.SERVER_NAME + "?");
					if(doit) {
						wiki.saveUI();
					}
					
				}
			});
		}
		return true;
	}
}
