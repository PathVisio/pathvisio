package org.pathvisio.visualization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a VisualizationMethod instance by name.
 * @author thomas
 */
public class VisualizationMethodRegistry {
	private Map<String, VisualizationMethodProvider> methodProviders = 
		new HashMap<String, VisualizationMethodProvider>();
	
	private static VisualizationMethodRegistry current;
	
	public static VisualizationMethodRegistry getCurrent() {
		if(current == null) {
			current = new VisualizationMethodRegistry();
		}
		return current;
	}
	
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
	public VisualizationMethod createVisualizationMethod(String name, Visualization v) {
		VisualizationMethodProvider p = methodProviders.get(name);
		if(p != null) {
			return p.create(v);
		}
		return null;
	}
}
