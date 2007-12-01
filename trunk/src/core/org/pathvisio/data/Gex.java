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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.CachedData.Data;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.util.ProgressKeeper;

/**
 * This class handles everything related to the Expression Data. It contains the database connection,
 * several methods to query data and write data and methods to convert a GenMAPP Expression Dataset
 * to hsqldb format
 */
public class Gex implements ApplicationEventListener 
{	
	private static Connection con;
			
	public static CachedData cachedData;
	
	/**
	 * Get the {@link Connection} to the Expression-data database
	 */
	public static Connection getCon() { return con; }
	/**
	 * Check whether a connection to the database exists
	 * @return	true is a connection exists, false if not
	 */
	public static boolean isConnected() { return con != null; }
	
	private static String dbName;
	/**
	 * Get the database name of the expression data currently loaded
	 */
	public static String getDbName() { return dbName; }
	
	/**
	 * Set the database name of the expression data currently loaded
	 * (Connection is not reset)
	 */
	public static void setDbName(String name) { dbName = name; }
						    
	private static HashMap<Integer, Sample> samples;
	/**
	 * Loads the samples used in the expression data (Sample table) in memory
	 */
	public static void setSamples()
	{		
		try {
			ResultSet r = con.createStatement().executeQuery(
					"SELECT idSample, name, dataType FROM samples"
			);
			samples = new HashMap<Integer, Sample>();
			while(r.next())
			{
				int id = r.getInt(1);
				samples.put(id, new Sample(id, r.getString(2), r.getInt(3)));					
			}
		} catch (Exception e) {
			Logger.log.error("while loading data from the 'samples' table: " + e.getMessage(), e);
		}
	}
	
	public static Sample getSample(int id) {
		return getSamples().get(id);
	}
	
	public static HashMap<Integer, Sample> getSamples()
	{
		if(samples == null) setSamples();
		return samples;
	}
	
	public static List<String> getSampleNames() {
		return getSampleNames(-1);
	}
	
	public static List<String> getSampleNames(int dataType) {
		List<String> names = new ArrayList<String>();
		List<Sample> sorted = new ArrayList<Sample>(samples.values());
		Collections.sort(sorted);
		for(Sample s : sorted) {
			if(dataType == s.dataType || dataType == -1)
				names.add(s.getName());
		}
		return names;
	}
	
	public static List<Sample> getSamples(int dataType) {
		List<Sample> smps = new ArrayList<Sample>();
		List<Sample> sorted = new ArrayList<Sample>(samples.values());
		Collections.sort(sorted);
		for(Sample s : sorted) {
			if(dataType == s.dataType || dataType == -1)
				smps.add(s);
		}
		return smps;
	}
	
	public static List<Data> getCachedData(Xref idc)
	{
		if(cachedData != null) {
			return cachedData.getData(idc);
		} else {
			return null;
		}
	}
	
	public static CachedData getCachedData() {
		return cachedData;
	}

	/**
	 * Gets all available expression data for the given gene id and returns a string
	 * containing this data in a HTML table
	 * @param idc	the {@link Xref} containing the id and code of the geneproduct to look for
	 * @return		String containing the expression data in HTML format or a string displaying a
	 * 'no expression data found' message in HTML format
	 */
	public static String getDataString(Xref idc)
	{
		String noDataFound = "<P><I>No expression data found";
		String exprInfo = "<P><B>Gene id on mapp: " + idc.getId() + "</B><TABLE border='1'>";
		
		String colNames = "<TR><TH>Sample name";
		if(		con == null //Need a connection to the expression data
				|| !Gdb.getCurrentGdb().isConnected() //and to the gene database
		) return noDataFound;
		
		List<Data> pwData = cachedData.getData(idc);
		
		if(pwData == null) return noDataFound;
		
		for(Data d : pwData){
			colNames += "<TH>" + d.getIdCodePair().getId();
		}
		
		String dataString = "";
		for(Sample s : getSamples().values())
		{
			dataString += "<TR><TH>" + s.name;
			for(Data d : pwData)
			{
				dataString += "<TH>" + d.getSampleData(s.idSample);
			}
		}
		
		return exprInfo + colNames + dataString + "</TABLE>";
	}
	
