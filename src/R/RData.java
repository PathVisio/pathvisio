package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import R.RCommands.RException;

import util.FileUtils;
import util.XmlUtils.PathwayParser;
import util.XmlUtils.PathwayParser.Gene;

public class RData {
	List<File> pwFiles;
	
	
	/**
	 * Create a new RData instance containing the given pathway(s) and expression data (if loaded).
	 * Extra cross-references for GeneProducts will
	 * be stored when neccessary to match the expression data with pathways.
	 * @param pathways	The pathway or directory to include.
	 * @param recursive Whether to include subdirectories or not (ignored if argument 'pathways' points
	 * to a single file)
	 */
	public RData(File pathways, boolean recursive) {
		//Get the pathway files
		pwFiles = FileUtils.getFiles(pathways, "xml", recursive);
	}
	
	public void doTest(Rengine re) {
//		TEST:
//		 just send all the genes in the pathways to R and save workspace
		System.err.println("setting home directory");
		re.eval("setwd('/home/thomas/afstuderen/code/gmml-visio/trunk/tools/GmmlVisio2R')");
//		re.eval("setwd('D:/Mijn Documenten/Studie/afstuderen/code/gmml-visio/trunk/tools/GmmlVisio2R')");
		System.err.println("sourcing source.r");
		re.eval("source('source.r')");
		long t = System.currentTimeMillis();
		List<Pathway> pws = new ArrayList<Pathway>();
		List<GeneProduct> gps = new ArrayList<GeneProduct>();
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			for(File f : pwFiles) { ;
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
		} catch(Exception e) { e.printStackTrace(); }
		PathwaySet testPathwaySet = new PathwaySet("testset", pws);
		try { 
			testPathwaySet.toR(re, "testset"); 
		}
		catch(RException e) { e.printStackTrace(); }
		System.out.println("Time: " + (System.currentTimeMillis() - t));
		try {
			RCommands.evalE(re, "save(file='test.RData', list='testset')");
		} catch(RException e) { e.printStackTrace(); }
		
	}
	
	abstract class RObject {
		REXP rexp = null; //Cache REXP, leads to JNI errors!
		
		abstract REXP getREXP(Rengine re) throws RException;
		
		long getRef(Rengine re) throws RException {
	        return getREXP(re).xp;
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
			
			RCommands.assign(re, "tmpGps", geneProducts);
			System.err.println(RCommands.evalE(re, "exists('tmpGps')"));
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
	
	class ResultSet extends RObject {

		REXP getREXP(Rengine re) throws RException {
			return null;
		}
	}
}
