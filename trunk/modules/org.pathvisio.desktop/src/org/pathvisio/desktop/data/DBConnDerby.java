// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.desktop.data;

import java.awt.Component;

import javax.swing.JFileChooser;

import org.bridgedb.gui.SimpleFileFilter;
import org.bridgedb.rdb.construct.DataDerby;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;

/**
 * user interface functions for single-file Derby databases.
 */
public class DBConnDerby extends DataDerby implements DBConnectorSwing
{
	static final String DB_EXT_NAME_GEX = "Expression datasets";
	static final String DB_EXT_NAME_GDB = "Synonym databases";

	//TODO: reduce redundancy between openChooseDbDialog and openNewDbDialog,
	public String openChooseDbDialog(Component parent)
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		if (getDbType() == TYPE_GDB)
		{
			jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_PGDB));
			jfc.addChoosableFileFilter(new SimpleFileFilter(DB_EXT_NAME_GDB, "*.bridge|*.pgdb", true));
		}
		else
		{
			jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_PGEX));
			jfc.addChoosableFileFilter(new SimpleFileFilter(DB_EXT_NAME_GEX, "*." + DB_FILE_EXT_GEX, true));
		}

		int status = jfc.showDialog (parent, "Open database");
		if(status == JFileChooser.APPROVE_OPTION)
		{
			if (getDbType() == TYPE_GDB)
			{
				PreferenceManager.getCurrent().setFile (GlobalPreference.DIR_LAST_USED_PGDB, jfc.getCurrentDirectory());
			}
			else
			{
				PreferenceManager.getCurrent().setFile (GlobalPreference.DIR_LAST_USED_PGEX, jfc.getCurrentDirectory());
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
			jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_PGDB));
			jfc.addChoosableFileFilter(new SimpleFileFilter(DB_EXT_NAME_GDB, "*." + DB_FILE_EXT_GDB, true));
		}
		else
		{
			jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(GlobalPreference.DIR_LAST_USED_PGEX));
			jfc.addChoosableFileFilter(new SimpleFileFilter(DB_EXT_NAME_GEX, "*." + DB_FILE_EXT_GEX, true));
		}

		int status = jfc.showDialog (parent, "Choose filename for database");
		if(status == JFileChooser.APPROVE_OPTION)
		{
			if (getDbType() == TYPE_GDB)
			{
				PreferenceManager.getCurrent().setFile (GlobalPreference.DIR_LAST_USED_PGDB, jfc.getCurrentDirectory());
			}
			else
			{
				PreferenceManager.getCurrent().setFile (GlobalPreference.DIR_LAST_USED_PGEX, jfc.getCurrentDirectory());
			}
			return jfc.getSelectedFile().toString();
		}
		return null;
	}

}
