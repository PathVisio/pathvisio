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
package org.pathvisio.plugins;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.xml.rpc.ServiceException;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.data.DataException;
import org.pathvisio.gui.swing.ProgressDialog;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.model.ConverterException;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.wikipathways.WikiPathwaysCache;
import org.pathvisio.wikipathways.WikiPathwaysClient;

/**
 * A plugin that let's PathVisio download a set of Pathways from WikiPathways
 */
public class WPDownloadPlugin implements Plugin
{
	private PvDesktop desktop;
	
	public void init(PvDesktop desktop) 
	{
		this.desktop = desktop;
		desktop.registerMenuAction ("File", wpAction);
	}

	private final WpAction wpAction = new WpAction();
	
	private class WpAction extends AbstractAction
	{
		WpAction()
		{
			putValue (NAME, "Download set from WikiPathways"); 
		}
		
		public void actionPerformed(ActionEvent arg0) 
		{
			doCache(new File ("/home/martijn/wikipathways"));
		}
	}
	
	private WikiPathwaysClient client = null;
	private WikiPathwaysCache cache = null;
	
	public void doCache (final File cacheDir) 
	{
		final ProgressKeeper pk = new ProgressKeeper();
		
		final ProgressDialog d = new ProgressDialog(desktop.getFrame(), 
				"", pk, false, true);

		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

			Exception ex = null;
			
			@Override
			protected Void doInBackground() {
				try
				{
					if(client == null) {
						client = new WikiPathwaysClient();
					}
					cache = new WikiPathwaysCache(client, cacheDir);
					cache.update(pk);
				}
				catch (ServiceException ex1)
				{
					ex = ex1;
				}
				catch (IOException ex2)
				{
					ex = ex2;
				} 
				catch (ConverterException ex4)
				{
					ex = ex4;
				}
				finally
				{
					pk.finished();
				}
				return null;
			}
			
			@Override
			protected void done()
			{
				if (ex != null)
				{ 
					ex.printStackTrace();
					JOptionPane.showMessageDialog(desktop.getFrame(), "Exception while updating cache\n" + ex.getMessage());
				}
				
			}
		};
		
		sw.execute();
		d.setVisible(true);
	}
}
