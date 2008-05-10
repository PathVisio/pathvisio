//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 

//http://www.apache.org/licenses/LICENSE-2.0 

//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.

package org.pathvisio.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.model.XrefWithSymbol;
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
		try {
			Statement s = con.createStatement();

			String query =
				"SELECT attrvalue FROM attribute WHERE " +
				"attrname = 'Symbol' AND id = '" + ref.getId() + "' " +
				"AND code = '" + ref.getDataSource().getSystemCode() + "'";
			ResultSet r = s.executeQuery(query);

			while(r.next()) 
			{
				return r.getString(1);
			}
		} catch (SQLException e) {
			Logger.log.error("Unable to query suggestions", e);
		}
		return null;
	}

	PreparedStatement pstXrefExists = null;
	
	// lazy initialization of prepared statement
	private PreparedStatement getPstXrefExists () throws SQLException
	{		
		if (pstXrefExists == null)
		{
			String query =
				"SELECT id FROM " + table_DataNode + " WHERE " +
				"id = ? AND code = ?";
			pstXrefExists = con.prepareStatement(query);
		}
		return pstXrefExists;
	}
	
	/**
	 * Simply checks if an xref occurs in the datanode table.
	 */
	public boolean xrefExists(Xref xref) 
	{
		try 
		{
			PreparedStatement pst = getPstXrefExists();
			pst.setString(1, xref.getId());
			pst.setString(2, xref.getDataSource().getSystemCode());
			ResultSet r = pst.executeQuery();

			while(r.next()) 
			{
				return true;
			}
		} 
		catch (SQLException e) 
		{
			Logger.log.error("Unable to query suggestions", e);
		}
		return false;
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

	/**
	 * @deprecated use {@link Engine#getBackpageTextProvider()} instead!
	 * @return
	 */
	public static BackpageTextProvider getBackpageTextProvider() {
		return Engine.getCurrent().getBackpageTextProvider();
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

	// lazy initialization
	private PreparedStatement pstEnsId2RefsNoCode = null;
	private PreparedStatement getPstEnsId2RefsNoCode () throws SQLException
	{
		if (pstEnsId2RefsNoCode == null)
		{
			pstEnsId2RefsNoCode = con.prepareStatement(
					"SELECT idRight, codeRight FROM link " +
					"WHERE idLeft = ?");
		}
		return pstEnsId2RefsNoCode;
	}

	// lazy initialization
	private PreparedStatement pstEnsId2RefsWithCode = null;
	private PreparedStatement getPstEnsId2RefsWithCode () throws SQLException
	{
		if (pstEnsId2RefsWithCode == null)
		{
			pstEnsId2RefsWithCode = con.prepareStatement(
					"SELECT idRight, codeRight FROM link " +
					"WHERE idLeft = ? AND codeRight = ?");
		}
		return pstEnsId2RefsWithCode;
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
			PreparedStatement pst;
			if (resultDs == null)
			{
				pst = getPstEnsId2RefsNoCode();
				pst.setString(1, ensId);
			}
			else
			{
				pst = getPstEnsId2RefsWithCode();
				pst.setString(1, ensId);
				pst.setString(2, resultDs.getSystemCode());
			}
			ResultSet r1 = pst.executeQuery();
			while(r1.next()) 
			{
				crossIds.add(new Xref(r1.getString(1), DataSource.getBySystemCode(r1.getString(2))));
			}
		}
		catch(Exception e) 
		{
			Logger.log.error("Unable to get cross references for ensembl gene " +
					"'" + ensId + "'", e);
		}

		timer.stopToLog("> endId2Refs: (" + ensId + ")");
		return crossIds;
	}

	// lazy initialization
	private PreparedStatement pstRef2EnsIds = null;
	private PreparedStatement getPstRef2EnsIds () throws SQLException
	{
		if (pstRef2EnsIds == null)
		{
			pstRef2EnsIds = con.prepareStatement (
					"SELECT idLeft FROM link " +
					"WHERE idRight = ? AND codeRight = ?"
				);
		}
		return pstRef2EnsIds;
	}
	
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
		try 
		{
			PreparedStatement pst = getPstRef2EnsIds();
			pst.setString (1, xref.getId());
			pst.setString (2, xref.getDataSource().getSystemCode());
			ResultSet r1 = pst.executeQuery();
			while(r1.next()) 
			{
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

	public List<Xref> getCrossRefsByAttribute(String attrName, String attrValue) {
		Logger.log.trace("Fetching cross references by attribute: " + attrName + " = " + attrValue);
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);

		formatter.format(
				"SELECT %1$s.%2$s, %1$s.%3$s FROM %1$s " +
				"LEFT JOIN %4$s ON %4$s.%2$s = %1$s.%2$s AND %4$s.%3$s = %1$s.%3$s " +
				"WHERE attrName = '%5$s' AND attrValue = '%6$s'",
				table_DataNode, "id", "code", "Attribute", attrName, attrValue
		);

		String query = sb.toString();		
		List<Xref> refs = new ArrayList<Xref>();

		try {
			ResultSet r = con.createStatement().executeQuery(query);
			while(r.next()) {
				Xref ref = new Xref(r.getString(1), DataSource.getBySystemCode(r.getString(2)));
				refs.add(ref);
			}
		} catch(SQLException e) {
			Logger.log.error("Unable to fetch cross-ref by attribute", e);
		}
		Logger.log.trace("End fetching cross references by attribute");
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
		if ((props & DBConnector.PROP_RECREATE) == 0)
		{
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
	public void createGdbTables() 
	{
		Logger.log.info("Info:  Creating tables");

		try 
		{
			Statement sh = con.createStatement();
			sh.execute("DROP TABLE info");
			sh.execute("DROP TABLE link");
			sh.execute("DROP TABLE gene");
		} 
		catch(Exception e) 
		{
			Logger.log.error("Unable to drop gdb tables (ignoring): " + e.getMessage());
		}
		
		try
		{
			Statement sh = con.createStatement();
			sh.execute(
					"CREATE TABLE					" +
					"		info							" +
					"(	  version INTEGER PRIMARY KEY		" +
			")");
			Logger.log.info("Info table created");
			sh.execute( //Add compatibility version of GDB
					"INSERT INTO info VALUES ( " + GDB_COMPAT_VERSION + ")");
			Logger.log.info("Version stored in info");
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
			Logger.log.info("Link table created");
			sh.execute(
					"CREATE TABLE					" +
					"		gene							" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     backpageText VARCHAR(800),		" +
					"     PRIMARY KEY (id, code)    		" +
					" )										");
			Logger.log.info("Gene table created");
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
	 * Get up to limit suggestions for a symbol autocompletion. 
	 * the search will be case insensitive by default
	 * @param text The text to base the suggestions on
	 * @param limit The number of results to limit the search to
	 */
	public List<String> getSymbolSuggestions(String text, int limit) {
		return getSymbolSuggestions(text, limit, false);
	}

	/**
	 * Get up to limit suggestions for a symbol autocompletion
	 * @param text The text to base the suggestions on
	 * @param limit The number of results to limit the search to
	 * @param caseSensitive if true, the search will be case sensitive
	 */
	public List<String> getSymbolSuggestions(String text, int limit, boolean caseSensitive) 
	{		
		List<String> result = new ArrayList<String>();
		try {
			Statement s = con.createStatement();

			s.setQueryTimeout(query_timeout);
			if(limit > NO_LIMIT) s.setMaxRows(limit);

			//TODO: use prepared statement
			String query = String.format(
					"SELECT attrvalue FROM attribute WHERE " +
					"attrname = 'Symbol' AND %s LIKE '%s%%'",
					caseSensitive ? "attrvalue" : "LOWER(attrvalue)",
							caseSensitive ? text : text.toLowerCase()
			);

			ResultSet r = s.executeQuery(query);

			while(r.next()) 
			{
				String symbol = r.getString("attrValue");
				result.add(symbol);
			}
		} catch (SQLException e) {
			Logger.log.error("Unable to query suggestions", e);
		}
		//if(limit > NO_LIMIT && result.size() == limit) sugg.add("...results limited to " + limit);
		return result;
	}


	/**
	 * Get up to limit suggestions for a identifier autocompletion. 
	 * The search will be case insensitive by default
	 * @param text The text to base the suggestions on
	 * @param limit The number of results to limit the search to
	 */
	public List<Xref> getIdSuggestions(String text, int limit) {
		return getIdSuggestions(text, limit, false);
	}
	
	/**
	 * Get up to limit suggestions for a identifier autocompletion
	 * @param text The text to base the suggestions on
	 * @param limit The number of results to limit the search to
	 * @param caseSensitive if true, the search will be case sensitive
	 */
	public List<Xref> getIdSuggestions(String text, int limit, boolean caseSensitive) 
	{		
		List<Xref> result = new ArrayList<Xref>();
		try {
			Statement s = con.createStatement();

			s.setQueryTimeout(query_timeout);
			if(limit > NO_LIMIT) s.setMaxRows(limit);

			StringBuilder sb = new StringBuilder();
			Formatter formatter = new Formatter(sb);

			//TODO: use prepared statement
			formatter.format(
					"SELECT id, code FROM %1$s WHERE " +
					"%3$s LIKE '%2$s%%'",
					table_DataNode, caseSensitive ? text : text.toLowerCase(), 
							caseSensitive ? "id" : "LOWER(id)"
			);

			String query = sb.toString();
			ResultSet r = s.executeQuery(query);

			while(r.next()) {
				String id = r.getString(1);
				DataSource ds = DataSource.getBySystemCode(r.getString(2));
				Xref ref = new Xref(id, ds);
				result.add (ref);
			}
		} catch (SQLException e) {
			Logger.log.error("Unable to query suggestions", e);
		}
//		if(limit > NO_LIMIT && sugg.size() == limit) sugg.add("...results limited to " + limit);
		return result;
	}

	/**
	 * free text search for matching symbols or identifiers
	 * @param text The text to base the suggestions on
	 * @param limit The number of results to limit the search to
	 */
	public List<XrefWithSymbol> freeSearch (String text, int limit) 
	{		
		List<XrefWithSymbol> result = new ArrayList<XrefWithSymbol>();
		try {
			PreparedStatement ps1 = con.prepareStatement(
					"SELECT dn.id, dn.code, attr.attrvalue " +
					"FROM " +
							table_DataNode + " AS dn " +
					"	LEFT JOIN " +
					"		attribute AS attr " +
					"	ON               " +
					"		dn.id = attr.id AND dn.code = attr.code " +
					"WHERE " +
					"		LOWER(dn.id) LIKE ?" +
					"	AND " +
					"			(attr.attrname IS NULL " +
					"		OR " +
					"			attr.attrname = 'Symbol') "
					);
			ps1.setQueryTimeout(query_timeout);
			if(limit > NO_LIMIT) 
			{
				ps1.setMaxRows(limit);
			}

			ps1.setString(1, text.toLowerCase() + "%");
			ResultSet r = ps1.executeQuery();
			while(r.next()) {
				String id = r.getString(1);
				DataSource ds = DataSource.getBySystemCode(r.getString(2));
				String sym = r.getString(3);
				XrefWithSymbol ref = new XrefWithSymbol (new Xref(id, ds), sym);
				result.add (ref);
			}
			
			if (result.size() >= limit)
			{
				return result;
			}
			
			PreparedStatement ps2 = con.prepareStatement(
					"SELECT dn.id, dn.code, attr.attrvalue " +
					"FROM " + table_DataNode + " AS dn " +
					"	JOIN attribute AS attr " +
					"	ON " +
					"	dn.id = attr.id AND dn.code = attr.code " +
					"WHERE " +
					"		attr.attrname = 'Symbol'" +
					" 	AND " +
					"	LOWER(attr.attrvalue) LIKE ?"
			);
			ps2.setString(1, "%" + text.toLowerCase() + "%");
			r = ps2.executeQuery();

			while(r.next()) {
				String id = r.getString(1);
				DataSource ds = DataSource.getBySystemCode(r.getString(2));
				String sym = r.getString(3);
				XrefWithSymbol ref = new XrefWithSymbol (new Xref(id, ds), sym);
				result.add (ref);
			}
			
		} catch (SQLException e) {
			Logger.log.error("Unable to run query", e);
		}
		return result;
	}
	
    PreparedStatement pstGene = null;
    PreparedStatement pstLink = null;

	/**
	 * Add a gene to the gene database
	 */
	public int addGene(Xref ref, String bpText) 
	{
    	if (pstGene == null) throw new NullPointerException();
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
    	if (pstLink == null) throw new NullPointerException();
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
					" ON " + table_DataNode + "(code)"
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
	public void preInsert() throws DataException
	{
		try
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
		catch (SQLException e)
		{
			throw new DataException (e);
		}
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
			ResultSet r = con.createStatement().executeQuery("SELECT COUNT(*) FROM " + table_DataNode);
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
