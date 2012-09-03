package org.pathvisio.data;

/**
 * Exception wrapper for use by all org.pathvisio.data interfaces.
 * The intention is that this class is used to wrap exceptions of implementing classes,
 * for example if the data is backed by an SQL database, SQLException would be wrapped
 * inside a DataException
 */
public class DataException extends Exception
{
	/**
	 * @param the exception being wrapped
	 */
	public DataException(Throwable e)
	{
		super (e);
	}

	/**
	 * @param msg a simple diagnostic message
	 */
	public DataException(String msg)
	{
		super (msg);
	}

	/**
	 * @param msg a simple diagnostic message
	 * @param e the exception being wrapped.
	 */
	public DataException(String msg, Throwable e)
	{
		super (msg, e);
	}

}
