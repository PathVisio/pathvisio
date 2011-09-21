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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.bridgedb.rdb.GdbProvider;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.util.FileUtils;

/**
 * Maintains an index for a collection of GPML files
 *
 * @author thomas
 */
public class GpmlIndexer {
	Set<Class<? extends IndexerBase>> indexers = new HashSet<Class<? extends IndexerBase>>();

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
		registerDefaultIndexers();
	}

	void registerDefaultIndexers() {
		indexers.add(PathwayIndexer.class);
		indexers.add(DataNodeIndexer.class);
		indexers.add(RelationshipIndexer.class);
		indexers.add(LiteratureIndexer.class);
	}

	Set<IndexerBase> createIndexers(String source, Pathway pathway, IndexWriter writer) {
		Set<IndexerBase> instances = new HashSet<IndexerBase>();

		for(Class<? extends IndexerBase> ic : indexers) {
			try {
				Constructor<? extends IndexerBase> c = ic.getConstructor(
						String.class, Pathway.class, IndexWriter.class
				);
				IndexerBase i = c.newInstance(source, pathway, writer);
				i.setGdbProvider(gdbs);
				instances.add(i);
			} catch (Exception e) {
				Logger.log.error("Unable to create indexer " + ic, e);
			}
		}
		return instances;
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
		writer.optimize();
		writer.close();
	}

	/**
	 * Updates the index for the given GPML file
	 */
	public void update(File gpmlFile) throws ConverterException,
			CorruptIndexException, IOException {
		Pathway p = new Pathway();
		String source = sourceProvider.getSource(gpmlFile);
		if (gpmlFile.exists()) {
			Logger.log.trace("Updaging index for: " + gpmlFile);
			p.readFromXml(gpmlFile, true);
			Set<IndexerBase> indexers = createIndexers(source, p, writer);
			// Add if exists on file system
			gpmlFiles.add(gpmlFile);
			// Update index
			for(IndexerBase i : indexers) {
				Logger.log.trace("Processing indexer: " + i);
				Logger.log.trace("Removing pathway " + i.getIndexerName());
				i.removePathway();
				Logger.log.trace("Indexing pathway " + i.getIndexerName());
				i.indexPathway();
			}
		} else {
			Logger.log.trace("Removing from index: " + gpmlFile);
			// Remove from index if doesn't exist
			gpmlFiles.remove(gpmlFile);
			Set<IndexerBase> indexers = createIndexers(source, p, writer);
			for(IndexerBase i : indexers) {
				i.removePathway();
			}
		}
	}
}
