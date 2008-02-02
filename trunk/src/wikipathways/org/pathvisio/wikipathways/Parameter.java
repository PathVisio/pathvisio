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
package org.pathvisio.wikipathways;

/**
 * Input parameter for the WikiPathways editor. Non-defaults need to be
 * set before calling {@link WikiPathways#init(org.pathvisio.view.VPathwayWrapper, org.pathvisio.util.ProgressKeeper, java.net.URL)}.
 * @author thomas
 */
public enum Parameter {
	PW_NAME("pwName"),
	PW_URL("pwUrl", false),
	PW_SPECIES("pwSpecies"),
	PW_NEW("new", null),
	USER("user", null),
	RPC_URL("rpcUrl"),
	/**
	 * A comma seperated list of categories
	 */
	CATEGORIES("categories", ""),
	/**
	 * The hostname of the gene database server (default: wikipathways.org)
	 */
	GDB_SERVER("gdb_server", "wikipathways.org"),
	/**
	 * The revision id of the pathway
	 */
	REVISION("revision", true),
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