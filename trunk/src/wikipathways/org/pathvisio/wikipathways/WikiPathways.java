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
package org.pathvisio.wikipathways;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcHttpClientConfig;
import org.apache.xmlrpc.client.XmlRpcHttpTransport;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.DBConnectorDerbyServer;
import org.pathvisio.data.GdbManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.gui.swing.actions.CommonActions;
import org.pathvisio.gui.wikipathways.Actions;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Pathway.StatusFlagEvent;
import org.pathvisio.model.Pathway.StatusFlagListener;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.view.VPathway;
import org.xml.sax.SAXException;

/**
 * Base class that handles WikiPathways related actions for the pathway editor applet
 * @author thomas
 *
 */
public class WikiPathways implements ApplicationEventListener, StatusFlagListener {		
	public static final String COMMENT_DESCRIPTION = "WikiPathways-description";
	public static final String COMMENT_CATEGORY = "WikiPathways-category";
	
	UserInterfaceHandler uiHandler;
	HashMap<String, String> cookie;
	
	File localFile;
	
	/**
	 * Keep track of changes with respect to the remote version of the pathway
	 * (because the {@link Pathway#hasChanged()} also depends on locally saved version
	 */
	boolean remoteChanged;
	boolean initPerformed;
	boolean isInit;
	
	MainPanel mainPanel;
	
	public WikiPathways(UserInterfaceHandler uiHandler) {
		this.uiHandler = uiHandler;
		cookie = new HashMap<String, String>();
		
		Engine.getCurrent().addApplicationEventListener(this);
	}
	
	public void setUiHandler(UserInterfaceHandler uih) {
		uiHandler = uih;
	}
		
	public void init(ProgressKeeper progress, URL base) throws Exception {
		isInit = true;
		
		progress.setTaskName("Starting editor");
		
		WikiPathwaysInit.init();
		WikiPathwaysInit.registerXmlRpcExporters(new URL(getRpcURL()), Engine.getCurrent());
		
		loadCookies(base);
		
		for(Parameter p : Parameter.values()) {
			//Check for required
			if(p.isRequired()) {
				assert p.getValue() != null : 
					"Missing required argument '" + p.name() + "'";
			}	
		}
		
		progress.report("Loading pathway...");
		
		if(isNew()) { //Create new pathway
			Logger.log.trace("WIKIPATHWAYS INIT: new pathway");
			Engine.getCurrent().newPathway();
			//Set the initial information
			Pathway p = Engine.getCurrent().getActivePathway();
			PathwayElement info = p.getMappInfo();
			info.setMapInfoName(Parameter.PW_NAME.getValue());
			info.setAuthor(Parameter.USER.getValue());
			info.setOrganism(Parameter.PW_SPECIES.getValue());
		} else { //Download and open the pathway
			Logger.log.trace("WIKIPATHWAYS INIT: open pathway");
			Engine.getCurrent().openPathway(new URL(getPwURL()));
			Engine.getCurrent().getActivePathway().setSourceFile(null); //To trigger save as
		}
		
		//Register status flag listener to override changed flag for local saves
		Engine.getCurrent().getActivePathway().addStatusFlagListener(this);
		
		initVPathway();
		
		if(isReadOnly()) {
			uiHandler.showInfo("Read-only", 
					"You are not logged in to " + Globals.SERVER_NAME +
					" so the pathway will be opened in read-only mode");
		}
		
		progress.report("Connecting to database...");
		
		//Connect to the gene database
		DBConnector connector = new DBConnectorDerbyServer(Parameter.GDB_SERVER.getValue(), 1527);
		Engine.getCurrent().setDBConnector(connector, DBConnector.TYPE_GDB);
		
		GdbManager.setGeneDb(getPwSpecies());
		
		GdbManager.setMetaboliteDb("metabolites");
		
		isInit = false;
		initPerformed = true;
	}
	
	public boolean initPerformed() {
		return initPerformed;
	}
	
	public boolean isInit() {
		return isInit;
	}
	
	public void initVPathway() {
		Engine e = Engine.getCurrent();
		Pathway p = e.getActivePathway();
		VPathway vp = e.getActiveVPathway();
		if(p != null && vp == null) {
			Logger.log.trace("Create VPathway");
			e.createVPathway(p);
		}
		vp = e.getActiveVPathway();
		if(vp != null) {
			vp.setEditMode(!isReadOnly());
		}
	}
	
	/**
	 * Returns true when the pathway has changed with respect to the
	 * last saved wiki version
	 */
	public boolean hasChanged() {
		return remoteChanged || Engine.getCurrent().getActivePathway().hasChanged();
	}
	public String getPwName() {
		return Parameter.PW_NAME.getValue();
	}

	public String getPwSpecies() {
		return Parameter.PW_SPECIES.getValue();
	}

