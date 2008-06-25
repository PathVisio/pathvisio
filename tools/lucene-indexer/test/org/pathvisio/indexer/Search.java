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

import org.apache.lucene.analysis.KeywordAnalyzer;
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
			//Start with fresh index
			for(File f : indexDir.listFiles()) {
				f.delete();
			}
			Engine.init();
			
			//Connect to any GDB in the preferences
			PreferenceManager prefs = new PreferenceManager();
			prefs.load();
			
			GdbProvider gdbs = new GdbProvider();
			//TODO: use test database!
			
			GpmlIndexer indexer = new GpmlIndexer(indexDir, pathwayDir, gdbs);
			indexer.update();
			indexer.close();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception during indexing: " + e.getMessage());
		}
	}
	
	public void testTitleSearch() {
		Hits hits = query("name:oxidative");
		assertTrue("nr of hits should be 1, is: " + hits.length(), hits.length() == 1);
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
	
	public void testSimpleRelation() throws CorruptIndexException, IOException {
		String q1 = RelationshipIndexer.FIELD_LEFT + ":A"; //Right should be D
		Hits hits = query(q1);
		assertTrue(hits.length() == 1);
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_RIGHT, "D"));
		
		String q2 = RelationshipIndexer.FIELD_RIGHT + ":A"; //Left should be B, C and E
		hits = query(q2);
		assertTrue(hits.length() == 3);
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_LEFT, "B"));
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_LEFT, "C"));
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_LEFT, "E"));
	}
	
	public void testMetabolicReaction() throws CorruptIndexException, IOException {
		//Substrate1 should yield product1 and product2
		String q1 = RelationshipIndexer.FIELD_LEFT + ":substrate1";
		Hits hits = query(q1);
		assertTrue(hits.length() == 1); //Only one relation, but with two 'right' fields
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_RIGHT, "product1"));
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_RIGHT, "product2"));
		
		String q2 = RelationshipIndexer.FIELD_MEDIATOR + ":inhibitor";
		hits = query(q2);
		assertTrue(hits.length() == 1); //Only one relation, but with two 'mediator' fields
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_MEDIATOR, "inhibitor"));
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_MEDIATOR, "catalyst"));
	}
	
	boolean searchHits(Hits hits, String field, String result) throws CorruptIndexException, IOException {
		boolean found = false;
		Logger.log.info("Searching hits for: " + result + " in " + field);
		Logger.log.info("\tNr hits: " + hits.length());
		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			for(String value: doc.getValues(field)) {
				Logger.log.info("\tfound: " + value);
				if(result.equals(value)) {
					found = true;
				}
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
	
	public Hits query(String q)  {
		try {
			Logger.log.info("Query: '" + q + "'");
			QueryParser parser = new QueryParser(
					PathwayIndexer.FIELD_SOURCE,
					new KeywordAnalyzer()
			);
			Query query = parser.parse(q);
			return query(query);
		} catch(Exception e) {
			fail(e.getMessage());
		}
		return null;
	}
}