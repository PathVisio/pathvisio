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
package org.pathvisio.gui;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.pathvisio.view.VPathway;
import org.pathvisio.preferences.Preferences;
import org.pathvisio.util.Utils;
import org.pathvisio.data.DBConnector;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.Sleak;
import org.pathvisio.Globals;

/**
 * This class contains the essential parts of the program: the window, drawing and gpml data
 */
public abstract class Engine {
	public static final String SVG_FILE_EXTENSION = "svg";
	public static final String SVG_FILTER_NAME = "Scalable Vector Graphics (*." + SVG_FILE_EXTENSION + ")";
	public static final String PATHWAY_FILE_EXTENSION = "gpml";
	public static final String PATHWAY_FILTER_NAME = "PathVisio Pathway (*." + PATHWAY_FILE_EXTENSION + ")";
	public static final String GENMAPP_FILE_EXTENSION = "mapp";
	public static final String GENMAPP_FILTER_NAME = "GenMAPP Pathway (*." + GENMAPP_FILE_EXTENSION + ")";
	
	/**
	 * the transparent color used in the icons for visualization of protein/mrna data
	 */
	public static final RGB TRANSPARENT_COLOR = new RGB(255, 0, 255);
	
	/**
	 * {@link Pathway} object containing JDOM representation of the gpml pathway 
	 * and handle gpml related actions
	 */
	
	static MainWindow window;
	static VPathway drawing;
	static Pathway gmmlData;
	
	private static ImageRegistry imageRegistry;
	private static Preferences preferences;
	public static final Logger log = new Logger();
	
	private static File DIR_APPLICATION;
	private static File DIR_DATA;
	static boolean USE_R;
		
	/**
	 * Get the {@link ApplicationWindow}, the UI of the program
	 */
	public static MainWindow getWindow() {
		if(window == null) window = new MainWindow();
		return window;
	}
	
	/**
	 * Initiates an instance of {@link MainWindow} that is monitored by Sleak.java,
	 * to monitor what handles (to OS device context) are in use. For debug purposes only 
	 * (to check for undisposed widgets)
	 * @return The {@link MainWindow} monitored by Sleak.java
	 */
	public static MainWindow getSleakWindow() {
		//<DEBUG to find undisposed system resources>
		DeviceData data = new DeviceData();
		data.tracking = true;
		Display display = new Display(data);
		Sleak sleak = new Sleak();
		sleak.open();
		
		Shell shell = new Shell(display);
		window = new MainWindow(shell);
		return window;
		//</DEBUG>
	}
	
	/**
	 * Get the {@link Preferences} containing the user preferences
	 */
	public static PreferenceStore getPreferences() { 
		if(preferences == null) preferences = new Preferences();
		return preferences; 
	}
	
	/**
	 * Get the {@link ImageRegistry} containing commonly used images
	 */
	public static ImageRegistry getImageRegistry() { 
		if(imageRegistry == null) imageRegistry = new ImageRegistry();
		return imageRegistry; 
	}
	
	/**
	 * Set the {@link ImageRegistry} containing commonly used images
	 */
	public static void setImageRegistry(ImageRegistry _imageRegistry) {
		imageRegistry = _imageRegistry;
	}
	
	/**
	 * Get the {@link URL} for the resource stored in a jar file in the classpath
	 * @param name	the filename of the resource
	 * @return the URL pointing to the resource
	 */
	public static URL getResourceURL(String name) {
		URL url = Engine.class.getClassLoader().getResource(name);
		if(url == null) log.error("Couldn't load resource '" + name + "'");
		return url;
	}
	
	/**
	 * Gets the currently open drawing
	 */
	public static VPathway getDrawing() {
		return drawing;
	}
		
	/**
	 * Returns the currently open Pathway
	 */
	public static Pathway getGmmlData() {
		return gmmlData;
	}
	
	/**
	 * application global clipboard.
	 */
	public static List<PathwayElement> clipboard = null;
	
