package util;

/*
 * Converter.java
 * Command Line GenMAPP to GMML Converter
 * Created on 15 augustus 2005, 20:28
 */

import data.*;
import java.io.*;
import java.sql.SQLException;

import debug.Logger;

/**
 * @author Thomas Kelder (t.a.j.kelder@student.tue.nl)
 */
public class Converter {  
    
	public static void printUsage()
	{
		System.out.println ("GenMAPP <-> GMML Converter\n" +
				"Usage:\n" +
				"\tjava Converter <input filename> [<output filename>]\n" +
				"\n" +
				"Converts between GenMAPP mapp format and Gmml-visio GMML format.\n" +
				"The conversion direction is determined from the extension of the input file.\n" +
				"Valid extensions are:\n" +
				"\t.mapp for GenMAPP mapp format,\n" +
				"\t.xml or .gmml for Gmml-visio GMML format\n." +
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
		
		MappFormat.log = log;
		GmmlFormat.log = log;
		
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
			else if (inputString.endsWith(".gmml"))
			{
				fromMappToGmml = false;
			}
			else
			{
				log.error ("Wrong extension for input file: must be .mapp, .xml or .gmml");
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
				outputString = outputString + (fromMappToGmml ? ".xml" : ".mapp");
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
					(fromMappToGmml ? "mapp to gmml " : "gmml to mapp"));

			boolean valid = true;
			
			try
			{
				if (fromMappToGmml)
				{
					GmmlData gmmlData = new GmmlData();
					gmmlData.readFromMapp(inputFile);
					gmmlData.writeToXML(outputFile, true);					
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
			catch (SQLException e)
			{
				log.error(e.getMessage(), e);
				System.exit(-2);	
			}
			catch (ClassNotFoundException e)
			{
				log.error (e.getMessage(), e);
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
