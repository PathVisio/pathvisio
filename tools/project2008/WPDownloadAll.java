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
// import the things needed to run this java file.
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;

import org.apache.xmlrpc.XmlRpcException;


public class WPDownloadAll
{
	/**
	 * download all the pathways from a server, using WikiPathWaysClient
	 * 
	 * 
	 * in the String[] args, 1 argument is given:
	 * in example:
	 * "C:\\WPClient"
	 * 
	 * This is the directory of the cache
	 * 	
	 * Good Luck!
	 */
	public static void main(String[] args)  throws XmlRpcException, IOException{
		downloadAll(args[0]);
	}
	
	public static void downloadNew(String path, Date d) throws XmlRpcException, IOException
	{
		// given path: path to store the pathway cache
		// and date: the date of the most recent changed 
		
		
		
		// make a new WikiPathwaysClient
		WikiPathwaysClient wp = new WikiPathwaysClient();
		
		// get the pathwaylist; all the known pathways are
		// stored in a list
		List<String> pathwayNames = wp.getRecentChanges(d);
		
		downloadFiles(pathwayNames,path,wp);

	}
	
	
	
	public static void downloadAll(String path) throws XmlRpcException, IOException
	{
		// given path: path to store the pathway cache
		
				// make a new WikiPathwaysClient
		WikiPathwaysClient wp = new WikiPathwaysClient();
		
		// get the pathwaylist; all the known pathways are
		// stored in a list
		List<String> pathwayNames = wp.getPathwayList();
		
		downloadFiles(pathwayNames,path,wp);


	}
	
	public static void downloadFiles(List<String> pathwayNames, String path, WikiPathwaysClient wp) throws XmlRpcException, IOException {
		// download all the files
		
		// give the extension of a pathway file
		String pwExtension = ".gpml";
		
		// remove all duplicates
		pathwayNames = removeDuplicates(pwExtension, path, pathwayNames);
		
		
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
				new File (pathToDownload + code + "_" + namePathway + pwExtension));
			System.out.println("Downloaded file "+(i+1)+" of "+pathwayNames.size()+ ": " + pathwayNames.get(i));
		}
	}
	
	
	
	public static List<String> removeDuplicates(String pwExtension, String path, List<String> pathwayNames){
		// get a list of files from the cache directory. All the files
		// that are already in the cache, are removed from the pathwayNames
		// list, so they won't be downloaded again.

		// get a list of all files inside the cache path
		File pwDir = new File(path + "\\");
		List<File> pwFilenames = FileUtils.getFileListing(pwDir, pwExtension);
		
		// for each files, reconstruct the pathname file (Species:Pathwayname), and
		// remove from the pathwaynames list
		for (File file: pwFilenames){
			String fullPath = file.getPath(); // i.e. C:\PWCache\Homo_sapiens\Hs_ACE-Inhibitor_pathway_PharmGKB.gpml 
			String neededPartOfFilename = fullPath.substring(path.length() + 1); // i.e. Homo_sapiens\Hs_ACE-Inhibitor_pathway_PharmGKB.gpml
			String[] temporary = neededPartOfFilename.split("\\\\"); // split at the slash; so i.e. temporary[0]: Homo_sapiens; temporary[1]: Hs_ACE-Inhibitor_pathway_PharmGKB.gpml 
			String species = temporary[0]; // i.e. species = Homo_sapiens
			String pathwayName = temporary[1].substring(3, temporary[1].length() - pwExtension.length()); // remove the extension and the first 3 characters i.e. ACE-Inhibitor_pathway_PharmGKB
			String pwayname = species+":"+pathwayName; // construct the pathway name: i.e. Homo_sapiens:ACE-Inhibitor_pathway_PharmGKB
			pathwayNames.remove(pwayname); // remove from pathwayNames
		}
		
		return pathwayNames;
		
	}
}
