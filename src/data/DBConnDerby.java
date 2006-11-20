package data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class DBConnDerby implements DBConnector {
	String lastDbName;
	
	public Connection createConnection(String dbName) throws Exception {
		return createConnection(dbName, PROP_NONE);
	}
	
	public Connection createConnection(String dbName, int props) throws Exception {
		boolean recreate = (props & PROP_RECREATE) != 0;
		if(recreate) {
			File dbFile = new File(dbName);
			dbFile.delete(); 
		}
		
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties prop = new Properties();
		prop.setProperty("create", Boolean.toString(recreate));
//		prop.setProperty("shutdown", "true");
		Connection con = DriverManager.getConnection("jdbc:derby:" + dbName + ";", prop);
		lastDbName = dbName;
		return con;
	}

	public void closeConnection(Connection con) throws SQLException {
		closeConnection(con, PROP_NONE);
	}
	
	public void closeConnection(Connection con, int props) throws SQLException {
		if(con != null) {
			if(lastDbName != null) 
				DriverManager.getConnection("jdbc:derby:" + lastDbName + ";shutdown=true");
			con.close();
		}
	}
	
	public String openChooseDbDialog(Shell shell, String filterPath) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		if(filterPath != null) dialog.setFilterPath(filterPath);
		String dir = dialog.open();
		return dir;
	}
	
	public String openNewDbDialog(Shell shell, String defaultName) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setFilterPath(defaultName);
		String dir = dialog.open();
		return dir;
	}

	public void setDatabaseReadonly(String dbName, boolean readonly) {	}
}
