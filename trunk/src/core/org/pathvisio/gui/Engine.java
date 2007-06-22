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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.SWT;
import org.pathvisio.Globals;
import org.pathvisio.data.DBConnector;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.Sleak;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayExporter;
import org.pathvisio.preferences.Preferences;
import org.pathvisio.util.Utils;
import org.pathvisio.util.SwtUtils.SimpleRunnableWithProgress;
import org.pathvisio.view.VPathway;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 This class contains the essential parts of the program: the window, drawing and gpml data
 It takes care of some basic Document handling facilities such as:
 - creating a new document
 - load / save / save as
 - asking if a changed file should be saved before closing
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
	
	private static MainWindow window;
	private static VPathway vPathway;
	private static Pathway pathway;
	
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
	   Updates the title of the main window.
	   Call at initialization of the program,
	   whenever the filename of the current document has changed,
	   or the change status has changed.
	*/
	public static void updateTitle()
	{
		if (pathway == null)
		{
			window.getShell().setText(Globals.APPLICATION_VERSION_NAME);
		}
		else
		{
			// get filename, or (New Pathway) if current pathway hasn't been opened yet
			String fname = (pathway.getSourceFile() == null) ? "(New Pathway)" :
				pathway.getSourceFile().getName();
			window.getShell().setText(
				"*" + fname + " - " +
				Globals.APPLICATION_VERSION_NAME
				);
		}
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
	public static VPathway getVPathway() {
		return vPathway;
	}
		
	/**
	 * Returns the currently open Pathway
	 */
	public static Pathway getPathway() {
		return pathway;
	}
	
	/**
	 * application global clipboard.
	 */
	public static List<PathwayElement> clipboard = null;
	
	/**
	 Open a pathway from a gpml file
	 Asks the user if the old pathway should be discarded, if necessary
	 */
	public static void openPathway(String pwf)
	{
		if (canDiscardPathway())
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
				vPathway = _drawing;
				pathway = _gmmlData;
				vPathway.fromGmmlData(_gmmlData);
				updateTitle();
				fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.OPEN_PATHWAY));
			}
		}
	}
	
	/**
	 Create a new pathway (drawing + gpml data)
	 Asks to discard an existing pathway
	 */
	public static void newPathway()
	{
		if (canDiscardPathway())
		{
			pathway = new Pathway();
			pathway.initMappInfo();
			vPathway = getWindow().createNewDrawing();
			vPathway.fromGmmlData(pathway);
			fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.NEW_PATHWAY));
			updateTitle();
		}
	}

	/**
	   Opens a file dialog and lets user select a file.
	   Then the pathways is saved to that file.
	   returns false if the action was cancelled by the user
	 */
	public static boolean savePathwayAs()
	{
		// Check if a gpml pathway is loaded
		if (pathway != null)
		{
			FileDialog fd = new FileDialog(window.getShell(), SWT.SAVE);
			fd.setText("Save");
			fd.setFilterExtensions(new String[] {"*." + Engine.PATHWAY_FILE_EXTENSION, "*.*"});
			fd.setFilterNames(new String[] {Engine.PATHWAY_FILTER_NAME, "All files (*.*)"});
			
			File xmlFile = pathway.getSourceFile();
			if(xmlFile != null) {
				fd.setFileName(xmlFile.getName());
				fd.setFilterPath(xmlFile.getPath());
			} else {
					fd.setFilterPath(Engine.getPreferences().getString(Preferences.PREF_DIR_PWFILES));
			}
			String fileName = fd.open();
			// Only proceed if user selected a file
			
			if(fileName == null) return false;
			
			// Append .gpml extension if not already present
			if(!fileName.endsWith("." + Engine.PATHWAY_FILE_EXTENSION)) 
				fileName += "." + Engine.PATHWAY_FILE_EXTENSION;
			
			File checkFile = new File(fileName);
			boolean confirmed = true;
			// If file exists, ask overwrite permission
			if(checkFile.exists())
			{
				confirmed = MessageDialog.openQuestion(window.getShell(),"",
													   "File already exists, overwrite?");
			}
			if(confirmed)
			{
				double usedZoom = vPathway.getPctZoom();
				// Set zoom to 100%
				vPathway.setPctZoom(100);					
				// Overwrite the existing xml file
				try
				{
					pathway.writeToXml(checkFile, true);
					updateTitle();
					// Set zoom back
					vPathway.setPctZoom(usedZoom);
				}
				catch (ConverterException e)
				{
					String msg = "While writing xml to " 
						+ checkFile.getAbsolutePath();					
					MessageDialog.openError (window.getShell(), "Error", 
											 "Error: " + msg + "\n\n" + 
											 "See the error log for details.");
					Engine.log.error(msg, e);
				}
			}
		}
		else
		{
			MessageDialog.openError (window.getShell(), "Error", 
									 "No gpml file loaded! Open or create a new gpml file first");
		}			
		return true;
	}

	/**
	   Checks if the current pathway has changes, and if so, pops up a dialog
	   offering to save.
	   This should always be called before you change pathway

	   @return returns false if the user pressed cancel. 
	   
	   TODO: Currently always asks, even if there were no changes since last save.
	 */
	static public boolean canDiscardPathway()
	{
		// checking not necessary if there is no pathway.
		if (pathway == null) return true;
		String[] opts =
		{
			IDialogConstants.YES_LABEL,
			IDialogConstants.NO_LABEL,
			IDialogConstants.CANCEL_LABEL
		};
		MessageDialog msgDlg = new MessageDialog (
			window.getShell(),
			"Save changes?",
			null,
			"Your pathway may have changed. Do you want to save?",
			MessageDialog.QUESTION,
			opts,
			0);
		int result = msgDlg.open();
		if (result == 2) // cancel
		{
			return false;
		}
		else if (result == 0) // yes
		{
			// return false if save is cancelled.
			return (savePathway());
		}
		// no
		return true;
	}
	
	/**
	 * Find out whether a drawing is currently open or not
	 * @return true if a drawing is open, false if not
	 */
	public static boolean isDrawingOpen() { return vPathway != null; }


	private static HashMap<String, PathwayExporter> exporters = new HashMap<String, PathwayExporter>();
	
	/**
	 * Add a {@link PathwayImporterExporter} that handles export of GPML to another file format
	 * @param export
	 */
	public static void addGpmlExporter(PathwayExporter export) {
		for(String ext : export.getExtensions()) {
			exporters.put(ext, export);
		}
	}
	
	public static PathwayExporter getGpmlExporter(String ext) {
		return exporters.get(ext);
	}
	
	public static HashMap<String, PathwayExporter> getGpmlExporters() {
		return exporters;
	}
	
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

	/**
	   Opens a URL in the default webbrowser.  Uses a progress dialog
	   if it takes a long time.  Shows an error message and returns
	   false if it somehow failed to open the web page.
	*/
	public static boolean openWebPage(String url, String progressMsg, String errMsg) {
		Shell shell = getWindow().getShell();
		if(shell == null || shell.isDisposed()) return false;
		
		SimpleRunnableWithProgress rwp = new SimpleRunnableWithProgress(
				Engine.class, "doOpenWebPage", new Class[] { String.class }, new Object[] { url }, null);
		SimpleRunnableWithProgress.setMonitorInfo(progressMsg, IProgressMonitor.UNKNOWN);
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, rwp);
			return true;
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			String msg = cause == null ? null : cause.getMessage();
			MessageDialog.openError(shell, "Error",
			"Unable to open web browser" +
			(msg == null ? "" : ": " + msg) +
			"\n" + errMsg);
			return false;
		} catch (InterruptedException ignore) { return false; }
	}
	
	public static void doOpenWebPage(String url) throws BrowserLaunchingInitializingException, BrowserLaunchingExecutionException, UnsupportedOperatingSystemException {
		BrowserLauncher bl = new BrowserLauncher(null);
		bl.openURLinBrowser(url);
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


/**
   save the current pathway
   returns false if the action was cancelled by the user
   
   Calls savePathwayAs if the filename of the current pathway is unknown,
   so that the user can set a location for this pathway
*/
	public static boolean savePathway()
	{
		boolean result = true;
		
		double usedZoom = vPathway.getPctZoom();
		// Set zoom to 100%
		vPathway.setPctZoom(100);			
		
        // Overwrite the existing xml file.
		// If the target file is read-only, let the user select a new pathway
		if (pathway.getSourceFile() != null && pathway.getSourceFile().canWrite())
		{
			try
			{
				pathway.writeToXml(pathway.getSourceFile(), true);
			}
			catch (ConverterException e)
			{
				String msg = "While writing xml to " 
					+ pathway.getSourceFile().getAbsolutePath();					
				MessageDialog.openError (window.getShell(), "Error", 
										 "Error: " + msg + "\n\n" + 
										 "See the error log for details.");
				Engine.log.error(msg, e);
			}
		}
		else
		{
			result = savePathwayAs();
		}
		// Set zoom back
		vPathway.setPctZoom(usedZoom);

		return result;
	}
}