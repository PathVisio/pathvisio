package data;

import java.sql.Connection;

import org.eclipse.swt.widgets.Shell;

public interface DBConnector {
	
	public Connection createConnection(String dbName) throws Exception;
	public Connection createConnection(String dbName, boolean forceCreate) throws Exception;
	public void closeConnection(Connection con) throws Exception;
	
	public String openChooseDbDialog(Shell shell);
	public String openNewDbDialog(Shell shell, String defaultName);
}
