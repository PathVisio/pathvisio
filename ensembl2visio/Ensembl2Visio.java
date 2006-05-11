import java.sql.*;
import java.sql.DriverManager;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.driver.ComparaDriver;

import org.ensembl.datamodel.*;
import org.ensembl.registry.*;
import org.ensembl.driver.*;
import org.ensembl.variation.driver.*;

public class Ensembl2Visio {
	final static String[] sysCodes = new String[] {
			"U", "X", "Em", "En", "L", "F", "T",
			"H", "I", "M", "Om", "Pd", "Pf",
			"Q", "R", "D", "S", "W",
			"Z"
	};
	
	final static String[] sysNames = new String[] {
		"UniProt/TrEMBL", "Affymetrix Probe Set ID", "EMBL", "Ensembl", "Entrez Gene", 
		"FlyBase", "Gene Ontology", "HUGO", "InterPro", "MGI", 
		"OMIM", "PDB", "Pfam", "RefSeq", "RGD", "SGD", 
		"UniGene", "WormBase", "ZFIN"
	};
	
	final static String[] lookFor = new String[] {
			"(?i)uniprot","(?i)affy", "\\bEMBL\\b", "Ensembl", "EntrezGene", "FlyBase", "GO",
			"HUGO", "InterPro", "MGI", "OMIM", "PDB", "Pfam", 
			"RefSeq", "RGD", "SGD", "UniGene", "WormBase",
			"ZFIN"
	};
	final static int ENS_CODE = 2;
	
	Vector patterns;
    
    Connection con;
    
    public static void main(String[] args) {
//    	int nrGenes = 10;
//    	String organism = "human";
    	String file = "ensembl_genes.txt";
    	String dbname = "ensembl_homo_sapiens_38_36";
    	Ensembl2Visio ensj = new Ensembl2Visio();
//    	ensj.fetchFromEnsembl(nrGenes, organism, file); // Do this with perl
    	ensj.createGdbFromTxt(file, dbname);
    }
    
    public Ensembl2Visio() {}

    public void compilePatterns()
    {
    	patterns = new Vector();
    	for(int i = 0; i < lookFor.length; i++)
    	{
    		patterns.add(Pattern.compile(lookFor[i]));		
    	}
    }
    
    public void createGdbFromTxt(String file, String dbname)
    {
    	PrintWriter eout = null;
	    try {
	        eout = new PrintWriter(new FileWriter("error.txt"));
	    } catch(IOException ex) {
	        ex.printStackTrace();
	    }
	    
	    compilePatterns();
	    
    	connect(dbname);
		createTables();
    	try
    	{
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
    			cols = l.split("\t");
    			codeIndex = getSystemCodeIndex(cols[2]);
    			if(codeIndex > -1)
    			{
    				code = sysCodes[codeIndex];
    				
    				try {
    					//System.out.println(cols[1] + "\t" + code + "\t" + createBackpageText(cols));
    					pstGene.setString(1, cols[1]);
    					pstGene.setString(2, code);
    					pstGene.setString(3, createBackpageText(cols));
    					pstGene.executeUpdate();
    				} catch (Exception e) { 
    					eout.println(cols[0] + "\t" + cols[1] + "\t" + code + "\t" + e.getMessage());
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
    					eout.println(cols[0] + "\t" + cols[1] + "\t" + code + "\t" + e.getMessage());
    					error++;
    				}
    			}
    			else
    			{
    				eout.println(cols[0] + "\t" + cols[1] + "\t" + "System code not found: " + cols[2]);
    			}
    			if(progress % 10000 == 0) {
    				System.out.println("Processed " + progress + " lines");
    				con.commit();
    			}
    			con.commit();
    		}
    		ResultSet r = con.createStatement().executeQuery("SELECT COUNT(*) FROM gene");
    		r.next();
    		System.out.println("total ids in gene table: " + r.getString(1));
    		System.out.println("total errors (duplicates): " + error);
    		r = con.createStatement().executeQuery("SELECT DISTINCT COUNT(idLeft) FROM link");
	    	close();
	    	// Set readonly to true
	    	Properties prop = new Properties();
	    	File propFile = new File(dbname + ".properties");
			try {
				prop.load(new FileInputStream(propFile));
				prop.setProperty("readonly","true");
				prop.store(new FileOutputStream(propFile), "HSQL Database Engine");
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}	
    }
    
