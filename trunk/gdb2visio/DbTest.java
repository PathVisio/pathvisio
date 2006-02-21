import java.io.*;
import java.sql.*;

class DbTest
{
	
	static String database_after = ";DriverID=22;READONLY=true";
    static String database_before =
            "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";

	static String fnGdbNew = "d:/GenMAPP data/Tutorial-Database-converted.mdb";
	static String fnGdb = "d:/GenMAPP data/Tutorial-Database.gdb";
	static String fnGmml = "d:/prg/gmml-visio/trunk/gmml_mapp_examples/Hs_Apoptosis.xml";
	static String fnGex = "d:/GenMAPP data/Tutorial-ExpressionData.gex";
	
	Connection conGdb;
	Connection conGex;
	
	/**
		constructor
	
		tries to create Connection objects
	*/
	DbTest()
	{
        try {
			// Load Sun's jdbc-odbc driver
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conGdb = DriverManager.getConnection(
				database_before + fnGdb + database_after, "", "");
			
			conGex = DriverManager.getConnection(
				database_before + fnGex + database_after, "", "");

		} 
		catch (Exception e) {
			System.out.println ("Error: " +e.getMessage());
			e.printStackTrace();
		} 
    }

	void go()
	{
		// first we fetch data from gdb
		try
		{
			Statement s = conGdb.createStatement();
			ResultSet r = s.executeQuery(
					"SELECT * FROM relations");
			while (r.next())
			{
				String codeLeft = r.getString("SystemCode");
				String codeRight = r.getString("RelatedCode");
				String tableName = r.getString("Relation");
				
				Statement sFetch = conGdb.createStatement();
				ResultSet rFetch = sFetch.executeQuery("SELECT * FROM `" + tableName + "`");
				
				while (rFetch.next())
				{
					String idLeft = rFetch.getString ("Primary");
					String idRight = rFetch.getString ("Related");
					String bridge = rFetch.getString ("Bridge");
					
					//System.out.println (idLeft + "\t" + codeLeft + "\t" + idRight + "\t" + codeRight + "\t" + bridge + "\n");
					// TODO insert the data in temporary new database

					/*
					"INSERT INTO `link`
						(`idLeft`, `codeLeft`, `idRight`, `codeRight`, `bridge`)
					 VALUES
						(?, ?, ?, ?, ?)
						";
					*/

					}
				
			}
		} catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
		
		// now we fetch data from gex
		
		try
		{
			Statement s = conGex.createStatement();
			
			ResultSet r = s.executeQuery (
				"SELECT * FROM Expression"
			);
			
			int cCol = r.getMetaData().getColumnCount();
			
			int i;

			for (i = 4; i < cCol; ++i)
			{
				String sampleName = r.getMetaData().getColumnName(i);					
				
				
				/*
				"INSERT INTO `samples`
					(`idSample`, `name`)
				VALUES
					(?, ?)
				*/
				
				System.out.println (i + "\t" + sampleName + "\n");
			}
			
			while (r.next())
			{
				String id = r.getString("ID");
				String code = r.getString("SystemCode");
				

				// from 4 to one before last, contains expression data				

				
				for (i = 4; i < cCol; ++i)
				{
					float datum = r.getFloat (i);
					
					// TODO insert data into temporary new database
					
					/*
					"INSERT INTO `expression`
						(`id`, `code`, `sample`, `data`)
					 VALUES
						(?, ?, ?, ?)
						";
					*/
					
					System.out.println (id + "\t" + code + "\t" + i + "\t" + datum + "\n");
				};
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		DbTest x = new DbTest();
		x.go();
	}
}