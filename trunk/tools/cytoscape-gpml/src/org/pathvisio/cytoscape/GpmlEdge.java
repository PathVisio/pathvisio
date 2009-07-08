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
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

import giny.view.EdgeView;
import giny.view.GraphView;

import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.LinAlg;
import org.pathvisio.view.LinAlg.Point;

/**
 * Class that holds a Cytoscape edge that has a GPML representation, which is stored
 * as edge attributes
 * @author thomas
 *
 */
public class GpmlEdge extends GpmlNetworkElement<CyEdge> {
	GpmlNode source;
	GpmlNode target;
	
	/**
	 * Constructor for this class. Creates a new GpmlEdge, based on the given
	 * edge and PathwayElement
	 * @param parent
	 * @param pwElm
	 */
	public GpmlEdge(CyEdge parent, PathwayElement pwElm, GpmlNode source, 
			GpmlNode target, AttributeMapper attributeMapper) {
		super(parent, pwElm);
		this.source = source;
		this.target = target;
		updateFromGpml(attributeMapper);
	}
		
	/**
	 * Creates a new GpmlEdge based on the given node view. A GPML representation
	 * (PathwayElement of type Line) will automatically created based on the edge view.
	 * @param parent
	 */
	public GpmlEdge(EdgeView eview, GpmlNode source, GpmlNode target, AttributeMapper attributeMapper) {
		this((CyEdge)eview.getEdge(), PathwayElement.createPathwayElement(ObjectType.LINE), source, target, attributeMapper);
		PathwayElement psource = source.getPathwayElement();
		PathwayElement ptarget = target.getPathwayElement();

		pwElmOrig.setStartGraphRef(source.getGraphIdContainer().getGraphId());
		pwElmOrig.setEndGraphRef(target.getGraphIdContainer().getGraphId());
	
		pwElmOrig.setMStartX(psource.getMCenterX());
		pwElmOrig.setMStartY(psource.getMCenterY());
		pwElmOrig.setMEndX(ptarget.getMCenterX());
		pwElmOrig.setMEndY(ptarget.getMCenterY());
		
		setPwElmCy(pwElmOrig.copy());
		//TODO: map line style and arrowheads
	}

	public CyAttributes getCyAttributes() {
		return Cytoscape.getEdgeAttributes();
	}

	public String getParentIdentifier() {
		return getParent().getIdentifier();
	}

	public void updateFromCytoscape(GraphView view, AttributeMapper attributeMapper) {
		super.updateFromCytoscape(view, attributeMapper);
		
		PathwayElement psource = source.getPathwayElement(view, attributeMapper);
		PathwayElement ptarget = target.getPathwayElement(view, attributeMapper);

		getPwElmCy().setStartGraphRef(source.getGraphIdContainer().getGraphId());
		getPwElmCy().setEndGraphRef(target.getGraphIdContainer().getGraphId());
				
		fixCoordinates(getPwElmCy(), psource, ptarget);
	}
	
	/**
	 * Find the border to connect to. Returns a point containing
	 * the relative coordinates.
	 */
	private Point findBorder(PathwayElement pwElm, double angle) {
		Point bp = new Point(-1, -1);

		double diagAngle = Math.atan(pwElm.getMHeight() / pwElm.getMWidth());
		double angleA = Math.abs(angle);
		/*    da < |a| < da + pi/2
		       \   /
		        \ /
|a| > da + pi/2	 \  |a| < da 
		        / \
		       /   \
		         da < |a| < da + pi/2
		 */

		if(angleA >= diagAngle && angleA <= diagAngle + Math.PI/2) {
			bp.x = 0; //center
			if(angle < 0) {
				bp.y += 2;
			}
		}
		if(angleA < diagAngle || angleA > diagAngle + Math.PI/2) {
			bp.y = 0;
			if(angle < Math.PI / 2 && angle > -Math.PI / 2) {
				bp.x += 2;
			}
		}

		return bp;
	}
	
	private Point[] findBorders(PathwayElement start, PathwayElement end) {
		Point psource = new Point(start.getMCenterX(), start.getMCenterY());
		Point ptarget = new Point(end.getMCenterX(), end.getMCenterY());
		
		double angle = LinAlg.angle(ptarget.subtract(psource), new Point(1, 0));
		double astart = angle;
		double aend = angle;
		if(angle < 0) 	aend += Math.PI;
		else 			aend -= Math.PI;
		if(angle == 0) {
			if(psource.x > ptarget.x) {
				aend += Math.PI;
				astart += Math.PI;
			}
		}
		Point pstart = findBorder(start, astart);
		Point pend = findBorder(end, aend);
		return new Point[] { pstart, pend };
	}
	
	private void fixCoordinates(PathwayElement toSet, PathwayElement source, PathwayElement target) {
		Point[] borders = findBorders(source, target);
		Point sp = borders[0];
		Point ep = borders[1];
		toSet.getMStart().setRelativePosition(sp.x, sp.y);
		toSet.getMEnd().setRelativePosition(ep.x, ep.y);
	}
}
