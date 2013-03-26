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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class PathVisioMain {

	/** The smoke-test option is for automated testing purposes.
	 * When set, PathVisio just tries loading plugins, and quits with exit code 0 on success or non-zero on error. */
	private static boolean isSmokeTest = false;
	
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
	public static final List<String> mustActivate = Arrays.asList (new String[] {
		"derby",
		"org.w3c.dom.events",
		"org.bridgedb.rdb",
		"org.pathvisio.core",
		"org.pathvisio.gui",
		"org.pathvisio.desktop",
		"org.pathvisio.visualization",
		"org.pathvisio.gex",
		"org.pathvisio.statistics"	
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
        
//        {"org.osgi.framework.storage.clean", "none"},
        
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
		
	private Properties getLaunchProperties()
	{	
		Properties launchProperties = new Properties();
		for (int i = 0; i < frameworkProperties.length; i++) {
			launchProperties.setProperty(frameworkProperties[i][0], frameworkProperties[i][1]);
		}
		// hides the felix cache in .PathVisio/bundle-cache
		launchProperties.setProperty("felix.cache.rootdir", getBundleCacheFile().getAbsolutePath());
		return launchProperties;
	}
	
	
	private SplashFrame frame;
	public void start() 
	{		
		frame = new SplashFrame();
		
		final SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
			protected Void doInBackground() throws Exception {
				try {
					String factoryClass = getFactoryClass();
					FrameworkFactory factory = (FrameworkFactory) Class.forName(factoryClass).newInstance();
					
					Framework framework = factory.newFramework(getLaunchProperties());
					framework.start();
					
					context = framework.getBundleContext();
					BundleLoader loader = new BundleLoader(context);
					
				 	/* load embedded bundles, i.e. all bundles that are inside pathvisio.jar */ 
			    	System.out.println("Installing bundles that are embedded in the jar.");
			    	
					Set<String> jarNames = loader.getResourceListing(PathVisioMain.class);
					int cnt = 0;
					int total = jarNames.size() + pluginLocations.size();
					
					for (String s : jarNames) 
					{
						String text = (s.length() > 50) ? s.substring(0, 50) : s;
						frame.getTextLabel().setText("<html>Install " + text + ".</html>");
						frame.repaint();
						publish(100 * (++cnt) / total);
						loader.installEmbeddedBundle(s);
					}

					frame.getTextLabel().setText("<html>Install active plugins.</html>");
					frame.repaint();
			    	System.out.println("Installing bundles from directories specified on the command-line.");
			    	for(String location : pluginLocations) {
			    		publish(100 * (++cnt) / total);
			    		loader.loadFromParameter(location);
					}
			    
					startBundles(context, loader.getBundles());
					
					frame.getTextLabel().setText("Start application.");
					frame.repaint();
				} catch(Exception ex) {
					reportException("Startup Error", ex);
					ex.printStackTrace();
				}
				return null;
			}
			
			protected void process(List<Integer> chunks) {
				for (Integer chunk : chunks) {
					frame.getProgressBar().setString("Installing modules..." + chunk + "%");
					frame.getProgressBar().setValue(chunk);
					frame.repaint();
				}
			}
				
			protected void done() {
				frame.setVisible(false);
			}
			
		};
	
		worker.execute();
	}

	private void startBundles(BundleContext context, Map<Bundle, String> bundles) throws BundleException 
	{
    	Set<String> mustActivateLeft = new HashSet<String>();
    	mustActivateLeft.addAll(mustActivate);
    	
    	Bundle activateLast = null;    	
		for (Map.Entry<Bundle, String> e : bundles.entrySet()) 
    	{
			Bundle b = e.getKey();
			if ("org.pathvisio.desktop".equals (b.getSymbolicName()))
			{
				// must be activated last
				activateLast = b;
				continue;
			}
    		startBundle(b, mustActivateLeft);
    	}
		
		System.out.println ("Saved org.pathvisio.desktop for last");
		
		startBundle(activateLast, mustActivateLeft);
		
		if (mustActivateLeft.size() > 0)
		{
			StringBuilder missing = new StringBuilder();
			for (String s : mustActivateLeft) missing.append (" " + s);
			JOptionPane.showMessageDialog(null, "Fatal: some essential bundles were missing: " + missing);
			System.exit (1);
		}
		
		// if we're doing a smoke test, and we got to this point, we've completed succesfully.
		// exit with error code 0 to indicate success.
		if (isSmokeTest) System.exit(0);
    }

	/** Start a single bundle, record any exceptions and update the mustActivateLeft set */
	public void startBundle(Bundle b, Set<String> mustActivateLeft)
	{
		String symbolicName = b.getSymbolicName();
		try {
			b.start();
			
			if (mustActivateLeft.contains (symbolicName))
			{
				mustActivateLeft.remove(symbolicName);
			}    				
			String name = (symbolicName.length() > 50) ? symbolicName.substring(0, 50) : symbolicName;
			frame.getTextLabel().setText("<html>Start " + name + "</html>");
			frame.repaint();
			System.out.println("Bundle " + symbolicName + " started");
		}
		catch (Exception ex)
		{ 
			System.out.println("Core Bundle " + b.getBundleId() + " with location " + b.getLocation() + " failed to start.");
			if (mustActivateLeft.contains (symbolicName))
			{
				reportException ("Fatal: could not start bundle " + symbolicName, ex);
				System.exit (1);
			}
			else
			{
				/* Non-fatal error */
				ex.printStackTrace();
				
				// if we're doing a smoke test, exit with error code.
				if (isSmokeTest) System.exit (-1);
			}
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

	public static void parseArguments(String [] args) 
	{
		pluginLocations = new ArrayList<String>();
		for(int i = 0; i < args.length; i++) {
			if ("-v".equals(args[i])) 
			{
				// read version.props
				Properties props = new Properties();
				try
				{
					InputStream is = PathVisioMain.class.getClassLoader().getResourceAsStream ("version.props");
					if (is != null)	props.load(is);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				System.out.println("PathVisio v" + props.getProperty("pathvisio.version") + ", build " + props.getProperty("pathvisio.revision"));
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
			} else if ("--smoketest".equals(args[i]))
			{
				isSmokeTest = true;
			}
			else {
				pathwayFile = args[i];
			}
		}
    }
	
	private static File getBundleCacheFile() {
		File appDir;
		String os = System.getProperty("os.name");
		if(os.startsWith("Win")) {	
			appDir = new File(System.getenv("APPDATA"), "PathVisio");
		} else { //All other OS
			appDir = new File(System.getProperty("user.home"), ".PathVisio");
		}
		if(!appDir.exists()) appDir.mkdirs();
		return new File(appDir.getAbsolutePath(), "bundle-cache");
	}
		
	private static boolean isArgument(String string) {
		if(string.equals("-p") || string.equals("-v") || string.equals("-h") || string.equals("-d") || string.equals("--smoketest")) {
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
				
				/* NOTE: the --smoketest option is not documented on purpose
				 * It's not for use by end-users. */
		);
	}

	/**
	 * An error dialog that is slightly more user-friendly than a stack dump.
	 */
	static void reportException(String message, Exception ex)
	{
		Throwable cause = ex;
		// get ultimate cause
		while (cause.getCause() != null) cause = cause.getCause();
		JOptionPane.showMessageDialog(null, message + "\n" +
				"Cause: " + cause.getClass().getName() + ":  " + cause.getMessage() + "\n" +
				"Please contact PathVisio developers");
		ex.printStackTrace();
	}
}
