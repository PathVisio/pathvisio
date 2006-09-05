package data;

import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * This class handles everything related to the Gene Database. It contains the database connection,
 * several methods to query data from the gene database and methods to convert a GenMAPP gene database
 * to hsqldb format
 */
public abstract class GmmlGdb {	
	/**
	 * The {@link Connection} to the Gene Database
	 */
	private static Connection con;
	
	/**
	 * Gets the {{@link Connection} to the Gene Database
	 * @return
	 */
	public static Connection getCon() { return con; }
	/**
	 * Check whether a connection to the database exists
	 * @return	true is a connection exists, false if not
	 */
	public static boolean isConnected() { return con != null; }
	
	/**
	 * {@link File} pointing to the current Gene Database (.properties file of the Hsql database)
	 */
	private static File gdbFile;
	/**
	 * Gets the private property gdbFile
	 * @return {@File} object that points to the file containing the Gene Database
	 */
	public static File getGdbFile() { return gdbFile; }
	
	/**
	 * Initiates this class. Checks the properties file for a previously
	 * used Gene Database and tries to open a connection if found.
	 */
	public static void init()
	{
		String currGdb = GmmlVision.getPreferences().getString("currentGdb");
		if(!currGdb.equals("") && !GmmlVision.getPreferences().isDefault("currentGdb"))
		{
			gdbFile = new File(currGdb);
			try {
				connect(null);
			} catch(Exception e) {
				setCurrentGdb(GmmlVision.getPreferences().getDefaultString("currentGdb"));
			}
		}
	}
	
	/**
	 * Sets the Gene Database that is currently in use
	 * @param gdb	The name of the gene database
	 */
	public static void setCurrentGdb(String gdb) {
		gdbFile = new File(gdb);
		GmmlVision.getPreferences().setValue("currentGdb", gdb);
		try { GmmlVision.getPreferences().save(); } 
		catch(Exception e) { GmmlVision.log.error("Unable to save preferences", e); } 
	}
		
	/**
	 * Gets the backpage info for the given gene id for display on {@GmmlBpBrowser}
	 * @param id	The gene id to get the backpage info for
	 * @param code	systemcode of the gene identifier
	 * @return		String with the backpage info, null if the gene was not found
	 */
	public static String getBpInfo(String id, String code) {
		try {
			Statement s = con.createStatement();
			ResultSet r = s.executeQuery("SELECT backpageText FROM gene " +
					"WHERE id = '" + id + "' AND code = '" + code + "'");
			r.next();
			String result = r.getString(1);
			return result;
		} catch(Exception e) { return null;	} //Gene not found
	}
	
	/**
	 * Checks whether the given gene exists in the gene database
	 * @param id
	 * @param code
	 * @return	true if the gene exists, false if not
	 */
	public static boolean hasGene(String id, String code)
	{
		try {
			ResultSet r = con.createStatement().executeQuery(
					"SELECT COUNT(*) FROM gene WHERE " +
					"id = '" + id + "' AND code = '" + code +"'"
			);
			r.next();
			return r.getInt(1) > 0 ? true : false;
		} catch(Exception e) { return false; }
	}
	/**
	 * Get all cross references (ids from every system representing 
	 * the same gene as the given id) for a given Ensembl id
	 * @param ensId		The Ensembl id to get the cross references for
	 * @return			{@ArrayList} containing all cross references found for this Ensembl id
	 * (empty if nothing found)
	 */
	public static ArrayList<String> ensId2Refs(String ensId) {
		ArrayList<String> crossIds = new ArrayList<String>();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight FROM link " +
					"WHERE idLeft = '" + ensId + "'"
					);
			while(r1.next()) {
				crossIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			GmmlVision.log.error("Unable to get cross references for ensembl gene " +
					"'" + ensId + "'", e);
		}
		return crossIds;
	}
	
	/**
	 * Get all Ensembl ids representing the same gene as the given gene id (from any system)
	 * @param ref	The gene id to get the Ensembl ids for
	 * @param code	systemcode of the gene identifier
	 * @return		{@ArrayList} containing all Ensembl ids found for this gene id
	 * (empty if nothing found)
	 */
	public static ArrayList<String> ref2EnsIds(String ref, String code)
	{
		ArrayList<String> ensIds = new ArrayList<String>();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + ref + "' AND codeRight = '" + code + "'"
			);
			while(r1.next()) {
				ensIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			GmmlVision.log.error("Unable to get ensembl genes for ensembl gene " +
					"'" + ref + "' with systemcode '" + code + "'", e);
		}
		return ensIds;
	}
	
	/**
	 * Get all cross references (ids from every system representing 
	 * the same gene as the given id) for a given id
	 * @param id	gene identifier to get the cross references for
	 * @param code	systemcode of the gene identifier
	 * @return
	 */
	public static List<String> getCrossRefs(String id, String code) {
		ArrayList<String> refs = new ArrayList<String>();
		ArrayList<String> ensIds = ref2EnsIds(id, code);
		for(String ensId : ensIds) refs.addAll(ensId2Refs(ensId));
		return refs;
	}
	/**
	 * Get all cross references (ids from every system representing 
	 * the same gene as the given id) for a given id (from any system) using a
	 * single SQL query
	 * NOTE: Don't use this due to performance reasons. Hsqldb seems to have
	 * trouble with more complicated select statements like this. Using multiple 
	 * simple select statements showed to be much faster, so use getCrossRefs instead
	 * @param id	gene identifier to get the cross references for
	 * @param code	systemcode of the gene identifier
	 * @return
	 */
