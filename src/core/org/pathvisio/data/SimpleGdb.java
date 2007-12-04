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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.Xref;
import org.pathvisio.util.Utils;

/**
 * SimpleGdb is the main implementation of the Gdb interface,
 * for dealing with single SQL-based pgdb's.
 * It's responsible for creating and querying a single 
 * pgdb relational database through the JDBC interface.
 *  
 * It wraps SQL statements in methods, 
 * so the rest of the apps don't need to know the
 * details of the Database schema.
 * 
 * It delegates dealing with the differences between 
 * various RDBMS's (Derby, Hsqldb etc.)
 * to a DBConnector instance.
 * A correct DBConnector instance needs to be 
 * passed to the constructor of SimpleGdb. 
 * 
 * In the PathVisio GUI environment, use GdbManager
 * to create and connect one or two centralized Gdb's. 
 * This will also automatically
 * find the right DBConnector from the preferences.
 *  
 * In a head-less or test environment, you can bypass GdbManager
 * and use SimpleGdb directly 
 * to create or connect to one or more pgdb's of any type.
 */
public class SimpleGdb implements Gdb
{		
	private static final int GDB_COMPAT_VERSION = 2; //Preferred schema version

	static 
	{
		// load header resource, a html template for creating back-pages.
		initializeHeader();
	}
		
	// the name of this table is "datanode" starting from
	// schema v2. 
	// it is "gene" for older tables.
	private String table_DataNode = "datanode";
	
	/**
	 * The {@link Connection} to the Gene Database
	 */
	// SQL connection
	private Connection con;
	// dbConnector, helper class for dealing with RDBMS specifcs.
	private DBConnector dbConnector;
	
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
	private String parseGeneSymbol(String bpInfo) {
		Pattern regex = Pattern.compile("<TH>Gene Name:<TH>(.+?)<TR>");
		Matcher matcher = regex.matcher(bpInfo);
		if(matcher.find())
			return matcher.group(1);
		else
			return null;
	}
	
	/**
	 * Gets the backpage info for the given gene id for display on BackpagePanel
	 * @param ref The gene to get the backpage info for
	 * @return String with the backpage info, null if the gene was not found
	 */
	public String getBpInfo(Xref ref) 
	{
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
		
		String  bpInfo = getBpInfo (ref);
		text += bpInfo == null ? "<I>No gene information found</I>" : bpInfo;

		text += getCrossRefText(ref);		

		return text + "</body></html>";
	}

	private static BackpageTextProvider backpageTextProvider = new BackpageTextProvider();
	
	public static BackpageTextProvider getBackpageTextProvider() {
		return backpageTextProvider;
	}
	
	String getCrossRefText(Xref ref) 
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
	
	private static String backpagePanelHeader;
	
	static String getBackpagePanelHeader()
	{
		return backpagePanelHeader;
	}
	
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
	public List<Xref> ensId2Refs(String ensId, DataSource resultDs) 
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
	public List<String> ref2EnsIds(Xref xref)
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
	public List<Xref> getCrossRefs (Xref idc, DataSource resultDs) 
	{
		Logger.log.trace("Fetching cross references");
		StopWatch timer = new StopWatch();
		timer.start();
		
		List<Xref> refs = new ArrayList<Xref>();
		List<String> ensIds = ref2EnsIds(idc);
		for(String ensId : ensIds) refs.addAll(ensId2Refs(ensId, resultDs));

		Logger.log.trace("END Fetching cross references for " + idc + "; time:\t" + timer.stop());
		return refs;
	}
				
	/**
	 * Opens a connection to the Gene Database located in the given file
	 * @param dbName The file containing the Gene Database. 
	 * @param connector An instance of DBConnector, to determine the type of database (e.g. DataDerby)
	 */
	public SimpleGdb(String dbName, DBConnector connector, int props) throws DataException
	{
		if(dbName == null) throw new NullPointerException();
		
		this.dbName = dbName;
		this.dbConnector = connector;
		
		Logger.log.trace("Opening connection to Gene Database " + dbName);

		con = dbConnector.createConnection(dbName, props);
		try
		{
			con.setReadOnly(true);
		}
		catch (SQLException e)
		{
			throw new DataException (e);
		}
		checkSchemaVersion();
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
		if(version < GDB_COMPAT_VERSION) {
			//Datanode table name is 'gene'
			table_DataNode = "gene";
		} else {
			table_DataNode = "datanode";
		}
	}
	
	/**
	 * Closes the {@link Connection} to the Gene Database if possible
	 */
	public void close() throws DataException 
	{
		if (con == null) throw new DataException("Database connection already closed");
		dbConnector.closeConnection(con);
	}
			
