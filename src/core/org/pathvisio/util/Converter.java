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
package org.pathvisio.util;

/*
 * Converter.java
 * Command Line GenMAPP to GPML Converter
 * Created on 15 augustus 2005, 20:28
 */

import java.io.File;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.MappFormat;

/**
 * @author Thomas Kelder (t.a.j.kelder@student.tue.nl)
 */
public class Converter {  
    
	public static void printUsage()
	{
		System.out.println ("GPML Converter\n" +
				"Usage:\n" +
				"\tjava Converter <input filename> [<output filename>]\n" +
				"\n" +
				"Converts between GPML format and several other formats:\n" +
				"\t- GPML (.gpml/.xml) <-> GenMAPP (.mapp)\n" +
				"\t- GPML (.gpml/.xml) -> SVG (.svg)\n" +
				"\t- GPML (.gpml/.xml) -> PNG (.png)\n" +
				"\t- GPML (.gpml/.xml) -> TIFF (.tiff)\n" +
				"\t- GPML (.gpml/.xml) -> PDF (.pdf)\n" +
				"The conversion direction is determined from the extension of the input file.\n" +
				"Return codes:\n" +
				"\t 0: OK\n" +
				"\t-1: Parameter or file error\n" +
				"\t-2: Conversion error\n"
			);
	}
	
	/**
     * Command line arguments:
     *
     */ 
    public static void main(String[] args) 
    {
    	Engine engine = Engine.getCurrent();
    	engine.addPathwayImporter(new GpmlFormat());
    	engine.addPathwayImporter(new MappFormat());
		engine.addPathwayExporter(new MappFormat());
		engine.addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_SVG));
		engine.addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PNG));
		engine.addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_TIFF));
		engine.addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PDF));
    	
        File inputFile = null;
        File outputFile = null;
        
        // Handle command line arguments
        // Check for custom output path
        Logger.log.setStream (System.err);		
						//debug, trace, info, warn, error, fatal
        Logger.log.setLogLevel (false, false, true, true, true, true);
		
		boolean error = false;
		if (args.length == 0)
		{
			Logger.log.error ("Need at least one command line argument");
			error = true;			
		}
		else if (args.length > 2)
		{
			Logger.log.error ("Too many arguments");
			error = true;
		}
		else
		{
			inputFile = new File(args[0]);
			outputFile = new File(args[1]);
			if(inputFile == null || !inputFile.canRead()) {
				Logger.log.error("Unable to read inputfile: " + inputFile);
			}
			if(outputFile == null || !outputFile.canWrite()) {
				Logger.log.error("Unable to write outputfile: " + outputFile);
			}
		}		
		
		if (!error)
		{
			try {
				engine.importPathway(inputFile);
				engine.exportPathway(outputFile);
			} catch(ConverterException e) {
				e.printStackTrace();
				System.exit(-2);
			}
		} else {
			printUsage();
			System.exit(-1);
		}
    }
}
