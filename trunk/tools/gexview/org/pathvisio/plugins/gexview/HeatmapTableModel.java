package org.pathvisio.plugins.gexview;

import javax.swing.table.AbstractTableModel;

import org.pathvisio.data.SimpleGex;

public class HeatmapTableModel extends AbstractTableModel 
{
	private static final long serialVersionUID = 1L;
	
	// input can't be null.
	private SimpleGex input;

	public HeatmapTableModel(SimpleGex input)
	{
		if (input == null) throw new NullPointerException();
		this.input = input;
	}
	
	public int getColumnCount() 
	{
		return input.getSamples(-1).size() + 1;
	}

	public String getColumnName(int column)
	{
		if (column == 0)
		{
			return "ID";
		}
		else
		{
			return input.getSample(column - 1).getName();
		}
	}
	
	public int getRowCount() 
	{
		return 1000;
	}

	public Object getValueAt (int rowIndex, int columnIndex) 
	{
		return "1.0";
	}
	
}
