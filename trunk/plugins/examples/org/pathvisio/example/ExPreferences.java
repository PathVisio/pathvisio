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
package org.pathvisio.example;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.pathvisio.core.preferences.Preference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.desktop.PreferencesDlg;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

/**
 * Example of how to use Preference Manager and Preferences Dlg
 */
public class ExPreferences implements Plugin {

	private PvDesktop desktop;

	/**
	 * Defines three example preferences
	 */
	enum ExPreference implements Preference
	{
		/**
		 * This will be the last opened dir of a JFileChooser.
		 * This preference will be stored but not available
		 */
		EXAMPLE_LAST_OPENED_DIR (System.getProperty("user.home")),

		/**
		 * An integer preference, available through the Example tab
		 * of the preference manager
		 */
		EXAMPLE_INT ("" + 42),

		/**
		 * A Color preference, available through the Example tab
		 * of the preference manager
		 */
		EXAMPLE_COLOR ("255,0,0");

		ExPreference (String defaultValue)
		{
			this.defaultValue = defaultValue;
		}

		private String defaultValue;

		public String getDefault() {
			return defaultValue;
		}
	}

	public void init(PvDesktop desktop)
	{
		this.desktop = desktop;

		PreferencesDlg dlg = desktop.getPreferencesDlg();

		dlg.addPanel("Example Plugin",
				dlg.builder()
					.colorField(ExPreference.EXAMPLE_COLOR, "Example Color")
					.integerField(ExPreference.EXAMPLE_INT, "Example Integer between 0 and 100", 0, 100)
					.build()
				);

		desktop.registerMenuAction("File", new ChooseFileAction());
	}

	private class ChooseFileAction extends AbstractAction
	{
		public ChooseFileAction()
		{
			// This will be the label of the pop up menu item.
			putValue (NAME, "Example File Chooser");
		}

		public void actionPerformed(ActionEvent arg0)
		{
			// Display a message with the actual class
			JFileChooser jfc = new JFileChooser();
			// set current directory to what was stored in the preferences
			// default: home directory.
			jfc.setCurrentDirectory(
					PreferenceManager.getCurrent().getFile(ExPreference.EXAMPLE_LAST_OPENED_DIR));
			int status = jfc.showOpenDialog(desktop.getFrame());
			if(status == JFileChooser.APPROVE_OPTION)
			{
				// save current directory of jfc.
				// next time will be opened in same location.
				PreferenceManager.getCurrent().setFile(ExPreference.EXAMPLE_LAST_OPENED_DIR,
						jfc.getCurrentDirectory());
			}
		}
	}

	public void done() {}
}
