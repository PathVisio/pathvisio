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
package data;

import gmmlVision.GmmlVision;

import java.io.File;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import preferences.GmmlPreferences;
import debug.StopWatch;

/**
 * This class handles everything related to the Gene Database. It contains the database connection,
 * several methods to query data from the gene database and methods to convert a GenMAPP gene database
 * to hsqldb format
 */
public abstract class GmmlGdb {	
	private static final int COMPAT_VERSION = 1;
	
	/**
	 * The {@link Connection} to the Gene Database
	 */
	private static Connection con;
	
	/**
	 * Gets the Connection to the Gene Database
	 */
	public static Connection getCon() { return con; }
	/**
	 * Check whether a connection to the database exists
	 * @return	true is a connection exists, false if not
	 */
	public static boolean isConnected() { return con != null; }
	
	private static String dbName;
	/**
	 * Gets the name of te currently used gene database
	 * @return the database name as specified in the connection string
	 */
	public static String getDbName() { return dbName; }
	
	/**
	 * Initiates this class. Checks the properties file for a previously
	 * used Gene Database and tries to open a connection if found.
	 */
	public static void init()
	{
		String currGdb = GmmlVision.getPreferences().getString(GmmlPreferences.PREF_CURR_GDB);
		if(!currGdb.equals("") && !GmmlVision.getPreferences().isDefault(GmmlPreferences.PREF_CURR_GDB))
		{
			dbName = currGdb;
			try {
				connect(null);
			} catch(Exception e) {
				setCurrentGdb(GmmlVision.getPreferences().getDefaultString(GmmlPreferences.PREF_CURR_GDB));
			}
		}
	}
	
	/**
	 * Sets the Gene Database that is currently in use
	 * @param dbNm	The name of the gene database
	 */
	public static void setCurrentGdb(String dbNm) {
		dbName = dbNm;
		GmmlVision.getPreferences().setValue(GmmlPreferences.PREF_CURR_GDB, dbNm);
		try { GmmlVision.getPreferences().save(); } 
		catch(Exception e) { GmmlVision.log.error("Unable to save preferences", e); } 
	}
	
	/**
	 * <TABLE border='1'><TR><TH>Gene ID:<TH>g4507224_3p_at<TR><TH>Gene Name:<TH>SRY<TR><TH>Description:<TH>Sex-determining region Y protein (Testis-determining factor). [Source:Uniprot/SWISSPROT;Acc:Q05066]<TR><TH>Secondary id:<TH>g4507224_3p_at<TR><TH>Systemcode:<TH>X<TR><TH>System name:<TH>Affymetrix Probe Set ID<TR><TH>Database name (Ensembl):<TH>Affymx Microarray U133</TABLE>
	 * @param id The gene id to get the symbol info for
	 * @param code systemcode of the gene identifier
	 * @return The gene symbol, or null if the symbol could not be found
	 */
	public static String getGeneSymbol(String id, String code) {
		String bpInfo = getBpInfo(id, code);
		return bpInfo == null ? null : parseGeneSymbol(bpInfo);
		
	}
	
	/**
	 * Parses the gene symbol from the backpage info
	 * @param bpInfo The backpage info (as obtained from {@link #getBpInfo(String, String)})
	 * @return The parsed gene symbol, or null if no symbol could be found
	 */
	public static String parseGeneSymbol(String bpInfo) {
		Pattern regex = Pattern.compile("<TH>Gene Name:<TH>(.+?)<TR>");
		Matcher matcher = regex.matcher(bpInfo);
		if(matcher.find())
			return matcher.group(1);
		else
			return null;
	}
	
