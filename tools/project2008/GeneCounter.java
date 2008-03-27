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
		
		// Totale aantal bekende genen in Ensemble Database (Ensemble.org)
		int numberOfGenesEN = 100;
		// Set aanmaken voor totale aantal gebruikte genen in WikiPathways
		Set<String> totalS=new HashSet<String>();
				
		

		
		// enter the directory that contains the pathways
		File dir = new File("D:\\My Documents\\Tue\\BIGCAT\\Rat");
		// get a list of files (recursive)
		List<File> filenames = getFileListing(dir);
		//System.out.println(filenames.get(1));
		System.out.println(filenames.size());
		
		/* Hier moet iets van een for-loop komen om de gegevens
		 * uit de verschillende Pathways te halen.
		 * */
		
		int i;
		//for (i=0;i<filenames.size();i++){
		for (i=0;i<5;i++){
		
			
			File fileName=filenames.get(i);
			System.out.println(fileName);
			
			//s berekenen
			Set<String> setOfRefPW=getRefPW(fileName);
		
			/*
			 * Hier moet de functie worden aangeroepen die de referenties omzet naar EN.
			 */
			//SimpleGdb db=new SimpleGdb("D:\\My Documents\\Tue\\BIGCAT",new DataDerby(),0);
			//db=getCrossRefs();
			
			
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
	public static Set<String> getRefPW(File filename) throws ConverterException{
		
		Set<String> s=new HashSet<String>();
		
		//File f = new File("D:\\My Documents\\Tue\\BIGCAT\\Rat\\"+namePathway);
		System.out.println("file = "+filename);
		
		Pathway p = new Pathway();
		p.readFromXml(filename, true);
				
		List<PathwayElement> pelts = p.getDataObjects();
		
		for (PathwayElement v:pelts){
			
			int type;
			type=v.getObjectType();
			
			if (type ==1){
				Xref reference;
				reference=v.getXref();
				String name=reference.getName();
				String id=reference.getId();
				System.out.println(id);
				//System.out.println(name);
				s.add(name);
				
			
			}
		}
		
		s.remove("null:");
		System.out.println(s);
		System.out.println(s.size());
		
		return s;
				
		
	}
	
	
	
}
