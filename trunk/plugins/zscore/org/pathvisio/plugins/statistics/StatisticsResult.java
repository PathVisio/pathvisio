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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bridgedb.IDMapper;
import org.bridgedb.rdb.IDMapperRdb;
import org.pathvisio.gex.SimpleGex;
import org.pathvisio.visualization.colorset.Criterion;

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
	SimpleGex gex;
	IDMapper gdb;
	
	public void save (File f) throws IOException
	{
		PrintStream out = new PrintStream (new FileOutputStream(f));
		
		out.println ("Statistics results for " + new Date());
		out.println ("Dataset: " + gex.getDbName());
		out.println ("Pathway directory: " + pwDir);
		out.println ("Gene database: " + gdb);
		out.println ("Criterion: " + crit.getExpression());
		out.println ("Rows in data (N): " + bigN);
		out.println ("Rows meeting criterion (R): " + bigR);
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
}