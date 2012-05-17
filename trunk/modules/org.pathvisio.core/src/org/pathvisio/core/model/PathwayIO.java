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
