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
package org.pathvisio.wikipathways;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JOptionPane;

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
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.Globals;
import org.pathvisio.Revision;
import org.pathvisio.data.GdbManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.CommonActions;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.gui.swing.SwingEngine.Browser;
import org.pathvisio.gui.wikipathways.Actions;
import org.pathvisio.gui.wikipathways.SaveReminder;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.Pathway.StatusFlagEvent;
import org.pathvisio.model.Pathway.StatusFlagListener;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.view.VPathway;
import org.xml.sax.SAXException;

/**
 * Base class that handles WikiPathways related actions for the pathway editor applet
 * @author thomas
 *
 */
public class WikiPathways implements StatusFlagListener, ApplicationEventListener {
	public static final String COMMENT_DESCRIPTION = "WikiPathways-description";
	public static final String COMMENT_CATEGORY = "WikiPathways-category";
	static final String APPLICATION_NAME = "WikiPathways pathway editor";

	private boolean disposed;

	private Parameter parameters = new Parameter();

	UserInterfaceHandler uiHandler;
	Map<String, String> cookie;

	File localFile;

	private SwingEngine swingEngine;

	/**
	 * Keep track of changes with respect to the remote version of the pathway
	 * (because the {@link Pathway#hasChanged()} also depends on locally saved version
	 */
	boolean remoteChanged;

	/**
	 * True when the pathway has never been saved before in this
	 * applet session
	 */
	boolean firstSave = true;
	boolean isUseGdb = false;

	MainPanel mainPanel;

	public WikiPathways(UserInterfaceHandler uiHandler, SwingEngine swingEngine)
	{
		this.uiHandler = uiHandler;
		cookie = new HashMap<String, String>();
		this.swingEngine = swingEngine;
		swingEngine.getEngine().addApplicationEventListener(this);
		swingEngine.setUrlBrowser(new Browser() {
			public void openUrl(URL url) {
				WikiPathways.this.uiHandler.showDocument(url, "_blank");
			}
		});
	}

	/**
	 * Set this parameter to true if a connection
	 * to the synonym databases is needed.
	 */
	public void setUseGdb(boolean use) {
		isUseGdb = use;
	}

	public boolean isUseGdb() {
		return isUseGdb;
	}

	/**
	 * Get the parameters container, that contains the
	 * input parameters
	 */
	public Parameter getParameters() {
		return parameters;
	}

	/**
	 * Get the pathway for this wiki instance
	 */
	public Pathway getPathway() {
		return swingEngine.getEngine().getActivePathway();
	}

	/**
	 * Get the pathway view for this wiki instance
	 */
	public VPathway getPathwayView() {
		return swingEngine.getEngine().getActiveVPathway();
	}

	/**
	 * Get the instance of SwingEngine used to hold the pathway
	 * wrapper and main panel. May return null if the editor
	 * has no pathway view (e.g. description editor)
	 */
	public SwingEngine getSwingEngine() {
		return swingEngine;
	}

	public Engine getEngine()
	{
		return swingEngine.getEngine();
	}

	public void setUiHandler(UserInterfaceHandler uih) {
		uiHandler = uih;
	}

