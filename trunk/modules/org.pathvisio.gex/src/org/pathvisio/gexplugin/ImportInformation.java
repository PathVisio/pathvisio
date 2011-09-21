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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bridgedb.DataSource;
import org.bridgedb.DataSourcePatterns;
import org.pathvisio.core.debug.Logger;

/**
 * This class contains the information needed to start importing a delimited
 * text file to an expression dataset. This information can be gathered using
 * the {@link GexImportWizard} or can be filled automatically
 */
public class ImportInformation {

	/**
	 * Points to the text file containing the expression data
	 */
	private File txtFile;

	/**
	 * Sets the text file containing the expression data
	 * @param aTxtFile {@link File} to set
	 */
	public void setTxtFile(File aTxtFile) throws IOException
	{
		if (!aTxtFile.equals(txtFile))
		{
			txtFile = aTxtFile;
			readSample();
			interpretSample();
		}
	}

	/**
	 * Get the private {@link File} txtFile
	 * @return {@link File} object pointing to the text file that contains the
	 * expression data
	 */
	public File getTxtFile() { return txtFile; }

	private String dbName;

	/**
	 * The database name in which the expression data is saved
	 */
	public void setGexName(String value)
	{
		dbName = value;
	}

	/**
	 * The database name in which the expression data is saved
	 */
	public String getGexName() { return dbName; }

	private double maximum;
	private double minimum;

	/** get/set maximum double value in this dataset */
	public double getMaximum() { return maximum; }
	public void setMaximum(double setpoint) { maximum = setpoint; }

	/** get/set minimum double value in this dataset */
	public double getMinimum() { return minimum; }
	public void setMinimum(double setpoint) { minimum = setpoint; }


	private List<String> errorList = new ArrayList<String>();

	/** Returns a list of errors made during importing data, the same list as saved
	 * in the error file (.ex.txt) */
	public List<String> getErrorList()
	{
		return errorList;
	}

	/** A error has been reported during importing data. The message is added to
	 * the list of errors. */
	public void addError(String message)
	{
		errorList.add(message);
	}

	private int firstDataRow = 1;

	/**
	 * linenumber (first line is 0) of the line where the data begins
	 */
	public int getFirstDataRow()
	{
		return firstDataRow;
	}

	/**
	 * linenumber (first line is 0) of the line where the data begins
	 */
	public void setFirstDataRow(int value)
	{
		assert (value >= 0);
		firstDataRow = value;
		if (firstHeaderRow > firstDataRow)
		{
			firstHeaderRow = firstDataRow;
		}
	}

	private int firstHeaderRow;

	/**
	 * linenumber (first line is 0) of the line where the header begins.
	 */
	public int getFirstHeaderRow()
	{
		return firstHeaderRow;
	}

	/**
	 * linenumber (first line is 0) of the line where the header begins.
	 * firstHeaderRow must be <= firstDataRow, so it will be set to the next line if that happens.
	 */
	public void setFirstHeaderRow(int value)
	{
		assert (value >= 0);
		firstHeaderRow = value;
		if (firstHeaderRow > firstDataRow)
		{
			firstDataRow = firstHeaderRow + 1;
		}
	}

	public boolean isHeaderRow (int row)
	{
		return row >= firstHeaderRow && row < firstDataRow;
	}

	public boolean isDataRow (int row)
	{
		return row >= firstDataRow;
	}

	/**
	 * linenumber (first line is 0) of the line containing the column headers
	 */
	int headerRow = 0;

	/**
	 * Column number (first column is 0) of the column containing the gene identifier
	 */
	private int idColumn = 0;
	public int getIdColumn()
	{
		return idColumn;
	}

	public void setIdColumn(int value)
	{
		idColumn = value;
	}

	private int syscodeColumn = 1;

	/**
	 * Column number (first column is 0) of the column containing the system code
	 */
	public int getSyscodeColumn()
	{
		return syscodeColumn;
	}

	/**
	 * Column number (first column is 0) of the column containing the system code
	 */
	public void setSysodeColumn (int value)
	{
		syscodeColumn = value;
	}

