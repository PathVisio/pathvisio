package org.pathvisio.wikipathways;

public enum Parameter {
	PW_NAME("pwName"),
	PW_URL("pwUrl", false),
	PW_SPECIES("pwSpecies"),
	PW_NEW("new", null),
	USER("user", null),
	RPC_URL("rpcUrl"),
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
	
	private void restoreDefault() {
		value = null;
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
	
	public static void restoreDefaults() {
		for(Parameter p : values()) {
			p.restoreDefault();
		}
	}
}