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

import java.io.PrintStream;

import org.pathvisio.Engine;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.preferences.GlobalPreference;

public class GuiInit {
	public static void init() {
		initImporters();
		initExporters();
		try {
			Engine.log.setStream(new PrintStream(GlobalPreference.FILE_LOG.getValue())); 
		} catch(Exception e) {}
		Engine.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level
		
	}
	
	private static void initImporters() {
		Engine.addPathwayImporter(new GpmlFormat());
	}
	
	private static void initExporters() {
		Engine.addPathwayExporter(new GpmlFormat());
	}
}
