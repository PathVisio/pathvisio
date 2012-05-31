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

import java.util.List;

/**
 * Common methods for {@link PathwayImporter}s and {@link PathwayExporter}s
 */
public interface PathwayIO
{
	public String getName();

	/**
	 * Get the possible extensions this importer/exporter can read (e.g. txt).
	 * The extensions do not have to be unique. 
	 * In case two importers use the same extension, the correct one will be chosen
	 * based on the result of PathwayImporter.isCorrectFileType().
	 * If that doesn't help, the user may be asked to pick an importer.
	 * The first item in the array is assumed to be the preferred extension.
	 * @return An array with the possible extensions (without '.')
	 */
	public String[] getExtensions();

	/**
	 * After import or export, this can be used to check if there are any warnings
	 * @returns a list of warning messages, or an empty list if there are none.
	 */
	public List<String> getWarnings();

}
