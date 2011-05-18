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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.bridgedb.rdb.GdbProvider;
import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.PreferenceManager;

public class Search extends TestCase {
	static final File pathwayDir = new File("tools/lucene-indexer/test-data");
	static final File indexDir = new File("tools/lucene-indexer/test-index");

	public void testCreateIndex() {
		try {
			indexDir.mkdirs();
			//Start with fresh index
			for(File f : indexDir.listFiles()) {
				f.delete();
			}

			//Connect to any GDB in the preferences
			PreferenceManager.init();
			GdbManager gdbmgr = new GdbManager();
			gdbmgr.initPreferred();

			GdbProvider gdbs = new GdbProvider();
			gdbs.addGlobalGdb(gdbmgr.getCurrentGdb());

			GpmlIndexer indexer = new GpmlIndexer(indexDir, pathwayDir, gdbs);
			indexer.update();
			indexer.close();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception during indexing: " + e.getMessage());
		}
	}

	public void testRemovePathway() {
		File firstFile = null;
		File renamedFile = null;
		int nrHits = -1;

		for(File f : pathwayDir.listFiles()) {
			if(f.getName().endsWith(".gpml")) {
				firstFile = new File(f.getAbsolutePath());
				renamedFile = new File(f.getAbsolutePath() + ".tmp");
				assertTrue(f.renameTo(renamedFile));
				break;
			}
		}

		Logger.log.info("Removing " + firstFile.getAbsolutePath() + " from index");

		try {
			//Update the index
			GdbProvider gdbs = new GdbProvider();
			GpmlIndexer indexer = new GpmlIndexer(indexDir, pathwayDir, gdbs);
			indexer.update(firstFile);
			indexer.close();

			//Try to find the pathway by it's source attribute
			Hits hits = query(
					new TermQuery(
							new Term(IndexerBase.FIELD_SOURCE, firstFile.getAbsolutePath())
					)
			);
			nrHits = hits.length();

			Logger.log.info("FirstFile: " + firstFile);
			Logger.log.info("RenamedFile: " + renamedFile);
			renamedFile.renameTo(firstFile);

			//Update the index
			indexer = new GpmlIndexer(indexDir, pathwayDir, gdbs);
			indexer.update(firstFile);
			indexer.close();
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertEquals(0, nrHits);
	}

	public void testTitleSearch() {
		Hits hits = query("name:oxidative");
		assertTrue("nr of hits should be 1, is: " + hits.length(), hits.length() == 1);
	}

	public void testOrganismSearch() {
		Hits hits = query("organism:\"Homo sapiens\"");
		assertTrue("nr of hits should be > 0, is: " + hits.length(), hits.length() > 0);
	}

	public void testDataNodeSearch() throws CorruptIndexException, IOException {
		Query query = new TermQuery(new Term(DataNodeIndexer.FIELD_ID, "8643"));
		Hits hits = query(query);
		assertTrue("nr of hits should be > 0, is: " + hits.length(), hits.length() > 0);
		boolean found = false;
		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			String source = doc.get(DataNodeIndexer.FIELD_SOURCE);
			if(source.endsWith("Hs_Hedgehog_Netpath_10.gpml")) {
				found = true;
				break;
			}
		}
		assertTrue(found);
	}

	public void testCrossRefSearch() throws CorruptIndexException, IOException {
		Query q1 = new TermQuery(new Term(DataNodeIndexer.FIELD_XID, "32786_at"));
		Query q2 = new TermQuery(new Term(DataNodeIndexer.FIELD_XID, "GO:0003700"));
		Hits hits = query(q1);
		assertTrue("Hits length: " + hits.length(), hits.length() > 0);
		hits = query(q2);
		assertTrue("Hits length: " + hits.length(), hits.length() > 0);
	}

	public void testCrossRefByCode() {
		//Search for 5157 L -> should get pathway 5157-L.gpml
		Query q1 = new TermQuery(new Term(DataNodeIndexer.FIELD_XID_CODE, "5157:L"));
		Hits hits = query(q1);
		assertTrue("Hits length: " + hits.length(), hits.length() == 1);

		//Search for 5157 H -> should get pathway 5157-H.gpml
		Query q2 = new TermQuery(new Term(DataNodeIndexer.FIELD_XID_CODE, "5157:H"));
		hits = query(q1);
		assertTrue("Hits length: " + hits.length(), hits.length() == 1);

	}

	boolean xrefInPathway(Hits hits, String pwName) throws CorruptIndexException, IOException {
		for(int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			String source = doc.get(DataNodeIndexer.FIELD_SOURCE);
			Hits pwh = query(new TermQuery(new Term(PathwayIndexer.FIELD_SOURCE, source)));
			if(searchHits(pwh, PathwayIndexer.FIELD_NAME, pwName)) {
				return true;
			}
		}
		return false;
	}

	public void testSimpleRelation() throws CorruptIndexException, IOException {
		String q1 = RelationshipIndexer.FIELD_LEFT + ":A"; //Right should be D
		Hits hits = query(q1);
		assertTrue(hits.length() > 0);
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_RIGHT, "D"));

		String q2 = RelationshipIndexer.FIELD_RIGHT + ":A"; //Left should be B, C and E
		hits = query(q2);
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_LEFT, "B"));
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_LEFT, "C"));
		assertTrue(searchHits(hits, RelationshipIndexer.FIELD_LEFT, "E"));
	}

	public void testCaseInsensitive() throws CorruptIndexException, IOException {
		String q1 = RelationshipIndexer.FIELD_LEFT + ":a"; //Stored as A
		Hits hits = query(q1);
		assertTrue(hits.length() > 0);
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

	public void testLiterature() {
		Query q1 = new TermQuery(new Term(LiteratureIndexer.FIELD_ID, "1234"));
		Hits hits = query(q1);
		assertTrue(hits.length() >= 1);
		String q2 = LiteratureIndexer.FIELD_AUTHOR + ":Marchand";
		hits = query(q2);
		assertTrue(hits.length() >= 1);
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
					new SimpleAnalyzer()
			);
			Query query = parser.parse(q);
			Logger.log.info("Parsed query: '" + query + "'");
			return query(query);
		} catch(Exception e) {
			fail(e.getMessage());
		}
		return null;
	}
}