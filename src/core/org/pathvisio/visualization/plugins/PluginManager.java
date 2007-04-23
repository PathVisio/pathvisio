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
package org.pathvisio.visualization.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.pathvisio.gui.Engine;
import org.pathvisio.util.Utils;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationManager.VisualizationEvent;
import org.pathvisio.data.GmmlGex;

public abstract class PluginManager {
	static final String PLUGIN_PKG = "visualization.plugins";
	static final String PKG_DIR = PLUGIN_PKG.replace('.', '/');
	static final String FILE_ADD_PLUGINS = "visplugins.xml";
	static final String XML_ELEMENT = "additional-plugins";
	static final String XML_ELM_PLUGIN = "plugin";
	static final String XML_ATTR_URL = "url";
	
	static Document addDoc;
	static final Set<Class> plugins = new LinkedHashSet<Class>();
	
	public static VisualizationPlugin getInstance(Class pluginClass, Visualization v) throws Throwable {
		Constructor c = pluginClass.getConstructor(new Class[] { Visualization.class });
		return (VisualizationPlugin)c.newInstance(new Object[] { v });
	}
		
	public static VisualizationPlugin instanceFromXML(Element xml, Visualization v) throws Throwable {
		String className = xml.getAttributeValue(VisualizationPlugin.XML_ATTR_CLASS);
		
		if(className == null) throw new IllegalArgumentException(
				"Element has no '" + VisualizationPlugin.XML_ATTR_CLASS + "' attribute");
		
		Class pluginClass = Class.forName(className);
		VisualizationPlugin p = getInstance(pluginClass, v);
		p.loadXML(xml);
		return p;
	}
	
	public static Class[] getPlugins() {
		return GmmlGex.isConnected() ?
				plugins.toArray(new Class[plugins.size()]) :
				getGenericPlugins();
	}
	
	public static Class[] getGenericPlugins() {
		Set<Class> generic = new LinkedHashSet<Class>();
		for(Class pc : plugins) {
			if(isGeneric(pc)) generic.add(pc);
		}
		return generic.toArray(new Class[generic.size()]);
	}
	
	public static boolean isGeneric(Class pluginClass) {
		try {
			return getInstance(pluginClass, null).isGeneric();
		} catch(Throwable e) { 
			e.printStackTrace();
			Engine.log.error("Unable to determine if plugin is generic", e);
			return false; 
		}
	}
	
	public static String[] getPluginNames() {
		String[] names = new String[plugins.size()];
		int i = 0;
		for(Class p : plugins) {
			names[i++] = getPluginName(p);
		}
		return names;
	}
	
	public static String getPluginName(Class pluginClass) {
		try {
			VisualizationPlugin p = getInstance(pluginClass, null);
			return p.getName();
		} catch(Throwable e) {
			Engine.log.error("Unable to get plugin name for " + pluginClass, e);
			return pluginClass.getName();
		}
	}

	public static void loadPlugins() throws Throwable {	
		Engine.log.trace("> Loading visualization plugins");
		Enumeration<URL> resources = 
			Engine.class.getClassLoader().getResources(PLUGIN_PKG.replace('.', '/'));
        while (resources.hasMoreElements()) {
        	URL url = resources.nextElement();
        	Engine.log.trace("visualization.plugins package found in: " + url);
        	try {
        		loadPlugin(url);
        	} catch(Throwable e) {
        		Engine.log.error("Error when loading plugins from " + url, e);
        	}
        }
        loadAdditional();
  	}
	    
	static void loadPlugin(File f) throws Throwable {
			loadPlugin(f.toURL());
	}
	
	public static void loadAdditionalPlugin(File file) throws Throwable {
		loadPlugin(file);
		saveAdditional(file.toURL());
		VisualizationManager.fireVisualizationEvent(
				new VisualizationEvent(PluginManager.class, VisualizationEvent.PLUGIN_ADDED));
	}
		
	static void loadPlugin(URL url) throws Throwable {
    	if(url.getProtocol().equals("jar")) {
    		loadFromJar(url);
    	} else if(url.getProtocol().equals("file")) {
    		File f = new File(url.getFile());
    		if(f.getName().endsWith(".jar")) 
    			loadFromJar(url);
    		else loadFromDir(url);
    	}
		else Engine.log.error("Unable to load additional plugin", new Exception("Unsupported URL protocol"));
	}
	
	static Document getAdditionalXML() {
		if(addDoc == null) {
			File f = new File(Engine.getApplicationDir(), FILE_ADD_PLUGINS);
			if(!f.exists()) {
				return createXML();
			} else {
				SAXBuilder parser = new SAXBuilder();
				try {
					Document doc = parser.build(f);
					return doc;
				} catch(Exception e) {
					Engine.log.error("Unable to load additional plugins file", e);
					return createXML();
				}
			}
		} else return addDoc;
		
	}
	
