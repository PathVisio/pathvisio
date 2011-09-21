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
package org.pathvisio.wikipathways;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pathvisio.debug.Logger;
import org.pathvisio.util.FileUtils;
import org.pathvisio.wikipathways.WikiPathwaysClient.WikiPathwaysException;

/**
 * In this class all the pathways from a server are downloaded, using WikiPathWaysClient.
 * It is also possible to download the pathways that are recently changed.
 */
public class WikiPathwaysCache
{
	static final String PW_EXT = "gpml";
	static final String PW_EXT_DOT = "." + PW_EXT;

	private File cacheDirectory;
	private WikiPathwaysClient wpClient;
	private List<File> files;

	public WikiPathwaysCache(File cacheDirectory) {
		this(new WikiPathwaysClient(), cacheDirectory);
	}

	public WikiPathwaysCache(WikiPathwaysClient wpClient, File cacheDirectory)
	{
		this.wpClient = wpClient;

		if (!(cacheDirectory.exists() && cacheDirectory.isDirectory()))
		{
			throw new IllegalArgumentException ("Illegal cache directory " + cacheDirectory);
		}
		this.cacheDirectory = cacheDirectory;
		files = FileUtils.getFiles(cacheDirectory, PW_EXT, true);
	}

	public List<File> getFiles()
	{
		return files;
	}

	/**
	 * Check for missing / outdated pathways
	 * and download them.
	 * Does nothing if there was no way to download.
	 * @return A list of files that were updated (either modified, added or deleted)
	 */
	public List<File> update()
	{
		Set<File> changedFiles = new HashSet<File>();

		long localdate = dateLastModified (files);
		Date d = new Date(localdate);
		DateFormat df = DateFormat.getDateTimeInstance();
		Logger.log.info("Date last modified: " + df.format(d));

		try
		{
			Logger.log.info("---[Get Recently Changed pathways]---");

			changedFiles.addAll(processRecentChanges(d));

			Logger.log.info("---[Updating new and removed pathways]---");

			List<String> pathwayNames = wpClient.getPathwayList();
			changedFiles.addAll(purgeRemoved(pathwayNames));
			changedFiles.addAll(downloadNew(pathwayNames));

			Logger.log.info("---[Ready]---");
			Logger.log.info("Updated pathways: " + changedFiles);

			// update list of files in cache.
			files = FileUtils.getFiles(cacheDirectory, "gpml", true);
		}
		catch (IOException e)
		{
			Logger.log.error("Couldn't update cache", e);
		}
		catch (WikiPathwaysException e)
		{
			Logger.log.error("Couldn't update cache", e);
		}

		return new ArrayList<File>(changedFiles);
	}

	private List<File> downloadNew(List<String> pathwayNames) throws IOException, WikiPathwaysException {
		List<String> newFiles = new ArrayList<String>();

		for(String pwName : pathwayNames) {
			File f = pathwayNameToFile(pwName);
			if(!f.exists()) {
				newFiles.add(pwName);
			}
		}

		return downloadFiles(newFiles);
	}

	/**
	 * In this method it is possible to download only the pathways that are recently changed.
	 * @return a list of files in the cache that has been updated
	 */
	private List<File> processRecentChanges (Date d) throws IOException, WikiPathwaysException
	{
		// given path: path to store the pathway cache
		// and date: the date of the most recent changed
		// get the pathwaylist; all the known pathways are
		// stored in a list
		List<String> pathwayNames = wpClient.getRecentChanges(d);
		return downloadFiles (pathwayNames);
	}

	/**
	 * In this method the files are downloaded.
	 * @return The list of downloaded files
	 */
	private List<File> downloadFiles (List<String> pathwayNames) throws IOException, WikiPathwaysException {
		List<File> files = new ArrayList<File>();

		// a for loop that downloads all individual pathways
		for (int i = 0; i < pathwayNames.size(); ++i)
		{
			// download the pathway and give status in console
			File pwFile = pathwayNameToFile(pathwayNames.get(i));
			wpClient.downloadPathway(pathwayNames.get(i), pwFile);
			files.add(pwFile);
			Logger.log.info("Downloaded file "+(i+1)+" of "+pathwayNames.size()+ ": " + pathwayNames.get(i));
		}
		return files;
	}

	private File pathwayNameToFile(String pathwayName) {
		String[] temporary = pathwayName.split(":");
		String species = temporary[0];
		String namePathway = temporary[1];

		// construct the download path
		String pathToDownload = cacheDirectory + File.separator + species + File.separator;

		//	make a folder for a species when it doesn't exist
		new File(pathToDownload).mkdirs();

		// make a 2 letters species code
		//TODO: ???
		temporary = species.split("_");
		String code = temporary[0].substring(0,1) + temporary[1].substring(0,1);


		// download the pathway and give status in console
		File pwFile = new File (pathToDownload + code + "_" + namePathway + PW_EXT_DOT);
		return pwFile;
	}

	private String fileToPathwayName(File f) {
		String filename = f.getName(); // gpml file
		String pathwayName = filename.substring(3, filename.length() - PW_EXT_DOT.length()); // remove the extension and the first 3 characters i.e. ACE-Inhibitor_pathway_PharmGKB
		String species = f.getParentFile().getName(); // i.e. species = Homo_sapiens
		String pwyName = species+":"+pathwayName; // construct the pathway name: i.e. Homo_sapiens:ACE-Inhibitor_pathway_PharmGKB
		return pwyName;
	}

	/**
	 * Converts the path of a cached file to the url
	 * on the wiki it is downloaded from.
	 * Note that this method assumes that the rpc file is
	 * wpi/wpi_rpc.php. E.g., if the rpc url is:
	 * <code>http://myhost.org/path/wpi/wpi_rpc.php</code>, the
	 * pathway url will be <code>http://myhost.org/path/index.php/Pathway:Species:Title</code>
	 */
	public String cacheFileToUrl(File f) {
		URL rpcUrl = wpClient.getRpUrl();
		String name = fileToPathwayName(f);
		String path = rpcUrl.getPath();
		path = path.substring(0, path.lastIndexOf("/wpi/wpi_rpc.php"));
		String base = "http://" + rpcUrl.getHost() + path;
		return base + "/index.php/Pathway:" + name;
	}

	private List<File> purgeRemoved(List<String> pathwayNames) {
		List<File> pwFilenames = FileUtils.getFiles(cacheDirectory, PW_EXT, true);
		List<File> deleted = new ArrayList<File>();

		for (File file : pwFilenames) {
			String name = fileToPathwayName(file);
			if(!pathwayNames.contains(name)) {
				deleted.add(file);
				file.delete();
			}
		}

		return deleted;
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
