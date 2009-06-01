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

package org.pathvisio.cytoscape.superpathways;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.pathvisio.cytoscape.GpmlConverter;
import org.pathvisio.cytoscape.GpmlHandler;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.GpmlFormatImpl1;
import org.pathvisio.model.Pathway;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.webservice.WebServiceClientManager;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyMenus;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;

public class SuperpathwaysPlugin extends CytoscapePlugin {
	private static final int WINDOW_WIDTH = 400;
	private static final int WINDOW_HEIGHT = 500;

	GpmlHandler gpmlHandler;
	//JFrame mWindow; 
	private static SuperpathwaysPlugin instance;
    SuperpathwaysGui spGui;
	
	/**
	 * Can be used by other plugins to get an instance of the
	 * SuperpathwaysPlugin.
	 * 
	 * @return The instance of SuperpathwaysPlugin, or null if the plugin wasn't
	 *         initialized yet by the PluginManager.
	 */
	public static SuperpathwaysPlugin getInstance() {
		return instance;
	}

	/**
	 * Initializes the SuperpathwaysPlugin. Should only be called by Cytoscape's
	 * plugin manager!
	 * 
	 * Only one instance of this class is allowed, but this constructor can't be
	 * made private because it's need by the Cytoscape plugin mechanism.
	 */
	public SuperpathwaysPlugin() {
		if (instance != null) {
			throw new RuntimeException(
					"SuperpathwaysPlugin is already instantiated! Use static"
							+ " method getInstance instead!");
		}

		instance = this;
		Logger.log.setLogLevel(true, false, true, true, true, true);

		gpmlHandler = new GpmlHandler();

		SuperpathwaysAction action = new SuperpathwaysAction();
		action.setPreferredMenu("Plugins");

		CytoscapeDesktop desktop = Cytoscape.getDesktop();
		CyMenus menu = desktop.getCyMenus();
		menu.addAction(action);

		SuperpathwaysPlugin spPlugin = SuperpathwaysPlugin.getInstance();
		SuperpathwaysClient spClient = new SuperpathwaysClient(spPlugin);

		WebServiceClientManager.registerClient(spClient);
		spGui = spClient.getGUI();
		spGui.setLocationRelativeTo(Cytoscape.getDesktop());
		//mWindow = new JFrame("Superpathways Plugin");
		//mWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		//mWindow.add(spGui);
		//mWindow.setLocationRelativeTo(Cytoscape.getDesktop());

	}

	/*
	 * public GpmlHandler getGpmlHandler() { return gpmlHandler; }
	 */

	public CyNetwork load(Pathway p, boolean newNetwork) {
		try {
			GpmlConverter converter = new GpmlConverter(gpmlHandler, p);

			// Get the nodes/edges indexes
			int[] nodes = converter.getNodeIndicesArray();
			int[] edges = converter.getEdgeIndicesArray();

			// Get the current network, or create a new one, if none is
			// available
			CyNetwork network = Cytoscape.getCurrentNetwork();
			if (newNetwork || network == Cytoscape.getNullNetwork()) {
				String title = converter.getPathway().getMappInfo()
						.getMapInfoName();
				network = Cytoscape.createNetwork(title == null ? "new network"
						: title, false);
			}

			// Add all nodes and edges to the network
			for (int nd : nodes) {
				network.addNode(nd);
			}
			for (int ed : edges)
				network.addEdge(ed);

			CyNetworkView view = Cytoscape.getNetworkView(network
					.getIdentifier());
			if (view == Cytoscape.getNullNetworkView()) {
				view = Cytoscape.createNetworkView(network);
				Cytoscape.firePropertyChange(
						CytoscapeDesktop.NETWORK_VIEW_FOCUS, null, view
								.getIdentifier());
			} else {
				view = Cytoscape.getCurrentNetworkView();
			}
			converter.layout(view);
			view.redrawGraph(true, false);

			return network;
		} catch (Exception ex) {
			Logger.log.error("Error while importing GPML", ex);
			JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
					"Error while importing GPML: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public static double mToV(double m) {
		return m * 1.0 / 15; // Should be stored in the model somewhere
								// (pathvisio)
	}

	public static double vToM(double v) {
		return v * 15.0;
	}

	/**
	 * This class gets attached to the menu item.
	 */
	public class SuperpathwaysAction extends CytoscapeAction {
		/**
		 * The constructor sets the text that should appear on the menu item.
		 */
		public SuperpathwaysAction() {
			super("Superpathways");
		}
		/**
		 * This method is called when the user selects the menu item.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			spGui.setVisible(true);
			
			
		}

	}

}
