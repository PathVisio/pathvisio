package org.wikipathways.indexer;

import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.w3c.dom.Element;

public class SearchResult {
	float score;
	Document doc;
	
	public SearchResult(Document doc, float score) {
		this.doc = doc;
		this.score = score;
	}
	

	private Element asXml(org.w3c.dom.Document xmlDoc) {
		Element result = xmlDoc.createElement(ELM_RESULT);
		result.setAttribute(ATTR_SCORE, "" + score);
		
		List<Field> fields = (List<Field>)doc.getFields();
		for(Field f : fields) {
			Element fe = xmlDoc.createElement(ELM_FIELD);
			fe.setAttribute("Name", f.name());
			fe.setAttribute("Value", f.stringValue());
			result.appendChild(fe);
		}
		
		return result;
	}
	
	/**
	 * Adds the search results as xml to the given DOM document.
	 */
	public static void asXml(Collection<SearchResult> results, org.w3c.dom.Document xmlDoc) {
		Element root = xmlDoc.createElement(ELM_ROOT);
		for(SearchResult r : results) {
			root.appendChild(r.asXml(xmlDoc));
		}
		xmlDoc.appendChild(root);
	}
	
	static final String ELM_RESULT = "SearchResult";
	static final String ELM_ROOT = "Results";
	static final String ATTR_SCORE = "Score";
	static final String ELM_FIELD = "Field";
}
