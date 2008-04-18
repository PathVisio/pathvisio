package org.pathvisio.plugins.project2008;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;


/**
 * In the link checker for each pathway is is checked if the links in that pathway exist in the 
 * database. An output file is created that shows a table. On the right column of that table the 
 * filenames are shown and on the left column of the table the percentage is shown of the links
 * that exist in the database.
 */

public class LinkChecker {
	/**
	* in the String[] args, 3 arguments are given:
	* in example:
	* "C:\\databases\\"
	* "C:\pathways"
	* "C:\\result.html"
	* 
	* The first one is the directory that contains the databases.
	* The second one is the directory that contains the pathway cache.
	* The third one is the filename (note the html extension) of where the results are stored.
	*/
	public static void main(String[] args) throws ConverterException, DataException, XmlRpcException, IOException {
		/**
		 * If connected to the internet, the boolean online has to be set to true. 
		 */
		boolean online = true; // set to true if connected to the internet
		
		/** 
		 * Check if the String[] args is given, and make Files containing the directories to
		 * the pathways and databases 
		 */ 
		File dbDir = null;
		File pwDir = null;
		String outfile = null;
		
		try {
			dbDir = new File(args[0]);
			pwDir = new File(args[1] + "\\");
			outfile=args[2];
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("String[] args not given!");
			System.exit(0);
		}

		/**
		 * Get a list of files of databases and pathways. Here the method 'getFileListing' is 
		 * executed.
		 */
		String pwExtension = ".gpml";
		String dbExtension = ".pgdb";
		List<File> pwFilenames = FileUtils.getFileListing(pwDir, pwExtension);
		List<File> dbFilenames = FileUtils.getFileListing(dbDir, dbExtension);
		
		
		if (online){
			/**
			 * If the boolean online is true, first the data is loaded from the last changed 
			 * pathway. With the date of this last change, the data of the other recently 
			 * changed pathways can also be loaded.
			 */		
			long localdate = dateLastModified(pwFilenames);
			Date d = new Date(localdate); 
			DateFormat df = DateFormat.getDateTimeInstance();
			System.out.println("Date last modified: "+df.format(d)); 
			System.out.println("---[Get Recently Changed Files]---");
			WPDownloadAll.downloadNew(args[1], d);
			
			System.out.println("---[Get All Other Files]---");
			// download all pathways to the pathway folder
			WPDownloadAll.downloadAll(args[1]);
			System.out.println("---[Ready]---");
			System.out.println("---[Start Checking Links]---");
		}
		
		/**
		 * Get a new list of files of pathways.
		 */ 
		dbFilenames = FileUtils.getFileListing(dbDir, dbExtension);
				
		/**
		 * In the following for-loop, all databases are loaded in in List<SimpleGdb> databases 
		 * and all filenames of the loaded databases are loaded in List<String> 
		 * databaseFilenames.
		 */
		List<SimpleGdb> databases          = new ArrayList<SimpleGdb>();
		List<String>    databasesFilenames = new ArrayList<String>();
		for (File dbFilename: dbFilenames){
			// load a database and add it to the list
			SimpleGdb database = new SimpleGdb(dbFilename.getPath(), new DataDerby(), 0);
			databases.add(database);
			// extract a filename and add it to the list
			databasesFilenames.add(dbFilename.getName());
		}
				
		/**
		 * With the try/catch the output file is created.
		 * Then in the for-loop all pathway files are loaded. And the percentage of found Xrefs
		 * in de database is given. 
		 */
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(outfile));
		}
		catch(IOException e){
			System.out.println("Can't open folder "+outfile);
			System.exit(0);
		}
		String titleOfHTMLPage = "LinkChecker.java results";
		out.print("<HTML><HEAD><TITLE>"+titleOfHTMLPage+"</TITLE></HEAD><BODY><center><h1>"+titleOfHTMLPage+"</h1><TABLE border=\"1\"><TR><TD><B>Filename</B></TD><TD><B>Percentage found in Gdb</B></TD></B></TR>");
		
		for (File filename:pwFilenames){
			Pathway pway = new Pathway();
				
			 /** 
			  * The pathway file can be validated. For this end, the boolean validate has to be set 
			  * to true.
			  */ 
			boolean validate = false; //Set to true if you want to validate the pathway file.
			try{
				pway.readFromXml(filename, validate);
			}
			catch(ConverterException e){
				System.out.println("empty file is found");
			}
			
			/**
			 * The right database for the pathway must be found. The filename of a database 
			 * must have te same two starting letters as the filenames of the pathway.
			 */
			int i = 0;
			int index = -1;
			for (String databaseFilename: databasesFilenames){
				if (databaseFilename.substring(0,2).equalsIgnoreCase(filename.getName().substring(0,2))){
					index = i;
				}
				i++;
			}		

			/**
			 * First a list is made that contains the Xref's.
			 * In the if statement:
			 * If the database is found, add a row to the table of the html file, containing the
			 * name of the pathway and the percentage of found Xref's in the databse.
			 * In the else statement:
			 * Is the database is not found, add a row to the table of the html file, containing
			 * the name of the pathway and the text "Database not found".
			 */
			List<Xref> xrefList = makeXrefList(pway);
			if (index != -1){
				out.print("<TR><TD>"+filename.getName()+"</TD>");
				String percentage = calculatePercentage(xrefList, databases.get(index));
				out.println("<TD>"+percentage+databasesFilenames.get(index)+")</TD></TR>");
			}
			else{
			out.print("<TR><TD>"+filename.getName()+"</TD>");
			out.println("<TD> Database not found </TD></TR>");				
			}
		
		}
		
		/**
		 * All pathway rows are added to the table. Now the HTML file has to be closed properly.
		 */
		out.print("</TABLE></center></BODY></HTML>");
		out.close();
		System.out.println("Results are stored in " + outfile);
	}
	
	/** 
	 * In this method, the percentage of Xref's found in the database is calculated.
	 * The properties you have to enter are:
	 * 'xrefList' (a list of all the xrefs from a pathway) and 
	 * 'database' (a SimpleGdb database that has to be checked if it contains the Xrefs).
	 */
	public static String calculatePercentage(List<Xref> xrefList, SimpleGdb database){
		
		int countTrue = 0;       // counter for the true outcome (a xref is found)
        int countTotal = 0;      // counter for the total of xrefs
		String percentage;       // string for the outcome
		int percentageint; // int for the actual percentage
		// Check each Xref from the xrefList if it is found in the database.
		for (Xref xref:xrefList){
			if (database.xrefExists(xref) == true){
				countTrue++;
			}
			countTotal++;
		}		
		// Calculate the percentage of found references.
		if (countTotal != 0){
			percentageint = Math.round(100*countTrue/countTotal);
			percentage = (percentageint+"% (of total: "+countTotal+" in ");
		}
		else{
			percentage = ("<font color=\"red\"><b>total: 0</b></font> (divide by zero) in ");
		}
		// Return the percentage.
		return percentage;
	}
	
	/** 
	 * In this method a list of Xrefs in made. The list contains all the Xrefs from a given 
	 * pathway. The property you have to give is:
	 * 'pway' (a Pathway where you want to extract all the Xrefs from).
	 */ 
	public static List<Xref> makeXrefList(Pathway pway){
		List<PathwayElement> pelts = pway.getDataObjects();
		List<Xref> xRefList = new ArrayList<Xref>();
		for (PathwayElement element:pelts){
			// Check if the objectType is a datanode, and add it to the list.
			if (element.getObjectType() == ObjectType.DATANODE){
				xRefList.add(element.getXref());
			}
		}
		// Return the list.
		return xRefList;
	}
	
	/**
	 * In this method the date is returned when the last change is made in a pathway. The
	 * property that has to be given is:
	 * 'pathways' (a list of pathways you want to have the most recent date from). 
	 */
	public static long dateLastModified(List<File> pathways){
		// Set initial value.
		long lastModified = 0;
		// Walk through all the pathways.
		for (File pathway:pathways){
			// If pathway is more recent, use this date.
			if (lastModified < pathway.lastModified()){
				lastModified = pathway.lastModified();
			}
		}
	return lastModified;
	}
	
}
