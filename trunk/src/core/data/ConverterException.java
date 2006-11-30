package data;

public class ConverterException extends Exception {

	private static final long serialVersionUID = 1L;

	ConverterException(String msg)
	{
		super(msg);
	}

	ConverterException(Exception e)
	{
		super(e.getClass() + ": " + e.getMessage());
		setStackTrace(e.getStackTrace());
	}


}
