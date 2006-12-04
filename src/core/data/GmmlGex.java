// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package data;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVision.ApplicationEvent;
import gmmlVision.GmmlVision.ApplicationEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import util.FileUtils;
import visualization.VisualizationManager;
import visualization.colorset.ColorSetManager;
import data.GmmlGdb.IdCodePair;
import data.GmmlGex.CachedData.Data;
import data.ImportExprDataWizard.ImportInformation;
import data.ImportExprDataWizard.ImportPage;
import debug.StopWatch;

/**
 * This class handles everything related to the Expression Data. It contains the database connection,
 * several methods to query data and write data and methods to convert a GenMAPP Expression Dataset
 * to hsqldb format
 */
public class GmmlGex implements ApplicationEventListener {
	public static final String XML_ELEMENT = "expression-data-visualizations";
	static final int COMPAT_VERSION = 1;
	
	private static Connection con;
	/**
	 * Get the {@link Connection} to the Expression-data database
	 * @return
	 */
	public static Connection getCon() { return con; }
	/**
	 * Check whether a connection to the database exists
	 * @return	true is a connection exists, false if not
	 */
	public static boolean isConnected() { return con != null; }
	
	private static String dbName;
	/**
	 * Get the database name of the expression data currently loaded
	 */
	public static String getDbName() { return dbName; }
	
	/**
	 * Set the database name of the expression data currently loaded
	 * (Connection is not reset)
	 */
	public static void setDbName(String name) { dbName = name; }
				
