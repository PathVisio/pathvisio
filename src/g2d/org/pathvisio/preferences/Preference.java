package org.pathvisio.preferences;


public interface Preference {
	public String name();
	
	public void setDefault(String defValue);
	
	public String getDefault();
	
	public void setValue(String newValue);

	public String getValue();
}
