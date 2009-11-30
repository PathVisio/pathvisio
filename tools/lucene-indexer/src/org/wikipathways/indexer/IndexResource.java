package org.wikipathways.indexer;

import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IndexResource extends ServerResource {
	static final int DEFAULT_LIMIT = 10000;

	protected final IndexService getIndexService() {
		return (IndexService)getApplication();
	}

	protected final WikiPathwaysSearcher getSearcher() {
		return getIndexService().getSearcher();
	}

	protected final int getLimit() throws NumberFormatException {
		String limitStr = (String)getRequest().getAttributes().get(IndexService.ATTR_LIMIT);
		if(limitStr != null && !"".equals(limitStr)) {
			return Integer.parseInt(limitStr);
		} else {
			return DEFAULT_LIMIT;
		}
	}

	static Representation createXmlError(String message) {
		try {
			DomRepresentation result = new DomRepresentation(MediaType.TEXT_XML);
			Document d = result.getDocument();

			Element eltError = d.createElement("error");
			Element eltMessage = d.createElement("message");
			eltMessage.appendChild(d.createTextNode(message));
			eltError.appendChild(eltMessage);
			d.appendChild(eltError);
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
