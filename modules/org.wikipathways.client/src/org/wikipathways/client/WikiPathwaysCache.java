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
package org.wikipathways.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

/**
 * This class can be used to maintain a local cache of all WikiPathways pathways,
 * that can be automatically updated throught the WikiPathways client.
 */
public class WikiPathwaysCache
{
	static final String PW_EXT = "gpml";
	static final String PW_EXT_DOT = "." + PW_EXT;
	static final String INFO_EXT = "info";

	private File cacheDirectory;
	private WikiPathwaysClient wpClient;
	private List<File> files;

	public WikiPathwaysCache(File cacheDirectory) throws ServiceException {
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
	 * @deprecated use update(null) instead
	 */
	public Collection<File> update() throws ConverterException, IOException
	{
		return update (null);
	}

	/**
	 * Check for missing / outdated pathways
	 * and download them.
	 * Does nothing if there was no way to download.
	 * @param keeper: an optional ProgressKeeper, may be null
	 * @return A list of files that were updated (either modified, added or deleted)
	 * 	the returned list can be partial if the task was cancelled
	 * @throws IOException
	 * @throws ConverterException
	 */
	public Collection<File> update(ProgressKeeper keeper) throws ConverterException, IOException
	{
		if (keeper != null) keeper.setTaskName("Checking last modified date");

		Set<File> changedFiles = new HashSet<File>();

		long localdate = dateLastModified (files);
		Date d = new Date(localdate);
		DateFormat df = DateFormat.getDateTimeInstance();
		Logger.log.info("Date last modified: " + df.format(d));

		Logger.log.info("---[Updating new and removed pathways]---");
		if (keeper != null)
		{
			keeper.setTaskName("Fetching pathway list");
			if (keeper.isCancelled()) return changedFiles;
		}

		List<WSPathwayInfo> pathways = Arrays.asList(wpClient.listPathways());

		changedFiles.addAll(purgeRemoved(pathways));
		changedFiles.addAll(downloadNew(pathways, keeper));

		if (keeper != null)
		{
			keeper.setTaskName("Fetching recently changed pathways");
			if (keeper.isCancelled()) return changedFiles;
		}

		Logger.log.info("---[Get Recently Changed pathways]---");

		changedFiles.addAll(processRecentChanges(d, keeper));

		Logger.log.info("---[Ready]---");
		Logger.log.info("Updated pathways: " + changedFiles);

		// update list of files in cache.
		if (keeper != null)
		{
			keeper.setTaskName("Updating local pathway list");
			if (keeper.isCancelled()) return changedFiles;
		}

		files = FileUtils.getFiles(cacheDirectory, "gpml", true);

		return changedFiles;
	}

	private List<File> downloadNew(Collection<WSPathwayInfo> pathways, ProgressKeeper keeper) throws ConverterException, IOException {
		Set<WSPathwayInfo> newPathways = new HashSet<WSPathwayInfo>();

		int i = 0;
		for(WSPathwayInfo p : pathways)
		{
			File f = pathwayToFile(p);
			if(!f.exists()) {
				newPathways.add(p);
			}

		}
		return downloadFiles(newPathways, keeper);
	}

	/**
	 * In this method it is possible to download only the pathways that are recently changed.
	 * @return a list of files in the cache that has been updated (partial if interrupted)
	 * @throws ConverterException
	 * @throws IOException
	 */
	private List<File> processRecentChanges (Date d, ProgressKeeper keeper) throws ConverterException, IOException
	{
		// given path: path to store the pathway cache
		// and date: the date of the most recent changed
		// get the pathwaylist; all the known pathways are
		// stored in a list
		WSPathwayInfo[] changes = wpClient.getRecentChanges(d);
		//Filter out deleted pathways
		Set<WSPathwayInfo> changeAndExist = new HashSet<WSPathwayInfo>();
		for(WSPathwayInfo p : changes) {
			//Cached file was not removed by purgeRemoved method
			if(pathwayToFile(p).exists()) {
				changeAndExist.add(p);
			}
		}
		return downloadFiles(changeAndExist, keeper);
	}

	/**
	 * Download the latest version of all given pathways to the cache directory
	 * @return The list of downloaded files (partial list if interrupted)
	 * @throws ConverterException
	 * @throws IOException
	 */
	private List<File> downloadFiles (Collection<WSPathwayInfo> pathways, ProgressKeeper keeper) throws ConverterException, IOException {
		List<File> files = new ArrayList<File>();

		int i = 1;
		for(WSPathwayInfo pwi : pathways) {
			if (keeper != null) keeper.report("Downloading " + pwi.getName());

			File file = pathwayToFile(pwi);
			WSPathway wsp = wpClient.getPathway(pwi.getId());
			Pathway p = WikiPathwaysClient.toPathway(wsp);
			p.writeToXml(file, true);
			// also write a file that stores some pathway info
			writeInfoFile(pwi);
			files.add(file);
			Logger.log.info("Downloaded file "+(i++)+" of "+pathways.size()+ ": " +
					pwi.getName() + "(" + pwi.getSpecies() + ")");
			if (keeper != null)
			{
				keeper.worked (50 / pathways.size());
				if (keeper.isCancelled()) return files;
			}
		}
		return files;
	}

	private File pathwayToFile(WSPathwayInfo pathway) {
		String species = pathway.getSpecies();

		// construct the download path
		String pathToDownload = cacheDirectory + File.separator + species + File.separator;

		//	make a folder for a species when it doesn't exist
		new File(pathToDownload).mkdirs();

		// download the pathway and give status in console
		File pwFile = new File (pathToDownload + pathway.getId() + PW_EXT_DOT);

		return pwFile;
	}

	private void writeInfoFile(WSPathwayInfo pathway) throws IOException {
		Properties prop = new Properties();
		prop.setProperty("Name", pathway.getName());
		prop.setProperty("Species", pathway.getSpecies());
		prop.setProperty("Url", pathway.getUrl());
		prop.setProperty("Revision", pathway.getRevision());
		prop.setProperty("Id", pathway.getId());
		prop.save(new FileOutputStream(getInfoFile(pathwayToFile(pathway))), "");
	}

	private File getInfoFile(File pathwayFile) {
		return new File(pathwayFile.getAbsolutePath() + "." + INFO_EXT);
	}

	//Assume that files are in the form: http://host/index.php/Pathway:{Organism}:{PathwayName}
	private String fileToPathwayName(File f) {
		String filename = f.getName(); // gpml file
		String pwyName = filename.substring(0, filename.length() - PW_EXT_DOT.length()); // remove the extension and the first 3 characters i.e. ACE-Inhibitor_pathway_PharmGKB
		//Parse the pathway name
		int slash = pwyName.lastIndexOf('/');
		pwyName = pwyName.substring(slash);
		return pwyName;
	}

	/**
	 * Get the source url (that points to the pathway
	 * on WikiPathways) for the given cache file.
	 */
	public String cacheFileToUrl(File f) throws FileNotFoundException, IOException {
		return getPathwayInfo(f).getUrl();
	}

	public WSPathwayInfo getPathwayInfo(File cacheFile) throws FileNotFoundException, IOException {
		File info = getInfoFile(cacheFile);
		Properties prop = new Properties();
		FileInputStream in = new FileInputStream(info);
		prop.load(in);
		in.close();
		WSPathwayInfo pi = new WSPathwayInfo(
				prop.getProperty("Id"),
				prop.getProperty("Url"),
				prop.getProperty("Name"),
				prop.getProperty("Species"),
				prop.getProperty("Revision")
		);
		return pi;
	}

	private List<File> purgeRemoved(Collection<WSPathwayInfo> pathways) {
		Set<File> remoteFiles = new HashSet<File>();
		for(WSPathwayInfo p : pathways) remoteFiles.add(pathwayToFile(p));

		List<File> cacheFiles = FileUtils.getFiles(cacheDirectory, PW_EXT, true);
		List<File> deleted = new ArrayList<File>();

		for (File file : cacheFiles) {
			if(!remoteFiles.contains(file)) {
				deleted.add(file);
				file.delete();
				//Delete info file on exit, so classes that use the cache
				//can still use the info in this session.
				getInfoFile(file).deleteOnExit();
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
		for (File pathway : pathways)
		{
			// If pathway is more recent, use this date.
			if (lastModified < pathway.lastModified())
			{
				lastModified = pathway.lastModified();
			}
		}
		return lastModified;
	}
}
