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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class DBConnDerby extends DataDerby implements DBConnectorSwt
{
	static final String[] DB_EXTS_GEX = new String[] { "*." + DB_FILE_EXT_GEX, "*.*"};
	static final String[] DB_EXTS_GDB = new String[] { "*." + DB_FILE_EXT_GDB, "*.*"};
	static final String[] DB_EXT_NAMES_GEX = new String[] { "Expression dataset", "All files" };
	static final String[] DB_EXT_NAMES_GDB = new String[] { "Gene database", "All files" };


	public String openChooseDbDialog(Shell shell)
	{
		FileDialog fd = DBConnectorUtils.createFileDialog(this, shell, SWT.OPEN, getDbExts(), getDbExtNames());
		return fd.open();
	}

	public String openNewDbDialog(Shell shell, String defaultName)
	{
		FileDialog fd = DBConnectorUtils.createFileDialog(this, shell, SWT.SAVE, getDbExts(), getDbExtNames());
		if(defaultName != null) fd.setFileName(defaultName);
		return fd.open();
	}

	String[] getDbExts() {
		switch(getDbType()) {
		case TYPE_GDB: return DB_EXTS_GDB;
		case TYPE_GEX: return DB_EXTS_GEX;
		default: return null;
		}
	}
	
	String[] getDbExtNames() {
		switch(getDbType()) {
		case TYPE_GDB: return DB_EXT_NAMES_GDB;
		case TYPE_GEX: return DB_EXT_NAMES_GEX;
		default: return null;
		}
	}	

}