	/**
	 * Loads expression data for all the given gene ids into memory
	 * @param refs	Genes to cache the expression data for
	 * (typically all genes in a pathway)
	 */
	protected static void cacheData(List<Xref> refs, ProgressKeeper p)
	{	
		cachedData = new CachedData();
		StopWatch timer = new StopWatch();
		timer.start();
			
		for(Xref pwIdc : refs)
		{
			String id = pwIdc.getId();			
			String code = pwIdc.getDataSource().getSystemCode();
			
			if(cachedData.hasData(pwIdc)) continue;
			
			ArrayList<String> ensIds = Gdb.getCurrentGdb().ref2EnsIds(pwIdc); //Get all Ensembl genes for this id
			
			HashMap<Integer, Data> groupData = new HashMap<Integer, Data>();
			
			if(ensIds.size() > 0) //Only create a Data object if the id maps to an Ensembl gene
			{				
				StopWatch tt = new StopWatch();
				StopWatch ts = new StopWatch();
				
				tt.start();
				
				groupData.clear();
				
				for(String ensId : ensIds)
				{	
					try {
						ts.start();
						
						ResultSet r = con.createStatement().executeQuery(
								"SELECT id, code, data, idSample, groupId FROM expression " +
								" WHERE ensId = '" + ensId + "'");
						//r contains all genes and data mapping to the Ensembl id
						while(r.next())
						{
							int group = r.getInt("groupId");
							Xref ref = new Xref(
									r.getString("id"), 
									DataSource.getBySystemCode(r.getString("code"))
									);
							Data data = groupData.get(group);
							if(data == null) {
								groupData.put(group, data = new Data(ref, group));
								cachedData.addData(pwIdc, data);
							}
							
							int idSample = r.getInt("idSample");					
							data.setSampleData(idSample, r.getString("data"));
						}
						
						ts.stopToLog("Fetching data for ens id: " + ensId + "\t");
					} catch (Exception e)
					{
						Logger.log.error("while caching expression data: " + e.getMessage(), e);
					}
				}
				
				tt.stopToLog(id + ", " + code + ": adding data to cache\t\t");
			}			
			if(p.isCancelled()) //Check if the process is interrupted
			{
				return;
			}
			p.worked(p.getTotalWork() / refs.size()); //Update the progress
		}
		p.finished();
		timer.stopToLog("Caching expression data\t\t\t");
		Logger.log.trace("> Nr of ids queried:\t" + refs.size());
	}
			
	/**
	 * {@link Connection} to the GenMAPP Expression Dataset
	 */
	private static Connection conGmGex;

	private static File gmGexFile;
	/**
	 * Returns the file that contains the GenMAPP Expression Dataset
	 */
	public static File getGmGexFile() { return gmGexFile; }
	/**
	 * Sets the file that contains the GenMAPP Expression Dataset
	 */
	public static void setGmGexFile(File file) { gmGexFile = file; }
	
