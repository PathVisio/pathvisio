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
package org.pathvisio.gui.wikipathways;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.wikipathways.UserInterfaceHandler;
import org.pathvisio.wikipathways.WikiPathways;

/**
 * A collection of actions related to WikiPathways
 * @author thomas
 */
public class Actions {
	public static abstract class WikiAction extends AbstractAction {
		UserInterfaceHandler uiHandler;
		WikiPathways wiki;
		public WikiAction(UserInterfaceHandler uih, WikiPathways w, String name, ImageIcon icon) {
			super(name, icon);
			uiHandler = uih;
			wiki = w;
		}
	}
	
	/**
	 * Exits the editor, either with or without saving the current pathway to WikiPathways
	 * @author thomas
	 */
	public static class ExitAction extends WikiAction {
		boolean doSave;
	
		public ExitAction(UserInterfaceHandler h, WikiPathways w, boolean save) {
			super(h, w, "Finish", new ImageIcon(save ? Engine.getCurrent().getResourceURL("icons/apply.gif") : Engine.getCurrent().getResourceURL("icons/cancel.gif")));
			doSave = save;
			String descr = doSave ? "Save pathway and close editor" : "Discard pathway and close editor";
			putValue(Action.SHORT_DESCRIPTION, descr);
		}
		public void actionPerformed(ActionEvent e) {
			boolean saved = true;
			try {
				if(doSave) {
					saved = wiki.saveUI();
					System.out.println("SAVED: saved");
				}
			} catch(Exception ex) {
				Logger.log.error("Unable to save pathway", ex);
				JOptionPane.showMessageDialog(null, "Unable to save pathway:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			if(saved) {
				uiHandler.showExitMessage("Please wait...the page will be reloaded");
				try {
					if(wiki.isNew()) {
						uiHandler.showDocument(new URL(wiki.getPwURL()), "_top");
					} else {
						uiHandler.showDocument(new URL("javascript:window.location.reload();"), "_top");
					}
				} catch (MalformedURLException ex) {
					Logger.log.error("Unable to refresh pathway page", ex);
				}
			}
		}
	}
	
	
	public static class SaveToServerAction extends WikiAction {
		public SaveToServerAction(UserInterfaceHandler h, WikiPathways w) {
			super(h, w, "Save to ", new ImageIcon(Engine.getCurrent().getResourceURL("icons/save.gif")));
			putValue(Action.SHORT_DESCRIPTION, "Save the pathway to " + Globals.SERVER_NAME);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			try {
				wiki.saveUI();
			} catch(Exception ex) {
				Logger.log.error("Unable to save pathway", ex);
				JOptionPane.showMessageDialog(null, "Unable to save pathway:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Action that switches an applet between fullscreen and embedded mode
	 * @author thomas
	 *
	 */
	public static class FullScreenAction extends WikiAction {
		final ImageIcon imgFull = new ImageIcon(
				Engine.getCurrent().getResourceURL("icons/fullscreen.gif"));
		final ImageIcon imgRestore = new ImageIcon(
				Engine.getCurrent().getResourceURL("icons/restorescreen.gif"));
		final String tooltip_full = "Switch to fullscreen mode";
		final String tooltip_restore = "Switch to embedded mode";
		
		JFrame frame;
		JApplet applet;
		
		public FullScreenAction(UserInterfaceHandler uiHandler, WikiPathways wiki, JApplet applet) {
			super(uiHandler, wiki, "Fullscreen", null);
			this.applet = applet;
			putValue(WikiAction.SMALL_ICON, imgFull);
			putValue(WikiAction.SHORT_DESCRIPTION, tooltip_full);
		}
				
		public void actionPerformed(ActionEvent e) {
			if(frame == null) {
				toFrame();
				putValue(WikiAction.SMALL_ICON, imgRestore);
				putValue(WikiAction.NAME, "Fullscreen");
				putValue(WikiAction.SHORT_DESCRIPTION, tooltip_restore);
			} else {
				toApplet();
				putValue(WikiAction.SMALL_ICON, imgFull);
				putValue(WikiAction.NAME, "Restore screen");
				putValue(WikiAction.SHORT_DESCRIPTION, tooltip_full);
			}
		}
		
		/**
		 * Creates a new frame and transfers the mainPanel from
		 * the applet to the frame
		 */
		private void toFrame() {
			final MainPanel mainPanel = wiki.getMainPanel();
			frame = new JFrame();
			applet.getContentPane().remove(mainPanel);
			frame.getContentPane().add(mainPanel);
			frame.setVisible(true);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					Logger.log.trace("Window closing, switch to applet");
					frame.getContentPane().remove(mainPanel);
					applet.getContentPane().add(mainPanel, BorderLayout.CENTER);
					applet.validate();
				}
			});
			applet.validate();
		}
		/**
		 * Disposes the frame and transfers the mainPanel to the
		 * applet
		 */
		private void toApplet() {
			MainPanel mainPanel = wiki.getMainPanel();
			frame.getContentPane().remove(mainPanel);
			applet.getContentPane().add(mainPanel, BorderLayout.CENTER);
			frame.setVisible(false);
			frame.dispose();
			frame = null;
			applet.validate();
		}
	}
}
