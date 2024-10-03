/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.view;

import java.awt.Color;
import java.net.URL;

import org.pathvisio.core.model.CellularComponentType;
import org.pathvisio.core.model.ConnectorType;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.IShape;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.model.MState;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElement.MAnchor;
import org.pathvisio.core.model.ShapeType;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.Resources;

/**
 * Contains a set of templates, patterns of PathwayElements that can
 * be added to a Pathway, including default values.
 */
public abstract class DefaultTemplates {
	public final static Color COLOR_METABOLITE = Color.BLUE;
	public final static Color COLOR_PATHWAY = new Color(20,150,30);
	public final static Color COLOR_LABEL = Color.DARK_GRAY;

	/**
	 * Abstract base for templates that only add a single PathwayElement
	 * to a Pathway
	 */
	static abstract class SingleElementTemplate implements Template {
		PathwayElement lastAdded;

		protected void addElement(PathwayElement e, Pathway p) {
			p.add(e);
			lastAdded = e;
		}

		/**
		 * Default implementation returns the view of the last added object
		 */
		public VPathwayElement getDragElement(VPathway vp) {
			if(lastAdded != null) {
				Graphics g = vp.getPathwayElementView(lastAdded);
				if(g == null) {
					throw new IllegalArgumentException(
							"Given VPathway doesn't contain last added element"
					);
				}
				return g;
			}
			return null; //No last object
		}

		public String getDescription() {
			return "Draw new " + getName();
		}

		public URL getIconLocation() {
			return  Resources.getResourceURL("new" + getName().toLowerCase() + ".gif");
		}

		public void postInsert(PathwayElement[] newElements) {
		}
	}

	/**
	 * Template for adding a single line denoting an interaction to a Pathway.
	 */
	public static class LineTemplate extends SingleElementTemplate {
		int style;
		LineType startType;
		LineType endType;
		ConnectorType connectorType;
		String name;

		public LineTemplate(String name, int style, LineType startType, LineType endType, ConnectorType connectorType) {
			this.style = style;
			this.startType = startType;
			this.endType = endType;
			this.connectorType = connectorType;
			this.name = name;
		}

		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.LINE);
			e.setMStartX(mx);
			e.setMStartY(my);
			e.setMEndX(mx);
			e.setMEndY(my);
			e.setLineStyle(style);
			e.setStartLineType(startType);
			e.setEndLineType(endType);
			e.setConnectorType(connectorType);
			addElement(e, p);

			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathway vp) {
			Line l = (Line)super.getDragElement(vp);
			return l.getEnd().getHandle();
		}

		public String getName()
		{
			return name;
		}
	}
	
	/**
	 * Template for adding a Label to a Pathway
	 */
	public static class LabelTemplate extends SingleElementTemplate {
		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.LABEL);
			e.setMCenterX(mx);
			e.setMCenterY(my);
			e.setInitialSize();
			e.setGraphId(p.getUniqueGraphId());
			e.setTextLabel("Label");
			e.setColor(COLOR_LABEL);
			addElement(e, p);

			return new PathwayElement[] { e };
		}
		public VPathwayElement getDragElement(VPathway vp) {
			return null; //Don't drag label on insert
		}

		public String getName() {
			return "Label";
		}
	}

	/**
	 * Template for adding a DataNode to a Pathway. Pass a DataNodeType upon creation
	 */
	public static class DataNodeTemplate extends SingleElementTemplate {
		DataNodeType type;

		public DataNodeTemplate(DataNodeType type) {
			this.type = type;
		}

		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			e.setMCenterX(mx);
			e.setMCenterY(my);
			e.setMWidth(1);
			e.setMHeight(1);
			e.setRotation(0);
            if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.DATANODES_ROUNDED)) {
                 e.setShapeType(ShapeType.ROUNDED_RECTANGLE);
            }
            e.setGraphId(p.getUniqueGraphId());
			e.setDataNodeType(type);

			//Default colors for different types
			if (type.equals(DataNodeType.METABOLITE)) {
				e.setColor(COLOR_METABOLITE);
			} else if (type.equals(DataNodeType.PATHWAY)) {
				e.setColor(COLOR_PATHWAY);
				e.setMFontSize(12);
				e.setBold(true);
				e.setShapeType(ShapeType.NONE);
			}

			e.setTextLabel(type.toString());
			addElement(e, p);
			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathway vp) {
			GeneProduct g = (GeneProduct)super.getDragElement(vp);
			return g.handleSE;
		}

		public String getName() {
			return type.toString();
		}
	}

	/**
	 * Template for adding a Shape to a Pathway. Pass a ShapeType upon creation.
	 */
	public static class ShapeTemplate extends SingleElementTemplate {
		IShape type;

		public ShapeTemplate(IShape type) {
			this.type = type;
		}

		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.SHAPE);
			e.setShapeType(type);
			e.setMCenterX(mx);
			e.setMCenterY(my);
			e.setMWidth(1);
			e.setMHeight(1);
			e.setRotation(0);
			e.setGraphId(p.getUniqueGraphId());
			addElement(e, p);

			//brace
