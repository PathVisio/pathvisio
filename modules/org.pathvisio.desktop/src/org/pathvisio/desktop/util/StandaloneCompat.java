/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2019 BiGCaT Bioinformatics
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.desktop.util;

import javax.swing.JOptionPane;

import org.pathvisio.core.data.GdbEvent;
import org.pathvisio.core.data.GdbManager.GdbEventListener;
import org.pathvisio.desktop.PvDesktop;

/**
 * Collection of compatibility "hacks" for the standalone PathVisio app.
 */
public class StandaloneCompat implements GdbEventListener
{
	private final PvDesktop desktop;

	public StandaloneCompat (PvDesktop desktop)
	{
		this.desktop = desktop;
		desktop.getSwingEngine().getGdbManager().addGdbEventListener(this);
	}

	public void gdbEvent(GdbEvent e)
	{
		switch (e.getType())
		{
		case ADDED:
			{
				String name = e.getName();

				//Very primitive check for updates
				if (name != null && (name.contains("Derby_2011") || name.contains("Derby_2010") || name.contains("Derby_2009") || name.contains("Derby_2008")))
				{
					JOptionPane.showMessageDialog(desktop.getFrame(),
					"A newer identifier mapping database is available\n" +
					"Please check http://www.pathvisio.org/downloads/download-bridgedbs/ for the latest database.");
				}
			}
		default:
			break;
		}
	}
}
