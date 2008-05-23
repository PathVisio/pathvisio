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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Engine;
import org.pathvisio.preferences.GlobalPreference;

/**
 * Helper functions for classes implementing DbConnectorSwt interface
 */
class DBConnectorUtils
{	
	/**
	 * Shortcut for creating a file dialog that has the right default directories for
	 * the database type of this connector
	 * @param shell
	 * @param type
	 * @param filterExtensions
	 * @param filterNames
	 * @return A file dialog with the default directories set
	 */
	static FileDialog createFileDialog(DBConnector db, Shell shell, int type, String[] filterExtensions, String[] filterNames) {
		FileDialog fileDialog = new FileDialog(shell, type);
		fileDialog.setText("Select database file");
		
		String filterPath = null;
		switch(db.getDbType()) {
		case DBConnector.TYPE_GDB: 
			filterPath = Engine.getCurrent().getPreferences().get(GlobalPreference.DIR_GDB);
			break;
		case DBConnector.TYPE_GEX:
			filterPath = Engine.getCurrent().getPreferences().get(GlobalPreference.DIR_EXPR);
			break;
		}
		if(filterPath != null) fileDialog.setFilterPath(filterPath);
		if(filterExtensions != null) fileDialog.setFilterExtensions(filterExtensions);
		if(filterNames != null) fileDialog.setFilterNames(filterNames);
		
		return fileDialog;
	}
	
	/**
	 * Shortcut for creating a directory dialog that has the right default directories for
	 * the database type of this connector
	 * @param shell
	 * @return A directory dialog with the default directories set
	 */
	static DirectoryDialog createDirectoryDialog(DBConnector db, Shell shell) {
		DirectoryDialog dirDialog = new DirectoryDialog(shell, SWT.NONE);
		dirDialog.setText("Select database file");
		
		String filterPath = null;
		switch(db.getDbType()) {
		case DBConnector.TYPE_GDB: 
			filterPath = Engine.getCurrent().getPreferences().get(GlobalPreference.DIR_GDB);
			break;
		case DBConnector.TYPE_GEX:
			filterPath = Engine.getCurrent().getPreferences().get(GlobalPreference.DIR_EXPR);
			break;
		}
		if(filterPath != null) dirDialog.setFilterPath(filterPath);
		
		return dirDialog;
	}
}