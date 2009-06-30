package org.pathvisio.cytoscape.superpathways;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;


public class SimpleCaseMergeTest {
	
	public SimpleCaseMergeTest(){

		// create cyNetwork1
		CyNetwork cyNetwork1 = Cytoscape.createNetwork("network1", false);

		CyNode node0 = Cytoscape.getCyNode("rain", true);
		CyNode node1 = Cytoscape.getCyNode("rainbow", true);
		CyNode node2 = Cytoscape.getCyNode("rabbit", true);
		CyNode node3 = Cytoscape.getCyNode("yellow", true);

		cyNetwork1.addNode(node0);
		cyNetwork1.addNode(node1);
		cyNetwork1.addNode(node2);
		cyNetwork1.addNode(node3);

		CyEdge edge0 = Cytoscape.getCyEdge(node0, node1,
				Semantics.INTERACTION, "pp", true);
		CyEdge edge1 = Cytoscape.getCyEdge(node0, node2,
				Semantics.INTERACTION, "pp", true);
		CyEdge edge2 = Cytoscape.getCyEdge(node0, node3,
				Semantics.INTERACTION, "pp", true);
		cyNetwork1.addEdge(edge0);
		cyNetwork1.addEdge(edge1);
		cyNetwork1.addEdge(edge2);

		// remove a node
		// cyNetwork.removeNode(node1.getRootGraphIndex(), true);
		// Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null,
		// cyNetwork);

		// destroy the network
		// Cytoscape.destroyNetwork(cyNetwork);
		// Cytoscape.firePropertyChange(Cytoscape.NETWORK_DESTROYED,
		// cyNetwork, null);

		// Where nodeID was the String id of the node in question,
		// shapeNames is
		// a string representing the desired shape: "Triangle", etc.
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		nodeAtts.setAttribute("yellow", "node.fillColor", "153,255,51");
		nodeAtts.setAttribute("yellow", "node.shape", "Diamond"); // "Triangle");

		// set the Xref attribute to the nodes of cyNetwork1
		nodeAtts.setAttribute("yellow", "Xref", "EnRn:ENSRNOG00000010756");

		nodeAtts.setAttribute("rain", "Xref", "L:314856");

		nodeAtts.setAttribute("rainbow", "Xref", "EnRn:ENSRNOG00000009696");

		nodeAtts.setAttribute("rabbit", "Xref", "L:25163");

		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);

		CyAttributes edgeAtts = Cytoscape.getEdgeAttributes();
		edgeAtts.setAttribute(edge0.getIdentifier(), "edge.label", "6");
		// edgeAtts.setAttribute("yellow","node.shape","Diamond");

		Cytoscape.createNetworkView(cyNetwork1);

		// create cyNetwork2
		CyNetwork cyNetwork2 = Cytoscape.createNetwork("network2", false);

		CyNode node20 = Cytoscape.getCyNode("chick", true);
		CyNode node21 = Cytoscape.getCyNode("wolf", true);
		CyNode node22 = Cytoscape.getCyNode("bear", true);
		CyNode node23 = Cytoscape.getCyNode("flower", true);

		cyNetwork2.addNode(node20);
		cyNetwork2.addNode(node21);
		cyNetwork2.addNode(node22);
		cyNetwork2.addNode(node23);

		CyEdge edge20 = Cytoscape.getCyEdge(node20, node21,
				Semantics.INTERACTION, "pp", true);
		CyEdge edge21 = Cytoscape.getCyEdge(node20, node22,
				Semantics.INTERACTION, "pp", true);
		CyEdge edge22 = Cytoscape.getCyEdge(node20, node23,
				Semantics.INTERACTION, "pp", true);
		cyNetwork2.addEdge(edge20);
		cyNetwork2.addEdge(edge21);
		cyNetwork2.addEdge(edge22);

	
		CyAttributes nodeAtts2 = Cytoscape.getNodeAttributes();
		nodeAtts2.setAttribute("chick", "node.fillColor", "255,0, 251");
		nodeAtts2.setAttribute("chick", "node.shape", "Triangle"); // "Triangle");

		// set the Xref attribute to the nodes of cyNetwork2
		nodeAtts2.setAttribute("chick", "Xref", "EnRn:ENSRNOG00000010756");

		nodeAtts2.setAttribute("wolf", "Xref", "EnRn:ENSRNOG00000006304");

		nodeAtts2.setAttribute("bear", "Xref", "EnRn:ENSRNOG00000003084");

		nodeAtts2.setAttribute("flower", "Xref", "Cp:5743");

		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);

		CyAttributes edgeAtts2 = Cytoscape.getEdgeAttributes();
		edgeAtts2.setAttribute(edge20.getIdentifier(), "edge.label", "8");
		// edgeAtts.setAttribute("yellow","node.shape","Diamond");

		Cytoscape.createNetworkView(cyNetwork2);

		
		//use the class PathwaysMerge to merge the above two pathways
		List<CyNetwork> nets = new ArrayList<CyNetwork>();
		nets.add(cyNetwork1);
		nets.add(cyNetwork2);

		PathwaysMerge pMerge=new PathwaysMerge();
		
		CyNetwork mergedNetwork = pMerge.mergeNetwork(nets, "Merged Network");
		Cytoscape.createNetworkView(cyNetwork2);

	}
}


