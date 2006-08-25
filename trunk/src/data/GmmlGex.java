package data;

import gmmlVision.GmmlVision;
import gmmlVision.GmmlVisionWindow;
import graphics.GmmlDrawing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import colorSet.GmmlColorCriterion;
import colorSet.GmmlColorGradient;
import colorSet.GmmlColorSet;
import colorSet.GmmlColorSetObject;
import data.GmmlGex.CachedData.Data;
import data.ImportExprDataWizard.ImportInformation;
import data.ImportExprDataWizard.ImportPage;

/**
 * This class handles everything related to the Expression Data. It contains the database connection,
 * several methods to query data and write data and methods to convert a GenMAPP Expression Dataset
 * to hsqldb format
 */
public abstract class GmmlGex {

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
	
	private static File gexFile;
	/**
	 * Get the {@link File} pointing to the Expression data 
	 * (.properties file of the Hsql database)
	 */
	public static File getGexFile() { return gexFile; }
	
	/**
	 * Set the {@link File} pointing to the Expression data
	 * (.properties file of the Hsql database)
	 */
	public static void setGexFile(File file) { gexFile = file; }
	
	private static Vector<GmmlColorSet> colorSets = new Vector<GmmlColorSet>();
	
	/**
	 * Gets the {@link ColorSet}s used for the currently loaded Expression data
	 */
	public static Vector<GmmlColorSet> getColorSets() { return colorSets; }
	
	/**
	 * Index of the colorSet that is currently used
	 */
	private static int colorSetIndex = -1;
	
	/**
	 * Set the index of the colorset to use
	 * @param colorSetIndex
	 */
	public static void setColorSetIndex(int _colorSetIndex)
	{
		GmmlVisionWindow window = GmmlVision.getWindow();
		colorSetIndex = _colorSetIndex;
		if(colorSetIndex < 0)
		{
			window.showLegend(false);
		} else {
			window.showLegend(true);
		}
		GmmlDrawing d = GmmlVision.getDrawing();
		if(d != null) { d.redraw(); }
	}
	
	public static void setColorSet(GmmlColorSet cs) {
		int ci = getColorSets().indexOf(cs);
		if(ci > -1) setColorSetIndex(ci);
	}
	
	/**
	 * Get the index of the currently used colorset
	 * @return
	 */
	public static int getColorSetIndex() { 
		return colorSetIndex;
	}
	
	/**
	 * Sets the {@link ColorSet}s used for the currently loaded Expression data
	 * @param colorSets {@link Vector} containing the {@link ColorSet} objects
	 */
	public static void setColorSets(Vector<GmmlColorSet> _colorSets)
	{
		colorSets = _colorSets;
	}
	
	/**
	 * Removes this {@link ColorSet}
	 * @param cs Colorset to remove
	 */
	public static void removeColorSet(GmmlColorSet cs) {
		if(colorSets.contains(cs)) {
			colorSets.remove(cs);
			if(colorSetIndex == 0 && colorSets.size() > 0) setColorSetIndex(colorSetIndex);
			else setColorSetIndex(colorSetIndex - 1);
		}
	}
	
	/**
	 * Removes this {@link ColorSet}
	 * @param i index of ColorSet to remove
	 */
	public static void removeColorSet(int i) {
		if(i > -1 && i < colorSets.size()) {
			removeColorSet(colorSets.get(i));
		}
	}
	
	/**
	 * Gets the names of all {@link GmmlColorSet}s used 
	 * @return
	 */
	public static String[] getColorSetNames()
	{
		String[] colorSetNames = new String[colorSets.size()];
		for(int i = 0; i < colorSetNames.length; i++)
		{
			colorSetNames[i] = ((GmmlColorSet)colorSets.get(i)).name;
		}
		return colorSetNames;
	}
	
