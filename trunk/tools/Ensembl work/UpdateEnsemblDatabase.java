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