	static Document createXML() {
		Document doc = new Document();
		doc.setRootElement(new Element(XML_ELEMENT));
		return doc;
	}

	static void loadAdditional() {
		Document doc = getAdditionalXML();
		Element root = doc.getRootElement();
		for(Object o : root.getChildren(XML_ELM_PLUGIN)) {
			URL url = null;
			try {
				url = new URL(((Element)o).getAttributeValue(XML_ATTR_URL));
				loadPlugin(new File(url.getFile()));
			} catch(Throwable ex) {
				Engine.log.error("Unable to load additional plugin", ex);
				if(url != null) removeAdditional(url);
			}
		}
	}
	
	static void saveAdditional(URL url) {
		Document doc = getAdditionalXML();
		Element root = doc.getRootElement();
		
		if(containsElement(root, url)) return;
		
		Element elm = new Element(XML_ELM_PLUGIN);
		elm.setAttribute(XML_ATTR_URL, url.toString());
		root.addContent(elm);
		saveXML(doc);
	}
	
	static void removeAdditional(URL url) {
		Document doc = getAdditionalXML();
		Element root = doc.getRootElement();
		
		Element toRemove = null;
		for(Object o : root.getChildren(XML_ELM_PLUGIN)) {
			Element e = (Element) o;
			String url1 = e.getAttributeValue(XML_ATTR_URL);
			if(url1.equals(url.toString())) {
				toRemove = e;
				break;
			}
		}
		root.removeContent(toRemove);
		saveXML(doc);
	}
	
	static void saveXML(Document doc) {
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		try {
			FileWriter fw = new FileWriter(new File(Engine.getApplicationDir(), FILE_ADD_PLUGINS));
			out.output(doc, fw);
			fw.close();
		} catch(IOException e) {
			Engine.log.error("Unable to save additional plugins", e);
		}
	}
	
	static boolean containsElement(Element root, URL url) {
		for(Object o : root.getChildren(XML_ELM_PLUGIN)) {
			String url1 = ((Element)o).getAttributeValue(XML_ATTR_URL);
			if(url1.equals(url.toString())) return true;
		}
		return false;
	}
	
	static void loadFromDir(URL url) throws Throwable {
		Engine.log.trace("\tLoading from directory " + url);
		File directory = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
		if (directory.exists()) {
            String[] files = directory.list(classFilter);
            for (String file : files)
            	addPlugin(Class.forName(PLUGIN_PKG + '.' + removeClassExt(file)));
        }
	}
	
	static void loadFromJar(URL url) throws Throwable {
		Engine.log.trace("\tLoading from jar connection " + url);
		JarFile f = null;
		if(url.getProtocol().equals("jar")) {
			JarURLConnection conn = (JarURLConnection)url.openConnection();
			f = conn.getJarFile();
		} else {
			f = new JarFile(url.getFile());
		}
		loadFromJar(f);
	}
		
	static void loadFromJar(JarFile jfile) throws Throwable {
		Throwable error = null;
		Engine.log.trace("\tLoading from jar file " + jfile);
		Enumeration e = jfile.entries();
		while (e.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)e.nextElement();
			Engine.log.trace("Checking " + entry);
			String entryname = entry.getName();
			if(entryname.startsWith(PKG_DIR) && entryname.endsWith(".class")) {
				try {
					String cn = removeClassExt(entryname.replace('/', '.').replace('$', '.'));
					Class pluginClass = Class.forName(cn);
					addPlugin(pluginClass);
				} catch(Throwable ex) {
					Engine.log.error("Unable to load plugin", ex);
					error = ex;
				}
			}
		}
		if(error != null) throw error;
	}
	
	static String removeClassExt(String fn) {
		return fn.substring(0, fn.length() - 6);
	}
	
	static void addPlugin(Class c) {
		Engine.log.trace("\t\tTrying to add " + c);
		if(isPlugin(c)) {
			Engine.log.trace("\t\t\t!> Adding " + c);
			plugins.add(c);
		}
	}
	
	static boolean isPlugin(Class c) {
		if(Modifier.isAbstract(c.getModifiers())) {
			Engine.log.trace("\t\t> Class " + c + " is not a visualization plugin (is abstract)");
			return false;
		}
		return Utils.isSubClass(c, VisualizationPlugin.class);
	}
			
	static  FilenameFilter classFilter = new FilenameFilter() {
		public boolean accept(File f, String name) {
			return name.endsWith(".class");
		}
    };
}
