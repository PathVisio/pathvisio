package org.pathvisio.data;

import java.sql.Connection;

public interface DBConnector {
	public static final int PROP_NONE = 0;
	public static final int PROP_RECREATE = 4;
	public static final int PROP_FINALIZE = 8;
	
	/**
	 * Type for gene database
	 */
	public static final int TYPE_GDB = 0;
	/**
	 * Type for expression database
	 */
	public static final int TYPE_GEX = 1;
	
	public abstract Connection createConnection(String dbName) throws Exception;
	public abstract Connection createConnection(String dbName, int props) throws Exception;	
}
