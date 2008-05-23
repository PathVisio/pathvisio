package org.pathvisio.preferences;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

import org.pathvisio.debug.Logger;
import org.pathvisio.util.ColorConverter;

/**
 * Loads & saves application preferences
 */
public class PreferenceManager 
{
	private Properties properties;
	private File propFile = null;
	
	boolean dirty;
	
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
			properties.load(new FileInputStream(propFile));
		}
		catch (IOException e)
		{
			Logger.log.error ("Could not read properties", e);
		}
		dirty = false;
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
		String oldVal = properties.getProperty(p.name());
		
		if (oldVal == null ? newVal == null : oldVal.equals (newVal))
		{
			// newVal is equal to oldVal, do nothing
		}
		else
		{
			properties.setProperty(p.name(), newVal);
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
		String key = p.name();
		String value = properties.getProperty(key);
		return (value == null ? p.getDefault() == null : value.equals (p.getDefault()));
	}	
}
