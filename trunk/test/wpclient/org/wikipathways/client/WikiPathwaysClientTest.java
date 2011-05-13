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
package org.wikipathways.client;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;

import junit.framework.TestCase;

import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.pathvisio.wikipathways.webservice.WSSearchResult;
import org.wikipathways.client.WikiPathwaysClient;

public class WikiPathwaysClientTest extends TestCase {
	protected void setUp() throws Exception {
		Logger.log.setStream(System.err);
		Logger.log.setLogLevel(true, true, true, true, true, true);
	}

	WikiPathwaysClient client;

	public WikiPathwaysClientTest() {
		super();
		try {
			client = new WikiPathwaysClient(new URL(
					"http://localhost/wikipathways/wpi/webservice/webservice.php"
			));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	String user = "TestUser";
	String pass = "h4ppyt3st1ng";

	private void login() throws RemoteException {
		client.login(user, pass);
	}

	public void testLogin() {
		try {
			login();
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testGetXrefs() {
		try {
			String[] ids = client.getXrefList("WP4", BioDataSource.ENTREZ_GENE);
			assertTrue("Number of returned ids is zero", ids.length >= 0);
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	public void testListPathways() {
		try {
			WSPathwayInfo[] r = client.listPathways();
			Logger.log.trace("Get pathway list returned " + r.length + " pathways");
			r = client.listPathways(Organism.HomoSapiens);
			Logger.log.trace("Get pathway list for Homo sapiens returned " + r.length + " pathways");
			//Check if all pathways are indeed human
			for(WSPathwayInfo wpi : r) {
				if(!Organism.HomoSapiens.latinName().equals(wpi.getSpecies())) {
					fail("Get pathway list for Homo sapiens included non-human pathway: " +
							wpi.getId() + " (" + wpi.getSpecies() + ")");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testGetRecentChanges() {
		try {
			WSPathwayInfo[] r = client.getRecentChanges(new Date(2008, 01, 01));
			for(WSPathwayInfo pi : r) {
				Logger.log.trace("Recent change: " + pi.getSpecies() + ":" + pi.getName());
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testGetPathway() {
		try {
			WSPathway wsp = client.getPathway("WP1");
			assertEquals("Returned wrong pathway", "WP1", wsp.getId());
			try {
				WikiPathwaysClient.toPathway(wsp);
			} catch(ConverterException e) {
				fail("Unable to create pathway object from response");
			}
			WikiPathwaysClient.toPathway(wsp);
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testUpdatePathway() {
		try {
			login();
			WSPathway wsp = client.getPathway("WP1");
			Pathway p = WikiPathwaysClient.toPathway(wsp);
			p.getMappInfo().addComment("Soap test - " + System.currentTimeMillis(), "Soap test");

			client.updatePathway(
					"WP1", p,
					"Soap test - " + System.currentTimeMillis(),
					Integer.parseInt(wsp.getRevision()));
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testSearchXref() {
		try {
			WSSearchResult[] results = client.findPathwaysByXref(
					new Xref("8743", BioDataSource.ENTREZ_GENE)
			);
			assertNotNull(results);
			assertTrue(results.length > 0);

			results = client.findPathwaysByXref(
					new Xref("GO:0016021", BioDataSource.GENE_ONTOLOGY)
			);
			assertNotNull(results);
			assertTrue(results.length > 0);

			results = client.findPathwaysByXref(
					new Xref("8743", BioDataSource.ENTREZ_GENE),
					new Xref("GO:0016021", BioDataSource.GENE_ONTOLOGY),
					new Xref("1234", null)
			);
			assertNotNull(results);
			assertTrue(results.length > 0);
		} catch (RemoteException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}