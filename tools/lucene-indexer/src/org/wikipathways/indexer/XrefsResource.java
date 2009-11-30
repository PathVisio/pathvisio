package org.wikipathways.indexer;

import java.net.URLDecoder;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

public class XrefsResource extends IndexResource {
	String pathwaySource;
	String code;

	protected void doInit() throws ResourceException {
		super.doInit();
		pathwaySource = URLDecoder.decode(
				(String)getRequest().getAttributes().get(IndexService.PAR_PATHWAYSOURCE)
		);
		code = URLDecoder.decode(
				(String)getRequest().getAttributes().get(IndexService.PAR_SYSCODE)
		);
	}

	@Get("text")
	public Representation getResult() {
		try {
			StringBuilder str = new StringBuilder();
			for(String x : getSearcher().listXrefs(pathwaySource, code)) {
				str.append(x);
				str.append("\n");
			}
			return new StringRepresentation(str.toString().trim(), MediaType.TEXT_PLAIN);
		} catch(Exception e) {
			e.printStackTrace();
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return new StringRepresentation(e.getMessage(), MediaType.TEXT_PLAIN);
		}
	}
}
