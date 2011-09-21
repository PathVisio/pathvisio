// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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

import java.io.IOException;

import org.pathvisio.core.model.PathwayElement;

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
	abstract public void insert(PathwayElement newElt);

	/**
	   Event to signify deletion of an element that was present in the
	   old document
	*/
	abstract public void delete(PathwayElement oldElt);

	/**
	   A single modified attribute of an element that is present in
	   both documents.  This is always called after a modifyStart and
	   before a modifyEnd event.
	   @param attr is the name of the attribute that was modified
	   @param oldVal is the old value of that attribute
	   @param newVal is the new value of that attribute
	 */
	abstract public void modifyAttr(String attr, String oldVal, String newVal);

	/**
	   Start of a list of modifications to an element that is present
	   in both documents. Will be followed by one or more modifyItem()
	   events, and finally by a modifyEnd() event.

	   modifyStart is guaranteed to be called only if one or more
	   attributes have changed.
	 */
	abstract public void modifyStart(PathwayElement oldElt, PathwayElement newElt);

	/**
	   Called to indicate the end of a list of modified attributes.
	 */
	abstract public void modifyEnd();

	/**
	   Write all pending output to file or flush output buffers,
	   depending on the actual implementation of this
	*/
	abstract public void flush() throws IOException;

}