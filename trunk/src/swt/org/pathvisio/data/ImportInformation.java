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

import org.pathvisio.debug.Logger;

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
	 * Delimiter used to seperate columns in the text file containing expression data
	 * TODO: let the user specify a delimiter
	 */
	String DELIMITER = "\t";

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
	public BufferedReader getBufferedReader() {
		try {
			if (in == null) {
				in = new BufferedReader(new FileReader(txtFile));
				// changed readahead from 10000 to 50000
				// 22.10.2007: changed readahead from 50000 to 500000
				// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4616869
				// TODO: this may still fail for long lines (more than 500000 bytes in 50 lines) 
				in.mark(500000);
			} else {
				in.reset();
			}
		} catch (Exception e) {
			Logger.log.error("Error reading file", e);
		} // TODO: handle exception
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
	 * header row specified by the user
	 * @return the column names
	 */
	public String[] getColNames() {
		try {
			BufferedReader in = getBufferedReader();
			int i = 0;
			while (i < headerRow - 1 && in.readLine() != null)
				i++; // Go to headerline
			return in.readLine().split(getDelimiter());
		} catch (IOException e) { // TODO: handle IOException
			Logger.log.error("Unable to get column names for importing expression data: " + e.getMessage(), e);
			return new String[] {};
		}
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