	/** Various possible column types */
	public enum ColumnType { COL_SYSCODE, COL_ID, COL_STRING, COL_NUMBER };

	public ColumnType getColumnType (int col)
	{
		if (col == idColumn) return ColumnType.COL_ID;
		if (isSyscodeFixed ? false : col == syscodeColumn) return ColumnType.COL_SYSCODE;
		return isStringCol(col) ? ColumnType.COL_STRING : ColumnType.COL_NUMBER;
	}

	/**
	 * Boolean which can be set to false if there is no column for the system code is available
	 * in the dataset.
	 */
	private boolean isSyscodeFixed = false;

	/**
	 * String containing the system code that has been set by the user in gexImportWizard
	 * (if no system code column is available).
	 */
	DataSource ds = null;

	/**
	 * Delimiter used to seperate columns in the text file containing expression data
	 */
	private String delimiter = "\t";

	/**
	 * Column numbers (first column is 0) of the columns of which the data should not be treated
	 * as numberic
	 */
	private Set<Integer> stringCols = new HashSet<Integer>();

	/**
	 * Set if the given column is of type String or not
	 */
	public void setStringColumn (int col, boolean value)
	{
		if (value)
			stringCols.add (col);
		else
			stringCols.remove(col);
	}

	/**
	 * Checks if the column for the given column index is marked as 'string column' and
	 * should not be treated as numeric
	 * @param colIndex	the index of the column to check (start with 0)
	 * @return true if the column is marked as 'string column', false if not
	 */
	public boolean isStringCol(int colIndex)
	{
		return stringCols.contains(colIndex);
	}

	/**
	 * Creates an excel "Column name" for a given 0-based column index.
	 * 0 -> A, 25-> Z
	 * 26 -> AA
	 * 26 + 26^2 -> AAA
	 * etc.
	 */
	static String colIndexToExcel (int i)
	{
		assert (i >= 0);
		String result = "";
		while (i >= 0)
		{
			 result = (char)('A' + i % 26) + result;
			 i /= 26;
			 i--;
		}
		return result;
	}

	/**
	 * Reads the column names from the text file containing the expression data at the
	 * header row specified by the user. Multiple header rows can also be read. When no header
	 * row is present, a header row is created manually.
	 *
	 * The column names are guaranteed to be unique, non-empty, and
	 * there will be at least as many columns as sampleMaxNumCols.
	 *
	 * @return the column names
	 */
	public String[] getColNames()
	{
		String[] result = null;
		result = new String[sampleMaxNumCols];

		// initialize columns to empty strings
		for(int i = 0; i < sampleMaxNumCols; i++) result[i] = "";

		// concatenate header rows
		if(!getNoHeader())
		{
			int i = 0;
			// Read headerlines till the first data row
			boolean first = true;
			while (i < firstDataRow)
			{
				for(int j = 0; j < cells[i].length; j++)
				{
					// All header rows are added
					if(i >= headerRow)
					{
						if (!first) result[j] += " ";
						result[j] = result[j] += cells[i][j].trim();
					}
				}
				first = false;
				i++;
			}
		}

		// check that column names are unique
		Set<String> unique = new HashSet<String>();
		// set remaining emtpy column names to default string
		for (int j = 0; j < result.length; ++j)
		{
			String col = result[j];
			if (col.equals ("") || unique.contains(col))
			{
				// generate default column name
				result[j] = "Column " + colIndexToExcel(j);
			}
			unique.add (result[j]);
		}
		return result;
	}

	/**
	 * indicates whether a header is present or not
	 * @return value of noHeader (true or false)
	 */
	public boolean getNoHeader()
	{
		return firstDataRow - firstHeaderRow <= 0;
	}

	/**
	 * Returns the boolean value set by the user which indicates whether
	 * the system code is fixed, or specified in another column
	 */
	public boolean isSyscodeFixed()
	{
		return isSyscodeFixed;
	}


	/**
	 * Sets the boolean value to determine if the system code
	 * is fixed, or specified in separate column
	 */
	public void setSyscodeFixed(boolean target)
	{
		isSyscodeFixed = target;
	}

