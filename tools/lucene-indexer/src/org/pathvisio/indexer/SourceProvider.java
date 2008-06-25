package org.pathvisio.indexer;

import java.io.File;

/**
 * Provides the source attribute for a GPML file
 * @author thomas
 */
public interface SourceProvider {
	/**
	 * Returns a value for the 'source' attribute
	 * for the given gpml file.
	 */
	public String getSource(File gpmlFile);
}
