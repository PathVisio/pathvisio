package org.pathvisio.wikipathways.client;

public class SearchException extends RuntimeException {
	public SearchException(String message) {
		super(message);
	}
	
	public SearchException(Throwable cause) {
		super(cause);
	}
}
