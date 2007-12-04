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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
public class SimpleGex
{		
	private Connection con;
	private DBConnector dbConnector;
			
	private static CachedData cachedData;

	/**
	 * Get the {@link Connection} to the Expression-data database
	 * @deprecated Shouldn't be exposed
	 */
	public Connection getCon() { return con; }
	/**
	 * Check whether a connection to the database exists
	 * @return	true is a connection exists, false if not
	 */
	public boolean isConnected() { return con != null; }
	
	private String dbName;
	/**
	 * Get the database name of the expression data currently loaded
	 */
	public String getDbName() { return dbName; }
	
	/**
	 * Set the database name of the expression data currently loaded
	 * (Connection is not reset)
	 */
	public void setDbName(String name) { dbName = name; }
						    
	private HashMap<Integer, Sample> samples;
	/**
	 * Loads the samples used in the expression data (Sample table) in memory
	 */
	public void setSamples()
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
	
	public Sample getSample(int id) {
		return getSamples().get(id);
	}
	
	public HashMap<Integer, Sample> getSamples()
	{
		if(samples == null) setSamples();
		return samples;
	}
	
	public List<String> getSampleNames() {
		return getSampleNames(-1);
	}
	
	public List<String> getSampleNames(int dataType) {
		List<String> names = new ArrayList<String>();
		List<Sample> sorted = new ArrayList<Sample>(samples.values());
		Collections.sort(sorted);
		for(Sample s : sorted) {
			if(dataType == s.dataType || dataType == -1)
				names.add(s.getName());
		}
		return names;
	}
	
	public List<Sample> getSamples(int dataType) {
		List<Sample> smps = new ArrayList<Sample>();
		List<Sample> sorted = new ArrayList<Sample>(samples.values());
		Collections.sort(sorted);
		for(Sample s : sorted) {
			if(dataType == s.dataType || dataType == -1)
				smps.add(s);
		}
		return smps;
	}
	
	public List<Data> getCachedData(Xref idc)
	{
		if(cachedData != null) {
			return cachedData.getData(idc);
		} else {
			return null;
		}
	}
	
	public CachedData getCachedData() {
		return cachedData;
	}

	/**
	 * Gets all available expression data for the given gene id and returns a string
	 * containing this data in a HTML table
	 * @param idc	the {@link Xref} containing the id and code of the geneproduct to look for
	 * @return		String containing the expression data in HTML format or a string displaying a
	 * 'no expression data found' message in HTML format
	 */
	public String getDataString(Xref idc)
	{
		String noDataFound = "<P><I>No expression data found";
		String exprInfo = "<P><B>Gene id on mapp: " + idc.getId() + "</B><TABLE border='1'>";
		
		String colNames = "<TR><TH>Sample name";
		if(		con == null //Need a connection to the expression data
				|| !GdbManager.getCurrentGdb().isConnected() //and to the gene database
		) return noDataFound;
		
		List<Data> pwData = cachedData.getData(idc);
		
		if(pwData == null) return noDataFound;
		
		for(Data d : pwData){
			colNames += "<TH>" + d.getXref().getId();
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
	protected void cacheData(List<Xref> refs, ProgressKeeper p)
	{	
		cachedData = new CachedData();
		StopWatch timer = new StopWatch();
		timer.start();
			
		for(Xref pwIdc : refs)
		{
			String id = pwIdc.getId();			
			String code = pwIdc.getDataSource().getSystemCode();
			
			if(cachedData.hasData(pwIdc)) continue;
			
			List<String> ensIds = GdbManager.getCurrentGdb().ref2EnsIds(pwIdc); //Get all Ensembl genes for this id
			
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
	 * Connects to the Expression database with
	 * option to remove the old database
	 * @param 	create true if the old database has to be removed, false for just connecting
	 */
	public SimpleGex(String dbName, boolean create, DBConnector connector) throws DataException
	{
		dbConnector = connector;
		if(create)
		{
			con = dbConnector.createNewGex(dbName);
		} 
		else 
		{
			con = dbConnector.createConnection(dbName);
			setSamples();
		}
		try
		{
			con.setReadOnly( !create );
		}
		catch (SQLException e)
		{
			throw new DataException (e);
		}
	}
	
	/**
	 * Connects to the Expression database 
	 */
//	public static void connect() throws Exception
//	{
//		connect(null, false, true);
//	}
//	
//	public static void connect(String dbName) throws Exception
//	{
//		connect(dbName, false, true);
//	}
		
	/**
	 * Close the connection to the Expression database, with option to execute the 'SHUTDOWN COMPACT'
	 * statement before calling {@link Connection#close()}
	 * @param finalize true to excecute the 'SHUTDOWN COMPACT' statement, false to just close the connection
	 */
	public void close(boolean finalize)
	{
		if(con != null)
		{
			try
			{				
				if(finalize) 
				{
					dbConnector.compact(con);
					dbConnector.createGexIndices(con);
					String newDb = dbConnector.finalizeNewDatabase(dbName);
					setDbName(newDb);
				} 
				else 
				{
					dbConnector.closeConnection(con);
				}	
			} 
			catch (Exception e) 
			{
				Logger.log.error("Error while closing connection to expression dataset " + dbName, e);
			}
			con = null;
		}
	}
	
	/**
	 * Close the connection excecuting the 'SHUTDOWN' statement 
	 * before calling {@link Connection#close()}
	 */
	public void close()
	{
		close(false);
	}

}
