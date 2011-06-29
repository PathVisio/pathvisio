package org.pathvisio.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.pathvisio.core.Revision;
import org.pathvisio.core.debug.Logger;


public class PathVisioMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		parseArguments(args);
		setProgramOptions();
		new PathVisioMain().start();
	}
	
	private static final String[] coreModules = {
		"modules/org.pathvisio.core.jar",
		"modules/org.pathvisio.gui.jar",
		"modules/org.pathvisio.desktop.jar"
	};
	
	private static final String[] libs = {
		"lib/com.springsource.org.jdom-1.1.0.jar",
		"lib/org.pathvisio.xml.apis-1.7.0.jar",
		"lib/org.pathvisio.xml.apis.ext-1.7.0.jar",
		"lib/org.pathvisio.pdftranscoder-1.7.0.jar",
		"lib/commons-math-2.0.jar",
		"lib/org.pathvisio.xerces-2.5.0.jar",
		"lib/derby.jar",
		"lib/org.pathvisio.jgoodies.forms-1.2.0.jar",
		"lib/org.pathvisio.batik.gvt-1.7.0.jar",
		"lib/org.pathvisio.batik.util-1.7.0.jar",
		"lib/org.pathvisio.batik.ext-1.7.0.jar",
		"lib/org.pathvisio.batik.anim-1.7.0.jar",
		"lib/org.pathvisio.batik.awt.util-1.7.0.jar",
		"lib/org.pathvisio.batik.bridge-1.7.0.jar",
		"lib/org.pathvisio.batik.codec-1.7.0.jar",
		"lib/org.pathvisio.batik.css-1.7.0.jar",
		"lib/org.pathvisio.batik.dom-1.7.0.jar",
		"lib/org.pathvisio.batik.extension-1.7.0.jar",
		"lib/org.pathvisio.batik.parser-1.7.0.jar",
		"lib/org.pathvisio.batik.script-1.7.0.jar",
		"lib/org.pathvisio.batik.svg.dom-1.7.0.jar",
		"lib/org.pathvisio.batik.svggen-1.7.0.jar",
		"lib/org.pathvisio.batik.transcoder-1.7.0.jar",
		"lib/org.pathvisio.batik.xml-1.7.0.jar",
		"lib/org.bridgedb.jar",
		"lib/org.bridgedb.bio.jar",
		"lib/org.bridgedb.rdb.jar",
		"lib/org.bridgedb.webservice.bridgerest.jar",
		"lib/org.bridgedb.gui.jar",
		"lib/org.bridgedb.rdb.construct.jar",
		"lib/org.pathvisio.browserlaunche-1.0.0.jar",
		"lib/org.pathvisio.swingworker-1.1.0.jar",
		
	};
	
	private Properties launchProperties = new Properties();
	
	private static final String[][] frameworkProperties = { 
        {"org.osgi.framework.bootdelegation", "sun.*,com.sun.*,apple.*,com.apple.*"},
        {"org.osgi.framework.system.packages.extra", "javax.xml.parsers,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers"},
        {"org.osgi.framework.storage.clean", "onFirstInit"}
    };
	
	private String factoryClass;
	
	public PathVisioMain () throws BundleException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		BufferedReader factoryReader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("META-INF/services/org.osgi.framework.launch.FrameworkFactory")));
		factoryClass = factoryReader.readLine();
		factoryClass = factoryClass.trim();
		factoryReader.close();
	}
	
	private BundleContext context;
	
	public void start() throws InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException {
		FrameworkFactory factory = (FrameworkFactory) Class.forName(factoryClass).newInstance();
		
		for (int i = 0; i < frameworkProperties.length; i++) {
			launchProperties.setProperty(frameworkProperties[i][0], frameworkProperties[i][1]);
		}
	
		Framework framework = factory.newFramework(launchProperties);
		framework.start();
		
		context = framework.getBundleContext();
    	List<Bundle> bundles = new ArrayList<Bundle>();
    	
    	Logger.log.info("Installing third party library bundles.");
    	for(String s : libs) {
    		File file = new File(s);
    		if(!file.exists()) {
    			Logger.log.error("Could not find bundle " + s + ". PathVisio was shut down.");
    			JOptionPane.showMessageDialog(null, "Could not load bundle: " + s + ".", 
    					"Bundle Loading Error", JOptionPane.ERROR_MESSAGE);
    			System.exit(0);
    		} else {
				Bundle b = context.installBundle("" + file.toURI());
				bundles.add(b);
    		}
		}
    	
    	Logger.log.info("Installing core bundles.");
    	for(String s : coreModules) {
    		File file = new File(s);
    		if(!file.exists()) {
    			Logger.log.error("Could not find bundle " + s + ". PathVisio was shut down.");
    			JOptionPane.showMessageDialog(null, "Could not load bundle: " + s + ".", 
    					"Bundle Loading Error", JOptionPane.ERROR_MESSAGE);
    			System.exit(0);
    		} else {
	    		Bundle b = context.installBundle("" + file.toURI());
				bundles.add(b);
    		}
		}
    	
    	startBundles(context, bundles);
    	
    	Logger.log.info("Installing plug-in bundles.");
    	new PluginLoader().installPlugins(pluginLocations, context);
	}
	
	private void startBundles(BundleContext context, List<Bundle> bundles) throws BundleException {
    	for (Bundle b : bundles) {
    		boolean success = false;
    		try {
    			b.start();
    			success = true;
    			Logger.log.info("Bundle " + b.getSymbolicName() + " started");
    		}
    		finally {
    			if (!success) {
    				System.out.println("Core Bundle " + b.getBundleId() + " failed to start.");
    			}
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

	public static void parseArguments(String [] args) {
		pluginLocations = new ArrayList<String>();
		for(int i = 0; i < args.length; i++) {
			if ("-v".equals(args[i])) {
				System.out.println("PathVisio v" + Revision.VERSION + ", build " + Revision.REVISION);
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
