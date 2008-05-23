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

import java.awt.Color;
import java.io.File;

import org.pathvisio.Engine;
import org.pathvisio.util.ColorConverter;

public enum GlobalPreference implements Preference {
	FILE_LOG(new File (Engine.getCurrent().getApplicationDir(), ".PathVisioLog")),
	WP_FILE_LOG(new File (Engine.getCurrent().getApplicationDir(), ".wikipathwaysLog")),
	
	COLOR_NO_CRIT_MET(new Color(200, 200, 200)),
	COLOR_NO_GENE_FOUND(Color.WHITE),
	COLOR_NO_DATA_FOUND(new Color(100, 100, 100)),
	COLOR_SELECTED(Color.RED),
	COLOR_HIGHLIGHTED(Color.GREEN),
	
	DATANODES_ROUNDED(Boolean.toString(false)),	
	
	DB_ENGINE_GDB("org.pathvisio.data.DBConnDerby"),
	DB_ENGINE_GEX("org.pathvisio.data.DBConnDerby"),
	DB_GDB_CURRENT("none"),
	DB_METABDB_CURRENT("none"),
	
	SHOW_ADVANCED_ATTRIBUTES(Boolean.toString(false)),
	MIM_SUPPORT(Boolean.toString(true)),
	SNAP_TO_ANGLE (Boolean.toString(false)),
	SNAP_TO_ANGLE_STEP ("15"),
	
	SNAP_TO_ANCHOR(Boolean.toString(true)),

	GUI_SIDEPANEL_SIZE("30"),
	
	DIR_PWFILES(new File(Engine.getCurrent().getDataDir().toString(), "pathways").toString()),
	DIR_GDB(new File(Engine.getCurrent().getDataDir().toString(), "gene databases").toString()),
	DIR_EXPR(new File(Engine.getCurrent().getDataDir().toString(), "expression datasets").toString())
	
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
	
	public void setDefault(String defValue) {
		defaultValue = defValue;
	}
		
}
