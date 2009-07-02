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

import giny.model.Edge;
import giny.model.GraphObject;
import giny.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.task.TaskMonitor;

public class PathwaysMerge {
	protected TaskMonitor taskMonitor;

	protected boolean interrupted; // to enable cancel of the network merge
									// operation

	public PathwaysMerge() {

		// status = "Merging networks... 0%";
		taskMonitor = null;
		interrupted = false;
	}

	public void interrupt() {
		interrupted = true;
	}

	public void setTaskMonitor(final TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
	}

	public CyNetwork mergeNetwork(final List<CyNetwork> networks, final String title) {
		if (networks == null || title == null) {
			throw new java.lang.NullPointerException();
		}

		if (networks.isEmpty()) {
			throw new java.lang.IllegalArgumentException("No merging network");
		}

		if (title.length() == 0) {
			throw new java.lang.IllegalArgumentException("Empty title");
		}
		// get node matching list
		List<Map<CyNetwork, Set<GraphObject>>> matchedNodeList = getMatchedList(
				networks, true);

		final Map<Node, Node> mapNN = new HashMap<Node, Node>();
		// save information on mapping from original nodes to merged nodes
		// to use when merge edges

		// merge nodes in the list
		final int nNode = matchedNodeList.size();
		List<Node> nodes = new Vector<Node>(nNode);
		for (int i = 0; i < nNode; i++) {

			if (interrupted) return null;
            updateTaskMonitor("Merging nodes...\n"+i+"/"+nNode,(i+1)*100/nNode);
            
            
			final Map<CyNetwork, Set<GraphObject>> mapNetNode = matchedNodeList
					.get(i);
			final Node node = mergeNode(mapNetNode);
			nodes.add(node);

			final Iterator<Set<GraphObject>> itNodes = mapNetNode.values()
					.iterator();
			while (itNodes.hasNext()) {
				final Set<GraphObject> nodes_ori = itNodes.next();
				final Iterator<GraphObject> itNode = nodes_ori.iterator();
				while (itNode.hasNext()) {
					final Node node_ori = (Node) itNode.next();
					mapNN.put(node_ori, node);
				}
			}
		}
		
		 updateTaskMonitor("Merging nodes completed",100);

		// match edges
		List<Map<CyNetwork, Set<GraphObject>>> matchedEdgeList = getMatchedList(
				networks, false);
		// merge edges
		final int nEdge = matchedEdgeList.size();
		final List<Edge> edges = new Vector<Edge>(nEdge);
		for (int i = 0; i < nEdge; i++) {

			if (interrupted) return null;
            updateTaskMonitor("Merging edges...\n"+i+"/"+nEdge,(i+1)*100/nEdge);
			
			final Map<CyNetwork, Set<GraphObject>> mapNetEdge = matchedEdgeList
					.get(i);
			// get the source and target nodes in merged network
			final Iterator<Set<GraphObject>> itEdges = mapNetEdge.values().iterator();

			final Set<GraphObject> edgeSet = itEdges.next();
			if (edgeSet == null || edgeSet.isEmpty()) {
				throw new java.lang.IllegalStateException(
						"Null or empty edge set");
			}

			final Edge edge_ori = (Edge) edgeSet.iterator().next();
			final Node source = mapNN.get(edge_ori.getSource());
			final Node target = mapNN.get(edge_ori.getTarget());
			if (source == null || target == null) { // some of the node may
				// be exluded when
				// intersection or
				// difference
				continue;
			}

			final boolean directed = edge_ori.isDirected();
			final CyAttributes attributes = Cytoscape.getEdgeAttributes();
			final String interaction = (String) attributes.getAttribute(
					edge_ori.getIdentifier(), Semantics.INTERACTION);

			final Edge edge = mergeEdge(mapNetEdge, source, target,
					interaction, directed);
			edges.add(edge);
		}
		
		updateTaskMonitor("Merging edges completed",100);

		// create new network
		final CyNetwork network = Cytoscape.createNetwork(nodes, edges, title);
		
		updateTaskMonitor("Successfully merged the selected "+networks.size()+" networks into network "+title+" with "+nNode+" nodes and "+nEdge+" edges",100);

		return network;
	}

