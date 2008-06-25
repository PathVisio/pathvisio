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

import java.awt.Component;

import javax.swing.JFileChooser;

import org.pathvisio.Engine;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.swing.SimpleFileFilter;

/**
 * user interface functions for single-file Derby databases.
 * swing version.
 * There is an identically named class for swt
 */
public class DBConnDerby extends DataDerby implements DBConnectorSwing
{
	static final String DB_EXT_NAME_GEX = "Expression datasets";
	static final String DB_EXT_NAME_GDB = "Synonym databases";
	
	public String openChooseDbDialog(Component parent) 
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		if (getDbType() == TYPE_GDB)
		{
			jfc.setCurrentDirectory(Engine.getCurrent().getPreferences().getFile(GlobalPreference.DIR_LAST_USED_PGDB));
			jfc.addChoosableFileFilter(new SimpleFileFilter(DB_EXT_NAME_GDB, "*." + DB_FILE_EXT_GDB, true));
		}
		else
		{
			jfc.setCurrentDirectory(Engine.getCurrent().getPreferences().getFile(GlobalPreference.DIR_LAST_USED_PGEX));
			jfc.addChoosableFileFilter(new SimpleFileFilter(DB_EXT_NAME_GEX, "*." + DB_FILE_EXT_GEX, true));
		}
		
		int status = jfc.showDialog (parent, "Open database");
		if(status == JFileChooser.APPROVE_OPTION) 
		{
			if (getDbType() == TYPE_GDB)
			{
				Engine.getCurrent().getPreferences().setFile (GlobalPreference.DIR_LAST_USED_PGDB, jfc.getCurrentDirectory());
			}
			else
			{
				Engine.getCurrent().getPreferences().setFile (GlobalPreference.DIR_LAST_USED_PGEX, jfc.getCurrentDirectory());
			}
			return jfc.getSelectedFile().toString();
		}
		return null;
	}
	
	public String openNewDbDialog(Component parent, String defaultName) 
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		
		if (getDbType() == TYPE_GDB)
		{
			jfc.setCurrentDirectory(Engine.getCurrent().getPreferences().getFile(GlobalPreference.DIR_LAST_USED_PGDB));
			jfc.addChoosableFileFilter(new SimpleFileFilter(DB_EXT_NAME_GDB, "*." + DB_FILE_EXT_GDB, true));		}
		else
		{
			jfc.setCurrentDirectory(Engine.getCurrent().getPreferences().getFile(GlobalPreference.DIR_LAST_USED_PGEX));
			jfc.addChoosableFileFilter(new SimpleFileFilter(DB_EXT_NAME_GEX, "*." + DB_FILE_EXT_GEX, true));
		}

		int status = jfc.showDialog (parent, "Choose filename for database");
		if(status == JFileChooser.APPROVE_OPTION) 
		{
			if (getDbType() == TYPE_GDB)
			{
				jfc.setCurrentDirectory(Engine.getCurrent().getPreferences().getFile(GlobalPreference.DIR_LAST_USED_PGDB));
			}
			else
			{
				jfc.setCurrentDirectory(Engine.getCurrent().getPreferences().getFile(GlobalPreference.DIR_LAST_USED_PGEX));
			}
			return jfc.getSelectedFile().toString();
		}
		return null;
	}

}
