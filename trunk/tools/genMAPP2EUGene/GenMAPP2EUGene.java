/**
 * @version 2006_07_05
 * @author Thomas Kelder (BiGCaT)
 */

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;


public class GenMAPP2EUGene {

	 static String database_after = ";DriverID=22;READONLY=true";
	 static String database_before =
	            "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
	 
	 static Logger log = new Logger();
	 
	 public static void main(String[] args) {
		 if(args.length != 1) {
			 System.out.println("Incorrect arguments\n" +
					 "Usage: java GenMAPP2EUGene {Dir}\n" +
					 "Where {Dir} is the directory containing the files to convert (subdirectories" +
			 "will be included)");
			 System.exit(0);
		 }
		 
		 
		 File dir = new File(args[0]);
		 GenMAPP2EUGene gm2eu = new GenMAPP2EUGene(dir);
		 System.out.println("Converting...");
		 gm2eu.convert(dir);
		 System.out.println("Finished!");
	 }
	 
	 public GenMAPP2EUGene(File directory) {
		 createSystemMappings();
		 String logFile = "conversion.log";
		 try { log.setStream(new PrintStream(logFile)); } 
		 catch(Exception e) { e.printStackTrace(); }
		 System.out.println("Generated conversion log: '" + logFile);
	 }
	 
	 void convert(File dir) {
		for(File f : getMappFiles(dir)) {
			Pathway p = new Pathway(f);
			p.writeToEUGene();
		}
	 }
	 
	 ArrayList<File> getMappFiles(File folder) {
		 ArrayList<File> pathways = new ArrayList<File>();
		 
		 //Get all .mapp files and sub-directories in this directory
		 File[] files = folder.listFiles(new FileFilter() {
			 public boolean accept(File f) {
				 return (f.isDirectory() || f.getName().endsWith(".mapp"));
			 }
		 });
		 //Recursively add the pathway files
		 for(File f : files) {
			 if(f.isDirectory()) pathways.addAll(getMappFiles(f));
			 else pathways.add(f);
		 }

		 return pathways;
	 }
	 
	 Connection con;
	 void connect(File f) throws Exception {
		 log.trace("Connecting to pathway " + f.getAbsolutePath());
		 String database = database_before + f.getAbsolutePath() + database_after;
		 // Close previous connection
		 if(con != null) { con.close(); con = null; }
		 // Load Sun's jdbc-odbc driver
		 try {
			 Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			 // Create the connection to the database
			 con = DriverManager.getConnection(database, "", "");
			 return;
		 } catch(ClassNotFoundException e) {
			 log.error("can't load sun's jdbc.odbc driver", e);
		 } catch(NullPointerException ne) {
			 log.error("sun's jdbc.odbc driver not correctly loaded (are you working in linux?)", ne);
		 }
		 System.out.println("Conversion aborted, couldn't load jdbc.odbc driver. " +
		 		"See conversion log for details");
		 System.exit(0);
	 }
	 
	 class Pathway {
		 File file;
		 String name;
		 String system;
		 ArrayList<String> ids;
		 ArrayList<String> codes; 
		 
		 public Pathway(File f)  { 
			 this.file = f;
			 this.name = f.getName().replace(".mapp", "");
			 //TODO: GMML support and switch between GMML/GenMAPP depending on filetype
			 readGenMAPP();
		 }
		 
