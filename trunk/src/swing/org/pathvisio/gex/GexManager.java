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
package org.pathvisio.gex;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.bridgedb.IDMapperException;
import org.bridgedb.rdb.DBConnector;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;

/**
 * Manage the centralized SimpleGex
 *
 * Use one of the two setCurrentGex methods
 * to connect to a new Gex database.
 *
 * Register for GexManagerEvent if you want to be notified
 * when a Gex is connected or closed.
 *
 */
public class GexManager
{
	private static GexManager gexManager = new GexManager();

	@Deprecated
	public static GexManager getCurrent()
	{
		return gexManager;
	}

	private SimpleGex currentGex = null;
	public SimpleGex getCurrentGex() { return currentGex; }

	private CachedData cachedData = null;
	public CachedData getCachedData() { return cachedData; }

	/**
	 * Returns true if the current gex is initialized
	 * (non-null), and if it is connected.
	 * If it returns true it is safe to work with getCurrentGex()
	 */
	public boolean isConnected()
	{
		return
			currentGex != null &&
			currentGex.isConnected();
	}

	/**
	 * Set a premade Gex instance as the current Gex.
	 * Mostly used for testing, when you want to specify
	 * the DBConnector used. The other setCurrentGex() is preferred
	 * for most use cases.
	 *
	 * @param gex a premade Gex instance
	 */
	public void setCurrentGex (SimpleGex gex)
	{
		close(); // close old gex.
		currentGex = gex;
		cachedData = new CachedData(gex);
		fireExpressionDataEvent(new GexManagerEvent(gex, GexManagerEvent.CONNECTION_OPENED));
	}

	/**
	 * Create or connect to a new Gex based on the dbName.
	 * Uses a DBConnector obtained from the preferences.
	 *
	 * @param dbName name of the database (usually file or directory name)
	 * @param create true if you want to create / overwrite a database
	 */
	public void setCurrentGex (String dbName, boolean create) throws IDMapperException
	{
		DBConnector connector;
		try
		{
			connector = getDBConnector();
		}
		catch (IllegalAccessException e)
		{
			throw new IDMapperException (e);
		}
		catch (InstantiationException e)
		{
			throw new IDMapperException (e);
		}
		catch (ClassNotFoundException e)
		{
			throw new IDMapperException (e);
		}
		SimpleGex gex = new SimpleGex (dbName, create, connector);
		setCurrentGex (gex);
	}

	public DBConnector getDBConnector() throws
		ClassNotFoundException,
		InstantiationException,
		IllegalAccessException
	{
		DBConnector connector = null;

		String className = null;
		className = PreferenceManager.getCurrent().get(GlobalPreference.DB_ENGINE_GEX);

		if(className == null) return null;

		Class<?> dbc = Class.forName(className);
		Object o = dbc.newInstance();
		if(o instanceof DBConnector)
		{
			connector = (DBConnector)dbc.newInstance();
			connector.setDbType(DBConnector.TYPE_GEX);
		}

		return connector;
	}

	/**
	 * Close the current Gex, if it wasn't already closed.
	 * Sends a GexManagerEvent around.
	 */
	public void close()
	{
		if (currentGex == null) return; // was already closed.
		fireExpressionDataEvent(new GexManagerEvent(currentGex, GexManagerEvent.CONNECTION_CLOSED));
		try
		{
			currentGex.close();
		}
		catch (IDMapperException e)
		{
			Logger.log.error ("Problem while closing previous gex", e);
		}
		currentGex = null; // garbage collection
		cachedData.dispose();
		cachedData = null;
	}

	/**
	 * Fire a {@link GexManagerEvent} to notify all {@link GexManagerListener}s registered
	 * to this class
	 * @param e
	 */
	private void fireExpressionDataEvent(GexManagerEvent e)
	{
		for(GexManagerListener l : listeners) l.gexManagerEvent(e);
	}

	/**
	 * Implement this interface if you want to receive an event when
	 * an expression dataset is opened / closed
	 */
	public interface GexManagerListener
	{
		public void gexManagerEvent(GexManagerEvent e);
	}

	Set<GexManagerListener> listeners = new HashSet<GexManagerListener>();

	/**
	 * Add a {@link GexManagerListener}, that will be notified if an
	 * event related to expression data occurs
	 * @param l The {@link GexManagerListener} to add
	 */
	public void addListener(GexManagerListener l)
	{
		listeners.add(l);
	}

	public void removeListener(GexManagerListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Events in the centralized Gex.
	 */
	public static class GexManagerEvent extends EventObject
	{

		/** Event passed just after a new Gex is opened */
		public static final int CONNECTION_OPENED = 0;
		/** Event passed just before the current Gex is closed */
		public static final int CONNECTION_CLOSED = 1;

		private int type;
		/** The type, one of CONNECTION_OPENED or CONNECTION_CLOSED */
		public int getType() { return type; }

		/**
		 * @param source the current SimpleGex
		 * @param type one of CONNECTION_OPENED or CONNECTION_CLOSED */
		public GexManagerEvent(Object source, int type)
		{
			super(source);
			this.source = source;
			this.type = type;
		}
	}

}
