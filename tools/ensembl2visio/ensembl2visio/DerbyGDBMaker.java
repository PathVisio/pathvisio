package ensembl2visio;

import java.sql.DriverManager;
import java.sql.SQLException;


public class DerbyGDBMaker extends GDBMaker {
	public DerbyGDBMaker(String txtFile, String dbName) {
		super(txtFile, dbName);
	}   	
	
	public void connect() throws ClassNotFoundException, SQLException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		con = DriverManager.getConnection("jdbc:derby:" + getDbName() + ";create=true");
	}

	String getDbName() {
		return "derby/" + super.getDbName();
	}

	
}
