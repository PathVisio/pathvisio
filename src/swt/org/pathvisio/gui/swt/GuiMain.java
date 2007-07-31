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
package org.pathvisio.gui.swt;

import java.io.File;
import java.io.PrintStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.pathvisio.Engine;
import org.pathvisio.data.Gdb;
import org.pathvisio.data.Gex;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.MappFormat;
import org.pathvisio.model.SvgFormat;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.swt.SwtPreferences;
import org.pathvisio.preferences.swt.SwtPreferences.SwtPreference;
import org.pathvisio.util.swt.SwtUtils;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.plugins.PluginManager;

/**
 * This class contains the main method and is responsible for initiating 
 * the application by setting up the user interface and creating all necessary objects
 */
public class GuiMain {
	
	/**
	 * Main method which will be carried out when running the program
	 */
	public static void main(String[] args)
	{		
		boolean debugHandles = false;
		for(String a : args) {
			if(		a.equalsIgnoreCase("--MonitorHandles") ||
					a.equalsIgnoreCase("-mh")) {
				debugHandles = true;
			}
			else if(a.equalsIgnoreCase("--UseR") ||
					a.equalsIgnoreCase("-ur")) {
				SwtEngine.getCurrent().USE_R = true;
			}
		}
		
		//Setup the application window
		MainWindow window = null;
		if(debugHandles)	window = SwtEngine.getCurrent().getSleakWindow();
		else				window = SwtEngine.getCurrent().getWindow();
		
		initiate();
		
		window.setBlockOnOpen(true);
		window.open();
		
		//Perform exit operations
		//TODO: implement PropertyChangeListener and fire exit property when closing
		// make classes themself responsible for closing when exit property is changed
		Gex.close();
		Gdb.close();
		//Close log stream
		Logger.log.getStream().close();
		
		Display.getCurrent().dispose();
	}
	
	/**
	 * Initiates some objects used by the program
	 */
	public static void initiate()
	{
		//initiate logger
		try { 
			GlobalPreference.FILE_LOG.setDefault(new File(SwtEngine.getCurrent().getApplicationDir(), ".PathVisioLog").toString());
			Logger.log.setStream(new PrintStream(GlobalPreference.FILE_LOG.getValue())); 
		} catch(Exception e) {}
		Logger.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level
		
		//load the preferences
		loadPreferences();
		
		//initiate Gene database (to load previously used gdb)
		Gdb.init();
		
		//load visualizations and plugins
		loadVisualizations();
		
		//create data directories if they don't exist yet
		createDataDirectories();
		
		//register listeners for static classes
		registerListeners();
				
		registerExporters();
		
		//NOTE: ImageRegistry will be initiated in "createContents" of MainWindow,
		//since the window has to be opened first (need an active Display)
	}
	
	/**
	 * Creates data directories stored in preferences (if not exist)
	 */
	static void createDataDirectories() {
		Preference[] dirPrefs = new Preference[] {
				SwtPreference.SWT_DIR_EXPR,
				SwtPreference.SWT_DIR_GDB,
				SwtPreference.SWT_DIR_PWFILES,
				SwtPreference.SWT_DIR_RDATA,
		};
		for(Preference p : dirPrefs) {
			File dir = new File(p.getValue());
			if(!dir.exists()) dir.mkdir();
		}
	}
	
			
	static void registerListeners() {
		VisualizationManager vmgr = new VisualizationManager();
		Gex gex = new Gex();
		
		Engine.getCurrent().addApplicationEventListener(vmgr);
		Engine.getCurrent().addApplicationEventListener(gex);
	}
	
	static void registerExporters() {
		Engine.getCurrent().addPathwayExporter(new MappFormat());
		Engine.getCurrent().addPathwayExporter(new SvgFormat());
		Engine.getCurrent().addPathwayExporter(new ImageExporter(ImageExporter.TYPE_PNG));
		Engine.getCurrent().addPathwayExporter(new ImageExporter(ImageExporter.TYPE_TIFF));
		Engine.getCurrent().addPathwayExporter(new ImageExporter(ImageExporter.TYPE_PDF));
	}
	
	static void loadVisualizations() {
		//load visualization plugins
		try {
			PluginManager.loadPlugins();
		} catch (Throwable e) {
			Logger.log.error("When loading visualization plugins", e);
		}
		
		VisualizationManager.loadGeneric();
	}
	
	static void loadPreferences() {
		Engine.getCurrent().setPreferenceCollection(new SwtPreferences());
	}
	
	/**
	 * Loads images used throughout the applications into an {@link ImageRegistry}
	 */
	static void loadImages(Display display)
	{
		ClassLoader cl = GuiMain.class.getClassLoader();
	
		ImageRegistry imageRegistry = new ImageRegistry(display);
		
		// Labels for color by expressiondata (mRNA and Protein)
		ImageData img = new ImageData(cl.getResourceAsStream("images/mRNA.bmp"));
		img.transparentPixel = img.palette.getPixel(SwtUtils.color2rgb(Engine.getCurrent().TRANSPARENT_COLOR));
		imageRegistry.put("data.mRNA",
				new Image(display, img));
		img = new ImageData(cl.getResourceAsStream("images/protein.bmp"));
		img.transparentPixel = img.palette.getPixel(SwtUtils.color2rgb(Engine.getCurrent().TRANSPARENT_COLOR));
		imageRegistry.put("data.protein",
				new Image(display, img));
		imageRegistry.put("sidepanel.minimize",
				ImageDescriptor.createFromURL(cl.getResource("icons/minimize.gif")));
		imageRegistry.put("sidepanel.hide",
				ImageDescriptor.createFromURL(cl.getResource("icons/close.gif")));
		imageRegistry.put("shell.icon", 
				ImageDescriptor.createFromURL(cl.getResource("images/bigcateye.gif")));
		imageRegistry.put("about.logo",
				ImageDescriptor.createFromURL(cl.getResource("images/logo.jpg")));
						imageRegistry.put("checkbox.unchecked",
				ImageDescriptor.createFromURL(cl.getResource("icons/unchecked.gif")));
		imageRegistry.put("checkbox.unavailable",
				ImageDescriptor.createFromURL(cl.getResource("icons/unchecked_unavailable.gif")));
		imageRegistry.put("checkbox.checked",
				ImageDescriptor.createFromURL(cl.getResource("icons/checked.gif")));
		imageRegistry.put("tree.collapsed",
				ImageDescriptor.createFromURL(cl.getResource("icons/tree_collapsed.gif")));
		imageRegistry.put("tree.expanded",
				ImageDescriptor.createFromURL(cl.getResource("icons/tree_expanded.gif")));
		SwtEngine.getCurrent().setImageRegistry(imageRegistry);
	}
	
}

