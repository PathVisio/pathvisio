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
package ensembl2visio;

public class CreateGDB {
	final static String[] dbfiles = new String[] {
		"human/homo_sapiens_38_36",
		"rat/rattus_norvegicus_core_39_34_i",
		"yeast/yeast_41_1d",
		"yeast/yeast_test",
		"c:/temp/yeast_test",
	};
	/*
	 * Commandline:
	 * - inputfile
	 * - dbname
	 * - dbtype (derby, hsqldb, h2)
	 */
	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.println("No commandline options specified, using in-code setting");
			String dbname = dbfiles[4];
			String file = dbname + ".txt";
//			GDBMaker gdbMaker = new HsqldbGDBMaker(dbname);
//			GDBMaker gdbMaker = new H2GDBMaker(dbname);
			GDBMaker gdbMaker = new DerbyGDBMaker(dbname);
			gdbMaker.toGDB(file);
		} else { //TODO: neat commandline options
			String txt = args[0];
			String dbname = args[1];
			String dbtype = args[2];
			GDBMaker gdbMaker = null;
			if		(dbtype.equals("derby")) 
				gdbMaker = new DerbyGDBMaker(dbname);
			else if	(dbtype.equals("hsqldb")) 
				gdbMaker = new HsqldbGDBMaker(dbname);
			else if	(dbtype.equals("h2")) 
				gdbMaker = new H2GDBMaker(dbname);
			if(gdbMaker != null) gdbMaker.toGDB(txt);
		}
		
	}
}
