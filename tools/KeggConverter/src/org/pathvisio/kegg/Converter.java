//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 

//http://www.apache.org/licenses/LICENSE-2.0 

//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.

package org.pathvisio.kegg;

import java.io.File;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.Organism;
import org.pathvisio.model.Pathway;

public class Converter {
	public static void main(String[] args) {
		Engine.init();
		Logger.log.setStream(System.err);
		Logger.log.setLogLevel(true, true, true, true, true, true);
		if(args.length < 2) {
			printHelp();
			System.exit(-1);
		}
		Organism organism = Organism.fromLatinName(args[0]);
		if(organism == null) {
			//try by short name
			organism = Organism.fromShortName(args[0]);
		}
		if(organism == null) {
			//finally, try by code
			organism = Organism.fromCode(args[0]);
		}
		if(organism == null) {
			//give up and print help
			Logger.log.error("Couldn't find organism for: " + args[0]);
			printHelp();
			System.exit(-2);
		}
		
		File file = new File(args[1]);
		recursiveConversion(file, organism);
	}
	
	private static void printHelp() {
		Logger.log.error("Invalid arguments! This script requires the following arguments:\n " +
				"-> organism: The full species name, e.g. 'Homo sapiens'\n" +
				"-> kgml file or directory: either a kgml file or a directory which will be converted recursively"
		);
	}
	
	private static void recursiveConversion(File dir, Organism organism) {
		if(dir.isDirectory()) {
			for(File f : dir.listFiles()) {
				recursiveConversion(f, organism);
			}
		} else {
			doConversion(dir, organism);
		}
	}
	
	private static void doConversion(File file, Organism organism) {
		try {
			Pathway pathway = KeggFormat.readFromKegg(file, organism);
			GpmlFormat.writeToXml(pathway, new File(file.getAbsolutePath() + ".gpml"), true);	
			BatikImageExporter imageExporter = new BatikImageExporter(BatikImageExporter.TYPE_PNG);
			imageExporter.doExport(new File(file.getAbsolutePath() + ".png"), pathway);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