	/**
	 * Open a pathway from a gpml file
	 */
	public static void openPathway(String pwf)
	{
		Pathway _gmmlData = null;
		VPathway _drawing = getWindow().createNewDrawing();
		
		// initialize new JDOM gpml representation and read the file
		try { 
			
			_gmmlData = new Pathway();
			if (pwf.endsWith(".mapp"))
			{
				_gmmlData.readFromMapp(new File(pwf));
			}
			else
			{
				_gmmlData.readFromXml(new File(pwf), true);
			}
		} catch(ConverterException e) {		
			if (e.getMessage().contains("Cannot find the declaration of element 'Pathway'"))
			{
				MessageDialog.openError(getWindow().getShell(), 
						"Unable to open Gpml file", 
						"Unable to open Gpml file.\n\n" +
						"The most likely cause for this error is that you are trying to open an old Gpml file. " +
						"Please note that the Gpml format has changed as of March 2007. " +
						"The standard pathway set can be re-downloaded from http://pathvisio.org " +
						"Non-standard pathways need to be recreated or upgraded. " +
						"Please contact the authors at martijn.vaniersel@bigcat.unimaas.nl if you need help with this.\n" +
						"\nSee error log for details");
				log.error("Unable to open Gpml file", e);
			}
			else
			{
				MessageDialog.openError(getWindow().getShell(), 
						"Unable to open Gpml file", e.getClass() + e.getMessage());
				log.error("Unable to open Gpml file", e);
			}
		}
		
		if(_gmmlData != null) //Only continue if the data is correctly loaded
		{
			drawing = _drawing;
			gmmlData = _gmmlData;
			drawing.fromGmmlData(_gmmlData);
			fireApplicationEvent(new ApplicationEvent(drawing, ApplicationEvent.OPEN_PATHWAY));
		}
		
	}
	
	/**
	 * Create a new pathway (drawing + gpml data)
	 */
	public static void newPathway() {
		gmmlData = new Pathway();
		gmmlData.initMappInfo();
		drawing = getWindow().createNewDrawing();
		drawing.fromGmmlData(gmmlData);
		fireApplicationEvent(new ApplicationEvent(drawing, ApplicationEvent.NEW_PATHWAY));
	}
	
	/**
	 * Find out whether a drawing is currently open or not
	 * @return true if a drawing is open, false if not
	 */
	public static boolean isDrawingOpen() { return drawing != null; }
			
	/**
	 * Get the working directory of this application
	 */
	public static File getApplicationDir() {
		if(DIR_APPLICATION == null) {
			DIR_APPLICATION = new File(System.getProperty("user.home"), "." + Globals.APPLICATION_NAME);
			if(!DIR_APPLICATION.exists()) DIR_APPLICATION.mkdir();
		}
		return DIR_APPLICATION;
	}
		
	public static File getDataDir() {
		if(DIR_DATA == null) {
			DIR_DATA = new File(System.getProperty("user.home"), Globals.APPLICATION_NAME + "-Data");
			if(!DIR_DATA.exists()) DIR_DATA.mkdir();
		}
		return DIR_DATA;
	}
	
	public static DBConnector getDbConnector(int type) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		DBConnector connector = null;
		String className = null;
		switch(type) {
		case DBConnector.TYPE_GDB:
			className = getPreferences().getString(Preferences.PREF_DB_ENGINE_GDB);
			break;
		case DBConnector.TYPE_GEX:
			className = getPreferences().getString(Preferences.PREF_DB_ENGINE_EXPR);
			break;
		}
		if(className == null) return null;
		
		Class dbc = Class.forName(className);
		
		if(Utils.isSubClass(dbc, DBConnector.class)) {
			connector = (DBConnector)dbc.newInstance();
			connector.setDbType(type);
		}
	
		return connector;
	}
		
	public static boolean isUseR() { return USE_R; }
	
	
	static List<ApplicationEventListener> applicationEventListeners  = new ArrayList<ApplicationEventListener>();
	
	/**
	 * Add an {@link ApplicationEventListener}, that will be notified if a
	 * property changes that has an effect throughout the program (e.g. opening a pathway)
	 * @param l The {@link ApplicationEventListener} to add
	 */
	public static void addApplicationEventListener(ApplicationEventListener l) {
		applicationEventListeners.add(l);
	}
	
	/**
	 * Fire a {@link ApplicationEvent} to notify all {@link ApplicationEventListener}s registered
	 * to this class
	 * @param e
	 */
	public static void fireApplicationEvent(ApplicationEvent e) {
		for(ApplicationEventListener l : applicationEventListeners) l.applicationEvent(e);
	}
	
	public interface ApplicationEventListener {
		public void applicationEvent(ApplicationEvent e);
	}
	
	public static class ApplicationEvent extends EventObject {
		private static final long serialVersionUID = 1L;
		public static final int OPEN_PATHWAY = 1;
		public static final int NEW_PATHWAY = 2;
		public static final int CLOSE_APPLICATION = 3;

		public Object source;
		public int type;
		
		public ApplicationEvent(Object source, int type) {
			super(source);
			this.source = source;
			this.type = type;
		}
	}

}