	/**
	 * Saves the {@link ColorSets} in the Vector {@link colorSets} to the Expression database
	 */
	public static void saveColorSets()
	{
		try
		{
//			setGexReadOnly(false);
			con.setReadOnly(false);
			Statement s = con.createStatement();
			s.execute("DELETE FROM colorSets");
			s.execute("DELETE FROM colorSetObjects");
			
			PreparedStatement sCs = con.prepareStatement(
					"INSERT INTO colorSets	" +
					"( colorSetId, name, criterion ) VALUES	" +
			"( ?, ?, ? )"	);
			PreparedStatement sCso = con.prepareStatement(
					"INSERT INTO colorSetObjects 	" +
					"( 	name, colorSetId,		" +
					"	criterion	) VALUES		" +
			"(	?, ?, ?	)"	);
			
			for(int i = 0; i < colorSets.size(); i++)
			{
				GmmlColorSet cs = (GmmlColorSet)colorSets.get(i);
				sCs.setInt(1, i);
				sCs.setString(2, cs.name);
				sCs.setString(3, cs.getCriterionString());
				sCs.execute();
				for(int j = 0; j < cs.colorSetObjects.size(); j++)
				{
					GmmlColorSetObject cso = (GmmlColorSetObject)cs.colorSetObjects.get(j);
					sCso.setString(1, cso.getName());
					sCso.setInt(2, i);
					sCso.setString(3, cso.getCriterionString());
					sCso.execute();
				}
			}
			con.setReadOnly(true);
		}
		catch (Exception e) {
			GmmlVision.log.error("while saving colorset information to expression database: " + gexFile, e);
		}
		
//		setGexReadOnly(true);
		
	}
	
	/**
	 * Load the colorset data stored in the Expression database in memory
	 */
	public static void loadColorSets()
	{
		try
		{
			Statement sCso = con.createStatement();
			ResultSet r = con.createStatement().executeQuery(
			"SELECT colorSetId, name, criterion FROM colorSets ORDER BY colorSetId" );
			colorSets = new Vector<GmmlColorSet>();
			while(r.next())
			{
				GmmlColorSet cs = new GmmlColorSet(r.getString(2), r.getString(3));
				colorSets.add(cs);
				ResultSet rCso = sCso.executeQuery(
						"SELECT * FROM colorSetObjects" +
						" WHERE colorSetId = " + r.getInt(1) +
				" ORDER BY id");
				while(rCso.next())
				{
					String name = rCso.getString(2);
					String criterion = rCso.getString(4);
					if(criterion.contains("GRADIENT"))
					{
						GmmlColorSetObject co = new GmmlColorGradient(cs, name, criterion);
						cs.addObject(co);
					} else if(criterion.contains("EXPRESSION"))
					{
						GmmlColorSetObject co = new GmmlColorCriterion(cs, name, criterion);
						cs.addObject(co);
					}
				}
			}
		}
		catch (Exception e)
		{
			GmmlVision.log.error("while loading colorset information from expression database: " + gexFile, e);
		}
		
	}
	
	public static CachedData cachedData;
	public static class CachedData
	{
		private HashMap<String, Data> data;		
		public CachedData()
		{
			if(samples == null) setSamples(); //Cache the samples in the dataset
			data = new HashMap<String, Data>();
		}
		
		public boolean hasData(String id, String code)
		{
			if(data.containsKey(id)) return data.get(id).id.equalsIgnoreCase(id);
			else return false;
		}
		
		public Data getData(String id, String code)
		{
			Data d = null;
			if(data.containsKey(id)) { 
				d = data.get(id);
				if(!d.id.equalsIgnoreCase(id)) d = null;
			}
			return d;
		}
		
		public void addData(String id, Data mappIdData)
		{
			data.put(id, mappIdData);
		}
		
		public class Data
		{
			private String id;
			private String code;
			private HashMap<Integer, Object> sampleData;
			private HashMap<String, Data> refData;
			
			public Data(String id, String code) {
				this.id = id;
				this.code = code;
				refData = new HashMap<String, Data>();
				sampleData = new HashMap<Integer, Object>();
			}
			
