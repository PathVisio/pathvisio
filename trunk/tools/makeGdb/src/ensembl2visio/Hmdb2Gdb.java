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

import org.pathvisio.Engine;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdbFactory;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;

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
	 * (For example "/home/martijn/uni/wrk/metabolomics/20080507_hmdb_extracted_links.txt")
	 * assumes database type is derby (unzipped)
	 */
	public static void main(String[] args)
	{
		Logger.log.setStream (System.out);
		String dbname = args[0];
		String file = args[1];
		
		Hmdb2Gdb h2g = new Hmdb2Gdb();
		Engine.init();
		
    	try 
    	{
			SimpleGdb simpleGdb = SimpleGdbFactory.createInstance(dbname, new DataDerby(), DBConnector.PROP_RECREATE);
 
    		h2g.init (dbname, simpleGdb);
    		h2g.run(file);
    		h2g.done();
    	}
		catch (DataException e) 
		{
			Logger.log.error ("DataException ", e);
		}	
		catch (Exception e)
		{
			Logger.log.error ("Exception ", e);
		}   	
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
		String[] synonyms = null;
	}
	
	SimpleGdb simpleGdb;
	String dbName;
	
	void run (String file) throws IOException, DataException
	{
		BufferedReader in = new BufferedReader(new FileReader(file));
		in.readLine(); // skip header row 
		Compound c;
		while ((c = parseNext (in)) != null)
		{
			progress++;
			addCompound (c);
			if(progress % PROGRESS_INTERVAL == 0) {
				Logger.log.info("Processed " + progress + " lines");
				simpleGdb.commit();
			}
			
			Logger.log.info (c.symbol + " added");
		}
	}
	

	StopWatch timer;
	
	void init(String dbname, SimpleGdb simpleGdb) throws DataException, ClassNotFoundException
	{
		timer = new StopWatch();    	
    	
		this.simpleGdb = simpleGdb;
		this.dbName = dbname;
		
    	Logger.log.info("Timer started");
    	timer.start();
//		simpleGdb.connect (true);

		simpleGdb.createGdbTables();
		simpleGdb.preInsert();	
    }
	
	void done() throws DataException
	{
		simpleGdb.commit();

    	Logger.log.info("Timer stopped: " + timer.stop());
    	
    	Logger.log.info("total ids in gene table: " + simpleGdb.getGeneCount());
    	Logger.log.info("total errors (duplicates): " + error);
		
    	Logger.log.info("END processing text file");
		
    	Logger.log.info("Compacting database");
		
		Logger.log.info("Closing connections");
		

    	simpleGdb.finalize();
	}
	
	int error = 0;
	int progress = 0;

	void addCompound (Compound c)
	{
		String bpText = "<TABLE border='1'>" +
		"<TR><TH>Metabolite:<TH>" + c.symbol +
		"<TR><TH>Bruto Formula:<TH>" + c.formula + 
		"</TABLE>";
		
		Xref ref = new Xref (c.idHmdb, DataSource.HMDB);
		error += simpleGdb.addGene(ref, bpText);
		error += simpleGdb.addLink(ref, ref);
		error += simpleGdb.addAttribute(ref, "Symbol", c.symbol);

		if (c.symbol != null)
		{
			// hmdb id is actually also the NUGOWIKI id.
			Xref right = new Xref (c.idHmdb, DataSource.NUGOWIKI);
			error += simpleGdb.addGene (right, bpText);
			error += simpleGdb.addLink (ref, right);
			error += simpleGdb.addAttribute(right, "Symbol", c.symbol);
		}
		
		if (c.idKegg != null)
		{
			Xref right = new Xref (c.idKegg, DataSource.KEGG_COMPOUND);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
			error += simpleGdb.addAttribute(right, "Symbol", c.symbol);
		}
		
		if (c.idChebi != null)
		{
			Xref right = new Xref (c.idChebi, DataSource.CHEBI);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
			error += simpleGdb.addAttribute(right, "Symbol", c.symbol);
		}
		
		if (c.idPubchem != null)
		{	
			Xref right = new Xref (c.idPubchem, DataSource.PUBCHEM);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
			error += simpleGdb.addAttribute(right, "Symbol", c.symbol);
		}
		
		if (c.idCas != null)
		{
			Xref right = new Xref (c.idCas, DataSource.CAS);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
			error += simpleGdb.addAttribute(right, "Symbol", c.symbol);
		}
		
		//TODO
		/*
		for (String synonym : c.synonyms)
		{
			error += simpleGdb.addAttribute(ref, "Synonym", synonym);			
		}
		*/
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
		final int COL_SYNONYMS = 9;
		final int COL_WIKIPEDIA = 10;
		
		String l;
		l = in.readLine();
		if (l == null) return null;
		
		Compound result = new Compound();
		
		String[] cols = l.split("\t");
			
		result.idHmdb = cols[COL_HMDB_ID]; // CAS no
		result.symbol = cols[COL_SYMBOL];
		result.formula = cols[COL_FORMULA];
		result.synonyms = cols[COL_SYNONYMS].split ("; ");
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
