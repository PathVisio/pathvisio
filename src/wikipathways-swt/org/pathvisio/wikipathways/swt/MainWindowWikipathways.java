package org.pathvisio.wikipathways.swt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Engine;
import org.pathvisio.gui.swt.MainWindow;
import org.pathvisio.gui.swt.SwtEngine;

public class MainWindowWikipathways extends MainWindow {
	WikiPathways wiki;
	
	public MainWindowWikipathways(WikiPathways w) {
		super();
		wiki = w;
	}
	protected boolean canHandleShellCloseEvent() {
		if(Engine.getCurrent().getActivePathway().hasChanged()) {
			Display.getCurrent().syncExec(new Runnable() {
				public void run() {
					boolean doit = MessageDialog.openQuestion(SwtEngine.getCurrent().getWindow().getShell(), "Save pathway?", 
							"Do you want to save the changes to " + wiki.pwName + " on " + WikiPathways.SITE_NAME + "?");
					if(doit) {
						wiki.saveUI();
					}
					
				}
			});
		}
		return true;
	}
}
