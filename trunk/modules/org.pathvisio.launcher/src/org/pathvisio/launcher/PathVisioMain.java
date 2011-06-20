package org.pathvisio.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;


public class PathVisioMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			// TODO: read command line options!
			
			new PathVisioMain();
		} catch (BundleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static final String[] coreModules = {
		"modules/org.pathvisio.core.jar",
		"modules/org.pathvisio.gui.jar",
		"modules/org.pathvisio.desktop.jar"
	};
	
	private static final String[] libs = {
		"bnd/com.springsource.org.jdom-1.1.0.jar",
		"bnd/org.pathvisio.xml.apis-1.7.0.jar",
		"bnd/org.pathvisio.xml.apis.ext-1.7.0.jar",
		"bnd/org.pathvisio.pdftranscoder-1.7.0.jar",
		"bnd/commons-math-2.0.jar",
		"bnd/org.pathvisio.xerces-2.5.0.jar",
		"bnd/derby.jar",
		"bnd/org.pathvisio.batik.gvt-1.7.0.jar",
		"bnd/org.pathvisio.batik.util-1.7.0.jar",
		"bnd/org.pathvisio.batik.ext-1.7.0.jar",
		"bnd/org.pathvisio.batik.anim-1.7.0.jar",
		"bnd/org.pathvisio.batik.awt.util-1.7.0.jar",
		"bnd/org.pathvisio.batik.bridge-1.7.0.jar",
		"bnd/org.pathvisio.batik.codec-1.7.0.jar",
		"bnd/org.pathvisio.batik.css-1.7.0.jar",
		"bnd/org.pathvisio.batik.dom-1.7.0.jar",
		"bnd/org.pathvisio.batik.extension-1.7.0.jar",
		"bnd/org.pathvisio.batik.parser-1.7.0.jar",
		"bnd/org.pathvisio.batik.script-1.7.0.jar",
		"bnd/org.pathvisio.batik.svg.dom-1.7.0.jar",
		"bnd/org.pathvisio.batik.svggen-1.7.0.jar",
		"bnd/org.pathvisio.batik.transcoder-1.7.0.jar",
		"bnd/org.pathvisio.batik.xml-1.7.0.jar",
		"bnd/org.pathvisio.bridgedb-1.0.3.jar",
		"bnd/org.pathvisio.bridgedb.bio-1.0.3.jar",
		"bnd/org.pathvisio.bridgedb.rdb-1.0.3.jar",
		"bnd/org.pathvisio.bridgedb.webservice.bridgerest-1.0.3.jar",
		"bnd/org.pathvisio.bridgedb.gui-1.0.3.jar",
		"bnd/org.pathvisio.bridgedb.rdb.construct-1.0.3.jar",
		"bnd/org.pathvisio.browserlaunche-1.0.0.jar",
		"bnd/org.pathvisio.jgoodies.forms-1.2.0.jar",
		"bnd/org.pathvisio.swingworker-1.1.0.jar",
		
	};
	
	private Properties launchProperties = new Properties();
	
	private static final String[][] frameworkProperties = { 
        {"org.osgi.framework.bootdelegation", "sun.*,com.sun.*,apple.*,com.apple.*"},
        {"org.osgi.framework.system.packages.extra", "javax.xml.parsers,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers"},
        {"org.osgi.framework.storage.clean", "onFirstInit"}
    };
	
	public PathVisioMain () throws BundleException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		BufferedReader factoryReader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("META-INF/services/org.osgi.framework.launch.FrameworkFactory")));
		
		String factoryClass = factoryReader.readLine();
		factoryClass = factoryClass.trim();
		factoryReader.close();
		
		System.out.println(factoryClass);
		
		FrameworkFactory factory = (FrameworkFactory) Class.forName(factoryClass).newInstance();
		
		for (int i = 0; i < frameworkProperties.length; i++) {
			launchProperties.setProperty(frameworkProperties[i][0], frameworkProperties[i][1]);
		}
		Framework framework = factory.newFramework(launchProperties);
		
		framework.start();
		
		BundleContext context = framework.getBundleContext();
		
		for(String s : libs) {
			Bundle b = context.installBundle(new File(s).toURI().toString());
			System.out.println("start " + b.getSymbolicName());
			b.start();
		}
		
		for(String s : coreModules) {
			Bundle b = context.installBundle(new File(s).toURI().toString());
			System.out.println("start " + b.getSymbolicName());
			b.start();
		}
		
		for(int i = 0; i < context.getBundles().length; i++) {
			System.out.println(context.getBundles()[i]);
		}
	}
	
}
