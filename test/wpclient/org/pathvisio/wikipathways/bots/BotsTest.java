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
package org.pathvisio.wikipathways.bots;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import junit.framework.TestCase;

import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.bots.Bot.BotException;
import org.pathvisio.wikipathways.bots.Bot.Result;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

public class BotsTest extends TestCase {
	protected void setUp() throws Exception {
		Logger.log.setStream(System.err);
		Logger.log.setLogLevel(true, true, true, true, true, true);
	}

	WikiPathwaysClient client;

	public BotsTest() {
		new File(cachePath).mkdirs();
		try {
			client = new WikiPathwaysClient(new URL(
					wsUrl
			));
			client.login(user, pass);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	static final String wsUrl = "http://localhost/wikipathways/wpi/webservice/webservice.php";
	static final String user = "TestUser";

	String botUser = "TestBot";//Should have webservice write permissions
	String botPass = "****";


	static final String pass = "h4ppyt3st1ng";
	static final String cachePath = "tmp";

	ConnectorBot createConnectorBot() throws BotException {
		Properties p = new Properties();
		p.put(ConnectorBot.PROP_WS_URL, wsUrl);
		p.put(ConnectorBot.PROP_USER, botUser);
		p.put(ConnectorBot.PROP_PASS, botPass);
		p.put(ConnectorBot.PROP_CACHE, cachePath);
		p.put(ConnectorBot.PROP_THRESHOLD, "1");
		return new ConnectorBot(p);
	}

	public void testConnectorTag() {
		Pathway pathway = new Pathway();
		pathway.getMappInfo().setOrganism(Organism.HomoSapiens.latinName());
		//Add a pathway with an unconnected line
		PathwayElement line = PathwayElement.createPathwayElement(ObjectType.LINE);
		pathway.add(line);
		WSPathwayInfo pwi = createConnectorPathway(pathway);
		addConnectorTag(pwi.getId());
		removeConnectorTag(pwi.getId());
		overrideConnectorTag(pwi.getId());
	}

	private WSPathwayInfo createConnectorPathway(Pathway pathway) {
		WSPathwayInfo pwi = null;
		try {
			pwi = client.createPathway(pathway);
			return pwi;
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return pwi;
	}

	/**
	 * Test if a tag is added when needed.
	 */
	private void addConnectorTag(String pwId) {
		try {
			//Run the bot, it should tag the created pathway
			Bot bot = createConnectorBot();
			Collection<Result> results = bot.scan();
			BotReport report = bot.createReport(results);
			bot.applyCurationTags(results, report);

			//Check if the pathway is tagged
			WSCurationTag[] tags = client.getCurationTags(pwId);
			boolean found = false;
			for(WSCurationTag t : tags) {
				if(t.getName().equals(ConnectorBot.CURATIONTAG)) {
					found = true;
					break;
				}
			}
			assertTrue("Should have added connector tag", found);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test if tag is removed when needed.
	 */
	private void removeConnectorTag(String pwId) {
		try {
			WSPathway wsp = client.getPathway(pwId);
			//Connect the line and update the pathway
			Pathway pathway = WikiPathwaysClient.toPathway(wsp);
			PathwayElement line = null;
			for(PathwayElement pwe : pathway.getDataObjects()) {
				if(pwe.getObjectType() == ObjectType.LINE) {
					line = pwe;
					break;
				}
			}
			assertNotNull(line);

			PathwayElement e1 = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			PathwayElement e2 = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			pathway.add(e1);
			pathway.add(e2);
			e1.setGeneratedGraphId(); e2.setGeneratedGraphId();
			line.getMStart().setGraphRef(e1.getGraphId());
			line.getMEnd().setGraphRef(e2.getGraphId());
			client.updatePathway(pwId, pathway, "junit test - connecting line",
					Integer.parseInt(wsp.getRevision()));

			//Run the bot, it should remove the tag
			Bot bot = createConnectorBot();
			Collection<Result> results = bot.scan();
			BotReport report = bot.createReport(results);
			bot.applyCurationTags(results, report);

			//Check if the pathway is tagged
			WSCurationTag[] tags = client.getCurationTags(wsp.getId());
			boolean found = false;
			for(WSCurationTag t : tags) {
				if(t.getName().equals(ConnectorBot.CURATIONTAG)) {
					found = true;
					break;
				}
			}
			assertFalse("Should have removed connector tag", found);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test if override mechanism works properly.
	 */
	public void overrideConnectorTag(String pwId) {
		try {
			WSPathway wsp = client.getPathway(pwId);
			Pathway pathway = WikiPathwaysClient.toPathway(wsp);

			//Reset the line
			for(PathwayElement pwe : new ArrayList<PathwayElement>(pathway.getDataObjects())) {
				switch(pwe.getObjectType()) {
				case DATANODE:
				case LINE:
					pathway.remove(pwe);
					break;
				}
			}
			PathwayElement line = PathwayElement.createPathwayElement(ObjectType.LINE);
			pathway.add(line);
			client.updatePathway(pwId, pathway, "junit test", Integer.parseInt(wsp.getRevision()));

			//Re-add the tag
			addConnectorTag(pwId);

			//Manually remove the tag, it shouldn't be re-added
			client.removeCurationTag(pwId, ConnectorBot.CURATIONTAG);

			//Try to re-add the tag, it should fail
			//Run the bot, it should tag the created pathway
			Bot bot = createConnectorBot();
			Collection<Result> results = bot.scan();
			BotReport report = bot.createReport(results);
			bot.applyCurationTags(results, report);

			WSCurationTag[] tags = client.getCurationTags(pwId);
			boolean found = false;
			for(WSCurationTag t : tags) {
				if(t.getName().equals(ConnectorBot.CURATIONTAG)) {
					found = true;
					break;
				}
			}
			assertFalse("Should not have added connector tag", found);

			//Change the number of unconnected lines, the tag should be added again
			PathwayElement line2 = PathwayElement.createPathwayElement(ObjectType.LINE);
			pathway.add(line2);

			wsp = client.getPathway(pwId);
			client.updatePathway(pwId, pathway, "junit test", Integer.parseInt(wsp.getRevision()));

			addConnectorTag(pwId);
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
