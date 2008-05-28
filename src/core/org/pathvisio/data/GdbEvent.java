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

import java.util.EventObject;

public class GdbEvent  extends EventObject
{
	/**
	   Called from GdbManager, when a GDB is loaded, either
	   when it was selected from the data -> select gdb menu
	   or during data import.
	 */
	public static final int GDB_CONNECTED = 7;

	private int type;
	
	public GdbEvent(Object source, int type) 
	{
		super(source);
		this.type = type;
	}

	public int getType() { return type; }
}
