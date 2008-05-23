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
package org.pathvisio.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.DBConnectorSwing;
import org.pathvisio.data.GdbManager;
import org.pathvisio.data.GexManager;
import org.pathvisio.data.SimpleGex;
import org.pathvisio.debug.Logger;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.gui.VisualizationDialog;

import edu.stanford.ejalbert.BrowserLauncher;

public class StandaloneActions 
{
	public static URL IMG_OPEN = Engine.getCurrent().getResourceURL("open.gif");
	public static URL IMG_NEW = Engine.getCurrent().getResourceURL("new.gif");

	public static final Action openAction = new OpenAction();
	public static final Action helpAction = new HelpAction();
	public static final Action newAction = new NewAction();
	public static final Action selectGeneDbAction = new SelectGeneDbAction("Gene");
	public static final Action selectMetaboliteDbAction = new SelectGeneDbAction("Metabolite");
	public static final Action importGexDataAction = new ImportGexDataAction();
	public static final Action selectGexAction = new SelectGexAction();
	public static final Action aboutAction = new AboutAction();
	public static final Action preferencesAction = new PreferencesAction();

	/**
	 * Open the online help in a browser window.
	 * In menu->help->help or F1
	 */
	public static class HelpAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
	
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
	 * Open a pathway from disk.
	 * In menu->file->open
	 */
	public static class OpenAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
	
		public OpenAction() 
		{
			super();
			putValue(NAME, "Open");
			putValue(SMALL_ICON, new ImageIcon (StandaloneActions.IMG_OPEN));
			putValue(SHORT_DESCRIPTION, "Open a pathway file");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		}
	
		public void actionPerformed(ActionEvent e) 
		{
			if (SwingEngine.getCurrent().canDiscardPathway())
			{
				SwingEngine.getCurrent().openPathway();
			}
		}
	}

	/**
	 * Create a new pathway action
	 * In menu->file->new pathway
	 */
	public static class NewAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
	
		public NewAction() 
		{
			super();
			putValue(NAME, "New");
			putValue(SMALL_ICON, new ImageIcon(IMG_NEW));
			putValue(SHORT_DESCRIPTION, "Start a new, empty pathway");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		}
	
		public void actionPerformed(ActionEvent e) 
		{
			if (SwingEngine.getCurrent().canDiscardPathway())
			{
				SwingEngine.getCurrent().newPathway();
			}
		}
	}
	
	/**
	 * Import gex data and create a new gex database from it
	 */
	public static class ImportGexDataAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;
		
		public ImportGexDataAction()
		{
			super();
			putValue (NAME, "Import expression data");
			putValue (SHORT_DESCRIPTION, "Import data from a tab delimited text file, for example experimental data from a high-throughput experiment");
		}
		
		public void actionPerformed (ActionEvent e)
		{
			GexImportWizard wizard = new GexImportWizard();
			int ret = wizard.showModalDialog();
			
			// ret == (0=Finish,1=Cancel,2=Error) 
		}
	}
	
	/**
	 * Let the user open an expression dataset
	 * @author thomas
	 */
	public static class SelectGexAction extends AbstractAction {
		public SelectGexAction() {
			putValue(NAME, "Select expression dataset");
			putValue(SHORT_DESCRIPTION, "Select expression dataset");
		}
		
		public void actionPerformed(ActionEvent e) {
			try 
			{
				/**
				 * Get the preferred database connector to connect to Gex databases, 
				 * and try to cast it to swingDbConnector.
				 * throws an exception if that fails
				 */
				DBConnectorSwing dbcon;
				DBConnector dbc = Engine.getCurrent().getDbConnector(DBConnector.TYPE_GEX);
				if(dbc instanceof DBConnectorSwing) 
				{
					dbcon = (DBConnectorSwing)dbc;
				} 
				else 
				{
					//TODO: better handling of error
					throw new IllegalArgumentException("Not a Swing database connector");
				}

				String dbName = dbcon.openChooseDbDialog(null);
				
				if(dbName == null) return;
				
				GexManager.setCurrentGex(dbName, false);
			} 
			catch(Exception ex) 
			{
				String msg = "Failed to open expression dataset; " + ex.getMessage();
				JOptionPane.showMessageDialog(null, 
						"Error: " + msg + "\n\n" + "See the error log for details.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				Logger.log.error(msg, ex);
			}
		}
	}
	
	/**
	 * Let the user pick a gene or metabolite database.
	 * Invoked in menu->data->select gene database
	 */
	public static class SelectGeneDbAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		String dbType;
		/** 
		 * type should be "Gene" or "Metabolite"
		 */
		public SelectGeneDbAction(String type) 
		{
			super();
			dbType = type;
			assert (dbType.equals ("Gene") || dbType.equals ("Metabolite"));
			putValue(NAME, "Select " + dbType + " Database");
			putValue(SHORT_DESCRIPTION, "Select " + dbType + " Database");
		}

		public void actionPerformed(ActionEvent e) 
		{
			try 
			{
				/**
				 * Get the preferred database connector to connect to Gex or Gdb databases, 
				 * and try to cast it to swingDbConnector.
				 * throws an exception if that fails
				 */
				DBConnectorSwing dbcon;
				DBConnector dbc = Engine.getCurrent().getDbConnector(DBConnector.TYPE_GDB);
				if(dbc instanceof DBConnectorSwing) 
				{
					dbcon = (DBConnectorSwing)dbc;
				} 
				else 
				{
					//TODO: better handling of error
					throw new IllegalArgumentException("Not a Swing database connector");
				}

				String dbName = dbcon.openChooseDbDialog(null);
				
				if(dbName == null) return;
				
				GdbManager.setGeneDb(dbName);
			} 
			catch(Exception ex) 
			{
				String msg = "Failed to open " + dbType + " Database; " + ex.getMessage();
				JOptionPane.showMessageDialog(null, 
						"Error: " + msg + "\n\n" + "See the error log for details.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				Logger.log.error(msg, ex);
			}
		}
	}
	
	/**
	 * Open the about dialog,
	 * showing a list of authors and the current program version
	 */
	public static class AboutAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public AboutAction() 
		{
			super();
			putValue(NAME, "About");
			putValue(SHORT_DESCRIPTION, "About " + Globals.APPLICATION_NAME);
			putValue(LONG_DESCRIPTION, "About " + Globals.APPLICATION_NAME);
		}

		public void actionPerformed(ActionEvent e) 
		{
			AboutDlg.createAndShowGUI();
		}
	}

	/**
	 * Show preferences dialog.
	 * Invoked in menu->edit->preferences
	 */
	public static class PreferencesAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public PreferencesAction() 
		{
			super();
			putValue(NAME, "Preferences");
			putValue(SHORT_DESCRIPTION, "Edit preferences");
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			PreferencesDlg dlg = new PreferencesDlg();
			dlg.createAndShowGUI();
		}
	}

	public static class VisualizationAction extends AbstractAction {
		MainPanel mainPanel;
		
		public VisualizationAction(MainPanel mainPanel) {
			putValue(NAME, "Visualization options");
			this.mainPanel = mainPanel;
		}
		
		public void actionPerformed(ActionEvent e) {
			new VisualizationDialog(
					VisualizationManager.getCurrent(),
					null,
					mainPanel
			).setVisible(true);
		}
	}
}
