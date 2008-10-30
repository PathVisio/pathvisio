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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.pathvisio.data.DataException;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.wikipathways.WikiPathwaysCache;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

/**
 * Checks for unconnected lines
 * @author thomas
 */
public class ConnectorBot {
	private static final String CURATIONTAG = "Curation:NoInteractions";
	WikiPathwaysCache cache;
	WikiPathwaysClient client;
	
	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting ConnectorBot");
			ConnectorBot bot = new ConnectorBot(new File(args[0]));
			Collection<ConnectorBotReport> results = bot.scan();
			
			Logger.log.trace("Generating HTML report");
			String htmlReport = bot.createHtmlReport(results);
			
			Logger.log.trace("Writing HTML report");
			File htmlFile =  new File(args[1]);
			FileWriter out = new FileWriter(htmlFile);
			out.append(htmlReport);
			out.close();

			if(args.length == 5) {
				bot.client.login(args[3], args[4]);
				double threshold = Double.parseDouble(args[2]);
				bot.applyCurationTags(results, threshold);
			}

		} catch(Exception e) {
			e.printStackTrace();
			printUsage();
		}
	}
	
	static private void printUsage() {
		System.out.println(
			"Usage:\n" +
			"java org.pathvisio.wikipathways.bots.ConnectorBot cacheDir reportFile [threshold user pass]\n" +
			"Where:\n" +
			"-cacheDir: the directory that will be used to cache downloaded pathways\n" +
			"-reportFile: the file to save the HTML report to\n" +
			"-threshold is an optional threshold percentage used to add curation tags. " +
			"-user is the username of the WikiPathways account that will be used for tagging " +
			"-pass is the password for the WikiPathways account" +
			"If the percentage of connected lines is below the threshold, a curation tag will " +
			"be added to the pathway. If no threshold is specified, no pathways will be tagged."
		);
	}
	
	public ConnectorBot(File cacheDir, WikiPathwaysClient client) throws DataException, IOException, ServiceException {
		this.client = client;
		if(client == null) {
			this.client = new WikiPathwaysClient();
		}
		cache = new WikiPathwaysCache(this.client, cacheDir);
	}
	
	public ConnectorBot(File cacheDir) throws DataException, IOException, ServiceException {
		this(cacheDir, null);
	}
	
	public String createHtmlReport(Collection<ConnectorBotReport> results) {
		String html = "<html><head><script src=\"sorttable.js\"></script>";
		html += " <link rel=\"stylesheet\" type=\"text/css\" href=\"botresult.css\">";
		html += "</head><body>";
		
		DateFormat df = DateFormat.getDateInstance();
		html += "<h1>ConnectorBot scan report (" + df.format(new Date()) + ")</h1>";
		html += "<table class=\"sortable botresult\"><tbody>";
		html += "<th>Pathway<th>Organism<th>Nr lines<th>Nr connected<th>% connected";
		for(ConnectorBotReport r : results) {
			html += r.tableRow();
		}
		
		html += "</tbody></table></body></html>";
		return html;
	}
	
	public void applyCurationTags(Collection<ConnectorBotReport> results, double threshold) throws RemoteException {
		Logger.log.info("Tagging pathways");
		for(ConnectorBotReport r : results) {
			//Find an existing tag
			String pwId = r.getPathwayInfo().getId();
			//First check if the existing tag is up-to-date
			WSCurationTag[] tags = client.getCurationTags(pwId);
			String currTagText = null;
			for(WSCurationTag t : tags) {
				if(CURATIONTAG.equals(t.getName())) {
					currTagText = t.getText();
					break;
				}
			}
			
			//Apply a tag if the % of valid xrefs is smaller than the threshold
			if(r.getPercentValid() < threshold) {
				String newTagText = r.getTagText();
				if(!newTagText.equals(currTagText)) {
					Logger.log.info("Applying tag to " + pwId);
					client.saveCurationTag(
							pwId,
							CURATIONTAG, newTagText
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
	
	public Collection<ConnectorBotReport> scan() throws ConverterException, FileNotFoundException, IOException {
		cache.update();
		
		List<ConnectorBotReport> reports = new ArrayList<ConnectorBotReport>();
		
		for(File f : cache.getFiles()) {
			reports.add(scanPathway(f));
		}
		return reports;
	}
	
	private ConnectorBotReport scanPathway(File pathwayFile) throws ConverterException, FileNotFoundException, IOException {
		ConnectorBotReport report = new ConnectorBotReport(cache.getPathwayInfo(pathwayFile));
		Pathway pathway = new Pathway();
		pathway.readFromXml(pathwayFile, true);
		
		String orgName = pathway.getMappInfo().getOrganism();
		Organism org = Organism.fromLatinName(orgName);
		if(org == null) org = Organism.fromShortName(orgName);

		for(PathwayElement pwe : pathway.getDataObjects()) {
			if(pwe.getObjectType() == ObjectType.LINE) {
				boolean valid = 
					pwe.getMStart().isLinked() &&
					pwe.getMEnd().isLinked();
				
				report.addLine(pwe, valid);
			}
		}
		
		return report;
	}
	
	private class ConnectorBotReport {
		WSPathwayInfo pathwayInfo;
		
		Map<PathwayElement, Boolean> lines = new HashMap<PathwayElement, Boolean>();
		
		public ConnectorBotReport(WSPathwayInfo pathwayInfo) {
			this.pathwayInfo = pathwayInfo;
		}
		
		public void addLine(PathwayElement pwe, boolean valid) {
			lines.put(pwe, valid);
		}
		
		public WSPathwayInfo getPathwayInfo() {
			return pathwayInfo;
		}

		public int getNrLines() {
			return lines.size();
		}
		
		public double getPercentValid() {
			return (double)(100 * getNrValid()) / getNrLines();
		}
		
		public double getPercentInvalid() {
			return (double)(100 * getNrInvalid()) / getNrLines();
		}
		
		public int getNrInvalid() {
			return getNrLines() - getNrValid();
		}
		
		public int getNrValid() {
			int v = 0;
			for(PathwayElement pwe : lines.keySet()) {
				if(lines.get(pwe)) {
					v++;
				}
			}
			return v;
		}
		
		public String getTagText() {
			String txt = getNrInvalid() + " out of " + getNrLines() +
				" lines are not properly connected.";
			return txt;
		}
		
		/**
		 * <tr>
		 * <td><a href="pwurl">pwName</a>
		 * <td>pwSpecies
		 * <td>nrXrefs
		 * <td>nrValid
		 * <td>%valid
		 */
		public String tableRow() {
			String html = "<tr>";
			html += "<td><a href=\"" + pathwayInfo.getUrl() + "\">" +
				pathwayInfo.getName() + "</a></td>";
			html += "<td>" + pathwayInfo.getSpecies() + "</td>";
			html += "<td>" + getNrLines() + "</td>";
			html += "<td>" + getNrValid() + "</td>";
			html += "<td>" + (int)(getPercentValid() * 100) / 100 + "</td>";
			return html;
		}
	}
}
