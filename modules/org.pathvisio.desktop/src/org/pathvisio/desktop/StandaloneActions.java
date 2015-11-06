// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.desktop;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.net.URI;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.Globals;
import org.pathvisio.core.util.Resources;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.ViewActions;
import org.pathvisio.desktop.dialog.RunLocalPluginDialog;
import org.pathvisio.gui.SwingEngine;

/**
 * collection of menu / toolbar Actions that are only needed
 * in the standalone (non-applet)
 * version of PathVisio.
 */
public class StandaloneActions implements ApplicationEventListener
{
	private static final URL IMG_OPEN = Resources.getResourceURL("open.gif");
	private static final URL IMG_NEW = Resources.getResourceURL("new.gif");

	public final Action openAction;
	public final Action helpAction;
	public final Action newAction;
	public final Action selectGeneDbAction;
	public final Action selectMetaboliteDbAction;
	public final Action selectInteractionDbAction;
	public final Action preferencesAction;
	public final Action searchAction;
	public final Action newPluginManagerAction;
	public final Action loadLocalBundlesAction;
	public final Action printAction;

	StandaloneActions (PvDesktop desktop)
	{
		SwingEngine swingEngine = desktop.getSwingEngine();
		openAction = new OpenAction(swingEngine);
		helpAction = new HelpAction();
		newAction = new NewAction(swingEngine);
		selectGeneDbAction = new SelectGeneDbAction(desktop, "Gene");
		selectMetaboliteDbAction = new SelectGeneDbAction(desktop, "Metabolite");
		/**
		 * @author anwesha
		 */
		selectInteractionDbAction = new SelectGeneDbAction(desktop, "Interaction");
		preferencesAction = new PreferencesAction(desktop);
		searchAction = new SearchAction(swingEngine);
		newPluginManagerAction = new NewPluginManagerAction(desktop);
		loadLocalBundlesAction = new LoadLocalBundlesAction(desktop);
		//registering this class to receive Application level events (used in PrintAction) 
		swingEngine.getEngine().addApplicationEventListener(this);
		printAction = new PrintAction(swingEngine);
	}

	public void applicationEvent(ApplicationEvent e) {
		if(e.getType() == ApplicationEvent.Type.VPATHWAY_CREATED) {
			ViewActions va = ((VPathway)e.getSource()).getViewActions();
			va.registerToGroup(printAction, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.resetGroupStates();
		}
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

		public void actionPerformed(ActionEvent e) {
			//TODO: wrap in thread, progress dialog
			String url = Globals.HELP_URL;
			try {
				if(Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI(url));
				} else {
					new JOptionPane("Could not open default browser.\n Please go to\n" + url + "\nin your browser.", JOptionPane.WARNING_MESSAGE);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Help -> Plugin Manager
	 * Show a list of active plugins and errors that
	 * occurred while initializing the plugin manager.
	 */
	public static class NewPluginManagerAction extends AbstractAction
	{
		PvDesktop pvDesktop;
	
		public NewPluginManagerAction(PvDesktop desktop)
		{
			super();
			this.pvDesktop = desktop;
			putValue(NAME, "Plugin manager");
			putValue(SHORT_DESCRIPTION, "Information about active plugins");
		}
	
		public void actionPerformed(ActionEvent e)
		{
			pvDesktop.getPluginManagerExternal().showGui(pvDesktop.getFrame());
		}
	}


	/**
	 * Plugins -> Install local plugins
	 * allows users to start all bundles in a directory
	 */
	public static class LoadLocalBundlesAction extends AbstractAction
	{
		PvDesktop pvDesktop;

		public LoadLocalBundlesAction(PvDesktop desktop)
		{
			super();
			this.pvDesktop = desktop;
			putValue(NAME, "Install local plugins");
			putValue(SHORT_DESCRIPTION, "Information about active plugins");
		}

		public void actionPerformed(ActionEvent e)
		{
			RunLocalPluginDialog dlg = new RunLocalPluginDialog(pvDesktop);
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
	 * Let the user pick a gene , metabolite or interaction database.
	 * Invoked in menu->data->select gene database
	 */
	public static class SelectGeneDbAction extends AbstractAction
	{
		PvDesktop desktop;

		String dbType;
		/**
		 * type should be "Gene","Metabolite", or Interaction
		 */
		public SelectGeneDbAction(PvDesktop desktop, String type)
		{
			super();
			this.desktop = desktop;
			dbType = type;
			assert (dbType.equals ("Gene") || dbType.equals ("Metabolite")|| dbType.equals ("Interaction"));
			putValue(NAME, "Select " + dbType + " Database");
			putValue(SHORT_DESCRIPTION, "Select " + dbType + " Database");
		}

		public void actionPerformed(ActionEvent e)
		{
			desktop.selectGdb(dbType);
		}

	}

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
	
	/**
	 * Print menu item. Prints the current Pathway
	 */
	public static class PrintAction extends AbstractAction
	{
		SwingEngine swingEngine;

		public PrintAction(SwingEngine swingEngine)
		{
			super();
			this.swingEngine = swingEngine;
			putValue(NAME, "Print");
			putValue(SHORT_DESCRIPTION, "Print Pathway");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{	
			PrinterJob pj = PrinterJob.getPrinterJob();
			pj.setJobName(" Print Component ");
			PageFormat originalPageFormat = pj.defaultPage();
			//pops up the Page Setup Dialogue
			PageFormat returnedPageFormat = pj.pageDialog(originalPageFormat);
			/*check whether the user canceled or okayed the Page Setup Dialogue by comparing thePageFormat objects 
			 * if canceled, cancel the Print operation*/ 
			if(returnedPageFormat == originalPageFormat)
				return;
			pj.setPrintable (new Printable() {    
				@Override
				public int print(java.awt.Graphics graphics, PageFormat pageFormat, int pageIndex)	
					throws PrinterException {
					if (pageIndex > 0){
						return Printable.NO_SUCH_PAGE;
					}
					VPathway vPathway = swingEngine.getEngine().getActiveVPathway();
					Graphics2D g2 = (Graphics2D) graphics;
					double xScale = pageFormat.getImageableWidth()/vPathway.getVWidth();
					double yScale = pageFormat.getImageableHeight()/vPathway.getVHeight();
					//scaling ratio is being set to the minimum of the two, so that the scaling covers even the larger one. 
					double minScale = Math.min(xScale, yScale);
					g2.translate(pageFormat.getImageableX(),pageFormat.getImageableY());
					//scaling both x and y using the minimum of the 2 scale ratios calculated above.
					//i.e using the same scaling ratio for both x and y axis to maintain the aspect ratio (width:height)
					g2.scale(minScale, minScale);
					vPathway.draw(g2);
					return Printable.PAGE_EXISTS;
				}
			});
			
			if (pj.printDialog() == false)
				return;

			try {
				pj.print();
			} catch (PrinterException ex) {
				//may be popup a dialogue saying that there's a problem with the print system (printer problems) 
				System.out.println("PrinterException while printing Pathway "+ex.getMessage());
			}

		}
	}

}
