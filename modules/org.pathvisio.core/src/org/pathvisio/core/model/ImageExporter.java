// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.model;

import java.util.Collections;
import java.util.List;

/**
 * Base class for a couple of image exporters.
 * This class adds the ability to remember (a) valid extension(s) for a
 * certain image type, using that information it is possible to select the
 * right exporter depending on the chosen extension.
 *
 * Subclass this to implement specific image types.
 */
public abstract class ImageExporter implements PathwayExporter {
	public static final String TYPE_PNG = "png";
	public static final String TYPE_TIFF = "tiff";
	public static final String TYPE_PDF = "pdf";
	public static final String TYPE_SVG = "svg";

	private String type;
	private String[] extensions;

	public ImageExporter(String type) {
		this.type = type;
	}

	public String[] getExtensions() {
		if(extensions == null) {
			extensions = new String[] { getDefaultExtension() };
		}
		return extensions;
	}

	public String getType() { return type; }

	public String getDefaultExtension() {
		return type;
	}

	public String getName() {
		return type.toUpperCase();
	}


	public void noExporterException() throws ConverterException {
		throw new ConverterException("No exporter for this image format");
	}
	
	@Override
	public List<String> getWarnings()
	{
		return Collections.emptyList();
	}	
}
