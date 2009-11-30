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
package org.pathvisio.wikipathways;

import java.net.URL;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.MappFormat;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.RasterImageExporter;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.Utils;
import org.pathvisio.view.MIMShapes;

/**
 * Static utility class that contains a collection of global methods for {@link WikiPathways}.
 * @author thomas
 */
public class WikiPathwaysInit
{
	static void init(Engine engine, PreferenceManager preferences) throws Exception
	{
		String logDest = preferences.get(GlobalPreference.WP_FILE_LOG);
		Logger.log.setDest (logDest);
		Logger.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level

		MIMShapes.registerShapes();

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

		engine.addPathwayExporter(new GpmlFormat());
		engine.addPathwayImporter(new GpmlFormat());
		if(Utils.getOS() == Utils.OS_WINDOWS) {
			engine.addPathwayImporter(new MappFormat());
		}

		engine.setApplicationName(WikiPathways.APPLICATION_NAME);
	}

	public static void registerXmlRpcExporters(URL rpcUrl, Engine engine) {
		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_PDF));
		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_SVG));
		engine.addPathwayExporter(new RasterImageExporter(ImageExporter.TYPE_PNG));
		if(Utils.getOS() == Utils.OS_WINDOWS) {
			engine.addPathwayExporter(new MappFormat());
		}
//		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_TIFF)); disabled, see bug #166
	}
}
