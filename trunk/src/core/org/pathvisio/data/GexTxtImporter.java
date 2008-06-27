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
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.debug.Logger;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.ProgressKeeper;

/**
 * Functions to create a new Gex database
 * based on a text file.
 */
public class GexTxtImporter 
{
	/**
	 * Imports expression data from a text file and saves it to an hsqldb expression database
	 * @param info		{@link GexImportWizard.ImportInformation} object that contains the 
	 * information needed to import the data
	 * @param p	{@link ProgressKeeper} that reports the progress of the process and enables
	 * the user to cancel. May be null for headless mode operation.
	 */
	public static void importFromTxt(ImportInformation info, ProgressKeeper p, Gdb currentGdb)
	{
		SimpleGex result = null;
		int importWork = 0;
		int finalizeWork = 0;
		if (p != null)
		{
			importWork = (int)(p.getTotalWork() * 0.8);
			finalizeWork = (int)(p.getTotalWork() * 0.2);
		}
		
//		Open a connection to the error file
		String errorFile = info.getDbName() + ".ex.txt";
		int errors = 0;
		PrintStream error = null;
		try {
			File ef = new File(errorFile);
			ef.getParentFile().mkdirs();
			error = new PrintStream(errorFile);
		} catch(IOException ex) {
			if (p != null) p.report("Error: could not open exception file: " + ex.getMessage());
			error = System.out;
		}
		
		StopWatch timer = new StopWatch();
		try 
		{
			if (p != null) p.report("\nCreating expression dataset");
						
			//Create a new expression database (or overwrite existing)
			result = new SimpleGex(info.getDbName(), true, GexManager.getCurrent().getDBConnector());
			
			if (p != null)
			{
				p.report("Importing data");
				p.report("> Processing headers");
			}
			timer.start();
			
			BufferedReader in = new BufferedReader(new FileReader(info.getTxtFile()));
			//Get the number of lines in the file (for progress)
			int nrLines = FileUtils.getNrLines(info.getTxtFile().toString());
			
			String[] headers = info.getColNames();
			//Parse sample names and add to Sample table
			result.prepare();
			int sampleId = 0;
			ArrayList<Integer> dataCols = new ArrayList<Integer>();
			for(int i = 0; i < headers.length; i++)
			{
				if(p != null && p.isCancelled())
				{
					//User pressed cancel  
					result.close();
					error.close();
					return;
				}

				//skip the gene and systemcode column if there is one
				if(
					(info.getSyscodeColumn() && i != info.getIdColumn() && i != info.getCodeColumn()) ||
					(!info.getSyscodeColumn() && i != info.getIdColumn())
					)
				{ 
					String header = headers[i];
					if (header.length() >= 50)
					{
						header = header.substring(0, 49);
					}
					try {
						result.addSample(
							sampleId++, 
							header, 
							info.isStringCol(i) ? Types.CHAR : Types.REAL);
						dataCols.add(i);
					}
					catch(Error e) { 
						errors = reportError(info, error, "Error in headerline, can't add column " + i + 
							" due to: " + e.getMessage(), errors);
						
					}
				}
			}
			
			if (p != null) p.report("> Processing lines");
			
			//Check ids and add expression data
			for(int i = 1; i < info.getFirstDataRow(); i++) in.readLine(); //Go to line where data starts
			String line = null;
			int n = info.getFirstDataRow() - 1;
			int added = 0;
			int worked = importWork / nrLines;
			
			boolean maximumNotSet = true;
			boolean minimumNotSet = true;
			double maximum = 1; // Dummy value
			double minimum = 1; // Dummy value

			DecimalFormat nf = new DecimalFormat();
			DecimalFormatSymbols dfs = nf.getDecimalFormatSymbols();
			DecimalFormat df = new DecimalFormat();
			if (info.digitIsDot())
			{
				dfs.setGroupingSeparator('.');
				dfs.setDecimalSeparator(',');
			}

			while((line = in.readLine()) != null) 
			{
				if(p != null && p.isCancelled()) 
				{ 
					result.close(); 
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
				if (p != null) p.setTaskName("Importing expression data - processing line " + n + "; " + errors + " exceptions");
				//Check id and add data
				String id = data[info.getIdColumn()].trim();
				
				/*Set the system code to the one found in the dataset if there is a system code column,
				 * otherwise set the system code to the one selected (either by the user or by regular 
				 * expressions.*/
				DataSource ds;
				if (info.getSyscodeColumn()) 
				{
					ds = DataSource.getBySystemCode(data[info.getCodeColumn()].trim());
				}
				else 
				{
					ds = info.getDataSource();
				}
				Xref ref = new Xref (id, ds);
				//Find the Ensembl genes for current gene
				List<String> ensIds = currentGdb.ref2EnsIds(ref); 
				
				if(ensIds == null || ensIds.size() == 0) //No Ensembl gene found
				{
					errors = reportError(info, error, "Line " + n + ":\t" + ref + 
							"\tNo Ensembl gene found for this identifier", errors);
				} else { //Gene maps to an Ensembl id, so add it
					boolean success = true;
					for( String ensId : ensIds) //For every Ensembl id add the data
					{
						for(int col : dataCols)
						{
							String value = data[col];
							
							if(!info.isStringCol(col) 
									&& (value == null || value.equals(""))) {
								value = "NaN";
							}

							//Determine maximum and minimum values.
							
							try
							{
								double dNumber = nf.parse(value).doubleValue(); 
								value = "" + dNumber; 
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
							}
							catch (ParseException e)
							{
								// we've got a number in a non-number column.
								// safe to ignore
								Logger.log.warn ("Number format exception in non-string column " + e.getMessage());
							}
							
							//End of determining maximum and minimum values. After the data has been read, 
							//maximum and minimum will have their correct values.
							
							try 
							{
								//TODO: use autocommit (false) and commit only every 1000 queries or so. 
								result.addExpr(
										ref, 
										ensId,
										Integer.toString(dataCols.indexOf(col)),
										value,
										added);
							} 
							catch (Exception e) 
							{
								errors = reportError(info, error, "Line " + n + ":\t" + line + "\n" + 
										"\tException: " + e.getMessage(), errors);
								success = false;
							}
						}
					}
					if(success) added++;
				}
				if (p != null) p.worked(worked);
			}
			
			//Data is read and written to the database
			
			//Writing maximum and minimum to ImportInformation
			info.setMaximum(maximum);
			info.setMinimum(minimum);
			
			if (p != null) p.report(added + " genes were added succesfully to the expression dataset");
			if(errors > 0) 
			{
				if (p != null) p.report(errors + " exceptions occured, see file '" + errorFile + "' for details");
			} else {
				new File(errorFile).delete(); // If no errors were found, delete the error file
			}
			if (p != null) p.setTaskName("Closing database connection");

			result.finalize();
			if (p != null) p.worked(finalizeWork);
			
			error.println("Time to create expression dataset: " + timer.stop());
			error.close();
			
			GexManager.getCurrent().setCurrentGex(result.getDbName(), false);
			if (p != null) p.finished();
		} 
		catch(Exception e) 
		{ 
			if (p != null) p.report("Import aborted due to error: " + e.getMessage());
			Logger.log.error("Expression data import error", e);
			try
			{
				result.close();
			}
			catch (DataException f) 
			{ Logger.log.error ("Exception while aborting database", f); }
			error.close();
		}
	}

	private static int reportError(ImportInformation info, PrintStream log, String message, int nrError) 
	{
		info.addError(message);
		log.println(message);
		nrError++;
		return nrError;
	}
	

}
