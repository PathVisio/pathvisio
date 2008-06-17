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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
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
 * - PATHWAY: the pathway in which the interaction occurs
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
 *   D        C
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
 * So in the example, the following fields will be created:
 * A: LEFT
 * D: LEFT
 * F: MEDIATOR
 * C: MEDIATOR
 * E: RIGHT
 * B: RIGHT
 * 
 * @author thomas
 */
public class RelationshipIndexer {
	Pathway pathway;
	IndexWriter writer;
	String source;
	
	public RelationshipIndexer(String source, Pathway pathway, IndexWriter writer) {
		this.pathway = pathway;
		this.writer = writer;
		this.source = source;
	}
	
	public void index() throws CorruptIndexException, IOException {
		//Find all connectors that do not connect to an anchor
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(isRelation(pe)) {
				indexRelationship(pe);
			}
		}
		
	}
	
	void indexRelationship(PathwayElement relation) throws CorruptIndexException, IOException {
		Document doc = new Document();
		doc.add(new Field(
				FIELD_PATHWAY, source, Field.Store.YES, Field.Index.UN_TOKENIZED
		));
		
		//Add obvious left and right
		PathwayElement left = pathway.getElementById(
				relation.getMStart().getGraphRef()
		);
		addElement(FIELD_LEFT, left, doc);
		PathwayElement right = pathway.getElementById(
				relation.getMEnd().getGraphRef()
		);
		addElement(FIELD_RIGHT, right, doc);
		
		writer.addDocument(doc);
	}
	
	void addElement(String field, PathwayElement pe, Document doc) {
		//We need something to identify the element
		//for now, use textlabel
		//TODO: use stable pathway id + graphid!
		String text = pe.getTextLabel();
		if(text != null) {
		doc.add(new Field(
				field, text, Field.Store.YES, Field.Index.UN_TOKENIZED
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
	
	public static final String FIELD_PATHWAY = "pathway";
	public static final String FIELD_LEFT = "left";
	public static final String FIELD_RIGHT = "right";
	public static final String FIELD_MEDIATOR = "mediator";
}
