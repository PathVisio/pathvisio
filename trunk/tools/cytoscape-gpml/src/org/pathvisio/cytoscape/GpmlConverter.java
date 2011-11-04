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
package org.pathvisio.cytoscape;

import giny.view.GraphView;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.GraphLink.GraphIdContainer;
import org.pathvisio.core.model.PathwayElement.MAnchor;

import cytoscape.CyEdge;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.groups.CyGroup;
import cytoscape.groups.CyGroupManager;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.view.CyNetworkView;

/**
 * Converts GPML or fragments of GPML to a network.
 * Invoked both by direct loading from file, or by clipboard actions.
 */
public class GpmlConverter {
	public static final String PROP_LABEL_AS_NODE = "gpml.label.as.node";

	List<CyEdge> edges = new ArrayList<CyEdge>();

	Map<GraphIdContainer, CyNode> nodeMap = new HashMap<GraphIdContainer, CyNode>();
	Map<PathwayElement, String[]> edgeMap = new HashMap<PathwayElement, String[]>();

	GpmlHandler gpmlHandler;
	Pathway pathway;
	
	private boolean loadAsNetwork = false;

	private GpmlConverter(GpmlHandler h) {
		gpmlHandler = h;
	}

	public GpmlConverter(GpmlHandler gpmlHandler, Pathway p) {
		this(gpmlHandler);
		pathway = p;
		convert();
	}
	
	public GpmlConverter(GpmlHandler gpmlHandler, Pathway p, boolean loadAsNetwork) {
		this(gpmlHandler);
		this.loadAsNetwork = loadAsNetwork;
		pathway = p;
		convert();
	}

	public GpmlConverter(GpmlHandler gpmlHandler, String gpml) throws ConverterException {
		this(gpmlHandler);
		pathway = new Pathway();
		GpmlFormat.readFromXml(pathway, new StringReader(gpml), true);
		convert();
	}

	private void convert() {
		edgeMap.clear();
		edges.clear();
		nodeMap.clear();
		nodeIds.clear();
		
		findNodes();
		findEdges();
	}

	public Pathway getPathway() {
		return pathway;
	}

	Map<GraphIdContainer, String> nodeIds = new HashMap<GraphIdContainer, String>();

	private String generateNodeId(GraphIdContainer o, String preferred) {
		String id = preferred;
		if(id != null) {
			CyNode node = Cytoscape.getCyNode(id, false);
			if(node != null) {
				if(loadAsNetwork) {
					Logger.log.trace("Adding id " + id);
					nodeIds.put(o, id);
					return id;
				}
				id = null; //Node already exists, use graphId instead!
			}
		}
		if(id == null || "".equals(id)) {
			id = o.getGraphId();
			//Get an id if it's not already there
			if(id == null) {
				id = pathway.getUniqueGraphId();
				o.setGraphId(id);
			}
		}
		Logger.log.trace("Adding id " + id);
		nodeIds.put(o, id);
		return id;
	}

	private String getNodeId(GraphIdContainer o) {
		return nodeIds.get(o);
	}

	private void findNodes() {
		for(PathwayElement o : pathway.getDataObjects()) {
			ObjectType type = o.getObjectType();
			if(
					type == ObjectType.BIOPAX ||
					type == ObjectType.LEGEND ||
					type == ObjectType.INFOBOX ||
					type == ObjectType.MAPPINFO
				) {
				continue;
			}
			if(loadAsNetwork && (type == ObjectType.LABEL || type == ObjectType.SHAPE)) {
				continue;
			}
			String id = generateNodeId(o, o.getTextLabel());

			CyNode n = null;
			switch(type) {
			case GROUP:
				Logger.log.trace("Creating group: " + id);
				n = addGroup(o);
				if(n == null) {
					Logger.log.error("Group node is null");
				} else {
					Logger.log.trace("Created group node: " + n.getIdentifier());
				}
				break;
			case LINE:
				if(isEdge(o)) {
					continue; //Don't add an annotation node for an edge
				}
			default:
				//Create a node for every pathway element
				Logger.log.trace("Creating node: " + id + " for " + o.getGraphId() + "@" + o.getObjectType());
				n = Cytoscape.getCyNode(id, true);
			}

			gpmlHandler.addNode(n, o);
			nodeMap.put(o, n);
		}
		processGroups();
	}

