// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.wikipathways.client;

import java.util.HashMap;
import java.util.Map;

public class State {
	Map<String, String> values = new HashMap<String, String>();

	public State() {
	}

	public State(String str) {
		if(str != null && str.length() > 0) {
			String[] data = str.split(SEP);
			for(String pair : data) {
				String[] keyvalue = pair.split(IS);
				if(keyvalue.length != 2) {
					throw new IllegalArgumentException(
							"Invalid key/value pair: " + pair + " in " + str
					);
				}
				setValue(keyvalue[0], keyvalue[1]);
			}
		}
	}

	public void setValue(String key, String value) {
		values.put(key, value);
	}

	public String getValue(String key) {
		return values.get(key);
	}

	public Map<String, String> getValues() {
		return values;
	}

	public String toString() {
		StringBuilder strb = new StringBuilder();
		for(String key : values.keySet()) {
			String value = values.get(key);
			if(value == null || value.length() == 0) {
				continue; //Skip empty values;
			}
			strb.append(key);
			strb.append(IS);
			strb.append(value);
			strb.append(SEP);
		}
		if(strb.length() > SEP.length()) {
			return strb.substring(0, strb.length() - SEP.length());
		} else {
			return "";
		}
	}

	static final String SEP = "&";
	static final String IS = "=";

	static final String KEY_PATHWAY = "pathway";
	static final String KEY_FACTOR_TYPE = "factortype";
	static final String KEY_FACTOR_VALUES = "factorvalues";
	static final String KEY_PANEL = "panel";

	static final String PANEL_PATHWAY = "pathway";
	static final String PANEL_FACTOR = "factor";
	static final String PANEL_IMAGE = "image";
}
