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
import java.io.IOException;
import java.util.Arrays;

import org.pathvisio.core.debug.Logger;

/**
   Class containing main method
*/
public class GpmlDiff
{
	static File oldFile = null;
	static File newFile = null;
	static String outputType = "dgpml";
	static final String[] OUTPUT_TYPES = {"dgpml", "basic", "table"};
	static String simFunType = "better";
	static final String[] SIMFUN_TYPES = {"basic", "better"};

	/**
	   Parse Command-line Options
	*/
	static boolean parseCliOptions(String argv[])
	{
		int pos = 0;
		String error = null;
		if (pos >= argv.length) error = "Expected option or old pathway file";
		while (error == null && (argv[pos].equals ("-o") || argv[pos].equals ("-s")))
		{
			if (argv[pos].equals ("-o"))
			{
				pos++;
				if (pos >= argv.length) error = "Expected output type after -o";
				if (error == null)
				{
					outputType = argv[pos];
					if (!Arrays.asList(OUTPUT_TYPES).contains (outputType))
					{
						error = "Outputtype " + outputType + " is not allowed";
					}
					if (error == null)
					{
						pos++;
						if (pos >= argv.length) error = "Expected option or old pathway file";
					}
				}
			}
			else if (argv[pos].equals ("-s"))
			{
				pos++;
				if (pos >= argv.length) error = "Expected simfun type after -s";
				if (error == null)
				{
					simFunType = argv[pos];
					if (!Arrays.asList(SIMFUN_TYPES).contains (simFunType))
					{
						error = "SimFunType " + simFunType + " is not allowed";
					}
					if (error == null)
					{
						pos++;
						if (pos >= argv.length) error = "Expected option or old pathway file";
					}
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
			"Finds the difference between two Pathways in gpml format\n" +
			"\n" +
			"Usage:\n" +
			"  gpmldiff [-o dgpml|basic|table] old.gpml new.gpml\n" +
			"  -o: output format. Default is dgpml\n"
			);
		System.exit(1);
	}

	/**
	 * Simple straightforward utility function to create a patch of two files.
	 */
	// TODO: move to better location
	public static void makePatch (File oldPwy, File newPwy, File patch) throws IOException
	{
		PwyDoc oldDoc = PwyDoc.read(oldPwy);
		PwyDoc newDoc = PwyDoc.read(newPwy);
		DiffOutputter out = new DgpmlOutputter(patch);
		SimilarityFunction simFun = new BetterSim();
		SearchNode result = oldDoc.findCorrespondence (newDoc, simFun, new BasicCost());
		oldDoc.writeResult (result, newDoc, out);
		out.flush();
	}

	/**
	   Entry point.
	*/
    public static void main(String argv[])
	{
//		try
//		{
//			Logger.log.setStream (new PrintStream("log.txt"));
//		}
//		catch (IOException e) {}
		Logger.log.setStream (System.err);
		Logger.log.setLogLevel (true, true, true, true, true, true);
		if (parseCliOptions(argv))
		{
			PwyDoc oldDoc = PwyDoc.read (oldFile);
			PwyDoc newDoc = PwyDoc.read (newFile);

			SimilarityFunction simFun;
			if (simFunType.equals ("basic"))
			{
				simFun = new BasicSim();
			}
			else
			{
				simFun = new BetterSim();
			}

			if (outputType.equals ("table"))
			{
				PwyDoc.printSimTable (oldDoc, newDoc, simFun);
			}
			else
			{
				SearchNode result = oldDoc.findCorrespondence (newDoc, simFun, new BasicCost());

				DiffOutputter out = null;
				if (outputType.equals ("basic"))
				{
					out = new BasicOutputter();
				}
				else if (outputType.equals ("dgpml"))
				{
					out = new DgpmlOutputter();
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
}