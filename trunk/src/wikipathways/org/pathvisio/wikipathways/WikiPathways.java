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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

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
import org.pathvisio.data.Gdb;
import org.pathvisio.model.ConverterException;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayWrapper;
import org.xml.sax.SAXException;

public class WikiPathways implements ApplicationEventListener {
	public static String SITE_NAME = "WikiPathways.org";
	
	UserInterfaceHandler uiHandler;
	HashMap<String, String> cookie;
	
	File localFile;
	
	boolean ovrChanged;
	
	public WikiPathways(UserInterfaceHandler uiHandler) {
		this.uiHandler = uiHandler;
		cookie = new HashMap<String, String>();
		Engine.addApplicationEventListener(this);
	}

	public void init(VPathwayWrapper wrapper) throws Exception {
		WikiPathwaysEngine.init();
		
		for(Parameter p : Parameter.values()) {
			//Check for required
			assert !p.isRequired() || p.getValue() != null : 
				"Missing required argument '" + p.name() + "'";
		}

		if(isNew()) { //Create new pathway
			Engine.newPathway(wrapper);
		} else { //Download and open the pathway
			Engine.openPathway(new URL(getPwURL()), wrapper);
		}

		//TODO: notify user about this and hide edit actions
		Engine.getActiveVPathway().setEditMode(isReadOnly());
		
		//Connect to the gene database
		DBConnector connector = new DBConnectorDerbyServer("wikipathways.org", 1527);
		Engine.setDBConnector(connector, DBConnector.TYPE_GDB);
		
		//Gdb.connect(getPwSpecies());
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
			
	public boolean isNew() {
		return Parameter.PW_NEW.getValue() != null;
	}
	
	public boolean isReadOnly() {
		return getUser() != null;
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
		
	public boolean saveUI() {
		VPathway vPathway = Engine.getActiveVPathway();
		if(vPathway != null && vPathway.getGmmlData().hasChanged()) {
			final String description = uiHandler.askInput("Specify description", "Give a description of your changes");
			if(description != null) {
				RunnableWithProgress<Boolean> r = new RunnableWithProgress<Boolean>() {
					public Boolean excecuteCode() {
						try {
							saveToWiki(description);
							return true;
						} catch (Exception e) {
							Engine.log.error("Unable to save pathway", e);
							uiHandler.showError("Unable to save pathway", e.getClass() + ": " + e.getMessage());
						}
						return false;
					}
				};
				uiHandler.runWithProgress(r, "Saving pathway", ProgressKeeper.PROGRESS_UNKNOWN, false, true);
				return r.get();
			}
		}
		return false;
	}
	
	protected void saveToWiki(String description) throws XmlRpcException, IOException, ConverterException {		
		//TODO: check if changed
		if(ovrChanged || Engine.getActivePathway().hasChanged()) {
			ovrChanged = true; //In case we get an error, save changes next time
			File gpmlFile = getLocalFile();
			//Save current pathway to local file
			Engine.savePathway(gpmlFile);
			saveToWiki(description, gpmlFile);
			ovrChanged = false; //Save successful, don't save next time
		} else {
			Engine.log.trace("No changes made, ignoring save");
			//Do nothing, no changes made
		}
	}
	
	protected void saveToWiki(String description, File gpmlFile) throws XmlRpcException, IOException {	
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(getRpcURL()));
	
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcCookieTransportFactory ctf = new XmlRpcCookieTransportFactory(client);
	
		XmlRpcCookieHttpTransport ct = (XmlRpcCookieHttpTransport)ctf.getTransport();
		for(String key : cookie.keySet()) {
			Engine.log.trace("Setting cookie: " + key + "=" + cookie.get(key));
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

	
	public void applicationEvent(ApplicationEvent e) {
		/*
		switch(e.type) {
		case ApplicationEvent.APPLICATION_CLOSE:
			VPathway vPathway = Engine.getActiveVPathway();
			if(vPathway == null || vPathway.getGmmlData().hasChanged()) {
				int status  = uiHandler.askCancellableQuestion("", 
						"Do you want to save the changes to " + getPwName() + " on " + SITE_NAME + "?");
				if(status == UserInterfaceHandler.Q_TRUE) {
					saveUI();
				} else if(status == UserInterfaceHandler.Q_CANCEL) {
					e.doit = false;
				}
			} else {
				//Silently close
			}
		}
		*/
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
}
