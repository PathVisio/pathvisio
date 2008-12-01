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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.rpc.ServiceException;

import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.client.IdSearchPanel;
import org.pathvisio.wikipathways.client.Query;
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
		if(!ImageManager.isInit()) {
			client = new WikiPathwaysClient(getClientUrl());
			ImageManager.init(client);
		}
		imageMgr = ImageManager.getInstance();
	}
	
	protected static URL getClientUrl() {
		URL url = null;
		try {
			url = new URL(
					"http://www.wikipathways.org/wpi/webservice/webservice.php"
			);
			Properties prop = new Properties();
			prop.load(new FileInputStream(new File("wikipathways.props")));
			String wsurl = (String)prop.get("webservice-url");
			if(wsurl != null) {
				url = new URL(wsurl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}
	
	private WikiPathwaysClient getClient() {
		return client;
	}
	
	public Result[] search(Query query) throws SearchException {
		try {
			if(Query.TYPE_ID.equals(query.getType())) { //System code specified, use id search
				return searchIdentifier(query);
			} else { //Use text search
				return searchText(query.getText());
			}
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	private Result createPathwayResult(final WSSearchResult wsr, String id) {
		imageMgr.setServerBasePath(getServletContext().getRealPath("org/pathvisio/wikipathways/public/"));

		String pdescr = null;
		String iddescr = null;
		try {
			if(id != null) {
				Pathway pathway = imageMgr.getPathway(wsr);
				String idlist = "<ul>";
				for(WSIndexField f : wsr.getFields()) {
					if("graphId".equals(f.getName())) {
						for(String graphId : f.getValues()) {
							PathwayElement pwe = pathway.getElementById(graphId);
							if(pwe != null) {
								idlist += "<li>" + pwe.getTextLabel() + " (";
								Xref xref = pwe.getXref();
								idlist += "<a href='" + xref.getUrl() + "'>" +
									xref.getId() + ", " + xref.getDatabaseName() + "</a>)";
							}
						}
					}
				}
				if(!"<ul>".equals(idlist)) {
					iddescr = idlist + "</ul>";
				}
			}
			
		} catch(ConverterException e) {
			e.printStackTrace();
		}
		
		String descr = "<table class='" + ResultsTable.STYLE_DESCRIPTION + "'>";
		descr += "<tr><td><i>Title:</i> " + wsr.getName();
		descr += "<tr><td><i>Organism:</i> " + wsr.getSpecies();
		descr += "<tr><td><i>Pathway ID:</i> " + wsr.getId();
		if(iddescr != null) descr += "<tr><td><i>" +
				"IDs mapping to " + id + ":</i> " + iddescr;
		descr += "</table>";
		
		Result r = new Result(
				wsr.getId(),
				wsr.getName() + " (" + wsr.getSpecies() + ")",
				descr,
				wsr.getUrl(),
				wsr.getSpecies()
		);
		WSIndexField[] fields = wsr.getFields();
		if(fields != null) {
			for(WSIndexField f : wsr.getFields()) {
				r.setProperty(f.getName(), f.getValues());
			}
		}
		r.setImageId("" + ImageManager.getGpmlId(wsr.getId(), wsr.getRevision()));
		
		imageMgr.startDownload(wsr); //Start downloading image that will be requested by client later
		return r;
	}

	private Result[] searchText(String query) throws ServiceException, RemoteException {
		WSSearchResult[] wsResults = getClient().findPathwaysByText(query, null);
		Result[] results = new Result[wsResults.length];
		for(int i = 0; i < wsResults.length; i++) {
			results[i] = createPathwayResult(wsResults[i], null);
		}
		return results;
	}

	private Result[] searchIdentifier(Query query) throws RemoteException {
		WSSearchResult[] wsResults = new WSSearchResult[0];
		
		String id = query.getText();
		String system = query.getField(Query.FIELD_SYSTEM);
		if(IdSearchPanel.SYSTEM_ALL.equals(system) || system == null) {
			wsResults = client.findPathwaysByXref(id);
		} else {
			DataSource ds = DataSource.getByFullName(system);
			Xref xref = new Xref(id, ds);
			wsResults = client.findPathwaysByXref(xref);
		}
		
		Result[] results = new Result[wsResults.length];
		for(int i = 0; i < wsResults.length; i++) {
			results[i] = createPathwayResult(wsResults[i], id);
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
	
	public String[] getSystemNames() {
		String[] names = DataSource.getFullNames().toArray(new String[0]);
		Arrays.sort(names);
		return names;
	}
	
	public String[] getOrganismNames() {
		String[] orgs;
		try {
			orgs = client.listOrganisms();
			Arrays.sort(orgs);
			return orgs;
		} catch (RemoteException e) {
			throw new SearchException(e);
		}
	}
}
