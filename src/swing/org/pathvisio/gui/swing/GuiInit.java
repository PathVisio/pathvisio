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
package org.pathvisio.gui.swing;

import java.io.File;
import java.io.PrintStream;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.MappFormat;
import org.pathvisio.preferences.GlobalPreference;

/**
 * Static utility class for user interface initialization. The {@link #init()} method
 * should be called before starting the GUI.
 * @author thomas
 */
public class GuiInit {
	/**
	 * Performs initiation needed by the user interface. This method register the importers/exporters
	 * and sets the log. This method is called from {@link GuiMain#createAndShowGUI(MainPanel)}
	 */
	public static void init() {
		initImporters();
		initExporters();
		try {
			GlobalPreference.FILE_LOG.setDefault(
					new File(Engine.getCurrent().getApplicationDir(), ".PathVisioLog").toString()
			);
			Logger.log.setStream(new PrintStream(GlobalPreference.FILE_LOG.getValue())); 
		} catch(Exception e) {
			System.err.println("Unable to set log stream to " + GlobalPreference.FILE_LOG.getValue());
			e.printStackTrace();
		}
		Logger.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level
		
	}
	
	private static void initImporters() {
		Engine.getCurrent().addPathwayImporter(new MappFormat());
		Engine.getCurrent().addPathwayImporter(new GpmlFormat());
	}
	
	private static void initExporters() 
	{
		Engine.getCurrent().addPathwayExporter(new MappFormat());
		Engine.getCurrent().addPathwayExporter(new GpmlFormat());
	}
}
