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
import java.io.File;

import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;

import java.util.*;

public class GeneCounter {

	/**
	 * @param args
	 * @throws ConverterException 
	 * @throws DataException 
	 */
	public static void main(String[] args) throws ConverterException, DataException {
		
		// Total amount of known genes in the Ensembl Database (http://www.ensembl.org).
		int numberOfGenesEN = 17738;
		// Make a set to store the genes used in WikiPathways.
		Set<Xref> totalS=new HashSet<Xref>();
		
		// A SimpleGdb Database is used to be able to load the Database downloaded from internet. 
		//SimpleGdb db=new SimpleGdb("D:\\My Documents\\Tue\\BIGCAT\\Rn_39_34i.pgdb",new DataDerby(),0);		
		SimpleGdb db=new SimpleGdb("D:\\My Documents\\school\\jaar 3\\Semester 2\\project blok 3E\\Rn_39_34i.pgdb",new DataDerby(),0);		
		
		// The pathways are stored in the following directoryIn the following directory, the pathways are stored.
		//File dir = new File("D:\\My Documents\\Tue\\BIGCAT\\Rat");
		File dir = new File("D:\\My Documents\\school\\jaar 3\\Semester 2\\project blok 3E\\wikipathways_1206450480");
		
		// Here the method "getFileListing" is executed. In this method all files that are stored in the  list of files is created, so that each file can easily be loaded. 
		List<File> filenames = getFileListing(dir);
		//The number of files that is loaded can be printed. 
		//System.out.println(filenames.size());
		
		/* Hier moet iets van een for-loop komen om de gegevens
		 * uit de verschillende Pathways te halen.
		 * */
		
		int i;
		//for (i=0;i<filenames.size();i++){
		for (i=0;i<filenames.size();i++){
		
			
			File fileName=filenames.get(i);
			//System.out.println(fileName);
			
			//s berekenen
			Set<Xref> setOfRefPW=getRefPW(fileName,db);
		
			
			
			
			//s in de totale set zetten
			totalS.addAll(setOfRefPW);
		}
		/* Einde for loop
		 */
		
		// Output: Grootte van de totale set
		System.out.println(totalS);
		System.out.println(totalS.size());
		
		double usedgenes=totalS.size();
		usedgenes=usedgenes/numberOfGenesEN;
		System.out.println("Percentage of used genes at http://www.wikipathways.org = "+usedgenes+"%");
		//
		
}
	
	
	static public List<File> getFileListing(File path){
		// make a new list of files
		List<File> files = new ArrayList<File>();
		
		// get all the files and directories contained in the given path
	    File[] content = path.listFiles();
	    
	    // use a for loop to walk through content
	    for(File file : content) {
	    	  if ( file.isDirectory() ) {
	    		// if the file is a directory use recursion to get the contents of the sub-path
	    		List<File> subpath = getFileListing(file);
	    		// add the files contained in this sub-directory to the files list
		        files.addAll(subpath);
		      }
		      else {
		    	  // only use the file if it has a valid extension
		    	  if( file.getName().endsWith(".gpml") ) {
		    	 // add all files in the directory to the list files
		    	 files.add(file);
		    	 }
		    }
		}
	    // return all the obtained files
	    return files;
	}

	
	/*
	 * Deze functie geeft een set van alle referenties van een Pathway.
	 */
	public static Set<Xref> getRefPW(File filename,SimpleGdb db) throws ConverterException{
		
		Set<Xref> s=new HashSet<Xref>();
		
		//File f = new File("D:\\My Documents\\Tue\\BIGCAT\\Rat\\"+namePathway);
		//System.out.println("file = "+filename);
		
		Pathway p = new Pathway();
		p.readFromXml(filename, true);
				
		List<PathwayElement> pelts = p.getDataObjects();
		
		
		for (PathwayElement v:pelts){
			
			int type;
			type=v.getObjectType();
			
			if (type ==1){
				Xref reference;
				reference=v.getXref();
				//System.out.println(reference);
				
				List<Xref> cRef=db.getCrossRefs(reference,DataSource.ENSEMBL);
				
			
				s.addAll(cRef);
				
			
			}
		}
		
		//s.remove("null:");
		//System.out.println(s);
		//System.out.println(s.size());
		
		return s;
				
		
	}
	
	
	
}
