// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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

package org.pathvisio.cytoscape.superpathways;

import static cytoscape.data.webservice.CyWebServiceEvent.WSEventType.IMPORT_NETWORK;
import static cytoscape.data.webservice.CyWebServiceEvent.WSEventType.SEARCH_DATABASE;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.rpc.ServiceException;

import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSSearchResult;
import org.wikipathways.client.WikiPathwaysClient;

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

public class SuperpathwaysClient extends WebServiceClientImplWithGUI<WikiPathwaysClient, SuperpathwaysGui> implements NetworkImportWebServiceClient {
	private static final String DISPLAY_NAME = "Superpathways Web Service Client";
    private static final String CLIENT_ID = "superpathways";
	
	//private static final String DISPLAY_NAME = "WikiPathways Web Service Client";
	//private static final String CLIENT_ID = "wikipathways";
	
	protected static final String WEBSERVICE_URL = "wikipathways.webservice.uri";
	public static final String ATTR_PATHWAY_URL = "wikipathways.url";
	
	private WikiPathwaysClient stub;
	private String prevURL = null;
	
	private SuperpathwaysPlugin spPlugin;
	//private GpmlPlugin gpmlPlugin;
	
	public SuperpathwaysClient(SuperpathwaysPlugin spPlugin) {
		super(CLIENT_ID, DISPLAY_NAME, new ClientType[] { ClientType.NETWORK }, null, null, null);
		Logger.log.setLogLevel(true, true, true, true, true, true);
		setProperties();
		getStub();
		this.spPlugin = spPlugin;
		setGUI(new SuperpathwaysGui(this));
		SuperpathwaysGui a=getGUI();
	}

	public SuperpathwaysPlugin getPlugin(){
		return spPlugin;
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
	

	public WikiPathwaysClient getStub() {
		String urlString = CytoscapeInit.getProperties().getProperty(WEBSERVICE_URL);
		if(stub == null || prevURL == null || !prevURL.equals(urlString)) {
			try {
				URL url = new URL(urlString);
				stub = new WikiPathwaysClient(url);
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
	
	
	
	
	public void executeService(CyWebServiceEvent e)throws CyWebServiceException {  
		//must implemented, since WebServiceClientImplWithGUI is subclass of WebServiceClientImpl, and executeService is an abstract method of WebServiceClientImpl
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
	protected void openPathway(GetPathwayParameters query) {
		OpenTask task = new OpenTask(query);
		
		JTaskConfig config = new JTaskConfig();
		config.displayCancelButton(false);
		config.setModal(true);	
		TaskManager.executeTask(task, config);
	}
	
	private void search(FindPathwaysByTextParameters request) throws CyWebServiceException {
		SearchTask task = new SearchTask(request);
		JTaskConfig config = new JTaskConfig();
		config.displayCancelButton(true);
		config.setModal(false);
		TaskManager.executeTask(task, config);
	}
	
	protected String[] listOrganisms() throws RemoteException {
		return getStub().listOrganisms();
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
				WSPathway r = getStub().getPathway(query.id, query.revision);
				Pathway pathway = WikiPathwaysClient.toPathway(r);
				CyNetwork network = spPlugin.load(pathway, true);
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

		public void setTaskMonitor(TaskMonitor m)throws IllegalThreadStateException {
			monitor = m;
		}
		
	}
	
	class SearchTask implements Task, CyWebServiceEventListener {

		FindPathwaysByTextParameters query;
		TaskMonitor monitor;

		public SearchTask(FindPathwaysByTextParameters query) {
			this.query = query;
			WebServiceClientManager.getCyWebServiceEventSupport().addCyWebServiceEventListener(this);
		}

		public String getTitle() {
			return "Searching...";
		}

		public void run() {
			try {
				WSSearchResult[] result = getStub().findPathwaysByText(query.query, query.species);
				gui.setResults(result);
				System.out.println("Superpathways-run!");
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

		public void executeService(CyWebServiceEvent event)	throws CyWebServiceException {
			//Not 2.6 compatible
			//			if (event.getEventType().equals(WSEventType.CANCEL)) {
			//				throw new CyWebServiceException(CyWebServiceException.WSErrorCode.REMOTE_EXEC_FAILED);
			//			}
		}
	}
	
	
	
	
	public VisualStyle getDefaultVisualStyle() {
		//must implemented here, since this class implement interface NetworkImportWebServiceClient
		return null; //TODO
	}
	
	public static class FindPathwaysByTextParameters {
		public String query;
		public Organism species = null;
	}
	
	public static class GetPathwayParameters {
		public String id;
		public int revision;
	}
	
	
	
}