	/**
	 * Get a list of matched nodes/edges
	 * 
	 * @param networks
	 *            Networks to be merged
	 * 
	 * @return list of map from network to node/edge
	 */
	protected List<Map<CyNetwork, Set<GraphObject>>> getMatchedList(
			final List<CyNetwork> networks, final boolean isNode) {
		if (networks == null) {
			throw new java.lang.NullPointerException();
		}

		if (networks.isEmpty()) {
			throw new java.lang.IllegalArgumentException("No merging network");
		}

		final List<Map<CyNetwork, Set<GraphObject>>> matchedList = new Vector<Map<CyNetwork, Set<GraphObject>>>();

		final int nNet = networks.size();

        // Get the total number nodes/edge to calculate the status
        int totalGO=0, processedGO=0;
        for (int i=0; i<nNet; i++) {
            final CyNetwork net = networks.get(i);
            totalGO += isNode?net.getNodeCount():net.getEdgeCount();
        }
		
	

		for (int i = 0; i < nNet; i++) {

			final CyNetwork net1 = networks.get(i);
			final Iterator<GraphObject> it;
			if (isNode) {
				it = net1.nodesIterator();
			} else { // edge
				it = net1.edgesIterator();
			}

			while (it.hasNext()) {
				 if (interrupted) return null;
	                updateTaskMonitor("Matching "+(isNode?"nodes":"edges")+"...\n"+processedGO+"/"+totalGO,processedGO*100/totalGO);
	                processedGO++;
	                
	                

				final GraphObject go1 = it.next();

				// chech whether any nodes in the matchedNodeList match with
				// this node
				// if yes, add to the list, else add a new map to the list
				boolean matched = false;
				final int n = matchedList.size();
				int j = 0;
				for (; j < n; j++) {
					final Map<CyNetwork, Set<GraphObject>> matchedGO = matchedList
							.get(j);
					final Iterator<CyNetwork> itNet = matchedGO.keySet()
							.iterator();
					while (itNet.hasNext()) {
						final CyNetwork net2 = itNet.next();
						// if (net1==net2) continue; // assume the same
						// network don't have nodes match to each other
						if (net1 == net2)
							continue;

						final Set<GraphObject> gos2 = matchedGO.get(net2);
						if (gos2 != null) {
							GraphObject go2 = gos2.iterator().next(); // since
							// there is only one node in the map
							if (isNode) { // NODE
								matched = matchNode(net1, (Node) go1, net2,
										(Node) go2);
							} else {// EDGE
								matched = matchEdge(net1, (Edge) go1, net2,
										(Edge) go2);
							}
							if (matched) {
								Set<GraphObject> gos1 = matchedGO.get(net1);
								if (gos1 == null) {
									gos1 = new HashSet<GraphObject>();
									matchedGO.put(net1, gos1);
								}
								gos1.add(go1);
								break;
							}
						}
					}
					if (matched) {
						break;
					}
				}
				if (!matched) { // no matched node found, add new map to the
					// list
					final Map<CyNetwork, Set<GraphObject>> matchedGO = new HashMap<CyNetwork, Set<GraphObject>>();
					Set<GraphObject> gos1 = new HashSet<GraphObject>();
					gos1.add(go1);
					matchedGO.put(net1, gos1);
					matchedList.add(matchedGO);
				}

			}

		}
		updateTaskMonitor("Matching "+(isNode?"nodes":"edges")+" completed",100);
		return matchedList;
	}

	public boolean matchEdge(final CyNetwork net1, Edge e1,
			final CyNetwork net2, Edge e2) {
		if (net1 == null || e1 == null || e2 == null) {
			throw new java.lang.NullPointerException();
		}

		// TODO should interaction be considered or not?
		final CyAttributes attributes = Cytoscape.getEdgeAttributes();

		Object i1 = attributes.getAttribute(e1.getIdentifier(),
				Semantics.INTERACTION);
		Object i2 = attributes.getAttribute(e2.getIdentifier(),
				Semantics.INTERACTION);

		if ((i1 == null && i2 != null) || (i1 != null && i2 == null)) {
			return false;
		}

		if (i1 != null && !i1.equals(i2))
			return false;

		if (e1.isDirected()) { // directed
			if (!e2.isDirected())
				return false;
			return matchNode(net1, e1.getSource(), net2, e2.getSource())
					&& matchNode(net1, e1.getTarget(), net2, e2.getTarget());
		} else { // non directed
			if (e2.isDirected())
				return false;
			if (matchNode(net1, e1.getSource(), net2, e2.getSource())
					&& matchNode(net1, e1.getTarget(), net2, e2.getTarget()))
				return true;
			if (matchNode(net1, e1.getSource(), net2, e2.getTarget())
					&& matchNode(net1, e1.getTarget(), net2, e2.getSource()))
				return true;
			return false;
		}
	}

