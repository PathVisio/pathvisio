package org.pathvisio.gui.parameter;

/**
 * Event to signify a change in the parameter model, either 
 * parameter value(s) or parameter type(s). 
 */
public class ParameterModelEvent
{
	/**
	 * Event type. What is affected: parameter values, types, or both?
	 */
	public enum Type
	{
		/** type, label or number of parameters changed. */
		MODEL_CHANGED, 
		/** multiple parameters changed value but not type or label. */
		VALUES_CHANGED, 
		/** single parameter changed value but not type or label. */
		VALUE_CHANGED,   
	};
	
	private final ParameterModelEvent.Type type;
	
	/** @return type of event */
	public ParameterModelEvent.Type getType() { return type; }
	
	/**
	 * Constructor.
	 * @param type Type of the parameter.
	 */
	public ParameterModelEvent(ParameterModelEvent.Type type)
	{
		this(type, -1);
	}

	/**
	 * Constructor.
	 * @param type Type of the parameter.
	 */
	public ParameterModelEvent(ParameterModelEvent.Type type, int index)
	{
		this.type = type;
		this.index = index;
	}

	private int index;
	public int getIndex() { return index; }

}