//			gdata.setOrientation(OrientationType.RIGHT);

			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathway vp) {
			GraphicsShape s = (GraphicsShape)super.getDragElement(vp);
			return s.handleSE;
		}

		public String getName() {
			return type.toString();
		}
	}

	/**
	 * Template for adding a Graphical line to a Pathway.
	 */
	public static class GraphicalLineTemplate extends SingleElementTemplate {
		int style;
		LineType startType;
		LineType endType;
		ConnectorType connectorType;
		String name;

		public GraphicalLineTemplate(String name, int style, LineType startType, LineType endType, ConnectorType connectorType) {
			this.style = style;
			this.startType = startType;
			this.endType = endType;
			this.connectorType = connectorType;
			this.name = name;
		}

		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.GRAPHLINE);
			e.setMStartX(mx);
			e.setMStartY(my);
			e.setMEndX(mx);
			e.setMEndY(my);
			e.setLineStyle(style);
			e.setStartLineType(startType);
			e.setEndLineType(endType);
			e.setConnectorType(connectorType);
			addElement(e, p);

			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathway vp) {
			Line l = (Line)super.getDragElement(vp);
			return l.getEnd().getHandle();
		}

		public String getName()
		{
			return name;
		}
	}
	
	/**
	 * Template for adding a Cellular Compartment Shape to a Pathway. Pass a ShapeType upon creation.
	 */
	public static class CellularComponentTemplate extends SingleElementTemplate {
		ShapeType type;
		CellularComponentType ccType;

		public CellularComponentTemplate(ShapeType type, CellularComponentType ccType)
		{
			this.type = type;
			this.ccType = ccType;
		}

		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.SHAPE);
			e.setShapeType(type);
			e.setMCenterX(mx);
			e.setMCenterY(my);
			e.setMWidth(1);
			e.setMHeight(1);
			e.setRotation(0);
			e.setColor(Color.LIGHT_GRAY);
			e.setLineThickness(3.0);
			if (ccType.equals(CellularComponentType.CELL) 
					|| ccType.equals(CellularComponentType.NUCLEUS) 
					|| ccType.equals(CellularComponentType.ORGANELLE))
			{
				e.setLineStyle(LineStyle.DOUBLE);
			} else if (ccType.equals(CellularComponentType.CYTOSOL) || ccType.equals(CellularComponentType.EXTRACELLULAR)
					|| ccType.equals(CellularComponentType.MEMBRANE))
			{
				e.setLineStyle(LineStyle.DASHED);
				e.setLineThickness(1.0);
			}
			e.setGraphId(p.getUniqueGraphId());
			e.setDynamicProperty(CellularComponentType.CELL_COMPONENT_KEY, ccType.toString());
			addElement(e, p);
			return new PathwayElement[] { e };
		}

		public VPathwayElement getDragElement(VPathway vp) {
			GraphicsShape s = (GraphicsShape)super.getDragElement(vp);
			return s.handleSE;
		}

		public String getName() {
			return ccType.getGpmlName();
		}
	}

	/**
	 * Template for an interaction, two datanodes with a connecting line.
	 */
	public static class InteractionTemplate implements Template {
		final static int OFFSET_LINE = 5;
		PathwayElement lastStartNode;
		PathwayElement lastEndNode;
		PathwayElement lastLine;

		LineType endType;
		LineType startType;

		int lineStyle;

		public InteractionTemplate() {
			endType = LineType.LINE;
			startType = LineType.LINE;
			lineStyle = LineStyle.SOLID;
		}

		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			//Add two GeneProduct DataNodes, connected by a line
			Template dnt = new DataNodeTemplate(DataNodeType.GENEPRODUCT);
			lastStartNode = dnt.addElements(p, mx, my)[0];
			lastStartNode.setInitialSize();
			lastEndNode = dnt.addElements(p, mx + 2 * lastStartNode.getMWidth(), my)[0];
			lastEndNode.setInitialSize();

			Template lnt = new LineTemplate("defaultline", lineStyle, startType, endType, ConnectorType.STRAIGHT);
			lastLine = lnt.addElements(p, mx, my)[0];
			lastLine.getMStart().linkTo(lastStartNode, 1, 0);
			lastLine.getMEnd().linkTo(lastEndNode, -1, 0);

			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}
		
	

		public VPathwayElement getDragElement(VPathway vp) {
			return null;
		}

		public String getName() {
			return "interaction";
		}

		public String getDescription() {
			return "Draw new " + getName();
		}

		public URL getIconLocation() {
			return  Resources.getResourceURL("new" + getName().toLowerCase() + ".gif");
		}

		public void postInsert(PathwayElement[] newElements) {
		}
	}
	/**
	 * Template for an inhibition interaction, two datanodes with a MIM_INHIBITION line.
	 */
	public static class InhibitionInteractionTemplate extends InteractionTemplate {
		@Override
		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			super.addElements(p, mx, my);
			lastLine.setEndLineType(MIMShapes.MIM_INHIBITION);
			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}
		@Override
		public String getName() {
			return "inhibition interaction";
		}
	}
	/**
	 * Template for a stimulation interaction, two datanodes with a MIM_STIMULATION line.
	 */
	public static class StimulationInteractionTemplate extends InteractionTemplate {
		@Override
		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			super.addElements(p, mx, my);
			lastLine.setEndLineType(MIMShapes.MIM_STIMULATION);
			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}
		@Override
		public String getName() {
			return "stimulation interaction";
		}
	}

	/**
	 * Template for a phosphorylation interaction, two Protein Datanodes with a MIM_MODIFICATION line.
	 */

	public static class PhosphorylationTemplate extends InteractionTemplate {
		//static final double OFFSET_CATALYST = 50;
		PathwayElement lastPhosphorylation;
		//PathwayElement lastPhosLine;

		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			super.addElements(p, mx, my);
			lastStartNode.setDataNodeType(DataNodeType.PROTEIN);
			lastEndNode.setDataNodeType(DataNodeType.PROTEIN);
			lastStartNode.setTextLabel("Protein");
			lastEndNode.setTextLabel("P-Protein");
			lastLine.setEndLineType(MIMShapes.MIM_MODIFICATION);
			
			
			PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.STATE);                        
			elt.setInitialSize();
			elt.setTextLabel("P");
			((MState)elt).linkTo (lastEndNode, 1.0, 1.0);
			elt.setShapeType(ShapeType.OVAL);
			p.add(elt);
			elt.setGeneratedGraphId();			
			
			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine };
		}

		public String getName() {
			return "Phosphorylation";
		}
	}
	
	
	/**
	 * Template for a reaction, two Metabolites with a connecting arrow, and a GeneProduct (enzyme)
	 * pointing to an anchor on that arrow.
	 */
	public static class ReactionTemplate extends InteractionTemplate {
		static final double OFFSET_CATALYST = 50;
		PathwayElement lastCatalyst;
		PathwayElement lastCatLine;

		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			super.addElements(p, mx, my);
			Template dnt = new DataNodeTemplate(DataNodeType.GENEPRODUCT);
			lastCatalyst = dnt.addElements(p, mx + lastStartNode.getMWidth(), my - OFFSET_CATALYST)[0];
			lastCatalyst.setInitialSize();
			lastCatalyst.setTextLabel("Catalyst");

			lastStartNode.setDataNodeType(DataNodeType.METABOLITE);
			lastStartNode.setColor(COLOR_METABOLITE);
			lastStartNode.setTextLabel("Substrate");

			lastEndNode.setDataNodeType(DataNodeType.METABOLITE);
			lastEndNode.setColor(COLOR_METABOLITE);
			lastEndNode.setTextLabel("Product");
			
			lastLine.setEndLineType(MIMShapes.MIM_CONVERSION);
			MAnchor anchor = lastLine.addMAnchor(0.5);

			Template lnt = new LineTemplate("line", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.STRAIGHT);
			lastCatLine = lnt.addElements(p, mx, my)[0];

			lastCatLine.getMStart().linkTo(lastCatalyst, 0, 1);
			lastCatLine.getMEnd().linkTo(anchor, 0, 0);
			lastCatLine.setEndLineType(MIMShapes.MIM_CATALYSIS);

			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine, lastCatalyst };
		}

		public String getName() {
			return "reaction";
		}
	}
	
	/**
	 * Template for a reaction, two Metabolites with a connecting arrow, and a GeneProduct (enzyme)
	 * pointing to an anchor on that arrow.
	 */
	public static class ReversibleReactionTemplate extends InteractionTemplate {
		static final double OFFSET_CATALYST = 50;
		PathwayElement lastCatalyst;
		PathwayElement lastCatalyst2;
		PathwayElement lastCatLine;
		PathwayElement lastCatLine2;
		PathwayElement lastReverseLine;

 		public PathwayElement[] addElements(Pathway p, double mx, double my) {
			super.addElements(p, mx, my);
			Template dnt = new DataNodeTemplate(DataNodeType.PROTEIN);
			lastCatalyst = dnt.addElements(p, mx + lastStartNode.getMWidth(), my - OFFSET_CATALYST)[0];
			lastCatalyst.setInitialSize();
			lastCatalyst.setTextLabel("Catalyst 1");

 			lastCatalyst2 = dnt.addElements(p, mx + lastStartNode.getMWidth(), my + OFFSET_CATALYST)[0];
			lastCatalyst2.setInitialSize();
			lastCatalyst2.setTextLabel("Catalyst 2");

 			lastStartNode.setDataNodeType(DataNodeType.METABOLITE);
			lastStartNode.setColor(COLOR_METABOLITE);
			lastStartNode.setTextLabel("Metabolite 1");

			lastEndNode.setDataNodeType(DataNodeType.METABOLITE);
			lastEndNode.setColor(COLOR_METABOLITE);
			lastEndNode.setTextLabel("Metabolite 2");
			lastLine.setEndLineType(MIMShapes.MIM_CONVERSION);

 			MAnchor anchor = lastLine.addMAnchor(0.5);

 			Template lnt = new LineTemplate("line", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.STRAIGHT);
			lastCatLine = lnt.addElements(p, mx, my)[0];

 			lastCatLine.getMStart().linkTo(lastCatalyst, 0, 1);
			lastCatLine.getMEnd().linkTo(anchor, 0, 0);
			lastCatLine.setEndLineType(MIMShapes.MIM_CATALYSIS);

 			Template rev = new LineTemplate("line", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.STRAIGHT);
			lastReverseLine = rev.addElements(p, mx, my)[0];

 			lastReverseLine.getMStart().linkTo(lastEndNode, -1, 0.5);
			lastReverseLine.getMEnd().linkTo(lastStartNode, 1, 0.5);
			lastReverseLine.setEndLineType(MIMShapes.MIM_CONVERSION);

 			MAnchor anchor2 = lastReverseLine.addMAnchor(0.5);

 			Template lnt2 = new LineTemplate("line", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.STRAIGHT);
			lastCatLine2 = lnt2.addElements(p, mx, my)[0];

 			lastCatLine2.getMStart().linkTo(lastCatalyst2, 0, -1);
			lastCatLine2.getMEnd().linkTo(anchor2, 0, 0);
			lastCatLine2.setEndLineType(MIMShapes.MIM_CATALYSIS);

 			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine, lastCatalyst, lastCatalyst2 }; //These elements are selected in PV, so users can move them around.
		}

 		public String getName() {
			return "ReversibleReaction";
		}
	}
	
}
