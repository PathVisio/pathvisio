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

public class LinkChecker {
	/**
	* in the String[] args, 3 arguments are given:
	* in example:
	* "C:\\databases\\"
	* "C:\pathways"
	* "C:\\result.html"
	* 
	* the first one is the directory that contains the databases
	* the second one is the directory that contains the pathway cache
	* the third one is the filename (note the html extension!) of where the results are stored'
	* 	
	* Good Luck!
	*/
	public static void main(String[] args) throws ConverterException, DataException, XmlRpcException, IOException {
		boolean online = true; // set to true if connected to the internet
		
		// check if the String[] args is given, and make Files 
		// containing the directories to the pathways and databases
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

		if (online){
			// download all pathways to the pathway folder
			WPDownloadAll.download(args[1]);
		}
		
		// get a list of files of databases and pathways
		String pwExtension = ".gpml";
		String dbExtension = ".pgdb";
		List<File> pwFilenames = FileUtils.getFileListing(pwDir, pwExtension);
		List<File> dbFilenames = FileUtils.getFileListing(dbDir, dbExtension);
		long localdate = dateLastModified(pwFilenames);
		Date d = new Date(localdate); 
		DateFormat df = DateFormat.getDateTimeInstance();
		System.out.println("Date last modified: "+df.format(d)); 
				
		// Load all databases in List<SimpleGdb> databases,
		// and load all filenames of the loaded databases
		// in List<String> databaseFilenames
		List<SimpleGdb> databases          = new ArrayList<SimpleGdb>();
		List<String>    databasesFilenames = new ArrayList<String>();
		
		for (File dbFilename: dbFilenames){
			
			// load a database and add it to the list
			SimpleGdb database = new SimpleGdb(dbFilename.getPath(), new DataDerby(), 0);
			databases.add(database);
			
			// extract a filename and add it to the list
			databasesFilenames.add(dbFilename.getName());
			
			}
				
		// create the output file
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(outfile));
		}
		catch(IOException e){
			System.out.println("Can't open folder "+outfile);
			System.exit(0);
		}
		
		// print the first lines of the created HTML file
		String titleOfHTMLPage = "LinkChecker.java results";
		out.print("<HTML><HEAD><TITLE>"+titleOfHTMLPage+"</TITLE></HEAD><BODY><center><h1>"+titleOfHTMLPage+"</h1><TABLE border=\"1\"><TR><TD><B>Filename</B></TD><TD><B>Percentage found in Gdb</B></TD></B></TR>");
		
		// load all pathway files, and give the percentage of found Xrefs in the database.
		for (File filename:pwFilenames)
		{
			// load the pathway
			Pathway pway = new Pathway();
			boolean validate = false; // set to true if you want to validate the pathway file
			
			try{
				pway.readFromXml(filename, validate);
			}
			catch(ConverterException e){
				System.out.println("empty file is found");
			}
		
			// make a list containing the Xref's 
			List<Xref> xrefList = makeXrefList(pway);
			
			// find the good database for the pathway;
			// the filename of a database must have the same 2 starting letters as
			// the filename of the pathway
			int i = 0;
			int index = -1;
			for (String databaseFilename: databasesFilenames){
				if (databaseFilename.substring(0,2).equalsIgnoreCase(filename.getName().substring(0,2))){
					index = i;
					}
				i++;
			}		
			
			// if the database is found, add a row to the table of the html file, 
			// containing the name of the pathway and the percentage of found
			// Xref's in the database
			if (index != -1){
				out.print("<TR><TD>"+filename.getName()+"</TD>");
				String percentage = calculatePercentage(xrefList, databases.get(index));
				out.println("<TD>"+percentage+databasesFilenames.get(index)+")</TD></TR>");
				}
			// if the database is not found, add a row to the table of the html file,
			// containing the name of the pathyway and "db not found"
			else{
			out.print("<TR><TD>"+filename.getName()+"</TD>");
			out.println("<TD> db not found </TD></TR>");				
			}
		
		}
		
		// all pathway rows are added to the table. Now the HTML file has to be closed properly
		out.print("</TABLE></center></BODY></HTML>");
		out.close();
		System.out.println("Results are stored in " + outfile);
	}
	
	/** in this method, the percentage of Xref's found in the database is calculated.
	*   the property's you have to enter are xrefList; a list of all the xrefs from
	*   a pathway, and database; a SimpleGdb database that has to be checked if it
	*   contains the Xrefs.
	*/
	public static String calculatePercentage(List<Xref> xrefList, SimpleGdb database){
		
		int countTrue = 0;       // counter for the true outcome (a xref is found)
        int countTotal = 0;      // counter for the total of xrefs
		String percentage;       // string for the outcome
		double percentagedouble; // double for the actual percentage
		
		// check each Xref from the xrefList if it is found in the database
		for (Xref xref:xrefList)
		{
			if (database.xrefExists(xref) == true){
				countTrue++;
				}
			
			countTotal++;
			}
				
		// calculate the percentage of found references
		if (countTotal != 0){
			percentagedouble = 100*countTrue/countTotal;
			percentage = (percentagedouble+"% (of total: "+countTotal+" in ");
			}
		else{
			percentage = ("<font color=\"red\"><b>total: 0</b></font> (divide by zero) in ");
			}

		// return the output
		return percentage;
		}
	
	/** In this method a list of Xrefs in made. The list contains all the
	*   Xrefs from a given pathway. The property you have to give
	*   is pway, a Pathway where you want to extract all the Xrefs from
	*/
	public static List<Xref> makeXrefList(Pathway pway){
		
		List<PathwayElement> pelts = pway.getDataObjects();
		List<Xref> xRefList = new ArrayList<Xref>();
		
		for (PathwayElement element:pelts){
			// check if the objectType is a datanode, and add it to the list
			if (element.getObjectType() == ObjectType.DATANODE)
			{
				xRefList.add(element.getXref());
				}
			}
		
		// return the list
		return xRefList;
	}
	
	public static long dateLastModified(List<File> pathways){
		// set initial value
		long lastModified = 0;
		// walk through all the pathways
		for (File pathway:pathways){
			// if pathway is more recent, use this date
			if (lastModified < pathway.lastModified()){
				lastModified = pathway.lastModified();
			}
		}
	return lastModified;
	}
	
}
