/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2019 BiGCaT Bioinformatics
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.desktop.gex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.rdb.construct.DBConnector;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.data.DataException;
import org.pathvisio.data.DataInterface;
import org.pathvisio.data.IRow;
import org.pathvisio.data.ISample;

/**
 * Responsible for creating and querying a pgex database.
 * SimpleGex wraps SQL statements in methods,
 * so the rest of the apps don't need to know the
 * details of the Database schema.
 * For this, SimpleGex uses the generic JDBC interface.
 *
 * It delegates dealing with the differences between Derby, Hsqldb etc.
 * to a DBConnector instance.
 * You need to pass a correct DBConnector instance at creation of
 * SimpleGex.
 *
 * In the PathVisio GUI environment, use GexManager
 * to create and connect to a centralized Gex.
 * This will also automatically
 * find the right DBConnector from the preferences.
 *
 * In a head-less or test environment, you can bypass GexManager
 * to create or connect to one or more databases of any type.
 */
//TODO: add function to query # of samples
public class SimpleGex implements DataInterface
{
	private static final int GEX_COMPAT_VERSION = 2; //Preferred schema version
	private static final int SAMPLE_NAME_LEN = 50; // max length of sample names

	private Connection con;
	private DBConnector dbConnector;
	private int commitCount = 0;

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
	private void setDbName(String name) { dbName = name; }

	// cache of samples
	private Map<Integer, Sample> samples;

	PreparedStatement pstSample = null;
	PreparedStatement pstExpr = null;

	public void prepare() throws SQLException
	{
		pstSample = con.prepareStatement(
				" INSERT INTO SAMPLES " +
				"	(idSample, name, dataType)  " +
		" VALUES (?, ?, ?)		  ");
		pstExpr = con.prepareStatement(
				"INSERT INTO expression			" +
				"	(id, code,      			" +
				"	 idSample, data, groupId)	" +
		"VALUES	(?, ?, ?, ?, ?)			");
	}


	/**
	 * add a Sample to the db.
	 * Must call preprare() before.
	 */
	public void addSample(int sampleId, String value, int type) throws SQLException
	{
		assert (pstSample != null);
		if (value.length() >= SAMPLE_NAME_LEN)
			throw new IllegalArgumentException ("Sample name can't be longer than " + SAMPLE_NAME_LEN + " chars");
		pstSample.setInt(1, sampleId);
		pstSample.setString(2, value);
		pstSample.setInt(3, type);
		pstSample.execute();
		con.commit();
	}

	/**
	 * Add an expression row to the db. Must call prepare() before.
	 */
	public void addExpr(Xref ref, String idSample, String value, int group)
		throws SQLException
	{
		assert (pstExpr != null);
		pstExpr.setString(1, ref.getId());
		pstExpr.setString(2, ref.getDataSource().getSystemCode());
		pstExpr.setString(3, idSample);
		//TODO: this is a hack.
		// Proper solution: ask user which columns contain data
		// don't even try to import annotation and other stuff
		// give an exception if a data value is longer than 50
		String truncValue = value;
		if (value.length() > 50) truncValue = value.substring(0, 50);
		pstExpr.setString(4, truncValue);
		pstExpr.setInt(5, group);
		pstExpr.execute();
		if (++commitCount % 1000 == 0) con.commit();
	}

	public ISample getSample(int id) throws DataException
	{
		return getSamples().get(id);
	}

	public Sample findSample(String name) throws DataException
	{
		//TODO: create a map for faster lookups.
		for (Sample s : samples.values())
		{
			if (s.getName().equals(name)) return s;
		}
		return null;
	}

	/**
	 * Returns samples as a list, ordered by Id.
	 * <p>
	 * Note that there is no guarantee that the index of a Sample in the list
	 * is equal to the id of that sample.
	 * In other words, result.get(n).getId() < result.get(n+1).getId(), but
	 * NOT: result.get(n).getId() == n
	 */
	@Override
	public List<? extends ISample> getOrderedSamples() throws DataException
	{
		List<ISample> result = new ArrayList<ISample>(getSamples().values());
		Collections.sort (result);
		return result;
	}

