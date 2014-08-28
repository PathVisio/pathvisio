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
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

/**
 * Checks for unconnected lines
 * @author thomas
 */
public class ConnectorBot extends Bot {
	static final String CURATIONTAG = "Curation:NoInteractions";
	static final String PROP_THRESHOLD = "threshold";

	double threshold;

	public ConnectorBot(Properties props) throws BotException {
		super(props);
		String thr = props.getProperty(PROP_THRESHOLD);
		if(thr != null) threshold = Double.parseDouble(thr);
	}

	public String getTagName() {
		return CURATIONTAG;
	}

	public BotReport createReport(Collection<Result> results) {
		BotReport report = new BotReport(
			new String[] {
				"Nr lines", "Nr connected", "% connected"
			}
		);
		report.setTitle("ConnectorBot scan report");
		report.setDescription("The ConnectorBot checks for properly connected lines");
		for(Result r : results) {
			ConnectorBotResult cr = (ConnectorBotResult)r;
			report.setRow(
					r.getPathwayInfo(),
					new String[] {
						"" + cr.getNrLines(),
						"" + cr.getNrValid(),
						"" + (int)(cr.getPercentValid() * 100) / 100 //Round to two decimals
					}
			);
		}
		return report;
	}

	protected Result scanPathway(File pathwayFile) throws BotException {
		try {
			ConnectorBotResult report = new ConnectorBotResult(
					getCache().getPathwayInfo(pathwayFile));
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
		} catch(Exception e) {
			throw new BotException(e);
		}
	}

	private class ConnectorBotResult extends Result {
		WSPathwayInfo pathwayInfo;

		Map<PathwayElement, Boolean> lines = new HashMap<PathwayElement, Boolean>();

		public ConnectorBotResult(WSPathwayInfo pathwayInfo) {
			super(pathwayInfo);
			this.pathwayInfo = pathwayInfo;
		}

		public boolean equalsTag(String tag) {
			return getTagText().equals(tag);
		}

		public boolean shouldTag() {
			return getPercentValid() < threshold;
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

//		public double getPercentInvalid() {
//			return (double)(100 * getNrInvalid()) / getNrLines();
//		}

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
	}

	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting ConnectorBot");
			Properties props = new Properties();
			props.load(new FileInputStream(new File(args[0])));
			ConnectorBot bot = new ConnectorBot(props);
			Bot.runAll(bot, new File(args[1] + ".html"), new File(args[1] + ".txt"));
		} catch(Exception e) {
			e.printStackTrace();
			printUsage();
		}
	}

	static private void printUsage() {
		System.out.println(
			"Usage:\n" +
			"java org.pathvisio.wikipathways.bots.ConnectorBot propsfile reportfilename\n" +
			"Where:\n" +
			"-propsfile: a properties file containing the bot properties\n" +
			"-reportfilename: the base name of the file that will be used to write reports to " +
			"(extension will be added automatically)\n"
		);
	}
}
