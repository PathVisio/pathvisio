package org.pathvisio.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * base implementation of PathwayImporter and PathwayExporter warnings mechanism. 
 */
public abstract class AbstractPathwayFormat implements PathwayImporter, PathwayExporter
{
	private List<String> warnings = new ArrayList<String>();
	
	protected void clearWarnings()
	{
		warnings.clear();
	}
	
	/**
	 * Can be used by overriding classes to add to the list of warnings. Don't forget to call {@link clearWarnings} at the start of conversion.
	 */
	protected void emitWarning (String warning)
	{
		warnings.add (warning);
	}
	
	@Override
	public boolean isCorrectType(File f)
	{
		return true;
	}

	@Override
	public List<String> getWarnings()
	{
		return warnings;
	}

}