    public String createBackpageText(String[] cols)
    {
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
    
    public int getSystemCodeIndex(String s)
    {   	
    	Iterator it = patterns.iterator();
    	int i = 0;
    	while(it.hasNext())
    	{
    		Matcher m = ((Pattern)it.next()).matcher(s);
    		if(m.find())
    		{
    			return i;
    		}
    		i++;
    	}
		return -1;
    }
    
    public void connect(String dbname) {
    	Properties prop = new Properties();
    	prop.setProperty("user","sa");
    	prop.setProperty("password","");
    	try
    	{
		Class.forName("org.hsqldb.jdbcDriver");
		con = DriverManager.getConnection("jdbc:hsqldb:file:" + dbname, prop);
		con.setAutoCommit(true);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public void connectH2(String dbname) {
    	try {
    	Class.forName("org.h2.Driver");
    	con = DriverManager.
    	  getConnection("jdbc:h2:file:" + dbname, "sa", "");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
	public void close() {
		System.out.println("Closing connections");
		try
		{
			if(con != null) {
				con.commit();
				Statement sh = con.createStatement();
				sh.executeQuery("SHUTDOWN");
				sh.close();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void createTables() {
		System.out.println ("Info:  Creating tables");
		
		try {
			Statement sh = con.createStatement();
			sh.execute("DROP TABLE link IF EXISTS");
			sh.execute("DROP TABLE gene IF EXISTS");
		} catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
		}
		try
		{
			Statement sh = con.createStatement();
			sh.execute("DROP TABLE link IF EXISTS");
			sh.execute(
					"CREATE CACHED TABLE					" +
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
			sh.execute("DROP TABLE gene IF EXISTS");
			sh.execute(
					"CREATE CACHED TABLE							" +
					"		gene							" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     backpageText VARCHAR,				" +
					"     PRIMARY KEY (id, code)			" +
			" )										");
			sh.execute(
					"CREATE INDEX i_code" +
					" ON gene(code)"
					);
			
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void fetchFromEnsembl(int nrGenes, String organism, String file)
	{
		PrintWriter out = null;
	    try {
	        out = new PrintWriter(new FileWriter(file));
	    } catch(IOException ex) {
	        ex.printStackTrace();
	    }
	    
	    try
	    {
	    	System.out.println("Start fetching genes");
	    	long start = System.currentTimeMillis();
	    	CoreDriver coreDriver = connectLocalEns();
	    	GeneAdaptor ga = coreDriver.getGeneAdaptor();
	    	List genes = ga.fetchAll(false);
	    	long time = System.currentTimeMillis() - start;
	    	System.out.println("Finished fetching " + ga.fetchCount() + " genes in " + time + " ms");
	    	System.out.println("Start processing " + Math.min(nrGenes, ga.fetchCount()) + " genes");
	    	start = System.currentTimeMillis();
	    	
	    	Iterator it = genes.iterator();
	    	int i = 0;
	    	Gene g;
	    	List erefs;
	    	Iterator eIt;
	    	String ensID;
	    	ExternalRef e;
	    	// Columns in output:
	    	// <ENSG> <XREF/ENSG> <DBNAME> <GENENAME> <DESCR>
	    	while(it.hasNext() && i < nrGenes)
	    	{
	    		g = (Gene)it.next();
	    		ensID = g.getAccessionID();
	    		out.println(
	    				ensID + "\t" +
	    				ensID + "\t" +
	    				g.getDisplayName() + "\t" +
	    				"Ensembl" + "\t" + 
	    				g.getDescription()
	    				);
	    		erefs = g.getExternalRefs(true);
	    		eIt = erefs.iterator();
	    		while(eIt.hasNext())
	    		{
	    			e = (ExternalRef)eIt.next();
	    			out.println(
	    					ensID + "\t" +
	    					e.getDisplayID() + "\t" +
	    					e.getExternalDatabase().getName() + "\t" +
	        				e.getDescription()
	    			);
	    		}
	    		if(i % 1000 == 0)
	    			System.out.println(i + "\t" + ensID);
	    		i++;
	    	}
	    	time = System.currentTimeMillis() - start;
	    	System.out.println("Finished processing " + Math.min(nrGenes, ga.fetchCount()) + " genes in " + time + " ms");
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	    out.close();
	}

	public CoreDriver connectLocalEns()
	{
		Properties database = new Properties();
		database.put("host","localhost");			// location of the database ("localhost")
		database.put("port","3306");				// accesport for the database (default port: "3306")
		database.put("user","root");				// username for the database ("root")
		database.put("password",""); 				// comment because no password is needed
		database.put("database","homo_sapiens_core_38_36");		// name of Database ("Ensembl")
		database.put("schema_version","38");		// version 37, found in the folder name
		CoreDriver coreDriver = null;
		try {
			coreDriver = CoreDriverFactory.createCoreDriver(database);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return coreDriver;
	}

	public CoreDriver connectRemoteEns(String organism)
	{
		CoreDriver coreDriver = null;
		try {
			Registry dr = Registry.createDefaultRegistry();
			coreDriver = dr.getGroup(organism).getCoreDriver();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return coreDriver;
	}
}