//	Don't use this, multiple simple select queries is faster
//	Use getCrossRefs instead
	public static List<String> getCrossRefs1Query(String id, String code) {
		List<String> crossIds = new ArrayList<String>();
		try {
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight FROM link " +
					"WHERE codeRight = '" + code + "' AND " +
							"idLeft IN ( 		  " +
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + id + "')"
					);
			while(r1.next()) {
				crossIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			GmmlVision.log.error("Unable to get cross references for gene " +
					"'" + id + ", with systemcode '" + code + "'", e);
		}
		return crossIds;
	}
	
	/**
	 * Opens a {@link Connection} to the Gene Database located in the given file
	 * @param gdbFile	The file containing the Gene Database. This file needs to be the
	 * .properties file of the Hsqldb database
	 * @return	null if the connection was created, a String with an error message if an error occured
	 */
	public static void connect(File gdbFile) throws Exception
	{
		if(gdbFile == null) gdbFile = GmmlGdb.gdbFile;
		if(!gdbFile.canRead()) throw new Exception("Can't access file '" + gdbFile.toString() + "'");
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
	}
	
	/**
	 * Closes the {@link Connection} to the Gene Database if possible
	 */
	public static void close()
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
				GmmlVision.log.error("while shutting down gdb: " +e.getMessage(), e);
			}
		}
	}
	
	
	/**
	 * Converts the given GenMAPP Gene Database to a Hsqldb Gene Database as used in this program
	 * <BR>This method reports all errors occured during the conversion to a file named 'convert_gdb_log.txt'
	 * @param gmGdbFile		The file containing the GenMAPP Gene Database to be converted
	 * @param gdbFile		The file where the new Hsqldb Gene Database has to be stored (the .properties
	 * file of the Hsqldb database)
	 */
	public static void convertGdb(File gmGdbFile, File gdbFile) {

		PrintWriter error = null;
	    try {
	        error = new PrintWriter(new FileWriter("convert_gdb_log.txt"));
	    } catch(IOException ex) {
	    	GmmlVision.log.error("Unable to open error file: " + ex.getMessage(), ex);
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
					"	 backpageText)" +
			"VALUES (?, ?, ?)");
			
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
								}
							}
							bpText = bpText + "</TABLE>";
							//System.out.println(id + "\t" + systemCode + "\t");
							pstmt.setString(1, id);
							pstmt.setString(2, systemCode);
							pstmt.setString(3, bpText);
							pstmt.execute();
						} catch (SQLException e) {
							error.println ("Error: " + e.getMessage());
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
		}
	}
	
	/**
	 * {@link ConvertThread} for conversion of the GenMAPP Gene Database
	 * @see {@link convertGdb}
	 */
	private static ConvertThread convertThread;
	
	private static File convertGmGdbFile;
	private static File convertGdbFile;
	/**
	 * Set the GenMAPP Gene database file to convert from
	 * @param file
	 */
	public static void setConvertGmGdbFile(File file) { convertGmGdbFile = file; }
	/**
	 * Set the Gene database file to convert to
	 * @param file
	 */
	public static void setConvertGdbFile(File file) { convertGdbFile = file; }
	
	/**
	 * This class is a {@link Thread} that converts a GenMAPP Gene Database and keeps the progress
	 * of the conversion
	 */
	public static class ConvertThread extends Thread
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
	
	/**
	 * Gets the {@link IRunnableWithProgress} for starting the {@link ConvertThread} 
	 * and monitor the progress of the conversion
	 */
	public static IRunnableWithProgress getConvertRunnable() { return convertRunnable; }
	
	private static IRunnableWithProgress convertRunnable = new IRunnableWithProgress() {		
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
	
	/**
	 * Excecutes several SQL statements to create the tables and indexes in the database the given
	 * connection is connected to
	 * @param convertCon	The connection to the database the tables are created in
	 */
	public static void createTables(Connection convertCon) {
		GmmlVision.log.trace("Info:  Creating tables");
		
		try {
			Statement sh = convertCon.createStatement();
			sh.execute("DROP TABLE link IF EXISTS");
			sh.execute("DROP TABLE gene IF EXISTS");
		} catch(Exception e) {
			GmmlVision.log.error("Unable to drop gdb tables: "+e.getMessage(), e);
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
					"     PRIMARY KEY (id, code)			" +
			" )										");
			sh.execute(
					"CREATE INDEX i_code" +
					" ON gene(code)"
					);
			
		} catch (Exception e)
		{
			GmmlVision.log.error("while creating gdb tables: " + e.getMessage(), e);
		}
	}
	
}
