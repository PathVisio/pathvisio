/* UpdateEnsemblDatabase.java is created by Roeland Plessius, March 2006
 * 
 * This program executes the command line with a batch file.
 * This batch file contains the information to create the Ensembl database.
 * More info: "README for Ensembl database updater.doc"
 * 
 */ 

// First some packages are imported
import java.io.*;
import sun.net.ftp.*;

public class UpdateEnsemblDatabase {
  public UpdateEnsemblDatabase(String cmdline) {
    try {
     String line;
     Process p = Runtime.getRuntime().exec(cmdline);
     BufferedReader input = new BufferedReader (new InputStreamReader(p.getInputStream()));
     while ((line = input.readLine()) != null) {
       System.out.println(line);
       }
     input.close();
     
     } 
    catch (Exception e) {
    	System.out.println("UpdateEnsemblDatbase.java produced an error");
     e.printStackTrace();
     }
   }

public static void main(String argv[]) {
  new UpdateEnsemblDatabase("Ensembl.bat");
  }
}