// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.data.webservice.CyWebServiceEvent;
import cytoscape.data.webservice.CyWebServiceEvent.WSEventType;
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
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualStyle;

import edu.stanford.ejalbert.BrowserLauncher;
import giny.view.EdgeView;
import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.rpc.ServiceException;

import org.bridgedb.bio.Organism;
import org.pathvisio.cytoscape.GpmlPlugin;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSIndexField;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSSearchResult;

/**
 * WebserviceClient implementation, for accessing the
 * WikiPathways webservice in a standard way from within Cytoscape.
 */
public class CyWikiPathwaysClient extends WebServiceClientImplWithGUI<WikiPathwaysClient, CyWikiPathwaysClientGui> implements NetworkImportWebServiceClient {
	private static final String DISPLAY_NAME = "WikiPathways Web Service Client";
	private static final String CLIENT_ID = "wikipathways";
	protected static final String WEBSERVICE_URL = "wikipathways.webservice.uri";

	public static final String ATTR_PATHWAY_URL = "wikipathways.url";

	private WikiPathwaysClient stub;
	private GpmlPlugin gpmlPlugin;

	public CyWikiPathwaysClient(GpmlPlugin gpmlPlugin) {
		super(CLIENT_ID, DISPLAY_NAME,
				new ClientType[] { ClientType.NETWORK },
				null, null, null
		);
		Logger.log.setLogLevel(true, true, true, true, true, true);
		setProperties();
		getStub();
		this.gpmlPlugin = gpmlPlugin;
		setGUI(new CyWikiPathwaysClientGui(this));
	}

	private String prevURL = null;

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

