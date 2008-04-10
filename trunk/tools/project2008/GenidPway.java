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

// import the things needed to run this java file.
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.pathvisio.data.DataDerby;
import org.pathvisio.data.DataException;
import org.pathvisio.data.SimpleGdb;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Xref;

// class to get the genid's (ensemble format) from all the pathways in a directory.
public class GenidPway{
	
	public static void main(String[] args) throws DataException, ConverterException{	
		// empty; main is never used
	}
	
	// return a set of strings, containing all the genids (ensemble format) from all pathways in a directory
	public static Set<String> getGenidPways(String dbDir, String pwDirString) throws DataException, ConverterException{
		
		// open pathway directory
		File pwDir = new File(pwDirString);
		
		// get all the xrefs from the pathways using the getXrefs method
		Set<Xref> xrefs = getXrefs(dbDir,pwDir);
		
		// create a new set of strings, where all the genid's are loaded
		Set<String> setWithGenIdsInPathways = new HashSet<String>();
		
		// loop through all xrefs. For each xref, get the id (=genid in ensemble format!) and add it to the set of strings
		for(Xref xref: xrefs){
			setWithGenIdsInPathways.add(xref.getId());
		}		
		
		// return the set of strings containing all the genids
		return setWithGenIdsInPathways;
	}
	
	// load all the pathways, get the xrefs, change the genid's to ensemble format, and return the xrefs as a set
	public static Set<Xref> getXrefs(String dbDir,File pwDir) throws DataException, ConverterException{

		// Here the method "getFileListing" from the class FileUtils is executed.
		// In this method all files that are stored in a directory are stored in a
		// list of strings. These then can be easily loaded
		String pwExtension = ".gpml";
		List<File> filenames = FileUtils.getFileListing(pwDir, pwExtension);
		
		// Make a set to store the genes used in the pathways.
		Set<Xref> allGenes=new HashSet<Xref>();
		
		// A SimpleGdb Database is used to be able to load the database 
		SimpleGdb db=new SimpleGdb(dbDir,new DataDerby(),0);
		
		
		// In the following for-loop the information from all different pathways must be loaded. 
		for (File filename: filenames){
			
			// in the method getRefPW from the GeneCounter class, all xrefs are loaded
			// and converted to the ensemble format.
			Set<Xref> setOfRefPW=GeneCounter.getRefPW(filename,db);			
				
			// add this new set to 'allGenes'
			allGenes.addAll(setOfRefPW);
		}
		
		// return all genes
		return allGenes;
	}
	
	
	
	
	
}