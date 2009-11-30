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


public class H2GdbMaker extends GdbMaker {

	public H2GdbMaker(String dbName) {
		super(dbName);
	}

	public void connect(boolean create) throws ClassNotFoundException, SQLException {
		//TODO: use create parameter
    	Class.forName("org.h2.Driver");
    	con = DriverManager.
    	  getConnection("jdbc:h2:file:" + getDbName(), "sa", "");
	}

	String getDbName() {
		return "h2/" + super.getDbName();
	}

}
