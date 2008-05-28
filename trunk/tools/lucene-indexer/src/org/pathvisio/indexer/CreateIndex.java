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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.GdbManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

public class CreateIndex {
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
	 * The file for a pathway
	 */
	public static final String FIELD_FILE = "file";
	
	public static void main(String[] args) {
		Logger.log.setStream(System.err);
		
		if(args.length != 2) {
			printHelp();
		}
		
		index(new File(args[1]), new File(args[0]));
	}
	
	static void printHelp() {
		System.out.println(
				"Usage:\n" +
				"java org.pathvisio.indexer.IndexerMain " +
				" {indexDir} {pathwayDir} [synonymDatabases]\n" +
				"- indexDir: the location to save the index to\n" +
				"- pathwayDir: the directory containing the GPML files to index\n"
		);
	}

	static void index(File pathwayDir, File indexDir) {
		//Build an index of all GPML files in the given directory		
		Logger.log.trace("Start indexing " + pathwayDir + " to " + indexDir);
		
		try {
			IndexWriter writer = new IndexWriter(indexDir, new StandardAnalyzer(), true);
			index(writer, pathwayDir);
			writer.close();
		} catch (Exception e) {
			Logger.log.error("Error while indexing pathways", e);
			System.exit(-1);
		}
	}
	
	
	static void index(IndexWriter writer, File dir) throws CorruptIndexException, ConverterException, IOException {
	    File[] files = dir.listFiles();

	    for (int i=0; i < files.length; i++) {
	        File f = files[i];
	        if (f.isDirectory()) {
	           index(writer, f);
	        } else if (f.getName().endsWith(".gpml")) {
	           indexFile(writer, f);
	        }
	    }
	}
	
	static void indexFile(IndexWriter writer, File file) throws ConverterException, CorruptIndexException, IOException {
		Pathway pathway = new Pathway();
		GpmlFormat.readFromXml(pathway, file, true);
		
		Document doc = new Document();
		doc.add(new Field(FIELD_FILE, file.getAbsolutePath(), Field.Store.YES, Field.Index.NO));
		
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
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.DATANODE) {
				indexDataNode(pe, doc);
			}
		}
		writer.addDocument(doc);
	}
	
	static void indexDataNode(PathwayElement pe, Document doc) {
		Xref xref = pe.getXref();

		addCrossRef(xref, doc, FIELD_ID, FIELD_ID_CODE);

		//Add cross references if connected
		if(GdbManager.isConnected()) {
			Gdb gdb = GdbManager.getCurrentGdb();
			List<Xref> crossRefs = gdb.getCrossRefs(xref);
			for(Xref c : crossRefs) {
				addCrossRef(c, doc, FIELD_XID, FIELD_XID_CODE);
			}
		}
	}

	static void addCrossRef(Xref xref, Document doc, String field_id, String field_id_code) {
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
