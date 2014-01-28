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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.jdom.Element;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;

/**
 * Class that indexes several metadata for a pathway.
 * 
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
	 * The ontology terms to which a pathway is associated
	 */
	public static final String FIELD_ONTOLOGY = "ontology";
	
	/**
	 * The ontology ids to which a pathway is associated
	 */
	public static final String FIELD_ONTOLOGY_ID = "ontologyId";

	/**
	 * The WikiPathways description of a pathway
	 */
	public static final String FIELD_DESCRIPTION = "description";

	/**
	 * The TextLabel attribute of an element on the pathway
	 */
	public static final String FIELD_TEXTLABEL = "textlabel";

	/**
	 * Tokenized version of source field (to allow free text search on
	 * identifiers)
	 */
	public static final String FIELD_SOURCEID = "sourceId";
	
	public static final String COMMENT_DESCRIPTION = "WikiPathways-description";
	public static final String COMMENT_CATEGORY = "WikiPathways-category";
	
	/**
	 * Create a PathwayIndexer
	 * 
	 * @param source
	 *            The source of the pathway (e.g. a file or url)
	 * @param p
	 *            The pathway to index
	 * @param w
	 *            The IndexWriter to write the index to
	 */
	public PathwayIndexer(String source, Pathway p, IndexWriter w) {
		super(source, p, w);
	}

	public void indexPathway() throws CorruptIndexException, IOException {
		Document doc = new Document();
		PathwayElement info = pathway.getMappInfo();
		doc.add(new Field(FIELD_NAME, info.getMapInfoName() == null ? "" : info
				.getMapInfoName(), Field.Store.YES, Field.Index.TOKENIZED));
		doc.add(new Field(FIELD_ORGANISM, info.getOrganism() == null ? ""
				: info.getOrganism(), Field.Store.YES, Field.Index.TOKENIZED));

		doc.add(new Field(FIELD_SOURCEID, source == null ? "" : source,
				Field.Store.YES, Field.Index.TOKENIZED));

		// Process text labels
		for (PathwayElement pe : pathway.getDataObjects()) {
			String txt = pe.getTextLabel();
			if (txt != null && !"".equals(txt)) {
				doc.add(new Field(FIELD_TEXTLABEL, txt, Field.Store.YES,
						Field.Index.TOKENIZED));
			}
		}

		// Process comments
		for (PathwayElement.Comment c : info.getComments()) {
			if (COMMENT_CATEGORY.equals(c)) {
				doc.add(new Field(FIELD_CATEGORY, c.getComment(),
						Field.Store.YES, Field.Index.TOKENIZED));
			}
			if (COMMENT_DESCRIPTION.equals(c)) {
				doc.add(new Field(FIELD_DESCRIPTION, c.getComment(),
						Field.Store.YES, Field.Index.TOKENIZED));
			}
		}

		// Process ontology terms
		if(pathway.getBiopax() != null) {
			Logger.log.trace("biopax found");
			org.jdom.Document bpxml = pathway.getBiopax().getBiopax();
			Element root = bpxml.getRootElement();
			for (Object child : root.getChildren()) {
				if (child instanceof Element) {
					Element elm = (Element) child;
					if ("opencontrolledvocabulary".equals(elm.getName()
							.toLowerCase())) {
						for (Object prop : elm.getChildren()) {
							if (prop instanceof Element) {
								Element prope = (Element)prop;
								if("term".equalsIgnoreCase(prope.getName())) {
									Logger.log.trace("ont term added " + prope.getText());
									doc.add(new Field(FIELD_ONTOLOGY, prope.getText(),
											Field.Store.YES, Field.Index.TOKENIZED));
								}
								if("id".equalsIgnoreCase(prope.getName())) {
									Logger.log.trace("ont id added " + prope.getText());
									doc.add(new Field(FIELD_ONTOLOGY_ID, prope.getText().replace(":", ""),
											Field.Store.YES, Field.Index.TOKENIZED));
								}
							}
						}
					}
				}
			}
		}
		addDocument(doc);
	}
}
