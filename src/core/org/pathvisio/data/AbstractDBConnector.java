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
import java.sql.SQLException;
import java.sql.Statement;

import org.pathvisio.debug.Logger;

public abstract class AbstractDBConnector implements DBConnector {
	public static final int COMPAT_VERSION = 1;
	private int dbType;
	
	public abstract Connection createConnection(String dbName) throws Exception;
	public abstract Connection createConnection(String dbName, int props) throws Exception;	
	
	public void closeConnection(Connection con) throws Exception {
		closeConnection(con, PROP_NONE);
	}
	

	public void closeConnection(Connection con, int props) throws Exception {
		con.close();
	}

	public final Connection createNewDatabase(String dbName) throws Exception {
		Connection con = createNewDatabaseConnection(dbName);
		createTables(con);
		return con;
	}
	
	private Connection createNewDatabaseConnection(String dbName) throws Exception {
		return createConnection(dbName, PROP_RECREATE);
	}

	public abstract String finalizeNewDatabase(String dbName) throws Exception;
		
	public void setDbType(int type) { dbType = type; }

	public int getDbType() { return dbType; }

	public void createTables(Connection con) throws Exception {	
			con.setReadOnly(false);
			Statement sh = con.createStatement();
			try { sh.execute("DROP TABLE info"); } catch(SQLException e) { Logger.log.error("Error: unable to drop expression data tables: "+e.getMessage(), e); }
			try { sh.execute("DROP TABLE samples"); } catch(SQLException e) { Logger.log.error("Error: unable to drop expression data tables: "+e.getMessage(), e); }
			try { sh.execute("DROP TABLE expression"); } catch(SQLException e) { Logger.log.error("Error: unable to drop expression data tables: "+e.getMessage(), e); }
			
			sh.execute(
					"CREATE TABLE					" +
					"		info							" +
					"(	  version INTEGER PRIMARY KEY		" +
					")");
			sh.execute( //Add compatibility version of GEX
					"INSERT INTO info VALUES ( " + COMPAT_VERSION + ")");
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

	public void createIndices(Connection con) throws SQLException {
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

	public void compact(Connection con) throws SQLException {
		//May be implemented by subclasses
	}
}