	public String getPwURL() {
		return Parameter.PW_URL.getValue();
	}

	public String getRpcURL() {
		return Parameter.RPC_URL.getValue();
	}

	public String getUser() {
		return Parameter.USER.getValue();
	}

	public void addCookie(String key, String value) {
		cookie.put(key, value);
	}
			
	public void loadCookies(URL url) {
		Logger.log.trace("Loading cookies");

		try {
			CookieHandler handler = CookieHandler.getDefault();
			if (handler != null)    {
				Map<String, List<String>> headers = handler.get(url.toURI(), new HashMap<String, List<String>>());
				if(headers == null) {
					Logger.log.error("Unable to load cookies: headers null");
					return;
				}
				List<String> values = headers.get("Cookie");
				for (String c : values) {
					String[] cvalues = c.split(";");
					for(String cv : cvalues) {
						String[] keyvalue = cv.split("=");
						if(keyvalue.length == 2) {
							Logger.log.trace("COOKIE: " + keyvalue[0] + " | " + keyvalue[1]);
							addCookie(keyvalue[0].trim(), keyvalue[1].trim());
						}
					}
				}
			}
		} catch(Exception e) {
			Logger.log.error("Unable to load cookies", e);
		}
	}
	
	public boolean isNew() {
		return Parameter.PW_NEW.getValue() != null;
	}
	
	public boolean isReadOnly() {
		return getUser() == null;
	}
	
	protected File getLocalFile() { 
		if(localFile == null) {
			try {
				localFile = File.createTempFile("tmp", ".gpml");
			} catch(Exception e) {
				return null;
			}
		}
		return localFile;
	}
		
	public UserInterfaceHandler getUserInterfaceHandler() {
		return uiHandler;
	}
	
	public boolean saveUI(String description) {
		Pathway pathway = Engine.getCurrent().getActivePathway();
		if(!remoteChanged && !pathway.hasChanged()) {
			uiHandler.showInfo("Save pathway", "You didn't make any changes");
			return false;
		}
		if(pathway != null) {
			if(description == null) {
				description = uiHandler.askInput("Specify description", "Give a description of your changes");
			}
			final String finalDescription = description;
			Logger.log.trace("Save description: " + description);
			if(description != null) {
				RunnableWithProgress<Boolean> r = new RunnableWithProgress<Boolean>() {
					public Boolean excecuteCode() {
						getProgressKeeper().setTaskName("Saving pathway");
						try {
							saveToWiki(finalDescription);
							return true;
						} catch (Exception e) {
							Logger.log.error("Unable to save pathway", e);
							uiHandler.showError("Unable to save pathway", e.getClass() + 
									"\n See error log (" + GlobalPreference.FILE_LOG.getValue() + ") for details");
						}
						return false;
					}
				};
				uiHandler.runWithProgress(r, "", ProgressKeeper.PROGRESS_UNKNOWN, false, true);
				return r.get();
			}
		}
		return false;
	}
	
	protected void saveToWiki(String description) throws XmlRpcException, IOException, ConverterException {		
		if(remoteChanged || Engine.getCurrent().getActivePathway().hasChanged()) {
			File gpmlFile = getLocalFile();
			//Save current pathway to local file
			Engine.getCurrent().savePathway(gpmlFile);
			remoteChanged = true; //In case we get an error, save changes next time
			saveToWiki(description, gpmlFile);
			remoteChanged = false; //Save successful, don't save next time
		} else {
			Logger.log.trace("No changes made, ignoring save");
			throw new ConverterException("You didn't make any changes");
		}
	}
	
	protected void saveToWiki(String description, File gpmlFile) throws XmlRpcException, IOException {	
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(getRpcURL()));
	
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcCookieTransportFactory ctf = new XmlRpcCookieTransportFactory(client);
	
		XmlRpcCookieHttpTransport ct = (XmlRpcCookieHttpTransport)ctf.getTransport();
		for(String key : cookie.keySet()) {
			Logger.log.trace("Setting cookie: " + key + "=" + cookie.get(key));
			ct.addCookie(key, cookie.get(key));
		}
		
		client.setTransportFactory(ctf);
		client.setConfig(config);
		
		RandomAccessFile raf = new RandomAccessFile(gpmlFile, "r");
		byte[] data = new byte[(int)raf.length()];
		raf.readFully(data);
		byte[] data64 = Base64.encodeBase64(data);
		Object[] params = new Object[]{ getPwName(), getPwSpecies(), description, data64 };
				