	public void init(ProgressKeeper progress, URL base) throws Exception {
		progress.setTaskName("Starting editor");

		WikiPathwaysInit.init(getEngine(), PreferenceManager.getCurrent());
		WikiPathwaysInit.registerXmlRpcExporters(new URL(getRpcURL()), getEngine());

		Logger.log.trace("Code revision: " + Revision.REVISION);

		loadCookies(base);

		for(String name : parameters.getNames()) {
			//Check for required
			if(parameters.isRequired(name)) {
				assert parameters.getValue(name) != null :
					"Missing required argument '" + name + "'";
			}
		}

		progress.report("Loading pathway...");

		if(isNew()) { //Create new pathway
			Logger.log.trace("WIKIPATHWAYS INIT: new pathway");
			getEngine().newPathway();
			//Set the initial information
			setRemoteChanged(true);
			PathwayElement info = getPathway().getMappInfo();
			info.setMapInfoName(getPwName());
			info.setAuthor(getUser());
			info.setOrganism(getPwSpecies());
		} else { //Download and open the pathway
			Logger.log.trace("WIKIPATHWAYS INIT: open pathway");
			getEngine().openPathway(new URL(getPwURL()));
			getPathway().setSourceFile(null); //To trigger save as
		}

		initVPathway();

		if(isReadOnly()) {
			uiHandler.showInfo("Read-only",
					"You are not logged in to " + Globals.SERVER_NAME +
			" so the pathway will be opened in read-only mode");
		}

		//Start the save reminder
		startSaveReminder();

		progress.report("Connecting to database...");

		//Connect to bridgedb (currently supports connections strings for "idmapper-bridgerest" 
		//and "idmapper-jdbc" (for derby client)
		if(isUseGdb()) {
			String bridgeUrl = parameters.getValue(Parameter.GDB_SERVER);
			if(!bridgeUrl.endsWith("/")) bridgeUrl = bridgeUrl + "/";
			String geneUrl = "";
			if(bridgeUrl.startsWith("idmapper-bridgerest")) {
				Class.forName("org.bridgedb.webservice.bridgerest.BridgeRest");
				geneUrl = bridgeUrl + URLEncoder.encode(getPwSpecies(), "UTF-8");
			} else if(bridgeUrl.startsWith("idmapper-jdbc")) {
				Class.forName("org.apache.derby.jdbc.ClientDriver");
				Class.forName("org.bridgedb.rdb.IDMapperRdb");
				geneUrl = bridgeUrl +  getPwSpecies();
			}
			GdbManager gdbManager = swingEngine.getGdbManager();
			
			Logger.log.trace("Bridgedb connection string: " + geneUrl);
			gdbManager.setGeneDb(geneUrl);
			
			//Also connect to the metabolite database if we're using derby
			if(bridgeUrl.startsWith("idmapper-jdbc")) {
				String metUrl = bridgeUrl + "metabolites";
				gdbManager.setMetaboliteDb(metUrl);
			}
		}
	}

	public void dispose() {
		assert(!disposed);
		getEngine().dispose();
		getSwingEngine().dispose();
		swingEngine = null;
		mainPanel.dispose();
		mainPanel = null;
		disposed = true;
	}

	public void startSaveReminder() {
		SaveReminder.startSaveReminder(this, 10);
	}

	private void setRemoteChanged(boolean changed) {
		if(changed != remoteChanged) {
			remoteChanged = changed;
			fireStatusFlagEvent(new StatusFlagEvent(changed));
		}
	}

	public void initVPathway()
	{
		VPathway active = getPathwayView();
		if(active != null) { //Can be null incase of description/category applet
			active.setEditMode(!isReadOnly());
		}
	}

	/**
	 * Returns true when the pathway has changed with respect to the
	 * last saved wiki version
	 */
	public boolean hasChanged() {
		return remoteChanged;
	}

	/**
	 * Flag to override change flag on check for exit
	 */
	private boolean mayExit;

	/**
	 * Checks whether an editor may exit.
	 * @return true when the pathway hasn't changed, or setMayExit() was called with true as argument.
	 * false when the pathway has changed and the setMayExit() wasn't called;
	 */
	public boolean mayExit() {
		return !hasChanged() || mayExit;
	}

	/**
	 * Override the change flag used by {@link #mayExit()}
	 * @param mayExit
	 */
	public void setMayExit(boolean mayExit) {
		this.mayExit = mayExit;
	}

	public String getPwId() {
		return parameters.getValue(Parameter.PW_ID);
	}

	public String getPwName() {
		return parameters.getValue(Parameter.PW_NAME);
	}

	public String getPwSpecies() {
		return parameters.getValue(Parameter.PW_SPECIES);
	}

	public String getPwURL() {
		return parameters.getValue(Parameter.PW_URL);
	}

	public String getSiteURL() {
		return parameters.getValue(Parameter.SITE_URL);
	}

	public String getRpcURL() {
		return parameters.getValue(Parameter.RPC_URL);
	}

	public String getUser() {
		return parameters.getValue(Parameter.USER);
	}

	public int getRevision() {
		return Integer.parseInt(
				parameters.getValue(Parameter.REVISION)
		);
	}

