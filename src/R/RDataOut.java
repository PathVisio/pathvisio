package R;

import gmmlVision.GmmlVision;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
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
import colorSet.Criterion;
import data.GmmlGdb;
import data.GmmlGex;
import data.GmmlGdb.IdCodePair;
import data.GmmlGex.Sample;
import debug.StopWatch;

public class RDataOut {
	List<File> pwFiles;
	
	boolean exportPws = true;			//Export pathways or not
	boolean exportData = true;			//Export data or not
	
	File pwDir = new File(
		GmmlVision.getPreferences().getString("directories.gmmlFiles"));	//Pathway directory to import
	String exportFile = "temp.Rd";		//File name to export RData
	String pwsName = "myPathways";		//Name of pathwayset object
	String dsName = "myData";			//Name of dataset object
 	
	static int totalWorkData = Integer.MAX_VALUE;
	static int totalWorkPws = Integer.MAX_VALUE;
	
	DataSet cacheDataSet;
	PathwaySet cachePathwaySet;
	
	public RDataOut() {
		pwFiles = new ArrayList<File>();
	}
	
	public void setPathwayDir(File dir) 		{ pwDir = dir; }
	public File getPathwayDir()					{ return pwDir; }
	public void setExportFile(String fn) 		{ exportFile = fn; }
	public String getExportFile()				{ return exportFile; }
	public void setPathwaySetName(String pwn) 	{ pwsName = pwn; }
	public String getPathwaySetName()			{ return pwsName; }
	public void setDataSetName(String dsn) 		{ dsName = dsn; }
	public String getDataSetName()				{ return dsName; }
	
	/**
	 * Create a new RData instance containing the given pathway(s) and expression data (if loaded).
	 * Extra cross-references for GeneProducts will
	 * be stored when neccessary to match the expression data with pathways.
	 * @param pathways	The pathway or directory to include.
	 * @param recursive Whether to include subdirectories or not (ignored if argument 'pathways' points
	 * to a single file)
	 */
	public RDataOut(File pathways, boolean recursive) {
		this();
		//Get the pathway files
		pwFiles = FileUtils.getFiles(pathways, GmmlVision.PATHWAY_FILE_EXTENSION, recursive);
	}
	
	public List<File> getPathwayFiles() { return pwFiles; }
	
	public void checkValid() throws Exception {
		if(exportPws) {
			if(exportFile.equals("")) throw new Exception("specify file to export to");
			if(!pwDir.canRead()) throw new Exception("invalid pathway directory: " + this.pwDir);
			if(pwsName.equals("")) throw new Exception("No name specified for the exported pathwayset object");
		}
		if(exportData) {
			if(dsName.equals("")) throw new Exception("No name specified for the exported dataset object");
		}
	}
	
	public void doExport() throws RException, InvocationTargetException, InterruptedException {
		Rengine re = RController.getR();
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(GmmlVision.getWindow().getShell());
		SimpleRunnableWithProgress rwp = null;
		try {
			if(exportData) {
				rwp = new SimpleRunnableWithProgress(
						this.getClass(), "doExportData", 
						new Class[] { re.getClass() }, new Object[] { re }, this);
				SimpleRunnableWithProgress.setMonitorInfo(
						"Exporting data (task 1/2)", totalWorkData);
				dialog.run(true, true, rwp); 
			}
			if(exportPws) {
				rwp = new SimpleRunnableWithProgress(
						this.getClass(), "doExportPws", 
						new Class[] { re.getClass() }, new Object[] { re }, this);
				SimpleRunnableWithProgress.setMonitorInfo(
						"Exporting pathways (task 2/2)", totalWorkPws);
				dialog.run(true, true, rwp);
			}
			RCommands.eval("save(list = c('" + dsName + "', '" + pwsName + "'), file='"+ RCommands.fileNameToString(exportFile) + "')");
		} catch(InvocationTargetException ex) {
			RTemp.flush(true); //Clear temp variables
			RCommands.eval("save.image(file='"+ RCommands.fileNameToString(exportFile) + ".EX.RData')"); //Save datafile (to check what went wrong)
			RCommands.eval("rm(list=ls())"); //Remove everything from R workspace
			throw ex; //pay it forward!
		}
		
		// Free up memory (especially cacheDataSet is big)
		cacheDataSet = null;
		cachePathwaySet = null; 
	}