	/**
	 * Reads a list of Samples from the database,
	 * and returns them, indexed by key.
	 *
	 * This data is cached, so you can safely call this repeatedly.
	 */
	@Override
	public Map<Integer, ? extends ISample> getSamples() throws DataException
	{
		if(samples == null)
		{
			try {
				ResultSet r = con.createStatement().executeQuery(
						"SELECT idSample, name, dataType FROM samples"
				);
				samples = new HashMap<Integer, Sample>();
				while(r.next())
				{
					int id = r.getInt(1);
					samples.put(id, new Sample(id, r.getString(2), "undefined", r.getInt(3)));
				}
			} catch (SQLException e) {
				throw new DataException ("SQL exception while setting samples", e);
			}
		}
		return samples;
	}

	@Override
	public List<String> getSampleNames() {
		return getSampleNames(-1);
	}

	@Override
	public List<String> getSampleNames(int dataType) {
		List<String> names = new ArrayList<String>();
		List<Sample> sorted = new ArrayList<Sample>(samples.values());
		Collections.sort(sorted);
		for(Sample s : sorted) {
			if(dataType == s.getDataType() || dataType == -1)
				names.add(s.getName());
		}
		return names;
	}

	PreparedStatement pst1 = null;
	private PreparedStatement getPst1() throws SQLException
	{
		if (pst1 == null)
		{
			pst1 = con.prepareStatement(
			"SELECT id, code, data, idSample, groupId FROM expression " +
			" WHERE id = ? AND code = ?");
		}
		return pst1;
	}

	PreparedStatement pst2 = null;
	private PreparedStatement getPst2() throws SQLException
	{
		if (pst2 == null)
		{
			pst2 = con.prepareStatement("SELECT code FROM expression GROUP BY code");
		}
		return pst2;
	}

	/**
	 * get all datasouces used in this gex.
	 */
	@Override
	public Set<DataSource> getUsedDatasources() throws DataException
	{
		try
		{
			Set<DataSource> destFilter = new HashSet<DataSource>();
			PreparedStatement pst = getPst2();
			ResultSet r = pst.executeQuery();
			while (r.next())
			{
				destFilter.add(DataSource.getExistingBySystemCode(r.getString(1)));
			}
			return destFilter;
		}
		catch (SQLException ex)
		{
			throw new DataException (ex);
		}
	}

	@Override
	public Collection<? extends IRow> getData(Set<Xref> destRefs) throws DataException
	{
		try
		{
			PreparedStatement pst = getPst1();
			Map<Integer, ReporterData> groupData = new HashMap<Integer, ReporterData>();

			for (Xref destRef : destRefs)
			{
				pst.setString(1, destRef.getId());
				pst.setString(2, destRef.getDataSource().getSystemCode());
				ResultSet r = pst.executeQuery();

				//r contains all data mapping to the destref
				//there could be multiple data items
				while(r.next())
				{
					int group = r.getInt("groupId");
					ReporterData data = groupData.get(group);
					if(data == null) {
						data = new ReporterData(destRef, group);
						groupData.put(group, data);
					}
					int idSample = r.getInt("idSample");
					data.setSampleData(samples.get(idSample), r.getString("data"));
				}
			}
			return groupData.values();
		}
		catch (SQLException ex)
		{
			throw new DataException(ex);
		}
	}

	/**
	 * Connects to the Expression database with
	 * option to remove the old database
	 * Will use the passed connector type (of which a new instance is created)
	 * @param 	create true if the old database has to be removed, false for just connecting
	 */
	public SimpleGex(String dbName, boolean create, DBConnector connector) throws DataException
	{
		this.dbName = dbName;
		try
		{
			dbConnector = connector.getClass().newInstance();
		}
		catch (InstantiationException e)
		{
			throw new DataException (e);
		}
		catch (IllegalAccessException e)
		{
			throw new DataException (e);
		}

		dbConnector.setDbType(DBConnector.TYPE_GEX);
		if(create)
		{
			createNewGex(dbName);
		}
		else
		{
			try
			{
				con = dbConnector.createConnection(dbName, DBConnector.PROP_NONE);
			}
			catch (IDMapperException ex)
			{
				throw new DataException (ex);
			}
			getSamples(); // init samples cache
		}
	}

