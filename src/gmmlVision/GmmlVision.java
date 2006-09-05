package gmmlVision;

import graphics.GmmlDrawing;

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
import data.GmmlData;
import debug.Logger;

/**
 * This class contains the essential parts of the program: the window, drawing and gmml data
 */
public abstract class GmmlVision {
	/**
	 * the transparent color used in the icons for visualization of protein/mrna data
	 */
	static final RGB TRANSPARENT_COLOR = new RGB(255, 0, 255);
	
	/**
	 * {@link GmmlData} object containing JDOM representation of the gmml pathway 
	 * and handle gmml related actions
	 */
	
	static GmmlVisionWindow window;
	static GmmlDrawing drawing;
	static GmmlData gmmlData;
	
	private static ImageRegistry imageRegistry;
	private static GmmlPreferences preferences;
	public static final Logger log = new Logger();
		
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
	 * Open a pathway from a gmml file
	 */
	public static void openPathway(String pwf)
	{
		GmmlData _gmmlData = null;
		GmmlDrawing _drawing = getWindow().createNewDrawing();
		
		// initialize new JDOM gmml representation and read the file
		try { 
			_gmmlData = new GmmlData(pwf);
		} catch(Exception e) {
			MessageDialog.openError(getWindow().getShell(), 
					"Unable to open Gmml file", e.getClass() + e.getMessage());
			log.error("Unable to open Gmml file", e);
		}
		
		if(_gmmlData != null) //Only continue if the data is correctly loaded
		{
			drawing = _drawing;
			gmmlData = _gmmlData;
			drawing.fromGmmlData(_gmmlData);
			firePropertyChange(new PropertyEvent(drawing, PROPERTY_OPEN_PATHWAY));
		}
		
	}
	
	/**
	 * Create a new pathway (drawing + gmml data)
	 */
	public static void newPathway() {
		gmmlData = new GmmlData();
		gmmlData.initMappInfo();
		drawing = getWindow().createNewDrawing();
		drawing.fromGmmlData(gmmlData);
		firePropertyChange(new PropertyEvent(drawing, PROPERTY_NEW_PATHWAY));
	}
	
	/**
	 * Find out whether a drawing is currently open or not
	 * @return true if a drawing is open, false if not
	 */
	public static boolean isDrawingOpen() { return drawing != null; }
			
	//Property event handling
	public static final String PROPERTY_OPEN_PATHWAY = "property_open_pathway";
	public static final String PROPERTY_NEW_PATHWAY = "property_new_pathway";
	
	static List<PropertyListener> propertyListeners;
	
	/**
	 * Add a {@link PropertyListener} PropertyListener, that will be notified if a
	 * property changes that has an effect throughout the program (e.g. opening a pathway)
	 * @param l The {@link PropertyListener} to add
	 */
	public static void addPropertyListener(PropertyListener l) {
		if(propertyListeners == null) 
			propertyListeners = new ArrayList<PropertyListener>();
		propertyListeners.add(l);
	}
	
	/**
	 * Fire a {@link PropertyEvent} to notify all {@link PropertyListeners} registered
	 * to this class
	 * @param e
	 */
	public static void firePropertyChange(PropertyEvent e) {
		for(PropertyListener l : propertyListeners) l.propertyChanged(e);
	}
	
	public interface PropertyListener {
		public void propertyChanged(PropertyEvent e);
	}
	
	public static class PropertyEvent extends EventObject {
		private static final long serialVersionUID = 1L;

		public Object source;
		public String name;
		public String oldValue;
		public String newValue;
		
		public PropertyEvent(Object source, String name, String oldValue, String newValue) {
			super(source);
			this.source = source;
			this.name = name;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		public PropertyEvent(Object source, String name) {
			this(source, name, "", "");
		}
	}

}
