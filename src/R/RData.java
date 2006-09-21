package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import R.RCommands.RniException;
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
	
	static int totalWorkData = 10000;
	static int totalWorkPws = IProgressMonitor.UNKNOWN;
	
	
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
				SimpleRunnableWithProgress.setMonitorInfo(
						"Exporting data", totalWorkData);
				dialog.run(true, true, rwp); 
			}
			if(exportPws) {
				rwp = new SimpleRunnableWithProgress(
						this.getClass(), "doExportPws", 
						new Class[] { re.getClass() }, new Object[] { re }, this);
				SimpleRunnableWithProgress.setMonitorInfo(
						"Exporting pathways", totalWorkPws);
				dialog.run(true, true, rwp);
			}
						
			RCommands.eval("save.image(file='"+ exportFile + "')");
			
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
		testPathwaySet.toR(pwsName); 
		
		RTemp.flush(true);
		
		System.out.println("Exporting " + pwFiles.size() + " pathways took " + (System.currentTimeMillis() - t) + " ms");
	}
	
	public void doExportData(Rengine re) throws Exception {
		if(!GmmlGex.isConnected()) throw new Exception("No expression data loaded");
		DataSet ds = new DataSet(dsName);
		ds.toR(dsName);
		
		RTemp.flush(true);
	}
			
	static abstract class RObject {		
		void toR(String symbol) throws RException {
			Rengine re = RController.getR();
			long xp = getRef();
			re.rniAssign(symbol, xp, 0);
		}
		
		String toRTemp() throws RException { return toRTemp(true); }
		
		String toRTemp(boolean protect) throws RException {
			String tmpVar = RTemp.getNewVar(protect);
			toR(tmpVar);		
			return tmpVar;
		}
		
		abstract long getRef() throws RException;
	}
	
	static class PathwaySet extends RObject {
		String name;
		List<Pathway> pathways;
		
		PathwaySet(String name, List<Pathway> pathways) {
			this.name = name;
			this.pathways = pathways;
		}
				
		long getRef() throws RException {			
			String tmpVar = RTemp.getNewVar(true);
			
			RCommands.assign(tmpVar, pathways);
	
			String cmd = "PathwaySet('" + name + "', " + tmpVar + ")";		
			long xp = RCommands.eval(cmd).xp;
			
			RTemp.dispose(tmpVar);
			return xp;
		}
}
	
	static class Pathway extends RObject {
		String name;
		String fileName;
		List<GeneProduct> geneProducts;
		
		Pathway(String name, String fileName, List<GeneProduct> geneProducts) {
			this.name = name;
			this.fileName = fileName;
			this.geneProducts = geneProducts;
		}
		
		void addGeneProduct(GeneProduct gp) { geneProducts.add(gp); }
		
		long getRef() throws RException {
			String tmpVar = RTemp.getNewVar(true);
			RCommands.assign(tmpVar, geneProducts);
						
			String cmd = "Pathway('" + name + "','" + fileName + "', " + tmpVar + ")";			
			long xp =  RCommands.eval(cmd).xp;
			
			RTemp.dispose(tmpVar);
			return xp;
		}
	}
	
	static class GeneProduct extends RObject {
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
		
		long getRef() throws RException {
			System.err.println(ids + "\n" + codes);
			String tmpIds = RTemp.getNewVar(true);
			RCommands.assign(tmpIds, ids.toArray(new String[ids.size()]));
			String tmpCodes = RTemp.getNewVar(true);
			RCommands.assign(tmpCodes, codes.toArray(new String[codes.size()]));

			String cmd = "GeneProduct("+ tmpIds +", " + tmpCodes + ")";
            long xp = RCommands.eval(cmd).xp;
            
            RTemp.dispose(new String[] { tmpIds, tmpCodes});
            return xp;
            
		}
		
		public String toString() {
			return "GeneProduct: " + ids + ", " + codes;
		}
	}
	
	static class GeneProductData extends RObject {
		static int progressContribution;
		
		GeneProduct geneProduct;
		
		//We would like to cache the data, but often the datasets
		//are too large and cause 'out of heapspace' error...
		//So make only local references to the data and let the garbage collector do its work
//		HashMap<Sample, HashMap<String, Object>> data;
//		HashMap<Sample, HashMap<String, Integer>> sets;
		
		GeneProductData(GeneProduct geneProduct) throws Exception {
			this.geneProduct = geneProduct;
		}
		
		/**
		 * Queries the data for this objecs's {@link GeneProduct} from {@link GmmlGex}
		 */
		HashMap<Sample, HashMap<String, Object>> queryData() throws SQLException {
			HashMap<Sample, HashMap<String, Object>> data =
				new HashMap<Sample, HashMap<String, Object>>();
			
			Statement s = GmmlGex.getCon().createStatement();

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
					Object dobj = r.getString("data"); //The actual data
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
			System.err.println("GeneProductData queries took: " + (System.currentTimeMillis() - t));
			return data;
		}
		
		long getRef() throws RException {			
			String tmpGp = geneProduct.toRTemp(true);

			//Assign list with data
			String tmpData = tmpData();
			
			System.err.println(geneProduct.ids);
			System.err.println(geneProduct.codes);
			
			String cmd = 
				"GeneProductData(geneProduct = " + tmpGp + ", data = " + tmpData +")";
			long xp = RCommands.eval(cmd).xp;
			
			RTemp.dispose(new String[] { tmpGp, tmpData });
			
			//Update progress
			SimpleRunnableWithProgress.updateMonitor(progressContribution);
			
			return xp;
		}
		
		String tmpData() throws RException {
			Rengine re = RController.getR();
			
			//Query data from database
			HashMap<Sample, HashMap<String, Object>> data = null;
			try { 
				data = queryData();
			} catch(SQLException e ) { 
				throw new RException(null, "SQLException: "  + e.getMessage());
			}
			
			//Tmp vars
			String tmpData = RTemp.getNewVar(), tmpV = RTemp.getNewVar(), 
			tmpRn =  RTemp.getNewVar();
						
			List<Sample> smps = new ArrayList<Sample>(data.keySet());
			long[] refs = new long[smps.size()];
			String[] listNames = new String[smps.size()];
			
			for(int j = 0; j < smps.size(); j++) {
				Sample smp = smps.get(j);
				HashMap<String, Object> d = data.get(smp);
				
				int type = GmmlGex.getSamples().get(smp.idSample).getDataType();
				
				//Store rownames
				String[] idcs = new String[d.size()];
				int i = 0;
				for(String idc : d.keySet()) { idcs[i++] = idc;}
				RCommands.assign(tmpRn, idcs);
						
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
		
				RCommands.eval( 
						"cbind(" + tmpV +"); " +				//matrix out of vector
						"rownames(" + tmpV + ") = " + tmpRn);	//set rownames
				long xp = RCommands.eval(tmpV).xp;
				if(xp == 0) 
					throw new RniException(re, "GeneProductData#tmpData()", "reference zero", xp);
				re.rniProtect(xp);
				refs[j] = xp;
				listNames[j] = smp.getName();
			}
						
			long xp = re.rniPutVector(refs);
			long xp_nms = re.rniPutStringArray(listNames);
			re.rniSetAttr(xp, "names", xp_nms);
			
			re.rniAssign(tmpData, xp, 0);
			re.rniUnprotect(refs.length);
			
			RTemp.dispose(new String[] { tmpV, tmpRn,  } );
			
			return tmpData;
		}
	}
	
	static class DataSet extends RObject {
		int nrRows;
		
		String name;
		List<GeneProductData> data;
		
		DataSet(String name) throws Exception {
			this.name = name;
			data = new ArrayList<GeneProductData>();
			
			//Get the data from GmmlGex
			Statement s = GmmlGex.getCon().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
						
			//Create distinct gene products
			long t = System.currentTimeMillis();
			java.sql.ResultSet r = s.executeQuery("SELECT DISTINCT id, code FROM expression");
			System.out.println("DataSet query took: " + (System.currentTimeMillis() - t));
			
			//Get the size of the dataset
			r.last();
			nrRows = r.getRow();
			r.beforeFirst(); //Set the cursor back to the start
			
			//Calculate the progress contribution of a single GeneProductData
			GeneProductData.progressContribution = totalWorkData / nrRows;
			
			//Set GeneProductData for every GeneProduct
			while(r.next()) {
				RCommands.checkCancelled();
				String id = r.getString("id");
				String code = r.getString("code");
				data.add(new GeneProductData(new GeneProduct(id, code)));
			}
		}

		long getRef() throws RException {
			String tmpData = RTemp.getNewVar();
			RCommands.assign(tmpData, data);
			
			String cmd = "DataSet(name = '" + name + "', data = " + tmpData + ")";
			long xp = RCommands.eval(cmd).xp;
			
			RTemp.dispose(tmpData);

			return xp;
		}
	}
}
