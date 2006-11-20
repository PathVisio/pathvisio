package data;

import java.sql.Connection;

import org.eclipse.swt.widgets.Shell;

public interface DBConnector {
	int PROP_NONE = 0;
	int PROP_COMPACT = 2;
	int PROP_RECREATE = 4;
	
	public Connection createConnection(String dbName) throws Exception;
	public Connection createConnection(String dbName, int props) throws Exception;
	public void closeConnection(Connection con) throws Exception;
	public void closeConnection(Connection con, int props) throws Exception;
	
	public void setDatabaseReadonly(String dbName, boolean readonly);
	
	public String openChooseDbDialog(Shell shell, String filterPath);
	public String openNewDbDialog(Shell shell, String defaultName);
}
