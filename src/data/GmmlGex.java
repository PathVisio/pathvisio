package data;

import graphics.GmmlGeneProduct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.RGB;

import colorSet.*;

import data.GmmlDb.ConvertThread;

public class GmmlGex {
	public Connection con;
	Connection conGmGex;
	public File gexFile;
	public File gmGexFile;
	public GmmlGdb gmmlGdb;
	public Vector colorSets;
	
	ConvertThread convertThread;
	
	public GmmlGex(GmmlGdb gmmlGdb) {
		this.gmmlGdb = gmmlGdb;
		colorSets = new Vector();
	}
	
	public void setColorSets(Vector colorSets)
	{
		this.colorSets = colorSets;
	}
	
	public String[] getColorSetNames()
	{
		String[] colorSetNames = new String[colorSets.size()];
		for(int i = 0; i < colorSetNames.length; i++)
		{
			colorSetNames[i] = ((GmmlColorSet)colorSets.get(i)).name;
		}
		return colorSetNames;
	}
	
	public void setGexReadOnly(boolean readonly)
	{
		boolean reconnect = false;
		if(con != null)
		{
			reconnect = true;
			close();
		}

		Properties gexProp = new Properties();
		try {
		gexProp.load(new FileInputStream(gexFile));
		gexProp.setProperty("readonly", Boolean.toString(readonly));
		gexProp.store(new FileOutputStream(gexFile), "HSQL Database Engine");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(reconnect)
		{
			connect();
		}
	}
	
	public void saveColorSets()
	{
		try
		{
			setGexReadOnly(false);
			Statement s = con.createStatement();
			s.execute("DELETE FROM colorSets");
			s.execute("DELETE FROM colorSetObjects");
			
			PreparedStatement sCs = con.prepareStatement(
					"INSERT INTO colorSets	" +
					"( colorSetId, name ) VALUES	" +
					"( ?, ? )"	);
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
				sCs.execute();
				Vector colorSetObjects = cs.getColorSetObjects();
				for(int j = 0; j < colorSetObjects.size(); j++)
				{
					GmmlColorSetObject cso = (GmmlColorSetObject)colorSetObjects.get(j);
					sCso.setString(1, cso.name);
					sCso.setInt(2, i);
					sCso.setString(3, cso.getCriterionString());
					sCso.execute();
				}
			}
			setGexReadOnly(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void loadColorSets()
	{
		try
		{
			colorSets = new Vector();
			Statement sCso = con.createStatement();
			ResultSet r = con.createStatement().executeQuery(
				"SELECT * FROM colorSets ORDER BY colorSetId" );
			while(r.next())
			{
				GmmlColorSet cs = new GmmlColorSet(r.getString(2));
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
	
	public HashMap getDataHash(ArrayList refIds)
	{
		HashMap dataHash = new HashMap();
		if(con != null && gmmlGdb.con != null) {
			try 
			{	
				for(int i = 0; i < refIds.size(); i++) {
					ResultSet r = con.createStatement().executeQuery(
							"SELECT data, idSample FROM expression " +
							"WHERE id = '" + (String)refIds.get(i) + "'"
					);
					
					while(r.next())
					{
						try {
							dataHash.put(r.getInt(2), r.getString(1));
						} catch (Exception e) {
							System.out.println("(GmmlGex:getDataHash:datatype not of type double: " + e.getMessage());
						}
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return dataHash;
	}
	
	public String getDataString(String id) {
		String exprInfo = "<P><B>" + id + "</B><TABLE border='1'>";
		if(con != null && gmmlGdb.con != null) {
			try 
			{				
//				More complicated query, slower (~150 seconds)
//				ArrayList refs = getCrossRefs(id);
				ArrayList ensIds = gmmlGdb.ref2EnsIds(id);
				for(int j = 0; j < ensIds.size(); j++)
				{
					ArrayList refs = gmmlGdb.ensId2Refs((String)ensIds.get(j));
					
//					StringBuilder ensString = new StringBuilder();
//					for(int i = 0; i < refs.size(); i++) {
//					ensString.append("'" + refs.get(i) + "', ");
//					}
					
					for(int i = 0; i < refs.size(); i++) {
//						More complicated query, slower (~10 seconds)
//						ResultSet r = conGex.createStatement().executeQuery(
//						"SELECT id, data, idSample FROM expression " +
//						"WHERE id IN " +
//						"( " + ensString.substring(0,ensString.lastIndexOf(", ")) + " )"
//						);
						ResultSet r = con.createStatement().executeQuery(
								"SELECT id, data, idSample FROM expression " +
								"WHERE id = '" + (String)refs.get(i) + "'"
						);
						
						while(r.next())
						{
							String data = r.getString(2);
							ResultSet rsn = con.createStatement().executeQuery(
									"SELECT name FROM samples" +
									" WHERE idSample = " + r.getInt(3));
							rsn.next();
							String sampleName = rsn.getString(1);
							exprInfo += "<TR><TH>" + sampleName +
							"<TH>" + data;	
						}
					}
					exprInfo += "</TABLE>";
					return exprInfo;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public Vector getDataColumns()
	{
		Vector gexDataColumns = new Vector();
		try {
			ResultSet r = con.createStatement().executeQuery(
					"SELECT * FROM samples ORDER BY idSample"
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
			convertThread = new ConvertThread();
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
	
	public void convertGex()
	{
    	PrintWriter error = null;
	    try {
	        error = new PrintWriter(new FileWriter("convert_gex_error.txt"));
	    } catch(IOException ex) {
	        ex.printStackTrace();
	    }
	    
		connect(true);
		connectGmGex(gmGexFile);
		createTables();
		try {
			con.setAutoCommit(false);
			Statement s = conGmGex.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			PreparedStatement pstmt = con.prepareStatement(
					"INSERT INTO expression			" +
					"	(id, code,					" + 
					"	 idSample, data)			" +
			"VALUES	(?, ?, ?, ?)			");
			
			ResultSet r = s.executeQuery("SELECT * FROM Expression");
			r.last();
			int nrRows = r.getRow();
			r.beforeFirst();
			
			// Select data columns
			ResultSetMetaData rsmd = r.getMetaData();
			
			// Column 4 to 2 before last contain expression data
			int nCols = rsmd.getColumnCount();
			for(int i = 4; i < nCols - 1; i++) 
			{
				String sampleName = rsmd.getColumnName(i);
				// Add new sample
				con.createStatement().execute("INSERT INTO SAMPLES" +
						"	(idSample, name)" + 
						"VALUES (" + (i - 4) + ",'" + sampleName + "')");
			}

			int nq = 0;
			String id = "";
			String code = "";
			while(r.next()) {
				// Interrupt
				if(convertThread.isInterrupted) {
					closeGmGex();
					close();
					return;
				}
				id = r.getString(2);
				if(gmmlGdb.ref2EnsIds(id).size() > 0) {
					code = r.getString(3);
					for(int i = 4; i < nCols - 1; i++) {
						try {
							pstmt.setString(1,id);
							pstmt.setString(2,code);
							pstmt.setInt(3,(i - 4));
							pstmt.setString(4,r.getString(i));
							pstmt.execute();
						} catch (Exception e) {
							error.println(id + ", " + code + ", " + (i-4) + "\t" + e.getMessage());
						}
					}
				} else {
					error.println(id + "\tGene not found in gene database");
				}
				nq++;
				if(nq % 1000 == 0)
					con.commit();
					convertThread.progress += 100/nrRows;
			}
			con.commit();	
		} catch(Exception e) {
			error.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		closeGmGex();
		close();

		setGexReadOnly(true);
		
		convertThread.progress = 100;
	}
	
	public void connect(boolean clean)
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
	public void connect()
	{
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Properties prop = new Properties();
			prop.setProperty("user","sa");
			prop.setProperty("password","");
			//prop.setProperty("hsqldb.default_table_type","cached");
			String file = gexFile.getAbsolutePath().toString();
			con = DriverManager.getConnection("jdbc:hsqldb:file:" + 
					file.substring(0,file.lastIndexOf(".")), prop);
		} catch(Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		if(con != null)
		{
			try
			{
				Statement sh = con.createStatement();
				sh.executeQuery("SHUTDOWN"); // required, to write last changes
				sh.close();
				con = null;
			} catch (Exception e) {
				System.out.println ("Error: " +e.getMessage());
				e.printStackTrace();
			}
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
	
	public void createTables() {	
		try {
			con.setReadOnly(false);
			Statement sh = con.createStatement();
			sh.execute("DROP TABLE samples IF EXISTS");
			sh.execute("DROP TABLE expression IF EXISTS");
			sh.execute("DROP TABLE colorSets IF EXISTS");
			sh.execute("DROP TABLE colorSetObjects IF EXISTS");
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
			sh.execute(
					"CREATE CACHED TABLE				" +
					"		colorSets					" +
					"(	colorSetId INTEGER PRIMARY KEY,	" +
					"	name VARCHAR(50)	)");
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