		 void writeToEUGene() {
			 String euGeneSystem = null;
			 StringBuilder geneString = new StringBuilder();
			 StringBuilder missedGenes = new StringBuilder();
			 try {
				 euGeneSystem = getEUGeneSystem();
				 
				 for(int i = 0; i < ids.size(); i++) {
					 String code = codes.get(i);
					 String id = ids.get(i);
					 if(code.equals(system)) { //Check if gene is of most occuring system
						 geneString.append(id + "\n");
					 } else {
						 missedGenes.append("(" + id + ", " + code + ") ");
						 log.error("id '" + id + "' differs from pathway annotation system");
					 }
				 }
			 } catch(Exception e) { log.error("Unable to convert pathway", e); return; }
			 
			 //Write the file
			 File outFile = new File(file.getAbsolutePath().replace(".mapp", ".pwf"));
			 PrintStream out = null;
			 try {
			 out = new PrintStream(outFile);
			 } catch(Exception e) { 
				 log.error("Unable to open output file " + outFile);
				 return;
			}
			 //Print the data
			 out.println("//PATHWAY_NAME = " + name);
			 out.println("//PATHWAY_SOURCE = GenMAPP");
			 out.println("//PATHWAY_MARKER = " + euGeneSystem);
			 if(missedGenes.length() > 0) out.println("//GENES_NOT_CONVERTED (id, systemcode):" + missedGenes );
			 out.print(geneString);
			
			 out.close();
		 }
		 
		 void readGenMAPP() {
			 try { connect(file); } catch(Exception e) { 
				 log.error("unable to connect", e);
				 return;
			 }
			 
			 log.trace("Fetching genes");
			 
			 ids = new ArrayList<String>();
			 codes = new ArrayList<String>();
			 HashMap<String, Integer> codeCount = new HashMap<String, Integer>();
			 
			 try {
				 //Fetch genes
				  ResultSet r = con.createStatement().executeQuery(
						 "SELECT Id, SystemCode FROM Objects WHERE Type = 'Gene'" );
				 while(r.next()) {
					 String id = r.getString("Id");
					 String code = r.getString("SystemCode");
					 if(id != null && code != null 
							 && !id.equals("") && !code.equals("")) { ids.add(id); codes.add(code); }
					 
					 //Increase code count for this code
					 if(codeCount.containsKey(code)) codeCount.put(code, codeCount.get(code) + 1);
					 else codeCount.put(code, 1);
					 log.trace("\tFetched gene: " + id + ", " + code);
				 }
				 
				 //Get most occuring systemcode
				 String maxCode = null;
				 for(String code : codeCount.keySet()) {
					 if(maxCode == null || codeCount.get(code) > codeCount.get(maxCode)) {
						 maxCode = code;
					 }
				 }
				 system = maxCode;
				 
				 if(codeCount.keySet().size() > 1) {
				 log.warn("\tThis pathway contains genes with different SystemCodes; '" +
				 		maxCode + "' has the highest occurence and is therefore chosen as PATHWAY_MARKER" +
				 				" for the EUGene file\n\t Other SystemCodes found and their occurences: "
				 		+ codeCount);
				 }
				 
			 } catch(Exception e) { log.error("", e); }
			 log.trace("Finished reading pathway " + file);
		 }
		 
		 String getEUGeneSystem() throws Exception {
			 if(systemMappings.containsKey(system)) return systemMappings.get(system);
			 else throw new Exception("No corresponding EUGene system for '" + system + "'");
		 }
	 }
		 
	 HashMap<String, String> systemMappings;
	 void createSystemMappings() {
		systemMappings = new HashMap<String, String>();
		 for(int i = 0; i < euGeneSystems.length; i++) {
			systemMappings.put(genMappSystems[i], euGeneSystems[i]);
		}
	 }
	 
	 static String[] euGeneSystems = new String[]
	                                                    {
		 "ENSEMBL_GENE_ID",
		 "UNIPROT", 
		 "ENTREZ", 
		 "UNIGENE", 
		 "AFFYMETRIX", 
//		 "AGILENT", // Not supported by GenMAPP
		 "HGNC",
		 "PDB_ID", 
		 "SGD_ID" 
	                                                    };
	 static String[] genMappSystems = new String[]
	                                                     {
		 "En",
		 "S",
		 "L",
		 "U",
		 "X",
//		 "",
		 "H",
		 "Pd",
		 "D"
	                                                     };

	 
}
