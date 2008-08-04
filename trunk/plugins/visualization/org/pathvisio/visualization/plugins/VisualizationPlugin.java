// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import org.pathvisio.data.GexManager;
import org.pathvisio.data.GexManager.GexManagerEvent;
import org.pathvisio.data.GexManager.GexManagerListener;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationManager;
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

	public void init() {
		//Register the visualization methods
		VisualizationMethodRegistry reg = VisualizationMethodRegistry.getCurrent();
		reg.registerMethod(
				ColorByExpression.class.toString(), 
				new VisualizationMethodProvider() {
					public VisualizationMethod create(Visualization v, String registeredName) {
						return new ColorByExpression(v, registeredName);
					}
			}
		);
		reg.registerMethod(
				TextByExpression.class.toString(), 
				new VisualizationMethodProvider() {
					public VisualizationMethod create(Visualization v, String registeredName) {
						return new TextByExpression(v, registeredName);
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
		SwingEngine.getCurrent().registerMenuAction ("Data", new VisualizationAction(
				SwingEngine.getCurrent().getApplicationPanel())
		);
	}

	public static class VisualizationAction extends AbstractAction implements GexManagerListener {
		MainPanel mainPanel;
		
		public VisualizationAction(MainPanel mainPanel) {
			putValue(NAME, "Visualization options");
			this.mainPanel = mainPanel;
			setEnabled(GexManager.getCurrent().isConnected());
			GexManager.getCurrent().addListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			new VisualizationDialog(
					VisualizationManager.getCurrent(),
					SwingEngine.getCurrent().getFrame(),
					mainPanel
			).setVisible(true);
		}

		public void gexManagerEvent(GexManagerEvent e) 
		{
			boolean isConnected = GexManager.getCurrent().isConnected();
			Logger.log.trace("Visualization options action, gexmanager event, connected: " + isConnected);
			setEnabled(isConnected);
		}
	}
}
