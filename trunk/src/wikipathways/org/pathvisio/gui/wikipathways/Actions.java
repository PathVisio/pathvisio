package org.pathvisio.gui.wikipathways;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.wikipathways.WikiPathways;

public class Actions {
	static abstract class WikiAction extends AbstractAction {
		AppletMain applet;
		WikiPathways wiki;
		public WikiAction(AppletMain a, WikiPathways w, String name, ImageIcon icon) {
			super(name, icon);
			applet = a;
			wiki = w;
		}
	}
	
	class ExitAction extends WikiAction {
		boolean doSave;
		public ExitAction(AppletMain a, WikiPathways w, boolean save) {
			super(a, w, "Finish", new ImageIcon(save ? Engine.getCurrent().getResourceURL("icons/apply.gif") : Engine.getCurrent().getResourceURL("icons/cancel.gif")));
			doSave = save;
			String descr = doSave ? "Save pathway and close editor" : "Discard pathway and close editor";
			putValue(Action.SHORT_DESCRIPTION, descr);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("DEBUG: exit pressed, " + doSave);
			boolean saved = true;
			if(doSave) {
				saved = wiki.saveUI();
			}
			if(saved) {
				applet.endWithMessage("Please wait while you'll be redirected to the pathway page");
			}
		}
	}
	
	
	static class SaveToServerAction extends WikiAction {
		public SaveToServerAction(AppletMain a, WikiPathways w) {
			super(a, w, "Save to ", new ImageIcon(Engine.getCurrent().getResourceURL("icons/save.gif")));
			putValue(Action.SHORT_DESCRIPTION, "Save the pathway to " + Globals.SERVER_NAME);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
}
