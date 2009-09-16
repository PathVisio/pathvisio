package org.wikipathways.indexer;

import java.net.URLDecoder;
import java.util.List;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.pathvisio.indexer.PathwayIndexer;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

public class QueryResource extends IndexResource {
	String query;
	QueryParser parser = new QueryParser(PathwayIndexer.FIELD_NAME, new KeywordAnalyzer());

	protected void doInit() throws ResourceException {
		super.doInit();
		query = URLDecoder.decode(
				(String)getRequest().getAttributes().get(IndexService.PAR_QUERY)
		);
	}

	@Get("xml")
	public Representation getResult() {
		try {
			DomRepresentation domRep = new DomRepresentation(MediaType.TEXT_XML);
			Document doc = domRep.getDocument();
			if(query != null && !"".equals(query)) {
				List<SearchResult> results = getSearcher().query(parser.parse(query), getLimit());
				SearchResult.asXml(results, doc);
				return domRep;
			} else {
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return IndexResource.createXmlError("Invalid query: '" + query + "'");
			}
		} catch(Exception e) {
			e.printStackTrace();
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return IndexResource.createXmlError(e.getMessage());
		}
	}
}
