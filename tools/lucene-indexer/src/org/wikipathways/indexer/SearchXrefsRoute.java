package org.wikipathways.indexer;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

public class SearchXrefsRoute extends IndexResource {
	List<String> ids = new ArrayList<String>();
	List<String> codes = new ArrayList<String>();

	protected void doInit() throws ResourceException {
		super.doInit();
		List<Parameter> rawIds = (List<Parameter>)getRequest().getAttributes().get(IndexService.ATTR_ID);
		List<Parameter> rawCodes = (List<Parameter>)getRequest().getAttributes().get(IndexService.ATTR_CODE);
		ids.clear();
		codes.clear();
		for(Parameter id : rawIds) {
			ids.add(URLDecoder.decode(id.getValue()));
		}
		if(rawCodes != null) {
			for(Parameter code : rawCodes) {
				codes.add(code.getValue());
			}
		}
	}

	@Get("xml")
	public Representation getResult() {
		try {
			DomRepresentation domRep = new DomRepresentation(MediaType.TEXT_XML);
			Document doc = domRep.getDocument();

			List<String> myCodes = null;
			if(codes.size() == 0) {
				myCodes = Arrays.asList(new String[ids.size()]);
			} else if(codes.size() == 1) {
				myCodes = Arrays.asList(new String[ids.size()]);
				Collections.fill(myCodes, codes.get(0));
			} else {
				if(codes.size() != ids.size()) {
					throw new IllegalArgumentException(
							"Number of ids is not equal to number of codes"
					);
				}
				myCodes = codes;
			}

			Set<Xref> xrefs = new HashSet<Xref>();
			for(int i = 0; i < ids.size(); i++) {
				DataSource ds = null;
				String code = myCodes.get(i);
				if(code != null) {
					ds = DataSource.getBySystemCode(code);
				}
				xrefs.add(new Xref(ids.get(i), ds));
			}
			List<SearchResult> results = getSearcher().queryByXrefs(xrefs, getLimit());
			SearchResult.asXml(results, doc);
			return domRep;
		} catch(Exception e) {
			e.printStackTrace();
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return IndexResource.createXmlError(e.getMessage());
		}
	}
}
