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

import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;
import cytoscape.groups.CyGroupManager;
import ding.view.DGraphView;
import ding.view.DingCanvas;

import giny.view.GraphView;
import giny.view.NodeView;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.GraphLink.GraphIdContainer;

/**
 * Class that holds a Cytoscape node that has a GPML representation, which is stored
 * as node attributes
 * @author thomas
 *
 */
public class GpmlNode extends GpmlNetworkElement<CyNode> {
	public static final String PROPERTY_ANNOTATION_CANVAS = "gpml.annotation.canvas";
	
	Map<GraphView, Annotation> annotations = new HashMap<GraphView, Annotation>();

	/**
	 * Constructor for this class. Creates a new GpmlNode, based on the given
	 * node and PathwayElement
	 * @param parent
	 * @param pwElm
	 */
	public GpmlNode(CyNode parent, PathwayElement pwElm, AttributeMapper attributeMapper) {
		super(parent, pwElm, attributeMapper);
		String id = pwElm.getGraphId();
		if(id == null) {
			id = Integer.toHexString(parent.getRootGraphIndex());
		}
	}

	/**
	 * Creates a new GpmlNode based on the given node view. A GPML representation
	 * (PathwayElement of type DataNode) will automatically created based on the node view.
	 * @param parent
	 */
	public GpmlNode(NodeView view, AttributeMapper attributeMapper) {
		super((CyNode)view.getNode(), PathwayElement.createPathwayElement(
				CyGroupManager.isaGroup((CyNode)view.getNode()) ?
						ObjectType.GROUP : ObjectType.DATANODE));
		pwElmOrig.setTextLabel(parent.getIdentifier());
		pwElmOrig.setInitialSize();
		//Set graphid to rootgraph index
		String id = Integer.toHexString(getParent().getRootGraphIndex());
		pwElmOrig.setGraphId(id);
		setPwElmCy(pwElmOrig.copy());
	}

	/**
	 * Show this node's annotations on the given view
	 * @param view
	 * @param visible
	 */
	public void showAnnotations(GraphView view, boolean visible) {
		Annotation a = annotations.get(view);
		if(a != null) {
			a.setVisible(visible);
		}
	}

	public boolean isAnnotation(GraphView view) {
		return annotations.containsKey(view);
	}

	public void addAnnotation(GraphView view) {
		if(annotations.containsKey(view)) return; //Annotation already added

		Logger.log.trace("Adding annotation for " + this);

		NodeView nv = view.getNodeView(parent);
		DGraphView dview = (DGraphView) view;
		DGraphView.Canvas canvas = DGraphView.Canvas.BACKGROUND_CANVAS;
		//Check for preferences to see if we need to draw annotations on foreground or background
		Properties p = CytoscapeInit.getProperties();
		String canvasName = p.getProperty(PROPERTY_ANNOTATION_CANVAS);
		if(canvasName != null) {
			DGraphView.Canvas canvasPref = DGraphView.Canvas.valueOf(canvasName);
			if(canvasPref != null) canvas = canvasPref;
		}
		
		DingCanvas aLayer = dview.getCanvas(canvas);

		Annotation a = null;

		switch(pwElmOrig.getObjectType()) {
		case SHAPE:
			a = new Shape(pwElmOrig, dview);
			break;
		case LABEL:
			if(GpmlConverter.labelAsNode()) {
				break; //don't add annotation for label
			} else {
				a = new Label(pwElmOrig, dview);
				break;
			}
		case LINE:
			a = new Line(pwElmOrig, dview);
			break;
		case LEGEND:
		case MAPPINFO:
		case INFOBOX:
			//Only hide the node
			view.hideGraphObject(nv);
			break;
		}
		if(a != null) {
			// TODO: NEED TO FIX FOR 2.8!
			aLayer.add(a);
			view.hideGraphObject(nv);
			annotations.put(view, a);
		}
	}

	public void updateFromGpml(AttributeMapper attributeMapper, GraphView view) {
		super.updateFromGpml(attributeMapper, view);
		resetPosition(view);
	}

	protected void resetPosition(GraphView view) {
		NodeView nv = view.getNodeView(parent);
		if(nv == null) {
			Logger.log.trace("Null node view found in GpmlNode.resetPosition, a group?");
			return;
		}
		nv.setXPosition(GpmlPlugin.mToV(pwElmOrig.getMCenterX()), false);
		nv.setYPosition(GpmlPlugin.mToV(pwElmOrig.getMCenterY()), false);
	}

	protected void savePosition(GraphView view) {
		NodeView nv = (NodeView)view.getNodeView(parent);
		if(nv != null) { //View could be null, in case of hidden node
			getPwElmCy().setMCenterX(GpmlPlugin.vToM(nv.getXPosition()));
			getPwElmCy().setMCenterY(GpmlPlugin.vToM(nv.getYPosition()));
		}
	}

	public CyAttributes getCyAttributes() {
		return Cytoscape.getNodeAttributes();
	}

	public String getParentIdentifier() {
		return getParent().getIdentifier();
	}

	public GraphIdContainer getGraphIdContainer() {
		return getPathwayElement();
	}

	public void updateFromCytoscape(GraphView view, AttributeMapper attributeMapper) {
		super.updateFromCytoscape(view, attributeMapper);
		if(!isAnnotation(view)) {
			savePosition(view);
		}
	}
}
