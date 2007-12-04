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
package ensembl2visio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import org.pathvisio.debug.StopWatch;

/**
 * Provides a main for importing data into existing database
 * Used to add metabolomics data
 * 
 * @author martijn
 */
public class AppendGdb {

	/**
	 * @param args command line arguments
	 * 
	 * Commandline:
	 * - database directory (=dbname)
	 * - metabolite table .txt file
	 * assumes database type is derby (unzipped)
	 */
	public static void main(String[] args) 
	{
		String dbname = args[0];
		String file = args[1];
		
		DerbyGdbMaker gdbMaker = new DerbyGdbMaker(dbname);
	    AddMetabolitesFromTxt(gdbMaker, file, dbname);        
	}
	
	static void AddMetabolitesFromTxt(GdbMaker gdbMaker, String file, String dbname) 
    {
		StopWatch timer = new StopWatch();    	
    	
    	gdbMaker.info("Timer started");
    	timer.start();
    	try {
			gdbMaker.connect (true);

			gdbMaker.preInsert();			
			
			BufferedReader in = new BufferedReader(new FileReader(file));
			String l;
			l = in.readLine(); // skip header row 
			int codeIndex = -1;
			int error = 0;
			int progress = 0;
			int nCols = 8;
			String[] cols = new String[nCols];
	    	// Columns in input:
			// <
			while((l = in.readLine()) != null)
			{
				progress++;
				cols = l.split("\t", nCols);
				
				String idCas = cols[0]; // CAS no
				String id;
				String code;
				String bpText = "<TABLE border='1'>" +
    			"<TR><TH>Metabolite:<TH>" + cols[1] +
    			"<TR><TH>Bruto Formula:<TH>" + cols[3] + 
    			"</TABLE>";

				code = "Ca"; // CAS
				id = cols[0];		    	
				error += gdbMaker.addGene(idCas, id, code, bpText);
				error += gdbMaker.addLink(idCas, id, code);
					
				if (cols.length > 2 && cols[2].length() > 0)
				{
					code = "Ck"; // KEGG compound
					id = cols[2];		    	
					error += gdbMaker.addGene(idCas, id, code, bpText);
					error += gdbMaker.addLink(idCas, id, code);
				}
				
				if (cols.length > 7 && cols[7].length() > 0)
				{
					code = "Nw"; // NuGO wiki
					id = cols[7];		    	
					error += gdbMaker.addGene(idCas, id, code, bpText);
					error += gdbMaker.addLink(idCas, id, code);
				}
				
				if (cols.length > 5 && cols[5].length() > 0)
				{
					code = "Ce"; // ChEBI
					id = cols[5];		    	
					error += gdbMaker.addGene(idCas, id, code, bpText);
					error += gdbMaker.addLink(idCas, id, code);
				}
				
				if (cols.length > 6 && cols[6].length() > 0)
				{	
					code = "Cp"; // PuBCHEM
					id = cols[6];		    	
					error += gdbMaker.addGene(idCas, id, code, bpText);
					error += gdbMaker.addLink(idCas, id, code);
				}
				
				if (cols.length > 4 && cols[4].length() > 0)
				{
					code = "Ch"; // HMDB
					id = cols[4];		    	
					error += gdbMaker.addGene(idCas, id, code, bpText);
					error += gdbMaker.addLink(idCas, id, code);
				}
				
				if(progress % PROGRESS_INTERVAL == 0) {
					gdbMaker.info("Processed " + progress + " lines");
					gdbMaker.commit();
				}
			}
			gdbMaker.commit();

			gdbMaker.info("total ids in gene table: " + gdbMaker.getGeneCount());
			gdbMaker.info("total errors (duplicates): " + error);
			
			gdbMaker.info("END processing text file");
			
			gdbMaker.info("Compacting database");
			gdbMaker.compact();
	    }
		catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getNextException().getMessage());
		}	
		catch (Exception e)
		{
			e.printStackTrace();
		}
		gdbMaker.info("Closing connections");
				
    	gdbMaker.close();

		gdbMaker.postInsert();
    	gdbMaker.info("Timer stopped: " + timer.stop());
    }

	private final static long PROGRESS_INTERVAL = 100;
	
}
