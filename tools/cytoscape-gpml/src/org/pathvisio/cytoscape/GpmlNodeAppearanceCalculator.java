// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import giny.model.Node;

import java.awt.Color;

import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;

import cytoscape.CyNetwork;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualPropertyType;

public class GpmlNodeAppearanceCalculator extends NodeAppearanceCalculator {
	GpmlHandler gpmlHandler;
	
	public GpmlNodeAppearanceCalculator(GpmlHandler gpmlHandler) {
		this.gpmlHandler = gpmlHandler;
	}
	
	public void calculateNodeAppearance(NodeAppearance appr, Node node,
			CyNetwork network) {
		super.calculateNodeAppearance(appr, node, network);
		
		GpmlNode gn = gpmlHandler.getNode(node.getIdentifier());
		if(gn != null) {
			PathwayElement e = gn.getPathwayElement();
			
			//Node label
			String label = e.getTextLabel();
			appr.set(VisualPropertyType.NODE_LABEL, label != null ? label : e.getGraphId());
			
			//Node shape
			appr.set(VisualPropertyType.NODE_SHAPE, NodeShape.ELLIPSE);
			
			//Node colors
			Color stroke = e.getColor();
			if(stroke != null) {
				appr.set(VisualPropertyType.NODE_BORDER_COLOR, stroke);
			}
			Color fill = e.getFillColor();
			if(fill != null) {
				appr.set(VisualPropertyType.NODE_FILL_COLOR, fill);
			}
			
			//Node width/height
			appr.set(VisualPropertyType.NODE_WIDTH, GpmlPlugin.mToV(e.getMWidth()));
			appr.set(VisualPropertyType.NODE_HEIGHT, GpmlPlugin.mToV(e.getMHeight()));
			
			//For groups
			if(e.getObjectType() == ObjectType.GROUP) {
				appr.set(VisualPropertyType.NODE_SIZE, 5);
				appr.set(VisualPropertyType.NODE_LABEL, e.getTextLabel());
			}
			//For anchors
			if(e.getObjectType() == ObjectType.LINE) {
				appr.set(VisualPropertyType.NODE_SIZE, 5);
				appr.set(VisualPropertyType.NODE_LABEL, "anchor");
			}
		}
	}
		
	public String calculateNodeLabel(Node node, CyNetwork network) {
		GpmlNode gn = gpmlHandler.getNode(node.getIdentifier());
		if(gn != null) {
			PathwayElement e = gn.getPathwayElement();
			String label = e.getTextLabel();
			return label;
		}
		return null;
	}
}
