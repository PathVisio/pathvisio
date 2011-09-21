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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import javax.swing.JOptionPane;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class PathVisioMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		parseArguments(args);
		setProgramOptions();
		new PathVisioMain().start();
	}

	/** 
	 * The following bundles below must 
	 * activate without exception.
	 * If there is an exception while activating one of the 
	 * bundles below, an error message will be
	 * reported directly to the user, and PathVisio shuts down.
	 * Exceptions in other bundles are merely logged. 
	 */
	private static final List<String> mustActivate = Arrays.asList (new String[] {
		"derby.jar",
		"org.bridgedb.rdb.jar",
		"org.pathvisio.core.jar",
		"org.pathvisio.gui.jar",
		"org.pathvisio.desktop.jar",
		"org.pathvisio.visualization.jar",
		"org.pathvisio.gexplugin.jar",
		"org.pathvisio.statistics.jar"	
	});	
	
	private static final String[][] frameworkProperties = { 
        
		/* org.osgi.framework.bootdelegation=*, this means that
		 * all system classes are available to any bundle.
		 * 
		 * when running under felix, the following is necessary for batik to work properly,
		 * otherwise it will throw NoClassDefFoundError's left and right.
		 */
		{"org.osgi.framework.bootdelegation", "*"},
		
        {"org.osgi.framework.system.packages.extra", "javax.xml.parsers,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers"},
        
        {"org.osgi.framework.storage.clean", "onFirstInit"},
        
        /* following property is necessary for Felix: to prevent complaints 
         * about missing requirements ee=JSE2-1.2 on the javax.xml bundle. */
        {"org.osgi.framework.executionenvironment", "ee-1.6=JavaSE-1.6,J2SE-1.5,J2SE-1.4,J2SE-1.3,J2SE-1.2,JRE-1.1,JRE-1.0,OSGi/Minimum-1.2,OSGi/Minimum-1.1,OSGi/Minimum-1.0" } 

    };
	
	/**
	 * Determine which FrameWork factory class is available by reading a property in the jar file.
	 * This makes it easy to switch between felix, equinox or another OSGi implemenation.
	 * All you have to do is include either felix.jar or org.eclipse.osgi.jar in the classpath. 
	 * @throws IOException 
	 */
	private String getFactoryClass() throws IOException
	{
		BufferedReader factoryReader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("META-INF/services/org.osgi.framework.launch.FrameworkFactory")));
		String factoryClass = factoryReader.readLine();
		factoryClass = factoryClass.trim();
		factoryReader.close();
		return factoryClass;
	}
	
	private BundleContext context;
	
	/**
	 * An error dialog that is slightly more user-friendly than a stack dump.
	 */
	private void reportException(String message, Exception ex)
	{
		Throwable cause = ex;
		// get ultimate cause
		while (cause.getCause() != null) cause = cause.getCause();
		JOptionPane.showMessageDialog(null, message + "\n" +
				"Cause: " + cause.getClass().getName() + ":  " + cause.getMessage() + "\n" +
				"Please contact PathVisio developers");
		ex.printStackTrace();
	}
	
	private Properties getLaunchProperties()
	{	
		Properties launchProperties = new Properties();
		for (int i = 0; i < frameworkProperties.length; i++) {
			launchProperties.setProperty(frameworkProperties[i][0], frameworkProperties[i][1]);
		}
		return launchProperties;
	}
	
	public void start() 
	{	
    	try
    	{
			String factoryClass = getFactoryClass();
			FrameworkFactory factory = (FrameworkFactory) Class.forName(factoryClass).newInstance();
			
			Framework framework = factory.newFramework(getLaunchProperties());
			framework.start();
			
			context = framework.getBundleContext();
			Map <Bundle, String> bundles = new HashMap <Bundle, String>();
	    	
	       	/* load embedded bundles, i.e. all bundles that are inside pathvisio.jar */ 
	    	System.out.println("Installing core bundles.");
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
		    			if (mustActivate.contains (s))
		    			{
		    				reportException("Could not install bundle " + s, ex);
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
	    	    	
	    	startBundles(context, bundles);
	    	
	    	System.out.println("Installing plug-in bundles.");
	    	new PluginLoader().installPlugins(pluginLocations, context);
    	}
    	catch (Exception ex)
    	{
    		reportException("Startup Error", ex);
    		ex.printStackTrace();
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
		
		if (!dirURL.getProtocol().equals("jar")) throw new AssertionError("Expected URL with jar protocol. " +
				"Can not list files for " + dirURL);

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

	private void startBundles(BundleContext context, Map<Bundle, String> bundles) throws BundleException 
	{
    	Set<String> mustActivateLeft = new HashSet<String>();
    	mustActivateLeft.addAll(mustActivate);
    	
		for (Map.Entry<Bundle, String> e : bundles.entrySet()) 
    	{
			Bundle b = e.getKey();
    		try {
    			b.start();
    			
    			if (mustActivateLeft.contains (e.getValue()))
    			{
    				mustActivateLeft.remove(e.getValue());
    			}    				
    			System.out.println("Bundle " + b.getSymbolicName() + " started");
    		}
    		catch (Exception ex)
    		{ 
    			System.out.println("Core Bundle " + b.getBundleId() + " failed to start.");
    			if (mustActivateLeft.contains (e.getValue()))
    			{
    				reportException ("Fatal: could not start bundle " + e.getValue(), ex);
    				System.exit (1);
    			}
    			else
    			{
    				/* Non-fatal error */
    				ex.printStackTrace();
    			}
    		}
    	}
		if (mustActivateLeft.size() > 0)
		{
			StringBuilder missing = new StringBuilder();
			for (String s : mustActivateLeft) missing.append (" " + s);
			JOptionPane.showMessageDialog(null, "Fatal: some essential bundles were missing: " + missing);
			System.exit (1);
		}
    }
	
	public static final String ARG_PROPERTY_PGEX = "pathvisio.pgex";
	public static final String ARG_PROPERTY_PATHWAYFILE = "pathvisio.pathwayfile";
	public static List<String> pluginLocations;
	public static String pgexFile;
	public static String pathwayFile;
	
	// this is only a workaround to hand over the pathway and pgex file
	// from the command line when using the launcher
	// TODO: find better solution
	private static void setProgramOptions() {
		if(pgexFile != null) {
			System.setProperty(ARG_PROPERTY_PGEX, pgexFile);
		}
		if(pathwayFile != null) {
			System.setProperty(ARG_PROPERTY_PATHWAYFILE, pathwayFile);
		}
	}

	public static void parseArguments(String [] args) {
		pluginLocations = new ArrayList<String>();
		for(int i = 0; i < args.length; i++) {
			if ("-v".equals(args[i])) {
				//TODO: getVersion() / getRevision()
//				System.out.println("PathVisio v" + Engine.getVersion() + ", build " + Engine.getRevision());
				System.exit(0);
			} else if ("-h".equals(args[i])) {
				printHelp();
				System.exit(0);
			} else if ("-p".equals(args[i])) {
				if(i+1 < args.length && !isArgument(args[i+1])) {
					pluginLocations.add(args[i+1]);
					i++;
				} else {
					System.out.println ("Missing plugin location after -p option");
					printHelp();
					System.exit(-1);
				}
			} else if ("-d".equals(args[i])) {
				if(i+1 < args.length && !isArgument(args[i+1])) {
					pgexFile = args[i+1];
					i++;
				} else {
					System.out.println ("Missing data file location after -d option");
					printHelp();
					System.exit(-1);
				}
			} else {
				pathwayFile = args[i];
			}
		}
    }
		
	private static boolean isArgument(String string) {
		if(string.equals("-p") || string.equals("-v") || string.equals("-h") || string.equals("-d")) {
			return true;
		}
		return false;
	}

	private static void printHelp() {
		System.out.println(
				"pathvisio [options] [pathway file]\n" +
				"Valid options are:\n" +
				"-p: A plugin file/directory to load\n" +
				"-d: A pgex data file to load\n" +
				"-v: displays PathVisio version\n" +
				"-h: displays this help message"
		);
	}
	
}
