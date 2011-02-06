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
package org.pathvisio.cytoscape.visualmapping;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.pathvisio.cytoscape.AttributeMapper;
import org.pathvisio.cytoscape.GpmlAnchorNode;
import org.pathvisio.cytoscape.GpmlHandler;
import org.pathvisio.cytoscape.GpmlNetworkElement;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.StaticProperty;

import cytoscape.Cytoscape;
import cytoscape.visual.ArrowShape;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.PassThroughMapping;

/**
 * Defines a @link VisualStyle that renders a GPML network
 * as it would in Pathway programs.
 */
public class GpmlVisualStyle extends VisualStyle {
	public static final String NAME = "GPML";

	GpmlHandler gpmlHandler;
	AttributeMapper attrMapper;

	GlobalAppearanceCalculator gac;
	NodeAppearanceCalculator nac;
	EdgeAppearanceCalculator eac;

	public GpmlVisualStyle(GpmlHandler gpmlHandler, VisualStyle toCopy) {
		super(toCopy);
		setName(NAME);
		init(gpmlHandler);
	}

	public GpmlVisualStyle(GpmlHandler gpmlHandler) {
		super(NAME);
		init(gpmlHandler);
	}

	private void init(GpmlHandler gh) {
		this.gpmlHandler = gh;
		attrMapper = gpmlHandler.getAttributeMapper();

		gac = getGlobalAppearanceCalculator();
		nac = getNodeAppearanceCalculator();
		eac = getEdgeAppearanceCalculator();
		
		VisualMappingManager vm = Cytoscape.getVisualMappingManager();
		VisualStyle currentStyle = vm.getVisualStyle();
		
		if(gac == null) {
			gac = new GlobalAppearanceCalculator(currentStyle.getGlobalAppearanceCalculator());
			setGlobalAppearanceCalculator(gac);
		}
		if(nac == null) {
			nac = new NodeAppearanceCalculator(currentStyle.getNodeAppearanceCalculator());
			setNodeAppearanceCalculator(nac);
		}
		if(eac == null) {
			eac = new EdgeAppearanceCalculator(currentStyle.getEdgeAppearanceCalculator());
			setEdgeAppearanceCalculator(eac);
		}
		
		setGlobalMapping();
		setColorMapping();
		setLabelMapping();
		setTypeMapping();
		setArrowMapping();
		setLineTypeMapping();
		setNodeShapeMapping();
	}
	
