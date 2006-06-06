package data;

import java.io.File;
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

import colorSet.GmmlColorGradient;
import colorSet.GmmlColorSet;
import colorSet.GmmlColorSetObject;
import data.GmmlGdb.ConvertThread;

/**
 * This class handles everything related to the Expression Data. It contains the database connection,
 * several methods to query data and write data and methods to convert a GenMAPP Expression Dataset
 * to hsqldb format
 */
public class GmmlGex {
	/**
	 * {@link Connection} to the Expression data
	 */
	public Connection con;
	/**
	 * {@link File} pointing to the Expression data (.properties file of the Hsql database)
	 */
	public File gexFile;
	public GmmlGdb gmmlGdb;
	
	/**
	 * {@link ColorSet}s used for the currently loaded Expression data
	 */
	public Vector<GmmlColorSet> colorSets;
	
	/**
	 * Constructur for this class
	 * @param gmmlGdb	{@link GmmlGdb} object containing a connection to the Gene Database
	 */
	public GmmlGex(GmmlGdb gmmlGdb) {
		this.gmmlGdb = gmmlGdb;
		colorSets = new Vector<GmmlColorSet>();
	}
	
	/**
	 * Sets the {@link ColorSet}s used for the currently loaded Expression data
	 * @param colorSets {@link Vector} containing the {@link ColorSet} objects
	 */
	public void setColorSets(Vector colorSets)
	{
		this.colorSets = colorSets;
	}
	
	/**
	 * Gets the names of all {@link GmmlColorSet}s used 
	 * @return
	 */
	public String[] getColorSetNames()
	{
		String[] colorSetNames = new String[colorSets.size()];
		for(int i = 0; i < colorSetNames.length; i++)
		{
			colorSetNames[i] = ((GmmlColorSet)colorSets.get(i)).name;
		}
		return colorSetNames;
	}
	
//	public void setGexReadOnly(boolean readonly)
//	{
//		boolean reconnect = false;		
//		if(con != null)
//		{
//			System.out.println("reconnecting");
//			reconnect = true;
//			close(false);
//		}
//		Properties gexProp = new Properties();
//		try {
//		gexProp.load(new FileInputStream(gexFile));
//		gexProp.setProperty("readonly", Boolean.toString(readonly));
//		gexProp.store(new FileOutputStream(gexFile), "HSQL Database Engine");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if(reconnect)
//		{
//			connect();
//		}
//	}
	
	/**
	 * Saves the {@link ColorSets} in the Vector {@link colorSets} to the Expression database
	 */
	public void saveColorSets()
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
			e.printStackTrace();
		}
		
