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
package org.pathvisio.wikipathways;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.pathvisio.wikipathways.WikiPathwaysClient.WikiPathwaysException;

/**
 * In this class the class 'WikiPathwaysClient' can be tested.
 */
public class WPClientTest
{
	public static void main(String[] args) throws IOException, WikiPathwaysException
	{
		// make a new WikiPathwaysClient
		WikiPathwaysClient wp = new WikiPathwaysClient(
				new URL("http://137.120.89.38/wikipathways-test/wpi/wpi_rpc.php")
		);

		// create a cutoff date.
		// NB: any valid  Date() can be used for getRecentChanges,
		// you don't have to use GregorianCalendar
		Date cutoff = new GregorianCalendar (2008, Calendar.MARCH, 31).getTime();

		List<String> recentPathways = wp.getRecentChanges(cutoff);
		for (String pwy: recentPathways)
		{
			System.out.println (pwy);
		}

		// get the pathwaylist; all the known pathways are
		// stored in a list
		List<String> pathwayNames = wp.getPathwayList();


		// path to store the pathway cache
		String path = args[0];

		// a for loop that downloads all individual pathways
		for (int i = 0; i < pathwayNames.size(); ++i)
		{

			// get the species and pathwayname
			String pathwayName= pathwayNames.get(i);
			String[] temporary = pathwayName.split(":");
			String species = temporary[0];
			String namePathway = temporary[1];

			// construct the download path
			String pathToDownload = path + "\\" + species + "\\";

			//	make a folder for a species when it doesn't exist
			new File(pathToDownload).mkdir();

			// make a 2 letters species code
			temporary = species.split("_");
			String code = temporary[0].substring(0,1) + temporary[1].substring(0,1);


			// download the pathway and give status in console
				wp.downloadPathway(pathwayNames.get(i),
					new File (pathToDownload + code + "_" + namePathway + ".gpml"));
			System.out.println("Downloaded: " + pathwayNames.get(i));
		}
	}
}
