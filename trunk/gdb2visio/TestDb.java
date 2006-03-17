import java.io.*;
import java.sql.*;
import java.util.*;

public class TestDb {
	final static String database_after = ";DriverID=22;READONLY=true";
	final static String database_before =
		"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
	
	volatile boolean isInterrupted;
	public double progress;
	
	File gdbFile;
	File gexFile;
	File mappFile;
	File exprFile;
	
	Connection conGdb;
	Connection conGex;
	Connection conHdb;
	
	String[][] mappData;
	
	public TestDb() {
		isInterrupted = false;	
	}
	
	public long loadGdbTest() {
		// Check the starttime
		long startTime = System.currentTimeMillis();
		// Load the gdb
		loadGdb();
		// Check the endtime
		long endTime = System.currentTimeMillis();
		// Check if the method is interrupted
		if(isInterrupted) {
			isInterrupted = false;
			return -1;
		} else {
			return endTime - startTime;
		}
	}

	public long loadGexTest() {
		// Check the starttime
		long startTime = System.currentTimeMillis();
		// Load the gex
		loadGex();
		// Check the endtime
		long endTime = System.currentTimeMillis();
		// Check if the method is interrupted
		if(isInterrupted) {
			isInterrupted = false;
			return -1;
		} else {
			return endTime - startTime;
		}
	}
	
	public long loadMappTest() {
		// Check if a mapp file is specified
		if(mappFile != null) {
			// Load the mapp
			mappData = FileIO.loadGmml(mappFile);
		} else {
			// Get the standard mapp data
			standardMapp();
		}
		// Check the starttime
		long startTime = System.currentTimeMillis();
		// Load the data into the database
		loadMapp();
		// Check the endtime
		long endTime = System.currentTimeMillis();
		// Check if the method is interrupted
		if(isInterrupted) {
			isInterrupted = false;
			return -1;
		} else {
			return endTime - startTime;
		}
	}
	