	public void doExportPws(Rengine re) throws Exception {
		double contribXml = 0.2;
		PathwaySet.contribGdb = 0.7;
		double contribR = 0.1;
			
		checkValid();
		
		pwFiles = FileUtils.getFiles(pwDir, GmmlVision.PATHWAY_FILE_EXTENSION, true);

		//Calculate contribution of single Pathway
		Pathway.progressContribution = (int)((double)totalWorkPws * contribR / pwFiles.size());
		int pwContribXml = (int)((double)totalWorkPws * contribXml / pwFiles.size());
		
		cachePathwaySet = new PathwaySet(pwsName);
		
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		for(File f : pwFiles) {
			RCommands.checkCancelled();

			PathwayParser p = new PathwayParser(xmlReader);
			try { xmlReader.parse(f.getAbsolutePath()); } catch(Exception e) { 
				GmmlVision.log.error("Couldn't read " + f, e); 
				continue; 
			}
			
			Pathway pw = new Pathway(p.getName(), RCommands.fileToString(f), cachePathwaySet);

			for(Gene g : p.getGenes()) {
				String id = g.getId();
				String code = g.getCode();
				if(id.length() == 0 && code.length() == 0) continue; //Skip empty fields
				pw.addGeneProduct(new IdCodePair(g.getId(), g.getCode()));
			}
			
			//Update progress
			SimpleRunnableWithProgress.monitorWorked(pwContribXml);
		}

		if(exportData && cacheDataSet != null) 
			cachePathwaySet.addCrossRefs(cacheDataSet);
		
		cachePathwaySet.toR(pwsName);
	
		RTemp.flush(true);
	}
	
	public void doExportData(Rengine re) throws Exception {
		checkValid();
		
		cacheDataSet = new DataSet(dsName);
		
		cacheDataSet.toR(dsName);
		
		RTemp.flush(true);
	}
	
	public static void createSetVector(Criterion c, String dsName, String setName) 
	throws RException {
		Rengine re = RController.getR();
		
		int nrow = RCommands.dim(dsName)[0];
		String[] colnames = RCommands.colnames(dsName);
				
		//evaluate row by row
		boolean[] set = new boolean[nrow];
		for(int i = 0; i < nrow; i++) {
			REXP rexp = RCommands.eval(dsName + "[" + (i + 1) + ",]", true);
			RVector values = rexp.asVector();
			double[] dvalues = new double[values.size()];
			for(int j = 0; j < dvalues.length; j++) 
				dvalues[j] = values.at(j).asDouble();
			try {
				set[i] = c.evaluate(colnames, dvalues);
			} catch(Exception e) {
				set[i] = false;
			}
		}
		
		re.assign(setName, set);
	}
	
	static abstract class RObject {	
		static final RException EX_NO_GDB = 
			new RException(null, "No gene database loaded!");
		static final RException EX_NO_GEX = 
			new RException(null, "No expression dataset selected!");
		
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
		
		protected long returnRef(long xp, String tmp) throws RException {
			return returnRef(xp, new String[] { tmp });
		}
		
		long returnRef(long xp, String[] tmp) throws RException {			
			//Check for error in reference
			if(xp == 0) throw new RniException(RController.getR(), RniException.CAUSE_XP_ZERO);
			
			RTemp.dispose(tmp);
			return xp;
		}
	}
	
	static class PathwaySet extends RObject {
		static double contribGdb;
		String name;
		List<Pathway> pathways;	
		
		HashMap<IdCodePair, GeneProduct> geneProducts;
		
		PathwaySet(String name) {
			this.name = name;
			pathways = new ArrayList<Pathway>();
			geneProducts = new HashMap<IdCodePair, GeneProduct>();
		}
		
		void addPathway(Pathway pw) { pathways.add(pw); }
		
		long getRef() throws RException {				
			String tmpVar = RTemp.getNewVar(true);
			
			RCommands.assign(tmpVar, pathways);
	
			String cmd = "PathwaySet(name = '" + name + "', pathways = " + tmpVar + ")";		
			long xp = RCommands.eval(cmd).xp;
			
			return returnRef(xp, tmpVar);
		}
		
		GeneProduct getUniqueGeneProduct(IdCodePair idc) {
			GeneProduct gp = geneProducts.get(idc);
			if(gp == null) {
				gp = new GeneProduct(idc);
				geneProducts.put(idc, gp);
			}
			return gp;
		}
		