	public boolean isPrivate() {
		return parameters.getValue(Parameter.PRIVATE) != null;
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
		Logger.log.trace("ID: '" + parameters.getValue(Parameter.PW_ID) + "'");
		return "".equals(parameters.getValue(Parameter.PW_ID));
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
		if(!remoteChanged) {
			uiHandler.showInfo("Save pathway", "You didn't make any changes");
			return false;
		}
		if(getPathway() != null) {
			//Force species name to be the same as on wikipathways
			PathwayElement info = getPathway().getMappInfo();
			if(!getPwSpecies().equals(info.getOrganism())) {
				uiHandler.showInfo("Species mismatch",
						"The species of the current pathway doesn't match the " +
						"species of the pathway on " + Globals.SERVER_NAME +
						"\n\nCurrent species:\t  " + info.getOrganism() +
						"\n" + Globals.SERVER_NAME + " species:\t  " + getPwSpecies() +
						"\n\nThe species of the current pathway will be set to " + getPwSpecies() +
				" before saving.");
				info.setOrganism(getPwSpecies());
			}

			if(isNew() && firstSave) { //Automatically fill in description on new pathway
				description = "New pathway";
			}
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
							String msg =  e.getClass() +
							"\n See error log (" + PreferenceManager.getCurrent().get(GlobalPreference.WP_FILE_LOG) + ") for details";
							if(e.getMessage().startsWith("Revision out of date")) {
								msg =
									"Revision out of date.\n" +
									"This could mean somebody else modified the pathway since you downloaded it.\n" +
									"Please save your changes locally and copy your changes over to\n" +
									"the newest version.";
								if(isNew()) { //If this is a new pathway, give the option to save under different name
									String newName = getPwName();
									while(newName != null && getPwName().equals(newName)) {
										newName = uiHandler.askInput(getPwName() + "-1",
												"Somebody else already created a pathway under the same name.\n" +
										"Please specify a new name for this pathway.");
									}
									if(newName != null) {
										String newUrl = parameters.getValue(Parameter.PW_URL).replace(getPwName(), newName);
										parameters.setValue(Parameter.PW_NAME, newName);
										parameters.setValue(Parameter.PW_URL, newUrl);
										return saveUI(finalDescription);
									} else {
										return false;
									}
								}
							}
							uiHandler.showError("Unable to save pathway", msg);
						}
						return false;
					}
				};
				uiHandler.runWithProgress(r, "", false, true);
				return r.get();
			}
		}
		return false;
	}

	/**
	 * Tries to exit the wikipathways editor and redirects to the pathway page. The
	 * user will asked whether to save the pathway if any changes were made.
	 * If an error occurs, the user will be notified by a message and the applet
	 * will not exit.
	 * @param description The description to pass along to {@link #saveUI(String)}, may be null
	 * @param alwaysSave Always save when there are changes, don't ask user
	 * @return returns true when the editor exits, false if exit is aborted
	 */
	public boolean exit(boolean alwaysSave, String description) {
		boolean doSave = alwaysSave;
		try {
			if(!doSave && hasChanged()) {
				//Let user confirm close without save
				int answer = uiHandler.askCancellableQuestion(
						"Save changes?", "Your pathway may have changed. Do you want to save?");
				if(answer == UserInterfaceHandler.Q_CANCEL) {
					return false;
				} else {
					doSave = answer == UserInterfaceHandler.Q_TRUE;
				}
			}
			if(doSave) {
				saveUI(description);
			} else {
				setMayExit(true);
			}
		} catch(Exception ex) {
			Logger.log.error("Unable to save pathway", ex);
			JOptionPane.showMessageDialog(null, "Unable to save pathway:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(mayExit()) {
			Logger.log.trace("MayExit: " + mayExit());
			uiHandler.showExitMessage("Please wait...the page will be reloaded");
			try {
				URL url = null;
				String pwId = getPwId();
				if(pwId == null || "".equals(pwId)) {
					url = new URL(getSiteURL());
				} else {
					url = new URL(getSiteURL() + "/index.php?title=Pathway:" + getPwId());
				}
				Logger.log.error("Redirecting to " + url);
				uiHandler.showDocument(url, "_top");
			} catch (MalformedURLException ex) {
				Logger.log.error("Unable to refresh pathway page", ex);
			}
			return true;
		}
		return false;
	}

	protected void saveToWiki(String description) throws XmlRpcException, IOException, ConverterException {
		if(remoteChanged) {
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

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GpmlFormat.writeToXml(getPathway(), out, true);

			byte[] data = out.toByteArray();

			if(isNew()) {
				Logger.log.error("IS PRIVATE: " + isPrivate());
				Object[] params = new Object[]{ description, data, isPrivate() };
				Object response = client.execute("WikiPathways.createPathway", params);
				Map<String, String> map = (HashMap<String, String>)response;
				parameters.setValue(Parameter.REVISION, map.get("revision"));
				parameters.setValue(Parameter.PW_ID, map.get("id"));
				firstSave = false;
			} else {
				Object[] params = new Object[]{ getPwId(), description, data, getRevision() };
				Object response = client.execute("WikiPathways.updatePathway", params);
				//Update the revision in case we want to save again
				parameters.setValue(Parameter.REVISION, (String)response);
				firstSave = false;
			}

			getPathway().clearChangedFlag();
			setRemoteChanged(false); //Save successful, don't save next time
		} else {
			Logger.log.trace("No changes made, ignoring save");
			throw new ConverterException("You didn't make any changes");
		}
	}

	static class XmlRpcCookieTransportFactory implements XmlRpcTransportFactory {
		private final XmlRpcCookieHttpTransport transport;

		public XmlRpcCookieTransportFactory(XmlRpcClient pClient) {
			transport = new XmlRpcCookieHttpTransport(pClient);
		}

		public XmlRpcTransport getTransport() { return transport; }
	}

	/** Implementation of an HTTP transport that supports sending cookies with the
	 * HTTP header, based on the {@link java.net.HttpURLConnection} class.
	 */
	public static class XmlRpcCookieHttpTransport extends XmlRpcHttpTransport {
		private static final String USER_AGENT_MOD = USER_AGENT + " (Sun HTTP Transport, mod Thomas)";
		private static final String COOKIE_HEADER = "Cookie";
		private URLConnection conn;
		private Map<String, String> cookie;

		public XmlRpcCookieHttpTransport(XmlRpcClient pClient) {
			super(pClient, USER_AGENT_MOD);
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
				conn.setRequestProperty(COOKIE_HEADER, cookieString);
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
		CommonActions actions = swingEngine.getActions();
		Set<Action> hide = new HashSet<Action>();

		//Disable some actions
		hide.add(actions.importAction); //We have our own import action

		Action exitAction = new Actions.ExitAction(uiHandler, this, false, null);

		mainPanel = new MainPanel(swingEngine, hide);
		mainPanel.createAndShowGUI();

		mainPanel.getToolBar().addSeparator();

		//mainPanel.addToToolbar(saveAction, MainPanel.TB_GROUP_SHOW_IF_EDITMODE);
		mainPanel.addToToolbar(exitAction);

		swingEngine.setApplicationPanel(mainPanel);
		return mainPanel;
	}

	public void statusFlagChanged(StatusFlagEvent e) {
		//Set our own flag to true if changes are detected
		if(e.getNewStatus()) {
			setRemoteChanged(true);
		}
	}

	private Set<StatusFlagListener> statusFlagListeners = new HashSet<StatusFlagListener>();

	/**
	 * Register a statusflag listener to check for changes
	 * relative to the server version of the pathway
	 */
	public void addStatusFlagListener(StatusFlagListener l) {
		statusFlagListeners.add(l);
	}

	private void fireStatusFlagEvent(StatusFlagEvent e) {
		for(StatusFlagListener l : statusFlagListeners) {
			l.statusFlagChanged(e);
		}
	}

	public void applicationEvent(ApplicationEvent e) {
		if(e.getType() == ApplicationEvent.PATHWAY_NEW ||
				e.getType() == ApplicationEvent.PATHWAY_OPENED)
		{
			Pathway p = swingEngine.getEngine().getActivePathway();
			p.addStatusFlagListener(this);
			//Copy initial state of statusFlagListener
			//(could have been changed by earlier ApplicationEventListener)
			setRemoteChanged (p.hasChanged());
		}
	}
}
