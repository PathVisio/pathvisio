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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * 
 * TODO: currently logging = system.out (find way to log properly)
 */
public class BundleLoader 
{	
	private final BundleContext context;
	
	private final List<Bundle> plugins = new ArrayList<Bundle>();
	
	private final Map <Bundle, String> bundles = new HashMap <Bundle, String>();

	public BundleLoader(BundleContext context) 
	{
		this.context = context;
	}

	public void installPlugins(List<String> pluginLocations) 
	{
		for(String location : pluginLocations) {
			loadFromParameter(location);
		}
	}
	
	/**
	 * If argument is a dir, load all jar files in that dir recursively
	 * If argument is a single jar file, load that jar file
	 * This will install plugin bundles, but not start them.
	 * @param bundles 
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
	}
	
	/**
	 * installs a bundle from file
	 */
	void installFromFile(File file) {
		try {
			Bundle bundle = context.installBundle(file.toURI().toString());
			plugins.add(bundle);
			System.out.println ("Loading " + file.toURI());
			bundles.put (bundle, file.toURI().toString());
		}
		catch (BundleException e) 
		{
			if (e.getType() == BundleException.DUPLICATE_BUNDLE_ERROR)
			{
				// duplicate bundle is a warning, not an error
				System.out.println("WARNING " + file.getName() + "; " + e.getMessage());
			}
			else
			{
				System.out.println("Could not install bundle from file " + file.getName());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Install bundles that are embedded in the jar that we are currently running.
	 * If we're not running from jar, this step is simply skipped.
	 */
	void installEmbeddedBundles() throws URISyntaxException, IOException
	{
		System.out.println("Installing bundles embedded in jar.");
		Set<String> jarNames = getResourceListing(PathVisioMain.class);
		for (String s : jarNames) 
		{
			if (!s.endsWith(".jar")) continue; // skip non-jar resources.
			System.out.println ("Detected embedded bundle: " + s);
			
			URL locationURL = PathVisioMain.class.getResource('/' + s);
			if (locationURL != null)
			{
				System.out.println ("Loading " + locationURL);
				try {
					Bundle b = context.installBundle(locationURL.toString());
					bundles.put (b, s);
				}
				catch (Exception ex)
				{
					if (PathVisioMain.mustActivate.contains (s))
					{
						PathVisioMain.reportException("Could not install bundle " + s, ex);
						System.exit(1);
					}
					else
					{
						System.err.println ("Could not install bundle " + s);
						ex.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * List directory contents for the root jar. Not recursive. 
	 *
	 * @author Greg Briggs
	 *    modified LF 02/02/2011 to support java web start
	 * @param clazz
	 *            Any java class that lives in the same place as the resources
	 *            you want.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private Set<String> getResourceListing(Class<?> clazz)
			throws URISyntaxException, IOException 
	{
		String me = clazz.getName().replace(".", "/") + ".class";
		URL dirURL = clazz.getClassLoader().getResource(me);
		
		if (!dirURL.getProtocol().equals("jar")) 
		{
			System.out.println ("WARNING: Not running from a jar, can not list files for " + dirURL);
			return Collections.emptySet();
		}

		/* A JAR path */
		String protocol = dirURL.getPath().substring(0,	dirURL.getPath().indexOf(":"));
		
		Set<String> result = new HashSet<String>(); // avoid duplicates in
		// case it is a
		// subdirectory

		/* If we run locally, we'll get the file protocol */
		if ("file".equals(protocol)) {
			String jarPath = dirURL.getPath().substring(5,
					dirURL.getPath().indexOf("!")); // strip out only the
													// JAR
													// file
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); // gives ALL
															// entries
															// in jar
			while (entries.hasMoreElements()) {
				String entry = entries.nextElement().getName();
				int checkSubdir = entry.indexOf("/");
				if (checkSubdir >= 0) {
					// if it is a subdirectory, we just return the
					// directory
					// name
					entry = entry.substring(0, checkSubdir);
				}
				result.add(entry);
			}
		}
		/* If we're running webstart, we'll get http/https */ 
		if ("http".equals(protocol) || "https".equals(protocol)) {
			final ProtectionDomain domain = PathVisioMain.class.getProtectionDomain();
			final CodeSource source = domain.getCodeSource();
			URL url = source.getLocation();
			if (url.toExternalForm().endsWith(".jar")) {
				try {
					JarInputStream jarStream = new JarInputStream(url.openStream(), false);
					for (String entry : jarStream.getManifest().getEntries().keySet()) {
						result.add(entry);
					}
				}
				catch (IOException e) {
					System.err.println ("error reading manifest" + e.getMessage());
				}
			}
		}
		if (result.size() == 0)
		{
			throw new AssertionError("No files found for URL " + dirURL);
		}
		return result;
	}

	/** accessor method */
	public Map<Bundle, String> getBundles()
	{
		return bundles;
	}


}
