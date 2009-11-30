// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.wikipathways.WikiPathways;

/**
 * Class that indexes several metadata for a pathway.
 * @author thomas
 *
 */
public class PathwayIndexer extends IndexerBase {
	/**
	 * The name of a pathway
	 */
	public static final String FIELD_NAME = "name";
	/**
	 * The organism of a pathway
	 */
	public static final String FIELD_ORGANISM = "organism";

	/**
	 * The WikiPathways category a pathway belongs to
	 */
	public static final String FIELD_CATEGORY = "category";

	/**
	 * The WikiPathways description of a pathway
	 */
	public static final String FIELD_DESCRIPTION = "description";

	/**
	 * The TextLabel attribute of an element on the pathway
	 */
	public static final String FIELD_TEXTLABEL = "textlabel";

	/**
	 * Create a PathwayIndexer
	 * @param source The source of the pathway (e.g. a file or url)
	 * @param p The pathway to index
	 * @param w The IndexWriter to write the index to
	 */
	public PathwayIndexer(String source, Pathway p, IndexWriter w) {
		super(source, p, w);
	}

	public void indexPathway() throws CorruptIndexException, IOException {
		Document doc = new Document();
		PathwayElement info = pathway.getMappInfo();
		doc.add(
				new Field(
						FIELD_NAME,
						info.getMapInfoName() == null ? "" : info.getMapInfoName(),
						Field.Store.YES,
						Field.Index.TOKENIZED
				)
		);
		doc.add(
				new Field(
						FIELD_ORGANISM,
						info.getOrganism() == null ? "" : info.getOrganism(),
						Field.Store.YES,
						Field.Index.TOKENIZED
				)
		);

		//Process text labels
		for(PathwayElement pe : pathway.getDataObjects()) {
			String txt = pe.getTextLabel();
			if(txt != null && !"".equals(txt)) {
				doc.add(
						new Field(
								FIELD_TEXTLABEL,
								txt,
								Field.Store.YES,
								Field.Index.TOKENIZED
						)
					);
			}
		}

		//Process comments
		for(PathwayElement.Comment c : info.getComments()) {
			if(WikiPathways.COMMENT_CATEGORY.equals(c)) {
				doc.add(new Field(
						FIELD_CATEGORY,
						c.getComment(),
						Field.Store.YES,
						Field.Index.TOKENIZED
				));
			}
			if(WikiPathways.COMMENT_DESCRIPTION.equals(c)) {
				doc.add(new Field(
						FIELD_DESCRIPTION,
						c.getComment(),
						Field.Store.YES,
						Field.Index.TOKENIZED
				));
			}
		}
		addDocument(doc);
	}
}
