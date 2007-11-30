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

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.pathvisio.debug.Logger;

/**
   Abstract base class, to be derived for each different database
   implementation.
 */
public abstract class GdbMaker
{   	
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
	
	final static int ENS_CODE = 3;
	final static int SGD_CODE = 15;

	static final int COMPAT_VERSION = 1;
    
    protected Connection con;
      
    String dbName;
    
    Logger logInfo;
    Logger logError;
    
    public GdbMaker(String dbName) {
    	this.dbName = dbName;
    	setLoggers();
    }

        
    PreparedStatement pstGene;
    PreparedStatement pstLink;

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
    
	/**
	   connect to a gene database
	   if create == true, any pre-existing database will be overwritten.
	   if create == false, opens pre-existing database for appending.
	 */
    public abstract void connect(boolean create) throws ClassNotFoundException, SQLException;
    
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
    
    public void compact() throws SQLException { 
    	
    }

	/**
	   call after closing the database
	   Does nothing by default, some databases need to do some
	   cleanup operation after closing the database
	 */
	public void postInsert() {}
	

	public void createTables() throws SQLException {
		info("Info:  Creating tables");
		Statement sh = con.createStatement();
		try { sh.execute("DROP TABLE link"); } catch (Exception e) {}
		try { sh.execute("DROP TABLE gene"); } catch (Exception e) {}
		try { sh.execute("DROP TABLE info"); } catch (Exception e) {}
		sh.execute(
				"CREATE TABLE					" +
				"		info							" +
				"(	  version INTEGER PRIMARY KEY		" +
				")");
		sh.execute( //Add compatibility version of Gdb
				"INSERT INTO info VALUES ( " + COMPAT_VERSION + ")");
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

	/**
	   Create indices on the database
	   You can call this at any time after creating the tables,
	   but it is good to do it only after inserting all data.
	 */
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
	

	
	/**
	   prepare for inserting genes and/or links
	 */
	void preInsert() throws SQLException
	{
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
	}

	/**
	   commit inserted data
	 */
	void commit() throws SQLException
	{
		con.commit();
	}

	/**
	   returns number of rows in gene table
	 */
	int getGeneCount() throws SQLException
	{
		ResultSet r = con.createStatement().executeQuery("SELECT COUNT(*) FROM gene");
		r.next();
		int result = r.getInt (1);
		r.close();
		return result;
	}
}
