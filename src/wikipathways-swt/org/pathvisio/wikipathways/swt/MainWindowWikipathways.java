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
