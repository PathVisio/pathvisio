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

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.PreferenceCollection;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.view.Graphics;

/**
 * This class contains all user preferences used in this application
 */
public class SwtPreferences extends PreferenceStore implements PreferenceCollection, IPropertyChangeListener {
	private static final File preferenceFile = new File(SwtEngine.getCurrent().getApplicationDir(), ".PathVisio");
	
	public SwtPreferences() {
		this(preferenceFile.toString());
	}
	
	public SwtPreferences(String fileName) {
		super(fileName);
		loadPreferences();
	}
	
	public Preference byName(String name) {
		Preference p = null;
		if(name.startsWith("SWT")) {
			p = SwtPreference.valueOf(name);
		} else {
			p = GlobalPreference.valueOf(name);
		}
		return p;
	}
	
	protected void toEnums(Preference[] enumPrefs) {
		for(Preference p : enumPrefs) {
			p.setValue(getString(p.name()));
		}
	}
	
	protected void toEnums() {
		toEnums(SwtPreference.values());
		toEnums(GlobalPreference.values());
	}
	
	protected void fromEnums(Preference[] enumPrefs) {
		for(Preference p : enumPrefs) {
			setValue(p.name(), p.getValue());
		}
	}
	
	protected void fromEnums() {
		fromEnums(SwtPreference.values());
		fromEnums(GlobalPreference.values());
	}
	
	public void save() throws IOException {
		fromEnums();
		super.save();
	}
	
	/**
	 * Loads all stored users preferences and set defaults
	 */
	private void loadPreferences()
	{
		addPropertyChangeListener(this);
		
		for(Preference p : GlobalPreference.values()) {
			setDefault(p);
		}
		
		for(Preference p : SwtPreference.values()) {
			setDefault(p);
		}
		
		try {
			load();
		} catch(Exception e) { 
			Logger.log.error("Unable to load preferences", e);
		}
		
		toEnums();
		
		createDataDirectories();
		
	}
	
	private void setDefault(Preference p) {
		setDefault(p.name(), p.getDefault());
	}
	
	private void createDataDirectories() {
		// For the data directories: if not defined by user, create default directories
		Preference[] dataProps = new Preference[] 
		{ 
			SwtPreference.SWT_DIR_EXPR, SwtPreference.SWT_DIR_GDB, 
			SwtPreference.SWT_DIR_PWFILES, SwtPreference.SWT_DIR_RDATA 
		};
		
		for(Preference prop : dataProps) {
			File dir = new File(prop.getValue());
			if(!dir.exists()) dir.mkdirs();
		}
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		Preference p = byName(e.getProperty());
		if(p != null) {
			p.setValue(e.getNewValue().toString());
		}
		
		if(e.getProperty().equals(GlobalPreference.COLOR_SELECTED.name())) { 
			//if(e.getNewValue() instanceof RGB) Graphics.selectColor = (RGB)e.getNewValue();
			//else 
				Graphics.selectColor = ColorConverter.parseColorString((String)e.getNewValue());
			Engine.getCurrent().getActiveVPathway().redraw();
		}
		if(e.getProperty().startsWith("SWT_DIR_")) {
			createDataDirectories();
		}
	}
	
	public enum SwtPreference implements Preference {
		SWT_DIR_PWFILES(new File(SwtEngine.getCurrent().getDataDir().toString(), "pathways").toString()),
		SWT_DIR_GDB(new File(SwtEngine.getCurrent().getDataDir().toString(), "gene databases").toString()),
		SWT_DIR_EXPR(new File(SwtEngine.getCurrent().getDataDir().toString(), "expression datasets").toString()),
		SWT_DIR_RDATA(new File(SwtEngine.getCurrent().getDataDir().toString(), "R data").toString()),

		;
		
		SwtPreference(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		private String defaultValue;
		private String value;
		
		public String getDefault() {
			return defaultValue;
		}
		
		public void setDefault(String defValue) {
			defaultValue = defValue;
		}
		
		public void setValue(String newValue) {
			value = newValue;
		}
		
		public String getValue() {
			if(value != null) {
				return value;
			} else {
				return defaultValue;
			}
		}
	}
}