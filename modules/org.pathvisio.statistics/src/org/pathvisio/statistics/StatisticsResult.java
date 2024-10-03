/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bridgedb.IDMapper;
import org.pathvisio.desktop.gex.CachedData;
import org.pathvisio.desktop.visualization.Criterion;

/**
 * Result of a statistics calculation on a whole Pathway set.
 * Can be saved to disk
 */
public class StatisticsResult
{
	StatisticsTableModel stm;
	int bigN = 0;
	int bigR = 0;
	Criterion crit;
	File pwDir;
	CachedData gex;
	IDMapper gdb;
	String methodDesc;

	public void save (File f) throws IOException
	{
		PrintStream out = new PrintStream (new FileOutputStream(f));

		out.println ("Statistics results for " + new Date());
		out.println ("Dataset: " + gex.getDbName());
		out.println ("Pathway directory: " + pwDir);
		out.println ("Gene database: " + gdb);
		out.println ("Criterion: " + crit.getExpression());
		out.println (methodDesc);
		out.println ("Total data points (N): " + bigN);
		out.println ("Data points meeting criterion (R): " + bigR);
		out.println();

		stm.printData(out);
	}

	/**
	 * Get a list containing the statistics results per pathway.
	 */
	public List<StatisticsPathwayResult> getPathwayResults() {
		List<StatisticsPathwayResult> results =
			new ArrayList<StatisticsPathwayResult>(stm.getRowCount());
		for(int i = 0; i < stm.getRowCount(); i++) {
			results.add(stm.getRow(i));
		}
		return results;
	}

	public int getBigN() {
		return bigN;
	}

	public int getBigR() {
		return bigR;
	}

	public Criterion getCriterion() {
		return crit;
	}

	public IDMapper getIDMapper() {
		return gdb;
	}

	public CachedData getGex() {
		return gex;
	}

	public File getPathwayDir() {
		return pwDir;
	}
}
