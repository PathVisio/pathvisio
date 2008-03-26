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

/**
 * user interface functions for single-file Derby databases.
 * swing version.
 * There is an identically named class for swt
 */
public class DBConnDerby extends DataDerby implements DBConnectorSwing
{
	static final String[] DB_EXTS_GEX = new String[] { "*." + DB_FILE_EXT_GEX, "*.*"};
	static final String[] DB_EXTS_GDB = new String[] { "*." + DB_FILE_EXT_GDB, "*.*"};
	static final String[] DB_EXT_NAMES_GEX = new String[] { "Expression dataset", "All files" };
	static final String[] DB_EXT_NAMES_GDB = new String[] { "Gene database", "All files" };
	
	public String openChooseDbDialog(Component parent) 
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		
		//TODO: select right file filter for gex / gdb
		int status = jfc.showDialog (parent, "Open database");
		if(status == JFileChooser.APPROVE_OPTION) 
		{
			return jfc.getSelectedFile().toString();
		}
		return null;
	}
	
	public String openNewDbDialog(Component parent, String defaultName) 
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		
		//TODO: select right file filter for gex / gdb
		int status = jfc.showDialog (parent, "Choose filename for database");
		if(status == JFileChooser.APPROVE_OPTION) 
		{
			return jfc.getSelectedFile().toString();
		}
		return null;
	}

}
