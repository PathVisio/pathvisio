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
package org.wikipathways.bots;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSCurationTagHistory;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.wikipathways.client.WikiPathwaysCache;
import org.wikipathways.client.WikiPathwaysClient;

public abstract class Bot {
	public static final String PROP_WS_URL = "webservice-url";
	public static final String PROP_CACHE = "cache-path";
	public static final String PROP_USER = "user";
	public static final String PROP_PASS = "pass";

	private WikiPathwaysClient client;
	private WikiPathwaysCache cache;

	private String user;
	private String pass;

	public Bot(Properties props) throws BotException {
		try {
			//Create the client
			String urlStr = props.getProperty(PROP_WS_URL);
			if(urlStr != null) {
				client = new WikiPathwaysClient(new URL(urlStr));
			} else {
				client = new WikiPathwaysClient();
			}

			//Get the cache path
			String cacheStr = props.getProperty(PROP_CACHE);
			if(cacheStr == null) {
				throw new BotException("Property cache-path is missing");
			}
			cache = new WikiPathwaysCache(client, new File(cacheStr));

			//Login if possible
			user = props.getProperty(PROP_USER);
			pass = props.getProperty(PROP_PASS);
			if(user != null && pass != null) {
				client.login(user, pass);
			}
		} catch (Exception e) {
			if(e instanceof BotException) throw (BotException)e;
			else throw new BotException(e);
		}
	}

	public abstract BotReport createReport(Collection<Result> result);
	public abstract String getTagName();
	protected abstract Result scanPathway(File pathwayFile) throws BotException;

	public Collection<Result> scan() throws BotException {
		try {
			getCache().update(null);
		} catch(Exception e) {
			throw new BotException(e);
		}

		List<Result> reports = new ArrayList<Result>();

		for(File f : getCache().getFiles()) {
			reports.add(scanPathway(f));
		}
		return reports;
	}

	public Boolean updateTags() {
		return true;
	}
	
	public void applyCurationTags(Collection<Result> results, BotReport report) throws BotException {
		if(user == null || pass == null) {
			Logger.log.trace("No user account");
			return;
		}

		try {
			WSCurationTag[] allTags = client.getCurationTagsByName(getTagName());
			Map<String, WSCurationTag> tagsByPathway = new HashMap<String, WSCurationTag>();
			for(WSCurationTag t : allTags) {
				tagsByPathway.put(t.getPathway().getId(), t);
			}

			Set<WSPathwayInfo> overrides = new HashSet<WSPathwayInfo>();
			int i = 0;
			int size = results.size();
			for(Result r : results) {
				//Find an existing tag
				WSPathwayInfo pwi = r.getPathwayInfo();
				Logger.log.info("Processing pathway " + pwi.getId() + ", "+ i++ + " out of " + size);
				String pwId = pwi.getId();
				//First check if the existing tag is up-to-date
				WSCurationTag tag = tagsByPathway.get(pwId);

				if(r.shouldTag()) { //Add or update tag
					if(tag != null) { //We found an existing tag
						//See if it's up-to-date
						if(r.equalsTag(tag.getText())) {
							//No action needed
							Logger.log.info("Existing tag is up-to-date");
							continue;
						} else {
							//Update needed
							Logger.log.info("Applying updated tag to " + pwId);
							client.saveCurationTag(
									pwId,
									getTagName(), r.getTagText()
							);
						}
					} else { //No existing tag
						//See if it was removed by a user
						String oldtext = checkRemoved(r);
						if(oldtext != null) {
							//See if the measured values have changed after the
							//user removed the tag
							if(!r.equalsTag(oldtext)) {
								//Tag has changed, so re-apply
								Logger.log.info("Re-applying tag to " + pwId);
								client.saveCurationTag(
										pwId,
										getTagName(),r.getTagText()
								);
							} else {
								//Mark the pathway as overridden
								overrides.add(r.getPathwayInfo());
							}
						} else { //No override, add the tag
							Logger.log.info("Applying new tag to " + pwId);
							client.saveCurationTag(
									pwId,
									getTagName(),r.getTagText()
							);
						}
					}
				} else { //Remove tag if needed
					if(tag != null) {
						Logger.log.info("Removing tag from " + pwId);
						client.removeCurationTag(pwId, getTagName());
					}
				}
			}

			StringBuilder strb = new StringBuilder();
			for(WSPathwayInfo pwi : overrides) {
				strb.append("<a href='" + pwi.getUrl() + "'>" + pwi.getId() + "</a>, ");
			}
			if(strb.length() > 0) {
				report.setComment("User override tagging for pathways", strb.toString());
			}
		} catch(Exception e) {
			throw new BotException(e);
		}
	}

	private String checkRemoved(Result r) throws BotException {
		try {
			WSCurationTagHistory[] hist = client.getCurationTagHistory(r.getPathwayInfo().getId());

			//Find out if the tag was originally applied by this bot and then removed by a user
			for(WSCurationTagHistory h : hist) {
				if(user.equals(h.getUser())) {
					return null; //The current bot was the last one to touch the tag
				} else if("remove".equals(h.getAction())) {
					return h.getText(); //A user removed the tag
				}
			}
			return null;
		} catch(Exception e) {
			throw new BotException(e);
		}
	}

	protected WikiPathwaysClient getClient() {
		return client;
	}

	public WikiPathwaysCache getCache() {
		return cache;
	}

	abstract class Result {
		private WSPathwayInfo pathwayInfo;

		public Result(WSPathwayInfo pathwayInfo) {
			this.pathwayInfo = pathwayInfo;
		}

		public WSPathwayInfo getPathwayInfo() {
			return pathwayInfo;
		}

		/**
		 * Check if the variables in the results equal
		 * those in the tag text.
		 */
		public abstract boolean equalsTag(String tag);
		public abstract String getTagText();
		public abstract boolean shouldTag();
	}

	public static class BotException extends Exception {
		public BotException(Exception e) {
			super(e);
		}

		public BotException(String message) {
			super(message);
		}
	}

	public static void runAll(Bot bot, File htmlReport, File txtReport) throws BotException, IOException {
		Logger.log.trace("Running bot " + bot);
		Collection<Result> results = bot.scan();

		Logger.log.trace("Generating report");
		BotReport report = bot.createReport(results);

		if(bot.updateTags()) {
			Logger.log.trace("Applying curation tags");
			bot.applyCurationTags(results, report);
		}

		Logger.log.trace("Writing text report");
		report.writeTextReport(txtReport);

		Logger.log.trace("Writing HTML report");
		report.writeHtmlReport(htmlReport);
	}
}
