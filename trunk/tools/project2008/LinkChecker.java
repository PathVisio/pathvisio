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
		// TODO: load the database and the filenames in a nice way..
		
		
		
		// load the database of the rat species
		SimpleGdb database = new SimpleGdb("C:\\muis data\\Rn_39_34i\\Rn_39_34i.pgdb", new DataDerby(), 0);
		
		// create a list containing Strings that represent the filenames of a directory
		List<String> filenames = makeFilenameList();
		
		// for each filename create a list containing the Xref's
		for (String filename:filenames)
		{
			// load the file
			File file = new File(filename);
			
			// load the pathway
			Pathway pway = new Pathway();
			boolean validate = true;
			pway.readFromXml(file, validate);
		
			// make a list containing the Xref's 
			List<Xref> xrefList = makeXrefList(pway);
			
			// as a debug tool, show how much Xref's are found in the list
			System.out.println("size of the XrefList: "+xrefList.size());
			
			
			// give the precentage of Xrefs the database contains
			String percentage = calculatePercentage(xrefList, database);
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
	
	
	public static List<String> makeFilenameList(){
		// make a list containing strings which represent all the files in a directory. This has to be made beautifull,
		// 'cuz this way it looks kind of sloppy. ==> Ruben :)
		
		
		List<String> filenames = new ArrayList();
		String filename = "C:\\muis data\\Rn_Apoptosis.gpml";
		filenames.add(filename);
		filenames.add("C:\\muis data\\Rn_Alanine_and_aspartate_metabolism_KEGG.gpml");
		
		return filenames;
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