	void setGlobalMapping(){
		getGlobalAppearanceCalculator().setDefaultBackgroundColor(Color.WHITE);
		getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED,false);
	}

	void setNodeShapeMapping() {
		NodeAppearance nd = nac.getDefaultAppearance();
		nd.set(VisualPropertyType.NODE_SHAPE, NodeShape.RECT);
		nd.set(VisualPropertyType.NODE_WIDTH, 5.0);
		nd.set(VisualPropertyType.NODE_HEIGHT, 5.0);
		
		PassThroughMapping widthMapping = new PassThroughMapping(
				nac.getDefaultAppearance().get(VisualPropertyType.NODE_WIDTH), 
				StaticProperty.WIDTH.getName());

		PassThroughMapping heightMapping = new PassThroughMapping(
				nac.getDefaultAppearance().get(VisualPropertyType.NODE_HEIGHT), 
				StaticProperty.HEIGHT.getName());

		nac.setCalculator(
				new BasicCalculator("Node width", widthMapping, VisualPropertyType.NODE_WIDTH)
		);
		nac.setCalculator(
				new BasicCalculator("Node height", heightMapping, VisualPropertyType.NODE_HEIGHT)
		);
	}

	void setLineTypeMapping() {
		DiscreteMapping styleMapping = new DiscreteMapping(
				nac.getDefaultAppearance().get(VisualPropertyType.EDGE_LINE_STYLE),
				attrMapper.getMapping(StaticProperty.LINESTYLE),
				ObjectMapping.EDGE_MAPPING
		);
		styleMapping.putMapValue("" + LineStyle.SOLID, cytoscape.visual.LineStyle.SOLID);
		styleMapping.putMapValue("" + LineStyle.DASHED, cytoscape.visual.LineStyle.LONG_DASH);
		eac.setCalculator(
				new BasicCalculator("GPML edge style", styleMapping, VisualPropertyType.EDGE_LINE_STYLE)
		);
	}

	void setArrowMapping() {
		DiscreteMapping srcMapping = new DiscreteMapping(
				nac.getDefaultAppearance().get(VisualPropertyType.EDGE_SRCARROW_SHAPE),
				attrMapper.getMapping(StaticProperty.STARTLINETYPE),
				ObjectMapping.EDGE_MAPPING
		);
		setArrowMappings(srcMapping);
		eac.setCalculator(
				new BasicCalculator("GPML edge source shape", srcMapping, VisualPropertyType.EDGE_SRCARROW_SHAPE)
		);

		DiscreteMapping tgtMapping = new DiscreteMapping(
				nac.getDefaultAppearance().get(VisualPropertyType.EDGE_TGTARROW_SHAPE),
				attrMapper.getMapping(StaticProperty.ENDLINETYPE),
				ObjectMapping.EDGE_MAPPING
		);
		setArrowMappings(tgtMapping);
		eac.setCalculator(
				new BasicCalculator("GPML edge target shape", tgtMapping, VisualPropertyType.EDGE_TGTARROW_SHAPE)
		);

		String colAttr = attrMapper.getMapping(StaticProperty.COLOR);
		eac.setCalculator(
				new BasicCalculator(
						"GPML edge source color",
						new GpmlColorMapper(
								(Color)eac.getDefaultAppearance().get(VisualPropertyType.EDGE_SRCARROW_COLOR), colAttr
						),
						VisualPropertyType.EDGE_SRCARROW_COLOR
				)
		);
		eac.setCalculator(
				new BasicCalculator(
						"GPML edge target color",
						new GpmlColorMapper(
								(Color)eac.getDefaultAppearance().get(VisualPropertyType.EDGE_TGTARROW_COLOR), colAttr
						),
						VisualPropertyType.EDGE_TGTARROW_COLOR
				)
		);
	}

	private static Map<String, ArrowShape> attribute2arrow;
	private static Map<ArrowShape, String> arrow2attribute;

	public static Map<String, ArrowShape> getAttributeToArrow() {
		if(attribute2arrow == null) {
			attribute2arrow = new HashMap<String, ArrowShape>();
			attribute2arrow.put(LineType.LINE.getName(), ArrowShape.NONE);
			attribute2arrow.put(LineType.ARROW.getName(), ArrowShape.ARROW);
			attribute2arrow.put(LineType.LIGAND_ROUND.getName(), ArrowShape.CIRCLE);
			attribute2arrow.put(LineType.LIGAND_SQUARE.getName(), ArrowShape.DELTA);
			attribute2arrow.put(LineType.RECEPTOR.getName(), ArrowShape.DELTA);
			attribute2arrow.put(LineType.RECEPTOR_ROUND.getName(), ArrowShape.DELTA);
			attribute2arrow.put(LineType.RECEPTOR_SQUARE.getName(), ArrowShape.DELTA);
			attribute2arrow.put(LineType.TBAR.getName(), ArrowShape.T);

			attribute2arrow.put("mim-necessary-stimulation", ArrowShape.ARROW);
			attribute2arrow.put("mim-binding", ArrowShape.ARROW);
			attribute2arrow.put("mim-conversion", ArrowShape.ARROW);
			attribute2arrow.put("mim-stimulation", ArrowShape.ARROW);
			attribute2arrow.put("mim-catalysis", ArrowShape.ARROW);
			attribute2arrow.put("mim-inhibition", ArrowShape.ARROW);
			attribute2arrow.put("mim-cleavage", ArrowShape.ARROW);
		}
		return attribute2arrow;
	}

	public static Map<ArrowShape, String> getArrowToAttribute() {
		if(arrow2attribute == null) {
			arrow2attribute = new HashMap<ArrowShape, String>();
			Map<String, ArrowShape> attribute2arrow = getAttributeToArrow();
			for(String attribute : attribute2arrow.keySet()) {
				arrow2attribute.put(attribute2arrow.get(attribute), attribute);
			}
		}
		return arrow2attribute;
	}

	void setArrowMappings(DiscreteMapping mapping) {
		Map<String, ArrowShape> attribute2arrow = getAttributeToArrow();
		for(String attribute : attribute2arrow.keySet()) {
			mapping.putMapValue(attribute, attribute2arrow.get(attribute));
		}
	}

	void setTypeMapping() {
		DiscreteMapping typeMapping = new DiscreteMapping(
			nac.getDefaultAppearance().get(VisualPropertyType.NODE_SIZE),
			GpmlNetworkElement.ATTR_TYPE,
			ObjectMapping.NODE_MAPPING
		);
		typeMapping.putMapValue(ObjectType.GROUP.ordinal(), 5);
		typeMapping.putMapValue(GpmlAnchorNode.TYPE_ANCHOR, 5);

		nac.setCalculator(
				new BasicCalculator(
						GpmlNetworkElement.ATTR_TYPE,
						typeMapping,
						VisualPropertyType.NODE_SIZE
				)
		);
	}

	void setLabelMapping() {
		nac.setCalculator(
				new BasicCalculator(
						"canonicalName",
						new PassThroughMapping("", "canonicalName"),
						VisualPropertyType.NODE_LABEL
				)
		);
	}

	void setColorMapping() {
		//Default node color is white and semi-transparent
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FILL_COLOR, Color.WHITE);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_OPACITY, 220);

		String colAttr = attrMapper.getMapping(StaticProperty.COLOR);
		nac.setCalculator(
				new BasicCalculator(
						"GPML node border color",
						new GpmlColorMapper(
								(Color)nac.getDefaultAppearance().get(VisualPropertyType.NODE_BORDER_COLOR), colAttr
						),
						VisualPropertyType.NODE_BORDER_COLOR
				)
		);
		eac.setCalculator(
				new BasicCalculator(
						"GPML edge color",
						new GpmlColorMapper(
								(Color)eac.getDefaultAppearance().get(VisualPropertyType.EDGE_COLOR), colAttr
						),
						VisualPropertyType.EDGE_COLOR
				)
		);

	}
}
