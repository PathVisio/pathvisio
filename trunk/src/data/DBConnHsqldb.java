package data;

import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import debug.StopWatch;

import preferences.GmmlPreferences;

public class DBConnHsqldb implements DBConnector {
	String DB_FILE_EXT = "properties";
	
	public Connection createConnection(String dbName) throws Exception {
		return createConnection(dbName, PROP_NONE);
	}
	
	public Connection createConnection(String dbName, int props) throws Exception {
		boolean recreate = (props & PROP_RECREATE) != 0;
		if(recreate) {
			File dbFile = dbName2File(dbName);
			if(dbFile.exists()) dbFile.delete();
		}
	
		Class.forName("org.hsqldb.jdbcDriver");
		Properties prop = new Properties();
		prop.setProperty("user","sa");
		prop.setProperty("password","");
		prop.setProperty("hsqldb.default_table_type", "cached");
		prop.setProperty("ifexists", Boolean.toString(!recreate));
		
		StopWatch timer = new StopWatch();
		timer.start();
		Connection con = DriverManager.getConnection("jdbc:hsqldb:file:" + dbName, prop);
		GmmlVision.log.info("Connecting with hsqldb to " + dbName + ":\t" + timer.stop());
		return con;
	}

	public void closeConnection(Connection con) throws SQLException {
		closeConnection(con, PROP_NONE);
	}
	
	public void closeConnection(Connection con, int props) throws SQLException {
		boolean compact = (props & PROP_COMPACT) != 0;
		if(con != null) {
			Statement sh = con.createStatement();
			sh.executeQuery("SHUTDOWN" + (compact ? " COMPACT" : ""));
			sh.close();
			con.close();
		}
	}
	
	public String openChooseDbDialog(Shell shell, String filterPath) {
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setText("Select database file");
		if(filterPath != null) fileDialog.setFilterPath(filterPath);
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
	
	public void setDatabaseReadonly(String dbName, boolean readonly) {
		 setPropertyReadOnly(dbName, readonly);
	}
	
	void setPropertyReadOnly(String dbName, boolean readonly) {
    	Properties prop = new Properties();
		try {
			File propertyFile = dbName2File(dbName);
			prop.load(new FileInputStream(propertyFile));
			prop.setProperty("hsqldb.files_readonly", Boolean.toString(readonly));
			prop.store(new FileOutputStream(propertyFile), "HSQL Database Engine");
			} catch (Exception e) {
				GmmlVision.log.error("Unable to set database properties to readonly", e);
			}
	}
}
