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
/**
 * 
 */
package org.pathvisio.gui.swing;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.table.AbstractTableModel;

import org.pathvisio.debug.Logger;

class PreviewTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	private File txtFile = null;
	private String separator = "\t";

	private final int N = 50;
	String[] lines = null;
	String[][] cells = null;
	
	int maxNumCols = 0;
	int numRows = 0;
	
	private void readSample()
	{
		try 
		{
			BufferedReader in;
			in = new BufferedReader(new FileReader(txtFile));
			// changed readahead from 10000 to 500000
			// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4616869
			// TODO: this may still fail for long lines (more than 500000 bytes in 50 lines) 
			in.mark(500000);

			String line;
			numRows = 0;
			maxNumCols = 0;
			lines = new String[N];
			cells = new String[N][];
			while ((line = in.readLine()) != null && numRows < N) 
			{
				lines[numRows] = line;
				cells[numRows] = line.split(separator);
				int numCols = cells[numRows].length;
				if (numCols > maxNumCols)
				{
					maxNumCols = numCols;
				}
				numRows++;
			}
		} 
		catch (IOException e) 
		{ 		
			//TODO: pop up error dialog
			Logger.log.error("while generating preview for importing expression data: " + e.getMessage(), e);
		}
	}
	
	public void setTextFile (File f)
	{
		txtFile = f;
		readSample();
		fireTableStructureChanged();
	}
	
	public void setSeparator (String separator)
	{
		this.separator = separator;
		readSample();
		fireTableStructureChanged();
	}
	
	public int getColumnCount() 
	{
		return maxNumCols;
	}

	public int getRowCount() 
	{
		return numRows;
	}

	public Object getValueAt(int row, int col) 
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
}