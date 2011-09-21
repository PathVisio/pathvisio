// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.gexplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Types;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.debug.StopWatch;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.SimpleGex;
import org.pathvisio.gexplugin.ImportInformation.ColumnType;

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
	public static void importFromTxt(ImportInformation info, ProgressKeeper p, IDMapper currentGdb, GexManager gexManager)
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
		String errorFile = info.getGexName() + ".ex.txt";
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
			result = new SimpleGex(info.getGexName(), true, gexManager.getDBConnector());

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
			List<Integer> dataCols = new ArrayList<Integer>();
			for(int i = 0; i < headers.length; i++)
			{
				if(p != null && p.isCancelled())
				{
					//User pressed cancel
					result.close();
					error.close();
					return;
				}

				ColumnType type = info.getColumnType(i);
				//skip the id and systemcode column if there is one
				if(type == ColumnType.COL_NUMBER ||	type == ColumnType.COL_STRING)
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
							type == ColumnType.COL_STRING ? Types.CHAR : Types.REAL);
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
			for(int i = 0; i < info.getFirstDataRow(); i++) in.readLine(); //Go to line where data starts
			String line = null;
			int n = info.getFirstDataRow();
			int added = 0;
			int worked = importWork / nrLines;

			boolean maximumNotSet = true;
			boolean minimumNotSet = true;
			double maximum = 1; // Dummy value
			double minimum = 1; // Dummy value

			NumberFormat nf = NumberFormat.getInstance(
					info.digitIsDot() ? Locale.US : Locale.FRANCE);
			
			info.dataRowsImported = 0;
			info.rowsMapped = 0;
			
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
				info.dataRowsImported++;
				
				if (p != null) p.setTaskName("Importing expression data - processing line " + n + "; " + errors + " exceptions");
				//Check id and add data
				String id = data[info.getIdColumn()].trim();

				/*Set the system code to the one found in the dataset if there is a system code column,
				 * otherwise set the system code to the one selected (either by the user or by regular
				 * expressions.*/
				DataSource ds;
				if (info.isSyscodeFixed())
				{
					ds = info.getDataSource();
				}
				else
				{
					ds = DataSource.getBySystemCode(data[info.getSyscodeColumn()].trim());
				}
				Xref ref = new Xref (id, ds);

				//check if the ref exists
				boolean refExists = currentGdb.xrefExists(ref);

				if(!refExists)
				{
					errors = reportError(info, error, "Line " + n + ":\t" + ref +
							"\tError: Could not look up this identifier in the synonym database", errors);
				} else {
					errors = reportError(info, error, "Line " + n + ":\t" + ref +
							"\t", errors - 1); // decrement counter to count only the errors
					info.rowsMapped++;
				}
				// add row anyway
				{
					boolean success = true;
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
							double dNumber = nf.parse(value.toUpperCase()).doubleValue();
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
							result.addExpr(
									ref,
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
					if(success) added++;
				}
				if (p != null) p.worked(worked);
			}
			in.close();

			//Data is read and written to the database

			//Writing maximum and minimum to ImportInformation
			info.setMaximum(maximum);
			info.setMinimum(minimum);

			if (p != null) p.report(added + " rows of data were imported succesfully");
			if(errors > 0)
			{
				if (p != null) p.report(errors + " exceptions occured, see file '" + errorFile + "' for details");
			} else {
				new File(errorFile).delete(); // If no errors were found, delete the error file
			}
			if (p != null)
			{
				p.setTaskName("Finalizing database (this may take some time)");
				p.report ("Finalizing database");
			}

			result.finalize();
			if (p != null) p.worked(finalizeWork);

			error.println("Time to create expression dataset: " + timer.stop());
			error.close();

			gexManager.setCurrentGex(result.getDbName(), false);
			if (p != null)
			{
				p.setTaskName ("Done.");
				p.report ("Done.");
				p.finished();
			}
		}
		catch(Exception e)
		{
			if (p != null) p.report("Import aborted due to error: " + e.getMessage());
			Logger.log.error("Expression data import error", e);
			try
			{
				result.close();
			}
			catch (IDMapperException f)
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