		void addCrossRefs(DataSet dataSet) throws Exception {
			int worked = (int)((double)(totalWorkPws * contribGdb) / geneProducts.size());
			
			StopWatch sw = new StopWatch();
			sw.start();
			HashMap repHash = dataSet.getReporterHash();
			sw.stopToLog("creating reporter hashmap");
			
			sw.start();
			StopWatch sw2 = new StopWatch();
			for(IdCodePair pwidc : geneProducts.keySet()) {
				sw2.start();
				RCommands.checkCancelled();
				
				System.out.println("Processing " + pwidc);
				List<IdCodePair> pwrefs = GmmlGdb.getCrossRefs(pwidc);
				sw2.stopToLog("\tfetching crossrefs for " + pwidc);
				System.out.println("Crossrefs: " + pwrefs);
				sw2.start();
				for(IdCodePair ref : pwrefs) {
					if(repHash.containsKey(ref)) {
						geneProducts.get(pwidc).addReference(ref);
						System.out.println("adding " + ref + " to " + geneProducts.get(pwidc));
					}
				}
				sw2.stopToLog("\t\tmatching with reporters");
				SimpleRunnableWithProgress.monitorWorked(worked);
			}
			sw.stopToLog("Fetching crossrefs");
			
			System.out.println("Finished processing " + geneProducts.size() + " geneproducts");
		}
}
	
	static class Pathway extends RObject {
		static int progressContribution;
		
		String name;
		String fileName;
		List<GeneProduct> geneProducts;
		PathwaySet pws;
		
		Pathway(String name, String fileName, PathwaySet pathwaySet) {
			this.name = name;
			this.fileName = fileName;
			this.pws = pathwaySet;
			geneProducts = new ArrayList<GeneProduct>();
			
			pws.addPathway(this);
		}
		
		void addGeneProduct(IdCodePair ref) {
			geneProducts.add(pws.getUniqueGeneProduct(ref));
		}
		
		long getRef() throws RException {			
			String tmpVar = RTemp.getNewVar(true);
			RCommands.assign(tmpVar, geneProducts);
						
			String cmd = "Pathway(name = '" + name + 
				"',fileName = '" + fileName + 
				"', geneProducts = " + tmpVar + ")";			
			long xp =  RCommands.eval(cmd).xp;
			
			SimpleRunnableWithProgress.monitorWorked(progressContribution);
			return returnRef(xp, tmpVar);
		}
	}
	
	static class GeneProduct extends RObject {		
		List<IdCodePair> refs;
		
		GeneProduct() {
			refs = new ArrayList<IdCodePair>();
		}
		
		GeneProduct(String id, String code) {
			this();
			addReference(id, code);
		}
		
		GeneProduct(IdCodePair idc) {
			this();
			addReference(idc);
		}
		
		void addReference(IdCodePair idc) {
			if(!refs.contains(idc)) refs.add(idc);
		}
		
		void addReference(String id, String code) {
			IdCodePair idc = new IdCodePair(id, code);
			addReference(idc);
		}
		
		String[] getRowNames() {
			String[] rowNames = new String[refs.size()];
			int i = 0;
			for(IdCodePair ref : refs) rowNames[i++] = ref.getCode() + ":" + ref.getId();
			return rowNames;
		}
		
		long getRef() throws RException {
			Rengine re = RController.getR();
			String[] ar = new String[refs.size() * 2];
			int i = 0;
			for(IdCodePair ref : refs) {
				ar[i] = ref.getId(); //id
				ar[i++ + refs.size()] = ref.getCode(); //code
			}
			
			long ref_gp = re.rniPutStringArray(ar);
			re.rniProtect(ref_gp);
			re.rniSetAttr(ref_gp, "dim", re.rniPutIntArray(new int[] { refs.size(), 2 }));
			RCommands.setDimNames(ref_gp, getRowNames(), new String[] { "id", "code" });
			re.rniSetAttr(ref_gp, "class", re.rniPutString("GeneProduct"));
            re.rniUnprotect(1);
            
            return ref_gp;            
		}
		
		public void merge(GeneProduct gp) {
			for(IdCodePair ref : gp.refs) addReference(ref);
		}
		
		public String toString() {
			return "GeneProduct: " + refs;
		}
		
