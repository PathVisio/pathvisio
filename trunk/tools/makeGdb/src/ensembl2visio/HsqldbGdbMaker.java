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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.pathvisio.debug.Logger;


public class HsqldbGdbMaker extends GdbMaker {

	HsqldbGdbMaker (String dbname)
	{
		super (dbname);
	}

    public void connect(boolean create) throws ClassNotFoundException, SQLException {
		//TODO: use create parameter
    	removeOldFiles();
    	Properties prop = new Properties();
    	prop.setProperty("user","sa");
    	prop.setProperty("password","");
    	prop.setProperty("hsqldb.default_table_type","cached");

    	Class.forName("org.hsqldb.jdbcDriver");
    	con = DriverManager.getConnection("jdbc:hsqldb:file:" + getDbName(), prop);
    	con.setAutoCommit(true);
    }

    void removeOldFiles() {
    	new File(getDbName() + ".properties").delete();
    }

	public void close() {
		try {
			if(con != null) {
				con.commit();
				Statement sh = con.createStatement();
				sh.execute("SHUTDOWN COMPACT");
			}
		} catch(Exception e) {

		}
		super.close();
	}

	@Override
	public void postInsert()
	{
		setPropertyReadOnly(getDbName(), true);
	}

	String getDbName() {
		return "hsqldb/" + super.getDbName();
	}

	public void setPropertyReadOnly(String dbname, boolean readonly) {
		Properties prop = new Properties();
		File propFile = new File(dbname + ".properties");
		try {
			prop.load(new FileInputStream(propFile));
			prop.setProperty("hsqldb.files_readonly", Boolean.toString(readonly));
			prop.store(new FileOutputStream(propFile), "HSQL Database Engine");
		} catch (Exception e) {
			Logger.log.error("Unable to set readonly to " + readonly, e);
		}
    }

}
