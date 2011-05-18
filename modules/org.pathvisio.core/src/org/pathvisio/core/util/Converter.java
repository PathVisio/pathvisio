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
package org.pathvisio.core.util;

/*
 * Converter.java
 * Command Line GenMAPP to GPML Converter
 * Created on 15 augustus 2005, 20:28
 */

import java.io.File;

import org.pathvisio.core.Engine;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.BatikImageExporter;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.DataNodeListExporter;
import org.pathvisio.core.model.EUGeneExporter;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.ImageExporter;
import org.pathvisio.core.model.MappFormat;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayExporter;
import org.pathvisio.core.model.PathwayImporter;
import org.pathvisio.core.model.RasterImageExporter;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.MIMShapes;

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
        // Handle command line arguments
        // Check for custom output path
        Logger.log.setStream (System.err);
						//debug, trace, info, warn, error, fatal
        Logger.log.setLogLevel (false, false, true, true, true, true);

        PreferenceManager.init();
    	Engine engine = new Engine();
    	engine.addPathwayImporter(new GpmlFormat());
    	engine.addPathwayImporter(new MappFormat());
		engine.addPathwayExporter(new MappFormat());
    	engine.addPathwayExporter(new GpmlFormat());
		engine.addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_SVG));
		engine.addPathwayExporter(new RasterImageExporter(ImageExporter.TYPE_PNG));
		engine.addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_TIFF));
		engine.addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PDF));
		engine.addPathwayExporter(new EUGeneExporter());
		engine.addPathwayExporter(new DataNodeListExporter());

		// Transient dependency on Biopax converter
		try
		{
			Class<?> c = Class.forName("org.pathvisio.biopax3.BiopaxFormat");
			Object o  = c.newInstance();
			engine.addPathwayExporter((PathwayExporter)o);
			engine.addPathwayImporter((PathwayImporter)o);
		}
		catch (ClassNotFoundException ex)
		{
			Logger.log.warn("BioPAX converter not in classpath, BioPAX conversion not available today.");
		}
		catch (InstantiationException e)
		{
			Logger.log.error("BioPAX instantiation error", e);
		}
		catch (IllegalAccessException e)
		{
			Logger.log.warn("Access to BioPAX class is Illegal", e);
		}
		
		//Enable MiM support (for export to graphics formats)
		PreferenceManager.getCurrent().setBoolean(GlobalPreference.MIM_SUPPORT, true);
		MIMShapes.registerShapes();

        File inputFile = null;
        File outputFile = null;

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
				error = true;
			}
		}

		if (!error)
		{
			try {
				engine.importPathway(inputFile);
				Pathway pathway = engine.getActivePathway();
				engine.exportPathway(outputFile, pathway);
			} catch(ConverterException e) {
				e.printStackTrace();
				System.exit(-2);
			}
		} else {
			printUsage();
			System.exit(-1);
		}
		System.exit(0); //Everything OK, now force exit
    }
}
