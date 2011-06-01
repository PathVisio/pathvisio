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
package org.pathvisio.cytoscape;

import cytoscape.CyEdge;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualStyle;

import giny.model.Edge;
import giny.model.Node;
import giny.view.EdgeView;
import giny.view.GraphView;
import giny.view.NodeView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.cytoscape.visualmapping.GpmlVisualStyle;

/**
 * Class that handles the GPML representation of nodes and edges, stored
 * as attributes
 * @author thomas
 *
 */
public class GpmlHandler {
	CyAttributes nAttributes = Cytoscape.getNodeAttributes();
	CyAttributes eAttributes = Cytoscape.getEdgeAttributes();

	//String is node/edge identifier, which equals GPML's graphId
	Map<String, GpmlNode> nodes =  new HashMap<String, GpmlNode>();
	Map<String, GpmlEdge> edges = new HashMap<String, GpmlEdge>();
	Map<String, GpmlNetworkElement<?>> elements = new HashMap<String, GpmlNetworkElement<?>>();

	AttributeMapper attributeMapper = new DefaultAttributeMapper();

	public void setAttributeMapper(AttributeMapper attributeMapper) {
		this.attributeMapper = attributeMapper;
	}

	public AttributeMapper getAttributeMapper() {
		return attributeMapper;
	}

	public Collection<GpmlNode> getNodes() {
		return nodes.values();
	}

	public GpmlNetworkElement<?> getNetworkElement(String id) {
		return elements.get(id);
	}

	public GpmlNode getNode(String nodeId) {
		return nodes.get(nodeId);
	}

	public GpmlNode getNode(Node node) {
		return getNode(node.getIdentifier());
	}

	protected static final String ANCHOR_EDGE_TYPE = "anchor-connection";
	protected static final String GROUP_EDGE_TYPE = "group-connection";

	public boolean isAnchorEdge(Edge edge) {
		return ANCHOR_EDGE_TYPE.equals(
				eAttributes.getAttribute(edge.getIdentifier(), "interaction").toString()
		);
	}

	public boolean isGroupEdge(Edge edge) {
		return GROUP_EDGE_TYPE.equals(
				eAttributes.getAttribute(edge.getIdentifier(), "interaction").toString()
		);
	}

	/**
	 * Creates and adds a GpmlNode for the given NodeView, if it
	 * doesn't exist yet
	 * @param nview
	 * @return The GpmlNode for the given NodeView
	 */
	public GpmlNode createNode(NodeView nview) {
		String nid = nview.getNode().getIdentifier();
		GpmlNode gn = nodes.get(nid);
		if(gn == null) {
			addNode(gn = new GpmlNode(nview, getAttributeMapper()));
		}
		return gn;
	}

	private void addNetworkElement(GpmlNetworkElement<?> elm) {
		elements.put(elm.getParentIdentifier(), elm);
	}

	private void unlinkNetworkElement(String id) {
		elements.remove(id);
	}

	private void addNode(GpmlNode gn) {
		nodes.put(gn.getParentIdentifier(), gn);
		addNetworkElement(gn);
	}

	private void addEdge(GpmlEdge ge) {
		edges.put(ge.getParentIdentifier(), ge);
		addNetworkElement(ge);
	}

	public GpmlEdge getEdge(String edgeId) {
		return edges.get(edgeId);
	}

	public GpmlEdge getEdge(Edge e) {
		return getEdge(e.getIdentifier());
	}

	public GpmlEdge createEdge(EdgeView eview) {
		GraphView gview = eview.getGraphView();
		String eid = eview.getEdge().getIdentifier();
		GpmlEdge ge = edges.get(eid);
		if(ge == null) {
			GpmlNode gsource = createNode(gview.getNodeView(eview.getEdge().getSource()));
			GpmlNode gtarget = createNode(gview.getNodeView(eview.getEdge().getTarget()));
			addEdge(ge = new GpmlEdge(eview, gsource, gtarget, getAttributeMapper()));

		}
		return ge;
	}

