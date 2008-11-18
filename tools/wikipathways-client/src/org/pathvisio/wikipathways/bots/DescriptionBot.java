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
package org.pathvisio.wikipathways.bots;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.pathvisio.data.DataException;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement.Comment;
import org.pathvisio.wikipathways.WikiPathways;
import org.pathvisio.wikipathways.WikiPathwaysCache;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

/**
 * A bot to check for empty description fields and add a curation tag if the description
 * field is empty.
 */
public class DescriptionBot {
	private static final String CURATIONTAG = "Curation:MissingDescription";
	WikiPathwaysCache cache;
	WikiPathwaysClient client;
	
	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting Description Bot");
			DescriptionBot bot = new DescriptionBot(new File(args[0]));
			Map<WSPathwayInfo, Boolean> results = bot.scan();
			
			Logger.log.trace("Generating report");
			BotReport report = new BotReport(
					new String[] { "Has description" }
			);
			report.setTitle("DescriptionBot scan report");
			report.setDescription("Checks pathways for an empty description field");
			
			int nrMissing = 0;
			for(WSPathwayInfo p : results.keySet()) if(!results.get(p)) nrMissing++;
			report.setComment("Number of pathways", "" + results.size());
			report.setComment("Number of pathways missing description", "" + nrMissing);
			
			for(WSPathwayInfo p : results.keySet()) {
				report.setRow(p, new String[] { results.get(p) + "" });
			}
			
			File txtFile = new File(args[1] + ".txt");
			Logger.log.trace("Writing text report");
			report.writeTextReport(txtFile);
			
			Logger.log.trace("Writing HTML report");
			File htmlFile = new File(args[1] + ".html");
			report.writeHtmlReport(htmlFile);

			if(args.length == 4) {
				bot.client.login(args[2], args[3]);
				bot.applyCurationTags(results);
			}
		} catch(Exception e) {
			e.printStackTrace();
			printUsage();
		}
	}
	
	static private void printUsage() {
		System.out.println(
			"Usage:\n" +
			"java org.pathvisio.wikipathways.bots.DescriptionBot cacheDir reportFile [user pass]\n" +
			"Where:\n" +
			"-cacheDir: the directory that will be used to cache downloaded pathways\n" +
			"-reportFile: the base name of the files to save the reports to (will be appended" +
			" with correct extension automatically).\n" +
			"-user is the username of the WikiPathways account that will be used for tagging\n" +
			"-pass is the password for the WikiPathways account" +
			"If the description field is empty, a curation tag will " +
			"be added to the pathway. If no user account information is given, no pathways will be tagged."
		);
	}
	
	public DescriptionBot(File cacheDir, WikiPathwaysClient client) throws DataException, IOException, ServiceException {
		this.client = client;
		if(client == null) {
			this.client = new WikiPathwaysClient();
		}
		cache = new WikiPathwaysCache(this.client, cacheDir);
	}
	
	public DescriptionBot(File cacheDir) throws DataException, IOException, ServiceException {
		this(cacheDir, null);
	}
	
	public void applyCurationTags(Map<WSPathwayInfo, Boolean> pathways) throws RemoteException {
		Logger.log.info("Tagging pathways");
		for(WSPathwayInfo pwi : pathways.keySet()) {
			Logger.log.info("Getting current tag " + CURATIONTAG + " for " + pwi.getName());
			//Find an existing tag
			String pwId = pwi.getId();
			//First check if the existing tag is up-to-date
			WSCurationTag[] tags = client.getCurationTags(pwId);
			String currTagText = null;
			for(WSCurationTag t : tags) {
				if(CURATIONTAG.equals(t.getName())) {
					currTagText = t.getText();
					break;
				}
			}
			Logger.log.info("\tCurrent tag: " + currTagText);
			//Apply a tag if there is no description
			if(!pathways.get(pwi)) {
				if(currTagText == null) {
					Logger.log.info("Applying tag to " + pwId);
					client.saveCurationTag(
							pwId,
							CURATIONTAG, ""
					);
				}
			} else {
				//Remove the existing tag
				if(currTagText != null) {
					Logger.log.info("Removing tag from " + pwId);
					client.removeCurationTag(pwId, CURATIONTAG);
				}
			}
		}
	}
	
	public Map<WSPathwayInfo, Boolean> scan() throws ConverterException, FileNotFoundException, IOException {
		cache.update();
		
		//The pathways that miss a description
		Map<WSPathwayInfo, Boolean> pathways = new HashMap<WSPathwayInfo, Boolean>();
		for(File f : cache.getFiles()) {
			Pathway p = new Pathway();
			p.readFromXml(f, true);
			
			String comment = null;
			for(Comment c : p.getMappInfo().getComments()) {
				if(WikiPathways.COMMENT_DESCRIPTION.equals(c.getSource())) {
					comment = c.getComment();
					break;
				}
			}
			boolean hasDescr = comment != null && !"".equals(comment);
			WSPathwayInfo wsp = cache.getPathwayInfo(f);
			pathways.put(wsp, hasDescr);
		}
		return pathways;
	}
}