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
import java.util.List;
import java.util.ArrayList;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.DataSource;

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
	 * @param txtFile {@link File} to set
	 */
	public void setTxtFile(File txtFile)
	{
		//Close the connection to the previous file if exist
		if(in != null) {
			try { in.close(); } catch(Exception e) { 
				Logger.log.error("on closing file " + this.txtFile + ": " + e.getMessage(), e);
			}
			in = null;
		}
		this.txtFile = txtFile;
	}
	/**
	 * Get the private {@link File} txtFile
	 * @return {@link File} object pointing to the text file that contains the
	 * expression data
	 */
	public File getTxtFile() { return txtFile; } 
	
	/**
	 * The database name in which the expression data is saved
	 */
	public String dbName;

	private double maximum;
	private double minimum;
	
	/** get/set maximum double value in this dataset */
	public double getMaximum() { return maximum; }
	public void setMaximum(double setpoint) { maximum = setpoint; }
	
	/** get/set minimum double value in this dataset */
	public double getMinimum() { return minimum; }
	public void setMinimum(double setpoint) { minimum = setpoint; }
	
	
	private List<String> errorList = new ArrayList<String>();
	private int nrErrors = 0;
	
	/** Returns the number of errors made during importing data, for example when no
	 * Esembl gene is found*/
	public int getNrErrors() {
		return nrErrors;	
	}
	/** Returns a list of errors made during importing data, the same list as saved
	 * in the error file (.ex.txt) */
	public List<String> getErrorList() {
		return errorList;
	}
	/** A error has been reported during importing data. The message is added to 
	 * the list of errors. */
	public void addError(String message) {
		errorList.add(message);
		nrErrors++;
	}

	/**
	 * linenumber (first line is 1) of the line where the data begins
	 */
	int firstDataRow;
	/**
	 * linenumber (first line is 1) of the line containing the column headers
	 */
	int headerRow;
	/**
	 * Column number (first column is 0) of the column containing the gene identifier
	 */
	int idColumn;

	/**
	 * Column number (first column is 0) of the column containing the systemcode
	 */
	int codeColumn;
 	
 	/** 
 	 * True if there is no header in the data	
 	*/
 	private boolean noHeader;
	
	/**
	 * Boolean which can be set to false if there is no column for the system code is available
	 * in the dataset. */
	boolean syscodeColumn = true;
	
	/**
	 * String containing the system code that has been set by the user in gexImportWizard 
	 * (if no system code column is available).
	 */
	String syscode = "";
	
	/**
	 * Delimiter used to seperate columns in the text file containing expression data
	 */
	private String DELIMITER = "\t";

	/**
	 * Column numbers (first column is 0) of the columns of which the data should not be treated
	 * as numberic
	 */
	private int[] stringCols;

	/**
	 * Constructor for this class
	 * Sets the default values
	 */
	public ImportInformation() {
		// Set the defaults
		firstDataRow = 2;
		headerRow = 1;
		idColumn = 0;
		codeColumn = 1;
		noHeader = false;

	}

	/**
	 * {@link BufferedReader} to the text file, maintained while the wizard is open
	 */
	BufferedReader in;

	/**
	 * Get a {@link BufferedReader} to the text file containing the expression data
	 * Creates a new one 
	 * @return
	 */
	public BufferedReader getBufferedReader() throws IOException
	{
		if (in == null) 
		{
				in = new BufferedReader(new FileReader(txtFile));
				// changed readahead from 10000 to 500000
				// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4616869
				// TODO: this may still fail for long lines (more than 500000 bytes in 50 lines) 
				in.mark(500000);
		} 
		else 
		{
			in.reset();
		}
		return in;
	}

	/**
	 * Sets the private property stringCols
	 * @param cols	Column numbers (start with 0) of columns containing data that
	 * should not be treated as numeric
	 */
	public void setStringCols(int[] cols) {
		stringCols = cols;
	}

	/**
	 * Sets the private property stringCols
	 * @return	Column numbers (start with 0) of columns containing data that
	 * should not be treated as numeric, or an empty String[]
	 */
	public int[] getStringCols() {
		if (stringCols == null)
			stringCols = new int[] {};
		return stringCols;
	}

	/**
	 * Checks if the column for the given column index is marked as 'string column' and
	 * should not be treated as numeric
	 * @param colIndex	the index of the column to check (start with 0)
	 * @return true if the column is marked as 'string column', false if not
	 */
	public boolean isStringCol(int colIndex) {
		if (stringCols == null)
			return false;
		for (int col : stringCols)
			if (col == colIndex)
				return true;
		return false;
	}

	/**
	 * Reads the column names from the text file containing the expression data at the
	 * header row specified by the user. Multiple header rows can also be read. When no header
	 * row is present, a header row is created manually.
	 * @return the column names
	 */
	public String[] getColNames() 
	{
		if(getNoHeader() == true) {
			try {
				BufferedReader in = getBufferedReader();
				String[] firstLine = in.readLine().split(getDelimiter());
				int j = 1; // Number of the column
				String[] newColNames = new String[firstLine.length]; // Array with manually set column names
				for (String col : firstLine) {
					col = "Column " + j;
					newColNames[j-1] = col; // New column name
					j++;
				}
				return newColNames;
				
			} catch (IOException e) { // TODO: handle IOException
				Logger.log.error("Unable to get column names for importing expression data: " + e.getMessage(), e);
				return new String[] {};
			}
		}
		
		else {
			try {
				BufferedReader in = getBufferedReader();
				String[] line = in.readLine().split(getDelimiter()); // Read the first line
				int nrCol = line.length; // Number of columns
				// Initiate headerline as line with no characters
				String[] headerLine = new String[nrCol];
				for(int i = 0; i < nrCol; i++) headerLine[i] = "";
				
				int i = 0;
				while (i < firstDataRow - 1) { // Read headerlines till the first data row
					for(int j = 0; j < line.length; j++){  // Represents columns
						// All header rows are added
						if(i >= headerRow - 1) headerLine[j] = headerLine[j] + " " + line[j];
					}
					line = in.readLine().split(getDelimiter());
					i++;
				}
				return headerLine;
			}

			catch (IOException e) { // TODO: handle IOException
				Logger.log.error("Unable to get column names for importing expression data: " + e.getMessage(), e);
				return new String[] {};
			}
		}
	}
	
	/**
	 * Returns the boolean value which indicates whether a header is present or not
	 * @return value of noHeader (true or false)
	 */	
	public boolean getNoHeader() {
		return noHeader;
	}
	
	/**
	 * Sets the value of noHeader, taken from the target as specified where the method is used.
	 * noHeader can be set true or false
	 * @param target
	 */	 	
	public void setNoHeader(boolean target) {
		noHeader = target;
	}
	
	/**
	 * Returns the boolean value set by the user which indicates whether a column 
	 * system code column is present or not.
	 */
	public boolean getSyscodeColumn() {
		return syscodeColumn;
	}
	
	/**
	 * Sets the boolean value syscodeColumn to 'system code present' or 'system code 
	 * not present' in data. 
	 */
	public void setSyscodeColumn(boolean target) {
		syscodeColumn = target;
	}
	
	/**
	 * Returns the system code (String) selected from DataSources.systemCodes.
	 * @deprecated use getDataSource instead.
	 */
	public String getSyscode()
	{
		return syscode;
	}
	
	/**
	 * Sets the system code (String) to a value from DataSources.systemCodes.
	 * @deprecated use setDataSource instead
	 */
	public void setSyscode(String target) {
		syscode = target;
	}
	
	/**
	 * Sets the data source to use for all imported identifiers.
	 * Only meaningful if getSyscodeColumn returns false.
	 */
	public void setDataSource(DataSource value)
	{
		syscode = value.getSystemCode();
	}
	
	/**
	 * Gets the data source to use for all imported identifiers.
	 * Only meaningful if getSyscodeColumn retunrs false.
	 */
	public DataSource getDataSource()
	{
		return DataSource.getBySystemCode (syscode);
	}
	
	/**Returns the string that is used as the delimiter for reading the input data.
	 * This string is used to separate columns in the input data.
	 * The returned string can be any length, but during normal use it is typically 1 or 2 characters
	 * long.
	 */
	public String getDelimiter() {
		return DELIMITER;
	}
	
	/** Set the delimiter string. This string is used to separate columns in the input data. 
	 * The delimiter string can be set to any length, but during normal use it is typically
	 * 1 or 2 characters long
	 */
	public void setDelimiter(String target) {
		DELIMITER=target;
	}

}
