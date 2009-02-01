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
package org.pathvisio.plugins.statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pathvisio.data.DataException;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.ReporterData;
import org.pathvisio.data.SimpleGex;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Xref;
import org.pathvisio.model.XrefWithSymbol;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.PathwayParser;
import org.pathvisio.util.PathwayParser.ParseException;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.Stats;
import org.pathvisio.visualization.colorset.Criterion;
import org.pathvisio.visualization.colorset.Criterion.CriterionException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Calculates statistics on a set of Pathways, either step by step with intermediate results,
 * or all at once.  
 */
public class ZScoreCalculator 
{			
	private final StatisticsResult result;
	private final ProgressKeeper pk;
	private Iterator<File> iterator = null;
	private int i = 0;
	private List<File> files;
	
	public ZScoreCalculator(Criterion crit, File pwDir, SimpleGex gex, Gdb gdb, ProgressKeeper pk)
	{
		if (pk != null)
		{
			pk.setProgress (0);
			pk.setTaskName("Analyzing data");
		}
		
		result = new StatisticsResult();
		result.crit = crit;
		result.stm = new StatisticsTableModel();
		result.stm.setColumns(new Column[] {Column.PATHWAY_NAME, Column.R, Column.N, Column.TOTAL, Column.PCT, Column.ZSCORE});
		result.pwDir = pwDir;
		result.gex = gex;
		result.gdb = gdb;
		this.pk = pk;

		try
		{
			xmlReader = XMLReaderFactory.createXMLReader();
		}
		catch (SAXException e)
		{
			Logger.log.error("Problem while searching pathways", e);
			throw new IllegalStateException(); // TODO: more info in exception
		}
	}

	public Iterator<File> getIterator()
	{
		if (iterator == null)
		{
			// first we calculate N and R
			doCalculateTotals();
			if (pk != null && pk.isCancelled()) return null;
			
			if (pk != null) pk.setTaskName("Creating pathway list");
			
			// now we calculate n and r for each pwy				
			files = FileUtils.getFiles(result.pwDir, "gpml", true);
			iterator = files.iterator();								
		}
		return iterator;
	}
	
	public boolean hasNext()
	{
		return getIterator().hasNext();
	}
	
	public StatisticsPathwayResult next()
	{
		File f = getIterator().next();
		i++;
		if (pk != null)
		{
			pk.setProgress((int)((0.2 + (0.8 * (double)i / (double)files.size())) * 100.0));
		}

		StatisticsPathwayResult spr = doCalculatePathway(f);
		result.stm.addRow (spr);
		result.stm.sort();
		
		return spr;
	}
	
	/**
	 * call only after hsaNext() returns false
	 */
	public StatisticsResult getResult()
	{
		assert (iterator != null && !iterator.hasNext());
		return result;
	}

	/** calculate everything at once */
	public StatisticsResult calculate()
	{
		int i = 0;
		for (Iterator<File> it = getIterator(); it.hasNext(); )
		{
			File f = getIterator().next();
			if (pk != null)
			{
				pk.setProgress((int)((0.2 + (0.8 * (double)i / (double)files.size())) * 100.0));
			}
			StatisticsPathwayResult spr = doCalculatePathway(f);
			result.stm.addRow (spr);
		}
		result.stm.sort();
		return result;
	}
	/**
	 * calculate bigN and bigR
	 */
	private void doCalculateTotals()
	{
		try
		{
			int maxRow = result.gex.getMaxRow();
			for (int i = 0; i < maxRow; ++i)
			{
				if (pk != null && pk.isCancelled()) return;
				try
				{
					ReporterData d = result.gex.getRow(i);
					result.bigN++;
					boolean eval = result.crit.evaluate(d.getByName());
					if (eval)
					{
						result.bigR++;
					}		
//							Logger.log.trace ("Row " + i +  " (" + d.getXref() + ") = " + result);
				}
				catch (CriterionException e)
				{
					Logger.log.error ("Problem during row handling ", e);
				}
				
				if (pk != null) pk.setProgress ((int)(0.2 * (double)i / (double)maxRow * 100.0));
			}
		}
		catch (DataException e)
		{
			Logger.log.error ("Problem during calculation of R/N ", e);
			//TODO: better error handling
		}

		Logger.log.info ("N: " + result.bigN + ", R: " + result.bigR);
	}
	
	private StatisticsPathwayResult doCalculatePathway(File file)
	{
		try
		{
			if (pk != null) pk.setTaskName("Analyzing " + file.getName());
			
			PathwayParser pwyParser = new PathwayParser(file, xmlReader);
			
			Logger.log.info ("Calculating statistics for " + pwyParser.getName());
			
			List <Xref> srcRefs = new ArrayList<Xref>();
			for (XrefWithSymbol x : pwyParser.getGenes()) srcRefs.add (x.asXref());
			
			try
			{
				result.gex.cacheData(srcRefs, new ProgressKeeper(1000), result.gdb);
			}
			catch (DataException e)
			{
				Logger.log.error ("Exception while caching data", e);
			}

			int cPwyTotal = srcRefs.size();
			int cPwyMeasured = 0;
			
			double cPwyPositive = 0;
			
			for (Xref srcRef : srcRefs)
			{
				if (pk != null && pk.isCancelled()) return null;
				
				List<ReporterData> rows = result.gex.getCachedData().getData(srcRef);
				
				if (rows != null)
				{
					int cGeneTotal = rows.size();
					if (cGeneTotal > 0) { cPwyMeasured++; }
					int cGenePositive = 0;
					
					for (ReporterData row : rows)
					{
						if (pk != null && pk.isCancelled()) return null;
						try
						{	
							boolean eval = result.crit.evaluate(row.getByName());
							if (eval) cGenePositive++;
						}
						catch (CriterionException e)
						{
							Logger.log.error ("Unknown error during statistics", e);
						}
					}
				
					// Map the rows back to the corresponding genes. 
					// "yes" is counted, weighed by the # of rows per gene. 
					// This is our "r".
					
					//This line is different from MAPPFinder: if 2 out of 3 probes are positive, count only 2/3
					cPwyPositive += (double)cGenePositive / (double)cGeneTotal;
					
					//The line below is the original MAPPFinder behaviour: 
					//  count as fully positive if at least one probe is positive
					//if (cGenePositive > 0) cPwyPositive += 1;
				}
			}
			
			double z = Stats.zscore (cPwyMeasured, cPwyPositive, result.bigN, result.bigR);						
			
			StatisticsPathwayResult sr = new StatisticsPathwayResult (file, pwyParser.getName(), cPwyMeasured, (int)Math.round (cPwyPositive), cPwyTotal, z);
			return sr;
		}
		catch (ParseException pe)
		{
			Logger.log.warn ("Could not parse " + file + ", ignoring", pe);
			return null;
		}
	}
	
	private XMLReader xmlReader = null;
	
}