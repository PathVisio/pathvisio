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
package util;

/*
 * Converter.java
 * Command Line GenMAPP to GPML Converter
 * Created on 15 augustus 2005, 20:28
 */

import java.io.File;

import data.gpml.ConverterException;
import data.gpml.GmmlData;
import debug.Logger;

/**
 * @author Thomas Kelder (t.a.j.kelder@student.tue.nl)
 */
public class Converter {  
    
	public static void printUsage()
	{
		System.out.println ("GenMAPP <-> GPML Converter\n" +
				"Usage:\n" +
				"\tjava Converter <input filename> [<output filename>]\n" +
				"\n" +
				"Converts between GenMAPP mapp format and PathVisio GPML format.\n" +
				"The conversion direction is determined from the extension of the input file.\n" +
				"Valid extensions are:\n" +
				"\t.mapp for GenMAPP mapp format,\n" +
				"\t.xml or .gpml for PathVisio GPML format\n." +
				"\n" +
				"Return codes:\n" +
				"\t 0: OK\n" +
				"\t-1: Parameter or file error\n" +
				"\t-2: Conversion error\n" +
				"\t-3: Validation error\n");
	}
	
	
	/**
     * Command line arguments:
     *
     */ 
    public static void main(String[] args) 
    {
    	String outputString = "";
        String inputString = "";
        File inputFile = null;
        File outputFile = null;
        boolean fromMappToGmml = true;
        
        // Handle command line arguments
        // Check for custom output path
        Logger log = new Logger();
		log.setStream (System.err);		
						//debug, trace, info, warn, error, fatal
		log.setLogLevel (false, false, true, true, true, true);
		
		boolean error = false;
		if (args.length == 0)
		{
			log.error ("Need at least one command line argument");
			error = true;			
		}
		else if (args.length > 2)
		{
			log.error ("Too many arguments");
			error = true;
		}
		else
		{
			inputFile = new File(args[0]);
			inputString = args[0];
		}		
		
		if (!error)
		{
			if(inputString.endsWith(".mapp")) 
			{
				fromMappToGmml = true;
			}
			else if (inputString.endsWith(".xml"))
			{
				fromMappToGmml = false;
			}
			else if (inputString.endsWith(".gpml"))
			{
				fromMappToGmml = false;
			}
			else
			{
				log.error ("Wrong extension for input file: must be .mapp, .xml or .gpml");
				error = true;
			}
		}

		if (!error)
		{
			if (args.length == 2)
			{
				outputString = args[1];
			}
			else
			{
				outputString = inputString;
				int pos = outputString.lastIndexOf('.');
				if (pos >= 0)
					outputString = outputString.substring(0, pos);
				outputString = outputString + (fromMappToGmml ? ".gpml" : ".mapp");
			}
			outputFile = new File (outputString);
		}
		

		if (!error)
		{
			if (inputFile.exists() && inputFile.canRead())
				;			
			else
			{
				log.error("Can't read from file " + args[0]);
				error = true;
			}			
		}
		
		if (!error)
		{
			log.info("Source: " + inputString);
			log.info("Dest:   " + outputString);
			log.info("Going from " + 
					(fromMappToGmml ? "mapp to gpml " : "gpml to mapp"));

			boolean valid = true;
			
			try
			{
				if (fromMappToGmml)
				{
					GmmlData gmmlData = new GmmlData();
					gmmlData.readFromMapp(inputFile);
					gmmlData.writeToXml(outputFile, true);					
				}
				else
				{
					GmmlData gmmlData = new GmmlData();
					gmmlData.readFromXml(inputFile, true);					
					gmmlData.writeToMapp(outputFile);
				}
			}
			catch (ConverterException e)
			{
				log.error(e.getMessage(), e);
				System.exit(-2);			
			}
			System.exit(valid ? 0 : -3);
		}
		else
		{
			printUsage();
			System.exit(-1);
		}        
                
    }
}
