package org.pathvisio.launcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.util.FileUtils;

public class PluginLoader {
	
	private BundleContext context;

	public void installPlugins(List<String> pluginLocations, BundleContext context) {
		this.context = context;
		for(String s : pluginLocations) {
			System.out.println(s);
			loadFromParameter(s);
		}
	}
	
	/**
	 * If argument is a dir, load all jar files in that dir recursively
	 * If argument is a single jar file, load that jar file
	 * If argument is a class name, load that class from current class path
	 */
	void loadFromParameter(String param) {
		System.out.println(param);
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
			// see if it is a jar file.
			if(file.getName().endsWith(".jar"))
			{
				Logger.log.info("Detected plugin argument as jar " + param);
				loadFromJar(file, param);
				return;
			}
		}
		//It's not a directory or a jar file. 
		//not valid!
		Logger.log.info("No jar or dir found, assuming plugin argument is not valid.");
	}
	
	/**
	 * @param param original parameter that triggered this file to be included,
	 * this helps in understanding where plugins come from
	 */
	void loadFromJar(File file, String param)
	{
		try {
			//Disable security manager, so we can load plugin jars from
			//the .PathVisio folder, while using webstart
//			System.setSecurityManager(null);
			
			JarFile jarFile = new JarFile(file);
			Logger.log.trace("\tLoading from jar file " + jarFile);
			
			try {
				Bundle bundle = context.installBundle(file.toURI().toString());
				boolean success = false;
	    		try {
	    			bundle.start();
	    			success = true;
	    			Logger.log.info("Bundle " + bundle.getSymbolicName() + " started");
	    		}
	    		finally {
	    			if (!success) {
	    				System.out.println("Bundle " + bundle.getSymbolicName() + " failed to start.");
	    			}
	    		}
				
				
			} catch (BundleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
