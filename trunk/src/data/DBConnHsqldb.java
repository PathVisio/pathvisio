package data;

import gmmlVision.GmmlVision;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import preferences.GmmlPreferences;

public class DBConnHsqldb implements DBConnector {
	String DB_FILE_EXT = "properties";
	
	public Connection createConnection(String dbName) throws Exception {
		return createConnection(dbName, false);
	}
	
	public Connection createConnection(String dbName, boolean forceCreate) throws Exception {
		if(!forceCreate) {
			File dbFile = dbName2File(dbName);
			if(!dbFile.canRead()) throw new Exception("Can't access file '" + dbFile.toString() + "'");
		}
				
		Class.forName("org.hsqldb.jdbcDriver");
		Properties prop = new Properties();
		prop.setProperty("user","sa");
		prop.setProperty("password","");
		return DriverManager.getConnection("jdbc:hsqldb:file:" + dbName, prop);
	}

	public void closeConnection(Connection con) throws SQLException {
		if(con != null) {
			Statement sh = con.createStatement();
			sh.executeQuery("SHUTDOWN"); // required, to write last changes
			sh.close();
			con.close();
		}
	}
	
	public String openChooseDbDialog(Shell shell) {
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setText("Select database file");
		fileDialog.setFilterPath(GmmlVision.getPreferences().getString(GmmlPreferences.PREF_DIR_GDB));
		fileDialog.setFilterExtensions(new String[] {"*." + DB_FILE_EXT,"*.*"});
		fileDialog.setFilterNames(new String[] {"Database file","All files"});
		String file = fileDialog.open();
		if(file != null) file = file2DbName(file);
		return file;
	}
	
	public String openNewDbDialog(Shell shell, String defaultName) {
		FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		saveDialog.setText("Save");
		saveDialog.setFilterPath(GmmlVision.getPreferences().getString(GmmlPreferences.PREF_DIR_GDB));
		saveDialog.setFilterExtensions(new String[] {"*." + DB_FILE_EXT, "*.*"});
		saveDialog.setFilterNames(new String[] {"Database file", "All files"});
		saveDialog.setFileName(defaultName);
		String file = saveDialog.open();
		if(file != null) file = file2DbName(file);
		return file;
	}
	
	File dbName2File(String dbName) {
		return new File(dbName + '.' + DB_FILE_EXT);
	}
	
	String file2DbName(String fileName) {
		String end = '.' + DB_FILE_EXT;
		return fileName.endsWith(end) ? 
				fileName.substring(0, fileName.length() -  end.length()) : fileName;
	}
}
