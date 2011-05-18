// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
package org.pathvisio.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;

/**
 * GdbManager is responsible for maintaining a single
 * static Gene database for use in the GUI application
 *
 * This gene database could be a SimpleGdb,
 * DoubleGdb or aggregateGdb or otherwise.
 *
 * This class is not needed in headless mode.
 */
public class GdbManager extends AbstractListModel
{
	private final IDMapperStack currentGdb = new IDMapperStack();
	private IDMapper metabolites;
	private IDMapper genes;
	private Map<IDMapper, String> connectionStrings = new HashMap<IDMapper, String>();

	public GdbManager()
	{
		try
		{
			Class.forName ("org.bridgedb.file.IDMapperText");
			Class.forName ("org.bridgedb.rdb.IDMapperRdb");
		}
		catch (ClassNotFoundException ex)
		{
			Logger.log.error("Could not initilize GDB Manager", ex);
			//TODO: propagate exception???
		}
	}

	public IDMapperStack getCurrentGdb ()
	{
		return currentGdb;
	}

	/**
	 * Returns true if the current Gdb isConnected()
	 */
	public boolean isConnected()
	{
		return currentGdb.isConnected();
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
	public void setMetaboliteDb(String connectString) throws IDMapperException
	{
		removeMapper(metabolites);
		metabolites = null;
		if (connectString != null)
		{
			metabolites = BridgeDb.connect(connectString);
			if (metabolites != null)
			{
				PreferenceManager.getCurrent().set(GlobalPreference.DB_CONNECTSTRING_METADB, (connectString));
				addMapper(metabolites, connectString);
			}
		}
	}

	public void addMapper(String connectionString) throws IDMapperException
	{
		IDMapper mapper = BridgeDb.connect (connectionString);
		addMapper (mapper, connectionString);
	}

	public void addMapper(IDMapper mapper, String connectionString) throws IDMapperException
	{
		if (mapper == null) throw new NullPointerException();
		currentGdb.addIDMapper(mapper);
		connectionStrings.put(mapper, connectionString);
		GdbEvent e = new GdbEvent (this, GdbEvent.Type.ADDED, mapper.toString());
		fireGdbEvent (e);
		Logger.log.trace("Added database: " + mapper.toString());
	}

	public void removeMapper(IDMapper mapper) throws IDMapperException
	{
		if (mapper == null) return; // ignore
		currentGdb.removeIDMapper(mapper);
		connectionStrings.remove(mapper);
		if (mapper == metabolites) metabolites = null;
		if (mapper == genes) genes = null;
		GdbEvent e = new GdbEvent (this, GdbEvent.Type.REMOVED, mapper.toString());
		fireGdbEvent (e);
		mapper.close();
	}

	/**
	 * Implement this interface if you want to listen to Gdb Events.
	 */
	public interface GdbEventListener
	{
		public void gdbEvent(GdbEvent e);
	}

	/**
	 * Use this method if you want to respond to the connection of Gdb databases
	 */
	public void addGdbEventListener(GdbEventListener l)
	{
		if (l == null) throw new NullPointerException();
		gdbEventListeners.add(l);
	}

	public void removeGdbEventListener(GdbEventListener l) {
		gdbEventListeners.remove(l);
	}

	private void fireGdbEvent (GdbEvent e)
	{
		for(GdbEventListener l : gdbEventListeners) l.gdbEvent(e);
		// also notify ListModel listeners
		this.fireContentsChanged(this, 0, currentGdb.getSize());
	}

	private List<GdbEventListener> gdbEventListeners  = new ArrayList<GdbEventListener>();

	/**
	 * Set the global gene database
	 * with the given file- or
	 * directory name.
	 * The database type used for the connection
	 * depends on the value of the DB_ENGINE_GDB value
	 *
	 * use null to disconnect the current db.
	 */
	public void setGeneDb(String connectString) throws IDMapperException
	{
		removeMapper(genes);
		genes = null;
		if (connectString != null)
		{
			genes = BridgeDb.connect(connectString); 
			if (genes != null)
			{
				PreferenceManager.getCurrent().set(GlobalPreference.DB_CONNECTSTRING_GDB, (connectString));
				addMapper(genes, connectString);
			}
		}
	}

	/**
	 * Initiates this class. Checks the preferences for a previously
	 * used Gene Database and tries to open a connection if found.
	 * If that doesn't work, reverts attempts to use the default value for
	 * that property.
	 *
	 * Idem for the metabolite database.
	 * TODO: move to src/swing (only used standalone)
	 */
	public void initPreferred()
	{
		PreferenceManager prefs = PreferenceManager.getCurrent();
		// first do the Gene database
		String gdbName = prefs.get (GlobalPreference.DB_CONNECTSTRING_GDB);
		if(!gdbName.equals("") && !prefs.isDefault (GlobalPreference.DB_CONNECTSTRING_GDB))
		{
			try
			{
				setGeneDb(gdbName);
			}
			catch(IDMapperException e)
			{
				Logger.log.error("Setting previous Gdb failed.", e);
			}
		}
		// then do the Metabolite database
		gdbName = prefs.get(GlobalPreference.DB_CONNECTSTRING_METADB);
		if(!gdbName.equals("") && !prefs.isDefault (GlobalPreference.DB_CONNECTSTRING_METADB))
		{
			try
			{
				setMetaboliteDb(gdbName);
			}
			catch(Exception e)
			{
				Logger.log.error("Setting previous Metabolite db failed.", e);
			}
		}
	}

	public Object getElementAt(int arg0)
	{
		return currentGdb.getIDMapperAt(arg0);
	}

	public String getConnectionStringAt(int arg0)
	{
		return connectionStrings.get(currentGdb.getIDMapperAt(arg0));
	}
	
	public int getSize()
	{
		return currentGdb.getSize();
	}

	public IDMapper getGeneDb() { return genes; }
	public IDMapper getMetaboliteDb() { return metabolites; }
}
