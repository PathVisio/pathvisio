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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.pathvisio.data.GdbManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;

public class CreateIndex {
	private GdbManager gdbManager;
	
	public CreateIndex() {
		gdbManager = new GdbManager();
	}
	
	public CreateIndex(GdbManager gdbManager) {
		this.gdbManager = gdbManager;
	}
	
	public static void main(String[] args) {
		Logger.log.setStream(System.err);
		
		if(args.length != 2) {
			printHelp();
		}
		
		new CreateIndex().index(new File(args[1]), new File(args[0]));
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

	void index(File pathwayDir, File indexDir) {
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
	
	
	void index(IndexWriter writer, File dir) throws CorruptIndexException, ConverterException, IOException {
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
	
	void indexFile(IndexWriter writer, File file) throws ConverterException, CorruptIndexException, IOException {
		Pathway p = new Pathway();
		p.readFromXml(file, true);
		PathwayIndexer pwIndexer = new PathwayIndexer(file.getAbsolutePath(), p, writer);
		pwIndexer.setGdbManager(gdbManager);
		pwIndexer.indexPathway();
	}
}