	/**
	 * Gets the backpage info for the given gene id for display on GmmlBpBrowser
	 * @param id The gene id to get the backpage info for
	 * @param code systemcode of the gene identifier
	 * @return String with the backpage info, null if the gene was not found
	 */
	public static String getBpInfo(String id, String code) {
		StopWatch timer = new StopWatch();
		timer.start();
		
		try {
			Statement s = con.createStatement();
			ResultSet r = s.executeQuery("SELECT backpageText FROM gene " +
					"WHERE id = '" + id + "' AND code = '" + code + "'");
			r.next();
			String result = r.getString(1);
			
			timer.stopToLog("> getBpInfo");
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
	
//	static PreparedStatement pstEnsId2Refs;
	/**
	 * Get all cross references (ids from every system representing 
	 * the same gene as the given id) for a given Ensembl id
	 * @param ensId		The Ensembl id to get the cross references for
	 * @return			List containing all cross references found for this Ensembl id
	 * (empty if nothing found)
	 */	
	public static ArrayList<IdCodePair> ensId2Refs(String ensId) {
		StopWatch timer = new StopWatch();
		timer.start();
		
		ArrayList<IdCodePair> crossIds = new ArrayList<IdCodePair>();
		try {
//			if(pstEnsId2Refs == null) {
//				pstEnsId2Refs = getCon().prepareStatement(
//						"SELECT idRight, codeRight FROM link " +
//						"WHERE idLeft = ?"
//				);
//			}
//			pstEnsId2Refs.setString(1, ensId);
//			ResultSet r1 = pstEnsId2Refs.executeQuery();
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight, codeRight FROM link " +
					"WHERE idLeft = '" + ensId + "'");
			while(r1.next()) {
				crossIds.add(new IdCodePair(r1.getString(1), r1.getString(2)));
			}
		} catch(Exception e) {
			GmmlVision.log.error("Unable to get cross references for ensembl gene " +
					"'" + ensId + "'", e);
		}
		
		timer.stopToLog("> endId2Refs: (" + ensId + ")");
		return crossIds;
	}
	
//	static PreparedStatement pstRef2EnsIds;
	/**
	 * Get all Ensembl ids representing the same gene as the given gene id (from any system)
	 * @param ref	The gene id to get the Ensembl ids for
	 * @param code	systemcode of the gene identifier
	 * @return		ArrayList containing all Ensembl ids found for this gene id
	 * (empty if nothing found)
	 */
	public static ArrayList<String> ref2EnsIds(String ref, String code)
	{	
		StopWatch timer = new StopWatch();
		timer.start();
		
		ArrayList<String> ensIds = new ArrayList<String>();
		try {
//			if(pstRef2EnsIds == null) {
//				pstRef2EnsIds = getCon().prepareStatement(
//						"SELECT idLeft FROM link " +
//						"WHERE idRight = ? AND codeRight = ?"
//				);
//			}
//			pstRef2EnsIds.setString(1, ref);
//			pstRef2EnsIds.setString(2, code);
//			ResultSet r1 = pstRef2EnsIds.executeQuery();
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + ref + "' AND codeRight = '" + code + "'");
			while(r1.next()) {
				ensIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			GmmlVision.log.error("Unable to get ensembl genes for ensembl gene " +
					"'" + ref + "' with systemcode '" + code + "'", e);
		}
		
		timer.stopToLog("> ref2EnsIds (" + ref + "," + code + ")");
		return ensIds;
	}
	
	public static List<IdCodePair> getCrossRefs(IdCodePair idc) {
		GmmlVision.log.trace("Fetching cross references");
		StopWatch timer = new StopWatch();
		timer.start();
		
		ArrayList<IdCodePair> refs = new ArrayList<IdCodePair>();
		ArrayList<String> ensIds = ref2EnsIds(idc.getId(), idc.getCode());
		for(String ensId : ensIds) refs.addAll(ensId2Refs(ensId));

		GmmlVision.log.trace("END Fetching cross references for " + idc + "; time:\t" + timer.stop());
		return refs;
	}
	
	static boolean SINGLE_QUERY = false;
	/**
	 * Get all cross references (ids from every system representing 
	 * the same gene as the given id) for a given id
	 * @param id	gene identifier to get the cross references for
	 * @param code	systemcode of the gene identifier
	 * @return list of crossrefs
	 */
	public static List<IdCodePair> getCrossRefs(String id, String code) {
		if(SINGLE_QUERY)
			return getCrossRefs1Query(id, code);
		else
			return getCrossRefs(new IdCodePair(id, code));
	}
	
//	static PreparedStatement pstCrossRefs1Query;
	/**
	 * Get all cross references (ids from every system representing 
	 * the same gene as the given id) for a given id (from any system) using a
	 * single SQL query
	 * NOTE: Don't use this due to performance reasons. Hsqldb seems to have
	 * trouble with more complicated select statements like this. Using multiple 
	 * simple select statements showed to be much faster, so use getCrossRefs instead
	 * @param id	gene identifier to get the cross references for
	 * @param code	systemcode of the gene identifier
	 * @return list of IdCodePair-s
	 */
//	Don't use this, multiple simple select queries is faster
//	Use getCrossRefs instead
	public static List<IdCodePair> getCrossRefs1Query(String id, String code) {	
		StopWatch timer = new StopWatch();
		timer.start();
		
		List<IdCodePair> crossIds = new ArrayList<IdCodePair>();
		try {			
//			if(pstCrossRefs1Query == null) {
//				pstCrossRefs1Query = getCon().prepareStatement(
//						"SELECT idRight, codeRight FROM link " +
//						"WHERE idLeft IN ( " +
//						"SELECT idLeft FROM link " +
//						"WHERE codeRight = ? AND idRight = ? )"
//				);
//			}
//			pstCrossRefs1Query.setString(1, code);
//			pstCrossRefs1Query.setString(2, id);
//			ResultSet r1 = pstCrossRefs1Query.executeQuery();
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight, codeRight FROM link " +
					"WHERE idLeft IN ( " +
					"SELECT idLeft FROM link " +
					"WHERE codeRight = '" + code + "' AND idRight = '" + id + "' )");
			while(r1.next()) {
				String rid = r1.getString(1);
				String rcode = r1.getString(2);
				crossIds.add(new IdCodePair(rid, rcode));
			}
		} catch(Exception e) {
			GmmlVision.log.error("Unable to get cross references for gene " +
					"'" + id + ", with systemcode '" + code + "'", e);
		}
		
		GmmlVision.log.trace("\t> getCrossRefs1Query:\t" + timer.stop());
		return crossIds;
	}
	
