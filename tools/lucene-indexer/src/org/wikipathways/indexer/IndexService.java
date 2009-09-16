package org.wikipathways.indexer;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

/**
 * Restlet index service for wikipathways.
 * @author thomas
 */
public class IndexService extends Application {
	static final String PAR_QUERY = "query";
	static final String PAR_PATHWAYSOURCE = "pathway";
	static final String PAR_SYSCODE = "system";
	static final String ATTR_LIMIT = "limit";
	
	WikiPathwaysSearcher searcher;
	
	public IndexService(WikiPathwaysSearcher searcher) {
		this.searcher = searcher;
	}
	
	public WikiPathwaysSearcher getSearcher() {
		return searcher;
	}
	
	public Restlet createRoot() {
		Router router = new Router(getContext());
		Route queryRoute = router.attach("/query/{" + PAR_QUERY +"}", QueryResource.class);
		queryRoute.extractQuery(ATTR_LIMIT, ATTR_LIMIT, true);
		router.attach(queryRoute);
		
		Route xrefsRoute = router.attach("/xrefs/{" + PAR_PATHWAYSOURCE + "}/{" + PAR_SYSCODE + "}", XrefsResource.class);
		xrefsRoute.extractQuery(ATTR_LIMIT, ATTR_LIMIT, true);
		router.attach(xrefsRoute);
		return router;	
	}
}
