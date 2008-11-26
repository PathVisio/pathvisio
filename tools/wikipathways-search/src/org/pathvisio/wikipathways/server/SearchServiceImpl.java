// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.wikipathways.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.client.Result;
import org.pathvisio.wikipathways.client.ResultsTable;
import org.pathvisio.wikipathways.client.SearchException;
import org.pathvisio.wikipathways.client.SearchService;
import org.pathvisio.wikipathways.webservice.WSIndexField;
import org.pathvisio.wikipathways.webservice.WSSearchResult;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SearchServiceImpl extends RemoteServiceServlet implements SearchService {
	ImageManager imageMgr;
	WikiPathwaysClient client;
	
	public SearchServiceImpl() throws ServiceException {
		client = new WikiPathwaysClient();
		imageMgr = ImageManager.getInstance();
	}
	
	private WikiPathwaysClient getClient() {
		return client;
	}
	
	public Result[] search(String query) throws SearchException {
		try {
			Map<String, Result> results = new HashMap<String, Result>();
			
			int i = 0;
			Result[] idResults = searchIdentifier(query);
			for(Result r : idResults) {
				r.setOrder(i++);
				Result er = results.get(r.getPathwayId());
				if(er == null) {
					results.put(r.getPathwayId(), r);
				} else {
					//Add the index field to the existing result
					Map<String, String[]> props = r.getProperties();
					Map<String, String[]> eprops = er.getProperties();
					for(String p : props.keySet()) {
						if(eprops.containsKey(p)) {
							//Merge the values
							Set<String> combined = new HashSet<String>();
							for(String s : eprops.get(p)) combined.add(s);
							for(String s : props.get(p)) combined.add(s);
							er.setProperty(p, combined.toArray(new String[combined.size()]));
						} else {
							er.setProperty(p, props.get(p));
						}
					}
				}
			}
			
			Result[] textResults = searchText(query);
			for(Result r : textResults) {
				r.setOrder(i++);
				if(!results.containsKey(r.getPathwayId())) {
					results.put(r.getPathwayId(), r);
				}
			}
			
			List<Result> sortedResults = new ArrayList<Result>(results.values());
			Collections.sort(sortedResults, new Comparator<Result>() {
				public int compare(Result ra, Result rb) {
					return ra.getOrder() - rb.getOrder();
				}
			});
			return sortedResults.toArray(new Result[sortedResults.size()]);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	private Result createPathwayResult(final WSSearchResult wsr) {
		String descr = "<table class='" + ResultsTable.STYLE_DESCRIPTION + "'>";
		descr += "<tr><td><i>Title:</i> " + wsr.getName();
		descr += "<tr><td><i>Organism:</i> " + wsr.getSpecies();
		descr += "<tr><td><i>Identifier:</i> " + wsr.getId();
		descr += "<tr><td><i>Score:</i> " + wsr.getScore();
		descr += "</table>";
		
		Result r = new Result(
				wsr.getId(),
				wsr.getName() + " (" + wsr.getSpecies() + ")",
				descr,
				wsr.getUrl()
		);
		WSIndexField[] fields = wsr.getFields();
		if(fields != null) {
			for(WSIndexField f : wsr.getFields()) {
				r.setProperty(f.getName(), f.getValues());
			}
		}
		r.setImageId("" + ImageManager.getGpmlId(wsr.getId(), wsr.getRevision()));
		
		imageMgr.setServerBasePath(getServletContext().getRealPath("org/pathvisio/wikipathways/public/"));
		imageMgr.startDownload(wsr); //Start downloading image that will be requested by client later
		return r;
	}

	private Result[] searchText(String query) throws ServiceException, RemoteException {
		WSSearchResult[] wsResults = getClient().findPathwaysByText(query, null);
		Result[] results = new Result[wsResults.length];
		for(int i = 0; i < wsResults.length; i++) {
			results[i] = createPathwayResult(wsResults[i]);
		}
		return results;
	}

	private Result[] searchIdentifier(String query) throws RemoteException {
		WSSearchResult[] wsResults = getClient().findPathwaysByXref(query);
		Result[] results = new Result[wsResults.length];
		for(int i = 0; i < wsResults.length; i++) {
			results[i] = createPathwayResult(wsResults[i]);
		}
		return results;
	}
	
	public void waitForImage(String id) {
		try {
			imageMgr.waitForImage(id);
		} catch(Exception e) {
			throw new SearchException(e);
		}
	}
}