		client.execute("WikiPathways.updatePathway", params);
	}

	static class XmlRpcCookieTransportFactory implements XmlRpcTransportFactory {
		private final XmlRpcCookieHttpTransport TRANSPORT;

		public XmlRpcCookieTransportFactory(XmlRpcClient pClient) {
			TRANSPORT = new XmlRpcCookieHttpTransport(pClient);
		 }
		
		public XmlRpcTransport getTransport() { return TRANSPORT; }
	}

	/** Implementation of an HTTP transport that supports sending cookies with the
	 * HTTP header, based on the {@link java.net.HttpURLConnection} class.
	 */
	public static class XmlRpcCookieHttpTransport extends XmlRpcHttpTransport {
		private static final String userAgent = USER_AGENT + " (Sun HTTP Transport, mod Thomas)";
		private static final String cookieHeader = "Cookie";
		private URLConnection conn;
		private HashMap<String, String> cookie;
		
		public XmlRpcCookieHttpTransport(XmlRpcClient pClient) {
			super(pClient, userAgent);
			cookie = new HashMap<String, String>();
		}

		public void addCookie(String key, String value) {
			cookie.put(key, value);
		}
		
		protected void setCookies() {
			String cookieString = null;
			for(String key : cookie.keySet()) {
				cookieString = (cookieString == null ? "" : cookieString + "; ") + key + "=" + cookie.get(key);
			}
			if(cookieString != null) {
				conn.setRequestProperty(cookieHeader, cookieString);
			}
		}
		
		public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
			XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pRequest.getConfig();
			try {
				conn = config.getServerURL().openConnection();
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);
				setCookies();
			} catch (IOException e) {
				throw new XmlRpcException("Failed to create URLConnection: " + e.getMessage(), e);
			}
			return super.sendRequest(pRequest);
		}

		protected void setRequestHeader(String pHeader, String pValue) {
			conn.setRequestProperty(pHeader, pValue);
			
		}

		protected void close() throws XmlRpcClientException {
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		}

		protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig) {
			return HttpUtil.isUsingGzipEncoding(conn.getHeaderField("Content-Encoding"));
		}

		protected InputStream getInputStream() throws XmlRpcException {
			try {
				return conn.getInputStream();
			} catch (IOException e) {
				throw new XmlRpcException("Failed to create input stream: " + e.getMessage(), e);
			}
		}

		protected void writeRequest(ReqWriter pWriter) throws IOException, XmlRpcException, SAXException {
	        pWriter.write(conn.getOutputStream());
		}
	}
	
	public MainPanel getMainPanel() {
		if(mainPanel == null) {
			prepareMainPanel();
		}
		return mainPanel;
	}
	public MainPanel prepareMainPanel() {
		CommonActions actions = SwingEngine.getCurrent().getActions();
		Set<Action> hide = new HashSet<Action>();
		
		//Disable some actions
		if(!isNew()) hide.add(actions.importAction);
		
		Action saveAction = new Actions.ExitAction(uiHandler, this, true, null);
		Action discardAction = new Actions.ExitAction(uiHandler, this, false, null);
				
		mainPanel = new MainPanel(hide);
		
		mainPanel.getToolBar().addSeparator();
		
		mainPanel.addToToolbar(saveAction, MainPanel.TB_GROUP_SHOW_IF_EDITMODE);
		mainPanel.addToToolbar(discardAction);

		mainPanel.getBackpagePane().addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					uiHandler.showDocument(e.getURL(), "_blank");
				}
			}
		});	
		
		SwingEngine.getCurrent().setApplicationPanel(mainPanel);
		return mainPanel;
	}
	
	public void applicationEvent(ApplicationEvent e) {
		Pathway p = null;
		switch(e.getType()) {
		case ApplicationEvent.PATHWAY_NEW:
			p = (Pathway)e.getSource();
			p.getMappInfo().setOrganism(Organism.fromShortName(getPwSpecies()).latinName());
			p.getMappInfo().setMapInfoName(getPwName());
			break;
		case ApplicationEvent.PATHWAY_OPENED:
			p = (Pathway)e.getSource();
			//Force species name to be te same as on wikipathways
			String impSpecies = p.getMappInfo().getOrganism();
			Organism impOrg = Organism.fromLatinName(impSpecies);
			Organism wikiOrg = Organism.fromShortName(getPwSpecies());
			if(!wikiOrg.equals(impOrg)) {
				uiHandler.showError("Invalid species",
						"The species of the pathway you imported differs from the" +
						" species for the " + Globals.SERVER_NAME + " pathway you are editing.\n" +
						"It will be changed from '" + impSpecies + "' to '" + wikiOrg.latinName() + "'");
				p.getMappInfo().setOrganism(wikiOrg.latinName());
			}
			break;
		}
	}
	
	public void statusFlagChanged(StatusFlagEvent e) {
		//Set our own flag to true if changes are detected
		if(e.getNewStatus()) {
			remoteChanged = true;
		}
	}
}