	public static InputStream getXmlInput() {
		File xmlFile = new File(dbName + ".xml");
		try {
			if(!xmlFile.exists()) xmlFile.createNewFile();
			InputStream in = new FileInputStream(xmlFile);
			return in;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static OutputStream getXmlOutput() {
		try {
			File f = new File(dbName + ".xml");
			OutputStream out = new FileOutputStream(f);
			return out;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void saveXML() {
		if(!isConnected()) return;
		
		OutputStream out = getXmlOutput();
		
		Document xmlDoc = new Document();
		Element root = new Element(XML_ELEMENT);
		xmlDoc.setRootElement(root);
		
		root.addContent(VisualizationManager.getNonGenericXML());
		root.addContent(ColorSetManager.getXML());
		
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		try {
			xmlOut.output(xmlDoc, out);
			out.close();
		} catch(IOException e) {
			GmmlVision.log.error("Unable to save visualization settings", e);
		}
	}
	
	public static void loadXML() {
		Document doc = getXML();
		Element root = doc.getRootElement();
		Element vis = root.getChild(VisualizationManager.XML_ELEMENT);
		VisualizationManager.loadNonGenericXML(vis);
		Element cs = root.getChild(ColorSetManager.XML_ELEMENT);
		ColorSetManager.fromXML(cs);
	}
	
	public static Document getXML() {
		InputStream in = GmmlGex.getXmlInput();
		Document doc;
		Element root;
		try {
			SAXBuilder parser = new SAXBuilder();
			doc = parser.build(in);
			in.close();
			
			root = doc.getRootElement();
		} catch(Exception e) {
			doc = new Document();
			root = new Element(XML_ELEMENT);
			doc.setRootElement(root);
			
		}
		
		return doc;
	}
		
	public static CachedData cachedData;
	public static class CachedData
	{
		private HashMap<IdCodePair, Data> data;		
		public CachedData()
		{
			if(samples == null) setSamples(); //Cache the samples in the dataset
			data = new HashMap<IdCodePair, Data>();
		}
		
		public boolean hasData(IdCodePair idc)
		{
			if(data.containsKey(idc)) return data.get(idc).idcode.equals(idc);
			else return false;
		}
		
		public Data getData(IdCodePair idc)
		{
			Data d = null;
			if(data.containsKey(idc)) { 
				d = data.get(idc);
				if(!d.idcode.equals(idc)) d = null;
			}
			return d;
		}
		
		public void addData(String id, String code, Data mappIdData)
		{
			data.put(new IdCodePair(id, code), mappIdData);
		}
		
		public class Data implements Comparable
		{
			private IdCodePair idcode;
			private HashMap<Integer, Object> sampleData;
			private HashMap<IdCodePair, Data> refData;
			
			public Data(String id, String code) {
				idcode = new IdCodePair(id, code);
				refData = new HashMap<IdCodePair, Data>();
				sampleData = new HashMap<Integer, Object>();
			}
			
			public Data(IdCodePair idcode) {
				this.idcode = idcode;
				sampleData = new HashMap<Integer, Object>();
			}
			
			public boolean hasData() { return refData.size() > 0; }
			
			public void addRefData(String id, String code, int sampleId, String data) 
			{ 
				Data ref = null;
				IdCodePair idcode = new IdCodePair(id, code);
				if(refData.containsKey(idcode)) ref = refData.get(idcode);
				else ref = new Data(idcode);
				
				Object parsedData = null;
				try { parsedData = Double.parseDouble(data); }
				catch(Exception e) { parsedData = data; }
				ref.addSampleData(sampleId, parsedData);
				refData.put(idcode, ref);
			}
			
			public void addSampleData(int sampleId, Object data)
			{
				if(data != null) sampleData.put(sampleId, data);
			}
			
			public List<Data> getRefData()
			{
				List<Data> rd = new ArrayList<Data>(refData.values());
				Collections.sort(rd);
				return rd;
			}
			
			public Object getData(int idSample)
			{
				if(sampleData.containsKey(idSample)) return sampleData.get(idSample);
				return null;
			}
			
			public boolean hasMultipleData()
			{
				return refData.keySet().size() > 1;
			}
			
			public HashMap<Integer, Object> getSampleData() {
				if(sampleData.size() == 0) {
					if(refData.size() > 0) return getAverageSampleData();
				}
				return sampleData;
			}
			
			public HashMap<Integer, Object> getAverageSampleData()
			{
				if(refData.size() == 0) return null;
				HashMap<Integer, Object> averageData = new HashMap<Integer, Object>();
				for(int idSample: samples.keySet())
				{
					int dataType = samples.get(idSample).dataType;
					if(dataType == Types.REAL) {
						averageData.put(idSample, averageDouble(idSample));
					} else {
						averageData.put(idSample, averageString(idSample));
					}
				}
				return averageData;
			}
			
			private Object averageDouble(int idSample)
			{
				double avg = 0;
				int n = 0;
				for(Data d : refData.values()) {
					try { avg += (Double)d.getData(idSample); n++; } catch(Exception e) {}
				}
				if(n > 0) return avg / n;
				return averageString(idSample);
			}
			
			private Object averageString(int idSample)
			{
				StringBuilder sb = new StringBuilder();
				for(Data d : refData.values()) {
					sb.append(d.getData(idSample) + ", ");
				}
				int end = sb.lastIndexOf(", ");
				return end < 0 ? "" : sb.substring(0, end).toString();
			}

			public int compareTo(Object o) {
				Data d = (Data)o;
				return idcode.compareTo(d.idcode);
			}
		}
	}
    
	private static HashMap<Integer, Sample> samples;
	/**
	 * Loads the samples used in the expression data (Sample table) in memory
	 */
	public static void setSamples()
	{
		try {
			ResultSet r = con.createStatement().executeQuery(
					"SELECT idSample, name, dataType FROM samples"
			);
			samples = new HashMap<Integer, Sample>();
			while(r.next())
			{
				int id = r.getInt(1);
				samples.put(id, new Sample(id, r.getString(2), r.getInt(3)));					
			}
		} catch (Exception e) {
			GmmlVision.log.error("while loading data from the 'samples' table: " + e.getMessage(), e);
		}
	}
	
	public static Sample getSample(int id) {
		return getSamples().get(id);
	}
	
	/**
	 * This class represents a record in the Sample table of the Expression database. 
	 */
	public static class Sample implements Comparable<Sample>
	{
		private int idSample;
		private String name;
		private int dataType;
		
		/**
		 * Constructor of this class
		 * @param idSample	represents the 'idSample' column in the Sample table, an unique identifier
		 * for this sample
		 * @param name		represents the 'name' column in the Sample table, the name of the
		 * sample
		 * @param dataType	represents the 'dataType' column in the Sample table, the data type of
		 * the values stored in the column (using the field contsants in {@link java.sql.Types})
		 */
		public Sample(int idSample, String name, int dataType)
		{
			this.idSample = idSample;
			this.name = name;
			this.dataType = dataType;
		}
		
		public String getName() { return name == null ? "" : name; }
		protected void setName(String nm) { name = nm; }
		public int getDataType() { return dataType; }
		protected void setDataType(int type) { dataType = type; }
		public int getId() { return idSample; }
		protected void setId(int id) { idSample = id; }
		/**
		 * Compares this object to another {@link Sample} object based on the idSample property
		 * @param o	The {@link Sample} object to compare with
		 * @return	integer that is zero if the objects are equal, negative if this object has a
		 * lower idSample, positive if this object has a higher idSample
		 * @throws ClassCastException
		 */
		public int compareTo(Sample o)
		{
			return idSample - o.idSample;
		}
		
		public int hashCode() {
			return idSample;
		}
		
		public boolean equals(Object o) {
			if(o instanceof Sample) return ((Sample) o).idSample == idSample;
			return false;
		}
		
		/**
		 * Returns a readable String representation of this object
		 */
		public String toString()
		{
			return Integer.toString(idSample);
		}
	}
	
	/**
	 * Checks whether Expression data is cached for a given gene product
	 * @param idc	the {@link IdCodePair} containing the id and code of the geneproduct to look for
	 * @return		true if Expression data is found in cache, false if not
	 */
	public static boolean hasData(IdCodePair idc)
	{
		if(cachedData == null) return false;
		return cachedData.hasData(idc);
	}
	
	public static HashMap<Integer, Sample> getSamples()
	{
		if(samples == null) setSamples();
		return samples;
	}
	
	public static List<String> getSampleNames() {
		return getSampleNames(-1);
	}
	
	public static List<String> getSampleNames(int dataType) {
		List<String> names = new ArrayList<String>();
		List<Sample> sorted = new ArrayList<Sample>(samples.values());
		Collections.sort(sorted);
		for(Sample s : sorted) {
			if(dataType == s.dataType || dataType == -1)
				names.add(s.getName());
		}
		return names;
	}
	
	public static List<Sample> getSamples(int dataType) {
		List<Sample> smps = new ArrayList<Sample>();
		List<Sample> sorted = new ArrayList<Sample>(samples.values());
		Collections.sort(sorted);
		for(Sample s : sorted) {
			if(dataType == s.dataType || dataType == -1)
				smps.add(s);
		}
		return smps;
	}
	
	public static Data getCachedData(IdCodePair idc)
	{
		if(cachedData != null) return cachedData.getData(idc);
		return null;
	}
	
	/**
	 * Gets all available expression data for the given gene id and returns a string
	 * containing this data in a HTML table
	 * @param idc	the {@link IdCodePair} containing the id and code of the geneproduct to look for
	 * @return		String containing the expression data in HTML format or a string displaying a
	 * 'no expression data found' message in HTML format
	 */
	public static String getDataString(IdCodePair idc)
	{
		String noDataFound = "<P><I>No expression data found";
		String exprInfo = "<P><B>Gene id on mapp: " + idc.getId() + "</B><TABLE border='1'>";
		
		String colNames = "<TR><TH>Sample name";
		if(		con == null //Need a connection to the expression data
				|| GmmlGdb.getCon() == null //and to the gene database
		) return noDataFound;
		
		Data mappIdData = cachedData.getData(idc);
		if(mappIdData == null) return noDataFound;
		List<Data> refData = mappIdData.getRefData();
		if(refData == null) return noDataFound; //The gene doesn't have data after all
		for(Data d : refData)
		{
			colNames += "<TH>" + d.idcode.getId();
		}
		String dataString = "";
		for(Sample s : getSamples().values())
		{
			dataString += "<TR><TH>" + s.name;
			for(Data d : refData)
			{
				dataString += "<TH>" + d.getData(s.idSample);
			}
		}
		
		return exprInfo + colNames + dataString + "</TABLE>";
	}
	
	/**
	 * Loads expression data for all the given gene ids into memory
	 * @param ids	Gene ids to cache the expression data for
	 * @param code	Systemcodes of the gene identifiers
	 * (typically all genes in a pathway)
	 */
	private static void cacheData(ArrayList<String> ids, ArrayList<String> codes)
	{	
		cachedData = new CachedData();
		StopWatch timer = new StopWatch();
		timer.start();
		
//		PreparedStatement pstData = null;
//		try {
//			pstData = con.prepareStatement(
//					"SELECT id, code, data, idSample FROM expression " +
//			"WHERE ensId = ?");
//		} catch(SQLException e) {
//			GmmlVision.log.error("Unable to prepare statement", e);
//			return;
//		}

		
		for(int i = 0; i < ids.size(); i++)
		{
			String id = ids.get(i);
			String code = codes.get(i);
			
			ArrayList<String> ensIds = GmmlGdb.ref2EnsIds(id, code); //Get all Ensembl genes for this id
			
			if(ensIds.size() > 0) //Only create a RefData object if the id maps to an Ensembl gene
			{
				Data mappGeneData = cachedData.new Data(id, code);
				
				StopWatch tt = new StopWatch();
				StopWatch ts = new StopWatch();
				
				tt.start();
				
				for(String ensId : ensIds)
				{	
					try {
						ts.start();
						
//						pstData.setString(1, ensId);
//						ResultSet r = pstData.executeQuery();
						ResultSet r = con.createStatement().executeQuery(
								"SELECT id, code, data, idSample FROM expression " +
								" WHERE ensId = '" + ensId + "'");
						//r contains all genes and data mapping to the Ensembl id
						while(r.next())
						{
							int idSample = r.getInt("idSample");
							mappGeneData.addRefData(
									r.getString("id"), 
									r.getString("code"),
									idSample,
									r.getString("data"));	
						}
						
						ts.stopToLog("Fetching data for ens id: " + ensId + "\t");
					} catch (Exception e)
					{
						GmmlVision.log.error("while caching expression data: " + e.getMessage(), e);
					}
				}
				if(mappGeneData.hasData()) cachedData.addData(id, code, mappGeneData);
				
				tt.stopToLog(id + ", " + code + ": adding data to cache\t\t");
			}			
			if(cacheThread.isInterrupted) //Check if the process is interrupted
			{
				return;
			}
			cacheThread.progress += 100.0 / ids.size(); //Update the progress
		}
		cacheThread.progress = 100;
		timer.stopToLog("Caching expression data\t\t\t");
		GmmlVision.log.trace("> Nr of ids queried:\t" + ids.size());
	}
	
	/**
	 * {@link CacheThread} to facilitate caching of Expression data of genes in a pathway in a
	 * seperate {@link Thread}
	 */
	private static CacheThread cacheThread;
	/**
	 * This class is a {@link Thread} that is responsible for calling {@link cacheData()}
	 * and keeping the progress of its progress
	 */
	private static class CacheThread extends Thread
	{
		volatile double progress;
		volatile boolean isInterrupted;
		ArrayList<String> ids;
		ArrayList<String> codes;
		/**
		 * Constructor for this class
		 * @param ids	the gene ids that need to be passed on to {@link cacheData()}
		 * @param codes	The systemcodes of the gene identifiers
		 */
		public CacheThread(ArrayList<String> ids, ArrayList<String> codes) 
		{
			this.ids = ids;
			this.codes = codes;
		}
		
		public void run()
		{
			progress = 0;
			isInterrupted = false;
			cacheData(ids, codes);
		}
		
		public void interrupt()
		{
			isInterrupted = true;
		}
	}
	
	/**
	 * Creates an {@link IRunnableWithProgress} responsible for starting the 
	 * {@link CacheThread} and keeping track of the progress of this thread
	 * @param mappIds	the gene ids to pass on to {@link cacheData()}
	 * @param codes		the systemcodes of the gene identifiers
	 * @return
	 */
	public static IRunnableWithProgress createCacheRunnable(
			final ArrayList<String> mappIds, 
			final ArrayList<String> codes )
	{
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Caching expression data",100);
				cacheThread = new CacheThread(mappIds, codes);
				cacheThread.start();
				int prevProgress = 0;
				while(cacheThread.progress < 100) {
					if(monitor.isCanceled()) {
						cacheThread.interrupt();
						break;
					}
					if(prevProgress < (int)cacheThread.progress) {
						monitor.worked((int)cacheThread.progress - prevProgress);
						prevProgress = (int)cacheThread.progress;
					}
				}
				monitor.done();
			}
		};
	}
	
	private static ConvertThread convertThread;
	/**
	 * This class is a {@link Thread} that converts a GenMAPP Expression dataset and keeps the progress
	 * of the conversion
	 */
	private static class ConvertThread extends Thread
	{
		volatile double progress;
		volatile boolean isInterrupted;
		public ConvertThread() 
		{
			isInterrupted = false;
		}
		
		public void run()
		{
			progress = 0;
			convertGex();
		}
		
		public void interrupt()
		{
			isInterrupted = true;
		}
	}
	
	/**
	 * This {@link IRunnableWithProgress} starts the {@link ConvertThread} 
	 * and monitors the progress of the conversion
	 */
	public static IRunnableWithProgress convertRunnable = new IRunnableWithProgress() {
		public void run(IProgressMonitor monitor)
		throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Converting Gene Expression Dataset",100);
			convertThread = new ConvertThread();
			convertThread.start();
			int prevProgress = 0;
			while(convertThread.progress < 100) {
				if(monitor.isCanceled()) {
					convertThread.interrupt();
					break;
				}
				if(prevProgress < (int)convertThread.progress) {
					monitor.worked((int)convertThread.progress - prevProgress);
					prevProgress = (int)convertThread.progress;
				}
			}
			monitor.done();
		}
	};
	
	/**
	 * This {@link IRunnableWithProgress} is responsible for running the import expression data
	 * process and monitor the progress
	 * {@see GmmlGex#importFromTxt(ImportInformation, ImportPage, IProgressMonitor)}
	 */
	public static class ImportRunnableWithProgress implements IRunnableWithProgress {
		static final int totalWork = (int)1E6;
		ImportInformation info;
		ImportPage page;
		
		public ImportRunnableWithProgress(ImportInformation info, ImportPage page) {
			super();
			this.info = info;
			this.page = page;
		}
		
		public void run(IProgressMonitor monitor) 
		throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Importing expression data", totalWork);
			importFromTxt(info, page, monitor);
			monitor.done();
		}
	}
	
