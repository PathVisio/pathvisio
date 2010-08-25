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
package org.pathvisio.gui.parameter;

public class SimpleParameterModel extends AbstractParameterModel implements ParameterModel
{
	final Object[] values;
	final String[] labels;
	final Object[] metadata;

	protected SimpleParameterModel(Object[][] data)
	{
		values = new Object[data.length];
		metadata = new Object[data.length];
		labels = new String[data.length];
		
		for (int i = 0; i < data.length; ++i)
		{
			labels[i] = (String)data[i][0];
			values[i] = data[i][1];
			if (data[i].length > 2)
			{
				metadata[i] = data[i][2]; 
			}
			else
			{
				metadata[i] = data[i][1]; // just copy default
			}
		}
	}
	
	@Override
	public Object getMetaData(int i)
	{
		return metadata[i];
	}

	@Override
	public String getHint(int i)
	{
		return labels[i];
	}

	@Override
	public String getLabel(int i)
	{
		return labels[i];
	}

	@Override
	public int getNum()
	{
		return values.length;
	}

	@Override
	public Object getValue(int i)
	{
		return values[i];
	}

	@Override
	public void setValue(int i, Object val)
	{
		values[i] = val;
		fireParameterModelEvent(new ParameterModelEvent(ParameterModelEvent.Type.VALUE_CHANGED, i));
	}

}
