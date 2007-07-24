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
import java.util.Arrays;

/**
   Class containing main method
*/
class GpmlDiff
{
	static File oldFile = null;
	static File newFile = null;
	static String outputType = "svg";
	static final String[] outputTypes = {"svg", "dgpml", "basic"};

	/**
	   Parse Command-line Options
	*/
	static boolean parseCliOptions(String argv[])
	{
		int pos = 0;
		String error = null;
		if (pos >= argv.length) error = "Expected -o or old pathway file";
		if (argv[pos].equals ("-o"))
		{
			pos++;
			if (pos >= argv.length) error = "Expected -o or old pathway file";
			if (error == null)
			{				
				outputType = argv[pos];
				if (!Arrays.asList(outputTypes).contains (outputType))
				{
					error = "Outputtype " + outputType + " is not allowed";
				}
				if (error == null)
				{
					pos++;
					if (pos >= argv.length) error = "expected old pathway file";
				}
			}
		}			
		if (error == null)
		{
			oldFile = new File(argv[pos]);
			if (!oldFile.exists()) error = argv[pos] + ": File not found";
		}
		if (error == null)
		{
			pos++;
			if (pos >= argv.length) error = "expected new pathway file";
		}
		if (error == null)
		{
			newFile = new File(argv[pos]);
			if (!newFile.exists()) error = argv[pos] + ": File not found";
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
			"GpmlDiff\n" +
			"\n" +
			"Usage:\n" +
			"  gpmldiff [-o svg|dgpml] old.gpml new.gpml\n" +
			"  -o: output format. Choose svg for the visual output or dgpml for the\n" +
            "      gpmldiff patch format\n" +
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

			DiffOutputter out = null;
			if (outputType.equals ("basic"))
			{
				out = new BasicOutputter();
			}
			else if (outputType.equals ("dgpml"))
			{
				out = new DgpmlOutputter();
			}
			else if (outputType.equals ("svg"))
			{				
				out = new SvgOutputter(oldDoc, newDoc);
			}
			else
			{
				System.out.println ("Unknown ouput-type " + outputType);
				System.exit (1);
			}
			assert (out != null);
			oldDoc.writeResult (result, newDoc, out);
			try
			{
				out.flush();
			}
			catch (IOException e) { e.printStackTrace(); }
		}
	}
}