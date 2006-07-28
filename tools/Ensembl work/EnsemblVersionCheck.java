/* EnsemblVersionCheck.java is created by Roeland Plessius, March 2006
 * 
 * This program connects to the local database and to the remote ensembl database
 * and then checks the versions, if versions correspond do nothing,
 * if version do not correspond, warn the user to update
 */

//First some packages are imported
import java.util.*;
import org.ensembl.driver.*;

public class EnsemblVersionCheck extends Ensembl{
	public static void main (String []args){
		try{
//			 Create new variable, ensembl, of type Ensembl
			Ensembl ensembl = new Ensembl();
			
			
//	CREATION OF LOCAL DATABASE CONNECTION:
//			 Create new variable, database, of type Properties
			Properties localDatabase = new Properties();
			
//			 Ensembl.createDatabase, from the file Ensembl.java is used to define the database
			localDatabase = (Properties)ensembl.createLocalDatabase();
	
//			 Create a core driver
			CoreDriver localCoreDriver = CoreDriverFactory.createCoreDriver(localDatabase);
			
//	END OF LOCAL DATABASE CONNECTION CREATION
			
			
//	CREATION OF REMOTE DATABASE CONNECTION:
//			 Create new variable, database, of type Properties
			Properties remoteDatabase = new Properties();
			
//			 Ensembl.createDatabase, from the file Ensembl.java is used to define the database
			remoteDatabase = (Properties)ensembl.createRemoteDatabase();
	
//			 Create a core driver
			CoreDriver remoteCoreDriver = CoreDriverFactory.createCoreDriver(remoteDatabase);
			
//	END OF REMOTE DATABASE CONNECTION
			
//			 Get versions of local and remote database for comparison (therefore also conversion to int)
			int localVersion = Integer.parseInt(localCoreDriver.fetchDatabaseSchemaVersion());
			int remoteVersion = Integer.parseInt(remoteCoreDriver.fetchDatabaseSchemaVersion());
		
//			 If versions correspond then show user
			if(localVersion == remoteVersion){
				System.out.println("Your local database is up-to-date (version " + localVersion + ")");
			}
			
//			 If the versions do not correspond then warn user
			if(localVersion != remoteVersion){
				System.out.println("Please execute UpdateEnsemblDatabase.java");
				System.out.println("Your local database version (" + localVersion + ") is out of date");
				System.out.println("Current version remote database: " + remoteVersion);
			}

			
		}
		catch(Exception e){
//			catch is necessary for coredriver and geneadaptor
//			 in this case the folowing message is printed
			System.out.println("The program 'EnsemblVersionCheck' produced an error");
//			 then the source of the error is printed
			e.printStackTrace();
		}
	}
}