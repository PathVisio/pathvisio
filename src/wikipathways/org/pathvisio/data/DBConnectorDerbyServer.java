package org.pathvisio.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import javax.naming.OperationNotSupportedException;

import org.pathvisio.debug.StopWatch;

public class DBConnectorDerbyServer extends AbstractDBConnector {
	String host;
	int port;
	
	public DBConnectorDerbyServer(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public Connection createConnection(String dbName) throws Exception {
		Properties sysprop = System.getProperties();
		sysprop.setProperty("derby.storage.tempDirectory", System.getProperty("java.io.tmpdir"));
		sysprop.setProperty("derby.stream.error.file", File.createTempFile("derby",".log").toString());
		
		Class.forName("org.apache.derby.jdbc.ClientDriver");
		
		StopWatch timer = new StopWatch();
		timer.start();
		
		String url = "jdbc:derby://" + host + ":" + port + "/" + dbName;
		Connection con = DriverManager.getConnection(url);
		return con;
	}

	public Connection createConnection(String dbName, int props) throws Exception {
		return createConnection(dbName);
	}

	public String finalizeNewDatabase(String dbName) throws Exception {
		//Creating database not supported
		throw new OperationNotSupportedException("Can't create new database on server");
	}

}
