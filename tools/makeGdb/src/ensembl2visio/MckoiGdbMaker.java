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

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class MckoiGdbMaker extends GdbMaker {

	public MckoiGdbMaker(String dbName)
	{
		super(dbName);
	}

    public void connect(boolean create) throws ClassNotFoundException, SQLException {
		//TODO: use create parameter
    	Properties prop = new Properties();
    	prop.setProperty("user","sa");
    	prop.setProperty("password","");

    	Class.forName("org.hsqldb.jdbcDriver");
    	String url = "jdbc:mckoi:local://mckoi/" + getDbName() + "?create=true";
    	con = DriverManager.getConnection(url, prop);
    	con.setAutoCommit(true);
    }

	String getDbName() {
		return "mckoi/" + super.getDbName() + ".conf";
	}

	public void compact() throws SQLException {
		con.commit();

		Statement s = con.createStatement();

		s.execute("COMPACT TABLE gene");
		s.execute("COMPACT TABLE link");

		con.commit(); //Just to be sure...
	}
}

