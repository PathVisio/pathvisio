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

/**
 * Interface for an exporter that writes a pathway to a file
 */
public interface PathwayExporter extends PathwayIO 
{
	/**
	 * Export the given pathway to the file
	 * @param file The file to export to
	 * @param pathway The pathway to export
	 * @throws ConverterException when there is a fatal conversion problem. Implementations should only throw in case there is a non-recoverable error. Ohterwise, it should emit a warning.
	 */
	public void doExport(File file, Pathway pathway) throws ConverterException;
}
