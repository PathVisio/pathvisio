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

import org.pathvisio.gmmlVision.GmmlVision;

import java.sql.DriverManager;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import org.pathvisio.util.FileUtils;

public class DBConnDerbyDirectory extends DBConnDerby {	
	String lastDbName;
		
	public String finalizeNewDatabase(String dbName) throws Exception {
		try {
			DriverManager.getConnection("jdbc:derby:" + FileUtils.removeExtension(dbName) + ";shutdown=true");
		} catch(Exception e) {
			GmmlVision.log.error("Database closed", e);
		}
		return dbName;
	}
	
	public String openChooseDbDialog(Shell shell) {
		DirectoryDialog dd = createDirectoryDialog(shell);
		return dd.open();
	}

	public String openNewDbDialog(Shell shell, String defaultName) {
		DirectoryDialog dd = createDirectoryDialog(shell);
		if(defaultName != null) dd.setFilterPath(defaultName);
		return dd.open();
	}
}
