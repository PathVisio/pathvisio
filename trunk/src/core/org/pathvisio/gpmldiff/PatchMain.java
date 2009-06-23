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
package org.pathvisio.gpmldiff;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jdom.JDOMException;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;

/**
   Dgpml patch utility, main function
 */
public class PatchMain
{
	static void printUsage()
	{
		System.out.println (
			"Patch\n\n" +
			"Applies a set of differences to a pathway\n\n" +
			"Usage:\n" +
			"  Patch old.gpml < patch.dgpml"
			);
		System.exit (1);
	}

	static File oldFile;
	static int fuzz;
	static boolean reverse;
	
	static boolean parseCliOptions (String argv[])
	{
		String error = null;
		if (argv.length != 1) error = "One parameter expected";
		if (error == null)
		{
			oldFile = new File (argv[0]);
			if (!oldFile.exists())
			{
				error = argv[0] + ": File not found";
			}
		}
		if (error != null)
		{
			Logger.log.error (error);
			System.out.println (error);
			printUsage();
		}
		return error == null;
	}
	
	public static void main(String argv[])
	{
		Logger.log.setStream (System.err);
		if (parseCliOptions(argv))
		{
			PwyDoc pwy = PwyDoc.read (oldFile);
			assert (pwy != null);

			Patch patch = new Patch();
			try
			{
				patch.readFromReader (new InputStreamReader (System.in)); // read diff from STDIN
				if (reverse)
				{
					patch.reverse();
				}
				patch.applyTo (pwy, fuzz);
				pwy.write(pwy.getSourceFile());
			}
			catch (Exception e)
			{
				Logger.log.error ("Exception occured while processing patch", e);
			}
		}
	}
}