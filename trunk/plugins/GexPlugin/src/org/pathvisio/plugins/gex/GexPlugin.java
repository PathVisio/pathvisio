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
package org.pathvisio.plugins.gex;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.bridgedb.rdb.DBConnector;
import org.pathvisio.data.DBConnectorSwing;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.plugin.Plugin;

/**
 * This plugin enables Gex functionality. Currently only adds menu items
 * to import or load a Gex
 * @author thomas
 */
public class GexPlugin implements Plugin {

	public void init(PvDesktop desktop)
	{
		ImportGexDataAction importAction = new ImportGexDataAction(desktop);
		SelectGexAction selectAction = new SelectGexAction(desktop);

		desktop.registerMenuAction ("Data", importAction);
		desktop.registerMenuAction ("Data", selectAction);
	}

	public void done() {};

	/**
	 * Import gex data and create a new gex database from it
	 */
	public static class ImportGexDataAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		private final PvDesktop sae;

		public ImportGexDataAction(PvDesktop sae)
		{
			super();
			this.sae = sae;
			putValue (NAME, "Import expression data");
			putValue (SHORT_DESCRIPTION, "Import data from a tab delimited text file, for example experimental data from a high-throughput experiment");
		}

		public void actionPerformed (ActionEvent e)
		{
			GexImportWizard wizard = new GexImportWizard(sae);
			wizard.showModalDialog(sae.getSwingEngine().getFrame());
		}
	}

	/**
	 * Let the user open an expression dataset
	 * @author thomas
	 */
	public static class SelectGexAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;
		private final PvDesktop se;

		public SelectGexAction(PvDesktop standaloneEngine) {
			se = standaloneEngine;
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
				DBConnector dbc = se.getGexManager().getDBConnector();
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

				se.getGexManager().setCurrentGex(dbName, false);
				se.loadGexCache();
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
}
