package org.wikipathways.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.pathvisio.indexer.DataNodeIndexer;

public class WikiPathwaysSearcher {
	Searcher searcher;
	
	public WikiPathwaysSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	public List<SearchResult> query(Query query, int limit) throws IOException {
		TopDocs hits = searcher.search(query, null, limit);
		List<SearchResult> results = new ArrayList<SearchResult>();
		for(ScoreDoc sd : hits.scoreDocs) {
			results.add(new SearchResult(searcher.doc(sd.doc), sd.score));
		}
		return results;
	}
	
	public List<String> listXrefs(String pathwaySource, String sysCode) throws IOException {
		List<String> xrefs = new ArrayList<String>();

		TermQuery query = new TermQuery(new Term(
				DataNodeIndexer.FIELD_INDEXERID, DataNodeIndexer.class.getName() + pathwaySource)
		);
		TopDocs hits = searcher.search(query, null, 10000);
		for(ScoreDoc sd : hits.scoreDocs) {
			Document xrefDoc = searcher.doc(sd.doc);
			String idcode = xrefDoc.get(DataNodeIndexer.FIELD_XID_CODE);
			if(idcode != null && idcode.endsWith(":" + sysCode)) {
				xrefs.add(xrefDoc.get(DataNodeIndexer.FIELD_XID));
			}
		}
		return xrefs;
	}
}