		public String getName() {
			return refs.size() == 0 ? "no reference" : refs.get(0).toString();
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof GeneProduct)) return false;
			GeneProduct gp = (GeneProduct)o;
			for(IdCodePair ref : refs) 
				for(IdCodePair oref : gp.refs) 
					if(ref.equals(oref)) return true;
			return false;
		}
	}
		
	static class DataSet extends RObject {
		String[][] data; //[samples][reporters] (transposed for export convenience)
		IdCodePair [] reporters;
		HashMap<Integer, Integer> sample2Col;
		int[] col2Sample;
		
		String name;
		
		DataSet(String name) throws Exception {		
			this.name = name;
			queryData(); //Get the data from the gex database
		}
		
		HashMap<IdCodePair, IdCodePair> getReporterHash() {
			HashMap<IdCodePair, IdCodePair> repHash = new HashMap<IdCodePair, IdCodePair>();
			for(IdCodePair rep : reporters) {
				repHash.put(rep, rep);
			}
			return repHash;
		}
		
		List<String> getCodes() throws Exception {
			List<String> codes = new ArrayList<String>();
			ResultSet r = GmmlGex.getCon().createStatement().executeQuery(
					"SELECT DISTINCT code FROM expression");
			while(r.next()) codes.add(r.getString("code"));
			return codes;
		}
		
		long getRef() throws RException {
			Rengine re = RController.getR();
			
			if(data == null || data.length == 0)
				throw new RException(re, "No exportable data found in expression dataset");
			
			//Create the mixed-type data matrix in R
			//#1 create a long 1-dimensional list -> fill rows first
			//#2 set dims attribute to c(nrow, ncol)
			//et voila, we have a matrix
			HashMap<Integer, Sample> samples = GmmlGex.getSamples();
			long l_ref = re.rniInitVector(data.length * data[0].length);
			re.rniProtect(l_ref);
			
			for(int i = 0; i < data.length; i++) { //Rows (samples)
				int sid = col2Sample[i];
				int type = samples.get(sid).getDataType();
				
				for(int j = 0; j < data[i].length; j++) { //Columns (reporters)
					long e_ref;
					if(type == Types.REAL) {
						double[] value = new double[1];
						try {
							value[0] = Double.parseDouble(data[i][j]);
						} catch(Exception e) {
							GmmlVision.log.error("Unable to parse double when converting data to R: " + data[i][j], e);
						}
						e_ref = re.rniPutDoubleArray(value);
					} else {
						e_ref = re.rniPutString(data[i][j]);
					}
					re.rniVectorSetElement(e_ref, l_ref, i*data[i].length + j);
				}
			}
			
			
			//Set dimensions
			long d_ref = re.rniPutIntArray(new int[] { data[0].length, data.length });
			re.rniSetAttr(l_ref, "dim", d_ref);
			
			//Set rownames (reporters) and colnames (samples)
			String[] rep_names = new String[reporters.length];
			String[] smp_names = new String[data.length];
			
			for(int k = 0; k < reporters.length; k++)
				rep_names[k] = reporters[k].getName();
			
			for(int k = 0; k < data.length; k++) 
				smp_names[k] = samples.get(col2Sample[k]).getName();
			
			RCommands.setDimNames(l_ref, rep_names, smp_names);
					
			//Assign data matrix
			String tmpData = RTemp.getNewVar();
			re.rniAssign(tmpData, l_ref, 0);
			
			//Assign new dataset
			long xp = RCommands.eval("DataSet(data = " + tmpData + ", name = '" + name + "')").xp;
			
			re.rniUnprotect(1);
			return returnRef(xp, tmpData);
		}
		
		void queryData() throws Exception {			
			//Get the 'groups'
			Statement s = GmmlGex.getCon().createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			
			ResultSet r = s.executeQuery("SELECT DISTINCT groupId FROM expression");

			//Set the proper dimensions for the data matrix
			//Rows:
			r.last();
			int nrow = r.getRow();
			r.beforeFirst(); //Set the cursor back to the start
			//Columns:
			int ncol = GmmlGex.getSamples().size();
			
			data = new String[ncol][nrow];
			reporters = new IdCodePair[nrow];
			sample2Col = new HashMap<Integer, Integer>();
			int col = 0;
			col2Sample = new int[ncol];
			for(int sid : GmmlGex.getSamples().keySet()) {
				col2Sample[col] = sid;
				sample2Col.put(sid, col++);
			}
			
			//Calculate the progress contribution of a single group
			int progressContribution = (int)((double)totalWorkData / nrow);
						
			//Fill data matrix for every 'group'
			PreparedStatement pst_dta = GmmlGex.getCon().prepareStatement(
					"SELECT idSample, data FROM expression WHERE groupId = ?");
			PreparedStatement pst_rep = GmmlGex.getCon().prepareStatement(
					"SELECT DISTINCT id, code FROM expression WHERE groupid = ?");
			int i = -1;
			while(r.next()) {
				RCommands.checkCancelled();
				
				int group = r.getInt(1);
				pst_dta.setInt(1, group);
				pst_rep.setInt(1, group);
				
				ResultSet r1 = pst_rep.executeQuery();
				if(r1.next()) 
					reporters[++i] = new IdCodePair(r1.getString("id"), r1.getString("code"));
				r1 = pst_dta.executeQuery();
				while(r1.next()) {
					int sid = r1.getInt("idSample");
					String dta = r1.getString("data");
					data[sample2Col.get(sid)][i] = dta;
				}
				
				SimpleRunnableWithProgress.monitorWorked(progressContribution);
			}
		}	
	}
}
