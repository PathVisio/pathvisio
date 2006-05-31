package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import data.GmmlGex.ConvertThread;

public class GmmlGdb {
	final static File propsFile = new File("gdb.properties");
	public Connection con;
	public Properties props;
	
	File gdbFile;
	
	public GmmlGdb() {
		props = new Properties();
		try {
			// Check if properties file exists
			if(propsFile.canRead()) {
				props.load(new FileInputStream(propsFile));
			} else {
				// Create properties file
				propsFile.createNewFile();
				props.setProperty("currentGdb", "none");
			}
			if(!props.get("currentGdb").equals("none"))
			{
				gdbFile = new File((String)props.get("currentGdb"));
				if(connect(null) != null)
					setCurrentGdb("none");
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void setCurrentGdb(String gdb) {
		changeProps("currentGdb", gdb);
	}
	
	public void changeProps(String name, String value) {
		props.setProperty(name, value);
		try {
			props.store(new FileOutputStream(propsFile),"Gene Database Properties");
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getBpInfo(String id) {
		try {
			Statement s = con.createStatement();
			ResultSet r = s.executeQuery("SELECT backpageText FROM gene " +
					"WHERE id = '" + id + "'");
			r.next();
			String result = r.getString(1);
			return result;
		} catch(Exception e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList ensId2Refs(String ensId) {
		ArrayList crossIds = new ArrayList();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight FROM link " +
					"WHERE idLeft = '" + ensId + "'"
					);
			while(r1.next()) {
				crossIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return crossIds;
	}
	
//	Don't use this, multiple simple select queries is faster
//	Subsequentially use ref2EnsIds ensIds2Refs
	public ArrayList getCrossRefs(String id) {
		ArrayList crossIds = new ArrayList();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight FROM link " +
					"WHERE idLeft IN ( 		  " +
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + id + "')"
					);
			while(r1.next()) {
				crossIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return crossIds;
	}
	
	public ArrayList ref2EnsIds(String ref)
	{
		ArrayList ensIds = new ArrayList();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + ref + "'"
			);
			while(r1.next()) {
				ensIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ensIds;
	}
	
//	Don't use this, multiple simple select queries is faster
	public HashMap getEns2RefHash(ResultSet r, int ensIndex)
	{
		HashMap ens2RefHash = new HashMap();
		try {
			int pr = 0;
			StringBuilder idString = new StringBuilder();
			while(r.next()) {
				idString.append("'" + r.getString(ensIndex) + "', ");
				pr++;
				if(pr % 500 == 0) {
					System.out.println(pr);
				}
			}
			String ids = idString.substring(0,idString.lastIndexOf(", "));
		long t = System.currentTimeMillis();
		ResultSet r1 = con.createStatement().executeQuery(
				"SELECT idLeft, idRight FROM link WHERE idLeft IN ( " +
				ids + ")"
		);
		System.out.println("Query to find reference ids for genes took: " +
				(System.currentTimeMillis()-t) + " ms");
		while(r1.next()) {
			String id = r1.getString(1);
			if(ens2RefHash.containsKey(id)) {
				((ArrayList)ens2RefHash.get(id)).add(r1.getString(2));
			} else {
				ArrayList refIds = new ArrayList();
				refIds.add(r1.getString(2));
				ens2RefHash.put(id, refIds);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ens2RefHash;
	}
	
//	Don't use this, multiple simple select queries is faster
	public HashMap getRef2EnsHash(ResultSet r, int refIndex)
	{
		HashMap ref2EnsHash = null;
		try {
			int pr = 0;
			StringBuilder idString = new StringBuilder();
			while(r.next()) {
				idString.append("'" + r.getString(refIndex) + "', ");
				pr++;
				if(pr % 500 == 0) {
					System.out.println(pr);
				}
			}
			String ids = idString.substring(0,idString.lastIndexOf(", "));
			
			ref2EnsHash = new HashMap();
			long t = System.currentTimeMillis();
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idLeft, idRight FROM link WHERE idRight IN ( " +
					ids + ")"
			);
			System.out.println("Query to find ensembl ids for genes took: " +
					(System.currentTimeMillis()-t) + " ms");
			while(r1.next()) {
				String id = r1.getString(2);
				if(ref2EnsHash.containsKey(id)) {
					((ArrayList)ref2EnsHash.get(id)).add(r.getString(1));
				} else {
					ArrayList ensIds = new ArrayList();
					ensIds.add(r1.getString(1));
					ref2EnsHash.put(id, ensIds);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ref2EnsHash;
	}
	
	public String connect(File gdbFile)
	{
		if(gdbFile == null)
			gdbFile = this.gdbFile;
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Properties prop = new Properties();
			prop.setProperty("user","sa");
			prop.setProperty("password","");
			//prop.setProperty("hsqldb.default_table_type","cached");
			String file = gdbFile.getAbsolutePath().toString();
			con = DriverManager.getConnection("jdbc:hsqldb:file:" + 
					file.substring(0, file.lastIndexOf(".")), prop);
			con.setReadOnly(true);
			setCurrentGdb(gdbFile.getAbsoluteFile().toString());
			return null;
		} catch(Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public void close()
	{
		if(con != null)
		{
			try
			{
				Statement sh = con.createStatement();
				sh.executeQuery("SHUTDOWN"); // required, to write last changes
				sh.close();
				con = null;
			} catch (Exception e) {
				System.out.println ("Error: " +e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	//Convert from GenMAPP
	public void convertGdb(File gmGdbFile, File gdbFile) {

		PrintWriter error = null;
	    try {
	        error = new PrintWriter(new FileWriter("convert_gdb_log.txt"));
	    } catch(IOException ex) {
	        ex.printStackTrace();
	    }
	    
		error.println ("Info:  Fetching data from gdb");
		try
		{
			close();
			
			Connection convertCon = null;
			Connection conGdb = null;
			
			//Connect to GenMAPP gdb
			final String database_after = ";DriverID=22;READONLY=true";
			final String database_before =
				"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
			try {
				Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				conGdb = DriverManager.getConnection(
						database_before + gmGdbFile.toString() + database_after, "", "");
			} catch (Exception e) {
				error.println("Error: " +e.getMessage());
//				e.printStackTrace();
			}
			
			//Create hsqldb gdb
			// remove old property file
			error.println("Deleted old hsqldb file: " + gdbFile.delete());
			Properties prop = new Properties();
			prop.setProperty("user","sa");
			prop.setProperty("password","");
			try
			{
				String file = gdbFile.getAbsolutePath().toString();
				Class.forName("org.hsqldb.jdbcDriver");
				convertCon = DriverManager.getConnection("jdbc:hsqldb:file:" + 
						file.substring(0, file.lastIndexOf(".")) + ";shutdown=true", prop);
			}
			catch (Exception e)
			{
				error.println("Error: " + e.getMessage());
//				e.printStackTrace();
			}
			
			// Fetch size of database to convert (for progress monitor)
			Statement s = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet r = s.executeQuery("SELECT COUNT(*) FROM relations");
			int nrRelations = 1;
			if(r.next()) {
				nrRelations = r.getInt(1);
				error.println("nrRelations " + nrRelations);
			}
			r = s.executeQuery("SELECT COUNT(*) FROM Systems");
			int nrSystems = 1;
			if(r.next()) {
				nrSystems = r.getInt(1);
				error.println("nrSystems " + nrSystems);
			}
			
			// Create tables
			createTables(convertCon);
			
			// Fill link table
			s = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			r = s.executeQuery(
					"SELECT * FROM relations"
			);
			PreparedStatement pstmt = convertCon.prepareStatement(
					"INSERT INTO link				" +
					"	(idLeft, codeLeft,	 		" + 
					"	 idRight, codeRight,	 	" +
					"	bridge)						" +
			"VALUES	(?, ?, ?, ?, ?)			");
			
			while (r.next())
			{		
				String codeLeft = r.getString("SystemCode");
				String codeRight = r.getString("RelatedCode");
				String tableName = r.getString("Relation");
				
				if(codeLeft.equalsIgnoreCase("En")) // Only process link table if idLeft is Ensembl
					// This may lead to data loss, but because future databases are based solely on
					// Ensembl this should not be a problem
				{
					Statement sFetch = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					ResultSet rFetch = sFetch.executeQuery("SELECT * FROM `" + tableName + "`");
					
					error.println ("Debug: working on table " + tableName);
					while (rFetch.next())
					{	
						// Check if the thread is interrupted
						if(convertThread.isInterrupted) {
							return;
						}
						String idLeft = rFetch.getString ("Primary");
						String idRight = rFetch.getString ("Related");
						String bridge = rFetch.getString ("Bridge");
						
						pstmt.setString (1, idLeft);
						pstmt.setString (2, codeLeft);
						pstmt.setString (3, idRight);
						pstmt.setString (4, codeRight);
						pstmt.setString (5, bridge);
						
						try
						{
							pstmt.execute();
						}
						catch (SQLException e)
						{
							error.println("Error: " + e.getMessage() + "at " + idLeft + ", " + idRight);
							//e.printStackTrace();
						}
					}
				}
				// Update progress monitor
				convertThread.progress += 20.0/nrRelations;
			}
			
			// Fill gene table
			// Get the table names containing gene information
			r = s.executeQuery(
					"SELECT System, SystemCode FROM Systems"
			);
			
			pstmt = convertCon.prepareStatement(
					"INSERT INTO gene " +
					"	(id, code," +
					"	 backpageText, name)" +
			"VALUES (?, ?, ?, ?)");
			
			// Process every system table
			while(r.next()) {
				Statement tms = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				String systemTable = r.getString("System");
				String systemCode = r.getString("SystemCode");
				error.println ("Debug: working on table " + systemTable);
				
				// Check if table exists
				ResultSet tmr = conGdb.getMetaData().getTables(null, null, systemTable, null);
				if(tmr.next()) {
					ResultSet str = tms.executeQuery("SELECT * FROM " + systemTable);
					while(str.next()) {
						// Check if the thread is interrupted
						if(convertThread.isInterrupted) {
							return;
						}
						try {
							// Column ID is gene id
							String id = str.getString("ID");
							String name = "";
							try {	name = str.getString("Symbol"); } 
							catch (Exception e) {  try { name = str.getString("Name"); }
							catch (Exception e1) { } }
							// All further columns are backpage text
							String bpText = "<TABLE border='1'>";
							ResultSetMetaData strm = str.getMetaData();
							for(int i = 1; i < strm.getColumnCount(); i++) {
								try {
									String colName = strm.getColumnName(i);
									String colVal = str.getString(colName);
									bpText = bpText + "<TR><TH>" +
									colName + "<TH>" + colVal;
								} catch (SQLException e) {
									error.println ("Error: " + e.getMessage() + " at: " + bpText);
									//e.printStackTrace();
								}
							}
							bpText = bpText + "</TABLE>";
							//System.out.println(id + "\t" + systemCode + "\t");
							pstmt.setString(1, id);
							pstmt.setString(2, systemCode);
							pstmt.setString(3, bpText);
							pstmt.setString(4, name);
							pstmt.execute();
						} catch (SQLException e) {
							error.println ("Error: " + e.getMessage());
							//e.printStackTrace();
						}
					}
				}
				// Update progress monitor
				convertThread.progress += 80.0/nrSystems;
			}
			
			//Close connections
			error.println("Closing connections");
			Statement sh = convertCon.createStatement();
			sh.executeQuery("SHUTDOWN COMPACT");
			sh.close();
			conGdb.close();
			convertCon.close();
			
			//Set readonly to true
			prop = new Properties();
			prop.load(new FileInputStream(convertGdbFile));
			prop.setProperty("readonly","true");
			prop.store(new FileOutputStream(convertGdbFile), "HSQL Database Engine");
			
			if(gdbFile != null)
			{
				connect(gdbFile);
			}
			convertThread.progress = 100;
		}
		catch (Exception e)
		{
			error.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	ConvertThread convertThread;
	public File convertGmGdbFile;
	public File convertGdbFile;
	public class ConvertThread extends Thread
	{
		volatile double progress;
		volatile boolean isInterrupted;
		
		public ConvertThread() 
		{
			isInterrupted = false;
		}
		
		public void run()
		{
			progress = 0;
			convertGdb(convertGmGdbFile, convertGdbFile);
		}
		
		public void interrupt()
		{
			isInterrupted = true;
		}
	}
	
	public IRunnableWithProgress convertRunnable = new IRunnableWithProgress() {		
		public void run(IProgressMonitor monitor) {
			monitor.beginTask("Converting Gene Database",100);
			convertThread = new ConvertThread();
			convertThread.start();
			int prevProgress = 0;
			while(convertThread.progress < 100) {
				if(monitor.isCanceled()) {
					convertThread.interrupt();
					break;
				}
				if(prevProgress < (int)convertThread.progress) {
					monitor.worked((int)convertThread.progress - prevProgress);
					prevProgress = (int)convertThread.progress;
				}
			}
			monitor.done();
		}
	};
	
	public void createTables(Connection convertCon) {
		System.out.println ("Info:  Creating tables");
		
		try {
			Statement sh = convertCon.createStatement();
			sh.execute("DROP TABLE link IF EXISTS");
			sh.execute("DROP TABLE gene IF EXISTS");
		} catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
		}
		try
		{
			Statement sh = convertCon.createStatement();
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
					"	  name VARCHAR(50),					" +
					"     PRIMARY KEY (id, code)			" +
			" )										");
			sh.execute(
					"CREATE INDEX i_code" +
					" ON gene(code)"
					);
			sh.execute(
					"CREATE INDEX i_name" +
					" ON gene(name)"
					);
			
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
