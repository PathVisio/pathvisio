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
package org.pathvisio.preferences.swt;

import java.io.File;
import java.io.IOException;

import java.util.*;

import javax.naming.OperationNotSupportedException;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.PreferenceManager;

/**
 * This class contains all user preferences used in this application
 */
public class SwtPreferences implements IPreferenceStore//, IPropertyChangeListener 
{	
	public void propertyChange(PropertyChangeEvent e) {
//		Preference p = byName(e.getProperty());
//		if(p != null) {
//			p.setValue(e.getNewValue().toString());
//		}
//		
//		if(e.getProperty().equals(GlobalPreference.COLOR_SELECTED.name())) { 
//			//if(e.getNewValue() instanceof RGB) Graphics.selectColor = (RGB)e.getNewValue();
//			//else 
//				Graphics.selectColor = ColorConverter.parseColorString((String)e.getNewValue());
//			Engine.getCurrent().getActiveVPathway().redraw();
//		}
//		if(e.getProperty().startsWith("SWT_DIR_")) {
//			createDataDirectories();
//		}
	}
	
	PreferenceManager prefs;
	Map <String, Preference> prefMap = new HashMap <String, Preference>();
	
	public SwtPreferences (PreferenceManager prefs)
	{
		this.prefs = prefs;
		
		for (Preference p : GlobalPreference.values())
		{
			prefMap.put (p.name(), p);
		}
	}
	
	public void addPropertyChangeListener(IPropertyChangeListener arg0) {
		// TODO Auto-generated method stub
		throw new IllegalArgumentException("Not implemented");
		
	}

	public boolean contains(String arg0) 
	{
		return prefMap.containsKey(arg0);
	}

	public void firePropertyChangeEvent(String arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub
		throw new IllegalArgumentException("Not implemented");
		
	}

	public boolean getBoolean(String arg0) 
	{
		return prefs.getBoolean (prefMap.get(arg0));
	}

	public boolean getDefaultBoolean(String arg0) 
	{
		return prefMap.get(arg0).getDefault().equals ("" + true);
	}

	public double getDefaultDouble(String arg0) {
		throw new IllegalArgumentException("Not implemented");
		// TODO Auto-generated method stub
//		return 0;
	}

	public float getDefaultFloat(String arg0) {
		throw new IllegalArgumentException("Not implemented");
		// TODO Auto-generated method stub
//		return 0;
	}

	public int getDefaultInt(String arg0) 
	{
		return Integer.parseInt (prefMap.get(arg0).getDefault());
	}

	public long getDefaultLong(String arg0) {
		throw new IllegalArgumentException("Not implemented");
		// TODO Auto-generated method stub
//		return 0;
	}

	public String getDefaultString(String arg0) {
		return prefMap.get(arg0).getDefault();
	}

	public double getDouble(String arg0) {
		throw new IllegalArgumentException("Not implemented");
		// TODO Auto-generated method stub
//		return 0;
	}

	public float getFloat(String arg0) {
		throw new IllegalArgumentException("Not implemented");
		// TODO Auto-generated method stub
//		return 0;
	}

	public int getInt(String arg0) 
	{
		return prefs.getInt(prefMap.get(arg0));
	}

	public long getLong(String arg0) {
		throw new IllegalArgumentException("Not implemented");
		// TODO Auto-generated method stub
//		return 0;
	}

	public String getString(String arg0) 
	{
		return prefs.get(prefMap.get(arg0));
	}

	public boolean isDefault(String arg0) {
		throw new IllegalArgumentException("Not implemented");
		// TODO Auto-generated method stub
//		return false;
	}

	public boolean needsSaving() 
	{
		// not necessarily true, but that doesn't matter.
		// PreferenceManager keeps track by itself if it needs to save or not.
		return true;
	}

	public void putValue(String arg0, String arg1) 
	{
		prefs.set (prefMap.get (arg0), arg1);	
	}

	public void removePropertyChangeListener(IPropertyChangeListener arg0) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setDefault(String arg0, double arg1) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setDefault(String arg0, float arg1) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setDefault(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setDefault(String arg0, long arg1) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setDefault(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setDefault(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setToDefault(String arg0) 
	{
		Preference p = prefMap.get (arg0);
		prefs.set(p, p.getDefault());
		
	}

	public void setValue(String arg0, double arg1) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setValue(String arg0, float arg1) {
		// TODO Auto-generated method stub
		
		throw new IllegalArgumentException("Not implemented");
	}

	public void setValue(String arg0, int arg1) 
	{		
		prefs.setInt (prefMap.get (arg0), arg1);		
	}

	public void setValue(String arg0, long arg1) {
		// TODO Auto-generated method stub
		throw new IllegalArgumentException("Not implemented");
	}

	public void setValue(String arg0, String arg1) 
	{
		prefs.set (prefMap.get (arg0), arg1);		
	}

	public void setValue(String arg0, boolean arg1) 
	{
		prefs.setBoolean (prefMap.get (arg0), arg1);		
	}
}