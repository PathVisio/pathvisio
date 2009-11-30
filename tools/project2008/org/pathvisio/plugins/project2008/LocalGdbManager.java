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
package org.pathvisio.plugins.project2008;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.data.SimpleGdbFactory;
import org.pathvisio.model.Organism;
import org.pathvisio.util.FileUtils;

/**
 * Given a directory containing pgdb files,
 * LocalGdbManager can figure out which one is needed for which species.
 */
public class LocalGdbManager
{
	private File dbDir;

	/**
	 * Create a manager for a certain directory
	 * The constructor will scan the directory, recursively, for pgdb files.
	 */
	public LocalGdbManager (File dbDir)
	{
		if (!(dbDir.exists() && dbDir.isDirectory()))
		{
			throw new IllegalArgumentException ("Invalid database directory " + dbDir);
		}
		this.dbDir = dbDir;

		init();
	}

	//TODO: use global constant
	private static final String DB_EXTENSION = ".pgdb";

	private List<SimpleGdb> databases          = new ArrayList<SimpleGdb>();
	private List<String>    databasesFilenames = new ArrayList<String>();

	private void init()
	{
		/**
		 * Get a new list of files of pathways.
		 */
		List<File> dbFilenames = FileUtils.getFiles(dbDir, DB_EXTENSION, true);

		/**
		 * In the following for-loop, all databases are loaded in in List<SimpleGdb> databases
		 * and all filenames of the loaded databases are loaded in List<String>
		 * databaseFilenames.
		 */
		for (File dbFilename: dbFilenames)
		{
			// load a database and add it to the list
			try
			{
				SimpleGdb database = SimpleGdbFactory.createInstance(dbFilename.getPath(), new DataDerby(), 0);
				databases.add(database);
			}
			catch (DataException e)
			{
				System.out.println ("WARNING: Couldn't open database " + dbFilename);
			}
			// extract a filename and add it to the list
			databasesFilenames.add(dbFilename.getName());
		}

	}

	/**
	 * Guess the gene database that matches a pathway file
	 * and return it.
	 */
	public SimpleGdb getDatabaseForPathway(File filename)
	{
		/**
		 * The right database for the pathway must be found. The filename of a database
		 * must have te same two starting letters as the filenames of the pathway.
		 */
		int i = 0;
		int index = -1;
		for (String databaseFilename : databasesFilenames)
		{
			if (databaseFilename.substring(0,2).equalsIgnoreCase(filename.getName().substring(0,2))){
				index = i;
			}
			i++;
		}
		if (index == -1)
		{
			return null;
		}
		return databases.get(index);
	}

	/**
	 * Guess the gene database that matches an Organism Object and return it
	 */
	public SimpleGdb getDatabaseForOrganism (Organism organism)
	{
		int i = 0;
		int index = -1;
		for (String databaseFilename : databasesFilenames)
		{
			if (databaseFilename.substring(0,2).equalsIgnoreCase(organism.code()))
			{
				index = i;
			}
			i++;
		}
		if (index == -1)
		{
			return null;
		}
		return databases.get(index);
	}

}
