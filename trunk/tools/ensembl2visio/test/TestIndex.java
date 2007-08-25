package test;

import java.io.File;
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
		"	LEFT JOIN attribute ON datanode.id = attribute.id AND datanode.code = attribute.code " +
		"		WHERE attrName = 'Symbol' AND attrValue LIKE 'TNF%'",
		
		"SELECT attrValue FROM attribute WHERE attrName = 'Symbol' AND id = 'ENSG00000159958' AND code = 'En'"
	};
	
	static final String path = "E:\\Documents and Settings\\Thomas\\My Documents\\PathVisio data\\gene databases\\";

	static final String[] databases = new String[] {
		path + "Hs_Derby_20070817b_control.pgdb",
		path + "Hs_Derby_20070817b_combined.pgdb",
		path + "Hs_Derby_20070817b_combined.pgdb",
		path + "Hs_Derby_20070817b_mixed.pgdb",
		path + "Hs_Derby_20070817b_separate.pgdb"
	};
	
	public static void main(String[] args) {
		Logger log = new Logger();
		log.setLogLevel(true, true, false, true, true, true);
		
		log.setStream(System.err);
		
		for(String db : args) {
			try {
				TestIndex test = new TestIndex(db, log);
				test.doTest();
				log.trace("Finished!");
			} catch (Exception e) {
				log.error("Unable to test database " + db, e);
			}
		}
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
	
	void doTest() throws SQLException {
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
			log.trace("\t" + timer.stop() + "\tQUERY: '" + query + "'");
		}
		conn.close();
		clearCache();
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