	/**
	 * Sets the data source to use for all imported identifiers.
	 * Only meaningful if getSyscodeColumn returns false.
	 */
	public void setDataSource(DataSource value)
	{
		ds = value;
	}

	/**
	 * Gets the data source to use for all imported identifiers.
	 * Only meaningful if getSyscodeColumn retunrs false.
	 */
	public DataSource getDataSource()
	{
		return ds;
	}

	/**Returns the string that is used as the delimiter for reading the input data.
	 * This string is used to separate columns in the input data.
	 * The returned string can be any length, but during normal use it is typically 1 or 2 characters
	 * long.
	 */
	public String getDelimiter()
	{
		return delimiter;
	}

	/** Set the delimiter string. This string is used to separate columns in the input data.
	 * The delimiter string can be set to any length, but during normal use it is typically
	 * 1 or 2 characters long
	 */
	public void setDelimiter(String target)
	{
		delimiter = target;
		interpretSample();
	}

	private static final int NUM_SAMPLE_LINES = 50;
	private List<String> lines = null;
	private String[][] cells = null;

	private int sampleMaxNumCols = 0;

	public int getSampleMaxNumCols()
	{
		return sampleMaxNumCols;
	}

	public int getSampleNumRows()
	{
		return lines == null ? 0 : lines.size();
	}

	public String getSampleData (int row, int col)
	{
		if (cells != null && cells[row] != null &&
				cells[row].length > col)
		{
			return cells[row][col];
		}
		else
		{
			return "";
		}
	}

	private boolean digitIsDot = true;
	public boolean digitIsDot()
	{
		return digitIsDot;
	}

	public void setDigitIsDot (boolean value)
	{
		digitIsDot = value;
	}

	/** derive datasource from sample data */
	public void guessSettings()
	{
		isSyscodeFixed = !guessHasSyscodeColumn;
		if (guessDataSource != null) setDataSource(guessDataSource);
		if (guessHasSyscodeColumn && guessSyscodeColumn >= 0)
		{
			setSysodeColumn(guessSyscodeColumn);
		}
		if (guessIdColumn >= 0) setIdColumn(guessIdColumn);
		digitIsDot = guessDigitIsDot;
		Logger.log.info ("Guessing sysCode: " + guessHasSyscodeColumn + " " + guessSyscodeColumn +
				" id: " + guessIdColumn + " " + guessDataSource + " digitIsDot? " + guessDigitIsDot);
	}


	private boolean guessHasSyscodeColumn = true;
	private int guessSyscodeColumn = -1;
	private int guessIdColumn = -1;
	private boolean guessDigitIsDot;
	private DataSource guessDataSource = null;

	/**
	 * Helper class to keep track of how often patterns occur, and in which column
	 */
	private static class PatternCounter
	{
		private final Pattern p;
		private Map <Integer, Integer> counts = new HashMap <Integer, Integer>();
		private int total = 0;

		PatternCounter (Pattern p)
		{
			this.p = p;
		}

		void countCell (String cell, int column)
		{
			Matcher m = p.matcher(cell);

			// check if it matches
			if (m.matches())
			{
				//increase total and per-column counts
				int prev = counts.containsKey(column) ? counts.get(column) : 0;
				counts.put (column, ++prev);
				total++;
			}
		}

		int getTotal()
		{
			return total;
		}

		int getColumnCount(int col)
		{
			return (counts.containsKey(col) ? counts.get (col) : 0);
		}
	}

	/** fraction of cells that have to match a pattern for it to be a good guess */
	private static final double GOOD_GUESS_FRACTION = 0.9;

