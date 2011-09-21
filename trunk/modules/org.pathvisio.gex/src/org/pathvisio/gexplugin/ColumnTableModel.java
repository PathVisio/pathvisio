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
/**
 *
 */
package org.pathvisio.gexplugin;


import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.gexplugin.ImportInformation.ColumnType;

/**
 * Table model used in the column page of the Gex Import Wizard.
 * It displays a sample of the data to be imported, with the cells colored depending on
 * their type. e.g. an identifier is Green, a system code is Red and a header is yellow.
 */
class ColumnTableModel extends AbstractTableModel
{
	static private class HighlightedCellRenderer extends DefaultTableCellRenderer
	{
		ImportInformation info;

		public HighlightedCellRenderer(ImportInformation info)
	    {
	    	super();
	    	this.info = info;
	        setOpaque(true); //MUST do this for background to show up.
	    }

	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
	    {
	    	setBackground(getTypeColor(row, column));
	    	return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

	    @Override
	    public void setValue (Object value)
	    {
			setText (value.toString());
	    }

	    private static final Color LIGHT_RED = new Color (255, 192, 192);
	    private static final Color LIGHT_GREEN = new Color (192, 255, 192);
	    private static final Color LIGHT_YELLOW = new Color (255, 255, 192);
	    private static final Color LIGHT_MAGENTA = new Color (192, 255, 255);

		private Color getTypeColor (int row, int col)
		{
			Color result = Color.LIGHT_GRAY; // nothing
			if (info.isHeaderRow(row))
			{
				result = LIGHT_YELLOW;
			}
			else if (info.isDataRow(row))
			{
				ColumnType type = info.getColumnType(col);
				switch (type)
				{
				case COL_ID: result = LIGHT_GREEN; break;
				case COL_NUMBER: result = Color.WHITE; break;
				case COL_STRING: result = LIGHT_MAGENTA; break;
				case COL_SYSCODE: result = LIGHT_RED; break;
				}
			}
			return result;
		}
	}


	private ImportInformation info;

	ColumnTableModel (ImportInformation info)
	{
		this.info = info;
	}

	public void refresh()
	{
		fireTableStructureChanged();
	}

	public TableCellRenderer getTableCellRenderer()
	{
		return new HighlightedCellRenderer(info);
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

	public java.awt.Component getTableCellRendererComponent(
			javax.swing.JTable arg0, Object arg1, boolean arg2, boolean arg3,
			int arg4, int arg5) {
		// TODO Auto-generated method stub
		return null;
	}
}