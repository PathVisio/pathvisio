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
package org.pathvisio.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;

import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.ProgressKeeper;

public class GexTxtImporter 
{
	/**
	 * Imports expression data from a text file and saves it to an hsqldb expression database
	 * @param info		{@link GexImportWizard.ImportInformation} object that contains the 
	 * information needed to import the data
	 * @param p	{@link ProgressKeeper} that reports the progress of the process and enables
	 * the user to cancel
	 */
	public static void importFromTxt(ImportInformation info, ProgressKeeper p)
	{
		int importWork = (int)(p.getTotalWork() * 0.8);
		int finalizeWork = (int)(p.getTotalWork() * 0.2);
		
//		Open a connection to the error file
		String errorFile = info.dbName + ".ex.txt";
		int errors = 0;
		PrintStream error = null;
		try {
			File ef = new File(errorFile);
			ef.getParentFile().mkdirs();
			error = new PrintStream(errorFile);
		} catch(IOException ex) {
			p.report("Error: could not open exception file: " + ex.getMessage());
			error = System.out;
		}
		
		StopWatch timer = new StopWatch();
		try 
		{
			p.report("\nCreating expression dataset");
						
			//Create a new expression database (or overwrite existing)
			Gex.connect(info.dbName, true, false);
			
			p.report("Importing data");
			p.report("> Processing headers");
			
			timer.start();
			
			BufferedReader in = new BufferedReader(new FileReader(info.getTxtFile()));
			//Get the number of lines in the file (for progress)
			int nrLines = FileUtils.getNrLines(info.getTxtFile().toString());
			
			String[] headers = info.getColNames();
			//Parse sample names and add to Sample table
			PreparedStatement pstmt = Gex.getCurrentGex().getCon().prepareStatement(
					" INSERT INTO SAMPLES " +
					"	(idSample, name, dataType)  " +
			" VALUES (?, ?, ?)		  ");
			int sampleId = 0;
			ArrayList<Integer> dataCols = new ArrayList<Integer>();
			for(int i = 0; i < headers.length; i++)
			{
				if(p.isCancelled())
				{
					//User pressed cancel  
					Gex.getCurrentGex().close(true);
					error.close();
					return;
				}

				//skip the gene and systemcode column if there is one
				if(
					(info.getSyscodeColumn() && i != info.idColumn && i != info.codeColumn) ||
					(!info.getSyscodeColumn() && i != info.idColumn)
					)
				{ 
					try {
						pstmt.setInt(1, sampleId++);
						pstmt.setString(2, headers[i]);
						pstmt.setInt(3, info.isStringCol(i) ? Types.CHAR : Types.REAL);
						pstmt.execute();
						dataCols.add(i);
					}
					catch(Error e) { 
						errors = reportError(info, error, "Error in headerline, can't add column " + i + 
							" due to: " + e.getMessage(), errors);
						
					}
				}
			}
			
			p.report("> Processing lines");
			
			//Check ids and add expression data
			for(int i = 1; i < info.firstDataRow; i++) in.readLine(); //Go to line where data starts
			pstmt = Gex.getCurrentGex().getCon().prepareStatement(
					"INSERT INTO expression			" +
					"	(id, code, ensId,			" + 
					"	 idSample, data, groupId)	" +
			"VALUES	(?, ?, ?, ?, ?, ?)			");
			String line = null;
			int n = info.firstDataRow - 1;
			int added = 0;
			int worked = importWork / nrLines;
			
			boolean maximumNotSet = true;
			boolean minimumNotSet = true;
			double maximum = 1; // Dummy value
			double minimum = 1; // Dummy value

			while((line = in.readLine()) != null) 
			{
				if(p.isCancelled()) 
				{ 
					Gex.getCurrentGex().close(); 
					error.close(); 
					return; 
				} //User pressed cancel
				String[] data = line.split(info.getDelimiter(), headers.length);
				n++;
				if(n == info.headerRow) continue; //Don't add header row (very unlikely that this will happen)
				if(data.length < headers.length) {
					errors = reportError(info, error, "Number of columns in line " + n + 
							"doesn't match number of header columns",
							errors);
					continue;
				}
				p.setTaskName("Importing expression data - processing line " + n + "; " + errors + " exceptions");
				//Check id and add data
				String id = data[info.idColumn].trim();
				
				/*Set the system code to the one found in the dataset if there is a system code column,
				 * otherwise set the system code to the one selected (either by the user or by regular 
				 * expressions.*/
				DataSource ds;
				if (info.getSyscodeColumn()) 
				{
					ds = DataSource.getBySystemCode(data[info.codeColumn].trim());
				}
				else 
				{
					ds = info.getDataSource();
				}
				Xref ref = new Xref (id, ds);
				//Find the Ensembl genes for current gene
				ArrayList<String> ensIds = SimpleGdb.getCurrentGdb().ref2EnsIds(ref); 
				
				if(ensIds.size() == 0) //No Ensembl gene found
				{
					errors = reportError(info, error, "Line " + n + ":\t" + ref + 
							"\tNo Ensembl gene found for this identifier", errors);
				} else { //Gene maps to an Ensembl id, so add it
					boolean success = true;
					for( String ensId : ensIds) //For every Ensembl id add the data
					{
						for(int col : dataCols)
						{
							if(!info.isStringCol(col) 
									&& (data[col] == null || data[col].equals(""))) {
								data[col] = "NaN";
							}
							
							//Determine maximum and minimum values.
							
							double dNumber = new Double(data[col]).doubleValue();
							if(maximumNotSet || dNumber>maximum)
							{
								maximum=dNumber;
								maximumNotSet=false;
							}
							
							if(minimumNotSet || dNumber<minimum)
							{
								minimum=dNumber;
								minimumNotSet=false;
							}
							
							//End of determining maximum and minimum values. After the data has been read, 
							//maximum and minimum will have their correct values.
							
							try 
							{
								pstmt.setString(1, id);
								pstmt.setString(2, ds.getSystemCode());
								pstmt.setString(3, ensId);
								pstmt.setString(4, Integer.toString(dataCols.indexOf(col)));
								pstmt.setString(5, data[col]);
								pstmt.setInt(6, added);
								pstmt.execute();
							} 
							catch (Exception e) 
							{
								errors = reportError(info, error, "Line " + n + ":\t" + line + "\n" + 
										"\tException: " + error, errors);
								success = false;
							}
						}
					}
					if(success) added++;
				}
				p.worked(worked);
			}
			
			//Data is read and written to the database
			
			//Writing maximum and minimum to ImportInformation
			info.setMaximum(maximum);
			info.setMinimum(minimum);
			
			p.report(added + " genes were added succesfully to the expression dataset");
			if(errors > 0) {
				p.report(errors + " exceptions occured, see file '" + errorFile + "' for details");
			} else {
				new File(errorFile).delete(); // If no errors were found, delete the error file
			}
			p.setTaskName("Closing database connection");
			Gex.getCurrentGex().close(true);
			p.worked(finalizeWork);
			
			error.println("Time to create expression dataset: " + timer.stop());
			error.close();
			
			try 
			{
				Gex.connect(); //re-connect and use the created expression dataset
			} catch(Exception e) {
				Logger.log.error("Exception on connecting expression dataset from import thread", e);
			}

			p.finished();
		} 
		catch(Exception e) 
		{ 
			p.report("Import aborted due to error: " + e.getMessage());
			Logger.log.error("Expression data import error", e);
			Gex.getCurrentGex().close(true);
			error.close();
		}
	}

	private static int reportError(ImportInformation info, PrintStream out, String message, int nrError) 
	{
		info.addError(message);
		out.println(message);
		nrError++;
		return nrError;
	}
	

}
