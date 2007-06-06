package org.pathvisio.gpmldiff;

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
	abstract public void modify(PwyElt newElt, String path, String oldVal, String newVal);

	/**
	   Write all pending output to file or flush output buffers,
	   depending on the actual implementation of this
	*/
	abstract public void flush();

}