package org.wikipathways.indexer;

public interface FieldFilter {
	public boolean include(String name, String value);
}
