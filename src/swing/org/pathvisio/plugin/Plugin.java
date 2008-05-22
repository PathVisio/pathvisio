package org.pathvisio.plugin;

/**
 * This interface needs to be implemented by PathVisio plugins
 * @author thomas
 */
public interface Plugin {
	/**
	 * Called on loading the plugin
	 */
	public void init();
}
