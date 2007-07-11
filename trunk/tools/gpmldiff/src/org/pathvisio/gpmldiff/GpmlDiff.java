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
import org.pathvisio.debug.Logger;

/**
   Class containing main method
*/
class GpmlDiff
{
	static File oldFile = null;
	static File newFile = null;
	
	/**
	   Parse Command-line Options
	*/
	static boolean parseCliOptions(String argv[])
	{
		String error = null;
		if (argv.length != 2) error = "Two parameters expected";
		if (error == null)
		{
			oldFile = new File(argv[0]);
			if (!oldFile.exists()) error = argv[0] + ": File not found";
		}
		if (error == null)
		{
			newFile = new File(argv[1]);
			if (!newFile.exists()) error = argv[1] + ": File not found";
		}
		if (error != null)
		{
			Logger.log.error (error);
			System.out.println (error);
			printUsage();
		}
		return error == null;
	}
	
	/**
	   Prints helpful info on the command line
	*/
	static void printUsage()
	{
		System.out.print (
			"Gpmldiff\n" +
			"\n" +
			"Usage:\n" +
			"  Gpmldiff old.gpml new.gpml\n" +
			"\n" +
			"Finds the difference between the two files\n"
			);
		System.exit(1);
	}
	
	/**
	   Entry point.
	*/
    public static void main(String argv[])
	{
		try
		{
			Logger.log.setStream (new PrintStream("log.txt"));
		}
		catch (IOException e) {}
		if (parseCliOptions(argv))
		{
			PwyDoc oldDoc = PwyDoc.read (oldFile);
			PwyDoc newDoc = PwyDoc.read (newFile);
			SearchNode result = oldDoc.findCorrespondence (newDoc, new BasicSim(), new BasicCost());

//			DiffOutputter out = new BasicOutputter();
// 			DiffOutputter out = new DgpmlOutputter();
 			DiffOutputter out = new SvgOutputter(oldDoc, newDoc);
			
			oldDoc.writeResult (result, newDoc, out);
			try
			{
				out.flush();
			}
			catch (IOException e) { e.printStackTrace(); }
		}
	}
}