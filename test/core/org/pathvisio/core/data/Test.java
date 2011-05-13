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
package org.pathvisio.core.data;

import java.io.File;

import junit.framework.TestCase;

import org.pathvisio.core.data.GdbEvent;
import org.pathvisio.core.data.GdbManager.GdbEventListener;


public class Test extends TestCase implements GdbEventListener
{
	//TODO
	static final String GDB_HUMAN =
		System.getProperty ("user.home") + File.separator +
		"PathVisio-Data/gene databases/Hs_Derby_20080102.pgdb";
	static final String GDB_RAT =
		System.getProperty ("user.home") + File.separator +
		"PathVisio-Data/gene databases/Rn_Derby_20080102.pgdb";

	boolean eventReceived = false;

	public void gdbEvent (GdbEvent e)
	{
		if (e.getType() == GdbEvent.Type.ADDED)
		{
			eventReceived = true;
		}
	}

	//TODO: create test pgdb
	// suitable for mapping Hs_Apoptosis genes.

	public void testGdbManager()
	{
		// assertTrue (eventReceived);
		// test reception of event...
		//TODO
	}

}
