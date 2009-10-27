// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

/**
 * Event produced by the GdbManager. This event is generated when a new Gdb is loaded
 */
public class GdbEvent  extends EventObject
{

	/**
	   Called from GdbManager, when a GDB is loaded, either
	   when it was selected from the data -> select gdb menu
	   or during data import.
	 */
	public enum Type { 
		ADDED,
		REMOVED
	};

	private Type type;
	
	/**
	 * @param source the source of this event, should be a GdbManager
	 * @param type Currently the only implemented type is GDB_CONNECTED
	 * @param name the name of the database that was connected (if type == GDB_CONNECTED)
	 */
	public GdbEvent(Object source, Type type, String name) 
	{
		super(source);
		this.type = type;
		this.dbName = name;
	}

	public Type getType() { return type; }
		
	/**
	 * The name of the database that was connected.
	 * May be null depending on the event type.
	 */
	public String getName()
	{
		return dbName;
	}
	
	private final String dbName;
}
