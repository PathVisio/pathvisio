package org.pathvisio.preferences.swing;

import java.io.IOException;

import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.PreferenceCollection;

public class SwingPreferences implements PreferenceCollection {
	public Preference byName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void save() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public enum SwingPreference implements Preference {
		;
			String value;
			String defaultValue;
			
			
			SwingPreference(String defaultValue) {
				this.defaultValue = defaultValue;
			}
			
			public String getDefault() {
				return defaultValue;
			}

			public String getValue() {
				if(value == null) {
					return defaultValue;
				} else {
					return value;
				}
			}

			public void setDefault(String defValue) {
				defaultValue = defValue;
			}

			public void setValue(String newValue) {
				value = newValue;
			}
		}
}

