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

import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.Semantics;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyMenus;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;


public class SuperpathwaysPlugin extends CytoscapePlugin {
	

	private static SuperpathwaysPlugin instance;
	
	/**
	 * Can be used by other plugins to get an instance of the SuperpathwaysPlugin.
	 * @return The instance of SuperpathwaysPlugin, or null if the plugin wasn't initialized
	 * yet by the PluginManager.
	 */
	public static SuperpathwaysPlugin getInstance() {
		return instance;
	}
	
	/**
	 * Initializes the SuperpathwaysPlugin. Should only be called by Cytoscape's plugin manager!
	 * 
	 * Only one instance of this class is allowed, but this constructor can't be made 
	 * private because it's need by the Cytoscape plugin mechanism.
	 */
	public SuperpathwaysPlugin() {
		if(instance != null) {
			throw new RuntimeException("SuperpathwaysPlugin is already instantiated! Use static" +
					" method getInstance instead!");
		}
		instance = this;

		
		 //create a new action to respond to menu activation
		SuperpathwaysSelectionAction action = new SuperpathwaysSelectionAction();
        //set the preferred menu
        action.setPreferredMenu("Plugins");
        //and add it to the menus
        Cytoscape.getDesktop().getCyMenus().addAction(action);
		
	}
	
	 public class SuperpathwaysSelectionAction extends CytoscapeAction {
	        
	        /**
	         * The constructor sets the text that should appear on the menu item.
	         */
	        public SuperpathwaysSelectionAction() {super("Superpathways");}
	        
	        /**
	         * This method is called when the user selects the menu item.
	         */
	        public void actionPerformed(ActionEvent ae) {
	            //get the network object; this contains the graph
	            CyNetwork network = Cytoscape.getCurrentNetwork();
	            //get the network view object
	            CyNetworkView view = Cytoscape.getCurrentNetworkView();
	            //can't continue if either of these is null
	            if (network == null || view == null) {return;}
	            //put up a dialog if there are no selected nodes
	            if (view.getSelectedNodes().size() == 0) {
	                JOptionPane.showMessageDialog(view.getComponent(),
	                        "Please select one or more nodes.");
	            }
	            
	            //a container to hold the objects we're going to select
	            Set nodeViewsToSelect = new HashSet();
	            //iterate over every node view
	            for (Iterator i = view.getSelectedNodes().iterator(); i.hasNext(); ) {
	                NodeView nView = (NodeView)i.next();
	                //first get the corresponding node in the network
	                CyNode node = (CyNode)nView.getNode();
	                // get the neighbors of that node
	                List neighbors = network.neighborsList(node);
	                // and iterate over the neighbors
	                for (Iterator ni = neighbors.iterator(); ni.hasNext(); ) {
	                    CyNode neighbor = (CyNode)ni.next();
	                    // get the view on this neighbor
	                    NodeView neighborView = view.getNodeView(neighbor);
	                    //and add that view to our container of objects to select
	                    nodeViewsToSelect.add(neighborView);
	                }
	            }
	            //now go through our container and select each view
	            for (Iterator i = nodeViewsToSelect.iterator(); i.hasNext(); ) {
	                NodeView nView = (NodeView)i.next();
	                nView.setSelected(true);
	            }
	            //tell the view to redraw since we've changed the selection
	            view.redrawGraph(false, true);
	        }
	        
	    }

}

