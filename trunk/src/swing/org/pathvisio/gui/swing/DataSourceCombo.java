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
package org.pathvisio.gui.swing;

import javax.swing.JComboBox;

import org.pathvisio.model.DataSource;

public class DataSourceCombo extends JComboBox 
{
	private static final long serialVersionUID = 1L;

	public void initItems()
	{
		for (DataSource ds : DataSource.getDataSources())
		{
			addItem(ds.getFullName());
		}
	}
	
	public DataSource getSelectedDataSource()
	{
		return DataSource.getByFullName("" + getSelectedItem());
	}
	
	public void setSelectedDataSource(DataSource value)
	{
		setSelectedItem (value == null ? null : value.getFullName());
	}
}
