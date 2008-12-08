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
/* EnsemblFTPConnection.java is created by Roeland Plessius, March 2006
 * 
 * this file connects to the ensembl.org ftp, and is not finished yet,
 * purpose is to connect to ensembl ftp, search latest sql tables of the homo sapiens core database (for version 37: 
 * ftp://ftp.ensembl.org/pub/release-37/homo_sapiens_37_35j/data/mysql/homo_sapiens_core_37_35j/ )
 * and then download these tables to the folder D:\Ensembl
 * 
 * The connection to the database is created, the right folder for Database version 37 is selected
 * 
 * To be done: download .gz files, make folder version independent (for future updates)
 * 
 */
import sun.net.ftp.FtpClient;

class EnsemblFTPConnection{
	public static void main (String [] args){
		try {
			String host = "ftp.ensembl.org";
			String user = "Anonymous";
			String email = "scoopy0002@hotmail.com";
			String sDir = "pub/release-37/homo_sapiens_37_35j/data/mysql/homo_sapiens_core_37_35j/";
			System.out.println("Connecting to host " + host);
			FtpClient ensembl_ftp = new FtpClient(host);
			ensembl_ftp.login(user, email);
			System.out.println("User " + user + " login OK");
			System.out.print(ensembl_ftp.welcomeMsg);
			ensembl_ftp.cd(sDir);
		}
	    catch (Exception e) {
	    	System.out.println("EnsemblFTPConnection.java produced an error");
	    	e.printStackTrace();
	    }
	}
}