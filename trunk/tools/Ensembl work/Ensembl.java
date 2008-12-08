// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
/* Ensembl.java is created by Roeland Plessius March 2006
 * 
 * this file holds methods that are used by ReferenceSearchAllGenes.java and ReferenceSearchByName
 * The methods are: createLocalDatabase, createRemoteDatabase, showLocalDatabase, showRemoteDatabase, fetchGenesWithSearchID, fetchIDwithName  
 * 
 */

// First some packages are imported
import java.util.*;
import org.ensembl.datamodel.*;
import org.ensembl.driver.*;

class Ensembl{
	
	public Properties createLocalDatabase(){
//	 Creation of the database
//		INPUT: 	- none
//		OUTPUT: - database (Properties), the configuration of the databaseconnection 
		
//		 Database properties are defined:
		Properties database = new Properties();
//		 Automatic definition of the database properties:
		database.put("host","localhost");			// location of the database ("localhost")
		database.put("port","3306");				// accesport for the database (default port: "3306")
		database.put("user","root");				// username for the database ("root")
//		database.put("password",""); 				// comment because no password is needed
		database.put("database","Ensembl");		// name of Database ("Ensembl")
		database.put("schema_version","37");		// version 37, found in the folder name
//		 the database properties are then returned to the main program
		return database;
		}
	public Properties createRemoteDatabase(){
//		 Creation of the remote Ensembl database
//		INPUT: 	- none
//		OUTPUT: - database (Properties), the configuration of the databaseconnection 
		
	Properties database = new Properties();
	//Automatic definition of the database properties:
	database.put("host","ensembldb.ensembl.org");			// location of the database
	database.put("port","3306");							// accesport for the database
	database.put("user","anonymous");						// username for the database
	database.put("database_prefix","homo_sapiens_core");	// name of Database
	database.put("ensid_prefix","ENS");						// ID prefix
	return database;
	}
	public void showLocalDatabase(Properties database){
//	 Print the database properties on the screen
//		INPUT:	- database (Properties), the configuration of the databaseconnection
//		OUPUT:	- none
		
//		the database properties are defined by createDatabase and are printed here (as check usually)
		System.out.println("Local database information:");
		System.out.println("Host: " + database.get("host"));
		System.out.println("Port: " + database.get("port"));
		System.out.println("User: " + database.get("user"));
		System.out.println("Password: " + database.get("password"));
		System.out.println("Database: " + database.get("database"));
		System.out.println("Schema Version: " + database.get("schema_version"));
	}
	public void showRemoteDatabase(Properties database){
//		 Print the remote database properties on the screen
//			INPUT:	- database (Properties), the configuration of the databaseconnection
//			OUPUT:	- none
			
//			the database properties are defined by createRemoteDatabase and are printed here (as check usually)
			System.out.println("Remote database information:");
			System.out.println("Host: " + database.get("host"));
			System.out.println("Port: " + database.get("port"));
			System.out.println("User: " + database.get("user"));
			System.out.println("Database prefix: " + database.get("database_prefix"));
	}
	public Gene fetchGeneWithSearchID(String inputID, GeneAdaptor ga){
//	 With a given searchID, fetch the gene that corresponds with the ID
//		INPUT:	- inputID (String), the ID you want to search
//				- ga (GeneAdaptor), the GeneAdaptor of the original file is used to connect to the database
//		OUTPUT:	- gene (Gene), the gene that corresponds with the ID
		
//		 define the Gene gene, initialize as 'null' because of the try/catch
//		 defined outside try because it is the output of the method
		Gene gene = null;
		try{
//			 get in the database (by means of the geneAdaptor) and fetch the gene that corresponds with the ID
			gene = ga.fetch(inputID);
			}
//		 to catch a GeneAdaptor error 'catch' is used
		catch(Exception e){
//			 print the following text on the screen and the source of the error
			System.out.println("The program 'fetchGeneWithSearchID' produced an error");
			e.printStackTrace();
			}
//		Output is returned
		return gene;
	}
	public List getRefs(Gene gene){
//		With a given gene get the references
//		INPUT:	- gene (Gene), the gene that you want to search the references for
//		OUTPUT:	- refs (List), a list of references of the inputgene

//		 Define refs, the list in which the references are saved, use .getExternalRefs(), to search the references
		List refs = gene.getExternalRefs();
		
//		Next lines commented to reduce screenoutput, first created as check
//		Iterator refsIt = refs.iterator();
//		while(refsIt.hasNext()){
//			ExternalRef reference = (ExternalRef)refsIt.next();
//			System.out.println(reference.getDisplayID() + "\t" + reference.getExternalDatabase().getName());
//			}
		
//		 Output is returned
		return refs;
		}
	public List fetchIDwithName(String inputName, GeneAdaptor ga){
//		With a given name fetch the IDs that correspond with the name
//		INPUT:	- inputName (String), the name of the gene that you want to search for
//				- ga (GeneAdaptor), the GeneAdaptor of the original file is used to connect to the databasehe
//		OUTPUT: - searchIDs (List), a list of corresponding IDs
		
//		 First define searchIDs as null list
		List searchIDs = null;
//		 Try/catch is used because the geneadaptor can produce errors
		try{
//			 search for the ID for a given name by means of .fetchBySynonym
			searchIDs = ga.fetchBySynonym(inputName);
			}
//		 to catch a GeneAdaptor error 'catch' is used
		catch(Exception e){
//			 print the following text on the screen and the source of the error
			System.out.println("The program 'fetchIDwithName' produced an error");
			e.printStackTrace();
			}
//		 output is returned
		return searchIDs;
		}
}