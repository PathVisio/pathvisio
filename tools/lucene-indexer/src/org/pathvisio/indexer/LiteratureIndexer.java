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
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.pathvisio.biopax.BiopaxElementManager;
import org.pathvisio.biopax.BiopaxReferenceManager;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

/**
 * Indexes literature references for a pathway
 * @author thomas
 */
public class LiteratureIndexer {
	/**
	 * @see PathwayIndexer#FIELD_GRAPHID
	 */
	public static final String FIELD_GRAPHID = "graphId";
	
	/**
	 * @see PathwayIndexer#FIELD_SOURCE
	 */
	public static final String FIELD_SOURCE = PathwayIndexer.FIELD_SOURCE;
	
	/**
	 * The pubmed id
	 */
	public static final String FIELD_ID = "literature.pubmed";
	
	/**
	 * The publication title
	 */
	public static final String FIELD_TITLE = "literature.title";
	
	/**
	 * An author
	 */
	public static final String FIELD_AUTHOR = "literature.author";
	
	String source;
	Pathway pathway;
	IndexWriter writer;
	BiopaxElementManager bpMgr;
	
	public LiteratureIndexer(String source, Pathway pathway, IndexWriter writer) {
		this.source = source;
		this.pathway = pathway;
		this.writer = writer;
	}

	
	public void indexLiterature() throws CorruptIndexException, IOException {
		bpMgr = new BiopaxElementManager(pathway);
		for(PathwayElement pe : pathway.getDataObjects()) {
			indexLiterature(pe);
		}
	}
	
	void indexLiterature(PathwayElement pe) throws CorruptIndexException, IOException {
		BiopaxReferenceManager refMgr = new BiopaxReferenceManager(bpMgr, pe);
		for(PublicationXRef ref : refMgr.getPublicationXRefs()) {
			Document doc = new Document();
			doc.add(new Field(FIELD_SOURCE, source, Store.YES, Index.NO));
			if(pe.getGraphId() != null) {
				doc.add(new Field(FIELD_GRAPHID, pe.getGraphId(), Store.YES, Index.NO));
			}
			if(ref.getTitle() != null) {
				doc.add(new Field(FIELD_TITLE, ref.getTitle(), Store.YES, Index.TOKENIZED));
			}
			if(ref.getPubmedId() != null) {
				doc.add(new Field(FIELD_ID, ref.getPubmedId(), Store.YES, Index.UN_TOKENIZED));
			}
			for(String author : ref.getAuthors()) {
				doc.add(new Field(FIELD_AUTHOR, author, Store.YES, Index.TOKENIZED));
			}
			writer.addDocument(doc);
		}
	}

	/**
	 * Removes all literature for this pathway from the index.
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public void removeLiterature() throws CorruptIndexException, IOException {
		writer.deleteDocuments(new Term(FIELD_SOURCE, source));
	}
}
