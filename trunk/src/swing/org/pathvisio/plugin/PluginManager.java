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
package org.pathvisio.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.pathvisio.debug.Logger;

/**
 * This class loads and maintains a collection of plugins
 * @author thomas
 *
 */
public class PluginManager {
	//static final String PLUGIN_PKG = "org.pathvisio.visualization.plugins";
	//static final String PKG_DIR = PLUGIN_PKG.replace('.', '/');
	
	Set<Class<Plugin>> plugins = new LinkedHashSet<Class<Plugin>>();
	
	/**
	 * Create a plugin manager that loads plugins from the given locations
	 * @param pluginLocations An array of File objects pointing to jar files 
	 * or directories that will be searched recursively for jar files.
	 */
	public PluginManager(String[] pluginLocations) {
		for(String s : pluginLocations) {
			loadPlugins(s);
		}
	}
	
	/**
	 * Loads all plugins in the given directory
	 */
	void loadPlugins(String pluginLocation) {
		//See if the plugin is a file
		File dir = new File(pluginLocation);
		Logger.log.info ("Looking for plugins in file or directory " + dir.getAbsolutePath());
		if(dir.exists()) {
			if(dir.isDirectory()) {
				Logger.log.info("Detected plugin argument as dir");
				for(File f : dir.listFiles()) {
					loadPlugins(f.getAbsolutePath());
				}
			} else {
				Logger.log.info("Detected plugin argument as jar");
				loadPlugin(dir);
			}
		} else {
			//Otherwise, try to load the class directly
			Logger.log.info("File not found, assuming plugin argument is a class");
			loadAsClass(pluginLocation);
		}
	}
	
	/**
	 * Loads a plugin from the given jarfile
	 */
	void loadPlugin(File file) {
		Logger.log.trace("Attempt to load plugin jar: " + file);
		if(file.getName().endsWith(".jar")) {
			try {
				loadFromJar(new JarFile(file));
			} catch (IOException e) {
				Logger.log.error("\tUnable to load jar file '" + file + "'", e);
			}
		} else {
			Logger.log.trace("\tNot a jar file!");
		}
	}
	
	void loadFromJar(JarFile jarFile) {
		Logger.log.trace("\tLoading from jar file " + jarFile);
		Enumeration<?> e = jarFile.entries();
		while (e.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)e.nextElement();
			Logger.log.trace("Checking " + entry);
			String entryname = entry.getName();
			if(entryname.endsWith(".class")) {
				String cn = removeClassExt(entryname.replace('/', '.'));
				loadAsClass(cn);
			}
		}
	}
	
	void loadAsClass(String className) {
		try {
			Class<?> c = Class.forName(className);
			if(isPlugin(c)) {
				Class<Plugin> pluginClass = (Class<Plugin>)c;
				loadPlugin(pluginClass);
			}
		} catch(Throwable ex) {
			Logger.log.error("\tUnable to load plugin", ex);
		}
	}
	
	static String removeClassExt(String fn) {
		return fn.substring(0, fn.length() - 6);
	}
	
	void loadPlugin(Class<Plugin> c) throws InstantiationException, IllegalAccessException {
			c.newInstance().init();
			plugins.add(c);
			Logger.log.trace("\tLoaded plugin: " + c);
	}
	
	boolean isPlugin(Class<?> c) {
		Class<?>[] interfaces = c.getInterfaces();
		for(Class<?> i : interfaces) {
			if(i.equals(Plugin.class)) {
				return true;
			}
		}
		return false;
	}
}
