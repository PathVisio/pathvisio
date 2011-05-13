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
package org.pathvisio.wpdownload;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.xml.rpc.ServiceException;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.Preference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.ProgressDialog;
import org.wikipathways.client.WikiPathwaysCache;
import org.wikipathways.client.WikiPathwaysClient;

/**
 * A plugin that let's PathVisio download a set of Pathways from WikiPathways
 */
public class WPDownloadPlugin implements Plugin
{
	/**
	 * Preferences related to this plug-in that will be stored together with
	 * other PathVisio preferences.
	 */
	enum WPDownloadPreference implements Preference
	{
		WPDL_CACHE_DIR (new File(GlobalPreference.getDataDir().toString(), "wikipathways").toString()),
		WPDL_AUTO (Boolean.toString(false));

		WPDownloadPreference (String defaultValue)
		{
			this.defaultValue = defaultValue;
		}

		private String defaultValue;

		public String getDefault() {
			return defaultValue;
		}
	}

	private PvDesktop desktop;

	public void init(PvDesktop desktop)
	{
		this.desktop = desktop;
		desktop.registerMenuAction ("File", wpAction);

		// do automatically if preference is set.
		if (PreferenceManager.getCurrent().getBoolean(WPDownloadPreference.WPDL_AUTO))
		{
			doCache(PreferenceManager.getCurrent().getFile(WPDownloadPreference.WPDL_CACHE_DIR));
		}
	}

	public void done() {}

	private final WpAction wpAction = new WpAction();

	private class WpAction extends AbstractAction
	{
		WpAction()
		{
			putValue (NAME, "Update from WikiPathways");
		}

		public void actionPerformed(ActionEvent arg0)
		{
			doCache(PreferenceManager.getCurrent().getFile(WPDownloadPreference.WPDL_CACHE_DIR));
		}
	}

	private WikiPathwaysClient client = null;
	private WikiPathwaysCache cache = null;

	public void doCache (final File cacheDir)
	{
		final ProgressKeeper pk = new ProgressKeeper(100);

		final ProgressDialog d = new ProgressDialog(desktop.getFrame(),
				"", pk, true, true);

		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

			Exception ex = null;

			@Override
			protected Void doInBackground() {
				try
				{
					pk.setTaskName("Updating local pathay list");
					if(client == null) {
						client = new WikiPathwaysClient();
					}
					if (!cacheDir.exists())
						cacheDir.mkdirs();
					cache = new WikiPathwaysCache(client, cacheDir);
					pk.worked (10);
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
					JOptionPane.showMessageDialog(desktop.getFrame(),
							"Exception while updating cache\n" + ex.getMessage());
				}

			}
		};

		sw.execute();
		d.setVisible(true);
	}
}
