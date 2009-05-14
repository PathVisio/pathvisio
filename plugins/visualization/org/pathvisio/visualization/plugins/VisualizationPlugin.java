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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.pathvisio.debug.Logger;
import org.pathvisio.gex.GexManager.GexManagerEvent;
import org.pathvisio.gex.GexManager.GexManagerListener;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationMethod;
import org.pathvisio.visualization.VisualizationMethodProvider;
import org.pathvisio.visualization.VisualizationMethodRegistry;
import org.pathvisio.visualization.gui.VisualizationDialog;

/**
 * Plugin that registers several visualization methods
 * @author thomas
 *
 */
public class VisualizationPlugin implements Plugin {

	public void init(PvDesktop aDesktop) 
	{
		final PvDesktop desktop = aDesktop;
		
		//Register the visualization methods
		VisualizationMethodRegistry reg = 
			aDesktop.getVisualizationMethodRegistry();

		reg.registerMethod(
				ColorByExpression.class.toString(), 
				new VisualizationMethodProvider() {
					public VisualizationMethod create(Visualization v, String registeredName) {
						return new ColorByExpression(v, registeredName, desktop.getGexManager());
					}
			}
		);
		reg.registerMethod(
				TextByExpression.class.toString(), 
				new VisualizationMethodProvider() {
					public VisualizationMethod create(Visualization v, String registeredName) {
						return new TextByExpression(v, registeredName, desktop.getGexManager());
					}
			}
		);
		reg.registerMethod(
				DataNodeLabel.class.toString(), 
				new VisualizationMethodProvider() {
					public VisualizationMethod create(Visualization v, String registeredName) {
						return new DataNodeLabel(v, registeredName);
					}
			}
		);
		//Register the menu items
		desktop.registerMenuAction ("Data", new VisualizationAction(
				aDesktop)
		);
		
		Legend legendPane = new Legend(desktop);
		desktop.getSideBarTabbedPane().addTab ("Legend", legendPane);
	}

	/**
	 * Action / Menu item for opening the visualization dialog
	 */
	public static class VisualizationAction extends AbstractAction implements GexManagerListener {
		private static final long serialVersionUID = 1L;
		MainPanel mainPanel;
		private final PvDesktop ste;
		
		public VisualizationAction(PvDesktop ste) 
		{
			this.ste = ste;
			putValue(NAME, "Visualization options");
			this.mainPanel = ste.getSwingEngine().getApplicationPanel();
			setEnabled(ste.getGexManager().isConnected());
			ste.getGexManager().addListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			new VisualizationDialog(
					ste.getVisualizationManager(),
					ste.getSwingEngine().getFrame(),
					mainPanel
			).setVisible(true);
		}

		public void gexManagerEvent(GexManagerEvent e) 
		{
			boolean isConnected = ste.getGexManager().isConnected();
			Logger.log.trace("Visualization options action, gexmanager event, connected: " + isConnected);
			setEnabled(isConnected);
		}
	}
}