	public void loadGdb() {
		progress = 0;
		System.out.println ("Info:  Fetching data from gdb");
		try
		{
			Statement s = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet r = s.executeQuery(
					"SELECT * FROM relations"
				);
			r.last();
			int nrRelations = r.getRow();
			r.beforeFirst();
			
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
				rFetch.last();
				int nrGenes = rFetch.getRow();
				rFetch.beforeFirst();
				
				System.out.println ("Debug: working on table " + tableName);
				
				while (rFetch.next())
				{
					// This can take long, so make it possible to interrupt
					if(isInterrupted) {
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
					// Update progress monitor
					progress += (100 / (double)(nrGenes * nrRelations));
				}
				
				// Fill gene table
				// Get the tables with gene information
				Statement gts = conGdb.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet gtr = s.executeQuery(
						"SELECT * FROM systems"
					);
				
				PreparedStatement cgPstmt = conHdb.prepareStatement(
						"INSERT INTO gene " +
						"	(id, code," +
						"	 backpageText)" +
						"VALUES (?, ?, ?)");
				
			}
		} catch (Exception e)
		{
			//System.out.println ("Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	public void loadGex() {
		System.out.println ("Info:  Fetching data from gex");
		try
		{
			Statement s = conGex.createStatement();
			ResultSet r = s.executeQuery (
				"SELECT * FROM Expression"
			);
			
			int cCol = r.getMetaData().getColumnCount();
			int i;

			PreparedStatement pstmt = conHdb.prepareStatement(
				"INSERT INTO samples			" +
				"	(idSample, name)	 		" + 
				"VALUES	(?, ?)					");

			for (i = 4; i < cCol; ++i)
			{
				String sampleName = r.getMetaData().getColumnName(i);					
								
				//~ System.out.println (i + "\t" + sampleName + "\n");
				
				pstmt.setInt (1, i);
				pstmt.setString (2, sampleName);
				
				pstmt.execute();
			}
			
			PreparedStatement pstmtExpression = conHdb.prepareStatement(
				"INSERT INTO expression			" +
				"	(id, code, idSample, data)	" + 
				"VALUES	(?, ?, ?, ?)			");

			while (r.next())
			{
				int row = r.getRow();
				if (row % 100 == 0)
				{
					System.out.println ("Debug: Working on row " + row);
				}
				String id = r.getString("ID");
				String code = r.getString("SystemCode");
				
				// from 4 to one before last, contains expression data
				for (i = 4; i < cCol; ++i)
				{
					float datum = r.getFloat (i);
					
					// TODO insert data into temporary new database
					
					pstmtExpression.setString (1, id);
					pstmtExpression.setString (2, code);
					pstmtExpression.setInt (3, i);
					pstmtExpression.setFloat (4, datum);
					
					pstmtExpression.execute();
					
					//~ System.out.println (id + "\t" + code + "\t" + i + "\t" + datum + "\n");
				};
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	public void loadExprData() {
		String[][] exprData = FileIO.loadExprData(exprFile);
		// For every row:
		// -> Check if the geneID is in the link table
		// -> 
	}
	
	public void loadMapp() {
		System.out.println ("Info:  Loading mapp data");
		try {
			PreparedStatement pstmtGene = conHdb.prepareStatement(
				"INSERT INTO mapp " +
				"	(id, code)	" + 
				"VALUES	(?, ?)			");
			
			
			System.out.println (mappData.length);
			for (int i = 0; i < mappData.length; ++i)
			{
				System.out.println(mappData[i][0] + "\t" + mappData[i][1]);
				pstmtGene.setString (1, mappData[i][1]);
				pstmtGene.setString (2, mappData[i][0]);
				
				pstmtGene.execute();
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	public void createGdbTextTable() {
		try {
			// Try to delete existing text file
			File textTable = new File("testgdb.txt");
			textTable.delete();
			Statement sh = conHdb.createStatement();
			sh.executeQuery("DROP TABLE link IF EXISTS");
			sh.executeQuery(
					"CREATE TEXT TABLE						" +
					"		link							" +
					" (   idLeft VARCHAR(50) NOT NULL,		" +
					"     codeLeft VARCHAR(50) NOT NULL,	" +
					"     idRight VARCHAR(50) NOT NULL,		" +
					"     codeRight VARCHAR(50) NOT NULL,	" +
					"     bridge VARCHAR(50),				" +
					"     PRIMARY KEY (idLeft, codeLeft,    " +
					"		idRight, codeRight) 			" +
			" )										");
			sh.executeQuery("SET TABLE link SOURCE \"testgdb.txt\"");
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void createTables() {
		System.out.println ("Info:  Creating tables");
		try
		{
			Statement sh = conHdb.createStatement();
			sh.executeQuery("DROP TABLE link IF EXISTS");
			sh.executeQuery(
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
			sh.executeQuery("DROP TABLE samples IF EXISTS");
			sh.executeQuery(
					"CREATE CACHED TABLE                           " +
					"		samples							" +
					" (   idSample INTEGER PRIMARY KEY,		" +
					"     name VARCHAR(50)					" +
			" )										");
			sh.executeQuery("DROP TABLE expression IF EXISTS");
			sh.executeQuery(
					"CREATE CACHED TABLE							" +
					"		expression						" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     idSample INTEGER,					" +
					"     data REAL,						" +
					"     PRIMARY KEY (id, code, idSample)	" +
			" )										");
			sh.executeQuery("DROP TABLE gene IF EXISTS");
			sh.executeQuery(
					"CREATE CACHED TABLE							" +
					"		gene							" +
					" (   id VARCHAR(50),					" +
					"     code VARCHAR(50),					" +
					"     backpageText VARCHAR,				" +
					"     PRIMARY KEY (id, code)			" +
			" )										");
			sh.executeQuery("DROP TABLE mapp IF EXISTS");
			sh.executeQuery(
					"CREATE CACHED TABLE							" +
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
	
	public void connect(boolean gdb, boolean gex) {
		try {
			// Load Sun's jdbc-odbc driver
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			if(gdb) {
				conGdb = DriverManager.getConnection(
						database_before + gdbFile.toString() + database_after, "", "");
			}
			if(gex) {
				conGex = DriverManager.getConnection(
						database_before + gexFile.toString() + database_after, "", "");
			}
			Class.forName("org.hsqldb.jdbcDriver" );
			conHdb = DriverManager.getConnection("jdbc:hsqldb:file:HsDb", "sa", "");
		}
		catch (Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
		} 
	}
	
	public void close()
	{
		System.out.println ("Info:  Closing all connections");
		try
		{
			if(conGdb != null) {
				conGdb.close();
			}

			Statement sh = conHdb.createStatement();
			sh.executeQuery("SHUTDOWN"); // required, to write last changes
			
			conHdb.close();
			
			if(conGex != null) {
				conGex.close();
			}
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void standardMapp() {
		mappData = new String[][] {
//			 from: tutorial 2 mapp
			{"M", "MGI:101845"},
			{"M", "MGI:101934"},
			{"M", "MGI:101941"},
			{"M", "MGI:103012"},
			{"M", "MGI:103075"},
			{"M", "MGI:103197"},
			{"M", "MGI:103198"},
			{"M", "MGI:103199"},
			{"M", "MGI:103300"},
			{"M", "MGI:104556"},
			{"M", "MGI:104565"},
			{"M", "MGI:104738"},
			{"M", "MGI:104772"},
			{"M", "MGI:104779"},
			{"M", "MGI:105091"},
			{"M", "MGI:105380"},
			{"M", "MGI:107202"},
			{"M", "MGI:107799"},
			{"M", "MGI:108028"},
			{"M", "MGI:108042"},
			{"M", "MGI:108069"},
			{"M", "MGI:108086"},
			{"M", "MGI:108109"},
			{"M", "MGI:1096340"},
			{"M", "MGI:1096341"},
			{"M", "MGI:1097691"},
			{"M", "MGI:1100510"},
			{"M", "MGI:1201674"},
			{"M", "MGI:1202065"},
			{"M", "MGI:1276116"},
			{"M", "MGI:1277162"},
			{"M", "MGI:1298227"},
			{"M", "MGI:1298398"},
			{"M", "MGI:1309511"},
			{"M", "MGI:1328306"},
			{"M", "MGI:1328337"},
			{"M", "MGI:1329034"},
			{"M", "MGI:1333743"},
			{"M", "MGI:1333752"},
			{"M", "MGI:1333784"},
			{"M", "MGI:1333889"},
			{"M", "MGI:1338073"},
			{"M", "MGI:1341857"},
			{"M", "MGI:1343091"},
			{"M", "MGI:1343463"},
			{"M", "MGI:1345150"},
			{"M", "MGI:1347043"},
			{"M", "MGI:1347044"},
			{"M", "MGI:1351328"},
			{"M", "MGI:1351663"},
			{"M", "MGI:1353578"},
			{"M", "MGI:1354159"},
			{"M", "MGI:1354944"},
			{"M", "MGI:1355321"},
			{"M", "MGI:1859866"},
			{"M", "MGI:1860374"},
			{"M", "MGI:1861437"},
			{"M", "MGI:1891835"},
			{"M", "MGI:1913921"},
			{"M", "MGI:1927225"},
			{"M", "MGI:1929285"},
			{"M", "MGI:2137630"},
			{"M", "MGI:2146156"},
			{"M", "MGI:2154049"},
			{"M", "MGI:2183443"},
			{"M", "MGI:87859"},
			{"M", "MGI:88298"},
			{"M", "MGI:88311"},
			{"M", "MGI:88314"},
			{"M", "MGI:88315"},
			{"M", "MGI:88316"},
			{"M", "MGI:88350"},
			{"M", "MGI:88351"},
			{"M", "MGI:88354"},
			{"M", "MGI:88357"},
			{"M", "MGI:894293"},
			{"M", "MGI:96952"},
			{"M", "MGI:97503"},
			{"M", "MGI:97621"},
			{"M", "MGI:97874"},
			{"M", "MGI:98725"},
			{"M", "MGI:98834"},
			{"M", "MGI:99701"},
			{"S", "MD22_MOUSE"},
			{"S", "O43171"},
			{"S", "O43183"}};	
			/*
			{"L", "10013"},
			{"L", "10014"},
			{"L", "1017"},
			{"L", "1019"},
			{"L", "1021"},
			{"L", "1026"},
			{"L", "1029"},
			{"L", "10459"},
			{"L", "10744"},
			{"L", "10926"},
			{"L", "1111"},
			{"L", "11138"},
			{"L", "11200"},
			{"L", "146059"},
			{"L", "1647"},
			{"L", "1869"},
			{"L", "1870"},
			{"L", "1871"},
			{"L", "1874"},
			{"L", "1875"},
			{"L", "1876"},
			{"L", "2033"},
			{"L", "219972"},
			{"L", "23594"},
			{"L", "23595"},
			{"L", "25"},
			{"L", "26255"},
			{"L", "2932"},
			{"L", "3065"},
			{"L", "3066"},
			{"L", "4085"},
			{"L", "4088"},
			{"L", "4089"},
			{"L", "4171"},
			{"L", "4172"},
			{"L", "4173"},
			{"L", "4174"},
			{"L", "4175"},
			{"L", "4176"},
			{"L", "4193"},
			{"L", "472"},
			{"L", "4998"},
			{"L", "4999"},
			{"L", "5000"},
			{"L", "5001"},
			{"L", "5111"},
			{"L", "51564"},
			{"L", "5347"},
			{"L", "55869"},
			{"L", "5591"},
			{"L", "5925"},
			{"L", "5933"},
			{"L", "6502"},
			{"L", "699"},
			{"L", "701"},
			{"L", "7027"},
			{"L", "7040"},
			{"L", "7157"},
			{"L", "7465"},
			{"L", "7532"},
			{"L", "8243"},
			{"L", "8317"},
			{"L", "8318"},
			{"L", "8379"},
			{"L", "85417"},
			{"L", "8555"},
			{"L", "8556"},
			{"L", "8841"},
			{"L", "890"},
			{"L", "8900"},
			{"L", "891"},
			{"L", "894"},
			{"L", "896"},
			{"L", "898"},
			{"L", "902"},
			{"L", "9133"},
			{"L", "9134"},
			{"L", "9184"},
			{"L", "9232"},
			{"L", "9700"},
			{"L", "9759"},
			{"L", "983"},
			{"L", "990"},
			{"L", "991"},
			{"L", "993"},
			{"L", "994"},
			{"L", "995"},
			{"L", "999"},
			{"O", "LL:23665"},
			{"S", "A1AU_HUMAN"},
			{"S", "O14731"}};
			*/
	}
	
}