	/**
	 * Close the connection to the Expression database, with option to execute the 'SHUTDOWN COMPACT'
	 * statement before calling {@link Connection#close()}
	 */
	public void close() throws DataException
	{
		if(con != null)
		{
			try
			{
				dbConnector.closeConnection(con);
				con.close();
			}
			catch (IDMapperException e)
			{
				throw new DataException (e);
			}
			catch (SQLException ex)
			{
				throw new DataException (ex);
			}
			con = null;
		}
	}

	/**
	 * Create a new database with the given name. This includes creating tables.
	 * @param dbName The name of the database to create
	 * @return A connection to the newly created database
	 * @throws Exception
	 * @throws Exception
	 */
	public final void createNewGex(String dbName) throws DataException
	{
		try
		{
			con = dbConnector.createConnection(dbName, DBConnector.PROP_RECREATE);
			this.dbName = dbName;
			createGexTables();
		}
		catch (IDMapperException ex)
		{
			throw new DataException(ex);
		}
	}

	/**
	 * Excecutes several SQL statements to create the tables and indexes for storing
	 * the expression data
	 */
	protected void createGexTables() throws IDMapperException
	{
		try
		{
			con.setReadOnly(false);
			Statement sh = con.createStatement();
			try { sh.execute("DROP TABLE info"); } catch(SQLException e) { Logger.log.warn("Warning: unable to drop expression data tables: "+e.getMessage()); }
			try { sh.execute("DROP TABLE samples"); } catch(SQLException e) { Logger.log.warn("Warning: unable to drop expression data tables: "+e.getMessage()); }
			try { sh.execute("DROP TABLE expression"); } catch(SQLException e) { Logger.log.warn("Warning: unable to drop expression data tables: "+e.getMessage()); }

			sh.execute(
					"CREATE TABLE					" +
					"		info							" +
					"(	  version INTEGER PRIMARY KEY		" +
					")");
			sh.execute( //Add compatibility version of GEX
					"INSERT INTO info VALUES ( " + GEX_COMPAT_VERSION + ")");
			sh.execute(
					"CREATE TABLE                    " +
					"		samples							" +
					" (   idSample INTEGER PRIMARY KEY,		" +
					"     name VARCHAR(" + SAMPLE_NAME_LEN + "),					" +
					"	  dataType INTEGER					" +
			" )										");

			sh.execute(
					"CREATE TABLE					" +
					"		expression						" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     idSample INTEGER,					" +
					"     data VARCHAR(50),					" +
					"	  groupId INTEGER 					" +
	//				"     PRIMARY KEY (id, code, idSample, data)	" +
			")										");
			con.setAutoCommit(false);
			commitCount = 0;
		}
		catch (SQLException e)
		{
			throw new IDMapperException (e);
		}
	}

	/**
	 * Creates indices for a newly created expression database.
	 * @param con The connection to the expression database
	 * @throws SQLException
	 */
	public void createGexIndices() throws IDMapperException
	{
		try
		{
			con.setReadOnly(false);
			Statement sh = con.createStatement();
			sh.execute(
					"CREATE INDEX i_expression_id " +
			"ON expression(id)			 ");
			sh.execute(
					"CREATE INDEX i_expression_idSample " +
			"ON expression(idSample)	 ");
			sh.execute(
					"CREATE INDEX i_expression_data " +
			"ON expression(data)	     ");
			sh.execute(
					"CREATE INDEX i_expression_code " +
			"ON expression(code)	 ");
			sh.execute(
					"CREATE INDEX i_expression_groupId" +
			" ON expression(groupId)	");
		}
		catch (SQLException e)
		{
			// wrap up the sql exception
			throw new IDMapperException (e);
		}
	}

	/**
	 * Run this after insterting all sample / expression data
	 * once, to defragment the db and create indices.
	 * This method closes the current database connection in order
	 * for the {@link DBConnector} to clean up.
	 */
	public void finalize() throws IDMapperException
	{
		try
		{
			con.commit();
		}
		catch (SQLException e)
		{
			throw new IDMapperException (e);
		}
		dbConnector.compact(con);
		createGexIndices();
		dbConnector.closeConnection(con, DBConnector.PROP_FINALIZE);
		//The dbConnector may change the database file after cleaning up,
		//for example, the derby connector first creates the database as directory
		//and then adds the database to a zip file and removes the directory.
		//The database name needs to be changed to the zip file in this case.
		String newDb = dbConnector.finalizeNewDatabase(dbName);
		setDbName(newDb);
	}

