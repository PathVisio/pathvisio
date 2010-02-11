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
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Properties;

import org.pathvisio.biopax.reflect.BiopaxElement;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

/**
 * Checks for pathways that have no references to literature.
 * @author thomas
 *
 */
public class LiteratureBot extends Bot {
	private static final String CURATIONTAG = "Curation:NeedsReference";
	
	public LiteratureBot(Properties props) throws BotException {
		super(props);
	}
	
	@Override
	public String getTagName() {
		return CURATIONTAG;
	}
	
	@Override
	protected Result scanPathway(File pathwayFile) throws BotException {
		try {
			Pathway p = new Pathway();
			p.readFromXml(pathwayFile, true);

			boolean hasRef = false;
			for(BiopaxElement bpe : p.getBiopaxElementManager().getElements()) {
				if(bpe instanceof PublicationXRef) {
					hasRef = true;
					break;
				}
			}
			
			WSPathwayInfo wsp = getCache().getPathwayInfo(pathwayFile);
			return new LiteratureResult(wsp, hasRef);
		} catch(Exception e) {
			throw new BotException(e);
		}
	}
	
	@Override
	public BotReport createReport(Collection<Result> result) {
		BotReport report = new BotReport(
				new String[] { "Has literature reference" }
		);
		report.setTitle("LiteratureBot scan report");
		report.setDescription("Checks pathways for the presence of literature references.");

		int nrMissing = 0;
		for(Result r : result) {
			LiteratureResult dr = (LiteratureResult)r;
			if(!dr.hasReference) nrMissing++;
			report.setRow(dr.getPathwayInfo(), new String[] { dr.hasReference + "" });
		}
		report.setComment("Number of pathways", "" + result.size());
		report.setComment("Number of pathways without literature reference", "" + nrMissing);
		return report;
	}
	
	class LiteratureResult extends Result {
		boolean hasReference;

		public LiteratureResult(WSPathwayInfo pathwayInfo, boolean hasReference) {
			super(pathwayInfo);
			this.hasReference = hasReference;
		}

		public boolean equalsTag(String tag) {
			return getTagText().equals(tag);
		}

		public String getTagText() {
			return "";
		}

		public boolean shouldTag() {
			return !hasReference;
		}
	}
	
	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting LiteratureBot");
			Properties props = new Properties();
			props.load(new FileInputStream(new File(args[0])));
			LiteratureBot bot = new LiteratureBot(props);
			Bot.runAll(bot, new File(args[1] + ".html"), new File(args[1] + ".txt"));
		} catch(Exception e) {
			e.printStackTrace();
			printUsage();
		}
	}
	
	static private void printUsage() {
		System.out.println(
			"Usage:\n" +
			"java org.pathvisio.wikipathways.bots.LiteratureBot propsfile reportfilename\n" +
			"Where:\n" +
			"-propsfile: a properties file containing the bot properties\n" +
			"-reportfilename: the base name of the file that will be used to write reports to " +
			"(extension will be added automatically)\n"
		);
	}
}