	/**
	 * Class to store an id/code combination, which represents
	 * an unique gene product
	 */
	public static class IdCodePair implements Comparable {
		String id;
		String code;
		
		public IdCodePair(String id, String code) {
			this.id = id;
			this.code = code;
		}
		
		public String getCode() { return code; }
		public String getId() { return id; }
		
		public String getName() { return code + ":" + id; }
		public String toString() { return getName(); }
		
		public int hashCode() {
			return getName().hashCode();
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof IdCodePair)) return false;
			IdCodePair idc = (IdCodePair)o;
			return idc.getId().equals(getId()) && idc.getCode().equals(getCode());
		}

		public int compareTo(Object o) {
			IdCodePair idc = (IdCodePair)o;
			return getName().compareTo(idc.getName());
		}
		
		public boolean valid() {
			return code.length() > 0 && id.length() > 0;
		}

		public String getDatabaseName() {
			String name = MappFormat.sysCode2Name.get(getCode());
			return name == null ? getCode() : name;
		}
	}
	
	public static DBConnector getDBConnector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return GmmlVision.getDbConnector(DBConnector.TYPE_GDB);
	}
	
	/**
	 * Opens a {@link Connection} to the Gene Database located in the given file
	 * @param dbName The file containing the Gene Database. This file needs to be the
	 * .properties file of the Hsqldb database
	 */
	public static void connect(String dbName) throws Exception
	{
		if(dbName == null) dbName = getDbName();
		
		DBConnector connector = getDBConnector();
		con = connector.createConnection(dbName);
		con.setReadOnly(true);
//		Utils.checkDbVersion(con, COMPAT_VERSION); NOT FOR NOW
		setCurrentGdb(dbName);
	}
	
	/**
	 * Closes the {@link Connection} to the Gene Database if possible
	 */
	public static void close() 
	{
		if(con != null) {
			try {
				DBConnector connector = getDBConnector();
				connector.closeConnection(con);
			} catch(Exception e) {
				GmmlVision.log.error("Unable to close database connection", e);
			}
		}
	}
		
	/**
	 * Converts the given GenMAPP Gene Database to a Gene Database as used in this program
	 * <BR>This method reports all errors occured during the conversion to a file named 'convert_gdb_log.txt'
	 * @param gmGdbFile		The file containing the GenMAPP Gene Database to be converted
	 * @param dbName		The file where the new Gene Database has to be stored (the .properties
	 * file of the database)
	 */
	public static void convertGdb(File gmGdbFile, String dbName) {

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
			
			DBConnector connector = null;
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
			connector = getDBConnector();
			convertCon = connector.createConnection(dbName, DBConnector.PROP_RECREATE);
			
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
			connector.closeConnection(convertCon);
						
			if(dbName != null)
			{
				connect(dbName);
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
	private static String convertDbName;
	/**
	 * Set the GenMAPP Gene database file to convert from
	 * @param file
	 */
	public static void setConvertGmGdbFile(File file) { convertGmGdbFile = file; }
	/**
	 * Set the Gene database name to convert to
	 * @param name
	 */
	public static void setConvertGdbName(String name) { convertDbName = name; }
	
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
			convertGdb(convertGmGdbFile, convertDbName);
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
			sh.execute("DROP TABLE info");
			sh.execute("DROP TABLE link");
			sh.execute("DROP TABLE gene");
		} catch(Exception e) {
			GmmlVision.log.error("Unable to drop gdb tables: "+e.getMessage(), e);
		}
		try
		{
			Statement sh = convertCon.createStatement();
			sh.execute(
					"CREATE TABLE					" +
					"		info							" +
					"(	  version INTEGER PRIMARY KEY		" +
					")");
			sh.execute( //Add compatibility version of GDB
					"INSERT INTO version VALUES ( " + COMPAT_VERSION + ")");
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
			sh.execute(
					"CREATE TABLE							" +
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
