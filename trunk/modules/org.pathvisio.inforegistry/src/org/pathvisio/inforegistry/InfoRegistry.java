// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2014 BiGCaT Bioinformatics
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
package org.pathvisio.inforegistry;

/**
 * 
 * This registry class allows plugins to register/unregister
 * themselves as additional data provider.
 * 
 * @author mkutmon
 *
 */
public class InfoRegistry {

	private static InfoRegistry registry;
	
	// Registry implements the Singelton design pattern 
	// only one instance of the class is available and 
	// can be retrieved by plugins
	public static InfoRegistry getInfoRegistry() {
		if(registry == null) {
			registry = new InfoRegistry();
		}
		return registry;
	}
	
	private InfoRegistry() {
		// TODO: create set of registered providers!
	}
	
	
	
	public void registerInfoProvider(IInfoProvider provider) {
		// TODO!
	}
	
	public void unregisterInfoProvider(IInfoProvider provider) {
		// TODO
	}	
}
