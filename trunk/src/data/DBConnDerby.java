package data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class DBConnDerby implements DBConnector {
	
	public Connection createConnection(String dbName) throws Exception {
		return createConnection(dbName, false);
	}
	
	public Connection createConnection(String dbName, boolean forceCreate) throws Exception {
		String urlAttr = "";
		if(!forceCreate) {
			File dbFile = new File(dbName);
			dbFile.delete();
			urlAttr += "create=true"; 
		}
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		return DriverManager.getConnection("jdbc:derby:" + dbName + ";" + urlAttr);
	}

	public void closeConnection(Connection con) throws SQLException {
		if(con != null) {
			con.close();
		}
	}
	
	public String openChooseDbDialog(Shell shell) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		String dir = dialog.open();
		return dir;
	}
	
	public String openNewDbDialog(Shell shell, String defaultName) {
		return openChooseDbDialog(shell);
	}
}
