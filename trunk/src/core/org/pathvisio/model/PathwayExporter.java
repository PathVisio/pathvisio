package org.pathvisio.model;

import java.io.File;

public interface PathwayExporter {
	public String getName();
	public String[] getExtensions();
	public void doExport(File file, Pathway pathway) throws ConverterException;
}
