// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.pathvisio.gmmlVision.GmmlVision;

public class FileUtils {	
	/**
	 * Get all files in a directory
	 * @param directory	The directory to get the files from
	 * @param extension	The extension of the files to get
	 * @param recursive	Whether to include subdirectories or not
	 * @return A list of files with given extension present in the given directory
	 */
	public static ArrayList<File> getFiles(File directory, final String extension, boolean recursive) {
		ArrayList<File> fileList = new ArrayList<File>();
		
		if(!directory.isDirectory()) { //File is not a directory, return file itself (if has correct extension)
			if(directory.getName().endsWith("." + extension)) fileList.add(directory);
			return fileList;
		}
		
		//Get all files in this directory
		File[] files = directory.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return (f.isDirectory() || f.getName().endsWith("." + extension)) ? true : false;
			}
		});

		if(recursive) {
			//Recursively add the files
			for(File f : files) {
				if(f.isDirectory()) fileList.addAll(getFiles(f, extension, true));
				else fileList.add(f);
			}
		}
		return fileList;
	}
	
	public static void deleteRecursive(File file) {
		if(file.isDirectory()) {
			for(File f : file.listFiles()) deleteRecursive(f);
		}
		boolean deleted = file.delete();
		GmmlVision.log.trace((deleted ? "Deleted " : "Unable to delete ") + "file " + file);
	}
	
	/**
	 * Determine the number of lines in the given file.
	 * @param fileName	The file to get the number of lines from
	 * @return	the number of lines, or -1 if unable to determine the number of lines
	 */
	public static int getNrLines(String fileName) {
		int nrLines = -1;
		try
		{
			RandomAccessFile randFile = new RandomAccessFile(fileName, "r");
			long lastRec=randFile.length();
			randFile.close();
			FileReader fileRead = new FileReader(fileName);
			LineNumberReader lineRead = new LineNumberReader(fileRead);
			lineRead.skip(lastRec);
			nrLines=lineRead.getLineNumber()-1;
			fileRead.close();
			lineRead.close();
		}
		catch(IOException e)
		{
			GmmlVision.log.error("Unable to determine number of lines in file " + fileName, e);
		}
		return nrLines;
	}
	
	/**
	 * Removes the file extension (everything after the last occurence of '.')
	 */
	public static String removeExtension(String fname) {
		int dot = fname.lastIndexOf('.');
		if(dot > 0) fname = fname.substring(0, dot);
		return fname;
	}
}
