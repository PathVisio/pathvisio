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
package org.pathvisio.wikipathways.webservice;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.Pathway;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.GetPathway;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.GetPathwayAs;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.GetPathwayList;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.GetPathwayListResponse;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.GetPathwayResponse;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.GetRecentChanges;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.GetRecentChangesResponse;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.Login;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.UpdatePathway;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.WSAuth;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.WSPathway;
import org.pathvisio.wikipathways.webservice.WikiPathwaysStub.WSPathwayInfo;

public class WebserviceTest extends TestCase {
	protected void setUp() throws Exception {
		Logger.log.setStream(System.err);
		Logger.log.setLogLevel(true, true, true, true, true, true);
	}
	
	public void testListPathways() {
		try {
			WikiPathwaysStub stub = new WikiPathwaysStub();
			GetPathwayListResponse r = stub.getPathwayList(new GetPathwayList());
			Logger.log.trace("Get pathway list returned " + r.getPathways().length + " pathways");
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
		
	public void testGetRecentChanges() {
		try {
			WikiPathwaysStub stub = new WikiPathwaysStub();
			GetRecentChanges grc = new GetRecentChanges();
			grc.setTimestamp("20080618120000");
			GetRecentChangesResponse r = stub.getRecentChanges(grc);
			for(WSPathwayInfo pi : r.getPathways()) {
				Logger.log.trace("Recent change: " + pi.getSpecies() + ":" + pi.getName());
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Currently fails, see http://wso2.org/forum/thread/3884
	 */
	public void testNoRecentChanges() {
		try {
			WikiPathwaysStub stub = new WikiPathwaysStub();
			GetRecentChanges grc = new GetRecentChanges();
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMddHHmmss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			String timestamp = sdf.format(new Date()); //Use now as cutoff date, so recent changes will be empty
			grc.setTimestamp(timestamp);
			GetRecentChangesResponse r = stub.getRecentChanges(grc);
			assertEquals(r.getPathways(), null);
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testGetPathway() {
		try {
			WSPathway wsp = getPathway();
			assertTrue(("Apoptosis".equals(wsp.getName())));
			Logger.log.trace("Revision: " + wsp.getRevision());
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testLogin() {
		try {
			login();
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testGetPathwayAs() {
		try {
			WikiPathwaysStub stub = new WikiPathwaysStub();
			GetPathwayAs get = new GetPathwayAs();
			get.setFileType("txt");
			get.setPwName("Sandbox");
			get.setPwSpecies("Homo sapiens");
			get.setRevision(BigInteger.valueOf(0));
			stub.getPathwayAs(get);
			//Test passes when no fault is returned from the webservice
			//TODO: find a way to test the data itself
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testUpdatePathway() {
		try {
			WikiPathwaysStub stub = new WikiPathwaysStub();

			WSPathway wsp = getPathway();
			Pathway p = new Pathway();
			p.readFromXml(new StringReader(wsp.getGpml()), true);
			p.getMappInfo().setMapInfoName("Soap test - " + System.currentTimeMillis());
			String key = login();
			
			UpdatePathway update = new UpdatePathway();
			WSAuth wsauth = new WSAuth();
			wsauth.setUser(user);
			wsauth.setKey(key);
			update.setAuth(wsauth);
			update.setDescription("unit test");
			update.setPwName(wsp.getName());
			update.setPwSpecies(wsp.getSpecies());
			update.setRevision(Integer.parseInt(wsp.getRevision()));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GpmlFormat.writeToXml(p, out, true);
			update.setGpml(out.toString());
			stub.updatePathway(update);
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	String user = "TestUser";
	String pass = "h4ppyt3st1ng";
	
	private String login() throws RemoteException {
		WikiPathwaysStub stub = new WikiPathwaysStub();
		Login login = new Login();
		login.setName(user);
		login.setPass(pass);
		String key = stub.login(login).getAuth();
		Logger.log.trace("Logged in as '" + user + "'; key = '" + key + "'");
		return key;
	}
	
	private WSPathway getPathway() throws RemoteException {
		WikiPathwaysStub stub = new WikiPathwaysStub();

		GetPathway getPathway = new GetPathway();
		getPathway.setPwName("Apoptosis");
		getPathway.setPwSpecies("Homo sapiens");
		getPathway.setRevision(new BigInteger("0"));
		GetPathwayResponse response = stub.getPathway(getPathway);
		WSPathway wsp = response.getPathway();
		return wsp;
	}
}
