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
// import the things needed to run this java file.
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;


public class LinkChecker {


	/**
	 * @param args
	 * @throws ConverterException 
	 * @throws DataException 
	 */
	public static void main(String[] args) throws ConverterException, DataException {
		// enter the directorys that contains the pathways and databases
		File dbDir = new File("C:\\databases\\");
		File pwDir = new File("C:\\pathways\\");
		
		// get a list of files of databases and pathways
		String pwExtension = ".gpml";
		String dbExtension = ".pgdb";
		List<File> pwFilenames = getFileListing(pwDir, pwExtension);
		List<File> dbFilenames = getFileListing(dbDir, dbExtension);
		
		// Load all databases in List<SimpleGdb> databases,
		// and load all filenames of the loaded databases
		// in List<String> databaseFilenames
		List<SimpleGdb> databases = new ArrayList<SimpleGdb>();
		List<String> databasesFilenames = new ArrayList<String>();
		int i = 0;
		for (File dbFilename: dbFilenames){
			// load a database and add it to the list
			SimpleGdb database = new SimpleGdb(dbFilename.getPath(), new DataDerby(), 0);
			databases.add(i, database);
			
			// extract a filename and add it to the list
			databasesFilenames.add(i, dbFilename.getName());
			
			i++;
			}
		
		
		// for each filename create a list containing the Xref's
		for (File filename:pwFilenames)
		{
			// load the pathway
			Pathway pway = new Pathway();
			boolean validate = false;
			pway.readFromXml(filename, validate);
		
			// make a list containing the Xref's 
			List<Xref> xrefList = makeXrefList(pway);
			
			// as a debug tool, show how much Xref's are found in the list
			System.out.println(filename.getName()); 
			System.out.println("size of the XrefList: "+xrefList.size());
			
					
			// find the database for the pathway
			i = 0;
			int index = 0;
			for (String databaseFilename: databasesFilenames){
				if (databaseFilename.substring(0,2).equalsIgnoreCase(filename.getName().substring(0,2))){
					index = i;
					}
				i++;
				
				}
			System.out.println(databasesFilenames.get(index));
			
			String percentage = calculatePercentage(xrefList, databases.get(index));
			System.out.println("percentage found in DB: "+percentage);
		}
	}
	
	public static String calculatePercentage(List<Xref> xrefList, SimpleGdb database){
		// count how much of the Xref's exist in the database
		
		// initialize two counters. One for false outcomes, and one for true outcomes
		int countTrue = 0;
		int countFalse = 0;
		for (Xref reference:xrefList)
		{
			if (database.xrefExists(reference) == true)
			{
			countTrue++;
				}
			else
			{
			countFalse++;
				}
			}
		
		// calculated the total count. This has to be equal to xrefList.size. It's still here for debug purpose
		int countTotal = countTrue + countFalse;
		
		// calculate the precentage of found references
		double percentagedouble = 100*countTrue/countTotal;
		
		// create a string with the outcome
		String percentage = countTrue+" of "+countTotal+" found in DB; ("+percentagedouble+"%)";
		return percentage;
		
	}
	

	static public List<File> getFileListing(File path, String extension){
		// make a new list of files
		List<File> files = new ArrayList<File>();
		
		// get all the files and directories contained in the given path
	    File[] content = path.listFiles();
	    
	    // use a for loop to walk through content
	    for(File file : content) {
	    	  if ( file.isDirectory() ) {
	    		// if the file is a directory use recursion to get the contents of the sub-path
	    		List<File> subpath = getFileListing(file, extension);
	    		// add the files contained in this sub-directory to the files list
		        files.addAll(subpath);
		      }
		      else {
		    	  // only use the file if it has a valid extension
		    	  if( file.getName().endsWith(extension) ) {
		    	 // add all files in the directory to the list files
		    	 files.add(file);
		    	 }
		    }
		}
	    // return all the obtained files
	    return files;
	}


	
	public static List<Xref> makeXrefList(Pathway pway){
		// for every pathway element, check if it is a datanode.
		// if this is the case, put the xRef data in a list.
		List<PathwayElement> pelts = pway.getDataObjects();
		List<Xref> xRefList = new ArrayList();
		for (PathwayElement element:pelts){
			int objectType = element.getObjectType();
			// check if the objectType is a datanode
			if (objectType == ObjectType.DATANODE)
			{
				// retrieve the reference info
				Xref reference;
				reference = element.getXref();
				
				// add the reference info to a list
				xRefList.add(reference);
				
				// uncomment to get the name of the pathway element
				// String name;
				// name = element.getTextLabel();
				// System.out.println("GenID info: name: "+name);
				
				// uncomment to get the reference info (referenceId and databasename) 
				// of the pathway element				
				//String refId = reference.getName();
				//String databasename = reference.getDatabaseName();
				//System.out.println("Xref info: referenceID: "+refId+"  databasename: "+databasename);
				//System.out.println(" ");
				
				}
			}
		return xRefList;
		
	}
	
	}
