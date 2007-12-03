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

import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.GlobalPreference;

/**
 * GdbManager is responsible for maintaining a single
 * static Gene database for use in the GUI application
 * 
 * This gene database could be a SimpleGdb, 
 * DoubleGdb or aggregateGdb or otherwise.
 * 
 * TODO: this class is GUI-only. Move to either swt or swing subdir. 
 */
public class GdbManager 
{
	static private IGdb currentGdb = null;
	
	static public IGdb getCurrentGdb ()
	{
		return currentGdb;
	}

	static void setCurrentGdb(IGdb value)
	{
		currentGdb = value;
	}
	
	/**
	 * Initiates this class. Checks the properties file for a previously
	 * used Gene Database and tries to open a connection if found.
	 */
	public static void init()
	{
		String currGdb = GlobalPreference.DB_GDB_CURRENT.getValue();
		if(!currGdb.equals("") && !GlobalPreference.isDefault(GlobalPreference.DB_GDB_CURRENT))
		{
			try {
				SimpleGdb.connect(currGdb);
			} 
			catch(Exception e) 
			{
				Logger.log.error("Setting previous Gdb failed.", e);
				try {
					SimpleGdb.connect(currGdb);
				} 
				catch(Exception f) 
				{
					Logger.log.error("Setting default Gdb failed.", f);
				}
			}
		}
	}

}
