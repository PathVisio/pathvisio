/* ReferenceSearchAllGenes is created by Roeland Plessius, March 2006
 * 
 * This files extends Ensembl.java, it use some methods of that file.
 * The program first connects to a local Ensembl database by means of properties from Ensembl.java
 * next the programs searches the database for all gene that it contains. After that the references of these genes
 * are searched for in the database. All found references and genes are then placed is a file. 
 */

//

// First some packages are imported
import java.util.*;
import java.io.*;
import org.ensembl.driver.*;
import org.ensembl.datamodel.*;

public class ReferenceSearchAllGenes extends Ensembl{
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
//			 Fetch all features and put them in an iterator (uses less memory than fetchAll)
			Iterator allGenesIt = ga.fetchIterator();
//			 The total number of genes in the database is counted and printed 
			long geneCount = ga.fetchCount();
			System.out.println("");		// inserted to keep a clear console
			System.out.println("The total number of genes in the database is: " + geneCount);
			System.out.println("");		// inserted to keep a clear console
			
			String current_ID;
			Gene current_gene;
			
//			 Start writing the output file in which the AccessionID's are printed to (one file!)
			PrintWriter outputStream = null;
//			 With a PrintWriter it is possible that a file cannot be found,
//			 to take care of this error try/catch is used
			try{
//				 Create a new .txt file "references.txt" in which the output is saved
				outputStream = new PrintWriter(new FileOutputStream("references.txt"));
				}
			catch(FileNotFoundException e){
				System.out.println("Error opening the file 'references.txt'");
//				 FileNotFound error is caught and printed 
				}
			 
//			 The genes iterator (all genes) will be gone through
			while(allGenesIt.hasNext()){
//				 The gene that is currently processed is saved in current_gene
				current_gene = (Gene) allGenesIt.next();
//				 The corresponding ID is looked up in the database, this makes it possible to save the IDs in the file as well
				current_ID = current_gene.getAccessionID();
//				 search for the corresponding references and placed them in an iterator
				List references = ensembl.getRefs(current_gene);
				Iterator referenceIt = references.iterator();
//				 because a gene can have multiple references, these have to be gone through as well  
				while(referenceIt.hasNext()){
//					 current reference is selected from the iterator
					ExternalRef reference = (ExternalRef)referenceIt.next();
//					 save the reference together with the ID in a file, where 3 columns are created,
//					 Gene ID, name of the database and the reference ID, these columns are seperated by a 'tab'					 
					outputStream.println(current_ID + "\t" + reference.getExternalDatabase().getName() + "\t" + reference.getDisplayID());
					}
				}
//			 close the file when all iterators have been gone through
			outputStream.close();
			}

		catch(Exception e){
//			catch is necessary for coredriver and geneadaptor
//			 in this case the folowing message is printed
//			 then the source of the error is printed
			System.out.println("The program 'ReferenceSearchAllGenes' produced an error");
			e.printStackTrace();
			}
		}
	}