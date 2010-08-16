package org.pathvisio.gui.parameter;

import java.io.File;

import javax.swing.JFileChooser;

public class FileParameter
{
	private boolean isSave;
	private final String simpleFilter;
	private int fileType;
	private final String fileTypeName;

	public FileParameter ()
	{
		this ("All files", "*.*", false, JFileChooser.FILES_AND_DIRECTORIES);
	}
	
	/**
	 * @param fileType one of JFileChooser.FILE_AND_DIRECTORIES, ... 
	 */
	public FileParameter (String fileTypeName, String simpleFilter, boolean isSave, int fileType)
	{
		this.isSave = isSave;
		this.simpleFilter = simpleFilter;
		this.fileType = fileType;
		this.fileTypeName = fileTypeName;
	}
	
	public String getFileTypeName()
	{
		return fileTypeName;
	}
	
	public String getFilter()
	{
		return simpleFilter;
	}
	
	public int getFileType()
	{
		return fileType;
	}
	
	public boolean isSave()
	{
		return isSave;
	}	
}
