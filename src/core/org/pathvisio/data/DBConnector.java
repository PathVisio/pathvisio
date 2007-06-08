// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import org.pathvisio.gui.Engine;
import org.pathvisio.preferences.Preferences;

/**
 * This class provides the connection for the databases (annotation and expression database) used
 * in PathVisio. Implement the abstract methods when you want to add support for a new database engine.
 * @author Thomas
 */
public abstract class DBConnector {
	protected final int PROP_NONE = 0;
	protected static final int PROP_RECREATE = 4;
	protected static final int PROP_FINALIZE = 8;
	
	/**
	 * Type for gene database
	 */
	public static final int TYPE_GDB = 0;
	/**
	 * Type for expression database
	 */
	public static final int TYPE_GEX = 1;
	
	private int dbType;
	
	public abstract Connection createConnection(String dbName) throws Exception;
	public abstract Connection createConnection(String dbName, int props) throws Exception;	
	
	/**
	 * Close the given connection
	 * @param con The connection to be closed
	 * @throws Exception
	 */
	public void closeConnection(Connection con) throws Exception {
		closeConnection(con, PROP_NONE);
	}
	
	/**
	 * Close the given connection, and optionally finalize it after creation (using {@link #PROP_FINALIZE})
	 * @param con The connection to be closed
	 * @param props Close properties (one of {@link #PROP_NONE}, {@link #PROP_FINALIZE} or {@link #PROP_RECREATE})
	 * @throws Exception
	 */
	void closeConnection(Connection con, int props) throws Exception {
		con.close();
	}
	
	/**
	 * Create a new database with the given name. This includes creating tables.
	 * @param dbName The name of the database to create
	 * @return A connection to the newly created database
	 * @throws Exception
	 */
	protected final Connection createNewDatabase(String dbName) throws Exception {
		Connection con = createNewDatabaseConnection(dbName);
		createTables(con);
		return con;
	}
	
	private Connection createNewDatabaseConnection(String dbName) throws Exception {
		return createConnection(dbName, PROP_RECREATE);
	}
	
	/**
	 * This method is called to finalize the given database after creation
	 * (e.g. set read-only, archive files). The database name needs to returned, this
	 * may change when finalizing the database modifies the storage type (e.g. from directory
	 * to single file).
	 * @param dbName The name of the database to finalize	
	 * @throws Exception
	 * @return The name of the finalized database
	 */
	protected abstract String finalizeNewDatabase(String dbName) throws Exception;
	
	/**
	 * This method will be called when the user
	 * needs to select a database. Open a dialog (e.g. FileDialog) in this
	 * method to let the user select the database and return the database name.
	 * @param shell The shell to create the dialog
	 * @return The database name that was selected by the user, or null if no database was selected
	 */
	public abstract String openChooseDbDialog(Shell shell);
	
	/**
	 * This method will be called when the user
	 * needs to select a database to create. Open a dialog (e.g. FileDialog) in this
	 * method to let the user select the new database name/file/directory and return the database name.
	 * @param shell The shell to create the dialog
	 * @return The database name to create, or null if no database was specified
	 */
	public abstract String openNewDbDialog(Shell shell, String defaultName);
	
	/**
	 * Set the database type (one of {@link #TYPE_GDB} or {@link #TYPE_GEX})
	 * @param type The type of the database that will be used for this class
	 */
	public void setDbType(int type) { dbType = type; }
	/**
	 * Get the database type (one of {@link #TYPE_GDB} or {@link #TYPE_GEX})
	 * return The type of the database that is used for this class
	 */
	public int getDbType() { return dbType; }
	
