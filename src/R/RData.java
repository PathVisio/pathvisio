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
import org.rosuda.JRI.Rengine;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import util.FileUtils;
import util.SwtUtils.SimpleRunnableWithProgress;
import util.XmlUtils.PathwayParser;
import util.XmlUtils.PathwayParser.Gene;
import R.RCommands.RException;
import R.RCommands.RTemp;
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
						
			RCommands.evalEN("save.image(file='"+ exportFile + "')");
			
		} catch(InvocationTargetException ex) {
			rwp.openMessageDialog("Error", "Unable to export data: " + ex.getCause().getMessage());
			GmmlVision.log.error("Unable to export to R", ex);
			return;
		}
	}

	public void doExportPws(Rengine re) throws Exception {
		long t = System.currentTimeMillis();
		
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
		
		RTemp.flush(true);
		
		System.out.println("Exporting " + pwFiles.size() + " pathways took " + (System.currentTimeMillis() - t) + " ms");
	}
	
	public void doExportData(Rengine re) throws Exception {
		if(!GmmlGex.isConnected()) throw new Exception("No expression data loaded");
		DataSet ds = new DataSet(dsName);
		ds.toR(re, dsName);
		
		RTemp.flush(true);
	}
			
	abstract class RObject {		
		abstract void toR(Rengine re, String symbol) throws RException;
		
		String toRTemp(Rengine re) throws RException { return toRTemp(re, false); }
		
		String toRTemp(Rengine re, boolean protect) throws RException {
			String tmpVar = RTemp.getNewVar(protect);
			toR(re, tmpVar);			
			return tmpVar;
		}
	}
	
	class PathwaySet extends RObject {
		String name;
		List<Pathway> pathways;
		
		PathwaySet(String name, List<Pathway> pathways) {
			this.name = name;
			this.pathways = pathways;
		}
		
		void toR(Rengine re, String symbol) throws RException {
			String tmpVar = RTemp.getNewVar(true);
			RCommands.assign(re, tmpVar, pathways);
			
			String cmd = symbol + "= PathwaySet('" + name + "', " + tmpVar + ")";
			RCommands.evalEN(cmd);
			
			RTemp.unprotect(tmpVar);
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
		
		void toR(Rengine re, String symbol) throws RException {	
			String tmpVar = RTemp.getNewVar(true);
			RCommands.assign(re, tmpVar, geneProducts);
						
			String cmd = symbol + "= Pathway('" + name + "','" + fileName + "', " + tmpVar + ")";
			RCommands.evalEN(cmd);
			
			RTemp.unprotect(tmpVar);
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
		
		void toR(Rengine re, String symbol) throws RException {	
			String tmpIds = RTemp.getNewVar(false);
			RCommands.assign(re, tmpIds, ids.toArray(new String[ids.size()]));
			String tmpCodes = RTemp.getNewVar(false);
			RCommands.assign(re, tmpCodes, codes.toArray(new String[codes.size()]));

			String cmd = symbol + "= GeneProduct("+ tmpIds +", " + tmpCodes + ")";
            RCommands.evalEN(cmd);
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
		
		void toR(Rengine re, String symbol) throws RException {
			String tmpGp = geneProduct.toRTemp(re);
			
			//Assign data.frame with data
			String tmpData = assignData(re);
			
			String cmd = 
				symbol + "= GeneProductData(geneProduct = " + tmpGp + ", data = " + tmpData +")";
			RCommands.evalEN(cmd);
		}
		
		String assignData(Rengine re) throws RException {
			//Protect, just in case there are a lot of samples
			String tmpData = RTemp.assign(re, "list()", true);
			
			for(Sample smp : data.keySet()) {
				HashMap<String, Object> d = data.get(smp);
				
				int type = GmmlGex.getSamples().get(smp.idSample).getDataType();
				
				//Store rownames
				String[] idcs = new String[d.size()];
				int i = 0;
				for(String idc : d.keySet()) { idcs[i++] = idc;}
				
				String tmpRn = RTemp.getNewVar(false);
				RCommands.assign(re, tmpRn, idcs);
						
				String tmpV = RTemp.getNewVar(false);
				if(type == Types.REAL) {
					double[] dd = new double[d.size()];
					i = 0;
					for(String idc : d.keySet()) dd[i++] = Double.parseDouble((String)d.get(idc));
					re.assign(tmpV, dd);
				} else {
					String[] sd = new String[d.size()];
					i = 0;
					for(String idc : d.keySet()) sd[i++] = (String)d.get(idc);
					re.assign(tmpV, sd);
				}
				
				RCommands.evalEN( 
						"cbind(" + tmpV +"); " +
						"rownames(" + tmpV + ") = " + tmpRn);
				String tmpL = RTemp.assign(re, "list(" + tmpV + ")", false);
				RCommands.evalEN("names(" + tmpL + ") = '" + smp.getName() + "'");
				RCommands.evalEN(tmpData + "= append(" + tmpData + ", " + tmpL + ")");
			}
			
			return tmpData;
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

		void toR(Rengine re, String symbol) throws RException {
			String tmpData = RTemp.getNewVar(true);
			RCommands.assign(re, tmpData, data);
			
			String cmd = symbol + "= DataSet(name = '" + name + "', data = " + tmpData + ")";
			RCommands.evalE(cmd);
			
			RTemp.unprotect(tmpData);
		}
	}
}
