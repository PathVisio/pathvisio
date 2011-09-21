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
package org.pathvisio.plugins.project2008;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.pathvisio.gpmldiff.GpmlDiff;
import org.pathvisio.gpmldiff.PatchMain;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Organism;
import org.pathvisio.wikipathways.WikiPathwaysClient;

/**
 * One time script to take a set of pathways that were linked up,
 * Create patches,
 * filter out irrelevant changes,
 * and submit them to wikipatways
 */
public class IntegrateSjoerdsWork
{
	static final File SJOERD_DIR = new File("/home/martijn/Desktop/sjoerd");
	static final File CACHE_DIR = new File ("/home/martijn/wikipathways");

	public static void main (String[] args) throws IOException, JDOMException, ConverterException, InterruptedException
	{

		WikiPathwaysClient wpClient = new WikiPathwaysClient();
		wpClient.login (args[0], args[1]);

		// go through all sjoerds file
		for (File sjoerdPwy : SJOERD_DIR.listFiles())
		{
			// skip non-pathways
			if (!sjoerdPwy.getName().endsWith(".gpml")) { continue; }

			// transform to Species:Name_with_underscores form
			// remove part before _
			String wpName = sjoerdPwy.getName();
			Organism org = Organism.fromCode(wpName.substring (0, wpName.indexOf("_")));
			String orgName = org.latinName().replace(' ', '_');
			wpName = orgName + ":" + wpName.substring(wpName.indexOf("_") + 1);
			wpName = wpName.substring(0, wpName.lastIndexOf ("."));

			System.out.println ("Fetching " + wpName);
			File matchPwy = File.createTempFile("wikipathways", ".gpml");

			int baseRevision = wpClient.downloadPathway(wpName, matchPwy);

			// match them with a pathway
			System.out.println ("Comparing " + sjoerdPwy + " with " + matchPwy);

			// create a patch
			File tmp = File.createTempFile("patch", ".dgpml");
			System.out.println ("Writing patch " + tmp);

			GpmlDiff.makePatch(matchPwy, sjoerdPwy, tmp);

			// filter the patch (using external script)
			Process p = Runtime.getRuntime().exec("tools/project2008/filterpatch.pl " + tmp);
			int exitVal = p.waitFor();
			System.out.println ("filterpatch returned " + exitVal);

			// apply the patch
			File pwyOut = File.createTempFile("result", ".gpml");
			PatchMain.applyPatch(matchPwy, tmp, pwyOut);
			System.out.println ("patched file written to " + pwyOut);

			// upload the pathway
			wpClient.uploadPathway(wpName, pwyOut, baseRevision, "Sticky edges patch by Sjoerd");
		}
	}
}