			public boolean hasData() { return refData.size() > 0; }
			
			public void addRefData(String id, String code, int sampleId, String data) 
			{ 
				Data ref = null;
				if(refData.containsKey(id)) ref = refData.get(id);
				else ref = new Data(id, code);
				
				Object parsedData = null;
				try { parsedData = Double.parseDouble(data); }
				catch(Exception e) { parsedData = data; }
				ref.addSampleData(sampleId, parsedData);
				refData.put(id, ref);
			}
			
			public void addSampleData(int sampleId, Object data)
			{
				if(data != null) sampleData.put(sampleId, data);
			}
			
			public ArrayList<Data> getRefData()
			{
				return new ArrayList<Data>(refData.values());
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
	
	/**
	 * This class represents a record in the Sample table of the Expression database. 
	 */
	public static class Sample implements Comparable
	{
		public int idSample;
		private String name;
		public int dataType;
		
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
		
		/**
		 * Compares this object to another {@link Sample} object based on the idSample property
		 * @param o	The {@link Sample} object to compare with
		 * @return	integer that is zero if the objects are equal, negative if this object has a
		 * lower idSample, positive if this object has a higher idSample
		 * @throws ClassCastException
		 */
		public int compareTo(Object o) throws ClassCastException
		{
			if(o instanceof Sample)
			{
				return idSample - ((Sample)o).idSample;
			} else {
				throw new ClassCastException("Object is not of type Sample");
			}
		}
		
		/**
		 * Returns a String representation of this object, which is the idSample property in String form
		 */
		public String toString()
		{
			return Integer.toString(idSample);
		}
	}
	
	/**
	 * Checks whether Expression data is cached for a given gene id
	 * @param id	the gene id
	 * @param code	The systemcode of the gene identifier
	 * @return		true if Expression data is found in cache, false if not
	 */
	public static boolean hasData(String id, String code)
	{
		if(cachedData == null) return false;
		return cachedData.hasData(id, code);
	}
	
	public static HashMap<Integer, Sample> getSamples()
	{
		return samples;
	}
	
	public static Data getCachedData(String id, String code)
	{
		if(cachedData != null) return cachedData.getData(id, code);
		return null;
	}
	
	/**
	 * Gets all available expression data for the given gene id and returns a string
	 * containing this data in a HTML table
	 * @param id	the gene id for which the data has to be returned
	 * @param code	The systemcode of the gene identifier
	 * @return		String containing the expression data in HTML format or a string displaying a
	 * 'no expression data found' message in HTML format
	 */
	public static String getDataString(String id, String code)
	{
		String noDataFound = "<P><I>No expression data found";
		String exprInfo = "<P><B>Gene id on mapp: " + id + "</B><TABLE border='1'>";
		
		String colNames = "<TR><TH>Sample name";
		if(		con == null //Need a connection to the expression data
				|| GmmlGdb.getCon() == null //and to the gene database
		) return noDataFound;
		
		Data mappIdData = cachedData.getData(id, code);
		if(mappIdData == null) return noDataFound;
		ArrayList<Data> refData = mappIdData.getRefData();
		if(refData == null) return noDataFound; //The gene doesn't have data after all
		for(Data d : refData)
		{
			colNames += "<TH>" + d.id;
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
	public static void cacheData(ArrayList<String> ids, ArrayList<String> codes)
	{	
		cachedData = new CachedData();
		for(int i = 0; i < ids.size(); i++)
		{
			String id = ids.get(i);
			String code = codes.get(i);
			ArrayList<String> ensIds = GmmlGdb.ref2EnsIds(id, code); //Get all Ensembl genes for this id
			if(ensIds.size() > 0) //Only create a RefData object if the id maps to an Ensembl gene
			{
				Data mappGeneData = cachedData.new Data(id, code);
				
				for(String ensId : ensIds)
				{				
					try {					
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
					} catch (Exception e)
					{
						GmmlVision.log.error("while caching expression data: " + e.getMessage(), e);
					}
				}
				if(mappGeneData.hasData()) cachedData.addData(id, mappGeneData);
			}
			if(cacheThread.isInterrupted) //Check if the process is interrupted
			{
				return;
			}
			cacheThread.progress += 100.0 / ids.size(); //Update the progress
		}
		cacheThread.progress = 100;
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
		ImportInformation info;
		ImportPage page;
		
		public ImportRunnableWithProgress(ImportInformation info, ImportPage page) {
			super();
			this.info = info;
			this.page = page;
		}
		
		public void run(IProgressMonitor monitor) 
		throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Importing expression data", IProgressMonitor.UNKNOWN);
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
//		Open a connection to the error file
		String errorFile = info.gexFile + ".ex.txt";
		int errors = 0;
		PrintWriter error = null;
		try {
			error = new PrintWriter(new FileWriter(errorFile));
		} catch(IOException ex) {
			page.println("Error: could not open exception file: " + ex.getMessage());
		}
		
		try 
		{
			page.println("Creating expression dataset");
			
			//Create a new expression database (or overwrite existing)
			gexFile = info.gexFile;
			connect(true);
			createTables();
			
			page.println("Importing data");
			page.println("> Processing headers");
			BufferedReader in = new BufferedReader(new FileReader(info.getTxtFile()));
			in.mark(10000);
			String[] headers = info.getColNames();
			//Parse sample names and add to Sample table
			PreparedStatement pstmt = con.prepareStatement(
					" INSERT INTO SAMPLES " +
					"	(idSample, name, dataType)  " +
			" VALUES (?, ?, ?)		  ");
			int sampleId = 0;
			ArrayList<Integer> dataCols = new ArrayList<Integer>();
			for(int i = 0; i < headers.length; i++) {
				if(monitor.isCanceled()) { close(true, true); error.close(); return; } //User pressed cancel
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
			in.reset();
			for(int i = 1; i < info.firstDataRow; i++) in.readLine(); //Go to line where data starts
			pstmt = con.prepareStatement(
					"INSERT INTO expression			" +
					"	(id, code, ensId,			" + 
					"	 idSample, data)			" +
			"VALUES	(?, ?, ?, ?, ?)			");
			String line = null;
			int n = info.firstDataRow - 1;
			int added = 0;
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
			}
			page.println(added + " genes were added succesfully to the expression dataset");
			if(errors > 0) {
				page.println(errors + " errors occured, see file '" + errorFile + "' for details");
			} else {
				new File(errorFile).delete(); // If no errors were found, delete the error file
			}
			monitor.setTaskName("Closing database connection");
			close(true, true);
			error.close();
			connect(); //re-connect and use the created expression dataset
			
		} catch(Exception e) { 
			page.println("Import aborted due to error: " + e.getMessage());
			close(true, true);
			error.close();
		}
	}
	
	private static int reportError(PrintWriter out, String message, int nrError) 
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
			connect(true); //Connect and delete the old database if exists
			connectGmGex(gmGexFile); //Connect to the GenMAPP gex
			createTables();
			
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
	
	/**
	 * Connects to the Expression database (location given in {@link gexFile} with
	 * option to remove the old database
	 * @param 	clean true if the old database has to be removed, false for just connecting
	 * @return 	null if the connection was created, a String with an error message if an error occured
	 */
	public static void connect(boolean clean) throws Exception
	{
		if(clean)
		{
			//remove old property file
			File gexPropFile = gexFile;
			gexPropFile.delete();
			connect();
		}
		else
		{
			connect();
		}
	}
	
	/**
	 * Connects to the Expression database (location given in {@link gexFile}
	 * @return null if the connection was created, a String with an error message if an error occured
	 */
	public static void connect() throws Exception
	{
		Class.forName("org.hsqldb.jdbcDriver");
		Properties prop = new Properties();
		prop.setProperty("user","sa");
		prop.setProperty("password","");
		//prop.setProperty("hsqldb.default_table_type","cached");
		String file = gexFile.getAbsolutePath().toString();
		con = DriverManager.getConnection("jdbc:hsqldb:file:" + 
				file.substring(0,file.lastIndexOf(".")) + ";shutdown=true", prop);
		con.setReadOnly(true);
		
		setSamples();
		loadColorSets();
	}
	
	/**
	 * Close the connection to the Expression database, with option to execute the 'SHUTDOWN COMPACT'
	 * statement before calling {@link Connection.close()}
	 * @param shutdown	true to excecute the 'SHUTDOWN COMPACT' statement, false to just close the connection
	 */
	public static void close(boolean shutdown, boolean compact)
	{
		if(con != null)
		{
			try
			{
				Statement sh = con.createStatement();
				if(shutdown) {
					//Shutdown to write last changes, compact to compact the data file (can take a while)
					sh.executeQuery((shutdown ? "SHUTDOWN" : "") + (compact ? " COMPACT" : ""));
				}
				sh.close();
				con = null;
			} catch (Exception e) {
				GmmlVision.log.error("Error while closing connection to expression dataset " + gexFile, e);
			}
		}
	}
	
	/**
	 * Close the connection excecuting the 'SHUTDOWN' statement 
	 * before calling {@link Connection.close()}
	 */
	public static void close()
	{
		close(true, false);
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
	
	/**
	 * Excecutes several SQL statements to create the tables and indexes for storing 
	 * the expression data
	 */
	public static void createTables() {	
		try {
			con.setReadOnly(false);
			Statement sh = con.createStatement();
			sh.execute("DROP TABLE samples IF EXISTS");
			sh.execute("DROP TABLE expression IF EXISTS");
			sh.execute("DROP TABLE colorSets IF EXISTS");
			sh.execute("DROP TABLE colorSetObjects IF EXISTS");
			sh.execute("DROP TABLE textdata IF EXISTS");
		} catch(Exception e) {
			GmmlVision.log.error("Error: unable to drop expression data tables: "+e.getMessage(), e);
		}
		try
		{
			Statement sh = con.createStatement();
			sh.execute(
					"CREATE CACHED TABLE                    " +
					"		samples							" +
					" (   idSample INTEGER PRIMARY KEY,		" +
					"     name VARCHAR(50),					" +
					"	  dataType INTEGER					" +
			" )										");
			
			sh.execute(
					"CREATE CACHED TABLE					" +
					"		expression						" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"	  ensId VARCHAR(50),				" +
					"     idSample INTEGER,					" +
					"     data VARCHAR(50)					" +
//					"     PRIMARY KEY (id, code, idSample, data)	" +
			" )										");
			sh.execute(
					"CREATE INDEX i_expression_id " +
			"ON expression(id)			 ");
			sh.execute(
					"CREATE INDEX i_expression_ensId " +
			"ON expression(ensId)			 ");
			sh.execute(
					"CREATE INDEX i_expression_idSample " +
			"ON expression(idSample)	 ");
			sh.execute(
					"CREATE INDEX i_expression_data " +
			"ON expression(data)	     ");
			sh.execute(
					"CREATE CACHED TABLE				" +
					"		colorSets					" +
					"(	colorSetId INTEGER PRIMARY KEY,	" +
					"	name VARCHAR(50)," +
			"	criterion VARCHAR(100)	)");
			sh.execute(
					"CREATE CACHED TABLE				" +
					"		colorSetObjects				" +
					"(	id INTEGER IDENTITY,			" +
					"	name VARCHAR(50),				" +
					"	colorSetId INTEGER,				" +
					"	criterion VARCHAR(100)			" +
			" )							");
		} catch (Exception e)
		{
			GmmlVision.log.error("Error while creating expression data tables: " + e.getMessage(), e);
		}
	}
}
