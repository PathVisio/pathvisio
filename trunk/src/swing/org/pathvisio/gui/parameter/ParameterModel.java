package org.pathvisio.gui.parameter;

import java.io.File;

public interface ParameterModel
{
	/**
	 * @return number of parameters in this model.
	 */
	public int getNum();
	
	/**
	 * Get meta data about a parameter.
	 * @param i index of parameter that is queried
	 * @return class of parameter i
	 * <p>
	 * Possible return values:
	 * <ul>
	 * <li>File -> JTextField with JButton("Browse")
	 * <li>String -> JTextField 
	 * <li>URL -> radiobutton file / url and JTextField + Browse button 
	 * <li>Boolean -> checkbox
	 * <li>List<String> or String[] -> combobox
	 * </ul>
	 */
	public Object getMetaData(int i);
	
	/**
	 * Get a short label for a given parameter.
	 * @param i index of the parameter that is queried
	 * @return label for parameter i
	 */
	public String getLabel(int i);
	
	/**
	 * Get a longer hint or description for a given parameter.
	 * @param i index of the parameter that is queried
	 * @return label for parameter i 
	 */
	public String getHint(int i);
	
	/**
	 * Set a parameter value. Based on this action, other parameters may change.
	 * For example, if a URL parameter for a webservice is set, the webservice may be queried 
	 * for the allowed range of values for a second dataset parameter.
	 * @param i index of the parameter
	 * @param val new value for the parameter
	 */
	public void setValue(int i, Object val);

	/**
	 * Get the current value for a parameter. Will return a default value if 
	 * none was set before.
	 * @param i index of the parameter
	 * @return current value of the parameter
	 */
	public Object getValue(int i);
	
	/**
	 * Classes implement this interface to signify that they are ready to listen to changes
	 * to a ParameterModel.
	 */
	public interface ParameterModelListener
	{
		/** called whenever a change to a ParameterModel occurs.
		 * @param e Event object with mode information about the change */
		public void parametersChanged(ParameterModelEvent e);
	}

	public void addParameterModelListener(ParameterModelListener l);
	public void removeParameterModelListener(ParameterModelListener l);
	
	public File getFile(int i);
	
	public String getString (int i);
	
	public boolean getBoolean (int i);

}
