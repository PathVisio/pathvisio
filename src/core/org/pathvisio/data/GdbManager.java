// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.data;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;

/**
 * GdbManager is responsible for maintaining a single
 * static Gene database for use in the GUI application
 * 
 * This gene database could be a SimpleGdb, 
 * DoubleGdb or aggregateGdb or otherwise.
 * 
 * This class is not needed in headless mode. 
 */
public class GdbManager 
{
	static private DoubleGdb currentGdb = new DoubleGdb();
	
	static public Gdb getCurrentGdb ()
	{
		return currentGdb;
	}
	
	/**
	 * Returns true if there is currently a non-null
	 * gdb and it isConnected()
	 */
	static public boolean isConnected()
	{
		return 
			currentGdb != null &&
			currentGdb.isConnected();
	}

	/**
	 * Set the global metabolite database 
	 * with the given file- or
	 * directory name.
	 * The database type used for the connection
	 * depends on the value of the DB_ENGINE_GDB value
	 * 
	 * use null to disconnect the current db
	 */
	public static void setMetaboliteDb(String dbName) throws DataException
	{
		if (dbName == null)
		{
			currentGdb.setMetaboliteDb(null);
		}
		else
		{
			SimpleGdb gdb = connect (dbName);
			currentGdb.setMetaboliteDb(gdb);
		}
		
		ApplicationEvent e =
			new ApplicationEvent (Engine.getCurrent(), ApplicationEvent.GDB_CONNECTED);
		Engine.getCurrent().fireApplicationEvent (e);
		Logger.log.trace("Current Gene Database: " + dbName);
	
	}

	/**
	 * Set the global gene database
	 * with the given file- or
	 * directory name.
	 * The database type used for the connection
	 * depends on the value of the DB_ENGINE_GDB value
	 * 
	 * use null to disconnect the current db.
	 */
	public static void setGeneDb(String dbName) throws DataException
	{
		if (dbName == null)
		{
			currentGdb.setGeneDb(null);
		}
		else
		{
			SimpleGdb gdb = connect (dbName);
			currentGdb.setGeneDb(gdb);
		}
		ApplicationEvent e =
			new ApplicationEvent (Engine.getCurrent(), ApplicationEvent.GDB_CONNECTED);
		Engine.getCurrent().fireApplicationEvent (e);
		Logger.log.trace("Current Gene Database: " + dbName);
	}
	
	/**
	 * Helper method
	 * Connect to a database using the 
	 * DBConnector set in the global preferences.
	 */
	private static SimpleGdb connect(String gdbName) throws DataException
	{
		DBConnector con;
		try
		{
			con = Engine.getCurrent().getDbConnector(DBConnector.TYPE_GDB);
		}
		catch (ClassNotFoundException e)
		{
			throw new DataException (e);
		}
		catch (IllegalAccessException e)
		{
			throw new DataException (e);
		}
		catch (InstantiationException e)
		{
			throw new DataException (e);
		}
		
		SimpleGdb gdb = new SimpleGdb(gdbName, con, DBConnector.PROP_NONE);
		return gdb;
	}
	
	/**
	 * Initiates this class. Checks the preferences for a previously
	 * used Gene Database and tries to open a connection if found.
	 * If that doesn't work, reverts attempts to use the default value for
	 * that property.
	 * 
	 * Idem for the metabolite database.
	 */
	public static void init()
	{
		PreferenceManager prefs = Engine.getCurrent().getPreferences();
		// first do the Gene database
		String gdbName = prefs.get (GlobalPreference.DB_GDB_CURRENT);
		if(!gdbName.equals("") && !prefs.isDefault (GlobalPreference.DB_GDB_CURRENT))
		{
			try 
			{
				setGeneDb(gdbName);
			} 
			catch(DataException e) 
			{
				Logger.log.error("Setting previous Gdb failed.", e);
				try 
				{
					gdbName = GlobalPreference.DB_GDB_CURRENT.getDefault();
					setGeneDb(gdbName);
				} 
				catch(DataException f) 
				{
					Logger.log.error("Setting default Gdb failed.", f);
				}
			}
		}
		// then do the Metabolite database
		gdbName = prefs.get(GlobalPreference.DB_METABDB_CURRENT);
		if(!gdbName.equals("") && !prefs.isDefault(GlobalPreference.DB_METABDB_CURRENT))
		{
			try 
			{
				setMetaboliteDb(gdbName);
			} 
			catch(Exception e) 
			{
				Logger.log.error("Setting previous Metabolite db failed.", e);
				try 
				{
					gdbName = GlobalPreference.DB_GDB_CURRENT.getDefault();
					setMetaboliteDb(gdbName);
				} 
				catch(Exception f) 
				{
					Logger.log.error("Setting default Metabolite db failed.", f);
				}
			}
		}
	}
}
