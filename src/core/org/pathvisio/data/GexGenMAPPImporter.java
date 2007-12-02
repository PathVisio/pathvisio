package org.pathvisio.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.util.ProgressKeeper;

public class GexGenMAPPImporter 
{
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
	 * Converts the GenMAPP Expression Dataset to a expression database
	 * in Hsqldb format as used by this program.
	 * <BR><BR>This method reports all errors occured during the conversion to a file named 'convert_gex_error.txt'
	 */
	public static void convertGex(ProgressKeeper p)
	{
		//Open a connection to the error file
		PrintWriter error = null;
		try {
			error = new PrintWriter(new FileWriter("convert_gex_error.txt"));
		} catch(IOException ex) {
			Logger.log.error("Unable to open error file for gdb conversion: " + ex.getMessage(), ex);
		}
		
		try {
			Gex.connect(null, true, false); //Connect and delete the old database if exists
			connectGmGex(gmGexFile); //Connect to the GenMAPP gex
			
			Gex.getCurrentGex().getCon().setAutoCommit(false); //Keep control over when to commit, should increase speed
			Statement s = conGmGex.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			PreparedStatement pstmtExpr = Gex.getCurrentGex().getCon().prepareStatement(
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
				Gex.getCurrentGex().getCon().createStatement().execute("INSERT INTO SAMPLES" +
						"	(idSample, name, dataType)" + 
						"VALUES ( " + (i - 4) + ",'" + sampleName + "', " + dataType + " )");
			}
			
			//Fill the Expression table
			int nq = 0; //The number of queries excecuted
			String id = "";
			String code = "";
			while(r.next()) { //Process all rows of the expression data
				if(p.isCancelled()) //Check if the user cancelled the conversion
				{
					closeGmGex();
					Gex.getCurrentGex().close();
					return;
				}
				
				id = r.getString("ID");
				code = r.getString("SystemCode");
				ArrayList<String> ensIds = Gdb.getCurrentGdb().ref2EnsIds(new Xref (id, DataSource.getBySystemCode(code))); //Find the Ensembl genes for current gene
				
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
					Gex.getCurrentGex().getCon().commit();
				p.worked(p.getTotalWork()/nrRows); //Report progress
			}
			Gex.getCurrentGex().getCon().commit();	
		} catch(Exception e) {
			error.println("Error: " + e.getMessage());
		}
		error.println("END");
		error.close();
		closeGmGex();
		Gex.getCurrentGex().close();
		
//		setGexReadOnly(true);
		
		p.finished();
	}

	/**
	 * Connect to the GenMAPP Expression Dataset specified by the given file
	 * @param gmGexFile	File containing the GenMAPP Expression Dataset
	 */
	private static void connectGmGex(File gmGexFile) {
		String database_after = ";DriverID=22;READONLY=true";
		String database_before =
			"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conGmGex = DriverManager.getConnection(
					database_before + gmGexFile.toString() + database_after, "", "");
		} catch (Exception e) {
			Logger.log.error("Error: Unable to open connection go GenMAPP gex " + gmGexFile +
					": " +e.getMessage(), e);
		}
	}
	
	/**
	 * Close the connection to the GenMAPP Expression Dataset
	 */
	private static void closeGmGex() {
		if(conGmGex != null)
		{
			try {
				conGmGex.close();
				conGmGex = null;
			} catch (Exception e) {
				Logger.log.error("Error while closing connection to GenMAPP gex: " + e.getMessage(), e);
			}
		}
	}

}
