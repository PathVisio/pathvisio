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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;


public class GdbToTxt {
	public static void main(String[] args) {
		if(args.length != 2) {
			System.err.println("Error: Invalid number of arguments\n" +
					"Usage:\n" +
			"java Gdb2Txt {database file} {output file}");
			System.exit(1);
		}
		String dbName = args[0];
		String outFile = args[1];
		try {

			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

			String url = "jdbc:derby:";
			File dbFile = new File(dbName);
			if(dbFile.isDirectory()) {
				url += dbName;
			} else {
				url += "jar:(" + dbFile.toString() + ")database";
			}
			Connection con = DriverManager.getConnection(url);

			gdb2txt(con, new File(outFile));
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	static void gdb2txt(Connection con, File outFile) throws Exception {
		PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
		ResultSet r = con.createStatement().executeQuery(
				"SELECT * FROM link"
		);
		out.println("Ensembl ID\tDatabase ID\tDatabase code");
		while(r.next()) {
			String idLeft = r.getString(1);
			String idRight = r.getString(3);
			String codeRight = r.getString(4);
			out.println(idLeft + "\t" + idRight + "\t" + codeRight);
		}
		out.close();
	}

}
