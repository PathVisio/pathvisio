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
