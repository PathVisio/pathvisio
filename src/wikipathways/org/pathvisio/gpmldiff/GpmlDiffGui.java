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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;

import org.pathvisio.preferences.PreferenceManager;


/**
 * Standalone version of the GpmlDiff Applet,
 * mainly used for testing purposes.
 *
 * The main method takes two arguments: the file names
 * of the two pathways to be compared.
 */
class GpmlDiffGui
{
	private static final int WINDOW_WIDTH = 1000;
	private static final int WINDOW_HEIGHT = 500;

	public static void main (String[] argv)
	{
		PreferenceManager.init();
		JFrame window = new JFrame();
		final GpmlDiffWindow panel = new GpmlDiffWindow(window);
		panel.addFileActions();
		if (argv.length > 0)
		{
			File f = new File (argv[0]);
			if (f.exists())
			{
				panel.setFile(GpmlDiffWindow.PWY_OLD, f);
				if (argv.length > 1)
				{
					f = new File (argv[1]);
					if (f.exists())
					{
						panel.setFile (GpmlDiffWindow.PWY_NEW, f);
					}
				}
			}
		}
		window.setSize (WINDOW_WIDTH, WINDOW_HEIGHT);
		window.setContentPane(panel);
		window.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

		window.addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent arg0)
			{
				panel.zoomToFit();
			}
		});
		window.setVisible (true);


	}
}