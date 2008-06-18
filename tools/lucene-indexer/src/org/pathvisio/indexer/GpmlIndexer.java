package org.pathvisio.indexer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
			new IndexWriter(indexPath, new StandardAnalyzer()), 
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
	 * Updates the index for the given GPML file
	 */
	public void update(File gpmlFile) throws ConverterException,
			CorruptIndexException, IOException {
		Pathway p = new Pathway();
		String source = gpmlFile.getAbsolutePath();
		PathwayIndexer pwi = new PathwayIndexer(source, p, writer);
		RelationshipIndexer rli = new RelationshipIndexer(source, p, writer);
		if (gpmlFile.exists()) {
			Logger.log.trace("Updaging index for: " + gpmlFile);
			// Add if exists on file system
			gpmlFiles.add(gpmlFile);
			// Update index
			p.readFromXml(gpmlFile, true);
			pwi.setGdbProvider(gdbs);
			pwi.indexPathway();
			rli.indexRelationships();
		} else {
			Logger.log.trace("Removing from index: " + gpmlFile);
			// Remove from index if doesn't exist
			gpmlFiles.remove(gpmlFile);
			pwi.removePathway();
			rli.removeRelationships();
		}
	}
}
