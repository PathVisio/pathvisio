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
package org.pathvisio.model;

import java.io.File;

/**
 * Interface for an exporter that writes a pathway to a file
 * @author thomas
 *
 */
public interface PathwayExporter {
	/**
	 * Get the exporter name (to what file type does it export)
	 * @return
	 */
	public String getName();
	
	/**
	 * Get the possible extensions this exporter writes to (e.g. txt).
	 * The extension must be unique, the correct exporter will be chosen
	 * based on file extension. 
	 * @return An array with the possible extensions (without '.')
	 */
	public String[] getExtensions();
	
	/**
	 * Export the given pathway to the file
	 * @param file The file to export to
	 * @param pathway The pathway to export
	 * @throws ConverterException
	 */
	public void doExport(File file, Pathway pathway) throws ConverterException;
}
