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
package org.pathvisio.visualization.plugins;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.gex.GexManager.GexManagerEvent;
import org.pathvisio.desktop.gex.GexManager.GexManagerListener;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.desktop.visualization.Visualization;
import org.pathvisio.desktop.visualization.VisualizationEvent;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.desktop.visualization.VisualizationMethod;
import org.pathvisio.desktop.visualization.VisualizationMethodProvider;
import org.pathvisio.desktop.visualization.VisualizationMethodRegistry;
import org.pathvisio.gui.MainPanel;
import org.pathvisio.visualization.gui.VisualizationDialog;

/**
 * Plugin that registers several visualization methods
 */
public class VisualizationPlugin implements Plugin
{
	private JComboBox visualizationCombo;
	private PvDesktop desktop;
	private VisualizationComboModel model;

	public void init(PvDesktop aDesktop)
	{
		desktop = aDesktop;

		//Register the visualization methods
		VisualizationMethodRegistry reg =
			aDesktop.getVisualizationManager().getVisualizationMethodRegistry();

		reg.registerMethod(
				LegendVisualization.class.toString(),
				new VisualizationMethodProvider() {
					public VisualizationMethod create() {
						return new LegendVisualization(
								desktop.getVisualizationManager().getColorSetManager(),
								desktop.getSwingEngine().getEngine());
					}
			}
		);
		reg.registerMethod(
				ColorByExpression.class.toString(),
				new VisualizationMethodProvider() {
					public VisualizationMethod create() {
						return new ColorByExpression(desktop.getGexManager(), 
								desktop.getVisualizationManager().getColorSetManager());
					}
			}
		);
		reg.registerMethod(
				TextByExpression.class.toString(),
				new VisualizationMethodProvider() {
					public VisualizationMethod create() {
						return new TextByExpression(desktop.getGexManager());
					}
			}
		);
		reg.registerMethod(
				DataNodeLabel.class.toString(),
				new VisualizationMethodProvider() {
					public VisualizationMethod create() {
						return new DataNodeLabel();
					}
			}
		);
		//Register the menu items
		desktop.registerMenuAction ("Data", new VisualizationAction(
				aDesktop)
		);

		// combo box in toolbar to select visualization
 		model = new VisualizationComboModel(desktop.getVisualizationManager());
		visualizationCombo = new JComboBox(model);
		desktop.getSwingEngine().getApplicationPanel().addToToolbar(visualizationCombo);

		LegendPanel legendPane = new LegendPanel(desktop.getVisualizationManager());
		JTabbedPane tabPane = desktop.getSideBarTabbedPane();
		if(tabPane != null) {
			tabPane.addTab ("Legend", legendPane);
		}
	}

	public void done()
	{
		model.dispose();
	};


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
			setEnabled(isConnected);
		}
	}

	/**
	 * Model for ComboBox in toolbar, that selects you one of the
	 * visualizations contained in VisualizationManager, or "No Visualization"
	 */
	private static class VisualizationComboModel extends AbstractListModel
		implements ComboBoxModel, VisualizationManager.VisualizationListener
	{
		private static final String NO_VISUALIZATION = "No Visualization";
		private final VisualizationManager manager;
		VisualizationComboModel (VisualizationManager manager)
		{
			this.manager = manager;
			manager.addListener(this);
		}

		/**
		 * Call this to unregister from VisualizationManager
		 */
		public void dispose()
		{
			manager.removeListener(this);
		}

		public void visualizationEvent(VisualizationEvent e)
		{
			switch (e.getType())
			{
			case VisualizationEvent.VISUALIZATION_ADDED:
				fireIntervalAdded(this, 0, manager.getVisualizations().size() + 1);
				//TODO: smaller interval?
				break;
			case VisualizationEvent.VISUALIZATION_REMOVED:
				fireIntervalRemoved(this, 0, manager.getVisualizations().size() + 1);
				//TODO: smaller interval?
				break;
			case VisualizationEvent.VISUALIZATION_MODIFIED:
				fireContentsChanged(this, 0, manager.getVisualizations().size() + 1);
				//TODO: smaller interval?
				break;
			case VisualizationEvent.VISUALIZATION_SELECTED:
				fireContentsChanged(this, 0, manager.getVisualizations().size() + 1);
				break;
			}
		}

		public Object getSelectedItem()
		{
			Object result = manager.getActiveVisualization();
			if (result == null)
			{
				result = NO_VISUALIZATION;
			}
			return result;
		}

		public void setSelectedItem(Object arg0)
		{
			if (arg0 instanceof Visualization)
				manager.setActiveVisualization((Visualization)arg0);
			else
				manager.setActiveVisualization(-1);
		}

		public Object getElementAt(int arg0)
		{
			if (arg0 == 0)
			{
				return NO_VISUALIZATION;
			}
			else
			{
				return manager.getVisualizations().get(arg0-1);
			}
		}

		public int getSize() {
			return manager.getVisualizations().size() + 1;
		}
	}
}