	/**
	 * Imports expression data from a text file and saves it to an hsqldb expression database
	 * @param info		{@link ImportExprDataWizard.ImportInformation} object that contains the 
	 * information needed to import the data
	 * @param page		{@link ImportExprDataWizard.ImportPage} that reports information and errors
	 * during the import process
	 * @param monitor	{@link IProgressMonitor} that reports the progress of the process and enables
	 * the user to cancel
	 */
	private static void importFromTxt(ImportInformation info, ImportPage page, IProgressMonitor monitor)
	{
		int importWork = (int)(ImportRunnableWithProgress.totalWork * 0.8);
		int finalizeWork = (int)(ImportRunnableWithProgress.totalWork * 0.2);
		
//		Open a connection to the error file
		String errorFile = info.dbName + ".ex.txt";
		int errors = 0;
		PrintStream error = null;
		try {
			File ef = new File(errorFile);
			ef.getParentFile().mkdirs();
			error = new PrintStream(errorFile);
		} catch(IOException ex) {
			page.println("Error: could not open exception file: " + ex.getMessage());
			error = System.out;
		}
		
		StopWatch timer = new StopWatch();
		try 
		{
			page.println("\nCreating expression dataset");
						
			//Create a new expression database (or overwrite existing)
			dbName = info.dbName;
			connect(true, false);
			
			page.println("Importing data");
			page.println("> Processing headers");
			
			timer.start();
			
			BufferedReader in = new BufferedReader(new FileReader(info.getTxtFile()));
			//Get the number of lines in the file (for progress)
			int nrLines = FileUtils.getNrLines(info.getTxtFile().toString());
			
			String[] headers = info.getColNames();
			//Parse sample names and add to Sample table
			PreparedStatement pstmt = con.prepareStatement(
					" INSERT INTO SAMPLES " +
					"	(idSample, name, dataType)  " +
			" VALUES (?, ?, ?)		  ");
			int sampleId = 0;
			ArrayList<Integer> dataCols = new ArrayList<Integer>();
			for(int i = 0; i < headers.length; i++) {
				if(monitor.isCanceled()) { close(true); error.close(); return; } //User pressed cancel
				if(i != info.idColumn && i != info.codeColumn) { //skip the gene and systemcode column
					try {
						pstmt.setInt(1, sampleId++);
						pstmt.setString(2, headers[i]);
						pstmt.setInt(3, info.isStringCol(i) ? Types.CHAR : Types.REAL);
						pstmt.execute();
						dataCols.add(i);
					} catch(Error e) { 
						errors = reportError(error, "Error in headerline, can't add column " + i + 
								" due to: " + e.getMessage(), errors);
						
					}
				}
			}
			
			page.println("> Processing lines");
			
			//Check ids and add expression data
			for(int i = 1; i < info.firstDataRow; i++) in.readLine(); //Go to line where data starts
			pstmt = con.prepareStatement(
					"INSERT INTO expression			" +
					"	(id, code, ensId,			" + 
					"	 idSample, data, groupId)	" +
			"VALUES	(?, ?, ?, ?, ?, ?)			");
			String line = null;
			int n = info.firstDataRow - 1;
			int added = 0;
			int worked = importWork / nrLines;
			while((line = in.readLine()) != null) 
			{
				if(monitor.isCanceled()) { close(); error.close(); return; } //User pressed cancel
				String[] data = line.split(ImportInformation.DELIMITER, headers.length);
				n++;
				if(n == info.headerRow) continue; //Don't add header row (very unlikely that this will happen)
				if(data.length < headers.length) {
					errors = reportError(error, "Number of columns in line " + n + 
							"doesn't match number of header columns",
							errors);
					continue;
				}
				monitor.setTaskName("Importing expression data - processing line " + n + "; " + errors + " errors");
				//Check id and add data
				String id = data[info.idColumn];
				String code = data[info.codeColumn];
				ArrayList<String> ensIds = GmmlGdb.ref2EnsIds(id, code); //Find the Ensembl genes for current gene
				
				if(ensIds.size() == 0) //No Ensembl gene found
				{
					errors = reportError(error, "Line " + n + ": " + id + "\t" + code + 
							"\t No Ensembl gene found for this identifier", errors);
				} else { //Gene maps to an Ensembl id, so add it
					boolean success = true;
					for( String ensId : ensIds) //For every Ensembl id add the data
					{
						for(int col : dataCols)
						{
							try {
								pstmt.setString(1,id);
								pstmt.setString(2,code);
								pstmt.setString(3, ensId);
								pstmt.setString(4, Integer.toString(dataCols.indexOf(col)));
								pstmt.setString(5, data[col]);
								pstmt.setInt(6, added);
								pstmt.execute();
							} catch (Exception e) {
								errors = reportError(error, "Line " + n + ":\t" + line + "\n" + 
										"\tError: " + error, errors);
								success = false;
							}
						}
					}
					if(success) added++;
				}
				monitor.worked(worked);
			}
			page.println(added + " genes were added succesfully to the expression dataset");
			if(errors > 0) {
				page.println(errors + " errors occured, see file '" + errorFile + "' for details");
			} else {
				new File(errorFile).delete(); // If no errors were found, delete the error file
			}
			monitor.setTaskName("Closing database connection");
			close(true);
			monitor.worked(finalizeWork);
			
			error.println("Time to create expression dataset: " + timer.stop());
			error.close();
			
//			try {
//				connect(); //re-connect and use the created expression dataset
//			} catch(Exception e) {
//				GmmlVision.log.error("Exception on connecting expression dataset from import thread", e);
//			}
			
		} catch(Exception e) { 
			page.println("Import aborted due to error: " + e.getMessage());
			GmmlVision.log.error("Expression data import error", e);
			close(true);
			error.close();
		}
	}
	
