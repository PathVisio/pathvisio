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
	static final String ATTR_QUERY = "query";
	static final String PAR_PATHWAYSOURCE = "pathway";
	static final String PAR_SYSCODE = "system";
	static final String ATTR_ID = "id";
	static final String ATTR_CODE = "code";
	static final String ATTR_LIMIT = "limit";
	static final String ATTR_ANALYZER = "analyzer";

	WikiPathwaysSearcher searcher;

	public IndexService(WikiPathwaysSearcher searcher) {
		this.searcher = searcher;
	}

	public WikiPathwaysSearcher getSearcher() {
		return searcher;
	}

	public Restlet createRoot() {
		Router router = new Router(getContext());
		Route searchRoute = router.attach("/search", SearchResource.class);
		//Add query as get parameter as workaround to prevent stackoverflow error on very large urls
		searchRoute.extractQuery(ATTR_QUERY, ATTR_QUERY, true);
		searchRoute.extractQuery(ATTR_ANALYZER, ATTR_ANALYZER, true);
		searchRoute.extractQuery(ATTR_LIMIT, ATTR_LIMIT, true);

		Route searchByXrefsRoute = router.attach("/searchxrefs", SearchXrefsRoute.class);
		searchByXrefsRoute.extractQuery(ATTR_ID, ATTR_ID, false);
		searchByXrefsRoute.extractQuery(ATTR_CODE, ATTR_CODE, false);
		searchByXrefsRoute.extractQuery(ATTR_LIMIT, ATTR_LIMIT, true);

		Route xrefsRoute = router.attach("/xrefs/{" + PAR_PATHWAYSOURCE + "}/{" + PAR_SYSCODE + "}", XrefsResource.class);
		xrefsRoute.extractQuery(ATTR_LIMIT, ATTR_LIMIT, true);
		return router;
	}
}