	/**
	 * Converts the GenMAPP Expression Dataset to a expression database
	 * in Hsqldb format as used by this program.
	 * <BR><BR>This method reports all errors occured during the conversion to a file named 'convert_gex_error.txt'
	 */
	public static void convertGex(ProgressKeeper p)
	{
		//Open a connection to the error file
		PrintWriter error = null;
		try {
			error = new PrintWriter(new FileWriter("convert_gex_error.txt"));
		} catch(IOException ex) {
			Logger.log.error("Unable to open error file for gdb conversion: " + ex.getMessage(), ex);
		}
		
		try {
			connect(null, true, false); //Connect and delete the old database if exists
			connectGmGex(gmGexFile); //Connect to the GenMAPP gex
			
			con.setAutoCommit(false); //Keep control over when to commit, should increase speed
			Statement s = conGmGex.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			PreparedStatement pstmtExpr = con.prepareStatement(
					"INSERT INTO expression			" +
					"	(id, code, ensId,			" + 
					"	 idSample, data)			" +
			"VALUES	(?, ?, ?, ?, ?)			");
			
			ResultSet r = s.executeQuery("SELECT * FROM Expression");
			r.last();
			int nrRows = r.getRow(); //Get the number of rows for keeping track of the progress
			r.beforeFirst(); //Set the cursor back to the start
			
			// Fill the Sample table
			ResultSetMetaData rsmd = r.getMetaData(); 
			int nCols = rsmd.getColumnCount();
			for(int i = 4; i < nCols - 1; i++) // Column 4 to 2 before last contain expression data
			{
				int dataType = rsmd.getColumnType(i);
				String sampleName = rsmd.getColumnName(i);
				// Add new sample
				con.createStatement().execute("INSERT INTO SAMPLES" +
						"	(idSample, name, dataType)" + 
						"VALUES ( " + (i - 4) + ",'" + sampleName + "', " + dataType + " )");
			}
			
			//Fill the Expression table
			int nq = 0; //The number of queries excecuted
			String id = "";
			String code = "";
			while(r.next()) { //Process all rows of the expression data
				if(p.isCancelled()) //Check if the user cancelled the conversion
				{
					closeGmGex();
					close();
					return;
				}
				
				id = r.getString("ID");
				code = r.getString("SystemCode");
				ArrayList<String> ensIds = Gdb.getCurrentGdb().ref2EnsIds(new Xref (id, DataSource.getBySystemCode(code))); //Find the Ensembl genes for current gene
				
				if(ensIds.size() == 0) //No Ensembl gene found
				{
					error.println(id + "\t" + code + "\t No Ensembl gene found for this identifier");
				} else { //Gene maps to an Ensembl id, so add it
					ArrayList<String> data = new ArrayList<String>();
					for(int i = 4; i < nCols - 1; i++) { // Column 4 to 2 before last contain expression data
						data.add(r.getString(i));
					}
					for( String ensId : ensIds) //For every Ensembl id add the data
					{
						int i = 0;
						for(String str : data)
						{
							try {
								pstmtExpr.setString(1,id);
								pstmtExpr.setString(2,code);
								pstmtExpr.setString(3, ensId);
								pstmtExpr.setInt(4,i);
								pstmtExpr.setString(5,str);
								pstmtExpr.execute();
							} catch (Exception e) {
								error.println(id + ", " + code + ", " + i + "\t" + e.getMessage());
							}
							i++;
						}
					}
				}
				nq++;
				if(nq % 1000 == 0) //Commit every 1000 queries
					con.commit();
				p.worked(p.getTotalWork()/nrRows); //Report progress
			}
			con.commit();	
		} catch(Exception e) {
			error.println("Error: " + e.getMessage());
		}
		error.println("END");
		error.close();
		closeGmGex();
		close();
		
//		setGexReadOnly(true);
		
		p.finished();
	}
	
