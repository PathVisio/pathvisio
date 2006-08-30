package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

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
		//TEST:
		// just send all the genes in the pathways to R and save workspace
		re.eval("setwd('/home/thomas/afstuderen/code/gmml-visio/trunk/tools/GmmlVisio2R')");
		re.eval("source('source.r')");
		long t = System.currentTimeMillis();
		List<Pathway> pws = new ArrayList<Pathway>();
		List<GeneProduct> gps = new ArrayList<GeneProduct>();
		try {
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			for(File f : pwFiles) { 
//				System.out.println("start " + f.getName());
				gps.clear();
//				System.out.println("cleared gps");
				PathwayParser p = new PathwayParser(xmlReader);
				try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { 
					GmmlVision.log.error("Couldn't read " + f, e); 
					continue; 
				}
//				System.out.println("parsed file");
				for(Gene g : p.getGenes()) {
//					System.out.println("\t> processing gene " + g.getId());
					gps.add(new GeneProduct(g.getId(), g.getCode()));
				}
				pws.add(new Pathway(p.getName(), f.getName(), gps));
//				System.out.println("created pathway");
			}
		} catch(Exception e) { e.printStackTrace(); }
		PathwaySet testPathwaySet = new PathwaySet("testset", pws);
		testPathwaySet.toR(re, "testset");
		System.out.println("Time: " + (System.currentTimeMillis() - t));
//		re.eval("save(file='test.RData', list='testset')");
//		re.eval("y");
	}
	
	abstract class RObject {
		static final long NOREF = 0;
		
		long ref = NOREF;
		
		abstract long getRef(Rengine re);
		
		void toR(Rengine re, String symbol) {
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
		
		long getRef(Rengine re) {
			if(ref != NOREF) return ref;
			RCommands.assign(re, "tmpPws", pathways);
			
			REXP rexp = re.eval("PathwaySet('" + name + "', tmpPws)");
	        ref = rexp.xp;
	          
			RCommands.rm(re, "tmpPws");
			
			return ref;
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
		
		long getRef(Rengine re) {
			if(ref != NOREF) return ref;
			RCommands.assign(re, "tmpGps", geneProducts);
			
			REXP rexp = re.eval("Pathway('" + name + "','" + fileName + "', tmpGps)");
	        ref = rexp.xp;
	          
			RCommands.rm(re, "tmpGps");
			
			return ref;
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
		
		long getRef(Rengine re) {
			if(ref != NOREF) return ref;
			RCommands.assign(re, "tmpIds", ids.toArray(new String[ids.size()]));
			RCommands.assign(re, "tmpCodes", codes.toArray(new String[codes.size()]));

            REXP rexp = re.eval("GeneProduct(tmpIds, tmpCodes)");
            ref = rexp.xp;
			
			RCommands.rm(re, new String[] { "tmpIds", "tmpCodes" });
			return ref;
		}
	}
	
	class ResultSet extends RObject {

		long getRef(Rengine re) {
			return NOREF;
		}
		
	}	
}
