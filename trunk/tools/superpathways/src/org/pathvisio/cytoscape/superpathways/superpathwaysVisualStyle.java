package org.pathvisio.cytoscape.superpathways;

import java.awt.Color;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.ObjectMapping;

public class superpathwaysVisualStyle {
	
	public static final String vsName = "Superpathways Visual Style";
	
	public superpathwaysVisualStyle(){
//	 get the network and view
	CyNetwork network = Cytoscape.getCurrentNetwork();
	CyNetworkView networkView = Cytoscape.getCurrentNetworkView();

	// get the VisualMappingManager and CalculatorCatalog
	VisualMappingManager manager = Cytoscape.getVisualMappingManager();
	CalculatorCatalog catalog = manager.getCalculatorCatalog();

	// check to see if a visual style with this name already exists
	VisualStyle vs = catalog.getVisualStyle(vsName);
	if (vs == null) {
		// if not, create it and add it to the catalog
		vs = createVisualStyle(network);
		catalog.addVisualStyle(vs);
	}
	
	networkView.setVisualStyle(vs.getName()); // not strictly necessary

	// actually apply the visual style
	manager.setVisualStyle(vs);
	networkView.redrawGraph(true,true);
	}
	
	VisualStyle createVisualStyle(CyNetwork network) {

		NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
		
		// Discrete Mapping - set node colors 
		DiscreteMapping disMapping = new DiscreteMapping(Color.WHITE,
		                                                 ObjectMapping.NODE_MAPPING);
		disMapping.setControllingAttributeName("Source Pathway", network, false);
		disMapping.putMapValue(new Integer(1), NodeShape.DIAMOND);
		disMapping.putMapValue(new Integer(2), NodeShape.ELLIPSE);
		disMapping.putMapValue(new Integer(3), NodeShape.TRIANGLE);

		Calculator nodeColorCalculator = new BasicCalculator("Superpathways Node Color Calc",
		                                                  disMapping,
														  VisualPropertyType.NODE_FILL_COLOR);
		nodeAppCalc.setCalculator(nodeColorCalculator);


		// Create the visual style 
		VisualStyle visualStyle = new VisualStyle(vsName, nodeAppCalc, null, null);

		return visualStyle;
	}
}
