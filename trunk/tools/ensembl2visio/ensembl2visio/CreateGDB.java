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
//			GDBMaker gdbMaker = new HsqldbGDBMaker(file, dbname);
//			GDBMaker gdbMaker = new H2GDBMaker(file, dbname);
			GDBMaker gdbMaker = new DerbyGDBMaker(file, dbname);
			gdbMaker.toGDB();
		} else { //TODO: neat commandline options
			String txt = args[0];
			String dbname = args[1];
			String dbtype = args[2];
			GDBMaker gdbMaker = null;
			if		(dbtype.equals("derby")) 
				gdbMaker = new DerbyGDBMaker(txt, dbname);
			else if	(dbtype.equals("hsqldb")) 
				gdbMaker = new HsqldbGDBMaker(txt, dbname);
			else if	(dbtype.equals("h2")) 
				gdbMaker = new H2GDBMaker(txt, dbname);
			if(gdbMaker != null) gdbMaker.toGDB();
		}
		
	}
}
