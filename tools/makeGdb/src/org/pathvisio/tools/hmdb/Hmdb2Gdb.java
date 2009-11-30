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
package org.pathvisio.tools.hmdb;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.pathvisio.data.DBConnector;
import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.data.SimpleGdbFactory;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.tools.hmdb.ParseHmdb.Compound;
import org.pathvisio.tools.hmdb.ParseHmdb.ParseException;

/**
 * Program to create a metabolite database based on a
 * metabocards flat text file, which can be downloaded from http://www.hmdb.ca
 *
 * In fall '08 HMDB changed the metabocard file format,
 * This program is requires the newer format.
 */
public class Hmdb2Gdb
{
	/**
	 * @param args command line arguments
	 *
	 * Commandline:
	 * - output database: .pgdb
	 * - input metabocards .txt file
	 */
	public static void main(String[] args)
	{
		Logger.log.setStream (System.out);
		String dbname = args[0];
		String file = args[1];

		Hmdb2Gdb h2g = new Hmdb2Gdb();
		PreferenceManager.init();

    	try
    	{
			SimpleGdb simpleGdb = SimpleGdbFactory.createInstance(dbname, new DataDerby(), DBConnector.PROP_RECREATE);

    		h2g.init (dbname, simpleGdb);
    		h2g.run(new File (file));
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

	SimpleGdb simpleGdb;
	String dbName;

	StopWatch timer;

	private void init(String dbname, SimpleGdb simpleGdb) throws DataException, ClassNotFoundException
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

	private void done() throws DataException
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

	private void addCompound (Compound c)
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
		}

		if (c.idKegg != null) for (String id : c.idKegg)
		{
			Xref right = new Xref (id, DataSource.KEGG_COMPOUND);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
		}

		if (c.idChebi != null) for (String id : c.idChebi)
		{
			Xref right = new Xref (id, DataSource.CHEBI);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
		}

		if (c.idPubchem != null) for (String id : c.idPubchem)
		{
			Xref right = new Xref (id, DataSource.PUBCHEM);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
		}

		if (c.idCas != null) for (String id : c.idCas)
		{
			Xref right = new Xref (id, DataSource.CAS);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
		}

		if (c.idWikipedia != null) for (String id : c.idWikipedia)
		{
			Xref right = new Xref (id, DataSource.WIKIPEDIA);
			error += simpleGdb.addGene(right, bpText);
			error += simpleGdb.addLink(ref, right);
		}

		if (c.smiles != null)
		{
			error += simpleGdb.addAttribute(ref, "SMILES", c.smiles);
		}

		if (c.synonyms != null) for (String synonym : c.synonyms)
		{
			error += simpleGdb.addAttribute(ref, "Synonym", synonym);
		}
	}

	private void run(File f) throws IOException, DataException
	{
		ParseHmdb parser = new ParseHmdb();
		StopWatch sw = new StopWatch();
		sw.start();
		LineNumberReader br = new LineNumberReader (new FileReader(f));
		Compound c;
		try
		{
			while ((c = parser.readNext(br)) != null)
			{
				progress++;
				addCompound (c);
				if(progress % PROGRESS_INTERVAL == 0) {
					Logger.log.info("Processed " + progress + " record");
					simpleGdb.commit();
				}

				Logger.log.info (c.symbol + " added");
			}
			Logger.log.info ("Total: " + progress);
		}
		catch (ParseException pe)
		{
			System.err.println (pe.getMessage());
			System.err.println ("Please check that this is a valid metabocards file");
			pe.printStackTrace();
		}
		Logger.log.info ("Finished in " + sw.stop() + "ms");

	}

	private final static long PROGRESS_INTERVAL = 100;
}
