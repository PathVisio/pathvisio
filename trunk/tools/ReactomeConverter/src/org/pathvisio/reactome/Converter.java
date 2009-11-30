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
package org.pathvisio.reactome;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.pathvisio.Engine;
import org.pathvisio.model.Pathway;

/**
 * Reactome to GPML converter
 * @author thomas
 *
 */
public class Converter {
	public static void main(String[] args) {
		Engine.init();

		if(args.length < 2) {
			printHelp();
		}
		String db = args[0];
		String name = args[1];
		String pass = args[2];
		int pid = Integer.parseInt(args[3]);

		try {
			Connection con = connect(db, name, pass);
			Query query = new Query(con);

			ReactomeFormat rf = new ReactomeFormat(query);

			String pwName = query.getEventName(pid).replace(' ', '_');
			Pathway p = rf.convert(pid);
			p.writeToXml(new File(pwName + ".gpml"), true);
			p.writeToSvg(new File(pwName + ".svg"));
		} catch(Exception e) {
			printHelp();
			e.printStackTrace();
		}
	}

	static Connection connect(String db, String user, String pass) throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://" + db;
        Class.forName ("com.mysql.jdbc.Driver");
        return DriverManager.getConnection (url, user, pass);
	}

	static void printHelp() {
		System.out.println(
				"Usage:\n" +
				"mysql_database mysql_login mysql_pass pathway id\n" +
				"- mysql_database_url: the host/name database containing reactome" +
				" (e.g. 'localhost/reactome'" +
				"- mysql_login: the login name to the mysql database" +
				"- mysql_pass: the password to the mysql database" +
				"- pathway_id: the database id of the pathway to convert\n"
		);
	}
}
