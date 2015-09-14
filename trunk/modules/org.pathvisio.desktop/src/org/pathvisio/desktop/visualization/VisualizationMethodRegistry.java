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
package org.pathvisio.desktop.visualization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a VisualizationMethod instance by name.
 * @author thomas
 * @author anwesha
 */
public class VisualizationMethodRegistry {
	private Map<String, VisualizationMethodProvider> methodProviders =
		new HashMap<String, VisualizationMethodProvider>();
	private Map<String, VisualizationMethodProvider> edgemethodProviders =
			new HashMap<String, VisualizationMethodProvider>();
	private Map<String, VisualizationMethodProvider> complexmethodProviders =
			new HashMap<String, VisualizationMethodProvider>();

	/**
	 * Register a visualization method for the given name. The
	 * VisualizationMethodProvider will be used to create instances
	 * of VisualizationMethod.
	 */
	public void registerMethod(String name, VisualizationMethodProvider p) {
		methodProviders.put(name, p);
	}

	public void unregisterMethod(String name) {
		methodProviders.remove(name);
	}

	public Set<String> getRegisteredMethods() {
		return methodProviders.keySet();
	}
	/**
	 * Creates an instance of a VisualizationMethod subclass that is registered
	 * by the given name. Returns null if there is no subclass registered for the
	 * name.
	 */
	public VisualizationMethod createVisualizationMethod(String name) {
		VisualizationMethodProvider p = methodProviders.get(name);
		if(p != null) {
			return p.create();
		}
		return null;
	}
	
	/**
	 * Register a visualization method for Lines with the given name.
	 * The VisualizationMethodProvider will be used to create instances
	 * of VisualizationMethod.
	 */
	public void registerEdgeMethod(String name, VisualizationMethodProvider p) {
		edgemethodProviders.put(name, p);
	}

	public void unregisterEdgeMethod(String name) {
		edgemethodProviders.remove(name);
	}

	public Set<String> getRegisteredEdgeMethods() {
		return edgemethodProviders.keySet();
	}
	/**
	 * Creates an instance of a VisualizationMethod subclass that is registered
	 * by the given name. Returns null if there is no subclass registered for the
	 * name.
	 */
	public VisualizationMethod createEdgeVisualizationMethod(String name) {
		VisualizationMethodProvider p = edgemethodProviders.get(name);
		if(p != null) {
			return p.create();
		}
		return null;
	}
	
	/**
	 * Register a visualization method for Complexes with the given name.
	 * The VisualizationMethodProvider will be used to create instances
	 * of VisualizationMethod.
	 */
	public void registerComplexMethod(String name, VisualizationMethodProvider p) {
		complexmethodProviders.put(name, p);
	}

	public void unregisterComplexMethod(String name) {
		complexmethodProviders.remove(name);
	}

	public Set<String> getRegisteredComplexMethods() {
		return complexmethodProviders.keySet();
	}
	/**
	 * Creates an instance of a VisualizationMethod subclass that is registered
	 * by the given name. Returns null if there is no subclass registered for the
	 * name.
	 */
	public VisualizationMethod createComplexVisualizationMethod(String name) {
		VisualizationMethodProvider p = complexmethodProviders.get(name);
		if(p != null) {
			return p.create();
		}
		return null;
	}
}
