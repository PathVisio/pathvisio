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
package org.pathvisio.biopax;

import java.io.File;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;

/**
 * A main class for the BioPAX converter.
 * Will call the converter for the BioPAX file
 * provided in the first command line argument and
 * convert the pathway entities to GPML pathways.
 * The resulting pathways will be saved as a GPML file in 
 * the working directory.
 * @author thomas
 */
public class Converter {
	public static void main(String[] args) {
		Engine.init();
		Logger.log.setLogLevel(true, true, true, true, true, true);
		try {
			File inFile = new File(args[0]);
			BiopaxFormat bpf = new BiopaxFormat(inFile);
			int i = 0;
			for(Pathway p : bpf.convert()) {
				File f = p.getSourceFile();
				if(f == null) {
					f = new File(inFile.getName() + "-" + ++i + ".gpml");
				}
				p.writeToXml(f, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
