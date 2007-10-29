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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;

public class CreateGdb
{
	private List<Pattern> patterns = new ArrayList<Pattern>();

	/**
	   initialize patterns list
	*/
    private void compilePatterns()
	{
    	for(int i = 0; i < lookFor.length; i++) {
    		patterns.add(Pattern.compile(lookFor[i]));		
    	}
    }

	CreateGdb()
	{
		compilePatterns();
	}

    public int getSystemCodeIndex(String s)
	{
    	int i = 0;
		for (Pattern p : patterns)
		{
    		Matcher m = p.matcher(s);
    		if(m.find())
    		{
    			return i;
    		}
    		i++;
    	}
		return -1;
    }

	final static String[] dbfiles = new String[] {
		"human/homo_sapiens_38_36",
		"rat/rattus_norvegicus_core_39_34_i",
		"yeast/yeast_41_1d",
		"yeast/yeast_test",
		"c:/temp/yeast_test",
	};
	/*
	 * Commandline:
	 * - inputfile
	 * - dbname
	 * - dbtype (derby, hsqldb, h2)
	 */
	public static void main(String[] args)
	{
		CreateGdb x = new CreateGdb();
		x.run(args);
		
	}

	void run(String[] args)
	{
		if(args.length == 0) {
			System.out.println("No commandline options specified, using in-code setting");
			String dbname = dbfiles[4];
			String file = dbname + ".txt";
//			GdbMaker gdbMaker = new HsqldbGdbMaker(dbname);
//			GdbMaker gdbMaker = new H2GdbMaker(dbname);
			GdbMaker gdbMaker = new DerbyGdbMaker(dbname);
			createGDBFromTxt (file);
		} else { //TODO: neat commandline options
			String txt = args[0];
			String dbname = args[1];
			String dbtype = args[2];
			if (dbtype.equals("derby")) 
				gdbMaker = new DerbyGdbMaker(dbname);
			else if	(dbtype.equals("hsqldb")) 
				gdbMaker = new HsqldbGdbMaker(dbname);
			else if	(dbtype.equals("h2")) 
				gdbMaker = new H2GdbMaker(dbname);
			if(gdbMaker != null) createGDBFromTxt (txt);
		}
	}

	GdbMaker gdbMaker = null;
	
