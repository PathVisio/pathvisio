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
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.util.FileUtils;

/**
 * Maintains an index for a collection of GPML files
 * 
 * @author thomas
 */
public class GpmlIndexer {
	IndexWriter writer;
	Set<File> gpmlFiles = new HashSet<File>();
	GdbProvider gdbs;
	SourceProvider sourceProvider = new SourceProvider() {
		public String getSource(File gpmlFile) { return gpmlFile.getAbsolutePath(); }
	};
	
	/**
	 * Create an index for GPML file.
	 * 
	 * @param indexPath
	 *            The path to store the index
	 * @param gpmlPath
	 *            The path containing the GPML files to index
	 * @throws IOException 
	 * @throws ConverterException 
	 * @throws LockObtainFailedException 
	 * @throws CorruptIndexException 
	 */
	public GpmlIndexer(File indexPath, File gpmlPath, GdbProvider gdbs) throws CorruptIndexException, LockObtainFailedException, ConverterException, IOException {
		this(
			new IndexWriter(indexPath, new SimpleAnalyzer()), 
			new HashSet<File>(FileUtils.getFiles(gpmlPath, "gpml", true)), 
			gdbs
		);
	}

	public GpmlIndexer(IndexWriter indexWriter, Set<File> gpmlFiles,
			GdbProvider gdbs) throws ConverterException {
		this.gpmlFiles.addAll(gpmlFiles);
		this.gdbs = gdbs;
		this.writer = indexWriter;
	}

	public void setWriter(IndexWriter writer) {
		this.writer = writer;
	}

	public void setSourceProvider(SourceProvider spv) {
		sourceProvider = spv;
	}
	
	/**
	 * Updates the index for all listed files
	 */
	public void update() throws ConverterException, CorruptIndexException,
			IOException {
		for (File f : gpmlFiles) {
			update(f);
		}
	}

	/**
	 * Close the index writer
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void close() throws CorruptIndexException, IOException {
		writer.close();
	}
	
	/**
	 * Updates the index for the given GPML file
	 */
	public void update(File gpmlFile) throws ConverterException,
			CorruptIndexException, IOException {
		Pathway p = new Pathway();
		String source = sourceProvider.getSource(gpmlFile);
		PathwayIndexer pwi = new PathwayIndexer(source, p, writer);
		RelationshipIndexer rli = new RelationshipIndexer(source, p, writer);
		DataNodeIndexer dni = new DataNodeIndexer(source, p, writer);
		LiteratureIndexer li = new LiteratureIndexer(source, p, writer);
		if (gpmlFile.exists()) {
			Logger.log.trace("Updaging index for: " + gpmlFile);
			// Add if exists on file system
			gpmlFiles.add(gpmlFile);
			// Update index
			p.readFromXml(gpmlFile, true);
			pwi.indexPathway();
			dni.setGdbProvider(gdbs);
			dni.indexDataNodes();
			rli.indexRelationships();
			li.indexLiterature();
		} else {
			Logger.log.trace("Removing from index: " + gpmlFile);
			// Remove from index if doesn't exist
			gpmlFiles.remove(gpmlFile);
			pwi.removePathway();
			dni.removeDataNodes();
			rli.removeRelationships();
			li.removeLiterature();
		}
	}
}