	/**
	   commit inserted data
	 */
	public void commit() throws IDMapperException
	{
		try
		{
			con.commit();
		}
		catch (SQLException e)
		{
			throw new IDMapperException (e);
		}
	}


	PreparedStatement pstRow = null;

	// lazy instantiation of pstRow
	private PreparedStatement getPstRow() throws SQLException
	{
		if (pstRow == null)
		{
			pstRow = con.prepareStatement (
					"SELECT id, code, idSample, data, groupId " +
					"FROM expression " +
					"WHERE groupId = ?");
		}
		return pstRow;
	}

	@Override
	public ReporterData getRow(int rowId) throws DataException
	{
		Map<Integer, ? extends ISample> samples = getSamples();
		try
		{
			ReporterData result;
			PreparedStatement ps = getPstRow();
			ps.setInt(1, rowId);
			ResultSet rs = ps.executeQuery();

			Xref ref = null;
			result = new ReporterData (null, rowId);

			while (rs.next())
			{
				if (ref == null)
				{
					//TODO: this redundancy in ref is not normalized
					ref = new Xref (rs.getString(1), DataSource.getExistingBySystemCode(rs.getString(2)));
					result.setXref(ref);
				}

				int sample = rs.getInt(3);
				String value = rs.getString (4);
				result.setSampleData(samples.get(sample), value);
			}

			return result;
		}
		catch (SQLException e)
		{
			throw new DataException (e);
		}
	}

	@Override
	public int getNrRow() throws DataException
	{
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT MAX(groupId) FROM expression");
			rs.next();
			return rs.getInt(1) + 1;
		}
		catch (SQLException e)
		{
			throw new DataException (e);
		}
	}

	PreparedStatement pstRowIt = null;

	// lazy instantiation of pstRow
	private PreparedStatement getPstRowIterator() throws SQLException
	{
		if (pstRowIt == null)
		{
			pstRowIt = con.prepareStatement (
					"SELECT id, code, idSample, data, groupId " +
					"FROM expression " +
					"ORDER BY groupId");
		}
		return pstRowIt;
	}
	
	/**
	 * Go over groups in database one by one.
	 */
	private class RowIterator implements Iterator<IRow>
	{
		private ResultSet rs;
		private boolean hasNext;
		
		RowIterator() throws SQLException
		{
			PreparedStatement ps = getPstRowIterator();
			ResultSet rs = ps.executeQuery();
			hasNext = rs.next();
		}
		
		@Override
		public boolean hasNext() 
		{
			return hasNext;
		}

		@Override
		public IRow next() 
		{
			if (!hasNext) throw new NoSuchElementException();				
			
			// at this point, resultset should already be at beginning of new group.
			try
			{
				Map<Integer, ? extends ISample> samples = getSamples();
				ReporterData result;

				int currentGroup = rs.getInt(5);
				result = new ReporterData (null, rs.getInt(5));
				
				Xref ref = new Xref (rs.getString(1), DataSource.getExistingBySystemCode(rs.getString(2)));
				result.setXref(ref);
				
				do
				{
					int groupId = rs.getInt(5);
					
					if (groupId != currentGroup)
					{
						// we're done tallying this group, and recordset is ready for next group
						return result;
					}
					
					int sample = rs.getInt(3);
					String value = rs.getString (4);
					result.setSampleData(samples.get(sample), value);
				}
				while  ((hasNext = rs.next()) == true);

				return result;
			}
			catch (SQLException e)
			{
				Logger.log.error ("Error while iterating over elements", e);
				throw new NoSuchElementException("Error fetching next element: " + e.getMessage());
			} catch (DataException e) {
				Logger.log.error ("Error while iterating over elements", e);
				throw new NoSuchElementException("Error fetching next element: " + e.getMessage());
			}		
		}

		@Override
		public void remove() 
		{
			throw new UnsupportedOperationException();
		}
		
	}
	
	@Override
	public Iterable<IRow> getIterator() throws DataException 
	{
		try
		{
			final RowIterator it = new RowIterator();
			return new Iterable<IRow>()
			{
				@Override
				public Iterator<IRow> iterator() 
				{
					return it;
				}
			};
		}
		catch (SQLException ex)
		{
			throw new DataException(ex);
		}

	}
}
