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

public class Search extends TestCase {
	static final File pathwayDir = new File("tools/lucene-indexer/test-data");
	static final File indexDir = new File("tools/lucene-indexer/test-index");

	protected void setUp() throws Exception {
		createIndex();
	}
	
	public void createIndex() {
		try {
			CreateIndex.index(pathwayDir, indexDir);
		} catch(Exception e) {
			fail("Exception during indexing: " + e.getMessage());
		}
	}
	
	public void testOrganismSearch() {
		Query query = new TermQuery(new Term("organism", "Homo sapiens"));
		Hits hits = query(query);
		assertTrue("nr of hits should be > 0, is: " + hits.length(), hits.length() > 0);
	}
	
	public void testDataNodeSearch() throws CorruptIndexException, IOException {
		Query query = new TermQuery(new Term("xref.id", "8643"));
		Hits hits = query(query);
		boolean found = false;
		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			String name = doc.get("name");
			if("Hedgehog Signaling Pathway".equals(name)) {
				found = true;
			}
		}
		assertTrue(found);
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
			QueryParser parser = new QueryParser("name", new StandardAnalyzer());
			Query query = parser.parse(q);
			return query(query);
		} catch(Exception e) {
			fail(e.getMessage());
		}
		return null;
	}
}