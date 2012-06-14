// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;

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

	XrefAnalyzer analyzer;

	public DataNodeIndexer(String source, Pathway pathway, IndexWriter writer) {
		super(source, pathway, writer);
		analyzer = new XrefAnalyzer();
	}

	public void indexPathway() throws CorruptIndexException, IOException {
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.DATANODE) {
				indexDataNode(pe);
			}
		}
	}

	void indexDataNode(PathwayElement pe) throws CorruptIndexException, IOException {

		Set<Xref> addedXrefs = new HashSet<Xref>();

		Document doc = new Document();
		if(pe.getGraphId() != null) {
			doc.add(new Field(FIELD_GRAPHID, pe.getGraphId(), Store.YES, Index.NO));
		}
		Xref xref = pe.getXref();

		if(xref == null || xref.getDataSource() == null || xref.getId() == null) {
			return;
		}
		addCrossRef(xref, doc, FIELD_ID, FIELD_ID_CODE);
		addCrossRef(xref, doc, FIELD_XID, FIELD_XID_CODE); //Add original xref as mapped as well

		//Add cross references if connected
		Organism organism = Organism.fromLatinName(pathway.getMappInfo().getOrganism());
		if(gdbs != null) {
			for(IDMapper gdb : gdbs.getGdbs(organism)) {
				if(gdb != null && gdb.isConnected()) {
					try {
						Set<Xref> crossRefs = gdb.mapID(xref);
						for(Xref c : crossRefs) {
							if(!addedXrefs.contains(c)) {
								addCrossRef(c, doc, FIELD_XID, FIELD_XID_CODE);
								addedXrefs.add(c);
							}
						}
					} catch(IDMapperException e) {
						Logger.log.error("Unable to fetch cross references", e);
					}
				}
			}
		}
		addDocument(doc, analyzer);
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
						Field.Index.TOKENIZED
				)
			);
			//Also add untokenized for exact searches
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

	class XrefAnalyzer extends Analyzer {
		SimpleAnalyzer simpleAnalyzer = new SimpleAnalyzer();

		public TokenStream tokenStream(String field, Reader reader) {
			if(FIELD_XID_CODE.equals(field) || FIELD_ID_CODE.equals(field)) {
				return new CharTokenizer(reader) {
					protected boolean isTokenChar(char c) {
						return c != ':';
					}
				};
			} else {
				return simpleAnalyzer.tokenStream(field, reader);
			}
		}
	}
}
