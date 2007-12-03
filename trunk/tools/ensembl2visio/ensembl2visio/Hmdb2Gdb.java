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
import java.io.IOException;
import java.sql.SQLException;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;

/**
 * Provides a main for importing hmdb data (prepated with the parse_hmdb.pl script)
 * Create a Gene database with metabolite information
 * 
 * @author martijn
 */
public class Hmdb2Gdb 
{

	/**
	 * @param args command line arguments
	 * 
	 * Commandline:
	 * - database directory (=dbname)
	 * - metabolite table .txt file
	 * 
	 * (For example "/home/martijn/db/hmdb/20071123_metabocards_all.txt")
	 * assumes database type is derby (unzipped)
	 */
	public static void main(String[] args)
	{
		String dbname = args[0];
		String file = args[1];
		
		DerbyGdbMaker gdbMaker = new DerbyGdbMaker(dbname);
	    Hmdb2Gdb h2g = new Hmdb2Gdb();

    	try 
    	{
 
    		h2g.init (dbname, gdbMaker);
    		h2g.run(file);
    		h2g.done();
    	}
		catch (SQLException e) 
		{
			e.printStackTrace();
			System.err.println(e.getNextException().getMessage());
		}	
		catch (Exception e)
		{
			e.printStackTrace();
		}   	
		gdbMaker.toZip();
	}
		
	static class Compound
	{
		String idHmdb = null;
		String symbol = null;
		String formula = null;
		String idKegg = null;
		String idPubchem = null;
		String idChebi = null;
		String idCas = null;
	}
	
	GdbMaker gdbMaker;
	String dbName;
	
	void run (String file) throws IOException, SQLException
	{
		BufferedReader in = new BufferedReader(new FileReader(file));
		in.readLine(); // skip header row 
		Compound c;
		while ((c = parseNext (in)) != null)
		{
			progress++;
			addCompound (c);
			if(progress % PROGRESS_INTERVAL == 0) {
				gdbMaker.info("Processed " + progress + " lines");
				gdbMaker.commit();
			}
			
			gdbMaker.info (c.symbol + " added");
		}
	}
	

	StopWatch timer;
	
	void init(String dbname, GdbMaker gdbMaker) throws SQLException, ClassNotFoundException
	{
		timer = new StopWatch();    	
    	
		this.gdbMaker = gdbMaker;
		this.dbName = dbname;
		
    	gdbMaker.info("Timer started");
    	timer.start();
		gdbMaker.connect (true);

		gdbMaker.createTables();
		gdbMaker.preInsert();	
    }
	
	void done() throws SQLException
	{
		gdbMaker.commit();

    	gdbMaker.info("Timer stopped: " + timer.stop());
    	
		gdbMaker.info("total ids in gene table: " + gdbMaker.getGeneCount());
		gdbMaker.info("total errors (duplicates): " + error);
		
		gdbMaker.info("END processing text file");
		
		gdbMaker.info("Compacting database");
		gdbMaker.createIndices();
		gdbMaker.compact();
		
		gdbMaker.info("Closing connections");
		
    	gdbMaker.close();

		gdbMaker.postInsert();
    
	}
	
	int error = 0;
	int progress = 0;

	void addCompound (Compound c)
	{
		String bpText = "<TABLE border='1'>" +
		"<TR><TH>Metabolite:<TH>" + c.symbol +
		"<TR><TH>Bruto Formula:<TH>" + c.formula + 
		"</TABLE>";
		
		error += gdbMaker.addGene(c.idHmdb, c.idHmdb, DataSource.HMDB.getSystemCode(), bpText);
		error += gdbMaker.addLink(c.idHmdb, c.idHmdb, DataSource.HMDB.getSystemCode());

		if (c.idKegg != null)
		{
			error += gdbMaker.addGene(c.idHmdb, c.idKegg, DataSource.KEGG_COMPOUND.getSystemCode(), bpText);
			error += gdbMaker.addLink(c.idHmdb, c.idKegg, DataSource.KEGG_COMPOUND.getSystemCode());
		}
		
		if (c.idChebi != null)
		{
			error += gdbMaker.addGene(c.idHmdb, c.idChebi, DataSource.CHEBI.getSystemCode(), bpText);
			error += gdbMaker.addLink(c.idHmdb, c.idChebi, DataSource.CHEBI.getSystemCode());
		}
		
		if (c.idPubchem != null)
		{	
			error += gdbMaker.addGene(c.idHmdb, c.idPubchem, DataSource.PUBCHEM.getSystemCode(), bpText);
			error += gdbMaker.addLink(c.idHmdb, c.idPubchem, DataSource.PUBCHEM.getSystemCode());
		}
		
		if (c.idCas != null)
		{
			error += gdbMaker.addGene(c.idHmdb, c.idCas, DataSource.CAS.getSystemCode(), bpText);
			error += gdbMaker.addLink(c.idHmdb, c.idCas, DataSource.CAS.getSystemCode());
		}		
	}
	
	Compound parseNext (BufferedReader in) throws IOException
	{
		final int COL_HMDB_ID = 0;
		final int COL_SYMBOL = 1;
		final int COL_FORMULA = 2;
		final int COL_KEGG = 3;
		final int COL_BIOCYC = 4;
		final int COL_PUBCHEM = 5;
		final int COL_OMIM = 6;
		final int COL_CHEBI = 7;
		final int COL_CAS = 8;
		
		String l;
		l = in.readLine();
		if (l == null) return null;
		
		Compound result = new Compound();
		
		String[] cols = l.split("\t");
			
		result.idHmdb = cols[COL_HMDB_ID]; // CAS no
		result.symbol = cols[COL_SYMBOL];
		result.formula = cols[COL_FORMULA];
		if (cols.length > COL_KEGG && !cols[COL_KEGG].equals("Not Available"))
		{	
			result.idKegg = cols[COL_KEGG];
		}

		if (cols.length > COL_PUBCHEM && !cols[COL_PUBCHEM].equals("Not Available"))
		{
			result.idPubchem = cols[COL_PUBCHEM];
		}
		
		if (cols.length > COL_CHEBI && !cols[COL_CHEBI].equals("Not Available"))
		{
			result.idChebi = cols[COL_CHEBI];
		}
		
		if (cols.length > COL_CAS && !cols[COL_CAS].equals("Not Available"))
		{
			result.idCas = cols[COL_CAS];
		}
		
		return result;
	}
		
	private final static long PROGRESS_INTERVAL = 100;
	
}
