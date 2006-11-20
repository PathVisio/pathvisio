package data;

import gmmlVision.GmmlVision;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import preferences.GmmlPreferences;

public class DBConnDerby implements DBConnector {
	
	public Connection createConnection(String dbName) throws Exception {
		return createConnection(dbName, PROP_NONE);
	}
	
	public Connection createConnection(String dbName, int props) throws Exception {
		String urlAttr = "";
		if((props & PROP_RECREATE) != 0) {
			File dbFile = new File(dbName);
			dbFile.delete();
			urlAttr += "create=true"; 
		}
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		return DriverManager.getConnection("jdbc:derby:" + dbName + ";" + urlAttr);
	}

	public void closeConnection(Connection con) throws SQLException {
		closeConnection(con, PROP_NONE);
	}
	
	public void closeConnection(Connection con, int props) throws SQLException {
		if(con != null) {
			con.close();
		}
	}
	
	public String openChooseDbDialog(Shell shell) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setFilterPath(GmmlVision.getPreferences().getString(GmmlPreferences.PREF_DIR_GDB));
		String dir = dialog.open();
		return dir;
	}
	
	public String openNewDbDialog(Shell shell, String defaultName) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setFilterPath(defaultName);
		String dir = dialog.open();
		return dir;
	}
}
