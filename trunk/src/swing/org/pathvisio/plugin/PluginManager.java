// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.util.FileUtils;

/**
 * This class loads and maintains a collection of plugins
 */
public class PluginManager {
	//static final String PLUGIN_PKG = "org.pathvisio.visualization.plugins";
	//static final String PKG_DIR = PLUGIN_PKG.replace('.', '/');
	
	Set<Class<Plugin>> plugins = new LinkedHashSet<Class<Plugin>>();
	final List<String> pluginLocations;
	
	final PvDesktop standaloneEngine;
	
	public List<String> getLocations ()
	{
		return pluginLocations;
	}
	
	public Set<Class<Plugin>> getPluginClasses ()
	{
		return plugins;
	}
	
	/**
	 * Info about a plugin (active or non-active).
	 * Gives info about
	 * <li>from which jar it was loaded, if any
	 * <li>if there were any errors
	 * <li>which parameter caused it to be loaded
	 */
	public static class PluginInfo
	{
		public Class<Plugin> plugin;
		public File jar; // may be null if it wasn't a jar
		public Throwable error; // null if there was no error
		public String param; // parameter that caused this plugin to be loaded 
	}
	
	List<PluginInfo> info = new ArrayList<PluginInfo>();
	public List<PluginInfo> getPluginInfo()
	{
		return info;
	}
	
	/**
	 * Create a plugin manager that loads plugins from the given locations
	 * @param pluginLocations A list of Strings pointing to jar files 
	 * or directories that will be searched recursively for jar files.
	 */
	public PluginManager(List<String> pluginLocations, PvDesktop standaloneEngine) {
		this.standaloneEngine = standaloneEngine;
	
		this.pluginLocations = pluginLocations;
		
		for(String s : pluginLocations) {
			loadFromParameter(s);
		}
	}
	
	/**
	 * If argument is a dir, load all jar files in that dir recursively
	 * If argument is a single jar file, load that jar file
	 * If argument is a class name, load that class from current class path
	 */
	void loadFromParameter(String param) 
	{
		//See if the plugin is a file
		File file = new File(param);
		if(file.exists()) {
			// see if it is a directory
			if(file.isDirectory()) {
				Logger.log.info ("Looking for plugins in directory " + file.getAbsolutePath());
				for(File f : FileUtils.getFiles(file, "jar", true)) {
					loadFromJar (f, param);
				}
				return;
			} 
			if(file.getName().endsWith(".jar")) 
			{
				Logger.log.info("Detected plugin argument as jar " + param);
				loadFromJar(file, param);
				return;
			}
		}		
		//Otherwise, try to load the class directly
		Logger.log.info("No jar or dir found, assuming plugin argument is a class " + param);
		PluginInfo inf = new PluginInfo();
		inf.param = param;
		loadByClassName(param, inf, null);
	}
	
	/**
	 * @param param original parameter that triggered this file to be included,
	 * this helps in understanding where plugins come from
	 */
	void loadFromJar(File file, String param) 
	{
		PluginInfo inf = new PluginInfo();
		inf.jar = file;				
		inf.param = param;
		try
		{
			JarFile jarFile = new JarFile(file);
			Logger.log.trace("\tLoading from jar file " + jarFile);
			String pluginClasses = jarFile.getManifest().getMainAttributes().getValue("PathVisio-Plugin-Class");
			if (pluginClasses == null)
			{
				Logger.log.trace("No PathVisio-Plugin-Class attribute found, scanning.");
				Enumeration<?> e = jarFile.entries();
				while (e.hasMoreElements()) {
					ZipEntry entry = (ZipEntry)e.nextElement();
					Logger.log.trace("Checking " + entry);
					String entryname = entry.getName();
					if(entryname.endsWith(".class")) {
						String cn = removeClassExt(entryname.replace('/', '.'));
						URL u = new URL("jar", "", file.toURL() + "!/");
						ClassLoader cl = new URLClassLoader(new URL[] { u }, this.getClass().getClassLoader());
						
						loadByClassName (cn, inf, cl);
						// start building a new pluginInfo, it is possible that there
						// are multiple plugin classes in a single jar
						inf = new PluginInfo();
						inf.jar = file;
						inf.param = param;
					}
				}
			}
			else
			{
				Logger.log.trace("PathVisio-Plugin-Class is " + pluginClasses);
				URL u = new URL("jar", "", file.toURL() + "!/");
				ClassLoader cl = new URLClassLoader(new URL[] { u }, this.getClass().getClassLoader());
				for (String pluginClass : pluginClasses.split("[\\s,;:]+"))
				{
					loadByClassName (pluginClass, inf, cl);
				}
			}
		}
		catch (IOException ex)
		{
			inf.error = ex;
		}
	}
	
	static String removeClassExt(String fn) {
		return fn.substring(0, fn.length() - 6);
	}
	
	/**
	 * Try to instantiate the given class by name,
	 * and capture any error in PluginInfo
	 */
	private void loadByClassName(String className, PluginInfo inf, ClassLoader cl)
	{
		try {
			Class<?> c;
			if (cl != null)
			{
				c = cl.loadClass(className);
			}
			else
			{
				c = Class.forName(className);
			}
			
			if(isPlugin(c)) {
				Class<Plugin> pluginClass = (Class<Plugin>)c;
				inf.plugin = pluginClass;
				pluginClass.newInstance().init(standaloneEngine);
				plugins.add(pluginClass);
				info.add(inf);
				Logger.log.trace("\tLoaded plugin: " + c);
			}
		} catch(Throwable ex) {
			Logger.log.error("\tUnable to load plugin", ex);
			inf.error = ex;
			info.add(inf);
		}

	}
	
	/**
	 * Check if the given class implements the @link Plugin interface 
	 */
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
