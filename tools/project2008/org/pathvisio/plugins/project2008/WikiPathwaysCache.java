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
package org.pathvisio.plugins.project2008;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Date;

import org.apache.xmlrpc.XmlRpcException;

/**
 * In this class all the pathways from a server are downloaded, using WikiPathWaysClient.
 * It is also possible to download the pathways that are recently changed. 
 */
public class WikiPathwaysCache
{
	private File cacheDirectory;
	private WikiPathwaysClient wpClient = new WikiPathwaysClient();
	private List<File> files;
	
	public WikiPathwaysCache(File cacheDirectory) 
	{
		if (!(cacheDirectory.exists() && cacheDirectory.isDirectory()))
		{
			throw new IllegalArgumentException ("Illegal cache directory " + cacheDirectory);
		}
		this.cacheDirectory = cacheDirectory;
		files = FileUtils.getFileListing(cacheDirectory, ".gpml");
	}
	
	public List<File> getFiles()
	{
		return files;
	}

	/**
	 * Check for missing / outdated pathways
	 * and download them.
	 * Does nothing if there was no way to download.
	 */
	public void update()
	{
		long localdate = dateLastModified (files);
		Date d = new Date(localdate); 
		DateFormat df = DateFormat.getDateTimeInstance();
		System.out.println("Date last modified: "+df.format(d)); 

		try
		{
			System.out.println("---[Get Recently Changed Files]---");
			
			downloadNew(d);
		
			System.out.println("---[Get All Other Files]---");
			// download all pathways to the pathway folder
			downloadAll();
			System.out.println("---[Ready]---");
			// update list of files in cache.
			files = FileUtils.getFileListing(cacheDirectory, ".gpml");
			System.out.println("---[Start Checking Links]---");
		}
		catch (XmlRpcException e)
		{
			System.out.println("ERROR: Couldn't update cache");
		}
		catch (IOException e)
		{
			System.out.println("ERROR: Couldn't update cache");
		}
	}
	
	/**
	 * In this method it is possible to download only the pathways that are recently changed. 
	 */
	private void downloadNew (Date d) throws XmlRpcException, IOException
	{
		// given path: path to store the pathway cache
		// and date: the date of the most recent changed 
		
		
		// get the pathwaylist; all the known pathways are
		// stored in a list
		List<String> pathwayNames = wpClient.getRecentChanges(d);
		
		downloadFiles (pathwayNames);
	}
	
	/**
	 * In this method a list is created with pathwayNames that have to be downloaded. These 
	 * pathways are then being downloaded in the method 'downloadFiles'
	 */
	private void downloadAll() throws XmlRpcException, IOException
	{
		// given path: path to store the pathway cache
				
		// get the pathwaylist; all the known pathways are
		// stored in a list
		List<String> pathwayNames = wpClient.getPathwayList();
		
		downloadFiles (pathwayNames);
	}
	
	/**
	 * In this method the files are downloaded. 
	 */
	private void downloadFiles (List<String> pathwayNames) throws XmlRpcException, IOException {
		
		// give the extension of a pathway file
		String pwExtension = ".gpml";
		
		// remove all duplicates
		pathwayNames = removeDuplicates(pwExtension, pathwayNames);
				
		// a for loop that downloads all individual pathways
		for (int i = 0; i < pathwayNames.size(); ++i)
		{
			// get the species and pathwayname
			String pathwayName= pathwayNames.get(i);
			String[] temporary = pathwayName.split(":");
			String species = temporary[0];
			String namePathway = temporary[1];
			
			// construct the download path
			String pathToDownload = cacheDirectory + File.separator + species + File.separator;
			
			//	make a folder for a species when it doesn't exist
			new File(pathToDownload).mkdir();
			
			// make a 2 letters species code
			//TODO: ???
			temporary = species.split("_");
			String code = temporary[0].substring(0,1) + temporary[1].substring(0,1);
			
			
			// download the pathway and give status in console
			wpClient.downloadPathway(pathwayNames.get(i), 
				new File (pathToDownload + code + "_" + namePathway + pwExtension));
			System.out.println("Downloaded file "+(i+1)+" of "+pathwayNames.size()+ ": " + pathwayNames.get(i));
		}
	}
	
	/**
	 * In this method, a list is obtained of files from the cache directory. All the files that 
	 * are already in the cache, are removed from the pathwayNames list, so they won't be 
	 * downloaded again.
	 */
	private List<String> removeDuplicates (String pwExtension, List<String> pathwayNames)
	{
		// get a list of all files inside the cache path
		List<File> pwFilenames = FileUtils.getFileListing(cacheDirectory, pwExtension);
		
		// for each files, reconstruct the pathname file (Species:Pathwayname), and
		// remove from the pathwaynames list
		for (File file: pwFilenames)
		{
			String filename = file.getName(); // gpml file 
			String pathwayName = filename.substring(3, filename.length() - pwExtension.length()); // remove the extension and the first 3 characters i.e. ACE-Inhibitor_pathway_PharmGKB
			String species = file.getParentFile().getName(); // i.e. species = Homo_sapiens
			String pwayname = species+":"+pathwayName; // construct the pathway name: i.e. Homo_sapiens:ACE-Inhibitor_pathway_PharmGKB
			pathwayNames.remove(pwayname); // remove from pathwayNames
		}
		
		return pathwayNames;
		
	}

	/**
	 * In this method the date is returned when the last change is made in a pathway. The
	 * property that has to be given is:
	 * 'pathways' (a list of pathways you want to have the most recent date from). 
	 */
	private static long dateLastModified(List<File> pathways)
	{
		// Set initial value.
		long lastModified = 0;
		// Walk through all the pathways.
		for (File pathway:pathways)
		{
			// If pathway is more recent, use this date.
			if (lastModified < pathway.lastModified()){
				lastModified = pathway.lastModified();
			}
		}
		return lastModified;
	}
}