	private static int reportError(PrintStream out, String message, int nrError) 
	{
		out.println(message);
		nrError++;
		return nrError;
	}
	/**
	 * {@link Connection} to the GenMAPP Expression Dataset
	 */
	private static Connection conGmGex;

	private static File gmGexFile;
	/**
	 * Returns the file that contains the GenMAPP Expression Dataset
	 */
	public static File getGmGexFile() { return gmGexFile; }
	/**
	 * Sets the file that contains the GenMAPP Expression Dataset
	 */
	public static void setGmGexFile(File file) { gmGexFile = file; }
	
	/**
	 * Converts the GenMAPP Expression Dataset (given in {@link gmGexFile}) to a expression database
	 * in Hsqldb format as used by this program.
	 * <BR><BR>This method reports all errors occured during the conversion to a file named 'convert_gex_error.txt'
	 */
	public static void convertGex()
	{
		//Open a connection to the error file
		PrintWriter error = null;
		try {
			error = new PrintWriter(new FileWriter("convert_gex_error.txt"));
		} catch(IOException ex) {
			GmmlVision.log.error("Unable to open error file for gdb conversion: " + ex.getMessage(), ex);
		}
		
		try {
			connect(true, false); //Connect and delete the old database if exists
			connectGmGex(gmGexFile); //Connect to the GenMAPP gex
			
			con.setAutoCommit(false); //Keep control over when to commit, should increase speed
			Statement s = conGmGex.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			PreparedStatement pstmtExpr = con.prepareStatement(
					"INSERT INTO expression			" +
					"	(id, code, ensId,			" + 
					"	 idSample, data)			" +
			"VALUES	(?, ?, ?, ?, ?)			");
			
			ResultSet r = s.executeQuery("SELECT * FROM Expression");
			r.last();
			int nrRows = r.getRow(); //Get the number of rows for keeping track of the progress
			r.beforeFirst(); //Set the cursor back to the start
			
			// Fill the Sample table
			ResultSetMetaData rsmd = r.getMetaData(); 
			int nCols = rsmd.getColumnCount();
			for(int i = 4; i < nCols - 1; i++) // Column 4 to 2 before last contain expression data
			{
				int dataType = rsmd.getColumnType(i);
				String sampleName = rsmd.getColumnName(i);
				// Add new sample
				con.createStatement().execute("INSERT INTO SAMPLES" +
						"	(idSample, name, dataType)" + 
						"VALUES ( " + (i - 4) + ",'" + sampleName + "', " + dataType + " )");
			}
			
			//Fill the Expression table
			int nq = 0; //The number of queries excecuted
			String id = "";
			String code = "";
			while(r.next()) { //Process all rows of the expression data
				if(convertThread.isInterrupted) //Check if the user cancelled the conversion
				{
					closeGmGex();
					close();
					return;
				}
				
				id = r.getString("ID");
				code = r.getString("SystemCode");
				ArrayList<String> ensIds = GmmlGdb.ref2EnsIds(id, code); //Find the Ensembl genes for current gene
				
				if(ensIds.size() == 0) //No Ensembl gene found
				{
					error.println(id + "\t" + code + "\t No Ensembl gene found for this identifier");
				} else { //Gene maps to an Ensembl id, so add it
					ArrayList<String> data = new ArrayList<String>();
					for(int i = 4; i < nCols - 1; i++) { // Column 4 to 2 before last contain expression data
						data.add(r.getString(i));
					}
					for( String ensId : ensIds) //For every Ensembl id add the data
					{
						int i = 0;
						for(String str : data)
						{
							try {
								pstmtExpr.setString(1,id);
								pstmtExpr.setString(2,code);
								pstmtExpr.setString(3, ensId);
								pstmtExpr.setInt(4,i);
								pstmtExpr.setString(5,str);
								pstmtExpr.execute();
							} catch (Exception e) {
								error.println(id + ", " + code + ", " + i + "\t" + e.getMessage());
							}
							i++;
						}
					}
				}
				nq++;
				if(nq % 1000 == 0) //Commit every 1000 queries
					con.commit();
				convertThread.progress += 100.0/nrRows; //Report progress
			}
			con.commit();	
		} catch(Exception e) {
			error.println("Error: " + e.getMessage());
		}
		error.println("END");
		error.close();
		closeGmGex();
		close();
		
//		setGexReadOnly(true);
		
		convertThread.progress = 100;
	}
	
