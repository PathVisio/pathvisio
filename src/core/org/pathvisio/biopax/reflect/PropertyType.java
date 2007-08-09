package org.pathvisio.biopax.reflect;

public enum PropertyType {
	TITLE("http://www.w3.org/2001/XMLSchema#string", 1),
	YEAR("http://www.w3.org/2001/XMLSchema#string", 1),
	AUTHORS("http://www.w3.org/2001/XMLSchema#string"),
	ID("http://www.w3.org/2001/XMLSchema#string", 1),
	DB("http://www.w3.org/2001/XMLSchema#string", 1),
	SOURCE("http://www.w3.org/2001/XMLSchema#string", 1),
	;
	
	String datatype;
	int maxCardinality;
	
	PropertyType(String datatype) {
		this(datatype, BiopaxProperty.UNBOUND);
	}
	
	PropertyType(String datatype, int maxCardinality) {
		this.datatype = datatype;
		this.maxCardinality = maxCardinality;
	}
	
	BiopaxProperty getProperty(String value) {
		return new BiopaxProperty(name(), value, datatype, maxCardinality);
	}
}
