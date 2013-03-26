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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.pathvisio.core.biopax.BiopaxReferenceManager;
import org.pathvisio.core.biopax.PublicationXref;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;

/**
 * Indexes literature references for a pathway
 * @author thomas
 */
public class LiteratureIndexer extends IndexerBase {
	/**
	 * @see PathwayIndexer#FIELD_GRAPHID
	 */
	public static final String FIELD_GRAPHID = "graphId";

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

	public LiteratureIndexer(String source, Pathway pathway, IndexWriter writer) {
		super(source, pathway, writer);
	}

	public void indexPathway() throws CorruptIndexException, IOException {
		for(PathwayElement pe : pathway.getDataObjects()) {
			indexLiterature(pe);
		}
	}

	void indexLiterature(PathwayElement pe) throws CorruptIndexException, IOException {
		BiopaxReferenceManager refMgr = new BiopaxReferenceManager(pe);
		for(PublicationXref ref : refMgr.getPublicationXRefs()) {
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
			addDocument(doc);
		}
	}
}
