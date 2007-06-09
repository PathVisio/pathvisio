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
package org.pathvisio.gpmldiff;

import java.io.*;

/**
   Naive implementation of Outputter.
 */
class BasicOutputter extends DiffOutputter
{

	PrintStream output = null;
	
	BasicOutputter(File f)
	{
		//TODO: open file
	}
	
	BasicOutputter()
	{
		output = System.out;
	}

	public void flush()
	{
	}

	public void insert(PwyElt newElt)
	{
		output.println ("insert: " + newElt.summary());
	}

	public void delete(PwyElt oldElt)
	{
		output.println ("delete: " + oldElt.summary());
	}

	public void modify(PwyElt newElt, String path, String oldVal, String newVal)
	{
		output.println ("modify: " + newElt.summary() + "[" + path + ": '" + oldVal + "' -> '" + newVal + "']");
	}

}