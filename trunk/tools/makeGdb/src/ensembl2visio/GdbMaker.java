// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package ensembl2visio;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.pathvisio.debug.Logger;

/**
   Abstract base class, to be derived for each different database
   implementation.
 */
public abstract class GdbMaker
{


    protected Connection con;

    String dbName;

    public GdbMaker(String dbName) {
    	this.dbName = dbName;

    }



	/**
	   connect to a gene database
	   if create == true, any pre-existing database will be overwritten.
	   if create == false, opens pre-existing database for appending.
	 */
    public abstract void connect(boolean create) throws ClassNotFoundException, SQLException;

    public void close() {
    	try {
    		if(con != null) con.close();
    	} catch(Exception e) {
    		Logger.log.error("Error when closing connection", e);
    	}

    }

    String getDbName() {
    	return dbName;
    }

    public void compact() throws SQLException {

    }

	/**
	   call after closing the database
	   Does nothing by default, some databases need to do some
	   cleanup operation after closing the database
	 */
	public void postInsert() {}



	/**
	   Create indices on the database
	   You can call this at any time after creating the tables,
	   but it is good to do it only after inserting all data.
	 */
	public void createIndices() throws SQLException {
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
				" ON datanode(code)"
		);
		sh.execute(
				"CREATE INDEX i_acode" +
				" ON attribute(code)"
		);
		sh.execute(
				"CREATE INDEX i_aid" +
				" ON attribute(id)"
		);
		sh.execute(
				"CREATE INDEX i_attrname" +
				" ON attribute(attrname)"
		);
	}




	/**
	   commit inserted data
	 */
	void commit() throws SQLException
	{
		con.commit();
	}

}
