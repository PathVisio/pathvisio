// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.gui.swing;

import edu.stanford.ejalbert.BrowserLauncher;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.pathvisio.Globals;
import org.pathvisio.util.Resources;

/**
 * collection of menu / toolbar Actions that are only needed
 * in the standalone (non-applet)
 * version of PathVisio.
 */
public class StandaloneActions
{
	private static final URL IMG_OPEN = Resources.getResourceURL("open.gif");
	private static final URL IMG_NEW = Resources.getResourceURL("new.gif");

	public final Action openAction;
	public final Action helpAction;
	public final Action newAction;
	public final Action selectGeneDbAction;
	public final Action selectMetaboliteDbAction;
	public final Action preferencesAction;
	public final Action searchAction;
	public final Action pluginManagerAction;

	StandaloneActions (PvDesktop desktop)
	{
		SwingEngine swingEngine = desktop.getSwingEngine();
		openAction = new OpenAction(swingEngine);
		helpAction = new HelpAction();
		newAction = new NewAction(swingEngine);
		selectGeneDbAction = new SelectGeneDbAction(desktop, "Gene");
		selectMetaboliteDbAction = new SelectGeneDbAction(desktop, "Metabolite");
		preferencesAction = new PreferencesAction(desktop);
		searchAction = new SearchAction(swingEngine);
		pluginManagerAction = new PluginManagerAction(desktop);
	}

	/**
	 * Open the online help in a browser window.
	 * In menu->help->help or F1
	 */
	public static class HelpAction extends AbstractAction
	{

		public HelpAction()
		{
			super();
			putValue(NAME, "Help");
			putValue(SHORT_DESCRIPTION, "Open online help in a browser window");
			putValue(LONG_DESCRIPTION, "Open online help in a browser window");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		}

		public void actionPerformed(ActionEvent e)
		{
			//TODO: wrap in thread, progress dialog
			String url = Globals.HELP_URL;
			try
			{
				BrowserLauncher bl = new BrowserLauncher(null);
				bl.openURLinBrowser(url);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Help -> Plugin Manager
	 * Show a list of active plugins and errors that
	 * occurred while initializing the plugin manager.
	 */
	public static class PluginManagerAction extends AbstractAction
	{
		PvDesktop pvDesktop;

		public PluginManagerAction(PvDesktop desktop)
		{
			super();
			this.pvDesktop = desktop;
			putValue(NAME, "Plugin manager");
			putValue(SHORT_DESCRIPTION, "Information about active plugins");
		}

		public void actionPerformed(ActionEvent e)
		{
			PluginManagerDlg dlg = new PluginManagerDlg (pvDesktop);
			dlg.createAndShowGUI();
		}
	}

	/**
	 * Open a pathway from disk.
	 * In menu->file->open
	 */
	public static class OpenAction extends AbstractAction
	{

		SwingEngine swingEngine;

		public OpenAction(SwingEngine swingEngine)
		{
			super();
			this.swingEngine = swingEngine;
			putValue(NAME, "Open");
			putValue(SMALL_ICON, new ImageIcon (StandaloneActions.IMG_OPEN));
			putValue(SHORT_DESCRIPTION, "Open a pathway file");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e)
		{
			if (swingEngine.canDiscardPathway())
			{
				swingEngine.openPathway();
			}
		}
	}

	/**
	 * Create a new pathway action
	 * In menu->file->new pathway
	 */
	public static class NewAction extends AbstractAction
	{

		SwingEngine swingEngine;

		public NewAction(SwingEngine swingEngine)
		{
			super();
			this.swingEngine = swingEngine;
			putValue(NAME, "New");
			putValue(SMALL_ICON, new ImageIcon(IMG_NEW));
			putValue(SHORT_DESCRIPTION, "Start a new, empty pathway");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e)
		{
			if (swingEngine.canDiscardPathway())
			{
				swingEngine.newPathway();
			}
		}
	}

	/**
	 * Let the user pick a gene or metabolite database.
	 * Invoked in menu->data->select gene database
	 */
	public static class SelectGeneDbAction extends AbstractAction
	{
		PvDesktop desktop;

		String dbType;
		/**
		 * type should be "Gene" or "Metabolite"
		 */
		public SelectGeneDbAction(PvDesktop desktop, String type)
		{
			super();
			this.desktop = desktop;
			dbType = type;
			assert (dbType.equals ("Gene") || dbType.equals ("Metabolite"));
			putValue(NAME, "Select " + dbType + " Database");
			putValue(SHORT_DESCRIPTION, "Select " + dbType + " Database");
		}

		public void actionPerformed(ActionEvent e)
		{
			desktop.selectGdb(dbType);
		}

	}

	/**
	 * Open the about dialog,
	 * showing a list of authors and the current program version
	 */

	/**
	 * Show preferences dialog.
	 * Invoked in menu->edit->preferences
	 */
	public static class PreferencesAction extends AbstractAction
	{

		PvDesktop desktop;

		public PreferencesAction(PvDesktop aDesktop)
		{
			super();
			this.desktop = aDesktop;
			putValue(NAME, "Preferences");
			putValue(SHORT_DESCRIPTION, "Edit preferences");
		}

		public void actionPerformed(ActionEvent e)
		{
			PreferencesDlg dlg = desktop.getPreferencesDlg();
			dlg.createAndShowGUI(desktop.getSwingEngine());
		}
	}

	/** activates search pane */
	public static class SearchAction extends AbstractAction
	{

		SwingEngine swingEngine;

		public SearchAction(SwingEngine swingEngine)
		{
			super();
			this.swingEngine = swingEngine;
			putValue(NAME, "Search pathways");
			putValue(SHORT_DESCRIPTION, "Search pathways for a symbol or identifier");
		}

		public void actionPerformed(ActionEvent e)
		{
			//TODO: right now only shows search pane in side panel
			// really should pop up search dialog.
			JTabbedPane pane = swingEngine.getApplicationPanel().getSideBarTabbedPane();
			int index = pane.indexOfTab("Search");
			if (index > 0)
			{
				pane.setSelectedIndex (index);
			}
		}
	}

}