	public boolean matchNode(CyNetwork net1, Node n1, CyNetwork net2, Node n2) {
		// boolean result=false;
		if (net1 == null || n1 == null || n2 == null || net2 == null) {
			throw new java.lang.NullPointerException();
		}
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		Object Xref1 = nodeAtts.getAttribute(n1.getIdentifier(), "Xref");
		Object Xref2 = nodeAtts.getAttribute(n2.getIdentifier(), "Xref");

		if (Xref1.equals(Xref2)) {
			return true;
		} else {
			return false;
			//DataSource dataSourceNode1=(Xref)Xref1.getDataSource();
		}

		// need to add the idmapping result later

	}

	public Node mergeNode(final Map<CyNetwork, Set<GraphObject>> mapNetNode) {

		if (mapNetNode == null || mapNetNode.isEmpty()) {
			return null;
		}

		// Assign ID and canonicalName in resulting network
		// remove in Cytoscape3
		final Iterator<Set<GraphObject>> itNodes = mapNetNode.values()
				.iterator();
		Set<GraphObject> nodes = new HashSet<GraphObject>(); // 'nodes'
		// will
		// contains
		// all the
		// matched
		// nodes
		while (itNodes.hasNext()) {
			nodes.addAll(itNodes.next());
		}

		final Iterator<GraphObject> itNode = nodes.iterator();
		String id = new String(itNode.next().getIdentifier());
		
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		String XrefOfOneNode=(String)nodeAtts.getAttribute(id, "Xref");

		if (nodes.size() > 1) { // if more than 1 nodes to be merged, assign the
			// id as the combination of all identifiers
			while (itNode.hasNext()) {
				final Node node = (Node) itNode.next();
				id += "_" + node.getIdentifier();
			}

			// if node with this id exist, get new one
			String appendix = "";
			int app = 0;
			while (Cytoscape.getCyNode(id + appendix) != null) {
				appendix = "" + ++app;
			}
			id += appendix;
		}

		// Get the node with id or create a new node
		// for attribute confilict handling, introduce a conflict node here?
		final Node node = Cytoscape.getCyNode(id, true);

		// set other attributes as indicated in attributeMapping
		// setAttribute(id,mapNetNode,nodeAttributeMapping);
		
		nodeAtts.setAttribute(id,"Xref",XrefOfOneNode);
		if (nodes.size() > 1) {
			nodeAtts.setAttribute(id, "MergeIndicator", "Yes");
		}

		// CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		// String xref=nodeAtts.getAttribute(id, "Xref");

		return node;
	}

	public Edge mergeEdge(final Map<CyNetwork, Set<GraphObject>> mapNetEdge,
			final Node source, final Node target, final String interaction,
			final boolean directed) {
		// TODO: refactor in Cytoscape3
		if (mapNetEdge == null || mapNetEdge.isEmpty() || source == null
				|| target == null) {
			return null;
		}

		// Get the edge or create a new one attribute confilict handling?
		final Edge edge = Cytoscape.getCyEdge(source, target,
				Semantics.INTERACTION, interaction, true, directed); // ID
		// and
		// canonicalName
		// set
		// when
		// created
		final String id = edge.getIdentifier();

		// set other attributes as indicated in attributeMapping
		// setAttribute(id, mapNetEdge, edgeAttributeMapping);

		return edge;
	}
	
	private void updateTaskMonitor(String status, int percentage) {
        if (this.taskMonitor!=null) {
            taskMonitor.setStatus(status);
            taskMonitor.setPercentCompleted(percentage);
        }
    }
}