	/**
	 * Excecutes several SQL statements to create the tables and indexes for storing 
	 * the expression data
	 */
	protected static void createTables(Connection con) throws Exception {	
			con.setReadOnly(false);
			Statement sh = con.createStatement();
			try { sh.execute("DROP TABLE info"); } catch(SQLException e) { Engine.log.error("Error: unable to drop expression data tables: "+e.getMessage(), e); }
			try { sh.execute("DROP TABLE samples"); } catch(SQLException e) { Engine.log.error("Error: unable to drop expression data tables: "+e.getMessage(), e); }
			try { sh.execute("DROP TABLE expression"); } catch(SQLException e) { Engine.log.error("Error: unable to drop expression data tables: "+e.getMessage(), e); }
			
			sh.execute(
					"CREATE TABLE					" +
					"		info							" +
					"(	  version INTEGER PRIMARY KEY		" +
					")");
			sh.execute( //Add compatibility version of GEX
					"INSERT INTO info VALUES ( " + Gex.COMPAT_VERSION + ")");
			sh.execute(
					"CREATE TABLE                    " +
					"		samples							" +
					" (   idSample INTEGER PRIMARY KEY,		" +
					"     name VARCHAR(50),					" +
					"	  dataType INTEGER					" +
			" )										");
			
			sh.execute(
					"CREATE TABLE					" +
					"		expression						" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"	  ensId VARCHAR(50),				" +
					"     idSample INTEGER,					" +
					"     data VARCHAR(50),					" +
					"	  groupId INTEGER 					" +
//					"     PRIMARY KEY (id, code, idSample, data)	" +
					")										");
	}
	
	/**
	 * Creates indices for a newly created expression database.
	 * @param con The connection to the expression database
	 * @throws SQLException
	 */
	protected void createIndices(Connection con) throws SQLException {
		con.setReadOnly(false);
		Statement sh = con.createStatement();
		sh.execute(
				"CREATE INDEX i_expression_id " +
		"ON expression(id)			 ");
		sh.execute(
				"CREATE INDEX i_expression_ensId " +
		"ON expression(ensId)			 ");
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
	
	/**
	 * This method may be implemented when the database files need to be
	 * compacted or defragmented after creation of a new database. It will be called
	 * after all data is added to the database.
	 * @param con A connection to the database
	 * @throws SQLException
	 */
	protected void compact(Connection con) throws SQLException {
		//May be implemented by subclasses
	}
	
	/**
	 * Shortcut for creating a file dialog that has the right default directories for
	 * the database type of this connector
	 * @param shell
	 * @param type
	 * @param filterExtensions
	 * @param filterNames
	 * @return A file dialog with the default directories set
	 */
	protected FileDialog createFileDialog(Shell shell, int type, String[] filterExtensions, String[] filterNames) {
		FileDialog fileDialog = new FileDialog(shell, type);
		fileDialog.setText("Select database file");
		
		String filterPath = null;
		switch(getDbType()) {
		case TYPE_GDB: 
			filterPath = Engine.getPreferences().getString(Preferences.PREF_DIR_GDB);
			break;
		case TYPE_GEX:
			filterPath = Engine.getPreferences().getString(Preferences.PREF_DIR_EXPR);
			break;
		}
		if(filterPath != null) fileDialog.setFilterPath(filterPath);
		if(filterExtensions != null) fileDialog.setFilterExtensions(filterExtensions);
		if(filterNames != null) fileDialog.setFilterNames(filterNames);
		
		return fileDialog;
	}
	
	/**
	 * Shortcut for creating a directory dialog that has the right default directories for
	 * the database type of this connector
	 * @param shell
	 * @return A directory dialog with the default directories set
	 */
	protected DirectoryDialog createDirectoryDialog(Shell shell) {
		DirectoryDialog dirDialog = new DirectoryDialog(shell, SWT.NONE);
		dirDialog.setText("Select database file");
		
		String filterPath = null;
		switch(getDbType()) {
		case TYPE_GDB: 
			filterPath = Engine.getPreferences().getString(Preferences.PREF_DIR_GDB);
			break;
		case TYPE_GEX:
			filterPath = Engine.getPreferences().getString(Preferences.PREF_DIR_EXPR);
			break;
		}
		if(filterPath != null) dirDialog.setFilterPath(filterPath);
		
		return dirDialog;
	}
}
