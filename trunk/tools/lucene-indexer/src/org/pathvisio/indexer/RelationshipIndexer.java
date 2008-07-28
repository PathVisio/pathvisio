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
package org.pathvisio.indexer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.LineType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * Indexes relationships. The following rules will apply:
 * 
 * A relation will be created when a line connects two objects
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
public class RelationshipIndexer extends IndexerBase {
	public RelationshipIndexer(String source, Pathway pathway, IndexWriter writer) {
		super(source, pathway, writer);
	}
	
	public void indexPathway() throws CorruptIndexException, IOException {
		//Find all connectors that do not connect to an anchor
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(isRelation(pe)) {
				indexRelationship(pe);
			}
		}		
	}
	
	void indexRelationship(PathwayElement relation) throws CorruptIndexException, IOException {
		Document doc = new Document();
		
		Relation r = new Relation(relation);
		addElements(FIELD_LEFT, r.getLefts(), doc);
		addElements(FIELD_RIGHT, r.getRights(), doc);
		addElements(FIELD_MEDIATOR, r.getMediators(), doc);
		
		addDocument(doc);
	}
	
	void addElements(String field, Collection<PathwayElement> elms, Document doc) {
		for(PathwayElement e : elms) {
			addElement(field, e, doc);
		}
	}
	
	void addElement(String field, PathwayElement pe, Document doc) {
		//We need something to identify the element
		//for now, use textlabel
		//TODO: use stable pathway id + graphid!
		String text = pe.getTextLabel();
		if(text != null) {
			doc.add(new Field(
					field, text, Field.Store.YES, Field.Index.TOKENIZED
			));
		} else {
			Logger.log.error(
					"Unable to add " + pe + " to relationship index: no text label"
			);
		}
	}
	
	boolean isRelation(PathwayElement pe) {
		if(pe.getObjectType() == ObjectType.LINE) {
			MPoint s = pe.getMStart();
			MPoint e = pe.getMEnd();
			if(s.isLinked() && e.isLinked()) {
				//Objects behind graphrefs should be PathwayElement
				//so not MAnchor
				if(pathway.getElementById(s.getGraphRef()) != null &&
						pathway.getElementById(e.getGraphRef()) != null)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	static class Relation {
		private Set<PathwayElement> lefts = new HashSet<PathwayElement>();
		private Set<PathwayElement> rights = new HashSet<PathwayElement>();
		private Set<PathwayElement> mediators = new HashSet<PathwayElement>();
		
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
		
		Set<PathwayElement> getLefts() { return lefts; }
		Set<PathwayElement> getRights() { return rights; }
		Set<PathwayElement> getMediators() { return mediators; }
	}
	
	/**
	 * Field that contains all elements participating on the
	 * left (start) side of this interaction
	 */
	public static final String FIELD_LEFT = "left";
	/**
	 * Field that contains all elements participating on the
	 * right (end) side of this interaction
	 */
	public static final String FIELD_RIGHT = "right";
	/**
	 * Field that contains all elements participating as
	 * mediator of this interaction
	 */
	public static final String FIELD_MEDIATOR = "mediator";
}
