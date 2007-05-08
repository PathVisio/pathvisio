package org.pathvisio.model;

import java.io.File;

public interface PathwayImporter {
	public String getName();
	public String[] getExtensions();	
	public void doImport(File file, Pathway pathway) throws ConverterException;
}
