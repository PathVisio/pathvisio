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
package org.pathvisio.util;

import java.util.HashSet;
import java.util.Set;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.LineType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * Class to parse a relation between GPML objects, e.g. a biochemical reaction.
 * A relation can be created from a line that connects two objects
 * of type datanode, shape or label.
 * The following fields will be created:
 * - LEFT: an element that acts on the left side of an interaction
 * - RIGHT: an element that acts on the right side of an interaction
 * - MEDIATOR: an element that acts as mediator of an interaction
 * - SOURCE: the pathway containing the relation
 * 
 * The following example illustrates how the fields will be assigned.
 * 
 * Consider the following interaction:
 *  
 *            F      E
 *            |	    /
 *            v    /
 * A ---o-----o---o--> B
 *     /      T
 *    /       |
 *   D        C(C1, C2)
 * 
 * Where C is a group that contains C1 and C2.
 * 
 * The line A-B will serve as base for the relation, A will be
 * added to the LEFT field, B to the RIGHT field.
 * For all other elements that are connected to anchors of this line, the
 * following rules apply:
 * 
 * - If the line starts at the anchor and ends at the element, the element will
 * be added to the LEFT field
 * - If the line starts at the element, ends at the anchor and has *no* end
 * linetype (no arrow, T-bar or other shape), the element will be added to the RIGHT
 * field
 * - Else, the element will be added to the MEDIATOR field.
 * 
 * Additionally, if the element to be added is a group, all nested elements will
 * be added recursively.
 * 
 * So in the example, the following fields will be created:
 * A: LEFT
 * D: LEFT
 * F: MEDIATOR
 * C: MEDIATOR
 * C1:MEDIATOR
 * C2:MEDIATOR
 * E: RIGHT
 * B: RIGHT
 * 
 * @author thomas
 */
public class Relation {
	private Set<PathwayElement> lefts = new HashSet<PathwayElement>();
	private Set<PathwayElement> rights = new HashSet<PathwayElement>();
	private Set<PathwayElement> mediators = new HashSet<PathwayElement>();
	
	/**
	 * Parse a relation.
	 * @param relationLine The line that defines the relation.
	 */
	public Relation(PathwayElement relationLine) {
		if(relationLine.getObjectType() != ObjectType.LINE) {
			throw new IllegalArgumentException("Object type should be line!");
		}
		Pathway pathway = relationLine.getParent();
		if(pathway == null) {
			throw new IllegalArgumentException("Object has no parent pathway");
		}
		//Add obvious left and right
		addLeft(pathway.getElementById(
				relationLine.getMStart().getGraphRef()
		));
		addRight(pathway.getElementById(
				relationLine.getMEnd().getGraphRef()
		));
		//Find all connecting lines (via anchors)
		for(MAnchor ma : relationLine.getMAnchors()) {
			for(GraphRefContainer grc : ma.getReferences()) {
				if(grc instanceof MPoint) {
					MPoint mp = (MPoint)grc;
					PathwayElement line = mp.getParent();
					if(line.getMStart() == mp) {
						//Start linked to anchor, make it a 'right'
						if(line.getMEnd().isLinked()) {
							addRight(pathway.getElementById(line.getMEnd().getGraphRef()));
						}
					} else {
						//End linked to anchor
						if(line.getEndLineType() == LineType.LINE) {
							//Add as 'left'
							addLeft(pathway.getElementById(line.getMStart().getGraphRef()));
						} else {
							//Add as 'mediator'
							addMediator(pathway.getElementById(line.getMStart().getGraphRef()));
						}
					}
				} else {
					Logger.log.warn("unsupported GraphRefContainer: " + grc);
				}
			}
		}
	}
	
	void addLeft(PathwayElement pwe) {
		addElement(pwe, lefts);
	}
	
	void addRight(PathwayElement pwe) {
		addElement(pwe, rights);
	}
	
	void addMediator(PathwayElement pwe) {
		addElement(pwe, mediators);
	}
	
	void addElement(PathwayElement pwe, Set<PathwayElement> set) {
		if(pwe != null) {
			//If it's a group, add all subelements
			if(pwe.getObjectType() == ObjectType.GROUP) {
				for(PathwayElement ge : pwe.getParent().getGroupElements(pwe.getGroupId())) {
					addElement(ge, set);
				}
			}
			set.add(pwe);
		}
	}
	
	public Set<PathwayElement> getLefts() { return lefts; }
	public Set<PathwayElement> getRights() { return rights; }
	public Set<PathwayElement> getMediators() { return mediators; }
}
