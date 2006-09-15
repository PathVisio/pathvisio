package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import data.GmmlGex;

import R.RCommands.RException;

import util.FileUtils;
import util.XmlUtils.PathwayParser;
import util.XmlUtils.PathwayParser.Gene;

public class RData {
	List<File> pwFiles;
	
	boolean exportPws;			//Export pathways or not
	boolean exportData;			//Export data or not
	boolean incCrit;			//Include criteria in export data or not
	
	String pwDir = "";			//Pathway directory to import
	String exportFile = "";		//File name to export RData
	String pwsName = "";		//Name of pathwayset object
	String dsName = "";			//Name of dataset object
 
	PathwaySet cachePathwaySet;
	DataSet cacheDataSet;
	
	
	public RData() {
		pwFiles = new ArrayList<File>();
	}
	
	/**
	 * Create a new RData instance containing the given pathway(s) and expression data (if loaded).
	 * Extra cross-references for GeneProducts will
	 * be stored when neccessary to match the expression data with pathways.
	 * @param pathways	The pathway or directory to include.
	 * @param recursive Whether to include subdirectories or not (ignored if argument 'pathways' points
	 * to a single file)
	 */
	public RData(File pathways, boolean recursive) {
		this();
		//Get the pathway files
		pwFiles = FileUtils.getFiles(pathways, "xml", recursive);
	}
	
	public List<File> getPathwayFiles() { return pwFiles; }
	
	public void exportToR(Rengine re) throws Exception {
		if(exportData) doExportData(re);
		if(exportPws) doExportPws(re);
	}

	public void doExportPws(Rengine re) throws Exception {
		File pwDir = new File(this.pwDir);
		
		//Check some parameters
		if(exportFile.equals("")) throw new Exception("specify file to export to");
		if(!pwDir.canRead()) throw new Exception("invalid pathway directory: " + this.pwDir);
		if(pwsName.equals("")) throw new Exception("No name specified for the exported pathways object");
		
		pwFiles = FileUtils.getFiles(pwDir, "xml", true);

		List<Pathway> pws = new ArrayList<Pathway>();
		List<GeneProduct> gps = new ArrayList<GeneProduct>();

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		for(File f : pwFiles) {
			gps.clear();
			PathwayParser p = new PathwayParser(xmlReader);
			try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { 
				GmmlVision.log.error("Couldn't read " + f, e); 
				continue; 
			}

			for(Gene g : p.getGenes()) {
				gps.add(new GeneProduct(g.getId(), g.getCode()));
			}
			
			pws.add(new Pathway(p.getName(), f.getName(), gps));
		}
		PathwaySet testPathwaySet = new PathwaySet(pwsName, pws);
		testPathwaySet.toR(re, pwsName); 
		RCommands.evalE(re, "save.image(file='"+ exportFile + "')");
	}

	public void doExportData(Rengine re) throws Exception {
		
	}
		
	abstract class RObject {
		REXP rexp = null; //Cache REXP, leads to JNI errors!
		
		abstract REXP getREXP(Rengine re) throws RException;
		
		long getRef(Rengine re) throws RException {
	        long ref = getREXP(re).xp;
	        if(ref == 0) throw new RException(re, "Unable to get reference to symbol");
	        return ref;
		}
		
		void toR(Rengine re, String symbol) throws RException {
			long ref = getRef(re);
			re.rniAssign(symbol, ref, 0);
		}
	}
	
	class PathwaySet extends RObject {
		String name;
		List<Pathway> pathways;
		
		PathwaySet(String name, List<Pathway> pathways) {
			this.name = name;
			this.pathways = pathways;
		}
		
		REXP getREXP(Rengine re) throws RException {
//			if(rexp != null) return rexp;
			
			RCommands.assign(re, "tmpPws", pathways);
			
			String cmd = "PathwaySet('" + name + "', tmpPws)";
			rexp = RCommands.evalE(re, cmd);
			
			RCommands.rm(re, "tmpPws");
			return rexp;
		}	
	}
	
	class Pathway extends RObject {
		String name;
		String fileName;
		List<GeneProduct> geneProducts;
		
		Pathway(String name, String fileName, List<GeneProduct> geneProducts) {
			this.name = name;
			this.fileName = fileName;
			this.geneProducts = geneProducts;
		}
		
		void addGeneProduct(GeneProduct gp) { geneProducts.add(gp); }
		
		REXP getREXP(Rengine re) throws RException {
//			if(rexp != null) return rexp;
			
			System.err.println(geneProducts);
			RCommands.assign(re, "tmpGps", geneProducts);
			
			rexp = RCommands.evalE(re, "Pathway('" + name + "','" + fileName + "', tmpGps)");;
	          
			RCommands.rm(re, "tmpGps");
			return rexp;
		}		
	}
	
	class GeneProduct extends RObject {
		List<String> ids;
		List<String> codes;
		
		GeneProduct() {
			ids = new ArrayList<String>();
			codes = new ArrayList<String>();
		}
		
		GeneProduct(String id, String code) {
			this();
			addReference(id, code);
		}
		
		void addReference(String id, String code) { 
			ids.add(id);
			codes.add(code);
		}
		
		REXP getREXP(Rengine re) throws RException {
//			if(rexp != null) { System.err.println("\t### REXP existed"); return rexp; }
			
			RCommands.assign(re, "tmpIds", ids.toArray(new String[ids.size()]));
			RCommands.assign(re, "tmpCodes", codes.toArray(new String[codes.size()]));

            rexp = RCommands.evalE(re, "GeneProduct(tmpIds, tmpCodes)");
			RCommands.rm(re, new String[] { "tmpIds", "tmpCodes" });
			return rexp;
		}
		
		public String toString() {
			return "GeneProduct: " + ids + ", " + codes;
		}
	}
	
	class DataSet extends RObject {
		String name;
		List<GeneProduct> geneProducts;
		double[][] data;
		int[][] subsets; //value either 1 or 0
		
		DataSet() throws Exception {
			//Get the data from GmmlGex
			if(!GmmlGex.isConnected()) return;
			Statement s = GmmlGex.getCon().createStatement();
			
			//Get distinct gps
			java.sql.ResultSet r = s.executeQuery("SELECT DISTINCT id, code FROM data");
		
			//Subset samples (numeric)
			
			//Get sample data for gps
			
			
		}
		/*
		representation(
		name = "character",
		geneProducts = "list",
		data = "matrix",
		subsets = "matrix"(non-Javadoc)
		 */
		REXP getREXP(Rengine re) throws RException {
			return null;
		}
	}
	class ResultSet extends RObject {

		REXP getREXP(Rengine re) throws RException {
			return null;
		}
	}
}