	private boolean isEdge(PathwayElement e) {
		GraphIdContainer start = pathway.getGraphIdContainer(e.getMStart().getGraphRef());
		GraphIdContainer end = pathway.getGraphIdContainer(e.getMEnd().getGraphRef());
		Logger.log.trace("Checking if edge " + e.getGraphId() + ": " +
				isNode(start) + ", " + isNode(end)
		);
		return isNode(start) && isNode(end);
	}

	private boolean isNode(GraphIdContainer idc) {
		if(idc instanceof MAnchor) {
			//only valid if the parent line is an edge
			return isEdge(((MAnchor)idc).getParent());
		} else if(idc instanceof PathwayElement) {
			ObjectType ot = ((PathwayElement)idc).getObjectType();
			if(loadAsNetwork && ot == ObjectType.LABEL) return false;
			return
				ot == ObjectType.DATANODE ||
				ot == ObjectType.GROUP ||
				(labelAsNode() && ot == ObjectType.LABEL);
		} else {
			return false;
		}
	}

	/**
	 * Should labels be treated as nodes or as annotations?
	 * This can be controlled by the Cytoscape property named
	 * {@link #PROP_LABEL_AS_NODE}. If set to "true", labels will
	 * be treated as nodes.
	 */
	public static boolean labelAsNode() {
		Properties p = CytoscapeInit.getProperties();
		String value = (String)p.get(PROP_LABEL_AS_NODE);
		return Boolean.parseBoolean(value); //Defaults to false
	}

