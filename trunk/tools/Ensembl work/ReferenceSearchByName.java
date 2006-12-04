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
/* ReferenceSearchByName is created by Roeland Plessius, March 2006
 * 
 * This files extends Ensembl.java, it use some methods of that file.
 * The program first connects to a local Ensembl database by means of properties from Ensembl.java
 * next the program searches the ID that corresponds with the gene name, after that all references of that gene
 * are searched for in the database. All found references are finally placed in a file
 */

// First some packages are imported
import java.util.*;
import java.io.*;
import org.ensembl.driver.*;
import org.ensembl.datamodel.*;

public class ReferenceSearchByName extends Ensembl{
	public static void main (String []args){
		try{
//			 Create new variable, ensembl, of type Ensembl
			Ensembl ensembl = new Ensembl();
//			 Create new variable, database, of type Properties
			Properties database = new Properties();
			
//			 Ensembl.createDatabase, from the file Ensembl.java is used to define the database
			database = (Properties)ensembl.createLocalDatabase();
//			 As check the database properties are printed on the screen
			ensembl.showLocalDatabase(database);
	
//			 Create a core driver
			CoreDriver coreDriver = CoreDriverFactory.createCoreDriver(database);
//			 Create a gene adaptor
			GeneAdaptor ga = coreDriver.getGeneAdaptor();

//			 The total number of genes in the database is counted and printed 
			long geneCount = ga.fetchCount();
			System.out.println("");		// inserted to keep a clear console
			System.out.println("The total number of genes in the database is: " + geneCount);
			System.out.println("");		// inserted to keep a clear console
			
//			 The name of the gene
			String geneName = "TP53B_HUMAN";

//			 All genes that correspond with the given name will be searched for in the database by means of ensembl.fetchIDwithName
			List foundGenes =  ensembl.fetchIDwithName(geneName, ga);
//			 The found Genes are converted to an Iterator
			Iterator foundGenesIt = foundGenes.iterator();
			String currentID;
//			 The genes iterator will be gone through
			while(foundGenesIt.hasNext()){
//				 The gene that is currently processed is saved in gene
				Gene gene = (Gene)foundGenesIt.next();
//				The corresponding ID is looked up in the database
				currentID = gene.getAccessionID();
//				 Check if a gene has been found:
				if (gene != null){
//		 			 If a gene has been found, than print the name of that gene together with the ID
					System.out.println("Name " + geneName + " corresponds to ID " + currentID);
	
//					 If a gene has been found, search for the corresponding references
					List references = ensembl.getRefs(gene);
					
//					 Write the references to a file with the name of the searchID: (one file for each ID!)
					PrintWriter referenceOutputStream = null;
//					With a PrintWriter it is possible that a file cannot be found,
//					to take care of this error try/catch is used
					try{
//						 Create a new .txt file with the name of the gene and the ID
						referenceOutputStream = new PrintWriter(new FileOutputStream(geneName + " - " + currentID + ".txt"));
						}
					catch(FileNotFoundException e){
//						 FileNotFound error is caught and printed 
						System.out.println("Error opening the file '" + geneName + " - " + currentID +".txt'");
						}
//					 Create an Iterator out of the list 'references'
					Iterator referenceIt = references.iterator();
//					 Write the DisplayID and ExternalDatabase to a file
//					 for that the reference iterator has to be gone through first
					while(referenceIt.hasNext()){
//						 current reference is selected from the iterator
						ExternalRef reference = (ExternalRef)referenceIt.next();
//						 current reference is placed in the file, where 2 columns are created:
//							 name of the database and the reference ID, the columns are seperated by a 'tab'
						referenceOutputStream.println(reference.getExternalDatabase().getName() + "\t" + reference.getDisplayID());
						}
//					 close the outputStream, and so the reference file
					referenceOutputStream.close();
					}
				}	
			}
		catch(Exception e){
//			catch is necessary for coredriver and geneadaptor
//			 in this case the folowing message is printed
//			 then the source of the error is printed
			System.out.println("The program 'ReferenceSearchByName' produced an error");
			e.printStackTrace();
			}
		}
	}