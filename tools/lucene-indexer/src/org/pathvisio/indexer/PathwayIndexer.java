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
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.pathvisio.data.Gdb;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.wikipathways.WikiPathways;

/**
 * Class that indexes a pathway object
 * @author thomas
 *
 */
public class PathwayIndexer {
	/**
	 * The name of a pathway
	 */
	public static final String FIELD_NAME = "name";
	/**
	 * The organism of a pathway
	 */
	public static final String FIELD_ORGANISM = "organism";
	/**
	 * The source of a pathway (e.g. an url or file where the pathway
	 * is stored). This field should be unique for each pathway in the index.
	 */
	public static final String FIELD_SOURCE = "source";
	
	/**
	 * The WikiPathways category a pathway belongs to
	 */
	public static final String FIELD_CATEGORY = "category";
	
	/**
	 * The WikiPathways description of a pathway
	 */
	public static final String FIELD_DESCRIPTION = "description";
	
	/**
	 * A graphId of an element on the pathway
	 */
	public static final String FIELD_GRAPHID = "graphid";
	
	String source;
	Pathway pathway;
	IndexWriter writer;
	
	/**
	 * Create a PathwayIndexer
	 * @param source The source of the pathway (e.g. a file or url)
	 * @param p The pathway to index
	 * @param w The IndexWriter to write the index to
	 */
	public PathwayIndexer(String source, Pathway p, IndexWriter w) {
		this.source = source;
		this.pathway = p;
		this.writer = w;
	}
	
	/**
	 * Removes the pathway from the index. The pathway is identified
	 * by the {@link #FIELD_SOURCE} field.
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public void removePathway() throws CorruptIndexException, IOException {
		writer.deleteDocuments(new Term(FIELD_SOURCE, source));
	}
	
	/**
	 * Updates or adds the pathway to the index
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void indexPathway() throws CorruptIndexException, IOException {
		Document doc = new Document();
		doc.add(new Field(FIELD_SOURCE, source, Field.Store.YES, Field.Index.NO));
		
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
						Field.Index.UN_TOKENIZED
				)
		);
		//Process graph ids
		for(String id : pathway.getGraphIds()) {
			doc.add(
				new Field(
						FIELD_GRAPHID,
						id,
						Field.Store.YES,
						Field.Index.UN_TOKENIZED
				)
			);
		}
		
		//Process comments
		for(PathwayElement.Comment c : info.getComments()) {
			if(WikiPathways.COMMENT_CATEGORY.equals(c)) {
				doc.add(new Field(
						FIELD_CATEGORY,
						c.getComment(),
						Field.Store.YES,
						Field.Index.UN_TOKENIZED
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
		writer.updateDocument(new Term(FIELD_SOURCE, source), doc);
	}
}
