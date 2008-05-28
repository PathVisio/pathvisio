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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.Revision;
import org.pathvisio.data.DataException;
import org.pathvisio.data.GexManager;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.DataNodeListExporter;
import org.pathvisio.model.EUGeneExporter;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.MappFormat;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.swt.SwtUtils;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.plugins.PluginManager;
import org.pathvisio.view.MIMShapes;

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
		Engine.init();
		initLogs();
		SwtEngine.init(Engine.getCurrent());
		Engine.getCurrent().setApplicationName("PathVisio 1.0.2");
		if(debugHandles)	window = SwtEngine.getCurrent().getSleakWindow();
		else				window = SwtEngine.getCurrent().getWindow();
				
		initiate();
		checkUpdates();
		
		window.setBlockOnOpen(true);

		Logger.log.trace ("Open window");
		window.open();
		Logger.log.trace ("Window closed");
		
		GexManager.close();
		try
		{
			SwtEngine.getCurrent().getGdbManager().getCurrentGdb().close();
		}
		catch (DataException e)
		{
			Logger.log.error ("Problem during GdbManager.close()", e);
		}
		
		//Close log stream
		Logger.log.getStream().close();
		
		Display.getCurrent().dispose();
	}
	
	/**
	 * Checks for available updates and shows message if any available
	 */
	public static void checkUpdates() {
		try {
			URL versionUrl = new URL("http://blog.bigcat.unimaas.nl/~gmmlvisio/latestversion");
			URLConnection conn = versionUrl.openConnection();
			conn.setUseCaches(false);
			conn.setReadTimeout(500); //Don't wait too long if we're not connected to the internet
			InputStream in = (InputStream)conn.getInputStream();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));

			String theirString = bin.readLine();
			String thisString = Revision.REVISION;
			int thisVersion = -1;
			int theirVersion = -1;
			Pattern p = Pattern.compile("^[0-9]+");
			Matcher m = p.matcher(theirString);
			if(m.find()) {
				theirVersion = Integer.parseInt(theirString.substring(m.start(), m.end()));
			}
			m = p.matcher(thisString);
			if(m.find()) {
				thisVersion = Integer.parseInt(thisString.substring(m.start(), m.end()));
			}
			if(theirVersion == -1 || thisVersion == -1) {
				Logger.log.error("Invalid version number\n\tThis: " + thisString + 
						"\n\tTheirs: " + theirString);				
			} else {
				if(theirVersion > thisVersion) {
					MessageDialog.openInformation(new Shell(),
							
			"New version available",
			"There is a new version of " + Globals.APPLICATION_NAME + " avaliable.\n" +
			"Please visit http://pathvisio.org to download the newest version"
					);
				}
			}
		} catch(Exception e) {
			Logger.log.error("Unable to check application version", e);
		}
	}
	
	public static void initLogs()
	{
		String logDest = Engine.getCurrent().getPreferences().get(GlobalPreference.FILE_LOG);
		Logger.log.setDest(logDest);
		
		Logger.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level

		Logger.log.info ("Revision: " + Revision.REVISION);
		Logger.log.info ("Java version: " +
						 System.getProperty ("java.version") + ", " +
						 System.getProperty ("java.vendor"));
		Logger.log.info ("Java home: " + System.getProperty ("java.home"));
		Logger.log.info ("OS: " + System.getProperty ("os.name") +
						 System.getProperty ("os.version") + System.getProperty ("os.arch"));
		Logger.log.info ("Username: " + System.getProperty ("user.name"));
		
		Logger.log.trace ("Log initialized");
	}
	
	/**
	 * Initiates some objects used by the program
	 */
	public static void initiate()
	{

		// preferences loaded, now we can register mim shapes
		if (Engine.getCurrent().getPreferences().getBoolean (GlobalPreference.MIM_SUPPORT))
		{
			MIMShapes.registerShapes();
		}

		//load visualizations and plugins
		loadVisualizations();
		Logger.log.trace ("Plugins loaded");
		
		//create data directories if they don't exist yet
//		createDataDirectories();
		
		//register listeners for static classes
		registerListeners();
				
		registerExporters();
		registerImporters();
		
		//NOTE: ImageRegistry will be initiated in "createContents" of MainWindow,
		//since the window has to be opened first (need an active Display)
	}
	
