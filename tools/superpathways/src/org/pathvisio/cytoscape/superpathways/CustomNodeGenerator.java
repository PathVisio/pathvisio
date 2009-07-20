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

import giny.model.Node;

import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.File;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import ding.view.DNodeView;

public class CustomNodeGenerator {

    String pieLocation;
	Node node;

	
	public CustomNodeGenerator(String loc, Node n) {
		pieLocation = new String(loc);
		node = n;
	}

	// to create custom node graphics
	public void createCustomNode(CyNetwork cyNetwork) {
		Cytoscape.createNetworkView(cyNetwork);
		Rectangle2D rect = new Rectangle2D.Double(-20.0, -20.0, 40.0, 40.0);

		Paint paint = null;

		try {
			paint = new java.awt.TexturePaint(javax.imageio.ImageIO
					.read(new File(pieLocation)), rect);

			// new java.net.URL("http://cytoscape.org/people_photos/nerius.jpg")

		} catch (Exception exc) {
			paint = java.awt.Color.black;
		}

		giny.view.NodeView nv;
		// Obtain an instance of NodeView using documented Cytoscape API.....
		nv = (DNodeView) Cytoscape.getCurrentNetworkView().getNodeView(node);

		ding.view.DNodeView dnv = (ding.view.DNodeView) nv;
		dnv.addCustomGraphic(rect, paint, 0);
	}
}