	/**
	 * Add a node that will be linked to GPML information
	 * @param n	The Cytoscape node to give GPML information
	 * @param pwElm The GPML information
	 */
	public void addNode(CyNode n, PathwayElement pwElm) {
		addNode(new GpmlNode(n, pwElm, getAttributeMapper()));
	}
	
	public void addAnchorNode(CyNode n, PathwayElement line) {
		addNode(new GpmlAnchorNode(n, line, getAttributeMapper()));
	}

	/**
	 * Unlink the GPML information from the given node
	 * @param n
	 */
	public void unlinkNode(CyNode n) {
		nodes.remove(n.getIdentifier());
		unlinkNetworkElement(n.getIdentifier());
	}

	/**
	 * Add an edge that will be linked to GPML information
	 * @param n	The Cytoscape edge to give GPML information
	 * @param pwElm The GPML information
	 */
	public void addEdge(CyEdge e, PathwayElement pwElm) {
		GpmlNode gsource = getNode(e.getSource());
		GpmlNode gtarget = getNode(e.getTarget());
		addEdge(new GpmlEdge(e, pwElm, gsource, gtarget, getAttributeMapper()));
	}

    /**
     * Adds an annotation to the foreground canvas of the given view for
     * each node in the list that is linked to GPML information. An annotation will not
     * be created when the GPML element could be fully converted to a Cytoscape
     * node or edge (e.g. for data nodes or lines linked between two data nodes.
     * @param view
     * @param nodeList
     */
    public void addAnnotations(GraphView view, Collection<CyNode> nodeList) {
    	for(CyNode n : nodeList) {
    		GpmlNode gn = nodes.get(n.getIdentifier());
			if(gn != null && !edges.containsKey(gn)) { //Don't draw background line if it is an edge
				gn.addAnnotation(view);
			}
		}
    }

    /**
     * Show or hide the annotations (non node/edge elements) on the annotation canvas
     * of the given view
     * @param view
     * @param visible
     */
    public void showAnnotations(GraphView view, boolean visible) {
    	for(GpmlNode gn : nodes.values()) {
    		gn.showAnnotations(view, visible);
    	}
    }

    public Pathway createPathway(GraphView view) {
    	Pathway pathway = new Pathway();
    	pathway.getMappInfo().setMapInfoName(view.getIdentifier());
    	for(GpmlNetworkElement<?> ge : elements.values()) {
    		pathway.add(ge.getPathwayElement(view, attributeMapper));
    	}
    	return pathway;
    }

    public void applyGpmlVisualStyle() {
    	VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
    	CalculatorCatalog catalog = vmm.getCalculatorCatalog();
		VisualStyle gpmlStyle;
		Set<String> styles = catalog.getVisualStyleNames();
		if (styles.contains(GpmlVisualStyle.NAME)){
			Logger.log.trace("VisualStyle: reusing GPML style");
			gpmlStyle = catalog.getVisualStyle(GpmlVisualStyle.NAME);
		} else {
			Logger.log.trace("VisualStyle: creating GPML style");
			gpmlStyle = new GpmlVisualStyle(this);
			catalog.addVisualStyle(gpmlStyle);
		}
    	vmm.setVisualStyle(gpmlStyle);	
    }

    /**
     * Lays out the given nodes to the coordinates as stored in the linked GPML information
     * @param view
     * @param nodeList
     */
    public void applyGpmlLayout(GraphView view, Collection<CyNode> nodeList) {
    	for(CyNode node : nodeList) {
    		GpmlNode gn = nodes.get(node.getIdentifier());
    		if(gn == null) {
    			Logger.log.trace("Layout: skipping " + gn + ", not a GPML node");
    			continue; //Not a GPML node
    		}
    		gn.updateFromGpml(getAttributeMapper(), view);
    	}
		view.updateView();
    }

}
