package org.pathvisio.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.pathvisio.debug.Logger;

public class SimpleGdbFactory 
{
	static final int LATEST_SCHEMA_VERSION = 2;

	/**
	 * Opens a connection to the Gene Database located in the given file
	 * @param dbName The file containing the Gene Database. 
	 * @param connector An instance of DBConnector, to determine the type of database (e.g. DataDerby).
	 * A new instance of DbConnector class is instantiated automatically.
	 * 
	 * Use this instead of constructor to create an instance of SimpleGdb that matches the schema version.
	*/
	public static SimpleGdb createInstance(String dbName, DBConnector newDbConnector, int props) throws DataException
	{
		if(dbName == null) throw new NullPointerException();
		
		Gdb result;
		DBConnector dbConnector;
		Connection con;
		
		try
		{
			// create a fresh db connector of the correct type.
			dbConnector = newDbConnector.getClass().newInstance();
		}
		catch (InstantiationException e)
		{
			throw new DataException (e);
		} 
		catch (IllegalAccessException e) 
		{
			throw new DataException (e);
		}

		Logger.log.trace("Opening connection to Gene Database " + dbName);

		int version = 0;
		if ((props & DBConnector.PROP_RECREATE) == 0)
		{
			// read database and try to determine version
			
			con = dbConnector.createConnection(dbName, props);
			try 
			{
				ResultSet r = con.createStatement().executeQuery("SELECT schemaversion FROM info");
				if(r.next()) version = r.getInt(1);
			} 
			catch (SQLException e) 
			{
				//Ignore, older db's don't even have schema version
			}
			
			try
			{
				con.close();
			}
			catch (SQLException e) 
			{
				//Unable to close database, ignore.
			}
		}
		else
		{
			version = LATEST_SCHEMA_VERSION;
		}
		
		switch (version)
		{
		case 2:
			return new SimpleGdbImpl2(dbName, newDbConnector, props);
		//TODO add new schema versions here
		default:
			throw new DataException ("Unrecognized schema version '" + version + "', please make sure you have the latest " +
					"version of this software and databases");
		}		
	}
}
