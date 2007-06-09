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
package org.pathvisio.preferences;

import java.io.File;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.pathvisio.gui.Engine;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.view.Graphics;

/**
 * This class contains all user preferences used in this application
 */
public class Preferences extends PreferenceStore implements IPropertyChangeListener {
	private static final File preferenceFile = new File(Engine.getApplicationDir(), ".PathVisio");
	
	public Preferences() {
		this(preferenceFile.toString());
	}
	
	public Preferences(String fileName) {
		super(fileName);
		loadPreferences();
	}
	
	/**
	 * Loads all stored users preferences and set defaults
	 */
	public void loadPreferences()
	{
		addPropertyChangeListener(this);
		
		setDefault(PREF_FILES_LOG, new File(Engine.getApplicationDir(), ".PathVisioLog").toString());
		setDefault(PREF_COL_NO_CRIT_MET, ColorConverter.getRgbString(NO_CRITERIA_MET));
		setDefault(PREF_COL_NO_GENE_FOUND, ColorConverter.getRgbString(NO_GENE_FOUND));
		setDefault(PREF_COL_NO_DATA_FOUND, ColorConverter.getRgbString(NO_DATA_FOUND));
		setDefault(PREF_COL_SELECTED, ColorConverter.getRgbString(SELECTED));
		setDefault(PREF_COL_HIGHLIGHTED, ColorConverter.getRgbString(HIGHLIGHTED));
//		setDefault(PREF_COL_AMBIGIOUS_REP, ColorConverter.getRgbString(AMBIGIOUS_REP));
		setDefault(PREF_CURR_GDB, CURRENT_GDB);
		setDefault(PREF_DB_ENGINE_GDB, DB_ENGINE_GDB);
		setDefault(PREF_DB_ENGINE_EXPR, DB_ENGINE_EXPR);
		setDefault(PREF_SIDEPANEL_SIZE, SIDEPANEL_SIZE);
		
		setDefault(PREF_DIR_PWFILES, DIR_PWFILES);
		setDefault(PREF_DIR_GDB, DIR_GDBFILES);
		setDefault(PREF_DIR_EXPR, DIR_EXPRFILES);
		setDefault(PREF_DIR_RDATA, DIR_RDATAFILES);
		
		try {
			load();
		} catch(Exception e) { 
			try { save(); } catch(Exception ex) { } 
		}
		
		createDataDirectories();
		
	}

	private void createDataDirectories() {
		// For the data directories: if not defined by user, create default directories
		String[] dataProps = new String[] 
		{ 
			PREF_DIR_EXPR, PREF_DIR_GDB, 
			PREF_DIR_PWFILES, PREF_DIR_RDATA 
		};
		
		for(String prop : dataProps) {
			File dir = new File(getString(prop));
			if(!dir.exists()) dir.mkdirs();
		}
	}
	
	/**
	 * Returns the current value of the {@link RGB}-valued preference with  the given name
	 * @param name the name of the preference
	 * @return the {@link RGB}-valued preference
	 */
	public static RGB getColorProperty(String name) {
		return ColorConverter.parseRgbString(
				Engine.getPreferences().getString(name));
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		if(e.getProperty().equals(Preferences.PREF_COL_SELECTED)) { 
			if(e.getNewValue() instanceof RGB) Graphics.selectColor = (RGB)e.getNewValue();
			else Graphics.selectColor = ColorConverter.parseRgbString((String)e.getNewValue());
			Engine.getVPathway().redraw();
		}
		else if(e.getProperty().equals(Preferences.PREF_COL_HIGHLIGHTED)) {
			if(e.getNewValue() instanceof RGB) Graphics.highlightColor = (RGB)e.getNewValue();
			else Graphics.highlightColor = ColorConverter.parseRgbString((String)e.getNewValue());
			Engine.getVPathway().redraw();
		}
//		else if(e.getProperty().equals(Preferences.PREF_COL_AMBIGIOUS_REP)) {
//			if(e.getNewValue() instanceof RGB) GmmlGpColor.color_ambigious = (RGB)e.getNewValue();
//			else GmmlGpColor.color_ambigious = ColorConverter.parseRgbString((String)e.getNewValue());
//			Engine.getDrawing().redraw();
//		}
		else if(e.getProperty().startsWith("directories")) {
			createDataDirectories();
		}
	}
	
	// Preference names
	public static final String PREF_FILES_LOG = "files.log";
	
	public static final String PREF_COL_NO_CRIT_MET = "colors.no_criteria_met";
	public static final String PREF_COL_NO_GENE_FOUND = "colors.no_gene_found";
	public static final String PREF_COL_NO_DATA_FOUND = "colors.no_data_found";
	public static final String PREF_COL_SELECTED = "colors.selectColor";
	public static final String PREF_COL_HIGHLIGHTED = "colors.highlightColor";
//	public static final String PREF_COL_AMBIGIOUS_REP = "colors.ambigious_reporter";
		
	public static final String PREF_DIR_PWFILES = "directories.pathwayFiles";
	public static final String PREF_DIR_GDB = "directories.gdbFiles";
	public static final String PREF_DIR_EXPR = "directories.exprFiles";
	public static final String PREF_DIR_RDATA = "directories.RdataFiles";
		
	public static final String PREF_CURR_GDB = "currentGdb";
	public static final String PREF_DB_ENGINE_GDB = "dbengine.gdb";
	public static final String PREF_DB_ENGINE_EXPR = "dbengine.expr";
	public static final String PREF_SIDEPANEL_SIZE = "display.sidePanelSize";	
	
	// Defaults
	//> display
	//|> sidepanel size (percent)
	static int SIDEPANEL_SIZE = 30;
	//|> colors
	static RGB NO_CRITERIA_MET = new RGB(200, 200, 200);
	static RGB NO_GENE_FOUND = new RGB(255, 255, 255);
	static RGB NO_DATA_FOUND = new RGB(100, 100, 100);
	static RGB SELECTED = new RGB(255, 0, 0);
	static RGB HIGHLIGHTED = new RGB(0, 255, 0);
	static RGB AMBIGIOUS_REP = new RGB(0, 0, 255);
	
	// current gene database
	static String CURRENT_GDB = "none";

	//database engine
	static String DB_ENGINE_EXPR = "org.pathvisio.data.DBConnDerby";
	static String DB_ENGINE_GDB = "org.pathvisio.data.DBConnDerby";
	
	// directories
	static final String DIR_PWFILES = new File(Engine.getDataDir().toString(), "pathways").toString();
	static final String DIR_GDBFILES = new File(Engine.getDataDir().toString(), "gene databases").toString();
	static final String DIR_EXPRFILES = new File(Engine.getDataDir().toString(), "expression datasets").toString();
	static final String DIR_RDATAFILES = new File(Engine.getDataDir().toString(), "R data").toString();
}