    void createGDBFromTxt(String file)
	{
    	StopWatch timer = new StopWatch();
    	gdbMaker.info("Timer started");
    	timer.start();
    	try {
    	    
			String dbname = gdbMaker.getDbName();
        	gdbMaker.connect(true);
        	
    		gdbMaker.createTables();
    		    		    		
    		BufferedReader in = new BufferedReader(new FileReader(file));
    		String l;
    		String code = "";
    		int codeIndex = -1;
    		int error = 0;
    		int progress = 0;
			
    		String[] cols = new String[6];
        	// Columns in input:
        	// <ENSG> <XREF/ENSG> <DBNAME> <GENENAME> <DESCR>
    		// new: <ENSG> <PRIM_ID> <DBNAME> <DISP_NAME> <GENENAME> <DESCR>

			while((l = in.readLine()) != null)
    		{
    			progress++;
    			cols = l.split("\t", 6);
    			codeIndex = getSystemCodeIndex(cols[2]);
    			if(codeIndex > -1) {
    				code = GdbMaker.sysCodes[codeIndex];
    				
    				error += gdbMaker.addGene(cols[0], cols[1], code, createBackpageText(cols));
    				error += gdbMaker.addLink(cols[0], cols[1], code);
    				
    				if(codeIndex == GdbMaker.SGD_CODE) {
    					//Also add display_name and parse accnr from description
    					error += processSGD(cols);
    				}
    			}
    			else {
    				gdbMaker.logError.error(cols[0] + "\t" + cols[1] + "\t" + "System code not found: " + cols[2]);
    			}
    			if(progress % PROGRESS_INTERVAL == 0) {
    				gdbMaker.info("Processed " + progress + " lines");
    				gdbMaker.commit();
    			}
    		}
    		gdbMaker.commit();
    		
    		gdbMaker.info("total ids in gene table: " + gdbMaker.getGeneCount());
    		gdbMaker.info("total errors (duplicates): " + error);
    		//r = con.createStatement().executeQuery("SELECT DISTINCT COUNT(idLeft) FROM link");
    		
    		gdbMaker.info("END processint text file");
    		
    		gdbMaker.info("Creating indices");
    		gdbMaker.createIndices();
    		
    		gdbMaker.info("Compacting database");
    		gdbMaker.compact();
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
		gdbMaker.info("Closing connections");
				
    	gdbMaker.close();

		gdbMaker.postInsert();
    	gdbMaker.info("Timer stopped: " + timer.stop());
    }

	//GenMAPP SGD also accepts:
    //- acc nr: e.g. S0000001
    //- orf name: e.g. YHR055C (Ensembl id in Ensembl)
    //- gene name: e.g. CUP1-2
    int processSGD(String[] cols)
	{
    	int error = 0;
    	String ens = cols[0];
    	String code = GdbMaker.sysCodes[GdbMaker.SGD_CODE];
    	String dpn = cols[DISPLAY_NAME_INDEX];
    	String bpTxt = createBackpageText(cols);
    	
    	error += gdbMaker.addGene(ens, ens, code, bpTxt);
    	error += gdbMaker.addLink(ens, ens, code);
    	
    	error += gdbMaker.addGene(ens, dpn, code, bpTxt);
    	error += gdbMaker.addLink(ens, dpn, code);

    	String acc = parseSgdDescription(cols[5]);
    	if(acc != null) {
    		error += gdbMaker.addGene(ens, acc, code, bpTxt);
    		error += gdbMaker.addLink(ens, acc, code);
    	}

    	return error;
    }

	Pattern sgdPattern;
    final static String sgd_descr = "Source:Saccharomyces Genome Database;Acc:";

	String parseSgdDescription(String descr)
	{
    	if(sgdPattern == null) sgdPattern = Pattern.compile(sgd_descr);
    	Matcher m = sgdPattern.matcher(descr);
    	if(m.find())
		{
    		return descr.substring(m.end(), descr.indexOf(']', m.end()));
    	}
    	return null;
    }
	
	private final static long PROGRESS_INTERVAL = 100;
    private final static int DISPLAY_NAME_INDEX = 3;

	private String createBackpageText(String[] cols)
	{
    	int sysIndex = getSystemCodeIndex(cols[2]);
    	String descr = cols[5] == null ? "" : cols[5];
    	String name = cols[4] == null ? "" : cols[4];
    	String secId = cols[3] == null ? "" : cols[3];

    	String bpText = "<TABLE border='1'>" +
    			"<TR><TH>Gene ID:<TH>" + cols[1] +
    			"<TR><TH>Gene Name:<TH>" + name +
    			"<TR><TH>Description:<TH>" + descr +
    			"<TR><TH>Secondary id:<TH>" + secId +
    			"<TR><TH>Systemcode:<TH>" + GdbMaker.sysCodes[sysIndex] +
    			"<TR><TH>System name:<TH>" + GdbMaker.sysNames[sysIndex] + 
    			"<TR><TH>Database name (Ensembl):<TH>" + cols[2] +
    			"</TABLE>";
    	return bpText;
    }

	//NOTE: uses same ordering as GdbMaker.sysCodes
	final static String[] lookFor = new String[] {
			"(?i)uniprot","(?i)affy", "\\bEMBL\\b", "Ensembl", "EntrezGene", "FlyBase", "GO",
			"HUGO", "InterPro", "MGI", "OMIM", "PDB", "Pfam", 
			"RefSeq", "RGD", "SGD", "UniGene", "WormBase",
			"ZFIN"//, "Agilent"
	};


}
