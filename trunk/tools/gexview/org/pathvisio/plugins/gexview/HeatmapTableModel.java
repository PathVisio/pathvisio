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
package org.pathvisio.plugins.gexview;

import javax.swing.table.AbstractTableModel;

import org.bridgedb.IDMapperException;
import org.pathvisio.debug.Logger;
import org.pathvisio.gex.SimpleGex;

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
			try {
				return input.getSample(column - 1).getName();
			} 
			catch (IDMapperException e) 
			{
				Logger.log.error ("Could not set error name", e);
				return "###";
			}
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
