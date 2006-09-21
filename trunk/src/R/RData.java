package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import util.FileUtils;
//import util.Utils;
import util.SwtUtils.SimpleRunnableWithProgress;
import util.XmlUtils.PathwayParser;
import util.XmlUtils.PathwayParser.Gene;
import R.RCommands.RException;
import R.RCommands.RInterruptedException;
import data.GmmlGex;
import data.GmmlGex.Sample;

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
	
	public void doExport(Rengine re) throws Exception {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(GmmlVision.getWindow().getShell());
		SimpleRunnableWithProgress rwp = null;
		try {
			if(exportData) {
				rwp = new SimpleRunnableWithProgress(
						this.getClass(), "doExportData", 
						new Class[] { re.getClass() }, new Object[] { re }, this);
				rwp.setMonitorInfo("Exporting data", IProgressMonitor.UNKNOWN);
				dialog.run(true, true, rwp); 
			}
			if(exportPws) {
				rwp = new SimpleRunnableWithProgress(
						this.getClass(), "doExportPws", 
						new Class[] { re.getClass() }, new Object[] { re }, this);
				rwp.setMonitorInfo("Exporting pathways", IProgressMonitor.UNKNOWN);
				dialog.run(true, true, rwp);
			}
			
			RCommands.evalEN(re, "save.image(file='"+ exportFile + "')");
			
		} catch(InvocationTargetException ex) {
			rwp.openMessageDialog("Error", "Unable to export data: " + ex.getCause().getMessage());
			GmmlVision.log.error("Unable to export to R", ex);
			return;
		}
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
	}
	
	public void doExportData(Rengine re) throws Exception {
		if(!GmmlGex.isConnected()) throw new Exception("No expression data loaded");
		DataSet ds = new DataSet(dsName);
		ds.toR(re, dsName);
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
	
	class GeneProductData extends RObject {
		GeneProduct geneProduct;
		
		HashMap<Sample, HashMap<String, Object>> data;
		HashMap<Sample, HashMap<String, Integer>> sets;
		
		GeneProductData(GeneProduct geneProduct) throws Exception {
			this.geneProduct = geneProduct;
			data = new HashMap<Sample, HashMap<String, Object>>();
			sets = new HashMap<Sample, HashMap<String, Integer>>();
			
			queryData();
		}
		
		/**
		 * Queries the data for this objecs's {@link GeneProduct} from {@link GmmlGex}
		 */
		void queryData() throws Exception {
			Statement s = GmmlGex.getCon().createStatement();
			
//			//Get size of data array
//			java.sql.ResultSet r = s.executeQuery(
//					" SELECT TOP 1 COUNT(idSample) as nr FROM " +
//					"	SELECT idSample FROM data " +
//					" 		WHERE id IN(" + Utils.list2String(geneProduct.ids, '\'', ',') + ")" +
//					" 		AND code IN(" + Utils.list2String(geneProduct.codes, '\'', ',') + ")" +
//					" GROUP BY nr ORDER BY nr DESC"	);
//			r.next();
//			int size = r.getInt(1);

			//Using IN()
//			long t = System.currentTimeMillis();
//			java.sql.ResultSet r = s.executeQuery(
//				"SELECT id, code, idSample, data FROM expression " +
//				" WHERE id IN(" + Utils.list2String(geneProduct.ids, '\'', ',') + ")");
//			System.out.println("GeneProductData query took: " + (System.currentTimeMillis() - t));
			
			//Split over multiple queries
			long t = System.currentTimeMillis();
			java.sql.ResultSet r;
			for(int i = 0; i < geneProduct.codes.size(); i++) {
				r = s.executeQuery(
						"SELECT id, code, idSample, data FROM expression " +
						"WHERE id = '" + geneProduct.ids.get(i) + "' AND " +
						"code = '" + geneProduct.codes.get(i) + "'");
				HashMap<Integer, Sample> samples = GmmlGex.getSamples();
				while(r.next()) {
					int idSample = r.getInt("idSample");
					String idc = r.getString("id") + "|" + r.getString("code");
					Object dobj = r.getObject("data"); //The actual data
					Sample smp = samples.get(idSample);//The sample
					
					if(data.containsKey(smp)) {
						data.get(smp).put(idc, dobj);
					} else {
						HashMap<String, Object> hm = new HashMap<String, Object>();
						hm.put(idc, dobj);
						data.put(smp, hm);
					}
				}
			}
			System.out.println("GeneProductData queries took: " + (System.currentTimeMillis() - t));
		}
		
		REXP getREXP(Rengine re) throws RException {
			geneProduct.toR(re, "tmpGp");
			
			//Assign data.frame with data
			assignData(re, "tmpData");
			
			rexp = RCommands.evalE(re, 
					"GeneProductData(geneProduct = tmpGp, data = tmpData)");
			
			RCommands.rm(re, "tmpGp");
			
			return rexp;
		}
		
		void assignData(Rengine re, String symbol) throws RException {
			RCommands.evalEN(re, symbol + "= list()");
			
			for(Sample smp : data.keySet()) {
				HashMap<String, Object> d = data.get(smp);
				
				int type = GmmlGex.getSamples().get(smp.idSample).getDataType();
				
				//Store rownames
				String[] idcs = new String[d.size()];
				int i = 0;
				for(String idc : d.keySet()) { idcs[i++] = idc;}
				re.assign("tmpRn", idcs);
								
				if(type == Types.REAL) {
					double[] dd = new double[d.size()];
					i = 0;
					for(String idc : d.keySet()) dd[i++] = Double.parseDouble((String)d.get(idc));
					re.assign("tmpV", dd);
				} else {
					String[] sd = new String[d.size()];
					i = 0;
					for(String idc : d.keySet()) sd[i++] = (String)d.get(idc);
					re.assign("tmpV", sd);
				}
				
				RCommands.evalEN(re, 
						"cbind(tmpV); " +
						"rownames(tmpV) = tmpRn");
				RCommands.evalEN(re, "tmpL = list(tmpV)");
				RCommands.evalEN(re, "names(tmpL) = '" + smp.getName() + "'");
				RCommands.evalEN(re, symbol + "= append(" + symbol + ", tmpL)");
			}
			RCommands.rm(re, new String[] {"tmpRn", "tmpL", "tmpV"});
		}
	}
	
	class DataSet extends RObject {
		String name;
		List<GeneProductData> data;
		
		DataSet(String name) throws Exception {
			this.name = name;
			data = new ArrayList<GeneProductData>();
			List<GeneProduct> geneProducts = new ArrayList<GeneProduct>();
			
			//Get the data from GmmlGex
			Statement s = GmmlGex.getCon().createStatement();
			
			//Create distinct gene products
			long t = System.currentTimeMillis();
			java.sql.ResultSet r = s.executeQuery("SELECT DISTINCT id, code FROM expression");
			System.out.println("DataSet query took: " + (System.currentTimeMillis() - t));
			
			while(r.next()) {
				RCommands.checkCancelled();
				String id = r.getString("id");
				String code = r.getString("code");
				geneProducts.add(new GeneProduct(id, code));
			}

			//Set GeneProductData for every GeneProduct
			for(GeneProduct gp : geneProducts) {
				RCommands.checkCancelled();
				data.add(new GeneProductData(gp));
			}
		}

		REXP getREXP(Rengine re) throws RException {
			RCommands.assign(re, "tmpData", data);
			rexp = RCommands.evalE(re, "DataSet(name = '" + name + "', data = tmpData)");
			
			RCommands.rm(re, "tmpData");
			return rexp;
		}
	}
	class ResultSet extends RObject {

		REXP getREXP(Rengine re) throws RException {
			return null;
		}
	}
}
