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

import org.pathvisio.model.*;
import org.pathvisio.data.*;
import java.util.*;
import java.io.*;
import org.pathvisio.debug.StopWatch;

class TutorialFiles
{
	public static void main (String[] argv)
	{
    	StopWatch timer = new StopWatch();
    	System.out.println ("Timer started");
    	timer.start();
		
		final String tutorialPwy = "example-data/Hs_Apoptosis.gpml";
		Pathway pwy = new Pathway();
		try
		{
			pwy.readFromXml (new File (tutorialPwy), true);
		}
		catch (ConverterException e)
		{
			e.printStackTrace();
			return;
		}
		
		List<Xref> refs = pwy.getDataNodeXrefs();

		assert (refs.contains (new Xref ("8717", DataSource.ENTREZ_GENE)));
		assert (refs.contains (new Xref ("7132", DataSource.ENTREZ_GENE)));
		assert (!refs.contains (new Xref ("1111", DataSource.ENTREZ_GENE)));
		assert (refs.size() == 94);

		// now look up all cross references in the human Gdb.

		Gdb sourceGdb;
		try
		{
			//String dbName = "C:\\Documents and Settings\\martijn\\PathVisio-Data\\gene databases\\Hs_41_36c.pgdb";
			String dbName = "/home/martijn/PathVisio-Data/gene databases/Hs_41_36c.pgdb";
			sourceGdb = new SimpleGdb (dbName,	new DataDerby(), DBConnector.PROP_NONE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		try
		{
			DataDerby connector = new DataDerby();
			int error = 0;
			SimpleGdb targetGdb = new SimpleGdb("tutorial", connector, DBConnector.PROP_RECREATE);
			targetGdb.createGdbTables();
			targetGdb.preInsert();
			for (Xref i : refs)
			{
				List<Xref> newRefs = sourceGdb.getCrossRefs(i, DataSource.ENSEMBL);
				
				for (Xref j : newRefs)
				{
					String bpText = sourceGdb.getBpInfo(j);
					error += targetGdb.addGene(j, bpText);
					error += targetGdb.addLink(i.getId(), j);
					System.out.println(i + "\t" + j);
				}
			}
    		targetGdb.commit();
    		targetGdb.createGdbIndices();
    		targetGdb.compact();
    		System.out.println ("total errors (duplicates): " + error);
    		System.out.println ("total ids in gene table: " + targetGdb.getGeneCount());
    		targetGdb.close();
    		System.out.println ("Timer stopped: " + timer.stop());
    		connector.finalizeNewDatabase(targetGdb.getDbName());
		}
		catch (DataException e)
		{
			e.printStackTrace();
		}
	}
}