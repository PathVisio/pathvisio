package data;

import gmmlVision.GmmlVision;

import java.sql.Connection;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import preferences.GmmlPreferences;

public abstract class DBConnector {
	static final int PROP_NONE = 0;
	static final int PROP_RECREATE = 4;
	static final int PROP_FINALIZE = 8;
	
	public static final int TYPE_GDB = 0;
	public static final int TYPE_GEX = 1;
	
	int dbType;
	
	public abstract Connection createConnection(String dbName) throws Exception;
	public abstract Connection createConnection(String dbName, int props) throws Exception;	
	
	public void closeConnection(Connection con) throws Exception {
		closeConnection(con, PROP_NONE);
	}
	
	void closeConnection(Connection con, int props) throws Exception {
		con.close();
	}
	
	public Connection createNewDatabase(String dbName) throws Exception {
		return createConnection(dbName, PROP_RECREATE);
	}
	
	public abstract void finalizeNewDatabase(String dbName) throws Exception;
	
	public abstract String openChooseDbDialog(Shell shell);
	public abstract String openNewDbDialog(Shell shell, String defaultName);
	
	public void setDbType(int type) { dbType = type; }
	public int getDbType() { return dbType; }
	
	protected FileDialog createFileDialog(Shell shell, int type, String[] filterExtensions, String[] filterNames) {
		FileDialog fileDialog = new FileDialog(shell, type);
		fileDialog.setText("Select database file");
		
		String filterPath = null;
		switch(getDbType()) {
		case TYPE_GDB: 
			filterPath = GmmlVision.getPreferences().getString(GmmlPreferences.PREF_DIR_GDB);
			break;
		case TYPE_GEX:
			filterPath = GmmlVision.getPreferences().getString(GmmlPreferences.PREF_DIR_EXPR);
			break;
		}
		if(filterPath != null) fileDialog.setFilterPath(filterPath);
		if(filterExtensions != null) fileDialog.setFilterExtensions(filterExtensions);
		if(filterNames != null) fileDialog.setFilterNames(filterNames);
		
		return fileDialog;
	}
}
