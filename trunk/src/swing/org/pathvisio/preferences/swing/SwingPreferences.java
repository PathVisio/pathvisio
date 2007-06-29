// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
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