	/*
	 * read a sample from the selected text file
	 */
	private void readSample() throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(txtFile));
		lines = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null && lines.size() < NUM_SAMPLE_LINES)
		{
			lines.add(line);
		}
		in.close();
	}

	/* guess some parameters */
	private void interpretSample()
	{
		//"Guess" the system code based on the first 50 lines.

		//Make regular expressions patterns for the gene ID's.
		Map<DataSource, Pattern> patterns = DataSourcePatterns.getPatterns();

		//Make regular expressions pattern for the system code.
		final PatternCounter syscodeCounter = new PatternCounter (Pattern.compile("[A-Z][a-z]?"));
		final PatternCounter dotCounter = new PatternCounter (Pattern.compile("-?[0-9]*\\.[0-9]+"));
		final PatternCounter commaCounter = new PatternCounter (Pattern.compile("-?[0-9]*,[0-9]+"));

		//Make count variables.
		Map<DataSource, PatternCounter> counters = new HashMap<DataSource, PatternCounter>();

		for (DataSource ds : patterns.keySet())
		{
			counters.put (ds, new PatternCounter (patterns.get(ds)));
		}

		sampleMaxNumCols = 0;
		int row = 0;
		cells = new String[NUM_SAMPLE_LINES][];
		for (String line : lines)
		{
			cells[row] = line.split(delimiter);
			int numCols = cells[row].length;

			if (numCols > sampleMaxNumCols)
			{
				sampleMaxNumCols = numCols;
			}

			for (int col = 0; col < cells[row].length; ++col)
			{
				//Count all the times that an element matches a gene identifier.
				syscodeCounter.countCell (cells[row][col], col);
				commaCounter.countCell (cells[row][col], col);
				dotCounter.countCell (cells[row][col], col);

				for (DataSource ds : patterns.keySet())
				{
					counters.get(ds).countCell (cells[row][col], col);
				}
			}

			row++;
		}

		/*Calculate percentage of rows where a system code is found and
		 * compare with a given percentage*/
		{
			double max = 0;
			int maxCol = -1;

			for (int col = 0; col < sampleMaxNumCols; ++col)
			{
				double syscodepercentage = (double)syscodeCounter.getColumnCount(col) / (double)row;

				if (syscodepercentage > max)
				{
					max = syscodepercentage;
					maxCol = col;
				}
			}

			/*Set the selection to the codeRadio button if a system code is found
			 * in more than rows than the given percentage, otherwise set the
			 * selection to the syscodeRadio button*/
			if (max >= GOOD_GUESS_FRACTION)
			{
				guessHasSyscodeColumn = true;
				guessSyscodeColumn = maxCol;
			}
			else
			{
				guessHasSyscodeColumn = false;
				guessSyscodeColumn = -1;
			}
		}

		double commaTotal = commaCounter.getTotal();
		double dotTotal = dotCounter.getTotal();

		// if more than 90% of number-like patterns use a dot, then the digit symbol is a dot.
		guessDigitIsDot = ((dotTotal / (commaTotal + dotTotal)) > GOOD_GUESS_FRACTION);
		Logger.log.info ("readsample - I read " + dotTotal + " dots and " + commaTotal + " comma's. I'm guessing " + (guessDigitIsDot ? "dot" : "comma"));

		//Look for maximum.
		double max = 0;
		double second = 0;
		DataSource maxds = null;
		int maxCol = -1;

		for (int col = 0; col < sampleMaxNumCols; ++col)
		{
			for (DataSource ds : patterns.keySet())
			{
				//Determine the maximum of the percentages (most hits).
				//Sometimes, normal data can match a gene identifier, in which case percentages[i]>1.
				//Ignores these gene identifiers.
				double percentage = (double)counters.get(ds).getColumnCount(col)/(double)row;
				if (percentage > max && percentage <= 1)
				{
					// remember the second highest percentage too
					second = max;
					max = percentage;
					maxds = ds;
					maxCol = col;
				}
			}
		}

		//Select the right entry in the drop down menu and change the system code in importInformation
		guessDataSource = maxds;

		// only set guessIdColumn if the guess is a clear outlier,
		// if it's hardly above noise, just set it to 0. 
		if (max > 2 * second)
			guessIdColumn = maxCol;
		else
			guessIdColumn = 0;
	}

	int dataRowsImported = 0;
	int rowsMapped = 0;
	
	public int getDataRowsImported()
	{
		return dataRowsImported;
	}
	
	public int getRowsMapped()
	{
		return rowsMapped;
	}
}
