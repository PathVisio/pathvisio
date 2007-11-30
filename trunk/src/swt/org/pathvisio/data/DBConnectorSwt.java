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
import org.pathvisio.preferences.swt.SwtPreferences.SwtPreference;

/**
 * This class provides the connection for the databases (annotation and expression database) used
 * in PathVisio. Implement the abstract methods when you want to add support for a new database engine.
 * @author Thomas
 */
public abstract class DBConnectorSwt extends DBConnector {
	
	/**
	 * This method will be called when the user
	 * needs to select a database. Open a dialog (e.g. FileDialog) in this
	 * method to let the user select the database and return the database name.
	 * @param shell The shell to create the dialog
	 * @return The database name that was selected by the user, or null if no database was selected
	 */
	public abstract String openChooseDbDialog(Shell shell);
	
	/**
	 * This method will be called when the user
	 * needs to select a database to create. Open a dialog (e.g. FileDialog) in this
	 * method to let the user select the new database name/file/directory and return the database name.
	 * @param shell The shell to create the dialog
	 * @return The database name to create, or null if no database was specified
	 */
	public abstract String openNewDbDialog(Shell shell, String defaultName);
	
	/**
	 * Shortcut for creating a file dialog that has the right default directories for
	 * the database type of this connector
	 * @param shell
	 * @param type
	 * @param filterExtensions
	 * @param filterNames
	 * @return A file dialog with the default directories set
	 */
	protected FileDialog createFileDialog(Shell shell, int type, String[] filterExtensions, String[] filterNames) {
		FileDialog fileDialog = new FileDialog(shell, type);
		fileDialog.setText("Select database file");
		
		String filterPath = null;
		switch(getDbType()) {
		case TYPE_GDB: 
			filterPath = SwtPreference.SWT_DIR_GDB.getValue();
			break;
		case TYPE_GEX:
			filterPath = SwtPreference.SWT_DIR_EXPR.getValue();
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
	protected DirectoryDialog createDirectoryDialog(Shell shell) {
		DirectoryDialog dirDialog = new DirectoryDialog(shell, SWT.NONE);
		dirDialog.setText("Select database file");
		
		String filterPath = null;
		switch(getDbType()) {
		case TYPE_GDB: 
			filterPath = SwtPreference.SWT_DIR_GDB.getValue();
			break;
		case TYPE_GEX:
			filterPath = SwtPreference.SWT_DIR_EXPR.getValue();
			break;
		}
		if(filterPath != null) dirDialog.setFilterPath(filterPath);
		
		return dirDialog;
	}
}