	public static DBConnector getDBConnector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return Engine.getCurrent().getDbConnector(DBConnector.TYPE_GEX);
	}
	
	/**
	 * Connects to the Expression database with
	 * option to remove the old database
	 * @param 	create true if the old database has to be removed, false for just connecting
	 */
	public static void connect(String dbName, boolean create, boolean fireEvent) throws Exception
	{
		close();
		
		if(dbName != null) setDbName(dbName);
		
		DBConnector connector = getDBConnector();
		
		if(create) {
			con = connector.createNewDatabase(getDbName());
		} else {
			con = connector.createConnection(getDbName());
			setSamples();
			//TODO: move to GexSwt
			//loadXML();
		}

		con.setReadOnly( !create );
		
		if(fireEvent)
			fireExpressionDataEvent(new ExpressionDataEvent(Gex.class, ExpressionDataEvent.CONNECTION_OPENED));
	}
	
	/**
	 * Connects to the Expression database 
	 */
	public static void connect() throws Exception
	{
		connect(null, false, true);
	}
	
	public static void connect(String dbName) throws Exception
	{
		connect(dbName, false, true);
	}
		
	/**
	 * Close the connection to the Expression database, with option to execute the 'SHUTDOWN COMPACT'
	 * statement before calling {@link Connection#close()}
	 * @param finalize true to excecute the 'SHUTDOWN COMPACT' statement, false to just close the connection
	 */
	public static void close(boolean finalize)
	{
		if(con != null)
		{
			try
			{
				//TODO: move to GexSwt
				//saveXML();
				
				DBConnector connector = getDBConnector();
				if(finalize) {
					connector.compact(con);
					connector.createIndices(con);
					String newDb = connector.finalizeNewDatabase(dbName);
					setDbName(newDb);
				} else {
					connector.closeConnection(con);
				}
				fireExpressionDataEvent(new ExpressionDataEvent(Gex.class, ExpressionDataEvent.CONNECTION_CLOSED));
				
			} catch (Exception e) {
				Logger.log.error("Error while closing connection to expression dataset " + dbName, e);
			}
			con = null;
		}
	}
	
	/**
	 * Close the connection excecuting the 'SHUTDOWN' statement 
	 * before calling {@link Connection#close()}
	 */
	public static void close()
	{
		close(false);
	}
	
	/**
	 * Connect to the GenMAPP Expression Dataset specified by the given file
	 * @param gmGexFile	File containing the GenMAPP Expression Dataset
	 */
	public static void connectGmGex(File gmGexFile) {
		String database_after = ";DriverID=22;READONLY=true";
		String database_before =
			"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conGmGex = DriverManager.getConnection(
					database_before + gmGexFile.toString() + database_after, "", "");
		} catch (Exception e) {
			Logger.log.error("Error: Unable to open connection go GenMAPP gex " + gmGexFile +
					": " +e.getMessage(), e);
		}
	}
	
	/**
	 * Close the connection to the GenMAPP Expression Dataset
	 */
	public static void closeGmGex() {
		if(conGmGex != null)
		{
			try {
				conGmGex.close();
				conGmGex = null;
			} catch (Exception e) {
				Logger.log.error("Error while closing connection to GenMAPP gex: " + e.getMessage(), e);
			}
		}
	}
	
	public void applicationEvent(ApplicationEvent e) {
		switch(e.getType()) {
		case ApplicationEvent.APPLICATION_CLOSE:
			if(isConnected()) close();
		}
	}
	
	static List<ExpressionDataListener> listeners;
	
	/**
	 * Add a {@link ExpressionDataListener}, that will be notified if an
	 * event related to expression data occurs
	 * @param l The {@link ExpressionDataListener} to add
	 */
	public static void addListener(ExpressionDataListener l) {
		if(listeners == null) listeners = new ArrayList<ExpressionDataListener>();
		listeners.add(l);
	}
	
	/**
	 * Fire a {@link ExpressionDataEvent} to notify all {@link ExpressionDataListener}s registered
	 * to this class
	 * @param e
	 */
	public static void fireExpressionDataEvent(ExpressionDataEvent e) {
		for(ExpressionDataListener l : listeners) l.expressionDataEvent(e);
	}
	
	public interface ExpressionDataListener {
		public void expressionDataEvent(ExpressionDataEvent e);
	}
	
	public static class ExpressionDataEvent extends EventObject {
		private static final long serialVersionUID = 1L;
		public static final int CONNECTION_OPENED = 0;
		public static final int CONNECTION_CLOSED = 1;

		public Object source;
		public int type;
		
		public ExpressionDataEvent(Object source, int type) {
			super(source);
			this.source = source;
			this.type = type;
		}
	}
}
