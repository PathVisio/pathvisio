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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Xref;
import org.pathvisio.model.XrefWithSymbol;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.PathwayParser;
import org.pathvisio.util.PathwayParser.ParseException;
import org.pathvisio.wikipathways.WikiPathwaysCache;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * In the link checker for each pathway is is checked if the links in that pathway exist in the
 * database. An output file is created that shows a table. On the right column of that table the
 * filenames are shown and on the left column of the table the percentage is shown of the links
 * that exist in the database.
 */

public class LinkChecker
{

	public static void printUsage(String error)
	{
		System.out.println ("LinkChecker\n" +
				"	Checks references in pathways and prints a html table with the result\n"+
				"\n"+
				"Usage:\n"+
				"	java LinkChecker online <dbDir> <cacheDir> <outFile>\n" +
				"	java LinkChecker local <dbDir> <pwDir> <outFile>\n" +
				"\n" +
				"Where dbDir is a directory containing gene databases,\n"+
				"cachedir is a directory containing wikipahtways pathways,\n"+
				"and pwDir is the root of a directory tree containing pathways in GPML format.\n"+
				"\n" +
				"Error: " + error);
	}

	/**
	 * If connected to the internet, the boolean online has to be set to true.
	 */
	boolean isOnline = true; // set to true if connected to the internet

	/**
	 * Check if the String[] args is given, and make Files containing the directories to
	 * the pathways and databases
	 */
	File dbDir = null;
	File pwDir = null;
	PrintWriter out;

	/**
	 * sets online, out, dbDir and pwDir.
	 * Exits the program if there was an error.
	 */
	private void parseArgs (String[] args)
	{
		String errorMessage = null;

		int i = 0;
		if (args.length != 4)
		{
			errorMessage = "Expected 4 arguments";
		}
		if (errorMessage == null)
		{
			if (args[i].equals("local"))
			{
				isOnline = false;
			}
			else if (args[i].equals("online"))
			{
				isOnline = true;
			}
			else
			{
				errorMessage = "Expected 'local' or 'online'";
			}
			i++;
		}
		if (errorMessage == null)
		{
			dbDir = new File(args[i]);
			if (!(dbDir.exists() && dbDir.isDirectory()))
			{
				errorMessage = "Could not find database directory " + dbDir;
			}
			i++;
		}
		if (errorMessage == null)
		{
			pwDir = new File(args[i]);
			if (!(pwDir.exists() && pwDir.isDirectory()))
			{
				errorMessage = "Could not find pathway directory " + pwDir;
			}
			i++;
		}
		if (errorMessage == null)
		{
			// DO this for standard output:
			// out = new PrintWriter (System.out);
			try
			{
				out = new PrintWriter (new FileWriter (args[i]));
			}
			catch (IOException e)
			{
				errorMessage = "Couldn't open file for output: " + args[i];
			}
		}
		if (errorMessage != null)
		{
			printUsage(errorMessage);
			System.exit(1);
		}
	}

	private static class LinkCheckResult
	{
		File f = null;
		String name;
		String organism;
		int countTotal = 0;
		int countTrue = 0;
		boolean parseOk = false;
		boolean databaseOk = false;
	}

	LocalGdbManager localGdbManager = null;

	private List<LinkCheckResult> readPathways(List<File> pwyFiles)
	{
		List<LinkCheckResult> results = new ArrayList<LinkCheckResult>();
		XMLReader xmlReader = null;

		try
		{
			xmlReader = XMLReaderFactory.createXMLReader();
		}
		catch (SAXException e)
		{
			Logger.log.error ("Couldn't create XML reader");
			return results; // abort
		}

		for (File filename : pwyFiles)
		{
			LinkCheckResult result = new LinkCheckResult();
			result.f = filename;

			Logger.log.info ("Checking " + filename);
			SimpleGdb currentGdb = localGdbManager.getDatabaseForPathway(filename);
			/**
			 * First a list is made that contains the Xref's.
			 * In the if statement:
			 * If the database is found, add a row to the table of the html file, containing the
			 * name of the pathway and the percentage of found Xref's in the databse.
			 * In the else statement:
			 * Is the database is not found, add a row to the table of the html file, containing
			 * the name of the pathway and the text "Database not found".
			 */
			List<Xref> xrefList = new ArrayList<Xref>();
			try
			{
				PathwayParser pwy = new PathwayParser (filename, xmlReader);
				for (XrefWithSymbol sym : pwy.getGenes()) xrefList.add (sym.asXref());
				result.parseOk = true;
				result.name = pwy.getName();
				result.organism = filename.getName().substring (0, 2);
				result.countTotal = xrefList.size();
				if (currentGdb != null)
				{
					result.countTrue = countExistingXrefs (xrefList, currentGdb);
					result.databaseOk = true;
				}
				else
				{
					result.databaseOk = false;
				}
			}
			catch (ParseException e)
			{
				// ignore parse errors
				Logger.log.error ("Couldn't parse " + filename);
			}
			results.add (result);
		}
		return results;
	}

