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
package org.wikipathways.applet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Input parameters for the WikiPathways editors. Non-defaults need to be
 * set before calling {@link WikiPathways#init(org.pathvisio.view.VPathwayWrapper, org.pathvisio.util.ProgressKeeper, java.net.URL)}.
 * @author thomas
 */
public class Parameter {
	public static final String PW_ID = "pwId";
	public static final String PW_NAME = "pwName";
	public static final String PW_URL = "pwUrl";
	public static final String PW_SPECIES = "pwSpecies";
	public static final String USER = "user";
	public static final String RPC_URL = "rpcUrl";
	public static final String SITE_URL = "siteUrl";
	public static final String PRIVATE = "private";

	/**
	 * Parameter value: A comma seperated list of categories
	 */
	public static final String CATEGORIES = "categories";
	/**
	 * Parameter value: The hostname of the gene database server (default: wikipathways.org)
	 */
	public static final String GDB_SERVER = "gdb_server";
	/**
	 * Parameter value: The revision id of the pathway
	 */
	public static final String REVISION = "revision";

	private Map<String, ParameterValue> parameters = new HashMap<String, ParameterValue>();

	/**
	 * Constructor for the parameter container. Registers default parameters.
	 */
	public Parameter() {
		registerDefaults();
	}

	/**
	 * Get the names of all available parameters
	 */
	public Set<String> getNames() {
		return parameters.keySet();
	}

	/**
	 * Get the value for a parameter
	 * @param name The name of the parameter
	 * @return the value or null if the parameter doesn't exist
	 */
	public String getValue(String name) {
		String value = null;
		ParameterValue p = parameters.get(name);
		if(p != null) {
			value = p.getValue();
		}
		return value;
	}

	/**
	 * Set the value for a parameter
	 * @param name the name of the parameter
	 * @param value the new value
	 * @return true if the value was set successfully, false if there exists
	 * no parameter for the given name
	 */
	public boolean setValue(String name, String value) {
		ParameterValue p = parameters.get(name);
		if(p != null) {
			p.setValue(value);
			return true;
		}
		return false;
	}

	/**
	 * Check whether a parameter is required.
	 * @param name the name of the parameter
	 * @return true if the parameter is required, false if not
	 */
	public boolean isRequired(String name) {
		ParameterValue p = parameters.get(name);
		if(p != null) {
			return p.isRequired();
		}
		return false;
	}

	private void registerDefaults() {
		add(PW_ID, new ParameterValue(""));
		add(PW_NAME, new ParameterValue());
		add(PW_URL, new ParameterValue(null));
		add(PW_SPECIES, new ParameterValue());
		add(USER, new ParameterValue(null));
		add(RPC_URL, new ParameterValue());
		add(CATEGORIES, new ParameterValue(""));
		add(GDB_SERVER, new ParameterValue("wikipathways.org"));
		add(REVISION, new ParameterValue());
		add(SITE_URL, new ParameterValue());
		add(PRIVATE, new ParameterValue(null));
	}

	private void add(String name, ParameterValue p) {
		parameters.put(name, p);
	}

	/**
	 * Restore the default values for the parameters
	 */
	public void restoreDefaults() {
		for(ParameterValue p : parameters.values()) {
			p.restoreDefault();
		}
	}

	/**
	 * Container for a parameter value. Takes care of default values.
	 * @author thomas
	 *
	 */
	private static class ParameterValue {
		String value;
		String defaultValue;
		boolean isRequired;

		public ParameterValue() {
			isRequired = true;
		}

		public ParameterValue(String defaultValue) {
			this.defaultValue = defaultValue;
			isRequired = false;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		private void restoreDefault() {
			value = null;
		}

		public boolean isRequired() {
			return isRequired;
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
}