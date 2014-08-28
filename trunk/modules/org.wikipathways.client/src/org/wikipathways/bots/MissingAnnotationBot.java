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

import org.bridgedb.Xref;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

/**
 * Only check if a datanode has an empty id or datasource attribute
 * and adds a curation tag for pathways that contain not annotated
 * dataNodes.
 * @author mkutmon
 */
public class MissingAnnotationBot extends Bot {
	private static final String CURATIONTAG = "Curation:MissingXRef";
	private static final String PROP_THRESHOLD = "threshold";

	double threshold;

	public MissingAnnotationBot(Properties props) throws BotException {
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
				"Nr Xrefs", "Nr valid", "% valid", "Invalid DataNodes"
			}
		);
		report.setTitle("XRefBot scan report");
		report.setDescription("The XRefBot checks for valid DataNode annotations");

		for(Result r : results) {
			XRefResult xr = (XRefResult)r;
			report.setRow(
				xr.getPathwayInfo(),
				new String[] {
					"" + xr.getNrXrefs(),
					"" + xr.getNrValid(),
					"" + (int)(xr.getPercentValid() * 100) / 100, 
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
			
			for(PathwayElement pwe : pathway.getDataObjects()) {
				if(pwe.getObjectType() == ObjectType.DATANODE) {
					boolean annotated = false;
					Xref xref = pwe.getXref();
					if(xref.getId() != null && !xref.getId().equals("") && xref.getDataSource() != null) {
						annotated = true;
					}
					report.addXref(pwe, annotated);
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

		/**
		 * adapted function because of webservice issues when
		 * using tags longer than 300 letters
		 */
		public String getTagText() {
			String[] labels = getLabelStrings();
			
			String labelTitle = labels[0];
			labelTitle = labelTitle.replace("\n", " ");
			labelTitle = labelTitle.replace("/", " ");
			String labelText = labels[1];
			labelText = labelText.replace("\n", " ");
			labelText = labelText.replace("/", " ");
			
			String front = getNrInvalid() + " out of " + getNrXrefs() +
					" DataNodes have a missing external reference: " +
					"<span title=\"";
			
			String middle = "\">";
			String end = "</span>";
			
			int max = 300 - (front.length() + middle.length() + end.length() + labelText.length());
			if(labelTitle.length() > max) {
				labelTitle = labelTitle.substring(0, max-4) + ",...";
			}
			
			String txt = front + labelTitle + middle + labelText + end;
			return txt;
		}
	}

	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting MissingAnnotationBot");
			Properties props = new Properties();
			props.load(new FileInputStream(new File(args[0])));
			MissingAnnotationBot bot = new MissingAnnotationBot(props);
			Bot.runAll(bot, new File(args[1] + ".html"), new File(args[1] + ".txt"));
		} catch(Exception e) {
			e.printStackTrace();
			printUsage();
		}
	}

	static private void printUsage() {
		System.out.println(
			"Usage:\n" +
			"java org.pathvisio.wikipathways.bots.MissingAnnotationBot propsfile reportfilename\n" +
			"Where:\n" +
			"-propsfile: a properties file containing the bot properties\n" +
			"-reportfilename: the base name of the file that will be used to write reports to " +
			"(extension will be added automatically)\n"
		);
	}
}
