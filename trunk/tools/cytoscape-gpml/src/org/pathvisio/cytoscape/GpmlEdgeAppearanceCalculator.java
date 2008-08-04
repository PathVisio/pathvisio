package org.pathvisio.cytoscape;

import giny.model.Edge;

import java.awt.Color;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.LineType;
import org.pathvisio.model.PathwayElement;

import cytoscape.CyNetwork;
import cytoscape.visual.ArrowShape;
import cytoscape.visual.EdgeAppearance;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.LineStyle;
import cytoscape.visual.VisualPropertyType;

public class GpmlEdgeAppearanceCalculator extends EdgeAppearanceCalculator {
	GpmlHandler gpmlHandler;
	
	public GpmlEdgeAppearanceCalculator(GpmlHandler gpmlHandler) {
		this.gpmlHandler = gpmlHandler;
	}
	
	public void calculateEdgeAppearance(EdgeAppearance appr, Edge edge,
			CyNetwork network) {
		super.calculateEdgeAppearance(appr, edge, network);
		GpmlEdge ge = gpmlHandler.getEdge(edge.getIdentifier());
		if(ge != null) {
			PathwayElement e = ge.getPathwayElement();
			
			//Map arrow start/end
			ArrowShape start = getArrowShape(e.getStartLineType());
			ArrowShape end = getArrowShape(e.getEndLineType());
			
			appr.set(VisualPropertyType.EDGE_SRCARROW_SHAPE, start);
			appr.set(VisualPropertyType.EDGE_TGTARROW_SHAPE, end);
			
			//Map color
			Color lc = e.getColor();
			appr.set(VisualPropertyType.EDGE_COLOR, lc);
			appr.set(VisualPropertyType.EDGE_SRCARROW_COLOR, lc);
			appr.set(VisualPropertyType.EDGE_TGTARROW_COLOR, lc);
			
			//Map line style
			appr.set(VisualPropertyType.EDGE_LINE_STYLE, getLineStyle(e.getLineStyle()));
		}
		
	}
	
	private LineStyle getLineStyle(int ls) {
		if(ls == org.pathvisio.model.LineStyle.SOLID) {
			return LineStyle.SOLID;
		} else {
			return LineStyle.LONG_DASH;
		}
	}
	
	private ArrowShape getArrowShape(LineType lt) {
		ArrowShape as = ArrowShape.NONE;
		
		if(lt == null) {
			return as;
		} else if(lt == LineType.ARROW) {
			as = ArrowShape.ARROW;
		} else if(lt.toString().startsWith("mim")) {
			as = ArrowShape.ARROW; //All mim shapes to arrow for now
		} else if (lt == LineType.LIGAND_ROUND) {
			as = ArrowShape.CIRCLE;
		} else if (lt == LineType.LIGAND_SQUARE) {
			as = ArrowShape.DELTA; //No perfect mapping
		} else if (lt == LineType.RECEPTOR) {
			as = ArrowShape.DELTA; //No perfect mapping
		} else if (lt == LineType.RECEPTOR_ROUND) {
			as = ArrowShape.DELTA; //No perfect mapping
		} else if (lt == LineType.RECEPTOR_SQUARE) {
			as = ArrowShape.DELTA; //No perfect mapping
		} else if (lt == LineType.TBAR) {
			as = ArrowShape.T;
		}
		
		return as;
	}
}
