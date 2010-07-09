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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.cytoscape.GpmlConverter;
import org.pathvisio.cytoscape.GpmlHandler;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;
import org.pathvisio.preferences.GlobalPreference;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.webservice.WebServiceClientManager;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyMenus;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;

public class SuperpathwaysPlugin extends CytoscapePlugin {
	//private static final int WINDOW_WIDTH = 400;
	//private static final int WINDOW_HEIGHT = 500;

	GpmlHandler mGpmlHandler;
	JFrame mWindow; 
	private static SuperpathwaysPlugin mInstance;
    SuperpathwaysGui mSpGui;
	
    private String idmLocation = GlobalPreference.getDataDir().toString()
		+ "/gene databases/";
    private Map<Organism, IDMapper> idMappers = new HashMap<Organism, IDMapper>();
	
	public static SuperpathwaysPlugin getInstance() {
		return mInstance;
	}

	public SuperpathwaysPlugin() {
		BioDataSource.init();
		
		if (mInstance != null) {
			throw new RuntimeException(
					"SuperpathwaysPlugin is already instantiated! Use static"
							+ " method getInstance instead!");
		}

		mInstance = this;
		Logger.log.setLogLevel(true, false, true, true, true, true);
		mGpmlHandler = new GpmlHandler();
		
		

		SuperpathwaysPlugin spPlugin = SuperpathwaysPlugin.getInstance();
		SuperpathwaysClient spClient = new SuperpathwaysClient(spPlugin);
		WebServiceClientManager.registerClient(spClient);
		mSpGui = spClient.getGUI();
		//mSpGui.setLocationRelativeTo(Cytoscape.getDesktop());
		
		mWindow = new JFrame("Superpathways Plugin");
		//mWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		mWindow.add(mSpGui);
		mWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		SuperpathwaysAction action = new SuperpathwaysAction();
		action.setPreferredMenu("Plugins");

		CytoscapeDesktop desktop = Cytoscape.getDesktop();
		CyMenus menu = desktop.getCyMenus();
		menu.addAction(action);


	}

	public IDMapper getIDMapper(Organism organism) throws ClassNotFoundException, IDMapperException {
		Class.forName ("org.bridgedb.rdb.IDMapperRdb");
		
		IDMapper idm = idMappers.get(organism);
		if(idm == null) {
			IDMapperStack idms = new IDMapperStack();
			idm = idms;
			idMappers.put(organism, idm);
			
			//Try to find the available bridgedb databases, ask for location if necessary
			File[] idmFiles = new File(idmLocation).listFiles();
			boolean speciesFound = false;
			
			if(idmFiles != null) for(int i = 0; i < idmFiles.length; i++) {
				File file = idmFiles[i];
				if (file.isDirectory()) continue; // skip directories
				String fileName = file.getName();
				int index = fileName.indexOf("_");
				if (index < 0) continue; // Skip this file, not the pgdb naming scheme
				String prefix = fileName.substring(0, index);
				// System.out.println(speciesOrMetabolite);
				if (prefix.equals(organism.code())) {
					Logger.log.info("Connecting to idmapper " + file);
					idms.addIDMapper("idmapper-pgdb:" + file);
					speciesFound = true;
				} else if("metabolites".equals(prefix)) {
					Logger.log.info("Connecting to idmapper " + file);
					idms.addIDMapper("idmapper-pgdb:" + file);
				}
			}

			if (!speciesFound) {
				JOptionPane
						.showMessageDialog(
								mWindow,
								"Could not find bridgedb database for " + organism.latinName() + " in " + idmFiles + ". " +
										"\nPlease download the database from http://bridgedb.org and " +
										"point me to its location.");
	
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser
						.setDialogTitle("Choose the bridgedb database for " + organism.latinName());
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (chooser.showOpenDialog(mWindow) == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					if(file != null) {
						Logger.log.info("Connecting to idmapper " + file);
						idms.addIDMapper("idmapper-pgdb:" + file);
						idmLocation = file.getParent();
					}
				}
			}
		}
		return idm;
	}
	
	/*
	 * public GpmlHandler getGpmlHandler() { return mGpmlHandler; }
	 */

	public CyNetwork load(Pathway p, boolean newNetwork) {
		try {
			GpmlConverter converter = new GpmlConverter(mGpmlHandler, p);

			// Get the nodes/edges indexes
			int[] nodes = converter.getNodeIndicesArray();
			//System.out.println("There are " + nodes.length + " nodes in the pathway!");
			int[] edges = converter.getEdgeIndicesArray();
			//System.out.println("There are " + edges.length + " edges in the pathway!");
			
			
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
		return m;
	}

	public static double vToM(double v) {
		return v;
	}

	/**
	 * This class gets attached to the menu item.
	 */
	public class SuperpathwaysAction extends CytoscapeAction {
		
		public SuperpathwaysAction() {
			super("Superpathways");
		}
		
		public void actionPerformed(ActionEvent arg0) {
			mWindow.add(mSpGui);
			mWindow.setLocationRelativeTo(Cytoscape.getDesktop());
			mWindow.setVisible(true);
			mWindow.pack();
			
			
		}

	}

}
