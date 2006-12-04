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
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import debug.Logger;
import debug.StopWatch;


public abstract class GDBMaker {   	
	List<Pattern> patterns;
    
    Connection con;
      
    String dbName;
    String txtFile;
    
    Logger logInfo;
    Logger logError;
    
    public GDBMaker(String txtFile, String dbName) {
    	this.dbName = dbName;
    	this.txtFile = txtFile;
    	setLoggers();
    }

    void compilePatterns() {
    	patterns = new ArrayList<Pattern>();
    	for(int i = 0; i < lookFor.length; i++) {
    		patterns.add(Pattern.compile(lookFor[i]));		
    	}
    }
    
    public void toGDB() {
    	createGDBFromTxt(txtFile, getDbName());
    }
    
    PreparedStatement pstGene;
    PreparedStatement pstLink;
    
    void createGDBFromTxt(String file, String dbname) {
    	StopWatch timer = new StopWatch();
    	info("Timer started");
    	timer.start();
    	try {
    	    compilePatterns();
    	    
        	connect();
        	
    		createTables();
    		    		
    		con.setAutoCommit(false);
    		pstGene = con.prepareStatement(
    				"INSERT INTO gene " +
    				"	(id, code," +
    				"	 backpageText)" +
    				"VALUES (?, ?, ?)"
    		);
    		pstLink = con.prepareStatement(
    				"INSERT INTO link " +
    				"	(idLeft, codeLeft," +
    				"	 idRight, codeRight)" +
    				"VALUES (?, ?, ?, ?)"
    		);
    		
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
    				code = sysCodes[codeIndex];
    				
    				error += addGene(cols, code);
    				error += addLink(cols, code);
    				
    				if(codeIndex == SGD_CODE) {
    					//Also add display_name and parse accnr from description
    					error += processSGD(cols);
    				}
    			}
    			else {
    				logError.error(cols[0] + "\t" + cols[1] + "\t" + "System code not found: " + cols[2]);
    			}
    			if(progress % PROGRESS_INTERVAL == 0) {
    				info("Processed " + progress + " lines");
    				con.commit();
    			}
    		}
    		con.commit();
    		
    		ResultSet r = con.createStatement().executeQuery("SELECT COUNT(*) FROM gene");
    		r.next();
    		info("total ids in gene table: " + r.getString(1));
    		info("total errors (duplicates): " + error);
    		r = con.createStatement().executeQuery("SELECT DISTINCT COUNT(idLeft) FROM link");
    		
    		info("END processint text file");
    		
    		info("Creating indices");
    		createIndices();
    		
    		info("Compacing database");
    		compact();
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
		info("Closing connections");
				
