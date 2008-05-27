package org.pathvisio.indexer;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

public class CreateIndex {

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
				" {indexDir} {pathwayDir}\n" +
				"- indexDir: the location to save the index to\n" +
				"- pathwayDir: the directory containing the GPML files to index"
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
		doc.add(new Field("file", file.getAbsolutePath(), Field.Store.YES, Field.Index.NO));
		
		PathwayElement info = pathway.getMappInfo();
		doc.add(
				new Field(
						"name", 
						info.getMapInfoName() == null ? "" : info.getMapInfoName(), 
						Field.Store.YES, 
						Field.Index.UN_TOKENIZED
				)
		);
		doc.add(
				new Field(
						"organism",
						info.getOrganism() == null ? "" : info.getOrganism(), 
						Field.Store.YES, 
						Field.Index.UN_TOKENIZED
				)
		);
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.DATANODE) {
				Xref xref = pe.getXref();
				if(xref != null) {
					doc.add(
						new Field(
								"xref.id", 
								xref.getId(), 
								Field.Store.YES, 
								Field.Index.UN_TOKENIZED
						)
					);
					doc.add(
						new Field(
								"xref.database", 
								xref.getDatabaseName(), 
								Field.Store.YES, 
								Field.Index.UN_TOKENIZED
						)
					);
				}
			}
		}
		writer.addDocument(doc);
	}
}
