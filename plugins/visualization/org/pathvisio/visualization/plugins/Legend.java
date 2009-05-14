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
package org.pathvisio.visualization.plugins;


import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.visualization.VisualizationEvent;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationManager.VisualizationListener;
import org.pathvisio.visualization.colorset.ColorSetManager;

/**
* This class shows a legend for the currently loaded visualization and color-sets.
*/
public class Legend extends JPanel implements VisualizationListener {
	
	final ColorSetManager colorSetManager;
	final VisualizationManager visualizationManager;
	
	public Legend(PvDesktop desktop)
	{
		visualizationManager = desktop.getVisualizationManager();
		visualizationManager.addListener(this);
		colorSetManager = visualizationManager.getColorSetManager();
		createContents();
		rebuildContent();
	}

	/**
	 * Rebuild the contents of the legend (refresh the names
	 * in colorSetCombo and refresh the content)
	 */
	public void rebuildContent() 
	{
		refreshContent();
	}
	
	/**
	 * Refresh the content of the legend
	 */
	void refreshContent() 
	{		
	}

	/**
	 * Create the contents of the legend
	 */
	void createContents() 
	{	
	}

	public void visualizationEvent(final VisualizationEvent e) 
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				switch(e.getType()) 
				{
					case VisualizationEvent.VISUALIZATION_MODIFIED:
						rebuildContent();
						break;
					default:
						refreshContent();
				}
			}
		});
	}

}