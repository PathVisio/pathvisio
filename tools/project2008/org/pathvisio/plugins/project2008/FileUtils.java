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
package org.pathvisio.plugins.project2008;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * class which contains a method to get filenames Listings.
 */
//TODO: remove. Redundant.
public class FileUtils {

	/** In this method, a list of filenames is constructed. The list contains all the files
	*   with a given extension in a folder and all its sub-folders (and sub-sub-folders, etc.
	*   The method is recursive). The properties you have to enter are "path"; 
	*   a File containing the path that has to be searched,
	*   and "extension"; a String containing the extension where has to be searched for.
	*   
	*   e.g. 
	*   File path = new File("C:\\Windows");
	*   getFileListing(path, ".txt");
	*   
	*   returns something like
	*   "C:\\Windows\\readme.txt","C:\\Windows\\hoi.txt", "C:\\Windows\\System\\readme.txt", etc.
	*/
	static public List<File> getFileListing(File path, String extension){
		// create a new List of Files
		List<File> files = new ArrayList<File>();
		// get all the files and directories contained in the given path
	    File[] content = path.listFiles();
	    // use a for loop to walk through content
	    if (content != null) for(File file : content) {
	    	// if the file is a directory use recursion to get the contents of the sub-path
	    	// the files in this sub-path are added to the files list.
	    	if ( file.isDirectory() ) {
	    		List<File> subpath = getFileListing(file, extension);
		        files.addAll(subpath);
		      }
		    // if the file is not a directory, check the extension. When this is the
	    	// desired extension, add the file to the list.
	    	else {
		    	  if( file.getName().endsWith(extension) ) {
		    	 files.add(file);
		    	 }
		    }
		}
	    
	    // return the list with files
	    return files;
	}
	
		
}