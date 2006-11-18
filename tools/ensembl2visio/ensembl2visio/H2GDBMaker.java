package ensembl2visio;

import java.sql.DriverManager;
import java.sql.SQLException;


public class H2GDBMaker extends GDBMaker {
	
	public H2GDBMaker(String txtFile, String dbName) {
		super(txtFile, dbName);
	}

	public void connect() throws ClassNotFoundException, SQLException {
    	Class.forName("org.h2.Driver");
    	con = DriverManager.
    	  getConnection("jdbc:h2:file:" + getDbName(), "sa", "");
	}

	String getDbName() {
		return "h2/" + super.getDbName();
	}

}
