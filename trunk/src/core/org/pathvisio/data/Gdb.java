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
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.Utils;

/**
 * This class handles everything related to the Gene Database. It contains the database connection,
 * several methods to query data from the gene database and methods to convert a GenMAPP gene database
 * to hsqldb format
 */
public class Gdb 
{	
	// private, so you can't instantiate Gdb. Use the connect() method. 
	private Gdb()
	{
	}
	
	static 
	{
		initializeHeader();
	}
	
	static private Gdb currentGdb = null;
	
	static public Gdb getCurrentGdb ()
	{
		return currentGdb;
	}
	
	private static String table_DataNode = "datanode";
	
	private static final int COMPAT_VERSION = 2; //Preferred schema version
	
	/**
	 * The {@link Connection} to the Gene Database
	 */
	private Connection con;
	
	/**
	 * Gets the Connection to the Gene Database
	 * @deprecated Should be private.
	 */
	public Connection getCon() { return con; }
	/**
	 * Check whether a connection to the database exists
	 * @return	true is a connection exists, false if not
	 */
	public boolean isConnected() { return con != null; }
	
	private String dbName;
	/**
	 * Gets the name of te currently used gene database
	 * @return the database name as specified in the connection string
	 */
	public String getDbName() { return dbName; }
	
	/**
	 * Initiates this class. Checks the properties file for a previously
	 * used Gene Database and tries to open a connection if found.
	 */
	public static void init()
	{
		String currGdb = GlobalPreference.DB_GDB_CURRENT.getValue();
		if(!currGdb.equals("") && !GlobalPreference.isDefault(GlobalPreference.DB_GDB_CURRENT))
		{
			try {
				connect(currGdb);
			} 
			catch(Exception e) 
			{
				Logger.log.error("Setting previous Gdb failed.", e);
				try {
					connect(currGdb);
				} 
				catch(Exception f) 
				{
					Logger.log.error("Setting default Gdb failed.", f);
				}
			}
		}
	}
	
	/**
	 * <TABLE border='1'><TR><TH>Gene ID:<TH>g4507224_3p_at<TR><TH>Gene Name:<TH>SRY<TR><TH>Description:<TH>Sex-determining region Y protein (Testis-determining factor). [Source:Uniprot/SWISSPROT;Acc:Q05066]<TR><TH>Secondary id:<TH>g4507224_3p_at<TR><TH>Systemcode:<TH>X<TR><TH>System name:<TH>Affymetrix Probe Set ID<TR><TH>Database name (Ensembl):<TH>Affymx Microarray U133</TABLE>
	 * @param id The gene id to get the symbol info for
	 * @param code systemcode of the gene identifier
	 * @return The gene symbol, or null if the symbol could not be found
	 */
	public String getGeneSymbol(Xref ref) 
	{
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
	public String getBpInfo(Xref ref) {
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
	
	public String getBackpageHTML(Xref ref, String bpHead) {
		String text = backpagePanelHeader == null ? "" : backpagePanelHeader;
		if( ref == null || ref.getId() == null || ref.getDataSource() == null) return text;
		
		if (bpHead == null) bpHead = "";
		text += "<H1>Gene information</H1><P>";
		text += bpHead.equals("") ? bpHead : "<H2>" + bpHead + "</H2><P>";
		String bpInfo = getBpInfo(ref);
		text += bpInfo == null ? "<I>No gene information found</I>" : bpInfo;

		text += getCrossRefText(ref);

		return text + "</body></html>";
	}

	private static BackpageTextProvider backpageTextProvider = new BackpageTextProvider();
	
	public static BackpageTextProvider getBackpageTextProvider() {
		return backpageTextProvider;
	}
	
	private String getCrossRefText(Xref ref) 
	{
		List<Xref> crfs = getCrossRefs(ref);
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
	 * 
	 * @deprecated Use getCrossRefs instead
	 */	
	public ArrayList<Xref> ensId2Refs(String ensId, DataSource resultDs) 
	{
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
			if(resultDs != null) {
				codeLimit = " AND codeRight = '" + resultDs.getSystemCode() + "'";
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
	 * 
	 * @deprecated use getCrossRefs instead
	 */
	public ArrayList<String> ref2EnsIds(Xref xref)
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
	public List<Xref> getCrossRefs(Xref idc) 
	{
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
	public ArrayList<Xref> getCrossRefs (Xref idc, DataSource resultDs) 
	{
		Logger.log.trace("Fetching cross references");
		StopWatch timer = new StopWatch();
		timer.start();
		
		ArrayList<Xref> refs = new ArrayList<Xref>();
		ArrayList<String> ensIds = ref2EnsIds(idc);
		for(String ensId : ensIds) refs.addAll(ensId2Refs(ensId, resultDs));

		Logger.log.trace("END Fetching cross references for " + idc + "; time:\t" + timer.stop());
		return refs;
	}
			
	private static DBConnector getDBConnector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return Engine.getCurrent().getDbConnector(DBConnector.TYPE_GDB);
	}
	
	/**
	 * Opens a {@link Connection} to the Gene Database located in the given file
	 * @param dbName The file containing the Gene Database. This file needs to be the
	 * .properties file of the Hsqldb database
	 */
	public static void connect(String dbName) throws Exception
	{
		Gdb gdb = new Gdb();
		if(dbName == null) throw new NullPointerException();
		
		Logger.log.trace("Opening connection to Gene Database " + dbName);
		DBConnector connector = getDBConnector();
		gdb.con = connector.createConnection(dbName);
		gdb.con.setReadOnly(true);
		gdb.checkSchemaVersion();
		currentGdb = gdb;
		GlobalPreference.DB_GDB_CURRENT.setValue(dbName);
		ApplicationEvent e =
			new ApplicationEvent (Engine.getCurrent(), ApplicationEvent.GDB_CONNECTED);
		Engine.getCurrent().fireApplicationEvent (e);
		Logger.log.trace("Current Gene Database: " + dbName);
	}
	
	private void checkSchemaVersion() 
	{
		int version = 0;
		try 
		{
			ResultSet r = con.createStatement().executeQuery("SELECT schemaversion FROM info");
			if(r.next()) version = r.getInt(1);
		} 
		catch (Exception e) 
		{
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
	public void close() 
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
	 * Excecutes several SQL statements to create the tables and indexes in the database the given
	 * connection is connected to
	 * @param convertCon	The connection to the database the tables are created in
	 * Note: Official GDB's are created by AP, not with this code.
	 * This is just here for testing purposes.
	 */
	public void createTables(Connection convertCon) {
		Logger.log.trace("Info:  Creating tables");
		
		try 
		{
			Statement sh = convertCon.createStatement();
			sh.execute("DROP TABLE info");
			sh.execute("DROP TABLE link");
			sh.execute("DROP TABLE gene");
		} 
		catch(Exception e) 
		{
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
			
		} 
		catch (Exception e)
		{
			Logger.log.error("while creating gdb tables: " + e.getMessage(), e);
		}
	}
	
}