	/**
	 * Check if a working connection to the wikipathways
	 * web service is available.
	 */
	public boolean isConnected() {
		try {
			//Try to list organisms, if fails then we're probably
			//not connected
			listOrganisms();
			return true;
		} catch (RemoteException e) {
			return false;
		}
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

	public List<JMenuItem> getNodeContextMenuItems(NodeView nv) {
		List<JMenuItem> menuList = new ArrayList<JMenuItem>();

		//Add an item to find interactions
		menuList.add(new JMenuItem(new NodeInteractionsAction(this, nv)));

		//If the node is a found interaction, add items to open the source on WP
		CyAttributes attr = Cytoscape.getNodeAttributes();
		String nid = nv.getNode().getIdentifier();
		addPathwayMenuItems(attr, nid, menuList);
		return menuList;
	}

	public List<JMenuItem> getEdgeContextMenuItems(EdgeView ev) {
		List<JMenuItem> menuList = new ArrayList<JMenuItem>();

		//If the edge is a found interaction, add items to open the source on WP
		CyAttributes attr = Cytoscape.getEdgeAttributes();
		String eid = ev.getEdge().getIdentifier();
		addPathwayMenuItems(attr, eid, menuList);

		return menuList;
	}

	/**
	 * Adds menu items to open and import pathways based on the pathway info
	 * stored in the attributes (by the find interactions function).
	 * @param attr The attribute store
	 * @param nid The node or edge id
	 * @param menuList The list to add the menu items to
	 */
	private void addPathwayMenuItems(CyAttributes attr, String nid, List<JMenuItem> menuList) {
		List<String> urls = attr.getListAttribute(nid, ATTR_PATHWAY_URL);
		List<String> names = attr.getListAttribute(nid, ATTR_NAME);
		List<String> species = attr.getListAttribute(nid, ATTR_SPECIES);
		List<String> ids = attr.getListAttribute(nid, ATTR_ID);
		if(urls != null) {
			for(int i = 0; i < urls.size(); i++) {
				final String url = urls.get(i);
				String label = url;
				if(names != null && names.size() == urls.size()) {
					label = names.get(i);
				}
				if(species != null && species.size() == urls.size()) {
					label += " (" + species.get(i) + ")";
				}
				menuList.add(new JMenuItem(new AbstractAction("View pathway " + label + " on WikiPathways") {
					public void actionPerformed(ActionEvent e) {
						try {
							BrowserLauncher b = new BrowserLauncher(null);
							b.openURLinBrowser(url);
						} catch(Exception ex) {
							JOptionPane.showMessageDialog(
									null, "Unable to launch browser: " + ex.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE
							);
							Logger.log.error("Unable to open browser", ex);
						}
					}
				}));
				if(ids != null && ids.size() == urls.size()) {
					final String id = ids.get(i);
					menuList.add(new JMenuItem(new AbstractAction("Load pathway " + label + " as network") {
						public void actionPerformed(ActionEvent e) {
							GetPathwayParameters request = new GetPathwayParameters();
							request.id = id;
							try {
								WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(
										new CyWebServiceEvent(
												getClientID(), WSEventType.IMPORT_NETWORK, request
										)
								);
							} catch (CyWebServiceException e1) {
								JOptionPane.showMessageDialog(
										null, "Unable to open pathway: " + e1.getMessage(),
										"Error", JOptionPane.ERROR_MESSAGE
								);
								Logger.log.error("Unable to open pathway", e1);
							}
						}
					}));
				}
			}
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

	private static final int LEFT = -1;
	private static final int RIGHT = 1;
	private static final int MEDIATOR = 0;

	public static final String ATTR_NAME = "wikipathways.pathway";
	public static final String ATTR_SPECIES = "wikipathways.species";
	public static final String ATTR_ID = "wikipathways.id";

	protected void addInteractions(InteractionQuery query, WSSearchResult[] results) {
		String attrValue = query.getAttributeValue();
		Set<CyNode> addedNodes = new HashSet<CyNode>();

		for(WSSearchResult r : results) {
			String[] left = new String[] {};
			String[] right = new String[] {};
			String[] mediator = new String[] {};

			//Find the relevant field values
			for(WSIndexField f : r.getFields()) {
				String pathway = r.getUrl();
				if("left".equals(f.getName())) {
					left = f.getValues();
				}
				else if("right".equals(f.getName())) {
					right = f.getValues();
				}
				else if("mediator".equals(f.getName())) {
					mediator = f.getValues();
				}
			}

			//Add the interactions for each interaction type
			if(left != null) for(String name : left) {
				if(attrValue.equals(name)) {
					addedNodes.addAll(
							addInteractions(query, r, right, RIGHT)
					);
				}
			}
			if(right != null) for(String name : right) {
				if(attrValue.equals(name)) {
					addedNodes.addAll(
							addInteractions(query, r, left, LEFT)
					);
				}
			}
			if(mediator != null) for(String name : mediator) {
				if(attrValue.equals(name)) {
					addedNodes.addAll(
							addInteractions(query, r, left, MEDIATOR)
					);
					addedNodes.addAll(
							addInteractions(query, r, right, MEDIATOR)
					);
				}
			}
		}

		//Redraw the graph
		((CyNetworkView)query.getNodeView().getGraphView()).redrawGraph(true, false);

		//Layout nodes in a circle around the source node
		NodeView source = query.getNodeView();
		double radius = 50 + 5 * addedNodes.size();
		double angle = 0;
		double dangle = (2.0 * Math.PI) / (double)addedNodes.size();
		for(CyNode n : addedNodes) {
			NodeView nv = source.getGraphView().getNodeView(n);
			nv.setXPosition(source.getXPosition() + radius * Math.cos(angle), false);
			nv.setYPosition(source.getYPosition() + radius * Math.sin(angle), false);
			angle += dangle;
		}
	}

	private Set<CyNode> addInteractions(InteractionQuery query, WSSearchResult r, String[] names, int type) {
		Set<CyNode> addedNodes = new HashSet<CyNode>();

		NodeView nv = query.getNodeView();
		CyNetworkView view = (CyNetworkView)nv.getGraphView();
		CyNetwork network = view.getNetwork();
		CyNode n = (CyNode)nv.getNode();

		CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();
		CyAttributes nodeAttr = Cytoscape.getNodeAttributes();

		String pwUrl = r.getUrl();
		String pwName = r.getName();
		String pwSpecies = r.getSpecies();

		for(String name : names) {
			if(name.length() == 0 || "new group".equalsIgnoreCase(name)) {
				continue; //Skip groups and blanks
			}
			CyNode nn = Cytoscape.getCyNode(name, false);
			if(nn == null) {
				nn = Cytoscape.getCyNode(name, true);
				addedNodes.add(nn);
			}
			network.addNode(nn);

			CyNode n1 = nn;
			CyNode n2 = n;
			if(type == RIGHT) { //Swap order if new node is on RIGHT side
				n1 = n; n2 = nn;
			}
			//Find any existing edge (regardless of interaction type)
			CyEdge edge = null;
			for(CyEdge e : (List<CyEdge>)Cytoscape.getCyEdgesList()) {
				if(e.getSource() == n1 && e.getTarget() == n2) {
					edge = e;
				}
			}
			//If no edge exists, create a new one
			if(edge == null) {
				edge = Cytoscape.getCyEdge(
						n1, n2, Semantics.INTERACTION, "", true, true
				);
			}

			network.addEdge(edge);

			//Add some read-only attributes to store the source pathway info
			addToListAttribute(edgeAttr, edge.getIdentifier(), ATTR_PATHWAY_URL, r.getUrl());
			addToListAttribute(edgeAttr, edge.getIdentifier(), ATTR_NAME, r.getName());
			addToListAttribute(edgeAttr, edge.getIdentifier(), ATTR_SPECIES, r.getSpecies());
			addToListAttribute(edgeAttr, edge.getIdentifier(), ATTR_ID, r.getId());
			addToListAttribute(nodeAttr, nn.getIdentifier(), ATTR_PATHWAY_URL, r.getUrl());
			addToListAttribute(nodeAttr, nn.getIdentifier(), ATTR_NAME, r.getName());
			addToListAttribute(nodeAttr, nn.getIdentifier(), ATTR_SPECIES, r.getSpecies());
			addToListAttribute(nodeAttr, nn.getIdentifier(), ATTR_ID, r.getId());
			nodeAttr.setUserEditable(ATTR_PATHWAY_URL, false);
			nodeAttr.setUserEditable(ATTR_NAME, false);
			nodeAttr.setUserEditable(ATTR_SPECIES, false);
			nodeAttr.setUserEditable(ATTR_ID, false);
			edgeAttr.setUserEditable(ATTR_PATHWAY_URL, false);
			edgeAttr.setUserEditable(ATTR_NAME, false);
			edgeAttr.setUserEditable(ATTR_SPECIES, false);
			edgeAttr.setUserEditable(ATTR_ID, false);
		}
		return addedNodes;
	}

	private void addToListAttribute(CyAttributes attr, String id, String name, Object value) {
		List list = attr.getListAttribute(id, name);
		if(list == null) list = new ArrayList();
		if(!list.contains(value)) list.add(value);
		attr.setListAttribute(id, name, list);
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
		public Organism species = null;
	}

	public static class GetPathwayParameters {
		public String id;
		public int revision;
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

	class SearchInteractionsTask implements Task {
		TaskMonitor monitor;
		InteractionQuery query;

		public SearchInteractionsTask(InteractionQuery query) {
			this.query = query;
		}

		public String getTitle() {
			return "Searching interactions...";
		}

		public void halt() {
		}

		public void run() {
			try {
				WSSearchResult[] result = getStub().findInteractions(query.getAttributeValue());
				addInteractions(query, result);
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

		public void setTaskMonitor(TaskMonitor m)
				throws IllegalThreadStateException {
			monitor = m;
		}
	}
}
