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
package org.pathvisio.core.preferences;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.util.ColorConverter;

/**
 * Loads & saves application preferences
 */
public class PreferenceManager
{
	private Properties properties;
	private File propFile = null;
	private Set<PreferenceListener> listeners = new HashSet<PreferenceListener>();
	private boolean dirty;


	public void addListener(PreferenceListener listener) {
		listeners.add(listener);
	}

	private void fireEvent(Preference modifiedPref) {
		PreferenceEvent event = new PreferenceEvent(modifiedPref);
		for (PreferenceListener l : listeners) {
			l.preferenceModified(event);
		}
	}

	/**
	 * Stores preferences back to preference file, if necessary.
	 * Only writes to disk if the properties have changed.
	 */
	public void store()
	{
		if (dirty)
		{
			Logger.log.info ("Preferences have changed. Writing preferences");
			try
			{
				properties.store(new FileOutputStream(propFile), "");
				dirty = false;
			}
			catch (IOException e)
			{
				Logger.log.error ("Could not write properties");
			}
		}
	}

	/**
	 * Load preferences from file
	 */
	public void load()
	{
		properties = new Properties();
		propFile = new File(System.getProperty("user.home") + File.separator +
				".PathVisio" + File.separator + ".PathVisio");

		try
		{
			if(propFile.exists()) {
				properties.load(new FileInputStream(propFile));
				compatUpdate();
			} else {
				Logger.log.info("Preferences file " + propFile + " doesn't exist, using defaults");
			}
		}
		catch (IOException e)
		{
			Logger.log.error ("Could not read properties", e);
		}
		dirty = false;
	}

	/**
	 * Convert old / obsolete properties to new values.
	 * Old properties are left in place for backwards compatibility.
	 */
	private void compatUpdate()
	{
		if (properties.containsKey(GlobalPreference.DB_GDB_CURRENT.name()) &&
			!properties.containsKey(GlobalPreference.DB_CONNECTSTRING_GDB.name()))
			set(GlobalPreference.DB_CONNECTSTRING_GDB, "idmapper-pgdb:" + get(GlobalPreference.DB_GDB_CURRENT));
		if (properties.containsKey(GlobalPreference.DB_METABDB_CURRENT.name()) &&
			!properties.containsKey(GlobalPreference.DB_CONNECTSTRING_METADB.name()))
			set(GlobalPreference.DB_CONNECTSTRING_METADB, "idmapper-pgdb:" + get(GlobalPreference.DB_METABDB_CURRENT));
	}

	/**
	 * Get a preference as String
	 */
	public String get (Preference p)
	{
		String key = p.name();
		if (properties.containsKey(key))
		{
			return properties.getProperty(key);
		}
		else
		{
			return p.getDefault();
		}
	}

	public void set (Preference p, String newVal)
	{
		String oldVal = get(p);

		if (oldVal == null ? newVal == null : oldVal.equals (newVal))
		{
			// newVal is equal to oldVal, do nothing
		}
		else
		{
			if (newVal == null)
				properties.remove(p.name());
			else
				properties.setProperty(p.name(), newVal);
			fireEvent(p);
			dirty = true;
		}
	}

	public int getInt (Preference p)
	{
		return Integer.parseInt (get(p));
	}

	public void setInt (Preference p, int val)
	{
		set (p, "" + val);
	}

	public File getFile (Preference p)
	{
		return new File (get (p));
	}

	public void setFile (Preference p, File val)
	{
		set (p, "" + val);
	}

	public Color getColor (Preference p)
	{
		return ColorConverter.parseColorString(get (p));
	}

	public void setColor (Preference p, Color c)
	{
		set (p, ColorConverter.getRgbString(c));
	}

	public void setBoolean (Preference p, Boolean val)
	{
		set (p, "" + val);
	}

	public boolean getBoolean (Preference p)
	{
		return (get(p).equals (""  + true));
	}

	/**
	 * Returns true if the current value of Preference p equals the default value.
	 */
	public boolean isDefault (Preference p)
	{
		return !properties.containsKey(p.name());
	}


	static PreferenceManager preferences = null;

	/**
	@Deprecated use SwingEngine.getPreferenceManager() instead
	*/ 
	public static PreferenceManager getCurrent()
	{
		return preferences;
	}

	public static void init()
	{
		if (preferences == null)
		{
			preferences = new PreferenceManager();
			preferences.load();
		}
		else
		{
			Logger.log.warn ("PreferenceManager was initialized twice");
		}
	}
}
