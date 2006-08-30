package util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

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
}
