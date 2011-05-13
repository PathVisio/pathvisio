// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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


import javax.swing.table.AbstractTableModel;


/**
 * for table used in the header page of the GexImportWizard.
 */
class PreviewTableModel extends AbstractTableModel
{

	private ImportInformation info;

	PreviewTableModel (ImportInformation info)
	{
		this.info = info;
	}

	public void refresh()
	{
		fireTableStructureChanged();
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
		return info.getSampleData(row, col);
	}
}