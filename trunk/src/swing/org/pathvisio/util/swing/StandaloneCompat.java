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
package org.pathvisio.util.swing;

import javax.swing.JOptionPane;

import org.bridgedb.IDMapperException;
import org.bridgedb.bio.BioDataSource;
import org.pathvisio.data.GdbEvent;
import org.pathvisio.data.GdbManager.GdbEventListener;
import org.pathvisio.gex.GexManager.GexManagerEvent;
import org.pathvisio.gex.GexManager.GexManagerListener;
import org.pathvisio.gui.swing.PvDesktop;

/**
 * Collection of compatibility "hacks" for the standalone PathVisio app.
 */
public class StandaloneCompat implements GdbEventListener, GexManagerListener  
{
	private final PvDesktop desktop;
	
	public StandaloneCompat (PvDesktop desktop)
	{
		this.desktop = desktop;
		desktop.getGexManager().addListener(this);
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
				if (name != null && name.indexOf("_Derby_2008") > 0)
				{
					JOptionPane.showMessageDialog(desktop.getFrame(), 
					"A newer gene database is available\n" +
					"Please browse to http://www.pathvisio.org/Download to obtain the latest.");
				}
			}
		}
	}

	public void gexManagerEvent(GexManagerEvent e) 
	{
		if (e.getType() == GexManagerEvent.CONNECTION_OPENED)
		{
			try
			{
				if (desktop.getGexManager().getCurrentGex().getUsedDatasources().contains(BioDataSource.ENSEMBL))
				{
					JOptionPane.showMessageDialog(desktop.getFrame(), 
							"The selected data set contains old references to Ensembl\n" +
							"It is recommended that you redo the dataset import process");					
				}
			}
			catch (IDMapperException ex)
			{
				//ignore - no compatibility check today
			}
		}
		
	}

}