	/**
	 * Excecutes several SQL statements to create the tables and indexes in the database the given
	 * connection is connected to
	 * @param convertCon	The connection to the database the tables are created in
	 * Note: Official GDB's are created by AP, not with this code.
	 * This is just here for testing purposes.
	 */
	public void createGdbTables() {
		Logger.log.trace("Info:  Creating tables");
		
		try 
		{
			Statement sh = con.createStatement();
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
			Statement sh = con.createStatement();
			sh.execute(
					"CREATE TABLE					" +
					"		info							" +
					"(	  version INTEGER PRIMARY KEY		" +
					")");
			sh.execute( //Add compatibility version of GDB
					"INSERT INTO version VALUES ( " + GDB_COMPAT_VERSION + ")");
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

	public static final int NO_LIMIT = 0;
	public static final int NO_TIMEOUT = 0;
	public static int query_timeout = 5; //seconds

	/**
	 * Get up to limit suggestions for a symbol autocompletion
	 */
	public List<Map<PropertyType, String>> getSymbolSuggestions(String text, int limit) 
	{		
		List<Map<PropertyType, String>> result = new ArrayList<Map<PropertyType, String>>();
		try {
			Statement s = con.createStatement();
			
			s.setQueryTimeout(query_timeout);
			if(limit > NO_LIMIT) s.setMaxRows(limit);
			
			String query =
						"SELECT id, code, backpageText FROM gene WHERE " +
						"backpageText LIKE '%<TH>Gene Name:<TH>" + text + "%'";
			
			ResultSet r = s.executeQuery(query);
	
			while(r.next()) 
			{
				String sysCode = r.getString("code");
				String sysName = DataSource.getBySystemCode(sysCode).getFullName();				
				
				Map<PropertyType, String> item = new HashMap<PropertyType, String>();
				
				String symbol = parseGeneSymbol(r.getString("backpageText"));
				item.put (PropertyType.TEXTLABEL, symbol);
				item.put (PropertyType.DATASOURCE, sysName);
				item.put (PropertyType.GENEID, r.getString("id"));
				
				result.add(item);
			}
		} catch (SQLException e) {
			Logger.log.error("Unable to query suggestions", e);
		}
		//if(limit > NO_LIMIT && result.size() == limit) sugg.add("...results limited to " + limit);
		return result;
	}

	/**
	 * Get up to limit suggestions for a symbol autocompletion
	 */
	public List<Map<PropertyType, String>> getIdSuggestions(String text, int limit) 
	{		
		List<Map<PropertyType, String>> result = new ArrayList<Map<PropertyType, String>>();
		try {
			Statement s = con.createStatement();
			
			s.setQueryTimeout(query_timeout);
			if(limit > NO_LIMIT) s.setMaxRows(limit);
			
			String query = "";
			query =
					"SELECT id, code FROM gene WHERE " +
					"id LIKE '" + text + "%'";
			
			ResultSet r = s.executeQuery(query);
	
			while(r.next()) {
				String sysCode = r.getString("code");
				String sysName = DataSource.getBySystemCode(sysCode).getFullName();
				
				Map<PropertyType, String> item = new HashMap<PropertyType, String>();
				item.put (PropertyType.GENEID, r.getString("id"));
				item.put (PropertyType.DATASOURCE, sysName);
				result.add (item);
			}
		} catch (SQLException e) {
			Logger.log.error("Unable to query suggestions", e);
		}
//		if(limit > NO_LIMIT && sugg.size() == limit) sugg.add("...results limited to " + limit);
		return result;
	}

    PreparedStatement pstGene;
    PreparedStatement pstLink;

    /**
     * Add a gene to the gene database
     */
    public int addGene(Xref ref, String bpText) 
    {
		try 
		{
			pstGene.setString(1, ref.getId());
			pstGene.setString(2, ref.getDataSource().getSystemCode());
			pstGene.setString(3, bpText);
			pstGene.executeUpdate();
		} 
		catch (Exception e) 
		{ 
			Logger.log.error("" + ref, e);
			return 1;
		}
		return 0;
    }
    
    /**
     * Add a link to the gene database
     */
    public int addLink(String link, Xref ref) 
    {
    	try 
    	{
			pstLink.setString(1, link);
			pstLink.setString(2, DataSource.ENSEMBL.getSystemCode());
			pstLink.setString(3, ref.getId());
			pstLink.setString(4, ref.getDataSource().getSystemCode());
			pstLink.executeUpdate();
		} 
    	catch (Exception e)
		{
			Logger.log.error(link + "\t" + ref, e);
			return 1;
		}
		return 0;
    }
    
	/**
	   Create indices on the database
	   You can call this at any time after creating the tables,
	   but it is good to do it only after inserting all data.
	 */
	public void createGdbIndices() throws DataException 
	{
		try
		{
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
		catch (SQLException e)
		{
			throw new DataException (e);
		}
	}
	
	/**
	   prepare for inserting genes and/or links
	 */
	public void preInsert(Connection con) throws SQLException
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
	public void commit() throws DataException
	{
		try
		{
			con.commit();
		}
		catch (SQLException e)
		{
			throw new DataException (e);
		}
	}

	/**
	   returns number of rows in gene table
	 */
	public int getGeneCount() throws DataException
	{
		int result = 0;
		try
		{
			ResultSet r = con.createStatement().executeQuery("SELECT COUNT(*) FROM gene");
			r.next();
			result = r.getInt (1);
			r.close();
		}
		catch (SQLException e)
		{
			throw new DataException (e);
		}
		return result;
	}

	public void compact() throws DataException
	{
		dbConnector.compact(con);
	}

}
