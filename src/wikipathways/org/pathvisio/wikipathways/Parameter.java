package org.pathvisio.wikipathways;

public enum Parameter {
	PW_NAME("pwName"),
	PW_URL("pathwayUrl", false),
	PW_SPECIES("pwSpecies"),
	PW_NEW("new", "false"),
	USER("user"),
	RPC_URL("prcUrl"),
	;
	
	String name;
	String defaultValue;
	boolean required;
	String value;
	
	private Parameter(String name, boolean isRequired) {
		this.name = name;
		required = isRequired;
	}
	private Parameter(String name) {
		this(name, true);
	}
	
	private Parameter(String name, String defaultValue) {
		this(name, false);
		this.defaultValue = defaultValue;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		if(value == null && !isRequired()) {
			return defaultValue;
		} else {
			return value;
		}
	}
}