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
package org.pathvisio.wikipathways;

import java.io.File;
import java.net.URL;

import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.PropertyType;
import org.pathvisio.preferences.GlobalPreference;

/**
 * Static utility class that contains a collection of global methods for {@link WikiPathways}.
 * @author thomas
 */
public class WikiPathwaysInit {
	static void init() throws Exception {
		GlobalPreference.FILE_LOG.setDefault(new File(getApplicationDir(), ".wikipathwaysLog").toString());
		
		PropertyType.CENTERX.setHidden(true);
		PropertyType.CENTERY.setHidden(true);
		PropertyType.ENDX.setHidden(true);
		PropertyType.ENDY.setHidden(true);
		PropertyType.HEIGHT.setHidden(true);
		PropertyType.LAST_MODIFIED.setHidden(true);
		PropertyType.ORGANISM.setHidden(true);
		PropertyType.ROTATION.setHidden(true);
		PropertyType.STARTX.setHidden(true);
		PropertyType.STARTY.setHidden(true);
		PropertyType.WIDTH.setHidden(true);				
	}
		
	private static File DIR_APPLICATION;
	
	/**
	 * Get the working directory of this application
	 */
	public static File getApplicationDir() {
		if(DIR_APPLICATION == null) {
			DIR_APPLICATION = new File(System.getProperty("user.home"), "." + Globals.APPLICATION_NAME);
			if(!DIR_APPLICATION.exists()) DIR_APPLICATION.mkdir();
		}
		return DIR_APPLICATION;
	}
	
	public static void registerXmlRpcExporters(URL rpcUrl, Engine engine) {
		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_PDF));
		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_PNG));
		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_SVG));
//		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_TIFF)); disabled, see bug #166
		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_PDF));
	}
}
