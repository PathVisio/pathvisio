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
package org.pathvisio.gpmldiff;

import java.io.File;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JOptionPane;

import org.pathvisio.core.Engine;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.FileUtils;

/**
 * Applet class for the GpmlDiff Applet.
 * This applet can show two pathways side by side, comparing
 * them and highlighting the differences.
 *
 * It has two optional parameters: "old" and "new",
 * pointing to the url's of the old and new pathway respectively.
 */
public class AppletMain extends JApplet
{
	GpmlDiffWindow panel;

	void openUrl(int pwyType, String param)
	{
		try
		{
			URL url = new URL (param);

			String protocol = url.getProtocol();
			File f = null;
			if(protocol.equals("file"))
			{
				f = new File(url.getFile());
				panel.setFile (pwyType, f);
			}
			else
			{
				f = File.createTempFile("urlPathway", "." + Engine.PATHWAY_FILE_EXTENSION);
				FileUtils.downloadFile(url, f);
				panel.setFile (pwyType, f);
			}
		}
		catch(Exception e)
		{
			Logger.log.error ("Exception While downloading url: " + param, e);
			JOptionPane.showMessageDialog (
				this, "Error opening " + (pwyType == GpmlDiffWindow.PWY_OLD ? "old" : "new")
				+ " pathway named\n" + e.getMessage(), "Open pathway error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void init()
	{
		PreferenceManager.init();
		panel = new GpmlDiffWindow(this);
		setContentPane (panel);

		String fnOld = getParameter ("old");
		if (fnOld != null)
		{
			openUrl (GpmlDiffWindow.PWY_OLD, getParameter ("old"));
		}
		else
		{
			JOptionPane.showMessageDialog (
				this, "Missing 'old' parameter\nI won't be able to compare pathways", "Initialization error", JOptionPane.ERROR_MESSAGE);
			Logger.log.error ("Old parameter was missing");
		}
		String fnNew = getParameter ("new");
		if (fnNew != null)
		{
			openUrl (GpmlDiffWindow.PWY_NEW, getParameter ("new"));
		}
		else
		{
			JOptionPane.showMessageDialog (
				this, "Missing 'new' parameter\nI won't be able to compare pathways", "Initialization error", JOptionPane.ERROR_MESSAGE);
			Logger.log.error ("New parameter was missing");
		}
	}

	public void start()
	{
		panel.zoomToFit();
	}

}