//		setGexReadOnly(true);
		
	}
	
	/**
	 * Load the colorset data stored in the Expression database in memory
	 */
	public void loadColorSets()
	{
		try
		{
			colorSets = new Vector<GmmlColorSet>();
			Statement sCso = con.createStatement();
			ResultSet r = con.createStatement().executeQuery(
				"SELECT colorSetId, name, criterion FROM colorSets ORDER BY colorSetId" );
			while(r.next())
			{
				GmmlColorSet cs = new GmmlColorSet(r.getString(2), r.getString(3), this);
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
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Contains the different {@Sample} object representing records from the Sample table
	 * in the Expression database, with as key the 'idSample' field
	 */
	public HashMap<Integer, Sample> samples;
	/**
	 * Loads the samples used in the expression data (Sample table) in memory
	 */
	public void setSamples()
	{
		samples = new HashMap<Integer, Sample>();
		try {
			ResultSet r = con.createStatement().executeQuery(
					"SELECT idSample, name, dataType FROM samples"
					);
			
			while(r.next())
			{
				int id = r.getInt(1);
				samples.put(id, new Sample(id, r.getString(2), r.getInt(3)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This class represents a record in the Sample table of the Expression database. 
	 */
	public class Sample implements Comparable
	{
		public int idSample;
		public String name;
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
	 * @return		true if Expression data is found in cache, false if not
	 */
	public boolean hasData(String id)
	{
		if(data == null)
		{
			return false;
		}
		if(data.containsKey(id))
		{
			RefData refData = data.get(id);
			if(refData.sampleData.size() > 0)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This {@link HashMap} contains the cached data in the form of {@link RefData} objects
	 * for all gene ids on the currently loaded
	 * mapp for which data is found in the Expression database
	 */
	public HashMap<String, RefData> data;
	/**
	 * This class contains the cached data from the Expression database for a single gene id.
	 */
	public class RefData
	{
		String mappId;
		/**
		 * Contains the Expression data for all samples and all cross references for the
		 * gene id of this object. Uses idSample as key and an {@link ArrayList<String[]>}
		 * containing a String[] for every cross reference. This String[] with a length of 2 contains
		 * the gene id at index 0 and the corresponding data value at index 1
		 */
		public HashMap<Integer, ArrayList<String[]>> sampleData;
		
		/**
		 * Constructor for this class
		 * @param mappId	the gene id for which this object contains expression data
		 */
		public RefData(String mappId)
		{
			this.mappId = mappId;
			sampleData = new HashMap<Integer, ArrayList<String[]>>();
		}
		
		/**
		 * Gets all cross references for the gene id of this object ({@link mappId})
		 * for which Expression data is available
		 * @return
		 */
		public ArrayList<String> getRefIds()
		{
			int someSample = ((Sample)samples.values().toArray()[0]).idSample;
			ArrayList<String> refIds = new ArrayList<String>();
			if(!sampleData.containsKey(someSample)) return null;
			for(String[] s : sampleData.get(someSample))
			{
				refIds.add(s[0]);
			}
			return refIds;
		}
		
		/**
		 * Gets the average of the data over the cross references for every sample
		 * @return returns a {@link HashMap} with as key the sampleId and as value
		 * either a Double or a String (depending on the sample's data type) representing
		 * the average (over all cross references) in case the data type was Double
		 * or a combined String seperated by comma's in case the data type was String
		 */
		public HashMap<Integer, Object> getAvgSampleData()
		{
			HashMap<Integer, Object> avgSampleData = new HashMap<Integer, Object>();
			for(int i : sampleData.keySet())
			{
				if(samples.get(i).dataType == Types.REAL)
				{
					avgSampleData.put(i, getAvgDouble(sampleData.get(i)));
				} else 
				{
					avgSampleData.put(i, getAvgString(sampleData.get(i)));
				}
			}
			return avgSampleData;
		}
		
		/**
		 * Gets the average data value for an {@link ArrayList} containing a
		 * String[] (with the gene id at index 0 and the data value at index 1) for
		 * every cross reference. For the data value, only Strings that can be parsed as
		 * double are taken into account, others are ignored
		 * @param data	{@link ArrayList} containing a String[] 
		 * (with the gene id at index 0 and the data value at index 1) for
		 * every cross reference
		 * @return average of all data values
		 */
		public Double getAvgDouble(ArrayList<String[]> data)
		{
			double avg = 0;
			boolean numberFound = false;
			for(String[] d : data)
			{
				double v = 0;
				try { v = Double.parseDouble(d[2]); numberFound = true;} catch(Exception e) { }
				avg += v;
			}
			if(numberFound) { return avg / data.size(); } else { return null; }
		}
		
		/**
		 * Gets a combined string for an {@link ArrayList} containing a
		 * String[] (with the gene id at index 0 and the data value at index 1) for
		 * every cross reference.
		 * @param data	{@link ArrayList} containing a String[] 
		 * (with the gene id at index 0 and the data value at index 1) for
		 * every cross reference
		 * @return		A combined String consisting of all data values, seperated by a comma
		 */
		public String getAvgString(ArrayList<String[]> data)
		{
			StringBuilder str = new StringBuilder("Multiple values: ");
			for(String[] d : data)
			{
				str.append(d[2] + ", ");
			}
			return str.substring(0, str.lastIndexOf(", "));
		}
		
		/**
		 * Checks whether this object contains more than one cross reference with Expression data
		 * and therefore the data will be averaged
		 * @return	true if the data will be averaged, false if not
		 */
		public boolean isAveraged()
		{
			if(sampleData.size() > 0)
			{
				return sampleData.get(sampleData.keySet().toArray()[0]).size() > 1;
			}
			return false;
		}
	}	
	
	
	/**
	 * Gets all available expression data for the given gene id and returns a string
	 * containing this data in a HTML table
	 * @param id	the gene id for which the data has to be returned
	 * @return		String containing the expression data in HTML format or a string displaying a
	 * 'no expression data found' message in HTML format
	 */
	public String getDataString(String id)
	{
		String noDataFound = "<P><I>No expression data found";
		String exprInfo = "<P><B>Gene id on mapp: " + id + "</B><TABLE border='1'>";
		
		String colNames = "<TR><TH>Sample name";
		RefData refData = null;
		if(		con == null //Need a connection to the expression data
				|| gmmlGdb.getCon() == null //and to the gene database
				|| data.containsKey(id) //data in the expression dataset for this gene present
				|| refData.sampleData == null //does the RefData object contain data
				) return noDataFound;
		
		refData = data.get(id);
		ArrayList<String> refIds = refData.getRefIds();
		if(refIds == null) return noDataFound; //The gene doesn't have data after all
		for(String refId : refIds)
		{
			colNames += "<TH>" + refId;
		}
		String dataString = "";
		for(Sample s : samples.values())
		{
			dataString += "<TR><TH>" + s.name;
			if(refData.sampleData.get(s.idSample) != null)
			{
				for(String[] data : refData.sampleData.get(s.idSample))
				{
					dataString += "<TH>" + data[2];
				}
			}
		}
		
		return exprInfo + colNames + dataString;
	}
	
	/**
	 * Loads expression data for all the given gene ids into memory
	 * @param ids	Gene ids to cache the expression data for
	 * (typically all genes in a pathway)
	 */
	public void cacheData(ArrayList<String> ids)
	{	
		if(samples == null)	setSamples(); //Cache the samples in the dataset
	
		data = new HashMap<String, RefData>();
		for(String id : ids)
		{			 			
			ArrayList<String> ensIds = gmmlGdb.ref2EnsIds(id); //Get all Ensembl genes for this id
			if(ensIds.size() > 0) //Only create a RefData object if the id maps to an Ensembl gene
			{
				RefData refData = new RefData(id);
				data.put(id, refData);
				
				for(String ensId : ensIds)
				{				
					try {					
						ResultSet r = con.createStatement().executeQuery(
								"SELECT id, data, idSample FROM expression " +
								" WHERE ensId = '" + ensId + "'");
						//r contains all genes and data mapping to the Ensembl id
						while(r.next())
						{						 
							String[] data = new String[3];
							int idSample = r.getInt(3);
							if(refData.sampleData.containsKey(idSample))
							{//This sample is already present in the sampleData, append
								data[0] = r.getString(1);
								data[1] = ensId;
								data[2] = r.getString(2);
								refData.sampleData.get(idSample).add(data);
							}
							else
							{//This sample is not present yet, create
								ArrayList<String[]> d = new ArrayList<String[]>();
								data[0] = r.getString(1);
								data[1] = ensId;
								data[2] = r.getString(2);
								d.add(data);
								refData.sampleData.put(idSample, d);
							}						
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
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
	private CacheThread cacheThread;
	/**
	 * This class is a {@link Thread} that is responsible for calling {@link cacheData()}
	 * and keeping the progress of its progress
	 */
	private class CacheThread extends Thread
	{
		volatile double progress;
		volatile boolean isInterrupted;
		ArrayList<String> ids;
		/**
		 * Constructor for this class
		 * @param ids	the gene ids that need to be passed on to {@link cacheData()}
		 */
		public CacheThread(ArrayList<String> ids) 
		{
			this.ids = ids;
		}
		
		public void run()
		{
			progress = 0;
			isInterrupted = false;
			cacheData(ids);
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
	 * @return
	 */
	public IRunnableWithProgress createCacheRunnable(ArrayList<String> mappIds)
	{
		final ArrayList<String> ids = mappIds;
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Caching expression data",100);
				cacheThread = new CacheThread(ids);
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
	
	private ConvertThread convertThread;
	/**
	 * This class is a {@link Thread} that converts a GenMAPP Expression dataset and keeps the progress
	 * of the conversion
	 */
	private class ConvertThread extends Thread
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
	public IRunnableWithProgress convertRunnable = new IRunnableWithProgress() {
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
	 * {@link Connection} to the GenMAPP Expression Dataset
	 */
	private Connection conGmGex;
	/**
	 * File that contains the GenMAPP Expression Dataset
	 */
	public File gmGexFile;
	
	/**
	 * Converts the GenMAPP Expression Dataset (given in {@link gmGexFile}) to a expression database
	 * in Hsqldb format as used by this program.
	 * <BR><BR>This method reports all errors occured during the conversion to a file named 'convert_gex_error.txt'
	 */
	public void convertGex()
	{
		//Open a connection to the error file
    	PrintWriter error = null;
	    try {
	        error = new PrintWriter(new FileWriter("convert_gex_error.txt"));
	    } catch(IOException ex) {
	        ex.printStackTrace();
	    }
	    
		connect(true); //Connect and delete the old database if exists
		connectGmGex(gmGexFile); //Connect to the GenMAPP gex
		createTables();
		try {
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
				ArrayList<String> ensIds = gmmlGdb.ref2EnsIds(id); //Find the Ensembl genes for current gene
				
				if(ensIds.size() == 0) //Check if any Ensembl gene maps to current gene
				{
					error.println(id + "\tGene not found in gene database");
					try { // Try to find via name (NOTE: quick fix to enable conversion of Martijn's
						// Proteomics/transcriptomics dataset which contains gene symbols as identifier,
						// which supposingly is allowed in GenMAPP
						ResultSet r1 = gmmlGdb.getCon().createStatement().executeQuery(
								"SELECT id FROM gene " +
								"WHERE name = '" + id.trim() + "'"
						);
						r1.next();
						id = r1.getString(1); //Use the symbol as id
						ensIds = gmmlGdb.ref2EnsIds(id);
						if(ensIds.size() > 0) error.println("\t\tAdded gene by name/symbol: " + id + "");
					} catch(Exception e) {
//						e.printStackTrace();
						error.println("\t\tLooking for gene by name: not found in gdb + " + e.getMessage());
					}
				}
				if(ensIds.size() == 0) //No gene found by looking at symbol neither
				{
					error.println("\t\tLooking for gene by name: found in gdb, but no linking ensembl gene exists for " + id);
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
			e.printStackTrace();
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
	public String connect(boolean clean)
	{
		if(clean)
		{
			//remove old property file
			File gexPropFile = gexFile;
			gexPropFile.delete();
			return connect();
		}
		else
		{
			return connect();
		}
	}
	
	/**
	 * Connects to the Expression database (location given in {@link gexFile}
	 * @return null if the connection was created, a String with an error message if an error occured
	 */
	public String connect()
	{
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Properties prop = new Properties();
			prop.setProperty("user","sa");
			prop.setProperty("password","");
			//prop.setProperty("hsqldb.default_table_type","cached");
			String file = gexFile.getAbsolutePath().toString();
			con = DriverManager.getConnection("jdbc:hsqldb:file:" + 
					file.substring(0,file.lastIndexOf(".")) + ";shutdown=true", prop);
			
//			System.out.println(con.isReadOnly());
			con.setReadOnly(true);
			return null;
		} catch(Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	/**
	 * Close the connection to the Expression database, with option to execute the 'SHUTDOWN COMPACT'
	 * statement before calling {@link Connection.close()}
	 * @param shutdown	true to excecute the 'SHUTDOWN COMPACT' statement, false to just close the connection
	 */
	public void close(boolean shutdown)
	{
		if(con != null)
		{
			try
			{
				Statement sh = con.createStatement();
				if(shutdown) {
					sh.executeQuery("SHUTDOWN COMPACT"); // required, to write last changes
				}
				sh.close();
				con = null;
			} catch (Exception e) {
				System.out.println ("Error: " +e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Close the connection excecuting the 'SHUTDOWN COMPACT' statement 
	 * before calling {@link Connection.close()}
	 */
	public void close()
	{
		close(true);
	}
	
	/**
	 * Connect to the GenMAPP Expression Dataset specified by the given file
	 * @param gmGexFile	File containing the GenMAPP Expression Dataset
	 */
	public void connectGmGex(File gmGexFile) {
		String database_after = ";DriverID=22;READONLY=true";
		String database_before =
			"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conGmGex = DriverManager.getConnection(
					database_before + gmGexFile.toString() + database_after, "", "");
		} catch (Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Close the connection to the GenMAPP Expression Dataset
	 */
	public void closeGmGex() {
		if(conGmGex != null)
		{
			try {
				conGmGex.close();
				conGmGex = null;
			} catch (Exception e) {
				System.out.println ("Error: " +e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Excecutes several SQL statements to create the tables and indexes for storing 
	 * the expression data
	 */
	public void createTables() {	
		try {
			con.setReadOnly(false);
			Statement sh = con.createStatement();
			sh.execute("DROP TABLE samples IF EXISTS");
			sh.execute("DROP TABLE expression IF EXISTS");
			sh.execute("DROP TABLE colorSets IF EXISTS");
			sh.execute("DROP TABLE colorSetObjects IF EXISTS");
			sh.execute("DROP TABLE textdata IF EXISTS");
		} catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
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
					"     data REAL							" +
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
//			sh.execute(
//					"CREATE CACHED TABLE		" +
//					"		textdata			" +
//					"(	id VARCHAR(50),			" +
//					"	code VARCHAR(50),		" +
//					"   ensId VARCHAR(50),		" +	
//					"	idSample INTEGER,		" +
//					"	data VARCHAR(100)	)");
//			sh.execute(
//					"CREATE INDEX i_textdata_id " +
//					"ON textdata(id)			 ");
//			sh.execute(
//					"CREATE INDEX i_textdata_ensId " +
//					"ON textdata(ensId)			 ");
//			sh.execute(
//					"CREATE INDEX i_textdata_idSample " +
//					"ON textdata(idSample)	 ");
//			sh.execute(
//					"CREATE INDEX i_textdata_data " +
//					"ON textdata(data)	     ");
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
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
