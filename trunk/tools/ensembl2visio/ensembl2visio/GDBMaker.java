package ensembl2visio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.sql.Connection;
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
    
    public void createGDBFromTxt(String file, String dbname) {
    	StopWatch timer = new StopWatch();
    	info("Timer started");
    	timer.start();
    	try {
    	    compilePatterns();
    	    
        	connect();
        	
    		createTables();
    		
    		con.setAutoCommit(false);
    		java.sql.PreparedStatement pstGene = con.prepareStatement(
    				"INSERT INTO gene " +
    				"	(id, code," +
    				"	 backpageText)" +
    				"VALUES (?, ?, ?)"
    		);
    		java.sql.PreparedStatement pstLink = con.prepareStatement(
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
    		String[] cols = new String[4];
        	// Columns in input:
        	// <ENSG> <XREF/ENSG> <DBNAME> <GENENAME> <DESCR>
    		// new: <ENSG> <PRIM_ID> <DBNAME> <DISP_NAME> <GENENAME> <DESCR>
    		while((l = in.readLine()) != null)
    		{
    			progress++;
    			cols = l.split("\t", 4);
    			codeIndex = getSystemCodeIndex(cols[2]);
    			if(codeIndex > -1) {
    				code = sysCodes[codeIndex];
    				
    				try {
    					//System.out.println(cols[1] + "\t" + code + "\t" + createBackpageText(cols));
    					pstGene.setString(1, cols[1]);
    					pstGene.setString(2, code);
    					pstGene.setString(3, createBackpageText(cols));
    					pstGene.executeUpdate();
    				} catch (Exception e) { 
    					logError.error(cols[0] + "\t" + cols[1] + "\t" + code, e);
    					//e.printStackTrace();
    					error++;
    				}
    				try {
    					//System.out.println(cols[0] + "\t" + sysCodes[ENS_CODE] + "\t" + cols[1] + "\t" + code);
    					pstLink.setString(1, cols[0]);
    					pstLink.setString(2, sysCodes[ENS_CODE]);
    					pstLink.setString(3, cols[1]);
    					pstLink.setString(4, code);
    					pstLink.executeUpdate();
    				} catch (Exception e) { 
    					//e.printStackTrace();
    					logError.error(cols[0] + "\t" + cols[1] + "\t" + code, e);
    					error++;
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
    		
    		info("END");
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
		
    	close();
    	info("Timer stopped: " + timer.stop());
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
    	String descr = "";
    	String name = "";
    	String secId = "";
    	switch(cols.length) {
    	case 6:
    		descr = cols[5];
    	case 5:
    		name = cols[4];
    	case 4:
    		secId = cols[3];
    	}
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
		try { sh.execute("DROP TABLE gene"); } catch (Exception e) {}
		sh.execute(
				"CREATE TABLE							" +
				"		gene							" +
				" (   id VARCHAR(50),					" +
				"     code VARCHAR(50),					" +
				"     backpageText VARCHAR(800),				" +
				"     PRIMARY KEY (id, code)			" +
		" )										");
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
	final static long PROGRESS_INTERVAL = 10000;
}
