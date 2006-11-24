package gmmlVision;

import graphics.GmmlDrawing;

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

import preferences.GmmlPreferences;
import util.Utils;
import data.ConverterException;
import data.DBConnector;
import data.GmmlData;
import debug.Logger;

/**
 * This class contains the essential parts of the program: the window, drawing and gpml data
 */
public abstract class GmmlVision {
	public static final String APPLICATION_NAME = "PathVisio";
	public static final String PATHWAY_FILE_EXTENSION = "gpml";
	
	/**
	 * the transparent color used in the icons for visualization of protein/mrna data
	 */
	public static final RGB TRANSPARENT_COLOR = new RGB(255, 0, 255);
	
	/**
	 * {@link GmmlData} object containing JDOM representation of the gpml pathway 
	 * and handle gpml related actions
	 */
	
	static GmmlVisionWindow window;
	static GmmlDrawing drawing;
	static GmmlData gmmlData;
	
	private static ImageRegistry imageRegistry;
	private static GmmlPreferences preferences;
	public static final Logger log = new Logger();
	
	private static File DIR_APPLICATION;
	private static File DIR_DATA;
	static boolean USE_R;
		
	/**
	 * Get the {@link ApplicationWindow}, the UI of the program
	 * @return
	 */
	public static GmmlVisionWindow getWindow() {
		if(window == null) window = new GmmlVisionWindow();
		return window;
	}
	
	/**
	 * Initiates an instance of {@link GmmlVisionWindow} that is monitored by Sleak.java,
	 * to monitor what handles (to OS device context) are in use. For debug purposes only 
	 * (to check for undisposed widgets)
	 * @return The {@link GmmlVisionWindow} monitored by Sleak.java
	 */
	public static GmmlVisionWindow getSleakWindow() {
		//<DEBUG to find undisposed system resources>
		DeviceData data = new DeviceData();
		data.tracking = true;
		Display display = new Display(data);
		debug.Sleak sleak = new debug.Sleak();
		sleak.open();
		
		Shell shell = new Shell(display);
		window = new GmmlVisionWindow(shell);
		return window;
		//</DEBUG>
	}
	
	/**
	 * Get the {@link GmmlPreferences} containing the user preferences
	 * @return
	 */
	public static PreferenceStore getPreferences() { 
		if(preferences == null) preferences = new GmmlPreferences();
		return preferences; 
	}
	
	/**
	 * Get the {@link ImageRegistry} containing commonly used images
	 * @return
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
		URL url = GmmlVision.class.getClassLoader().getResource(name);
		if(url == null) log.error("Couldn't load resource '" + name + "'");
		return url;
	}
	
	/**
	 * Gets the currently open drawing
	 * @return
	 */
	public static GmmlDrawing getDrawing() {
		return drawing;
	}
		
	/**
	 * Returns the currently open GmmlData
	 * @return
	 */
	public static GmmlData getGmmlData() {
		return gmmlData;
	}
	
	/**
	 * Open a pathway from a gpml file
	 */
	public static void openPathway(String pwf)
	{
		GmmlData _gmmlData = null;
		GmmlDrawing _drawing = getWindow().createNewDrawing();
		
		// initialize new JDOM gpml representation and read the file
		try { 
			
			_gmmlData = new GmmlData();
			if (pwf.endsWith(".mapp"))
			{
				_gmmlData.readFromMapp(new File(pwf));
			}
			else
			{
				_gmmlData.readFromXml(new File(pwf), true);
			}
		} catch(ConverterException e) {
			MessageDialog.openError(getWindow().getShell(), 
					"Unable to open Gpml file", e.getClass() + e.getMessage());
			log.error("Unable to open Gpml file", e);
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
		gmmlData = new GmmlData();
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
	 * @return
	 */
	public static File getApplicationDir() {
		if(DIR_APPLICATION == null) {
			DIR_APPLICATION = new File(System.getProperty("user.home"), "." + APPLICATION_NAME);
			if(!DIR_APPLICATION.exists()) DIR_APPLICATION.mkdir();
		}
		return DIR_APPLICATION;
	}
		
	public static File getDataDir() {
		if(DIR_DATA == null) {
			DIR_DATA = new File(System.getProperty("user.home"), APPLICATION_NAME + "-Data");
			if(!DIR_DATA.exists()) DIR_DATA.mkdir();
		}
		return DIR_DATA;
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
