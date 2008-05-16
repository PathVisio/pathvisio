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


import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import org.pathvisio.data.ImportInformation;

class ColumnTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	
	private ImportInformation info;
	
	ColumnTableModel (ImportInformation info)
	{
		this.info = info;
	}
	
	public void refresh()
	{
		fireTableStructureChanged();
	}
	
	private String getType(int row, int col)
	{
		String result = "N"; // data
		if (info.getSyscodeColumn() && col ==  info.getCodeColumn())
		{
			result = "C";
		}
		if (col == info.getIdColumn())
		{
			result = "I";
		}
		return result;
	}
	
	public int getColumnCount() 
	{
		return info.getSampleMaxNumCols();
	}

	public int getRowCount() 
	{
		return info.getSampleNumRows();
	}

	public Object getValueAt(int row, int col) 
	{
		return getType (row, col) + ":" + info.getSampleData(row, col);
	}
}