package org.pathvisio.gui.parameter;

public interface Editor
{
	/**
	 * extract value from editor component and return it.
	 */
	public Object getValue();
	
	/**
	 * Update editor component with given value.
	 */
	public void setValue(Object val);
}