//	/**
//	 * Creates data directories stored in preferences (if not exist)
//	 */
//	static void createDataDirectories() {
//		Preference[] dirPrefs = new Preference[] {
//				SwtPreference.SWT_DIR_EXPR,
//				SwtPreference.SWT_DIR_GDB,
//				SwtPreference.SWT_DIR_PWFILES,
//				SwtPreference.SWT_DIR_RDATA,
//		};
//		for(Preference p : dirPrefs) {
//			File dir = new File(p.getValue());
//			if(!dir.exists()) dir.mkdir();
//		}
//	}
	
			
	static void registerListeners() {
		VisualizationManager vmgr = new VisualizationManager();		
		Engine.getCurrent().addApplicationEventListener(vmgr);
	}
	
	static void registerExporters() {
		Engine.getCurrent().addPathwayExporter(new MappFormat());
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_SVG));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PNG));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_TIFF));
		Engine.getCurrent().addPathwayExporter(new BatikImageExporter(ImageExporter.TYPE_PDF));
		Engine.getCurrent().addPathwayExporter(new DataNodeListExporter(SwtEngine.getCurrent().getGdbManager()));
		Engine.getCurrent().addPathwayExporter(new EUGeneExporter());
	}
	
	static void registerImporters() {
		Engine.getCurrent().addPathwayImporter(new MappFormat());
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
	
	/**
	 * Loads images used throughout the applications into an {@link ImageRegistry}
	 */
	static void loadImages(Display display)
	{
		ClassLoader cl = GuiMain.class.getClassLoader();
	
		ImageRegistry imageRegistry = new ImageRegistry(display);
		
		// Labels for color by expressiondata (mRNA and Protein)
		ImageData img = new ImageData(cl.getResourceAsStream("mRNA.bmp"));
		img.transparentPixel = img.palette.getPixel(SwtUtils.color2rgb(Engine.TRANSPARENT_COLOR));
		imageRegistry.put("data.mRNA",
				new Image(display, img));
		img = new ImageData(cl.getResourceAsStream("protein.bmp"));
		img.transparentPixel = img.palette.getPixel(SwtUtils.color2rgb(Engine.TRANSPARENT_COLOR));
		imageRegistry.put("data.protein",
				new Image(display, img));
		imageRegistry.put("sidepanel.minimize",
				ImageDescriptor.createFromURL(cl.getResource("minimize.gif")));
		imageRegistry.put("sidepanel.hide",
				ImageDescriptor.createFromURL(cl.getResource("close.gif")));
		imageRegistry.put("shell.icon", 
				ImageDescriptor.createFromURL(cl.getResource("bigcateye.gif")));
		imageRegistry.put("about.logo",
				ImageDescriptor.createFromURL(cl.getResource("logo.jpg")));
						imageRegistry.put("checkbox.unchecked",
				ImageDescriptor.createFromURL(cl.getResource("unchecked.gif")));
		imageRegistry.put("checkbox.unavailable",
				ImageDescriptor.createFromURL(cl.getResource("unchecked_unavailable.gif")));
		imageRegistry.put("checkbox.checked",
				ImageDescriptor.createFromURL(cl.getResource("checked.gif")));
		imageRegistry.put("tree.collapsed",
				ImageDescriptor.createFromURL(cl.getResource("tree_collapsed.gif")));
		imageRegistry.put("tree.expanded",
				ImageDescriptor.createFromURL(cl.getResource("tree_expanded.gif")));
		SwtEngine.getCurrent().setImageRegistry(imageRegistry);
	}
	
}

