package org.wikipathways.indexer;

import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
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

public class SearchResource extends IndexResource {
	String query;
	String analyzer;
	QueryParser defaultParser = new QueryParser(PathwayIndexer.FIELD_NAME, new StandardAnalyzer());

	protected void doInit() throws ResourceException {
		super.doInit();
		query = URLDecoder.decode(
				(String)getRequest().getAttributes().get(IndexService.ATTR_QUERY)
		);
		analyzer = (String)getRequest().getAttributes().get(IndexService.ATTR_ANALYZER);
	}

	@Get("xml")
	public Representation getResult() {
		try {
			DomRepresentation domRep = new DomRepresentation(MediaType.TEXT_XML);
			Document doc = domRep.getDocument();
			if(query != null && !"".equals(query)) {
				System.err.println(query);
				QueryParser parser = defaultParser;
				if(analyzer != null) {
					parser = new QueryParser(PathwayIndexer.FIELD_NAME, getAnalyzer(analyzer));
				}
				System.err.println(parser.parse(query));
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

	private Analyzer getAnalyzer(String className) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<? extends Analyzer> analyzerClass =
			(Class<? extends Analyzer>)this.getClass().getClassLoader().loadClass(className);
		return analyzerClass.getConstructor().newInstance();
	}
}
