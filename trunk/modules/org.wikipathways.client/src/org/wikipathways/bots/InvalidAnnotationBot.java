// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2014 BiGCaT Bioinformatics
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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdb.GdbProvider;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

/**
 * Checks if a datanode is annotated with an Xref that is 
 * not supported by the identifier mapping databases
 * Replaces old Xref bot together with MissingAnnotationBot
 * @author mkutmon
 */
public class InvalidAnnotationBot extends Bot {
	private static final String CURATIONTAG = "Curation:MissingXRef";
	private static final String PROP_THRESHOLD = "threshold";
	private static final String PROP_GDBS = "gdb-config";

	GdbProvider gdbs;
	double threshold;

	public InvalidAnnotationBot(Properties props) throws BotException {
		super(props);
		String thr = props.getProperty(PROP_THRESHOLD);
		if(thr != null) threshold = Double.parseDouble(thr);

		File gdbFile = new File(props.getProperty(PROP_GDBS));
		try {
			gdbs = GdbProvider.fromConfigFile(gdbFile);
			BioDataSource.init();
		} catch (Exception e) {
			throw new BotException(e);
		}
	}

	public String getTagName() {
		return CURATIONTAG;
	}

	public BotReport createReport(Collection<Result> results) {
		BotReport report = new BotReport(
			new String[] {
				"Nr Xrefs", "Nr invalid", "% invalid", "Invalid Annotations"
			}
		);
		report.setTitle("InvalidAnnotationBot scan report");
		report.setDescription("The InvalidAnnotationBot checks for invalid DataNode annotations");
		for(Result r : results) {
			XRefResult xr = (XRefResult)r;
			report.setRow(
					r.getPathwayInfo(),
					new String[] {
						"" + xr.getNrXrefs(),
						"" + xr.getNrInvalid(),
						"" + (int)(xr.getPercentInvalid() * 100) / 100, //Round to two decimals
						"" + xr.getLabelsForInvalid()
					}
			);
		}
		return report;
	}

	protected Result scanPathway(File pathwayFile) throws BotException {
		try {
			XRefResult report = new XRefResult(getCache().getPathwayInfo(pathwayFile));
			Pathway pathway = new Pathway();
			pathway.readFromXml(pathwayFile, true);

			String orgName = pathway.getMappInfo().getOrganism();
			Organism org = Organism.fromLatinName(orgName);
			if(org == null) org = Organism.fromShortName(orgName);

			for(PathwayElement pwe : pathway.getDataObjects()) {
				if(pwe.getObjectType() == ObjectType.DATANODE) {
					boolean valid = true;
					Xref xref = pwe.getXref();
					IDMapperStack gdb = gdbs.getStack(org);
					try {
						if(xref.getId() != null && xref.getDataSource() != null) {
							if(!gdb.xrefExists(xref)) {
								valid = false;
							}
						}
					} catch (IDMapperException e) {
						Logger.log.error("Error checking xref exists", e);
					}
					report.addXref(pwe, valid);
				}
			}

			return report;
		} catch(Exception e) {
			throw new BotException(e);
		}
	}

	private class XRefResult extends Result {
		Map<PathwayElement, Boolean> xrefs = new HashMap<PathwayElement, Boolean>();

		public XRefResult(WSPathwayInfo pathwayInfo) {
			super(pathwayInfo);
		}

		public boolean shouldTag() {
			return getPercentValid() < threshold;
		}

		public boolean equalsTag(String tag) {
			return getTagText().equals(tag);
		}

		public void addXref(PathwayElement pwe, boolean valid) {
			xrefs.put(pwe, valid);
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
					labels.add(pwe.getTextLabel() + "[" + pwe.getXref() + "]");
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
				" DataNodes have an incorrect external reference: " +
				"<span title=\"" + labels[0] + "\">" + labels[1] + "</span>";
			return txt;
		}
	}

	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting InvalidAnnotationBot");
			Properties props = new Properties();
			props.load(new FileInputStream(new File(args[0])));
			InvalidAnnotationBot bot = new InvalidAnnotationBot(props);
			
			Logger.log.trace("Running bot " + bot);
			Collection<Result> results = bot.scan();

			Logger.log.trace("Generating report");
			BotReport report = bot.createReport(results);

			Logger.log.trace("Writing text report");
			report.writeTextReport(new File(args[1] + ".txt"));

			Logger.log.trace("Writing HTML report");
			report.writeHtmlReport(new File(args[1] + ".html"));
		} catch(Exception e) {
			e.printStackTrace();
			printUsage();
		}
	}

	static private void printUsage() {
		System.out.println(
			"Usage:\n" +
			"java org.pathvisio.wikipathways.bots.InvalidAnnotationBot propsfile reportfilename\n" +
			"Where:\n" +
			"-propsfile: a properties file containing the bot properties\n" +
			"-reportfilename: the base name of the file that will be used to write reports to " +
			"(extension will be added automatically)\n"
		);
	}
}
