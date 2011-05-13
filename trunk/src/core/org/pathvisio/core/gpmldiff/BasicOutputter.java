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
package org.pathvisio.core.gpmldiff;

import java.io.File;
import java.io.PrintStream;

import org.pathvisio.core.model.PathwayElement;

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

	public void insert(PathwayElement newElt)
	{
		output.println ("insert: " + PwyElt.summary(newElt));
	}

	public void delete(PathwayElement oldElt)
	{
		output.println ("delete: " + PwyElt.summary(oldElt));
	}

	PathwayElement curOldElt = null;
	PathwayElement curNewElt = null;

	public void modifyStart (PathwayElement oldElt, PathwayElement newElt)
	{
		curOldElt = oldElt;
		curNewElt = newElt;
	}

	public void modifyEnd ()
	{
		curOldElt = null;
		curNewElt = null;
	}

	public void modifyAttr(String attr, String oldVal, String newVal)
	{
		assert (curOldElt != null);
		assert (curNewElt != null);
		output.println ("modify: " + PwyElt.summary(curNewElt) + "[" + attr + ": '" + oldVal + "' -> '" + newVal + "']");
	}



}