	private void report(List<LinkCheckResult> results)
	{
		Collections.sort (results, new Comparator<LinkCheckResult>()
		{
			public int compare(LinkCheckResult a, LinkCheckResult b)
			{
				if (!(a.parseOk && b.parseOk)) return a.parseOk ? 1 : (b.parseOk ? -1 : 0);

				int result = a.organism.compareTo(b.organism);
				if (result == 0)
				{
					int pctA = a.countTotal == 0 ? 0 : Math.round(100 * a.countTrue / a.countTotal);
					int pctB = b.countTotal == 0 ? 0 : Math.round(100 * b.countTrue / b.countTotal);

					result = pctA - pctB;
				}
				return result;
			}
		});

		String titleOfHTMLPage = "LinkChecker.java results";
		out.print("<HTML><HEAD><TITLE>"+titleOfHTMLPage+"</TITLE></HEAD><BODY><center><h1>"+titleOfHTMLPage+"</h1><TABLE border=\"1\"><TR><TD><B>Filename</B></TD><TD><B>Percentage found in Gdb</B></TD></B></TR>");

		for (LinkCheckResult result : results)
		{
			String percentage;

			if (result.countTotal != 0)
			{
				int percentageint = Math.round(100* result.countTrue / result.countTotal);
				percentage = percentageint + "% (" + result.countTrue + " of " + result.countTotal + ")";
			}
			else
			{
				percentage = ("<font color=\"red\"><b>total: 0</b></font>");
			}

			out.println (
				tr	(
						td (result.f.getName()) + "\n" +
						td (result.databaseOk ? percentage : "Database not found")
					)
				);

		}

		/**
		 * All pathway rows are added to the table. Now the HTML file has to be closed properly.
		 */
		out.print("</TABLE></center></BODY></HTML>");
		out.close();
		System.out.println ("Done writing html");
	}

	/**
	 * Get a list of files of pathways.
	 */
	private List<File> getPwyFiles()
	{
		String pwExtension = "gpml";
		List<File> pwyFiles;
		if (isOnline)
		{
			/**
			 * If the boolean online is true, first the data is loaded from the last changed
			 * pathway. With the date of this last change, the data of the other recently
			 * changed pathways can also be loaded.
			 */
			WikiPathwaysCache wp = new WikiPathwaysCache(pwDir);
			wp.update();
			pwyFiles = wp.getFiles();
		}
		else
		{
			pwyFiles = FileUtils.getFiles(pwDir, pwExtension, true);
		}
		return pwyFiles;
	}

	private void run()
	{
		List<File> pwyFiles = getPwyFiles();
		// initialize local Gdb manager
		localGdbManager = new LocalGdbManager(dbDir);
		List<LinkCheckResult> results = readPathways(pwyFiles);
		report (results);
	}

	/**
	* in the String[] args, 3 arguments are given:
	* in example:
	* "C:\\databases\\"
	* "C:\pathways"
	*
	* The first one is the directory that contains the databases.
	* The second one is the directory that contains the pathway cache.
	*/
	public static void main(String[] args)
	{
		LinkChecker linkChecker = new LinkChecker();
		linkChecker.parseArgs (args);
		linkChecker.run();
	}

	private int countExistingXrefs (List <Xref> xrefList, SimpleGdb database)
	{
		int countTrue = 0;

		for (Xref xref : xrefList)
		{
			try {
				if (database.xrefExists(xref) == true)
				{
					countTrue++;
				}
			} catch (DataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return countTrue;
	}

	/** wrap a td tag around msg */
	private String td (String msg)
	{
		return "<td>" + msg + "</td>";
	}

	/** wrap a tr tag around msg */
	private String tr (String msg)
	{
		return "<tr>" + msg + "</tr>";
	}

}
