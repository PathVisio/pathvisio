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

import org.pathvisio.Engine;
import org.pathvisio.data.GdbManager;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.DataNodeListExporter;
import org.pathvisio.model.EUGeneExporter;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.MappFormat;
import org.pathvisio.view.MIMShapes;

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
	public static void init() 
	{
		initImporters();
		initExporters();
		MIMShapes.registerShapes();
		GdbManager.init(); // read default gene databases
	}
	
	private static void initImporters() {
		Engine.getCurrent().addPathwayImporter(new MappFormat());
		Engine.getCurrent().addPathwayImporter(new GpmlFormat());
	}
	
	private static void initExporters() 
	{
		Engine.getCurrent().addPathwayExporter(new MappFormat());
		Engine.getCurrent().addPathwayExporter(new GpmlFormat());
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_SVG));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PNG));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_TIFF));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PDF));	
		Engine.getCurrent().addPathwayExporter(new DataNodeListExporter());
		Engine.getCurrent().addPathwayExporter(new EUGeneExporter());
	}
}
