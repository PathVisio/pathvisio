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
package org.pathvisio.util.swing;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.ProgressMonitor;

import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.core.Engine;
import org.pathvisio.core.data.XrefWithSymbol;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.core.util.PathwayParser;
import org.pathvisio.core.util.PathwayParser.ParseException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Utility functions for searching a directory tree of pathway files
 * for pathways that match certain criteria.
 */
public class SearchMethods
{
	public static final String MSG_NOT_IN_GDB = "Gene not found in selected gene database";
	public static final String MSG_NOTHING_FOUND = "Nothing found";
	public static final String MSG_CANCELLED = "cancelled";
	public static final String MSG_GDB_ERROR = "Gene database error";

	public static final double TOTAL_WORK = 1000.0;

	/**
	 * A helper class,
	 * let's one match a Pathway to certain search criteria
	 */
	private static abstract interface PathwayMatcher
	{
		/**
		 * searches file for a match
		 * returns a search result or null if the file doesn't match.
		 */
		MatchResult testMatch (File f);
	}

	/**
	 * Implementation of pathwayMatcher that matches
	 * if the pathway contains a specific xref,
	 * or crossrefs of that xref.
	 */
	public static class ByXrefMatcher implements PathwayMatcher
	{
		private Set<Xref> refs;

		public ByXrefMatcher(IDMapper gdb, Xref ref) throws SearchException
		{
			try
			{
				refs = gdb.mapID(ref);
			}
			catch (IDMapperException ex)
			{
				throw new SearchException (MSG_GDB_ERROR);
			}
			if(refs == null || refs.size() == 0) throw new SearchException(MSG_NOT_IN_GDB);
		}

		public MatchResult testMatch(File f)
		{
			//Get all genes in the pathway
			try
			{
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				PathwayParser parser = new PathwayParser(f, xmlReader);
				List<XrefWithSymbol> genes = parser.getGenes();
				//Check if one of the given ids is in the pathway
				for (XrefWithSymbol gene : genes)
				{
					//ignore symbol when comparing with refs from db.
					Xref geneWithoutSymbol =
						new Xref(gene.getId(), gene.getDataSource());
					if(refs.contains(geneWithoutSymbol))
					{
						//Gene found, add pathway to search result and break
						List<String> idsFound = new ArrayList<String>();
						List<XrefWithSymbol> matched = new ArrayList<XrefWithSymbol>();
						idsFound.add(gene.getId());
						matched.add(gene);
						return new MatchResult(f, idsFound, null, matched);
					}
				}
			}
			catch (ParseException e)
			{
				// ignore pathways that generate an exception.
				// They simply won't show up in search results.
			}
			catch (SAXException e)
			{
				// ignore pathways that generate an exception.
				// They simply won't show up in search results.
			}
			return null;
		}
	}

	/**
	 * Implementation of Pathway Matcher that matches the
	 * pathway only if any of the symbols of the datanodes
	 * matches a given regular expression.
	 */
	public static class ByPatternMatcher implements PathwayMatcher
	{
		private Pattern pattern;

		public ByPatternMatcher (String regex)
		{
			//Compile regex
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		}

		public MatchResult testMatch(File f)
		{
			try
			{
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				PathwayParser parser = new PathwayParser(f, xmlReader);
				List<XrefWithSymbol> genes = parser.getGenes();
				//Find what symbols match
				List<XrefWithSymbol> matched = new ArrayList<XrefWithSymbol>();
				List<String> idsFound = new ArrayList<String>();
				List<String> namesFound = new ArrayList<String>();

				for(XrefWithSymbol gene : genes)
				{
					Matcher m = pattern.matcher(gene.getSymbol());
					if(m.find())
					{
						matched.add(gene);
						idsFound.add(gene.getId());
						namesFound.add(gene.getSymbol());
					}
				}

				if(matched.size() > 0)
				{
					return new MatchResult (f, idsFound, namesFound, matched);
				}

			}
			catch (ParseException e)
			{
				// ignore pathways that generate an exception.
				// They simply won't show up in search results.
			}
			catch (SAXException e)
			{
				// ignore pathways that generate an exception.
				// They simply won't show up in search results.
			}
			return null;
		}
	}

	/**
	 * searchHelper: apply a certain Pathway Matcher to a directory
	 * full of pathways. Keep a progress monitor and check if it is cancelled by
	 * the user
	 */
	public static void searchHelper (final PathwayMatcher search, final File folder,
			final SearchTableModel srs, final JLabel lblNumFound, Component parent)
	{
		final int totalWork = 1000;

		final ProgressMonitor pmon = new ProgressMonitor(
				parent, "Pathway search", "searching pathways...",
				0, totalWork);

		SwingWorker<Integer, MatchResult> worker = new SwingWorker<Integer, MatchResult>()
		{
			@Override
			protected Integer doInBackground()
			{
				//get all pathway files in the folder and subfolders
				List<File> pathways = FileUtils.getFiles(folder, Engine.PATHWAY_FILE_EXTENSION, true);

				pmon.setProgress((int)(totalWork * 0.2));

				int i = 0;
				int matchCount = 0;

				for(File f : pathways)
				{
					if(pmon.isCanceled())
					{
						pmon.close();
						return matchCount;
					}
					MatchResult sr = search.testMatch (f);
					if (sr != null)
					{
						publish (sr);
						matchCount++;
					}

					i++;
					pmon.setProgress((int)(totalWork * 0.2 + totalWork * 0.8 * i / pathways.size()));
				}
				pmon.close (); // just to be sure
				return matchCount;
			}

			protected void process (List<MatchResult> matches)
			{
				for (MatchResult mr : matches)
				{
					srs.addRow(mr);
				}
			}

			@Override
			protected void done()
			{
				try
				{
					int matchCount = get();
					lblNumFound.setText(matchCount + " " + (matchCount == 1 ? "result" : "results") + " found");
				}
				catch (Exception e)
				{
					Logger.log.warn("Exception when getting match count", e);
				}
			}

		};
		worker.execute();
	}

	/**
	 * Base class for exceptions during search
	 */
	public static class SearchException extends Exception
	{

		SearchException(String msg)
		{
			super(msg);
		}
	}

}
