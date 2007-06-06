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