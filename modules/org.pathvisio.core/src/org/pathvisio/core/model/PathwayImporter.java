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

import java.io.File;

/** implemented by classes that can import a pathway from various different types */
public interface PathwayImporter extends PathwayIO 
{
	/**
	 * Inspect the file and determine if the file is suitable for import using this importer.
	 * For example, files ending in .xml could be examined for the local name and namespace of the root element.
	 * This function is invoked when multiple importers apply to a given file.
	 * <p>
	 * Implementations should check the file only superficially if at all. This function is merely a "tie-breaker" in case there are conflicting importers.  
	 * A return value of true doesn't automatically mean that the file is guaranteed to be valid, so no complex validation is required. 
	 * For naive implementations, it's always OK to simply return "true".
	 * @param f: the file to check
	 * @returns true if the file appears superficially to be of the correct file type. 
	 */
	public boolean isCorrectType (File f);
	
	/**
	 * @param File that contains pathway information
	 * @returns the result of the import, a fresh Pathway instance
	 * @throws ConverterException if the input file could not be read or parsed,
	 * 		or doesn't contain correct pathway information.
	 */
	public Pathway doImport(File file) throws ConverterException;
}
