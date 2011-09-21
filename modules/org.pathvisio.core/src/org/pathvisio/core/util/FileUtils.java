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
package org.pathvisio.core.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.core.debug.Logger;

/**
 * Collection of static utility methods dealing with files.
 */
public class FileUtils {
	/**
	 * Get all files in a directory
	 * @param directory	The directory to get the files from
	 * @param recursive	Whether to include subdirectories or not
	 * @return A list of files in the given directory
	 */
	public static List<File> getFiles(File directory, boolean recursive) {
		List<File> fileList = new ArrayList<File>();

		if(!directory.isDirectory()) { //File is not a directory, return file itself (if has correct extension)
			fileList.add(directory);
			return fileList;
		}

		//Get all files in this directory
		File[] files = directory.listFiles();

		//Recursively add the files
		for(File f : files)
		{
			if(f.isDirectory())
			{
				if (recursive) fileList.addAll(getFiles(f, true));
			}
			else fileList.add(f);
		}

		return fileList;
	}

	/**
	 * Get all files in a directory
	 * @param directory	The directory to get the files from
	 * @param extension	The extension of the files to get, without the dot so e.g. "gpml"
	 * @param recursive	Whether to include subdirectories or not
	 * @return A list of files with given extension present in the given directory
	 */
	public static List<File> getFiles(File directory, final String extension, boolean recursive) {
		List<File> fileList = new ArrayList<File>();

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

		//Recursively add the files
		for(File f : files)
		{
			if(f.isDirectory())
			{
				if (recursive) fileList.addAll(getFiles(f, extension, true));
			}
			else fileList.add(f);
		}

		return fileList;
	}

	/**
	 * Think "deltree" or "rm -r"
	 */
	public static void deleteRecursive(File file) {
		if(file.isDirectory()) {
			for(File f : file.listFiles()) deleteRecursive(f);
		}
		boolean deleted = file.delete();
		Logger.log.trace((deleted ? "Deleted " : "Unable to delete ") + "file " + file);
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
			Logger.log.error("Unable to determine number of lines in file " + fileName, e);
		}
		return nrLines;
	}

	/**
	 * Removes the file extension (everything from the last occurence of '.')
	 */
	public static String removeExtension(String fname) {
		int dot = fname.lastIndexOf('.');
		if(dot > 0) fname = fname.substring(0, dot);
		return fname;
	}

	/**
	 * Replaces file extension with something else. If there was no extension previously, the new one will simply be added.
	 * @param fname file name to use.
	 * @param extension New extension to use. Should not include dot.
	 */
	public static File replaceExtension(File fname, String extension)
	{
		return new File (removeExtension(fname.toString()) + "." + extension);
	}
	
	/**
	 * Downloads a remote file given by an URL to the given local file
	 * @param url The URL that specifies the location of the file to download
	 * @param toFile The local file to which the remote file will be downloaded
	 * @throws IOException
	 */
	public static void downloadFile(URL url, File toFile) throws IOException {
		OutputStream out = null;
		URLConnection conn = null;
		InputStream  in = null;
		out = new BufferedOutputStream(
				new FileOutputStream(toFile));
		conn = url.openConnection();
		conn.setUseCaches(false);
		in = conn.getInputStream();
		byte[] buffer = new byte[1024];
		int numRead;
		long numWritten = 0;
		while ((numRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, numRead);
			numWritten += numRead;
		}

		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.close();
		}
	}

	/**
	 * Maps a file from one point in the directory tree to another point.
	 * For example, with this function you can map from
	 *
	 * /home/username/input/pathways/human/metabolomic/pathway.gpml
	 * to
	 * /tmp/output/pathways/human/metabolomic/pathway.gpml
	 *
	 */
	public static File mapFileTree (File f, File srcDir, File destDir)
	{
		List<String> components = new ArrayList<String>();
		File dest = destDir;
		File src = f;

		// unwind source
		while (!src.equals(srcDir) && src != null)
		{
			components.add(f.getName());
			src = src.getParentFile();
		}

		// rewind dest
		for (String s : components)
		{
			dest = new File (destDir, s);
		}
		return dest;
	}
}
