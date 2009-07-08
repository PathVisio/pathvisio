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

import cytoscape.CyNode;

import giny.view.GraphView;
import giny.view.NodeView;

import java.awt.geom.Point2D;
import java.util.List;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.MLine;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MAnchor;

/**
 * Class that holds a Cytoscape edge that has a GPML representation, which is stored
 * as edge attributes
 * @author thomas
 *
 */
public class GpmlAnchorNode extends GpmlNode {
	public static final int TYPE_ANCHOR = ObjectType.values().length + 1;
	
	/**
	 * Constructor for this class. Creates a new GpmlNode, based on the given
	 * node and line
	 */
	public GpmlAnchorNode(CyNode parent, PathwayElement e, AttributeMapper attributeMapper) {
		super(parent, e, attributeMapper);
	}
	
	private MLine getMLine() {
		return (MLine)getPathwayElement();
	}
	
	MAnchor anchor;
	
	protected void cleanupAnchors() {
		PathwayElement pe = getPathwayElement();
		List<MAnchor> anchors = pe.getMAnchors();
		while(anchors.size() > 1) {
			anchors.remove(anchors.size() - 1);
		}
		anchor = anchors.get(0);
		setPwElmOrig(pe);
	}
	
	public GraphIdContainer getGraphIdContainer() {
		return anchor;
	}

	protected void resetPosition(GraphView view) {
		NodeView nv = view.getNodeView(parent);
		if(nv == null) {
			Logger.log.trace("Null node view found in GpmlNode.resetPosition, a group?");
			return;
		}
		MAnchor anchor = getPathwayElement().getMAnchors().get(0);
		Point2D p = getMLine().getConnectorShape().fromLineCoordinate(anchor.getPosition());
		Logger.log.trace("Setting position of anchor node to: " + p);
		nv.setXPosition(GpmlPlugin.mToV(p.getX()), false);
		nv.setYPosition(GpmlPlugin.mToV(p.getY()), false);
	}
	
	public void updateFromGpml(AttributeMapper attributeMapper) {
		super.updateFromGpml(attributeMapper);
		getCyAttributes().setAttribute(getParentIdentifier(), ATTR_TYPE, TYPE_ANCHOR);
		getCyAttributes().setAttribute(getParentIdentifier(), "canonicalName", "");
	}
	
	public void addAnnotation(GraphView view) {
		//Do nothing
	}
}
