package org.pathvisio.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.util.ProgressKeeper;

/**
 * functions to convert a GenMAPP gex database into
 * a PathVisio pgex database.
 * 
 * This will probably be removed in the future.
 */
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
		
		SimpleGex result;
		try {
			result = new SimpleGex (null, true, Engine.getCurrent().getDbConnector(DBConnector.TYPE_GEX));			
			connectGmGex(gmGexFile); //Connect to the GenMAPP gex
			
			result.getCon().setAutoCommit(false); //Keep control over when to commit, should increase speed
			Statement s = conGmGex.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			result.prepare();
			
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
				result.addSample(i - 4, sampleName, dataType);
			}
			
			//Fill the Expression table
			int nq = 0; //The number of queries excecuted
			String id = "";
			String code = "";
			int row = 0;
			while(r.next()) { //Process all rows of the expression data
				if(p.isCancelled()) //Check if the user cancelled the conversion
				{
					closeGmGex();
					result.close();
					return;
				}
				
				id = r.getString("ID");
				code = r.getString("SystemCode");
				Xref ref = new Xref (id, DataSource.getBySystemCode(code));
				//Find the Ensembl genes for current gene
				List<String> ensIds = GdbManager.getCurrentGdb().ref2EnsIds(ref);
				
				if(ensIds.size() == 0) //No Ensembl gene found
				{
					error.println(id + "\t" + code + "\t No Ensembl gene found for this identifier");
				} 
				else 
				{ 
					//Gene maps to an Ensembl id, so add it
					ArrayList<String> data = new ArrayList<String>();
					for (int i = 4; i < nCols - 1; i++) 
					{ 
						// Column 4 to 2 before last contain expression data
						data.add(r.getString(i));
					}
					for (String ensId : ensIds) 
					//For every Ensembl id add the data
					{
						int i = 0;
						for(String str : data)
						{
							try 
							{
								result.addExpr (
									ref, ensId,	"" + i,	str, row);
							} 
							catch (Exception e) 
							{
								error.println(id + ", " + code + ", " + i + "\t" + e.getMessage());
							}
							i++;
						}
					}
				}
				row++;
				nq++;
				if(nq % 1000 == 0) //Commit every 1000 queries
					result.commit();
				p.worked(p.getTotalWork()/nrRows); //Report progress
			}
			result.commit();	
			error.println("END");
			error.close();
			closeGmGex();			
			GexManager.setCurrentGex (result);
		} 
		catch(Exception e) 
		{
			error.println("Error: " + e.getMessage());
		}		
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
