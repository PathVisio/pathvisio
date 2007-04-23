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
package org.pathvisio.gmmlVision;

import java.io.File;
import java.io.PrintStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import org.pathvisio.preferences.GmmlPreferences;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.plugins.PluginManager;
import org.pathvisio.data.GmmlGdb;
import org.pathvisio.data.GmmlGex;
import org.pathvisio.model.GmmlData;

/**
 * This class contains the main method and is responsible for initiating 
 * the application by setting up the user interface and creating all necessary objects
 */
public class GmmlVisionMain {
	
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
				GmmlVision.USE_R = true;
			}
		}
		
		//Setup the application window
		GmmlVisionWindow window = null;
		if(debugHandles)	window = GmmlVision.getSleakWindow();
		else				window = GmmlVision.getWindow();
		
		initiate();
		
		window.setBlockOnOpen(true);
		window.open();
		
		//Perform exit operations
		//TODO: implement PropertyChangeListener and fire exit property when closing
		// make classes themself responsible for closing when exit property is changed
		GmmlGex.close();
		GmmlGdb.close();
		//Close log stream
		GmmlVision.log.getStream().close();
		
		Display.getCurrent().dispose();
	}
	
	/**
	 * Initiates some objects used by the program
	 */
	public static void initiate() {
		//initiate logger
		try { 
			GmmlVision.log.setStream(new PrintStream(
					GmmlVision.getPreferences().getString(GmmlPreferences.PREF_FILES_LOG))); 
		} catch(Exception e) {}
		GmmlVision.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level
		GmmlData.setLogger(GmmlVision.log);
		
		//initiate Gene database (to load previously used gdb)
		GmmlGdb.init();
		
		//load visualizations and plugins
		loadVisualizations();
		
		//create data directories if they don't exist yet
		createDataDirectories();
		
		//register listeners for static classes
		registerListeners();
				
		//NOTE: ImageRegistry will be initiated in "createContents" of GmmlVisionWindow,
		//since the window has to be opened first (need an active Display)
	}
	
	/**
	 * Creates data directories stored in preferences (if not exist)
	 */
	static void createDataDirectories() {
		String[] dirPrefs = new String[] {
				GmmlPreferences.PREF_DIR_EXPR,
				GmmlPreferences.PREF_DIR_GDB,
				GmmlPreferences.PREF_DIR_PWFILES,
				GmmlPreferences.PREF_DIR_RDATA,
		};
		for(String pref : dirPrefs) {
			File dir = new File(GmmlVision.getPreferences().getString(pref));
			if(!dir.exists()) dir.mkdir();
		}
	}
	
			
	static void registerListeners() {
		VisualizationManager vmgr = new VisualizationManager();
		GmmlGex gex = new GmmlGex();
		
		GmmlVision.addApplicationEventListener(vmgr);
		GmmlVision.addApplicationEventListener(gex);
	}
	
	static void loadVisualizations() {
		//load visualization plugins
		try {
			PluginManager.loadPlugins();
		} catch (Throwable e) {
			GmmlVision.log.error("When loading visualization plugins", e);
		}
		
		VisualizationManager.loadGeneric();
	}
	
	/**
	 * Loads images used throughout the applications into an {@link ImageRegistry}
	 */
	static void loadImages(Display display)
	{
		ClassLoader cl = GmmlVisionMain.class.getClassLoader();
	
		ImageRegistry imageRegistry = new ImageRegistry(display);
		
		// Labels for color by expressiondata (mRNA and Protein)
		ImageData img = new ImageData(cl.getResourceAsStream("images/mRNA.bmp"));
		img.transparentPixel = img.palette.getPixel(GmmlVision.TRANSPARENT_COLOR);
		imageRegistry.put("data.mRNA",
				new Image(display, img));
		img = new ImageData(cl.getResourceAsStream("images/protein.bmp"));
		img.transparentPixel = img.palette.getPixel(GmmlVision.TRANSPARENT_COLOR);
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
		GmmlVision.setImageRegistry(imageRegistry);
	}
	
}