	public static DBConnector getDBConnector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return GmmlVision.getDbConnector(DBConnector.TYPE_GEX);
	}
	
	/**
	 * Connects to the Expression database (location given in {@link gexFile} with
	 * option to remove the old database
	 * @param 	clean true if the old database has to be removed, false for just connecting
	 * @return 	null if the connection was created, a String with an error message if an error occured
	 */
	public static void connect(boolean create, boolean fireEvent) throws Exception
	{
		DBConnector connector = getDBConnector();
		
		if(create) {
			con = connector.createNewDatabase(dbName);
		} else {
			con = connector.createConnection(dbName);
			loadXML();
			setSamples();
		}

		con.setReadOnly( !create );
		
		if(fireEvent)
			fireExpressionDataEvent(new ExpressionDataEvent(GmmlGex.class, ExpressionDataEvent.CONNECTION_OPENED));
	}
	
	/**
	 * Connects to the Expression database (location given in {@link gexFile}
	 * @return null if the connection was created, a String with an error message if an error occured
	 */
	public static void connect() throws Exception
	{
		connect(false, true);
	}
		
	/**
	 * Close the connection to the Expression database, with option to execute the 'SHUTDOWN COMPACT'
	 * statement before calling {@link Connection.close()}
	 * @param shutdown	true to excecute the 'SHUTDOWN COMPACT' statement, false to just close the connection
	 */
	public static void close(boolean finalize)
	{
		if(con != null)
		{
			try
			{
				saveXML();
				
				DBConnector connector = getDBConnector();
				if(finalize) {
					connector.compact(con);
					connector.createIndices(con);
					connector.finalizeNewDatabase(dbName);
				} else {
					connector.closeConnection(con);
				}
				fireExpressionDataEvent(new ExpressionDataEvent(GmmlGex.class, ExpressionDataEvent.CONNECTION_CLOSED));
				
			} catch (Exception e) {
				GmmlVision.log.error("Error while closing connection to expression dataset " + dbName, e);
			}
		}
	}
	
	/**
	 * Close the connection excecuting the 'SHUTDOWN' statement 
	 * before calling {@link Connection.close()}
	 */
	public static void close()
	{
		close(false);
	}
	
	/**
	 * Connect to the GenMAPP Expression Dataset specified by the given file
	 * @param gmGexFile	File containing the GenMAPP Expression Dataset
	 */
	public static void connectGmGex(File gmGexFile) {
		String database_after = ";DriverID=22;READONLY=true";
		String database_before =
			"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conGmGex = DriverManager.getConnection(
					database_before + gmGexFile.toString() + database_after, "", "");
		} catch (Exception e) {
			GmmlVision.log.error("Error: Unable to open connection go GenMAPP gex " + gmGexFile +
					": " +e.getMessage(), e);
		}
	}
	
	/**
	 * Close the connection to the GenMAPP Expression Dataset
	 */
	public static void closeGmGex() {
		if(conGmGex != null)
		{
			try {
				conGmGex.close();
				conGmGex = null;
			} catch (Exception e) {
				GmmlVision.log.error("Error while closing connection to GenMAPP gex: " + e.getMessage(), e);
			}
		}
	}
	
	public void applicationEvent(ApplicationEvent e) {
		switch(e.type) {
		case ApplicationEvent.CLOSE_APPLICATION:
			if(isConnected()) close();
		}
	}
	
	static List<ExpressionDataListener> listeners;
	
	/**
	 * Add a {@link ExpressionDataListener}, that will be notified if an
	 * event related to expression data occurs
	 * @param l The {@link ExpressionDataListener} to add
	 */
	public static void addListener(ExpressionDataListener l) {
		if(listeners == null) listeners = new ArrayList<ExpressionDataListener>();
		listeners.add(l);
	}
	
	/**
	 * Fire a {@link ExpressionDataEvent} to notify all {@link ExpressionDataListener}s registered
	 * to this class
	 * @param e
	 */
	public static void fireExpressionDataEvent(ExpressionDataEvent e) {
		for(ExpressionDataListener l : listeners) l.expressionDataEvent(e);
	}
	
	public interface ExpressionDataListener {
		public void expressionDataEvent(ExpressionDataEvent e);
	}
	
	public static class ExpressionDataEvent extends EventObject {
		private static final long serialVersionUID = 1L;
		public static final int CONNECTION_OPENED = 0;
		public static final int CONNECTION_CLOSED = 1;

		public Object source;
		public int type;
		
		public ExpressionDataEvent(Object source, int type) {
			super(source);
			this.source = source;
			this.type = type;
		}
	}
}
