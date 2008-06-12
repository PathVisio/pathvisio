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
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.GdbManager;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;
import org.pathvisio.wikipathways.WikiPathways;

/**
 * Class that indexes a pathway object
 * @author thomas
 *
 */
public class PathwayIndexer {
	/**
	 * An identifier of a DataNode xref that is on the pathway
	 */
	public static final String FIELD_ID = "id";
	/**
	 * An identifier/code combination
	 * of a DataNode xref that is on the pathway.
	 * The combination is of the form:
	 * "identifier:code", where code is the system code
	 * obtained by {@link DataSource#getSystemCode()}.
	 */
	public static final String FIELD_ID_CODE = "id.database";
	/**
	 * An identifier that was found by looking up all cross references
	 * for a DataNode xref on the pathway.
	 */
	public static final String FIELD_XID = "x.id";
	/**
	 * An identifier/code combination
	 * of a DataNode xref that was found by looking up all cross references
	 * for a DataNode xref on the pathway.
	 * The combination is of the form:
	 * "identifier:code", where code is the system code
	 * obtained by {@link org.pathvisio.model.DataSource#getSystemCode()}.
	 */
	public static final String FIELD_XID_CODE = "x.id.database";
	/**
	 * The name of a pathway
	 */
	public static final String FIELD_NAME = "name";
	/**
	 * The organism of a pathway
	 */
	public static final String FIELD_ORGANISM = "organism";
	/**
	 * The source of a pathway (e.g. an url or file where the pathway
	 * is stored)
	 */
	public static final String FIELD_SOURCE = "source";
	
	/**
	 * The WikiPathways category a pathway belongs to
	 */
	public static final String FIELD_CATEGORY = "category";
	
	/**
	 * The WikiPathways description of a pathway
	 */
	public static final String FIELD_DESCRIPTION = "description";
	
	String source;
	Pathway pathway;
	IndexWriter writer;
	GdbManager gdbManager;
	
	/**
	 * Create a PathwayIndexer
	 * @param source The source of the pathway (e.g. a file or url)
	 * @param p The pathway to index
	 * @param w The IndexWriter to write the index to
	 */
	public PathwayIndexer(String source, Pathway p, IndexWriter w) {
		this.source = source;
		this.pathway = p;
		this.writer = w;
	}
	
	/**
	 * Set the GdbManager to use for looking up cross-references
	 * for datanode annotations. If the GdbManager is null, no cross
	 * references will be included in the index
	 */
	public void setGdbManager(GdbManager mgr) {
		this.gdbManager = gdbManager;
	}
	
	public void indexPathway() throws CorruptIndexException, IOException {
		Document doc = new Document();
		doc.add(new Field(FIELD_SOURCE, source, Field.Store.YES, Field.Index.NO));
		
		PathwayElement info = pathway.getMappInfo();
		doc.add(
				new Field(
						FIELD_NAME, 
						info.getMapInfoName() == null ? "" : info.getMapInfoName(), 
						Field.Store.YES, 
						Field.Index.UN_TOKENIZED
				)
		);
		doc.add(
				new Field(
						FIELD_ORGANISM,
						info.getOrganism() == null ? "" : info.getOrganism(), 
						Field.Store.YES, 
						Field.Index.UN_TOKENIZED
				)
		);
		//Process DataNodes
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.DATANODE) {
				indexDataNode(pe, doc);
			}
		}
		//Process comments
		for(PathwayElement.Comment c : info.getComments()) {
			if(WikiPathways.COMMENT_CATEGORY.equals(c)) {
				doc.add(new Field(
						FIELD_CATEGORY,
						c.getComment(),
						Field.Store.YES,
						Field.Index.UN_TOKENIZED
				));
			}
			if(WikiPathways.COMMENT_DESCRIPTION.equals(c)) {
				doc.add(new Field(
						FIELD_DESCRIPTION,
						c.getComment(),
						Field.Store.YES,
						Field.Index.TOKENIZED
				));
			}
		}
		writer.addDocument(doc);
	}
	
	void indexDataNode(PathwayElement pe, Document doc) {
		Xref xref = pe.getXref();

		addCrossRef(xref, doc, FIELD_ID, FIELD_ID_CODE);

		//Add cross references if connected
		if(gdbManager != null && gdbManager.isConnected()) {
			Gdb gdb = gdbManager.getCurrentGdb();
			List<Xref> crossRefs = gdb.getCrossRefs(xref);
			for(Xref c : crossRefs) {
				addCrossRef(c, doc, FIELD_XID, FIELD_XID_CODE);
			}
		}
	}

	void addCrossRef(Xref xref, Document doc, String field_id, String field_id_code) {
		if(xref != null) {
			String id = xref.getId();
			String code = "";
			if(xref.getDataSource() != null) {
				code = xref.getDataSource().getSystemCode();
			}
			doc.add(
				new Field(
						field_id,
						id, 
						Field.Store.YES, 
						Field.Index.UN_TOKENIZED
				)
			);
			doc.add(
				new Field(
						field_id_code,
						id + ":" + code, 
						Field.Store.YES, 
						Field.Index.UN_TOKENIZED
				)
			);
		}
	}
}
