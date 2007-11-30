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
package org.pathvisio.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.Xref;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.Utils;

/**
 * This class handles everything related to the Gene Database. It contains the database connection,
 * several methods to query data from the gene database and methods to convert a GenMAPP gene database
 * to hsqldb format
 */
public abstract class Gdb {	
	static {
		initializeHeader();
	}
	
	private static String table_DataNode = "datanode";
	
	private static final int COMPAT_VERSION = 2; //Preferred schema version
	
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
		String currGdb = GlobalPreference.DB_GDB_CURRENT.getValue();
		if(!currGdb.equals("") && !GlobalPreference.isDefault(GlobalPreference.DB_GDB_CURRENT))
		{
			dbName = currGdb;
			try {
				connect(null);
			} catch(Exception e) {
				setCurrentGdb(GlobalPreference.DB_GDB_CURRENT.getDefault());
			}
		}
	}
	
	/**
	 * Sets the Gene Database that is currently in use
	 * @param dbNm	The name of the gene database
	 */
	private static void setCurrentGdb(String dbNm) {
		dbName = dbNm; 
		GlobalPreference.DB_GDB_CURRENT.setValue(dbNm);
		ApplicationEvent e =
			new ApplicationEvent (Engine.getCurrent(), ApplicationEvent.GDB_CONNECTED);
		Engine.getCurrent().fireApplicationEvent (e);
	}
	
	/**
	 * <TABLE border='1'><TR><TH>Gene ID:<TH>g4507224_3p_at<TR><TH>Gene Name:<TH>SRY<TR><TH>Description:<TH>Sex-determining region Y protein (Testis-determining factor). [Source:Uniprot/SWISSPROT;Acc:Q05066]<TR><TH>Secondary id:<TH>g4507224_3p_at<TR><TH>Systemcode:<TH>X<TR><TH>System name:<TH>Affymetrix Probe Set ID<TR><TH>Database name (Ensembl):<TH>Affymx Microarray U133</TABLE>
	 * @param id The gene id to get the symbol info for
	 * @param code systemcode of the gene identifier
	 * @return The gene symbol, or null if the symbol could not be found
	 */
	public static String getGeneSymbol(Xref ref) {
		String bpInfo = getBpInfo(ref);
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
	 * Gets the backpage info for the given gene id for display on BackpagePanel
	 * @param id The gene id to get the backpage info for
	 * @param code systemcode of the gene identifier
	 * @return String with the backpage info, null if the gene was not found
	 */
	public static String getBpInfo(Xref ref) {
		StopWatch timer = new StopWatch();
		timer.start();
		
		try {
			Statement s = con.createStatement();
			ResultSet r = s.executeQuery(
					"SELECT backpageText FROM " + table_DataNode +
					" WHERE id = '" + ref.getId() + "' AND code = '" + 
					ref.getDataSource().getSystemCode() + "'");
			r.next();
			String result = r.getString(1);
			timer.stopToLog("> getBpInfo");
			return result;
		} catch(Exception e) { return null;	} //Gene not found
	}
	
	public static String getBackpageHTML(Xref ref, String bpHead) {
		String text = backpagePanelHeader == null ? "" : backpagePanelHeader;
		if( ref.getId() == null || ref.getDataSource() == null) return text;
		
		if (bpHead == null) bpHead = "";
		text += "<H1>Gene information</H1><P>";
		text += bpHead.equals("") ? bpHead : "<H2>" + bpHead + "</H2><P>";
		String bpInfo = Gdb.getBpInfo(ref);
		text += bpInfo == null ? "<I>No gene information found</I>" : bpInfo;

		text += getCrossRefText(ref);

		return text + "</body></html>";
	}

	private static BackpageTextProvider backpageTextProvider = new BackpageTextProvider();
	
	public static BackpageTextProvider getBackpageTextProvider() {
		return backpageTextProvider;
	}
	
	private static String getCrossRefText(Xref ref) 
	{
		List<Xref> crfs = Gdb.getCrossRefs(ref);
		if(crfs.size() == 0) return "";
		StringBuilder crt = new StringBuilder("<H1>Cross references</H1><P>");
		for(Xref cr : crfs) {
			String idtxt = cr.getId();
			String url = cr.getUrl();
			if(url != null) {
				int os = Utils.getOS();
				if(os == Utils.OS_WINDOWS) {
					//In windows: open in new browser window
					idtxt = "<a href='" + url + "' target='_blank'>" + idtxt + "</a>";
				} else {
					//This doesn't work under ubuntu, so no new windoe there
					idtxt = "<a href='" + url + "'>" + idtxt + "</a>";
				}
				
			}
			String dbName = cr.getDataSource().getFullName();
			crt.append( idtxt + ", " + (dbName != null ? dbName : cr.getDataSource().getSystemCode()) + "<br>");
		}
		return crt.toString();
	}
		
	/**
	 * Directory containing HTML files needed to display the backpage information
	 */
	final static String BPDIR = "backpage";
	/**
	 * Header file, containing style information
	 */
	final static String HEADERFILE = "header.html";
	
	static String backpagePanelHeader;
	
	/**
	 * Reads the header of the HTML content displayed in the browser. This header is displayed in the
	 * file specified in the {@link HEADERFILE} field
	 */
	private static void initializeHeader() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(
						Engine.getCurrent().getResourceURL(BPDIR + "/" + HEADERFILE).openStream()));
			String line;
			backpagePanelHeader = "";
			while((line = input.readLine()) != null) {
				backpagePanelHeader += line.trim();
			}
		} catch (Exception e) {
			Logger.log.error("Unable to read header file for backpage browser: " + e.getMessage(), e);
		}
	}	
			
	/**
	 * Get all cross references (ids from every system representing 
	 * the same gene as the given id) for a given Ensembl id
	 * @param ensId		The Ensembl id to get the cross references for
	 * @param resultCode If specified (not null), limit the results by only taking
	 * references with database code
	 * @return			List containing all cross references found for this Ensembl id
	 * (empty if nothing found)
	 */	
	public static ArrayList<Xref> ensId2Refs(String ensId, String resultCode) {
		StopWatch timer = new StopWatch();
		timer.start();
		
		ArrayList<Xref> crossIds = new ArrayList<Xref>();
		try {
//			if(pstEnsId2Refs == null) {
//				pstEnsId2Refs = getCon().prepareStatement(
//						"SELECT idRight, codeRight FROM link " +
//						"WHERE idLeft = ?"
//				);
//			}
//			pstEnsId2Refs.setString(1, ensId);
//			ResultSet r1 = pstEnsId2Refs.executeQuery();
			String codeLimit = "";
			if(resultCode != null) {
				codeLimit = " AND codeRight = '" + resultCode + "'";
			}
			ResultSet r1 = con.createStatement().executeQuery(
					"SELECT idRight, codeRight FROM link " +
					"WHERE idLeft = '" + ensId + "'" + codeLimit);
			while(r1.next()) {
				crossIds.add(new Xref(r1.getString(1), DataSource.getBySystemCode(r1.getString(2))));
			}
		} catch(Exception e) {
			Logger.log.error("Unable to get cross references for ensembl gene " +
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
	public static ArrayList<String> ref2EnsIds(Xref xref)
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
					"WHERE idRight = '" + xref.getId() + 
					"' AND codeRight = '" + xref.getDataSource().getSystemCode() + "'");
			while(r1.next()) {
				ensIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			Logger.log.error("Unable to get ensembl genes for ensembl gene " +
					"'" + xref.getId() + "' with systemcode '" + 
					xref.getDataSource().getSystemCode() + "'", e);
		}
		
		timer.stopToLog("> ref2EnsIds (" + xref.getId() + "," + 
				xref.getDataSource().getSystemCode() + ")");
		return ensIds;
	}
	
	/**
	 * Get all cross-references for the given id/code pair, restricting the
	 * result to contain only references from database with the given system
	 * code
	 * @param idc The id/code pair to get the cross references for
	 * @return An {@link ArrayList} containing the cross references, or an empty
	 * ArrayList when no cross references could be found
	 */
	public static List<Xref> getCrossRefs(Xref idc) {
		return getCrossRefs(idc, null);
	}
	
	/**
	 * Get all cross-references for the given id/code pair, restricting the
	 * result to contain only references from database with the given system
	 * code
	 * @param idc The id/code pair to get the cross references for
	 * @param resultCode The system code to restrict the results to
	 * @return An {@link ArrayList} containing the cross references, or an empty
	 * ArrayList when no cross references could be found
	 */
	public static ArrayList<Xref> getCrossRefs(Xref idc, String resultCode) {
		Logger.log.trace("Fetching cross references");
		StopWatch timer = new StopWatch();
		timer.start();
		
		ArrayList<Xref> refs = new ArrayList<Xref>();
		ArrayList<String> ensIds = ref2EnsIds(idc);
		for(String ensId : ensIds) refs.addAll(ensId2Refs(ensId, resultCode));

		Logger.log.trace("END Fetching cross references for " + idc + "; time:\t" + timer.stop());
		return refs;
	}
			
	public static DBConnector getDBConnector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return Engine.getCurrent().getDbConnector(DBConnector.TYPE_GDB);
	}
	
	/**
	 * Opens a {@link Connection} to the Gene Database located in the given file
	 * @param dbName The file containing the Gene Database. This file needs to be the
	 * .properties file of the Hsqldb database
	 */
	public static void connect(String dbName) throws Exception
	{
		if(dbName == null) dbName = getDbName();
		
		Logger.log.trace("Opening connection to Gene Database " + dbName);
		DBConnector connector = getDBConnector();
		con = connector.createConnection(dbName);
		con.setReadOnly(true);
		checkSchemaVersion();
		setCurrentGdb(dbName);
		Logger.log.trace("Current Gene Database: " + dbName);
	}
	
	private static void checkSchemaVersion() {
		int version = 0;
		try {
			ResultSet r = con.createStatement().executeQuery("SELECT schemaversion FROM info");
			if(r.next()) version = r.getInt(1);
		} catch (Exception e) {
			//Ignore, older db's don't even have schema version
		}
		if(version < COMPAT_VERSION) {
			//Datanode table name is 'gene'
			table_DataNode = "gene";
		} else {
			table_DataNode = "datanode";
		}
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
				Logger.log.error("Unable to close database connection", e);
			}
		}
	}
		
	/**
	 * Converts the given GenMAPP Gene Database to a Gene Database as used in this program
	 * <BR>This method reports all errors occured during the conversion to a file named 'convert_gdb_log.txt'
	 * @param gmGdbFile		The file containing the GenMAPP Gene Database to be converted
	 * @param dbName		The file where the new Gene Database has to be stored (the .properties
	 * file of the database)
	 * @deprecated Conversion of GenMAPP Gene Databases is not supported anymore
	 */
	public static void convertGdb(File gmGdbFile, String dbName) {

		PrintWriter error = null;
	    try {
	        error = new PrintWriter(new FileWriter("convert_gdb_log.txt"));
	    } catch(IOException ex) {
	    	Logger.log.error("Unable to open error file: " + ex.getMessage(), ex);
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
	 * @deprecated Conversion of GenMAPP Gene Databases is not supported anymore
	 */
	private static ConvertThread convertThread;
	
	private static File convertGmGdbFile;
	private static String convertDbName;
	/**
	 * Set the GenMAPP Gene database file to convert from
	 *@deprecated Conversion of GenMAPP Gene Databases is not supported anymore
	 * @param file
	 */
	public static void setConvertGmGdbFile(File file) { convertGmGdbFile = file; }
	/**
	 * Set the Gene database name to convert to
	 * @deprecated Conversion of GenMAPP Gene Databases is not supported anymore
	 * @param name
	 */
	public static void setConvertGdbName(String name) { convertDbName = name; }
	
	/**
	 * This class is a {@link Thread} that converts a GenMAPP Gene Database and keeps the progress
	 * of the conversion
	 * @deprecated Conversion of GenMAPP Gene Databases is not supported anymore
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
	 * Excecutes several SQL statements to create the tables and indexes in the database the given
	 * connection is connected to
	 * @param convertCon	The connection to the database the tables are created in
	 * @deprecated Use AP's scripts to create GDB!
	 */
	public static void createTables(Connection convertCon) {
		Logger.log.trace("Info:  Creating tables");
		
		try {
			Statement sh = convertCon.createStatement();
			sh.execute("DROP TABLE info");
			sh.execute("DROP TABLE link");
			sh.execute("DROP TABLE gene");
		} catch(Exception e) {
			Logger.log.error("Unable to drop gdb tables: "+e.getMessage(), e);
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
			Logger.log.error("while creating gdb tables: " + e.getMessage(), e);
		}
	}
	
}
