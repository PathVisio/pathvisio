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
package org.pathvisio.util.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;


/**
 * Like the default table model, this class is intended as a basis
 * for more specific table models.
 *
 * The basis for this tablemodel forms a simple list of objects.
 * These objects must extend the RowWithProperties interface.
 *
 * A rowWithProperties is an object that has a getProperty() method,
 * that is used by the table model to get the value for each column.
 * Properties (and aslo columns) are identified by static objects
 * derived from the PropertyColumn interface, usually enum objects.
 *
 * objects can be added without type casting with addRow and retrieved
 * with getRow()
 *
 * You can define which columns / properties are shown
 * with setColumns().
 *
 * To turn a list of any object into a simple table, you must have the following:
 *
 * @param <U> Column base class that implements PropertyColumn,
 *   wich enumerates the possible columns
 *
 * @param <T> Row base class that implements RowWithProperties<U>
 *
 * Then you can use the following table model:
 * ListWithPropertiesTableModel<U, T<U>>
 */
public class ListWithPropertiesTableModel<U extends PropertyColumn, T extends RowWithProperties<U>> extends AbstractTableModel
{

	protected List<T> rows = new ArrayList<T>();
	private U[] columns = null;

	public void addRow (T ps)
	{
		rows.add(ps);
		int newRowIndex = rows.size() - 1;
		fireTableRowsInserted(newRowIndex, newRowIndex);
	}

	public void setColumns (U[] cols)
	{
		columns = cols;
		fireTableStructureChanged();
	}

	public Class<?> getColumnClass(int arg0)
	{
		return String.class;
	}

	public int getColumnCount()
	{
		if (columns == null) return 0;
		return columns.length;
	}

	public String getColumnName(int i)
	{
		if (columns == null) return "";
		return columns[i].getTitle();
	}

	public int getRowCount()
	{
		return rows.size();
	}

	public Object getValueAt(int row, int col)
	{
		if (columns == null) return null;
		return rows.get(row).getProperty(columns[col]);
	}

	/**
	 * Get the row object by index
	 */
	public T getRow(int row)
	{
		return rows.get(row);
	}

}
