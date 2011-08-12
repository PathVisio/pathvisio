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
package org.pathvisio.core.preferences;

import java.awt.Color;
import java.io.File;

import org.pathvisio.core.util.ColorConverter;
import org.pathvisio.core.util.Utils;

/**
 * Type-safe set of preferences.
 * The preferences of PathVisio are backed by a standard Properties() object,
 * but that uses String as key, which is not safe.
 *
 * This enum is used to access the preferences in a type safe way.
 *
 * The preferences in this enum are used by the
 * core and the GUI application, but not by plug-ins.
 */
public enum GlobalPreference implements Preference
{
	FILE_LOG(new File (getApplicationDir(), "PathVisio.log")),
	WP_FILE_LOG(new File (getApplicationDir(), "WikiPathways.log")),

	COLOR_NO_CRIT_MET(new Color(180, 220, 180)),
	COLOR_NO_GENE_FOUND(new Color(180, 220, 220)),
	COLOR_NO_DATA_FOUND(new Color(180, 180, 180)),
	COLOR_SELECTED(Color.RED),
	COLOR_HIGHLIGHTED(Color.GREEN),
	COLOR_LINK(Color.BLUE),

	DATANODES_ROUNDED(Boolean.toString(false)),

	// TODO: this should be changed to org.pathvisio.desktop.data.DBConnDerby, 2 releases after 2.0.11
	DB_ENGINE_GEX("org.pathvisio.data.DBConnDerby"),

	@Deprecated
	DB_GDB_CURRENT("none"),
	@Deprecated
	DB_METABDB_CURRENT("none"),

	DB_CONNECTSTRING_GDB("idmapper-pgdb:none"),
	DB_CONNECTSTRING_METADB("idmapper-pgdb:none"),

	ENABLE_DOUBLE_BUFFERING(Boolean.toString(true)),
	SHOW_ADVANCED_PROPERTIES(Boolean.toString(false)),
	MIM_SUPPORT(Boolean.toString(true)),
	SNAP_TO_ANGLE (Boolean.toString(false)),
	SNAP_TO_ANGLE_STEP ("15"),

	SNAP_TO_ANCHOR(Boolean.toString(true)),

	GUI_SIDEPANEL_SIZE("30"),

	// pathway base dir
	DIR_PWFILES(new File(getDataDir(), "pathways").toString()),
	// gdb base dir
	DIR_GDB(new File(getDataDir(), "gene databases").toString()),
	// expr base dir
	DIR_EXPR(new File(getDataDir(), "expression datasets").toString()),

	// pathway last used dir
	DIR_LAST_USED_OPEN(new File(getDataDir(), "pathways").toString()),
	// pathway last used dir
	DIR_LAST_USED_SAVE(new File(getDataDir(), "pathways").toString()),
	// gdb last used dir
	DIR_LAST_USED_PGDB(new File(getDataDir(), "gene databases").toString()),
	// expr last used dir
	DIR_LAST_USED_PGEX(new File(getDataDir(), "expression datasets").toString()),
	// seach pane last used dir
	DIR_LAST_USED_SEARCHPANE(new File(getDataDir(), "pathways").toString()),

	DIR_LAST_USED_EXPRESSION_IMPORT(new File(getDataDir(), "expression datasets").toString()),

	DIR_LAST_USED_IMPORT(new File(getDataDir(), "pathways").toString()),
	DIR_LAST_USED_EXPORT(new File(getDataDir(), "pathways").toString()),

	MOST_RECENT_1 ((File)null),
	MOST_RECENT_2 ((File)null),
	MOST_RECENT_3 ((File)null),
	MOST_RECENT_4 ((File)null),
	MOST_RECENT_5 ((File)null),
	MOST_RECENT_6 ((File)null),
	MOST_RECENT_7 ((File)null),
	MOST_RECENT_8 ((File)null),
	MOST_RECENT_9 ((File)null),
	MOST_RECENT_10 ((File)null),

	WIN_X ("50"),
	WIN_Y ("50"),
	WIN_W ("800"),
	WIN_H ("600"),

	// don't use system look and feel by default on linux.
	USE_SYSTEM_LOOK_AND_FEEL(
			Boolean.toString(Utils.getOS() != Utils.OS_LINUX)
		),

	MAX_NR_CITATIONS ("" + 5),

	//Whether to convert text to paths in SVG export
	//Default to false, better performance in SVG renderers
	SVG_TEXT_AS_PATH ("" + false)
	;
	GlobalPreference(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	GlobalPreference(Color defaultValue)
	{
		this.defaultValue = ColorConverter.getRgbString(defaultValue);
	}

	GlobalPreference(File defaultValue)
	{
		this.defaultValue = "" + defaultValue;
	}

	private String defaultValue;

	public String getDefault() {
		return defaultValue;
	}

	private static File dirApplication = null;
	private static File dirData = null;
	private static File dirPlugin = null;

	/**
	 * Get the working directory of this application
	 */
	public static File getApplicationDir() {
		if(dirApplication == null) {
			dirApplication = new File(System.getProperty("user.home"), ".PathVisio");
			if(!dirApplication.exists()) dirApplication.mkdir();
		}
		return dirApplication;
	}
	
	public static File getPluginDir() {
		if(dirApplication == null) {
			getApplicationDir();
		}
		if(dirPlugin == null) {
			dirPlugin = new File(getApplicationDir(),"plugins");
			if(!dirPlugin.exists()) dirPlugin.mkdir();
		}
		return dirPlugin;
	}

	public static File getDataDir() {
		if(dirData == null) {
			dirData = new File(System.getProperty("user.home"), "PathVisio-Data");
			if(!dirData.exists()) dirData.mkdir();
		}
		return dirData;
	}
}
