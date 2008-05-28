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

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;

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
	private static SimpleGex currentGex = null; 
	public static SimpleGex getCurrentGex() { return currentGex; }
	
	/**
	 * Returns true if the current gex is initialized
	 * (non-null), and if it is connected.
	 * If it returns true it is safe to work with getCurrentGex()
	 */
	public static boolean isConnected()
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
	public static void setCurrentGex (SimpleGex gex)
	{
		close(); // close old gex.
		currentGex = gex;
		fireExpressionDataEvent(new GexManagerEvent(gex, GexManagerEvent.CONNECTION_OPENED));
	}
	
	/**
	 * Create or connect to a new Gex based on the dbName.
	 * Uses a DBConnector obtained from the preferences.
	 * 
	 * @param dbName name of the database (usually file or directory name)
	 * @param create true if you want to create / overwrite a database
	 */
	public static void setCurrentGex (String dbName, boolean create) throws DataException
	{
		DBConnector connector;
		try
		{
			connector = getDBConnector();		
		}
		catch (IllegalAccessException e)
		{
			throw new DataException (e);
		}
		catch (InstantiationException e)
		{
			throw new DataException (e);
		}
		catch (ClassNotFoundException e)
		{
			throw new DataException (e);
		}
		SimpleGex gex = new SimpleGex (dbName, create, connector);
		setCurrentGex (gex);
	}
	
	private static DBConnector getDBConnector() throws 
		ClassNotFoundException, 
		InstantiationException, 
		IllegalAccessException 
	{
		return Engine.getCurrent().getDbConnector(DBConnector.TYPE_GEX);
	}

	/**
	 * Close the current Gex, if it wasn't already closed.
	 * Sends a GexManagerEvent around.
	 */
	public static void close()
	{
		if (currentGex == null) return; // was already closed.
		fireExpressionDataEvent(new GexManagerEvent(currentGex, GexManagerEvent.CONNECTION_CLOSED));	
		try
		{
			currentGex.close();
		}
		catch (DataException e)
		{			
			Logger.log.error ("Problem while closing previous gex", e);
		}
		currentGex = null; // garbage collection
	}
	
	/**
	 * Fire a {@link GexManagerEvent} to notify all {@link GexManagerListener}s registered
	 * to this class
	 * @param e
	 */
	private static void fireExpressionDataEvent(GexManagerEvent e) 
	{
		for(GexManagerListener l : listeners) l.gexManagerEvent(e);
	}
	
	public interface GexManagerListener 
	{
		public void gexManagerEvent(GexManagerEvent e);
	}
	
	static Set<GexManagerListener> listeners = new HashSet<GexManagerListener>();
	
	/**
	 * Add a {@link GexManagerListener}, that will be notified if an
	 * event related to expression data occurs
	 * @param l The {@link GexManagerListener} to add
	 */
	public static void addListener(GexManagerListener l) 
	{
		listeners.add(l);
	}
	
	/**
	 * Events in the centralized Gex.
	 */
	public static class GexManagerEvent extends EventObject 
	{
		private static final long serialVersionUID = 1L;
		
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
