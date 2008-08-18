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
package org.pathvisio.cytoscape.wikipathways;


import java.io.StringReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.rpc.ServiceException;

import org.pathvisio.cytoscape.GpmlPlugin;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSSearchResult;
import org.pathvisio.wikipathways.webservice.WikiPathwaysLocator;
import org.pathvisio.wikipathways.webservice.WikiPathwaysPortType;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.webservice.CyWebServiceEvent;
import cytoscape.data.webservice.CyWebServiceEventListener;
import cytoscape.data.webservice.CyWebServiceException;
import cytoscape.data.webservice.NetworkImportWebServiceClient;
import cytoscape.data.webservice.WebServiceClientImplWithGUI;
import cytoscape.data.webservice.WebServiceClientManager;
import cytoscape.data.webservice.WebServiceClientManager.ClientType;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.ModulePropertiesImpl;
import cytoscape.visual.VisualStyle;

public class WikiPathwaysClient extends WebServiceClientImplWithGUI<WikiPathwaysPortType, WikiPathwaysClientGui> implements NetworkImportWebServiceClient {
	private static final String DISPLAY_NAME = "WikiPathways Web Service Client";
	private static final String CLIENT_ID = "wikipathways";
	protected static final String WEBSERVICE_URL = "wikipathways.webservice.uri";

	public static final String ATTR_PATHWAY_URL = "wikipathways.url";
	
	private WikiPathwaysPortType stub;
	private GpmlPlugin gpmlPlugin;
	
	public WikiPathwaysClient(GpmlPlugin gpmlPlugin) {
		super(CLIENT_ID, DISPLAY_NAME, 
				new ClientType[] { ClientType.NETWORK }, 
				null, null, null 
		);
		Logger.log.setLogLevel(true, true, true, true, true, true);
		setProperties();
		getStub();
		this.gpmlPlugin = gpmlPlugin;
		setGUI(new WikiPathwaysClientGui(this));
	}

	private String prevURL = null;
	
	public WikiPathwaysPortType getStub() {
		String urlString = CytoscapeInit.getProperties().getProperty(WEBSERVICE_URL);
		if(stub == null || prevURL == null || !prevURL.equals(urlString)) {
			try {
				URL url = new URL(urlString);
				stub = new WikiPathwaysLocator().getWikiPathwaysSOAPPort_Http(url);
				setClientStub(stub);
				prevURL = urlString;
				
				if(gui != null) gui.resetOrganisms();
			} catch (ServiceException e) {
				Logger.log.error("Unable to create WikiPathways webservice", e);
			} catch (MalformedURLException ue) {
				Logger.log.error("Invalid url to wsdl", ue);
			}
		}
		return stub;
	}
	
	private void setProperties() {
		//Using global properties, but need to initialize moduleproperties anyway.
		props = new ModulePropertiesImpl(CLIENT_ID, "wsc");
		
		Properties p = CytoscapeInit.getProperties();
		if(p.get(WEBSERVICE_URL) == null) {
			p.put(
					WEBSERVICE_URL, 
					"http://www.wikipathways.org/wpi/webservice/webservice.php"
			);
		}
	}

	public void executeService(CyWebServiceEvent e)
	throws CyWebServiceException {
		if(CLIENT_ID.equals(e.getSource())) {
			switch(e.getEventType()) {
			case IMPORT_NETWORK:
				Logger.log.info("Importing " + e.getParameter());
				openPathway((GetPathwayParameters)e.getParameter());
				break;
			case SEARCH_DATABASE:
				Logger.log.info("Searching " + e.getParameter());
				search((FindPathwaysByTextParameters)e.getParameter());
				break;
			}
		}
	}

	private void search(FindPathwaysByTextParameters request) throws CyWebServiceException {
		SearchTask task = new SearchTask(request);
		JTaskConfig config = new JTaskConfig();
		config.displayCancelButton(false);
		config.setModal(true);
		TaskManager.executeTask(task, config);
	}

	protected void openPathway(GetPathwayParameters query) {
		OpenTask task = new OpenTask(query);
		JTaskConfig config = new JTaskConfig();
		config.displayCancelButton(false);
		config.setModal(true);
		TaskManager.executeTask(task, config);
	}
	
	protected String[] listOrganisms() throws RemoteException {
		return getStub().listOrganisms();
	}

	public VisualStyle getDefaultVisualStyle() {
		return null; //TODO
	}

	public static class FindPathwaysByTextParameters {
		public String query;
		public String species = "";
	}
	
	public static class GetPathwayParameters {
		public String pwName;
		public String pwSpecies;
		public BigInteger revision;
	}
	
	class OpenTask implements Task {
		TaskMonitor monitor;
		GetPathwayParameters query;
		
		public OpenTask(GetPathwayParameters query) {
			this.query = query;
		}
		
		public String getTitle() {
			return "Opening pathway...";
		}

		public void halt() {
		}

		public void run() {
			try {
				WSPathway r = getStub().getPathway(query.pwName, query.pwSpecies, query.revision);
				String gpml = r.getGpml();
				Logger.log.trace(gpml);
				Pathway pathway = new Pathway();
				pathway.readFromXml(new StringReader(gpml), true);
				CyNetwork network = gpmlPlugin.load(pathway, true);
				if(network != null) {
					Cytoscape.getNetworkAttributes().setAttribute(
							network.getIdentifier(), ATTR_PATHWAY_URL, r.getUrl()
					);
				}
			} catch (Exception e) {
				Logger.log.error("Error while opening pathway", e);
				JOptionPane.showMessageDialog(
						gui, "Error: " + e.getMessage() + ". See log for details",
						"Error", JOptionPane.ERROR_MESSAGE
				);
			}
		}

		public void setTaskMonitor(TaskMonitor m)
				throws IllegalThreadStateException {
			monitor = m;
		}
		
	}
	
	class SearchTask implements Task, CyWebServiceEventListener {

		FindPathwaysByTextParameters query;
		TaskMonitor monitor;

		public SearchTask(FindPathwaysByTextParameters query) {
			this.query = query;
			WebServiceClientManager.getCyWebServiceEventSupport()
			.addCyWebServiceEventListener(this);
		}

		public String getTitle() {
			return "Searching...";
		}

		public void run() {
			try {
				WSSearchResult[] result = getStub().findPathwaysByText(query.query, query.species);
				gui.setResults(result);
				if(result == null || result.length == 0) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(
									gui, "The search didn't return any results", 
									"No results", JOptionPane.INFORMATION_MESSAGE
							);					
						}
					});
					
				}
			} catch (final Exception e) {
				Logger.log.error("Error while searching", e);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(
								gui, "Error: " + e.getMessage() + ". See log for details",
								"Error", JOptionPane.ERROR_MESSAGE
						);						
					}
				});
			}
		}

		public void halt() {
			//Not 2.6 compatible
			//			WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(
			//				new CyWebServiceEvent(CLIENT_ID, WSEventType.CANCEL, query)
			//			);
		}

		public void setTaskMonitor(TaskMonitor m)
		throws IllegalThreadStateException {
			this.monitor = m;
		}

		public void executeService(CyWebServiceEvent event)
		throws CyWebServiceException {
			//Not 2.6 compatible
			//			if (event.getEventType().equals(WSEventType.CANCEL)) {
			//				throw new CyWebServiceException(CyWebServiceException.WSErrorCode.REMOTE_EXEC_FAILED);
			//			}
		}
	}
}
