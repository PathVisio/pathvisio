package data;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class GmmlDb {
	final static File propsFile = new File(".gdbProperties");
	
	Connection conGdb;
	public Connection conGex;
	Connection conGmGex;
	
	public Properties props;
	
	File gdbFile;
	public File gexFile;
	public File gmGexFile;
	
	ConvertThread convertThread;
	
	public GmmlDb() {
		props = new Properties();
		try {
			// Check if properties file exists
			if(propsFile.canRead()) {
				props.load(new FileInputStream(propsFile));
			} else {
				// Create properties file
				propsFile.createNewFile();
				props.setProperty("currentGdb", "none");
			}
			if(!props.get("currentGdb").equals("none"))
			{
				gdbFile = new File((String)props.get("currentGdb"));
				if(!connectGdb(null))
					setCurrentGdb("none");
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void setCurrentGdb(String gdb) {
		changeProps("currentGdb", gdb);
	}
	
	public void changeProps(String name, String value) {
		props.setProperty(name, value);
		try {
			props.store(new FileOutputStream(propsFile),"Gene Database Properties");
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getBpInfo(String id) {
		try {
			Statement s = conGdb.createStatement();
			ResultSet r = s.executeQuery("SELECT backpageText FROM gene " +
					"WHERE id = '" + id + "'");
			r.next();
			String result = r.getString(1);
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getGexData(String id) {
		String exprInfo = "<P><B>" + id + "</B><TABLE border='1'>";
		if(conGex != null && conGdb != null) {
			try 
			{				
//				More complicated query, slower (~150 seconds)
//				ArrayList refs = getCrossRefs(id);
				ArrayList ensIds = ref2EnsIds(id);
				System.out.println(ensIds);
				ArrayList refs = ensId2Refs((String)ensIds.get(0));
				
				StringBuilder ensString = new StringBuilder();
				for(int i = 0; i < refs.size(); i++) {
					ensString.append("'" + refs.get(i) + "', ");
				}
				
				for(int i = 0; i < refs.size(); i++) {
//					More complicated query, slower (~10 seconds)
//					ResultSet r = conGex.createStatement().executeQuery(
//					"SELECT id, data, idSample FROM expression " +
//					"WHERE id IN " +
//					"( " + ensString.substring(0,ensString.lastIndexOf(", ")) + " )"
//					);
					ResultSet r = conGex.createStatement().executeQuery(
							"SELECT id, data, idSample FROM expression " +
							"WHERE id = '" + (String)refs.get(i) + "'"
					);
					
					while(r.next())
					{
						String data = r.getString(2);
						ResultSet rsn = conGex.createStatement().executeQuery(
								"SELECT name FROM samples" +
								" WHERE idSample = " + r.getInt(3));
						rsn.next();
						String sampleName = rsn.getString(1);
						exprInfo += "<TR><TH>" + sampleName +
						"<TH>" + data;	
					}
				}
				exprInfo += "</TABLE>";
				System.out.println(exprInfo);
				return exprInfo;
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public Vector getGexDataColumns()
	{
		Vector gexDataColumns = new Vector();
		try {
			ResultSet r = conGex.createStatement().executeQuery(
					"SELECT * FROM samples"
					);
			
			while(r.next())
			{
				int id = r.getInt(1);
				gexDataColumns.add(id, r.getString(2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gexDataColumns;
	}
	
	public ArrayList ensId2Refs(String ensId) {
		ArrayList<String> crossIds = new ArrayList<String>();
		try {
			ResultSet r1 = conGdb.createStatement().executeQuery(
					"SELECT idRight FROM link " +
					"WHERE idLeft = '" + ensId + "'"
					);
			while(r1.next()) {
				crossIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return crossIds;
	}
	
//	Don't use this, multiple simple select queries is faster
//	Subsequentially use ref2EnsIds ensIds2Refs
	public ArrayList getCrossRefs(String id) {
		ArrayList crossIds = new ArrayList();
		try {
			ResultSet r1 = conGdb.createStatement().executeQuery(
					"SELECT idRight FROM link " +
					"WHERE idLeft IN ( 		  " +
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + id + "')"
					);
			while(r1.next()) {
				crossIds.add(r1.getString(1));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return crossIds;
	}
	
	public ArrayList ref2EnsIds(String ref)
	{
		ArrayList<String> ensIds = new ArrayList<String>();
		try {
			ResultSet r1 = conGdb.createStatement().executeQuery(
					"SELECT idLeft FROM link " +
					"WHERE idRight = '" + ref + "'"
			);
			while(r1.next()) {
				System.out.println(r1.getString(1));
				ensIds.add(r1.getString(1));
			}
		} catch(Exception e) {
//			e.printStackTrace();
		}
		return ensIds;
	}
	
	public class ConvertThread extends Thread
	{
		volatile int progress;
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
	
	public IRunnableWithProgress convertRunnable = new IRunnableWithProgress() {
		public void run(IProgressMonitor monitor)
		throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Converting Gene Expression Dataset",100);
			convertThread = new GmmlDb.ConvertThread();
			convertThread.start();
			int prevProgress = 0;
			while(convertThread.progress < 100) {
				if(monitor.isCanceled()) {
					convertThread.interrupt();
					break;
				}
				if(prevProgress < convertThread.progress) {
					monitor.worked(convertThread.progress - prevProgress);
					prevProgress = convertThread.progress;
				}
			}
			monitor.done();
		}
	};
	
//	Don't use this, multiple simple select queries is faster
	public HashMap getEns2RefHash(ResultSet r, int ensIndex)
	{
		HashMap ens2RefHash = new HashMap();
		try {
			int pr = 0;
			StringBuilder idString = new StringBuilder();
			while(r.next()) {
				idString.append("'" + r.getString(ensIndex) + "', ");
				pr++;
				if(pr % 500 == 0) {
					System.out.println(pr);
				}
			}
			String ids = idString.substring(0,idString.lastIndexOf(", "));
		long t = System.currentTimeMillis();
		ResultSet r1 = conGdb.createStatement().executeQuery(
				"SELECT idLeft, idRight FROM link WHERE idLeft IN ( " +
				ids + ")"
		);
		System.out.println("Query to find reference ids for genes took: " +
				(System.currentTimeMillis()-t) + " ms");
		while(r1.next()) {
			String id = r1.getString(1);
			if(ens2RefHash.containsKey(id)) {
				((ArrayList)ens2RefHash.get(id)).add(r1.getString(2));
			} else {
				ArrayList refIds = new ArrayList();
				refIds.add(r1.getString(2));
				ens2RefHash.put(id, refIds);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ens2RefHash;
	}
	
//	Don't use this, multiple simple select queries is faster
	public HashMap getRef2EnsHash(ResultSet r, int refIndex)
	{
		HashMap ref2EnsHash = null;
		try {
			int pr = 0;
			StringBuilder idString = new StringBuilder();
			while(r.next()) {
				idString.append("'" + r.getString(refIndex) + "', ");
				pr++;
				if(pr % 500 == 0) {
					System.out.println(pr);
				}
			}
			String ids = idString.substring(0,idString.lastIndexOf(", "));
			
			ref2EnsHash = new HashMap();
			long t = System.currentTimeMillis();
			ResultSet r1 = conGdb.createStatement().executeQuery(
					"SELECT idLeft, idRight FROM link WHERE idRight IN ( " +
					ids + ")"
			);
			System.out.println("Query to find ensembl ids for genes took: " +
					(System.currentTimeMillis()-t) + " ms");
			while(r1.next()) {
				String id = r1.getString(2);
				if(ref2EnsHash.containsKey(id)) {
					((ArrayList)ref2EnsHash.get(id)).add(r.getString(1));
				} else {
					ArrayList ensIds = new ArrayList();
					ensIds.add(r1.getString(1));
					ref2EnsHash.put(id, ensIds);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ref2EnsHash;
	}
	
	public void convertGex()
	{
		connectGex(true);
		connectGmGex(gmGexFile);
		createGexTables();
		try {
			conGex.setAutoCommit(false);
			Statement s = conGmGex.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			PreparedStatement pstmt = conGex.prepareStatement(
					"INSERT INTO expression			" +
					"	(id, code,					" + 
					"	 idSample, data)			" +
			"VALUES	(?, ?, ?, ?)			");
			
			ResultSet r = s.executeQuery("SELECT * FROM Expression");

			// Select data columns
			ResultSetMetaData rsmd = r.getMetaData();
			
			// Column 4 to 2 before last contain expression data
			int nCols = rsmd.getColumnCount();
			for(int i = 4; i < nCols - 1; i++) 
			{
				String sampleName = rsmd.getColumnName(i);
				// Add new sample
				conGex.createStatement().execute("INSERT INTO SAMPLES" +
						"	(idSample, name)" + 
						"VALUES (" + (i - 4) + ",'" + sampleName + "')");
			}
			r.beforeFirst();
			int nq = 0;
			String id = "";
			String code = "";
			while(r.next()) {
				// Interrupt
				if(convertThread.isInterrupted) {
					closeGmGex();
					closeGex();
					return;
				}
				id = r.getString(2);
				if(ref2EnsIds(id).size() > 0) {
					code = r.getString(3);
					for(int i = 4; i < nCols - 1; i++) {
						try {
							pstmt.setString(1,id);
							pstmt.setString(2,code);
							pstmt.setInt(3,(i - 4));
							pstmt.setString(4,r.getString(i));
							pstmt.execute();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					System.out.println("Id '" + id + "' not found in gene database");
				}
				nq++;
				if(nq % 1000 == 0)
					conGex.commit();
			}
			conGex.commit();
			convertThread.progress += 50/(nCols - 5);	
		} catch(Exception e) {
			e.printStackTrace();
		}
		closeGmGex();
		closeGex();
		//Set readonly = true in properties file
		File gexPropFile = new File(gexFile.getAbsoluteFile().toString() + ".properties");
		Properties gexProp = new Properties();
		try {
		gexProp.load(new FileInputStream(gexPropFile));
		gexProp.setProperty("readonly","true");
		gexProp.store(new FileOutputStream(gexPropFile), "HSQL Database Engine");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		convertThread.progress = 100;
	}
	
	public boolean connectGdb(File gdbFile)
	{
		if(gdbFile == null)
			gdbFile = this.gdbFile;
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Properties prop = new Properties();
			prop.setProperty("user","sa");
			prop.setProperty("password","");
			//prop.setProperty("hsqldb.default_table_type","cached");
			conGdb = DriverManager.getConnection("jdbc:hsqldb:file:" + 
					gdbFile.getAbsolutePath().toString(), prop);
			conGdb.setReadOnly(true);
			setCurrentGdb(gdbFile.getAbsoluteFile().toString());
			return true;
		} catch(Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public void closeGdb()
	{
		try
		{
			Statement sh = conGdb.createStatement();
			sh.executeQuery("SHUTDOWN"); // required, to write last changes
			sh.close();
			conGdb = null;
		} catch (Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void connectGex(boolean clean)
	{
		if(clean)
		{
			//remove old property file
			File gexPropFile = new File(gexFile.getAbsoluteFile().toString() + ".properties");
			gexPropFile.delete();
			connectGex();
		}
		else
		{
			connectGex();
		}
	}
	public void connectGex()
	{
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Properties prop = new Properties();
			prop.setProperty("user","sa");
			prop.setProperty("password","");
			//prop.setProperty("hsqldb.default_table_type","cached");
			conGex = DriverManager.getConnection("jdbc:hsqldb:file:" + 
					gexFile.getAbsolutePath().toString(), prop);
		} catch(Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void closeGex()
	{
		try
		{
			Statement sh = conGex.createStatement();
			sh.executeQuery("SHUTDOWN"); // required, to write last changes
			sh.close();
			conGex = null;
		} catch (Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
		}
	}
	
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
	
	public void closeGmGex() {
		try {
			conGmGex.close();
			conGmGex = null;
		} catch (Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void closeAll() {
		if(conGex != null)
			closeGex();
		if(conGdb != null)
			closeGdb();
		if(conGmGex != null)
			closeGmGex();
	}
	public void createGexTables() {	
		try {
			conGex.setReadOnly(false);
			Statement sh = conGex.createStatement();
			sh.execute("DROP TABLE samples IF EXISTS");
			sh.execute("DROP TABLE expression IF EXISTS");
		} catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
		}
		try
		{
			Statement sh = conGex.createStatement();
			sh.execute(
					"CREATE CACHED TABLE                    " +
					"		samples							" +
					" (   idSample INTEGER PRIMARY KEY,		" +
					"     name VARCHAR(50)					" +
			" )										");
			
			sh.execute(
					"CREATE CACHED TABLE					" +
					"		expression						" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     idSample INTEGER,					" +
					"     data VARCHAR(50)					" +
//					"     PRIMARY KEY (id, code, idSample, data)	" +
			" )										");
			sh.execute(
					"CREATE INDEX i_expression_id " +
					"ON expression(id)			 ");
			sh.execute(
					"CREATE INDEX i_expression_idSample " +
					"ON expression(idSample)	 ");
			sh.execute(
					"CREATE INDEX i_expression_data " +
					"ON expression(data)	     ");
			
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
