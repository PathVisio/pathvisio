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

import junit.framework.TestCase;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.PreferenceManager;

public class Search extends TestCase {
	static final File pathwayDir = new File("tools/lucene-indexer/test-data");
	static final File indexDir = new File("tools/lucene-indexer/test-index");
	
	public void testCreateIndex() {
		try {
			Engine.init();
			
			//Connect to any GDB in the preferences
			PreferenceManager prefs = new PreferenceManager();
			prefs.load();
			
			GdbProvider gdbs = new GdbProvider();
			//TODO: use test database!
			
			GpmlIndexer indexer = new GpmlIndexer(indexDir, pathwayDir, gdbs);
			indexer.update();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception during indexing: " + e.getMessage());
		}
	}
	
	public void testOrganismSearch() {
		Query query = new TermQuery(
				new Term(PathwayIndexer.FIELD_ORGANISM, "Homo sapiens")
		);
		Hits hits = query(query);
		assertTrue("nr of hits should be > 0, is: " + hits.length(), hits.length() > 0);
	}
	
	public void testDataNodeSearch() throws CorruptIndexException, IOException {
		Query query = new TermQuery(new Term(PathwayIndexer.FIELD_ID, "8643"));
		Hits hits = query(query);
		boolean found = false;
		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			String name = doc.get(PathwayIndexer.FIELD_NAME);
			if("Hedgehog Signaling Pathway".equals(name)) {
				found = true;
			}
		}
		assertTrue(found);
	}
	
	public void testCrossRefSearch() throws CorruptIndexException, IOException {
		Query q1 = new TermQuery(new Term(PathwayIndexer.FIELD_XID, "32786_at"));
		Query q2 = new TermQuery(new Term(PathwayIndexer.FIELD_XID, "GO:0003700"));
		Hits hits = query(q1);
		assertTrue(searchHits(hits, PathwayIndexer.FIELD_NAME, "Oxidative Stress"));
		hits = query(q2);
		assertTrue(searchHits(hits, PathwayIndexer.FIELD_NAME, "Oxidative Stress"));
	}
	
	boolean searchHits(Hits hits, String field, String result) throws CorruptIndexException, IOException {
		boolean found = false;
		Logger.log.info("Searching hits for: " + result + " in " + field);
		Logger.log.info("\tNr hits: " + hits.length());
		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			String value = doc.get(field);
			Logger.log.trace("\tfound: " + value);
			if(result.equals(value)) {
				found = true;
			}
		}
		return found;
	}
	
	public Hits query(Query q) {
		try {
			IndexSearcher is = new IndexSearcher(indexDir.getAbsolutePath());

			Hits hits = is.search(q);
			return hits;
		} catch(Exception e) {
			fail(e.getMessage());
		}
		return null;
	}
	
	public Hits query(String q) {
		try {
			QueryParser parser = new QueryParser(
					PathwayIndexer.FIELD_NAME, 
					new StandardAnalyzer()
			);
			Query query = parser.parse(q);
			return query(query);
		} catch(Exception e) {
			fail(e.getMessage());
		}
		return null;
	}
}