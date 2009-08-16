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

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.pathvisio.cytoscape.visualmapping.GpmlColorMapper;
import org.pathvisio.model.PropertyType;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.EdgeAppearance;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearance;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.PassThroughMapping;

public class superpathwaysVisualStyle {

	public static final String vsName = "Superpathways Visual Style";
	
	int styleCount;

	public superpathwaysVisualStyle(String title, 
			Map<String, Color> pwNameToColor, int i) {
		// get the network view
		CyNetwork network = Cytoscape.getNetwork(title);
		cytoscape.view.CyNetworkView networkView = Cytoscape
				.getNetworkView(title);

		// get the VisualMappingManager and CalculatorCatalog
		VisualMappingManager manager = Cytoscape.getVisualMappingManager();
		CalculatorCatalog catalog = manager.getCalculatorCatalog();
        
		//the next code line is used for fix the bug (when doing several merges)
		//catalog.removeVisualStyle(vsName);
		styleCount=i;
		String styleName=vsName+"-"+String.valueOf(i);
		// check to see if a visual style with this name already exists
		VisualStyle vs = catalog.getVisualStyle(styleName);
		
		if (vs == null) {
			// if not, create it and add it to the catalog
			// vs = createVisualStyle(network, title, c);
			vs = createVisualStyle(network, pwNameToColor, styleName);
			catalog.addVisualStyle(vs);
		}else{
			System.out.println("Superpathway style is not null!");
		}

		networkView.setVisualStyle(vs.getName()); // not strictly necessary

		// actually apply the visual style
		manager.setVisualStyle(vs);
		networkView.redrawGraph(true, true);
	}

	VisualStyle createVisualStyle(CyNetwork network, Map<String, Color> pwNameToColor, String StyleName) {
		
		NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
		EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();
		GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();

		//set default value for the following property
		globalAppCalc.setDefaultBackgroundColor(new Color(200, 200, 255));
		nodeAppCalc.getDefaultAppearance().set(VisualPropertyType.NODE_SHAPE, NodeShape.ELLIPSE);
        edgeAppCalc.getDefaultAppearance().set(VisualPropertyType.EDGE_COLOR, new Color(0,0, 255));
        edgeAppCalc.getDefaultAppearance().set(VisualPropertyType.EDGE_LINE_WIDTH, 1.5);

		// Passthrough Mapping - set node label
		PassThroughMapping pm = new PassThroughMapping(new String(), "canonicalName");
		Calculator nlc = new BasicCalculator("Superpathways Node Label Calc",
				pm, VisualPropertyType.NODE_LABEL);
		nodeAppCalc.setCalculator(nlc);
		
		
		// Discrete Mapping - set node colors
		DiscreteMapping disMapping = new DiscreteMapping(Color.WHITE,
				ObjectMapping.NODE_MAPPING);

		disMapping
				.setControllingAttributeName("Source Pathway", network, false);

		Set<String> pwNames = pwNameToColor.keySet();
		Iterator<String> it = pwNames.iterator();
		while (it.hasNext()) {
			String name = it.next();
			disMapping.putMapValue(name, pwNameToColor.get(name));

		}

		Calculator nodeColorCalculator = new BasicCalculator(
				"Superpathways Node Color Calc", disMapping,
				VisualPropertyType.NODE_FILL_COLOR);

		nodeAppCalc.setCalculator(nodeColorCalculator);

		// Create the visual style
		VisualStyle visualStyle = new VisualStyle(StyleName, nodeAppCalc, edgeAppCalc,
				globalAppCalc);

		return visualStyle;
	}
}
