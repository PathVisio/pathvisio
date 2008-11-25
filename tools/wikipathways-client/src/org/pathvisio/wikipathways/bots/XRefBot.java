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
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.pathvisio.data.DataException;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.GdbProvider;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;
import org.pathvisio.wikipathways.WikiPathwaysCache;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

/**
 * Checks for DataNode XRef annotations and adds a curation tag for
 * pathways that contain more wrongly annotated DataNodes than a given threshold.
 */
public class XRefBot {
	private static final String CURATIONTAG = "Curation:MissingXRef";
	GdbProvider gdbs;
	WikiPathwaysCache cache;
	WikiPathwaysClient client;
	
	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting XRefBot");
			XRefBot bot = new XRefBot(new File(args[1]), new File(args[0]));
			Collection<XRefReport> results = bot.scan();
			
			Logger.log.trace("Generating report");
			BotReport report = bot.createReport(results);
			
			File txtFile = new File(args[2] + ".txt");
			Logger.log.trace("Writing text report");
			report.writeTextReport(txtFile);
			
			Logger.log.trace("Writing HTML report");
			File htmlFile = new File(args[2] + ".html");
			report.writeHtmlReport(htmlFile);
			
			if(args.length > 3) {
				if(args.length == 6) {
					bot.client.login(args[4], args[5]);
				}
				double threshold = Double.parseDouble(args[3]);
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
			"java org.pathvisio.wikipathways.bots.XRefBot cacheDir gdbConfig reportFile [threshold user pass]\n" +
			"Where:\n" +
			"-cacheDir: the directory that will be used to cache downloaded pathways\n" +
			"-gdbConfig: a configuration file that lists the available synonym databases\n" +
			"-reportFile: the base name of the files to save the reports to (will be appended" +
					" with correct extension automatically).\n" +
			"-threshold is an optional threshold percentage used to add curation tags. " +
			"-user is the username of the WikiPathways account that will be used for tagging " +
			"-pass is the password for the WikiPathways account" +
			"If the percentage of valid xrefs is below this threshold, a curation tag will " +
			"be added to the pathway. If no threshold is specified, no pathways will be tagged."
		);
	}
	
	public XRefBot(File gdbConfig, File cacheDir, WikiPathwaysClient client) throws DataException, IOException, ServiceException {
		gdbs = GdbProvider.fromConfigFile(gdbConfig);
		this.client = client;
		if(client == null) {
			this.client = new WikiPathwaysClient();
		}
		cache = new WikiPathwaysCache(this.client, cacheDir);
	}
	
	public XRefBot(File gdbConfig, File cacheDir) throws DataException, IOException, ServiceException {
		this(gdbConfig, cacheDir, null);
	}
	
	public BotReport createReport(Collection<XRefReport> results) {
		BotReport report = new BotReport(
			new String[] {
				"Nr Xrefs", "Nr valid", "% valid", "Invalid DataNodes"	
			}
		);
		report.setTitle("XRefBot scan report");
		report.setDescription("The XRefBot checks for valid DataNode annotations");
		for(XRefReport r : results) {
			report.setRow(
					r.getPathwayInfo(),
					new String[] {
						"" + r.getNrXrefs(),
						"" + r.getNrValid(),
						"" + (int)(r.getPercentValid() * 100) / 100, //Round to two decimals
						"" + r.getLabelsForInvalid()
					}
			);
		}
		return report;
	}
	
	public void applyCurationTags(Collection<XRefReport> results, double threshold) throws RemoteException {
		Logger.log.info("Tagging pathways");
		for(XRefReport r : results) {
			//Find an existing tag
			WSPathwayInfo pwi = r.getPathwayInfo();
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
	
	public Collection<XRefReport> scan() throws ConverterException, FileNotFoundException, IOException {
		cache.update();
		
		List<XRefReport> reports = new ArrayList<XRefReport>();
		
		for(File f : cache.getFiles()) {
			reports.add(scanPathway(f));
		}
		return reports;
	}
	
	private XRefReport scanPathway(File pathwayFile) throws ConverterException, FileNotFoundException, IOException {
		XRefReport report = new XRefReport(cache.getPathwayInfo(pathwayFile));
		Pathway pathway = new Pathway();
		pathway.readFromXml(pathwayFile, true);
		
		String orgName = pathway.getMappInfo().getOrganism();
		Organism org = Organism.fromLatinName(orgName);
		if(org == null) org = Organism.fromShortName(orgName);

		for(PathwayElement pwe : pathway.getDataObjects()) {
			if(pwe.getObjectType() == ObjectType.DATANODE) {
				boolean exists = false;
				Xref xref = pwe.getXref();
				for(Gdb gdb : gdbs.getGdbs(org)) {
					if(gdb.xrefExists(xref)) {
						exists = true;
						break;
					}
				}
				report.addXref(pwe, exists);
			}
		}
		
		return report;
	}
	
	private class XRefReport {
		WSPathwayInfo pathwayInfo;
		
		Map<PathwayElement, Boolean> xrefs = new HashMap<PathwayElement, Boolean>();
		
		public XRefReport(WSPathwayInfo pathwayInfo) {
			this.pathwayInfo = pathwayInfo;
		}
		
		public void addXref(PathwayElement pwe, boolean valid) {
			xrefs.put(pwe, valid);
		}
		
		public WSPathwayInfo getPathwayInfo() {
			return pathwayInfo;
		}

		public int getNrXrefs() {
			return xrefs.size();
		}
		
		public double getPercentValid() {
			return (double)(100 * getNrValid()) / getNrXrefs();
		}
		
		public double getPercentInvalid() {
			return (double)(100 * getNrInvalid()) / getNrXrefs();
		}
		
		public int getNrInvalid() {
			return getNrXrefs() - getNrValid();
		}
		
		public int getNrValid() {
			int v = 0;
			for(PathwayElement pwe : xrefs.keySet()) {
				if(xrefs.get(pwe)) {
					v++;
				}
			}
			return v;
		}
		
		public List<String> getLabelsForInvalid() {
			List<String> labels = new ArrayList<String>();
			for(PathwayElement pwe : xrefs.keySet()) {
				if(!xrefs.get(pwe)) {
					labels.add(pwe.getTextLabel());
				}
			}
			return labels;
		}
		
		private String[] getLabelStrings() {
			List<String> labels = getLabelsForInvalid();
			Collections.sort(labels);
			String labelString = "";
			String labelStringTrun = "";
			for(int i = 0; i < labels.size(); i++) {
				labelString += labels.get(i) + ", ";
				if(i < 3) {
					labelStringTrun += labels.get(i) + ", ";
				} else if(i == 3) {
					labelStringTrun += " ..., ";
				}
			}
			if(labelString.length() > 2) {
				labelString = labelString.substring(0, labelString.length() - 2);
			}
			if(labelStringTrun.length() > 2) {
				labelStringTrun = labelStringTrun.substring(0, labelStringTrun.length() - 2);
			}
			return new String[] { labelString, labelStringTrun };
		}
		
		public String getTagText() {
			String[] labels = getLabelStrings();
			
			//Limit length of label string
			if(labels[0].length() > 300) {
				labels[0] = labels[0].substring(0, 300) + "...";
			}
			String txt = getNrInvalid() + " out of " + getNrXrefs() +
				" DataNodes have an incorrect or missing external reference: " +
				"<span title=\"" + labels[0] + "\">" + labels[1] + "</span>";
			return txt;
		}
	}
}
