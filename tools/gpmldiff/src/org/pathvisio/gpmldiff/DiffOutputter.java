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

import java.io.IOException;

/**
   abstract base class for Diff Ouputters. Designed to handle a couple
   of events emitted by the comparison algorithm, and give output
   based on that.  This way we can support various output formats
   simply by subclassing DiffOutputter
 */
abstract class DiffOutputter
{
	/**
	   Event to signify insertion of a new PathwayElement that was not
	   present in the old document
	*/
	abstract public void insert(PwyElt newElt);

	/**
	   Event to signify deletion of an element that was present in the
	   old document
	*/
	abstract public void delete(PwyElt oldElt);

	/**
	   Modification of an element that is present in both documents.
	   @param path is an XPath-like expression that points to the
	   subelement or attribute that was modified.
	   @param oldVal is the old value of that tag / attribute
	   @param newVal is the new value of that tag / attribute
	 */
	abstract public void modify(PwyElt oldElt, PwyElt newElt, String path, String oldVal, String newVal);

	/**
	   Write all pending output to file or flush output buffers,
	   depending on the actual implementation of this
	*/
	abstract public void flush() throws IOException;

}