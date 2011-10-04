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
package org.pathvisio.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * when the command line option -p is used
 * the plugins will be installed and started
 * 
 * TODO: currently logging = system.out (find way to log properly)
 * 
 * @author martina
 *
 */
public class PluginLoader {
	
	private BundleContext context;
	
	private List<Bundle> plugins;
	
	public void installPlugins(List<String> pluginLocations, BundleContext context) {
		plugins = new ArrayList<Bundle>();
		this.context = context;
		for(String location : pluginLocations) {
			loadFromParameter(location);
		}
	}
	
	/**
	 * If argument is a dir, load all jar files in that dir recursively
	 * If argument is a single jar file, load that jar file
	 */
	void loadFromParameter(String filePath) {
		//See if the plugin is a file
		File file = new File(filePath);
		
		if(file.exists()) {
			// see if it is a directory
			if(file.isDirectory()) {
				System.out.println("Looking for plugins in directory " + file.getAbsolutePath());
				for(File f : file.listFiles()) {
					if(f.getName().endsWith(".jar")) {
						installFromFile (f);
					}
				}
			// see if it is a jar file.
			} else if(file.getName().endsWith(".jar")) {
				System.out.println("Detected plugin argument as jar " + filePath);
				installFromFile(file);
			}
		} else {
			System.out.println("Plug-in was not installed. Could not find plug-in bundle " + filePath + ".");
		}
		
		for(Bundle bundle : plugins) {
    		try {
    			bundle.start();
    			System.out.println("Bundle " + bundle.getSymbolicName() + " started");
    		} catch (BundleException e) {
    			System.out.println("Bundle " + bundle.getSymbolicName() + " failed to start.");
			}
		}
	}
	
	/**
	 * installs a bundle from file
	 */
	void installFromFile(File file) {
		try {
			Bundle bundle = context.installBundle(file.toURI().toString());
			plugins.add(bundle);				
		} catch (BundleException e) {
			System.out.println("Could not install bundle from file " + file.getName());
			e.printStackTrace();
		}
	}
}