	private void findEdges() {
		Logger.log.trace("Start finding edges");

		//First find edges that contain anchors
		//Add an AnchorNode for that line
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.LINE) {
				if(pe.getMAnchors().size() > 0 && isEdge(pe)) {
					CyNode n = Cytoscape.getCyNode(generateNodeId(pe, pe.getGraphId()), true);
					gpmlHandler.addAnchorNode(n, pe);
					for(MAnchor a : pe.getMAnchors()) {
						nodeMap.put(a, n);
						nodeIds.put(a, n.getIdentifier());
					}
				}
			}
		}
		//Create the cytoscape edges for each line for which
		//both the start and end points connect to a node
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.LINE) {
				if(isEdge(pe)) {
					//A line without anchors, convert to single edge
					if(pe.getMAnchors().size() == 0) {
						String source = getNodeId(pathway.getGraphIdContainer(pe.getMStart().getGraphRef()));
						String target = getNodeId(pathway.getGraphIdContainer(pe.getMEnd().getGraphRef()));

						Logger.log.trace("Line without anchors ( " + pe.getGraphId() + " ) to edge: " +
								source + ", " + target
						);

						String type = pe.getStartLineType() + ", " + pe.getEndLineType();
						CyEdge e = Cytoscape.getCyEdge(
								source,
								pe.getGraphId(),
								target,
								type
						);
						edges.add(e);
						gpmlHandler.addEdge(e, pe);
					//A line with anchors, split into multiple edges
					} else {
						String sId = nodeMap.get(
								pathway.getGraphIdContainer(pe.getMStart().getGraphRef())
						).getIdentifier();
						String eId = nodeMap.get(
								pathway.getGraphIdContainer(pe.getMEnd().getGraphRef())
						).getIdentifier();

						Logger.log.trace("Line with anchors ( " + pe.getGraphId() + " ) to edges: " +
								sId + ", " + eId
						);

						CyEdge es = Cytoscape.getCyEdge(
								sId,
								pe.getGraphId() + "_start",
								gpmlHandler.getNode(pe.getGraphId()).getParentIdentifier(),
								GpmlHandler.ANCHOR_EDGE_TYPE
						);
						edges.add(es);
						PathwayElement peStart = pe.copy();
						peStart.setEndLineType(null);
						gpmlHandler.addEdge(es, peStart);
						CyEdge ee = Cytoscape.getCyEdge(
								gpmlHandler.getNode(pe.getGraphId()).getParentIdentifier(),
								pe.getGraphId() + "end",
								eId,
								GpmlHandler.ANCHOR_EDGE_TYPE
						);
						edges.add(ee);
						PathwayElement peEnd = pe.copy();
						peStart.setStartLineType(null);
						gpmlHandler.addEdge(ee, peEnd);
					}
				}
			}
		}
		//Fix anchor links
		for(GpmlNode gn : gpmlHandler.getNodes()) {
			if(gn instanceof GpmlAnchorNode) {
				((GpmlAnchorNode)gn).cleanupAnchors();
			}
		}
	}

	public int[] getNodeIndicesArray() {
		int[] inodes = new int[nodeMap.size()];
		int i = 0;
		for(CyNode n : nodeMap.values()) {
			inodes[i++] = n.getRootGraphIndex();
		}
		return inodes;
	}

	public int[] getEdgeIndicesArray() {
		int[] iedges = new int[edges.size()];
		for(int i = 0; i< edges.size(); i++) {
			iedges[i] = edges.get(i).getRootGraphIndex();
		}
		return iedges;
	}

	//Add a group node
	private CyNode addGroup(PathwayElement group) {
		CyGroup cyGroup = CyGroupManager.findGroup(group.getGroupId());
		if(cyGroup == null) {
			cyGroup = CyGroupManager.createGroup(group.getGroupId(), null);
		}
		CyNode gn = cyGroup.getGroupNode();
		gn.setIdentifier(group.getGraphId() == null ? group.setGeneratedGraphId() : group.getGraphId());
		nodeIds.put(group, gn.getIdentifier());
		return gn;
	}

	//Add all nodes to the group
	private void processGroups() {
		for(PathwayElement pwElm : pathway.getDataObjects()) {
			if(pwElm.getObjectType() == ObjectType.GROUP) {

				GpmlNode gpmlNode = gpmlHandler.getNode(getNodeId(pwElm));
				CyGroup cyGroup = CyGroupManager.getCyGroup(gpmlNode.getParent());
				if(cyGroup == null) {
					Logger.log.warn("Couldn't create group: CyGroupManager returned null");
					return;
				}

				//The interaction name
				String interaction = GpmlHandler.GROUP_EDGE_TYPE;

				PathwayElement[] groupElements = pathway.getGroupElements(
						pwElm.getGroupId()
					).toArray(new PathwayElement[0]);

				//Create the cytoscape parts of the group
				for(int i = 0; i < groupElements.length; i++) {
					PathwayElement peI = groupElements[i];
					GpmlNetworkElement neI = gpmlHandler.getNetworkElement(getNodeId(peI));
					//Only add links to nodes, not to annotations
					if(neI instanceof GpmlNode) {
						cyGroup.addNode(((GpmlNode)neI).getParent());
						edges.add(Cytoscape.getCyEdge(
								cyGroup.getGroupNode().getIdentifier(),
								"inGroup: " + cyGroup.getGroupName(),
								neI.getParentIdentifier(), interaction)
						);

//						//Add links between all elements of the group
//						for(int j = i + 1; j < groupElements.length; j++) {
//							PathwayElement pe_j = groupElements[j];
//							GpmlNetworkElement<?> ne_j = gpmlHandler.getNetworkElement(pe_j.getGraphId());
//							if(ne_j instanceof GpmlNode) {
//								edges.add(Cytoscape.getCyEdge(
//										ne_i.getParentIdentifier(),
//										"inGroup: " + cyGroup.getGroupName(),
//										ne_j.getParentIdentifier(), interaction)
//								);
//							}
//						}
					}
				}
			}
		}
	}

	private void setGroupViewer(CyNetworkView view, String groupViewer) {
		for(GpmlNode gn : gpmlHandler.getNodes()) {
			if(gn.getPathwayElement().getObjectType() == ObjectType.GROUP) {
				CyGroup group = CyGroupManager.getCyGroup(gn.getParent());
				CyGroupManager.setGroupViewer(group, groupViewer, view, true);
			}
		}
	}

	/**
	 * Add annotations, layout nodes, then create and apply visual style.
	 * 
	 * @param view
	 */
	public void layout(GraphView view)
	{
		gpmlHandler.addAnnotations(view, nodeMap.values());
		if(loadAsNetwork) gpmlHandler.showAnnotations(view, false);
		gpmlHandler.applyGpmlLayout(view, nodeMap.values());
		if(loadAsNetwork) {
			CyLayoutAlgorithm alg = CyLayouts.getLayout("force-directed");
			alg.doLayout();
		}
		gpmlHandler.applyGpmlVisualStyle();
		view.fitContent();
	}
}
