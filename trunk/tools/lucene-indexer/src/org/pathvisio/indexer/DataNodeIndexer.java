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
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.pathvisio.data.DataException;
import org.pathvisio.data.Gdb;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

/**
 * Indexes every DataNode, including
 * it's synonym database references
 * @author thomas
 */
public class DataNodeIndexer extends IndexerBase {
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
	
	public DataNodeIndexer(String source, Pathway pathway, IndexWriter writer) {
		super(source, pathway, writer);
	}
	
	public void indexPathway() throws CorruptIndexException, IOException {
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.DATANODE) {
				indexDataNode(pe);
			}
		}
	}
	
	void indexDataNode(PathwayElement pe) throws CorruptIndexException, IOException {
		Document doc = new Document();
		doc.add(new Field(FIELD_SOURCE, source, Store.YES, Index.NO));
		doc.add(new Field(FIELD_GRAPHID, pe.getGraphId(), Store.YES, Index.NO));
		
		Xref xref = pe.getXref();

		addCrossRef(xref, doc, FIELD_ID, FIELD_ID_CODE);

		//Add cross references if connected
		Organism organism = Organism.fromLatinName(pathway.getMappInfo().getOrganism());
		if(gdbs != null) {
			for(Gdb gdb : gdbs.getGdbs(organism)) {
				if(gdb != null && gdb.isConnected()) {
					try {
						List<Xref> crossRefs = gdb.getCrossRefs(xref);
						for(Xref c : crossRefs) {
							addCrossRef(c, doc, FIELD_XID, FIELD_XID_CODE);
						}
					} catch(DataException e) {
						Logger.log.error("Unable to fetch cross references", e);
					}
				}
			}
		}
		addDocument(doc);
	}

	void addCrossRef(Xref xref, Document doc, String field_id, String field_id_code) {
		if(xref != null) {
			String id = xref.getId();
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
						xref2string(xref), 
						Field.Store.YES, 
						Field.Index.UN_TOKENIZED
				)
			);
		}
	}
	
	public static String xref2string(Xref xref) {
		String id = xref.getId();
		String code = "";
		if(xref.getDataSource() != null) {
			code = xref.getDataSource().getSystemCode();
		}
		return id + ":" + code;
	}
}
