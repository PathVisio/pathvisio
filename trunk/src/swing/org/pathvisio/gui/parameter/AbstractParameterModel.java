package org.pathvisio.gui.parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParameterModel implements ParameterModel
{
	public File getFile(int i)
	{
		return (File)getValue(i);
	}
	
	public String getString (int i)
	{
		return (String)getValue(i);
	}
	
	public boolean getBoolean (int i)
	{
		return (Boolean)getValue(i);
	}
	
	private List<ParameterModelListener> listeners = new ArrayList<ParameterModelListener>();
	
	public void addParameterModelListener(ParameterModelListener l)
	{
		listeners.add(l);
	}
	
	public void removeParameterModelListener(ParameterModelListener l)
	{
		listeners.remove(l);
	}

	protected void fireParameterModelEvent (ParameterModelEvent e)
	{
		for (ParameterModelListener l : listeners)
		{
			l.parametersChanged(e);
		}
	}
}
