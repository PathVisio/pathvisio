import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.*;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class GmmlGdb {
	public Connection conGdb;
	public Connection conHdb;
	
	public File gdbFile;
	public ConvertThread convertThread;
	public File hdbFile;
	final String tempDbName = "tempdb";
	
	public Properties props = new Properties();
	
	final static File propsFile = new File(".gdbProperties");
	final static String database_after = ";DriverID=22;READONLY=true";
	final static String database_before =
		"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
	
	public GmmlGdb() {
		try {
			// Check if properties file exists
			if(propsFile.canRead()) {
				props.load(new FileInputStream(propsFile));
			} else {
				// Create properties file
				propsFile.createNewFile();
				props.setProperty("currentGdb", "none");
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
		connectHdb(null);
		try {
			Statement s = conHdb.createStatement();
			ResultSet r = s.executeQuery("SELECT backpageText FROM gene " +
					"WHERE id = '" + id + "'");
			r.next();
			return r.getString(1);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void convertGdb() {
		// Perform process in seperate thread
		convertThread = new ConvertThread();
		convertThread.start();
	}
	
	public boolean selectGdb(File hdbFile) {
		// Check if database is valid (contains link table)
		connectHdb(hdbFile);
		try {
			ResultSet rs = conHdb.getMetaData().getTables(null, null, "LINK", null);
			if (!rs.next()) {
				return false;
			}
			close();
			// Copy data file to temp gdb
			File tempDir = new File("gdb" + File.separatorChar);
			tempDir.mkdir();
			File dataFile = new File(tempDir.getName(),tempDbName + ".data");
			File scriptFile = new File(tempDir.getName(),tempDbName + ".script");
			File propertiesFile = new File(tempDir.getName(),tempDbName + ".properties");
			copyFile(new File(hdbFile.getAbsoluteFile().toString() + ".data"), dataFile);
			copyFile(new File(hdbFile.getAbsoluteFile().toString() + ".script"), scriptFile);
			copyFile(new File(hdbFile.getAbsoluteFile().toString() + ".properties"), propertiesFile);
			// Set current gdb
			setCurrentGdb(hdbFile.getAbsolutePath().toString());
			// Check if database is copied correctly
			connectHdb(null);
			ResultSet r = conHdb.getMetaData().getTables(null, null, "LINK", null);
			if(!r.next()) {
				return false;
			}
			close();
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}	
	}
	
	public void copyFile(File src, File dst) {
		try {
			System.out.println(src.getName() + " canread: " + src.canRead());
			System.out.println(dst.getName() + " canwrite: " + src.canWrite());
			// Delete possible existing dest file
			if(dst.exists()) {
				dst.delete();
			}
			FileChannel srcChannel = new FileInputStream(src).getChannel();
			FileChannel dstChannel = new FileOutputStream(dst).getChannel();
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
			srcChannel.close();
			dstChannel.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public class ConvertThread extends Thread {
		volatile int progress;
		volatile boolean isInterrupted;
		IRunnableWithProgress runnableWithProgress;
		
		public ConvertThread() {
			super();
			isInterrupted = false;			
		}
		
		public void interrupt() {
			isInterrupted = true;
		}
		
		public void run() {
			progress = 0;
			isInterrupted = false;
			connectGdb();
			connectHdb(hdbFile);
			createTables();
			doConversion();
			close();
		}
	}
	
	public void doConversion() {
		System.out.println ("Info:  Fetching data from gdb");
		try
		{
			// Fetch size of database to convert (for progress monitor)
			Statement s = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet r = s.executeQuery("SELECT COUNT(*) FROM relations");
			int nrRelations = 1;
			if(r.next()) {
				nrRelations = r.getInt(1);
				System.out.println("nrRelations " + nrRelations);
			}
			r = s.executeQuery("SELECT COUNT(*) FROM Systems");
			int nrSystems = 1;
			if(r.next()) {
				nrSystems = r.getInt(1);
				System.out.println("nrSystems " + nrSystems);
			}
			
			// Fill link table
			s = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			r = s.executeQuery(
					"SELECT * FROM relations"
			);
			PreparedStatement pstmt = conHdb.prepareStatement(
					"INSERT INTO link				" +
					"	(idLeft, codeLeft,	 		" + 
					"	 idRight, codeRight,	 	" +
					"	bridge)						" +
			"VALUES	(?, ?, ?, ?, ?)			");
			
			while (r.next())
			{		
				String codeLeft = r.getString("SystemCode");
				String codeRight = r.getString("RelatedCode");
				String tableName = r.getString("Relation");
				
				Statement sFetch = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet rFetch = sFetch.executeQuery("SELECT * FROM `" + tableName + "`");
				
				System.out.println ("Debug: working on table " + tableName);
				while (rFetch.next())
				{	
					// Check if the thread is interrupted
					if(convertThread.isInterrupted) {
						return;
					}
					String idLeft = rFetch.getString ("Primary");
					String idRight = rFetch.getString ("Related");
					String bridge = rFetch.getString ("Bridge");
					
					pstmt.setString (1, idLeft);
					pstmt.setString (2, codeLeft);
					pstmt.setString (3, idRight);
					pstmt.setString (4, codeRight);
					pstmt.setString (5, bridge);
					
					try
					{
						pstmt.execute();
					}
					catch (SQLException e)
					{
						//System.out.println ("Error: " + e.getMessage());
						//e.printStackTrace();
					}
				}
				// Update progress monitor
				convertThread.progress += 30/nrRelations;
				System.out.println(convertThread.progress);
			}
			// Fill gene table
			// Get the table names containing gene information
			r = s.executeQuery(
					"SELECT System, SystemCode FROM Systems"
			);
			
			pstmt = conHdb.prepareStatement(
					"INSERT INTO gene " +
					"	(id, code," +
					"	 backpageText)" +
			"VALUES (?, ?, ?)");
			
			// Process every system table
			while(r.next()) {
				Statement tms = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				String systemTable = r.getString("System");
				String systemCode = r.getString("SystemCode");
				// Check if table exists
				ResultSet tmr = conGdb.getMetaData().getTables(null, null, systemTable, null);
				if(tmr.next()) {
					ResultSet str = tms.executeQuery("SELECT * FROM " + systemTable);
					while(str.next()) {
						// Check if the thread is interrupted
						if(convertThread.isInterrupted) {
							return;
						}
						try {
							// Column ID is gene id
							String id = str.getString("ID");
							// All further columns are backpage text
							String bpText = "<TABLE border='1'>";
							ResultSetMetaData strm = str.getMetaData();
							for(int i = 1; i < strm.getColumnCount(); i++) {
								try {
									String colName = strm.getColumnName(i);
									String colVal = str.getString(colName);
									bpText = bpText + "<TR><TH>" +
									colName + "<TH>" + colVal;
								} catch (SQLException e) {
									System.out.println ("Error: " + e.getMessage());
									//e.printStackTrace();
								}
							}
							bpText = bpText + "</TABLE>";
							//System.out.println(id + "\t" + systemCode + "\t");
							pstmt.setString(1, id);
							pstmt.setString(2, systemCode);
							pstmt.setString(3, bpText);
							pstmt.execute();
						} catch (SQLException e) {
							//System.out.println ("Error: " + e.getMessage());
							//e.printStackTrace();
						}
					}
				}
				// Update progress monitor
				convertThread.progress += 70/nrSystems;
				System.out.println(convertThread.progress);
			}
			convertThread.progress = 100;
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean connectGdb() {
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conGdb = DriverManager.getConnection(
					database_before + gdbFile.toString() + database_after, "", "");
			return true;
		} catch (Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean connectHdb(File hdbFile) {
		if(hdbFile == null) {
			hdbFile = new File("gdb" + File.separatorChar + tempDbName);
			System.out.println(hdbFile.getAbsolutePath().toString());
		}
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Properties prop = new Properties();
			prop.setProperty("user","sa");
			prop.setProperty("password","");
//			prop.setProperty("hsqldb.log_size","1");
			//prop.setProperty("hsqldb.default_table_type","cached");
			conHdb = DriverManager.getConnection("jdbc:hsqldb:file:" + 
					hdbFile.getAbsolutePath().toString(), prop);
			return true;
		} catch(Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public void close() {
		System.out.println ("Info:  Closing all connections");
		try
		{
			if(conGdb != null) {
				conGdb.close();
			}
			if(conHdb != null) {
				Statement sh = conHdb.createStatement();
				sh.executeQuery("SHUTDOWN"); // required, to write last changes
				sh.close();
			}
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void createTables() {
		System.out.println ("Info:  Creating tables");
		
		try {
			Statement sh = conHdb.createStatement();
			sh.execute("DROP TABLE link IF EXISTS");
			sh.execute("DROP TABLE samples IF EXISTS");
			sh.execute("DROP TABLE expression IF EXISTS");
			sh.execute("DROP TABLE gene IF EXISTS");
			sh.execute("DROP TABLE mapp IF EXISTS");
		} catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
		}
		try
		{
			Statement sh = conHdb.createStatement();
			sh.execute(
					"CREATE CACHED TABLE					" +
					"		link							" +
					" (   idLeft VARCHAR(50) NOT NULL,		" +
					"     codeLeft VARCHAR(50) NOT NULL,	" +
					"     idRight VARCHAR(50) NOT NULL,		" +
					"     codeRight VARCHAR(50) NOT NULL,	" +
					"     bridge VARCHAR(50),				" +
					"     PRIMARY KEY (idLeft, codeLeft,    " +
					"		idRight, codeRight) 			" +
			" )										");
			
			sh.execute(
					"CREATE TABLE                           " +
					"		samples							" +
					" (   idSample INTEGER PRIMARY KEY,		" +
					"     name VARCHAR(50)					" +
			" )										");
			
			sh.execute(
					"CREATE TABLE							" +
					"		expression						" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     idSample INTEGER,					" +
					"     data REAL,						" +
					"     PRIMARY KEY (id, code, idSample)	" +
			" )										");
			
			sh.execute(
					"CREATE CACHED TABLE							" +
					"		gene							" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     backpageText VARCHAR,				" +
					"     PRIMARY KEY (id, code)			" +
			" )										");
			
			sh.execute(
					"CREATE TABLE							" +
					"		mapp							" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     PRIMARY KEY (id, code)			" +
			" )										");
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