    	close();
    	info("Timer stopped: " + timer.stop());
    }
    
    int addGene(String[] cols, String code) {
    	return addGene(cols[0], cols[1], code, createBackpageText(cols));
    }
    
    int addGene(String ens, String id, String code, String bpText) {
		try {
			pstGene.setString(1, id);
			pstGene.setString(2, code);
			pstGene.setString(3, bpText);
			pstGene.executeUpdate();
		} catch (Exception e) { 
			logError.error(ens + "\t" + id + "\t" + code, e);
			return 1;
		}
		return 0;
    }
    
    int addLink(String[] cols, String code) {
    	return addLink(cols[0], cols[1], code);
    }
    
    int addLink(String ens, String id, String code) {
    	try {
			pstLink.setString(1, ens);
			pstLink.setString(2, sysCodes[ENS_CODE]);
			pstLink.setString(3, id);
			pstLink.setString(4, code);
			pstLink.executeUpdate();
		} catch (Exception e) {
			logError.error(ens + "\t" + id + "\t" + code, e);
			return 1;
		}
		return 0;
    }
    
    final static int DISPLAY_NAME_INDEX = 3;
    //GenMAPP SGD also accepts:
    //- acc nr: e.g. S0000001
    //- orf name: e.g. YHR055C (Ensembl id in Ensembl)
    //- gene name: e.g. CUP1-2
    int processSGD(String[] cols) {
    	int error = 0;
    	String ens = cols[0];
    	String code = sysCodes[SGD_CODE];
    	String dpn = cols[DISPLAY_NAME_INDEX];
    	String bpTxt = createBackpageText(cols);
    	
    	error += addGene(ens, ens, code, bpTxt);
    	error += addLink(ens, ens, code);
    	
    	error += addGene(ens, dpn, code, bpTxt);
    	error += addLink(ens, dpn, code);

    	String acc = parseSgdDescription(cols[5]);
    	if(acc != null) {
    		error += addGene(ens, acc, code, bpTxt);
    		error += addLink(ens, acc, code);
    	}

    	return error;
    }
    
    Pattern sgdPattern;
    final static String sgd_descr = "Source:Saccharomyces Genome Database;Acc:";
    String parseSgdDescription(String descr) {
    	if(sgdPattern == null) sgdPattern = Pattern.compile(sgd_descr);
    	Matcher m = sgdPattern.matcher(descr);
    	if(m.find()) {
    		return descr.substring(m.end(), descr.indexOf(']', m.end()));
    	}
    	return null;
    }
    
    void setLoggers() {
    	logInfo = new Logger();
    	logError = new Logger();
    	logError.setLogLevel(false, true, true, true, true, true);
    	
    	Calendar cl = Calendar.getInstance();
    	DateFormat df = new SimpleDateFormat("yyMMdd.HHmmss");
    	String now = df.format(cl.getTime());
	    try {
	    	File error = new File(getDbName() + "_" + now + ".error");
	    	File info = new File(getDbName() + "_" + now + ".info");
	    	error.getParentFile().mkdirs();
	    	
	    	logError.setStream(new PrintStream(error));
	    	logInfo.setStream(new PrintStream(info));
	    } catch(Exception e) {
	    	System.out.println("Unable to open output streams, using System.out");
	    	e.printStackTrace();
	    	logError.setStream(System.out);
	    	logInfo.setStream(System.out);
	    }
    }
        
    protected final void info(String msg) {
    	logInfo.info(msg);
    	System.out.println(msg);
    }
    
    protected final void error(String msg) {
    	logError.error(msg);
    }
    
    protected final void error(String msg, Exception e) {
    	logError.error(msg, e);
    }
    
    public String createBackpageText(String[] cols) {
    	int sysIndex = getSystemCodeIndex(cols[2]);
    	String descr = cols[5] == null ? "" : cols[5];
    	String name = cols[4] == null ? "" : cols[4];
    	String secId = cols[3] == null ? "" : cols[3];

    	String bpText = "<TABLE border='1'>" +
    			"<TR><TH>Gene ID:<TH>" + cols[1] +
    			"<TR><TH>Gene Name:<TH>" + name +
    			"<TR><TH>Description:<TH>" + descr +
    			"<TR><TH>Secondary id:<TH>" + secId +
    			"<TR><TH>Systemcode:<TH>" + sysCodes[sysIndex] +
    			"<TR><TH>System name:<TH>" + sysNames[sysIndex] + 
    			"<TR><TH>Database name (Ensembl):<TH>" + cols[2] +
    			"</TABLE>";
    	return bpText;
    }
            
    public abstract void connect() throws ClassNotFoundException, SQLException;
    
    public void close() {
    	try {
    		if(con != null) con.close();
    	} catch(Exception e) {
    		error("Error when closing connection", e);
    	}
    	logInfo.getStream().close();
    	logError.getStream().close();
    }
    
    String getDbName() {
    	return dbName;
    }

    public int getSystemCodeIndex(String s) {   	
    	Iterator it = patterns.iterator();
    	int i = 0;
    	while(it.hasNext()) {
    		Matcher m = ((Pattern)it.next()).matcher(s);
    		if(m.find())
    		{
    			return i;
    		}
    		i++;
    	}
		return -1;
    }
    
    public void compact() throws SQLException { 
    	
    }
    
	public void createTables() throws SQLException {
		info("Info:  Creating tables");
		Statement sh = con.createStatement();
		try { sh.execute("DROP TABLE link"); } catch (Exception e) {}
		sh.execute(
				"CREATE TABLE					" +
				"		link							" +
				" (   idLeft VARCHAR(50) NOT NULL,		" +
				"     codeLeft VARCHAR(50) NOT NULL,	" +
				"     idRight VARCHAR(50) NOT NULL,		" +
				"     codeRight VARCHAR(50) NOT NULL,	" +
				"     bridge VARCHAR(50),				" +
				"     PRIMARY KEY (idLeft, codeLeft,    " +
				"		idRight, codeRight) 			" +
		" )										");
		
		try { sh.execute("DROP TABLE gene"); } catch (Exception e) {}
		sh.execute(
				"CREATE TABLE							" +
				"		gene							" +
				" (   id VARCHAR(50),					" +
				"     code VARCHAR(50),					" +
				"     backpageText VARCHAR(800),				" +
				"     PRIMARY KEY (id, code)			" +
		" )										");
	}
	
	public void createIndices() throws SQLException {
		Statement sh = con.createStatement();
		sh.execute(
				"CREATE INDEX i_codeLeft" +
				" ON link(codeLeft)"
		);
		sh.execute(
				"CREATE INDEX i_idRight" +
				" ON link(idRight)"
		);
		sh.execute(
				"CREATE INDEX i_codeRight" +
				" ON link(codeRight)"
		);
		sh.execute(
				"CREATE INDEX i_code" +
				" ON gene(code)"
		);
	}
	
	final static String[] sysCodes = new String[] {
			"S", "X", "Em", "En", "L", "F", "T",
			"H", "I", "M", "Om", "Pd", "Pf",
			"Q", "R", "D", "U", "W",
			"Z"//, "A"
	};
	
	final static String[] sysNames = new String[] {
		"UniProt/TrEMBL", "Affymetrix Probe Set ID", "EMBL", "Ensembl", "Entrez Gene", 
		"FlyBase", "Gene Ontology", "HUGO", "InterPro", "MGI", 
		"OMIM", "PDB", "Pfam", "RefSeq", "RGD", "SGD", 
		"UniGene", "WormBase", "ZFIN"//, "Agilent Probe ID"
	};
	
	final static String[] lookFor = new String[] {
			"(?i)uniprot","(?i)affy", "\\bEMBL\\b", "Ensembl", "EntrezGene", "FlyBase", "GO",
			"HUGO", "InterPro", "MGI", "OMIM", "PDB", "Pfam", 
			"RefSeq", "RGD", "SGD", "UniGene", "WormBase",
			"ZFIN"//, "Agilent"
	};
	
	final static int ENS_CODE = 3;
	final static int SGD_CODE = 15;
	final static long PROGRESS_INTERVAL = 10000;
}
