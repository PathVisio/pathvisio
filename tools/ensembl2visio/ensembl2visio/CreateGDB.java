package ensembl2visio;

public class CreateGDB {
	final static String[] dbfiles = new String[] {
		"human/homo_sapiens_38_36",
		"rat/rattus_norvegicus_core_39_34_i",
	};
	
	public static void main(String[] args) {
		String dbname = dbfiles[0];
		String file = dbname + ".txt";
//		GDBMaker gdbMaker = new HsqldbGDBMaker(file, dbname);
//		GDBMaker gdbMaker = new H2GDBMaker(file, dbname);
		GDBMaker gdbMaker = new DerbyGDBMaker(file, dbname);
		gdbMaker.toGDB();
	}
}
