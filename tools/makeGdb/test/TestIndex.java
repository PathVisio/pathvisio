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
package test;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;

public class TestIndex {
	static final String[] queries = new String[] {
//		"SELECT * FROM attribute", 
		"SELECT datanode.id, datanode.code, attribute.attrValue FROM datanode " +
		"LEFT JOIN attribute ON datanode.id = attribute.id AND datanode.code = attribute.code " +
		"WHERE attrName = 'Symbol' AND attrValue LIKE 'TNF%'",
		
		"SELECT attrValue FROM attribute WHERE attrName = 'Symbol' AND id = 'ENSG00000159958' AND code = 'En'",
		
		"SELECT datanode.id, datanode.code, attribute.attrValue FROM datanode " +
		"LEFT JOIN attribute ON datanode.id = attribute.id AND datanode.code = attribute.code " +
		"WHERE attrName = 'Symbol' AND attrValue = 'hsa-mir-122a'"
	};
	
	static final String path = "E:\\Documents and Settings\\Thomas\\My Documents\\PathVisio data\\gene databases\\";
	
	public static void main(String[] args) {
		Logger log = new Logger();
		log.setLogLevel(false, false, false, false, true, true);
		
		log.setStream(System.err);
		PrintStream out = System.err;
		
		String[][] result = new String[queries.length][args.length];
		int j = 0;
		for(String db : args) {
			try {
				TestIndex test = new TestIndex(db, log);
				long[] t = test.doTest();
				for(int q = 0; q < t.length; q++) {
					result[q][j] = Long.toString(t[q]);
				}
				log.trace("Finished!");
				j++;
			} catch (Exception e) {
				log.error("Unable to test database " + db, e);
			}
		}

//		out.print("\t");
//		for(int i = 0; i < args.length; i++) {
//			out.print(args[i] + "\t");
//		}
//		out.print("\n");
		for(int q = 0; q < result.length; q ++) {
//			out.print(queries[q] + "\t");
			for(int i = 0; i < result[q].length; i++) {
				out.print(result[q][i] + "\t");
			}
			out.print("\n");
		}
		out.close();
	}
	
	String database;
	Connection conn;
	Logger log;
	
	public TestIndex(String database, Logger log) throws Exception {
		this.log = log;
		this.database = database;
		Properties sysprop = System.getProperties();
		sysprop.setProperty("derby.storage.tempDirectory", System.getProperty("java.io.tmpdir"));
		sysprop.setProperty("derby.stream.error.file", File.createTempFile("derby",".log").toString());
		
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		String url = "jdbc:derby:jar:(" + database + ")database";

		conn = DriverManager.getConnection(url);
	}
	
	long[] doTest() throws SQLException {
		long[] result = new long[queries.length];
		int q = 0;
		StopWatch timer = new StopWatch();
		
		Statement s = conn.createStatement();
		log.trace("Starting test on '" + database + "'");
		for(String query : queries) {
			timer.start();
			ResultSet r = s.executeQuery(query);
			int nc = r.getMetaData().getColumnCount();
			while(r.next()) {
				for(int i = 1; i <= nc; i++) log.info("\t\t" + r.getString(i));
			}
			long t = timer.stop();
			result[q++] = t;
			log.trace("\t" + t + "\tQUERY: '" + query + "'");
		}
		conn.close();
		clearCache();
		return result;
	}
	
	void clearCache() {
		File temp = new File(System.getProperty("java.io.tmpdir"));
		delete(temp);
	}
	
	void delete(File file) {
		for(File f : file.listFiles()) {
			if(f.isDirectory()) delete(f);
			//else log.trace("Removing temp: " + f + " (" + f.delete() + ")");
			f.delete();
		}
	}
}
