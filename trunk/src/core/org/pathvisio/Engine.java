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
package org.pathvisio;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayExporter;
import org.pathvisio.model.PathwayImporter;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.FileUtils;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayWrapper;

/**
 * This class manages loading, importing and exporting a Pathway and VPathway together.
 * 
 * TODO: there are some unrelated Global functions in here, but the intention is to move them away in the future. 
 */
public class Engine 
{	
	private VPathway vPathway; // may be null
	//TODO: standalone below is a hack to make Converter work
	private Pathway standalone = null; // only used when vPathway is null
	private VPathwayWrapper wrapper; // may also be null in case you
									 // don't need to interact with
									 // the pathway.
		
	public static final String SVG_FILE_EXTENSION = "svg";
	public static final String SVG_FILTER_NAME = "Scalable Vector Graphics (*." + SVG_FILE_EXTENSION + ")";
	public static final String PATHWAY_FILE_EXTENSION = "gpml";
	public static final String PATHWAY_FILTER_NAME = "PathVisio Pathway (*." + PATHWAY_FILE_EXTENSION + ")";
	public static final String GENMAPP_FILE_EXTENSION = "mapp";
	public static final String GENMAPP_FILTER_NAME = "GenMAPP Pathway (*." + GENMAPP_FILE_EXTENSION + ")";
	
	// use Engine.init() to create an Engine
	private Engine()
	{
		PreferenceManager.init();
	}
	
	/**
	 * the transparent color used in the icons for visualization of protein/mrna data
	 */
	public static final Color TRANSPARENT_COLOR = new Color(255, 0, 255);
	
	private static Engine currentEngine;
	
	/**
	 * Get the current instance of Engine
	 * @return
	 * @deprecated
	 */
	public static Engine getCurrent() 
	{
		if(currentEngine == null) 
		{
			throw new IllegalArgumentException ("Current Engine was not initialized!");
		}
		return currentEngine;
	}
	
	//TODO: use constructor instead of init()
	public static Engine init()
	{
		if (currentEngine != null)
		{
			Logger.log.warn ("Tried to initialize Engine for the second time");
		}
		else
		{
			currentEngine = new Engine();
		}
		return currentEngine;
	}
	
	/**
	 * Sets currentEngine to null.
	 */
	public static void destroy() {
		currentEngine = null;
	}
	
	/**
	 * Set the current Engine
	 * Any previous Engine will be lost
	 * @param e
	 */
	public static void setCurrent(Engine e) {
		currentEngine = e;
	}

	private File DIR_APPLICATION;
	private File DIR_DATA;
	
	/**
	 * Get the working directory of this application
	 */
	public File getApplicationDir() {
		if(DIR_APPLICATION == null) {
			DIR_APPLICATION = new File(System.getProperty("user.home"), ".PathVisio");
			if(!DIR_APPLICATION.exists()) DIR_APPLICATION.mkdir();
		}
		return DIR_APPLICATION;
	}
		
	public File getDataDir() {
		if(DIR_DATA == null) {
			DIR_DATA = new File(System.getProperty("user.home"), "PathVisio-Data");
			if(!DIR_DATA.exists()) DIR_DATA.mkdir();
		}
		return DIR_DATA;
	}
	
	/**
	   Set this to the toolkit-specific wrapper before opening or
	   creating a new pathway otherwise Engine can't create a vPathway.
	 */
	public void setWrapper (VPathwayWrapper value)
	{
		wrapper = value;
	}
	
	/**
	 * Gets the currently open drawing
	 */
	public VPathway getActiveVPathway() {
		return vPathway;
	}

	/**
	 * Returns the currently open Pathway
	 */
	public Pathway getActivePathway()
	{
		if (vPathway == null)
		{
			return standalone;
		}
		else
		{
			return vPathway.getPathwayModel();
		}
	}
		
	public void exportPathway(File file) throws ConverterException {
		Logger.log.trace("Exporting pathway to " + file);
		String fileName = file.toString();

		int dot = fileName.lastIndexOf('.');
		String ext = null;
		if(dot >= 0) {
			ext = fileName.substring(dot + 1, fileName.length());
		}
		if (ext.toLowerCase().equals("mapp") &&
				!System.getProperty("os.name").contains("Windows"))
		{
			throw new ConverterException ("MAPP format is only available on Windows operating systems");
		}
		PathwayExporter exporter = getPathwayExporter(ext);

		if(exporter == null) throw new ConverterException( "No exporter for '" + ext +  "' files" );

		exporter.doExport(file, getActivePathway());	
	}
	
	public void importPathway(File file) throws ConverterException
	{
		Logger.log.trace("Importing pathway from " + file);
		String fileName = file.toString();
		
		int dot = fileName.lastIndexOf('.');
		String ext = Engine.PATHWAY_FILE_EXTENSION; //
		if(dot >= 0) {
			ext = fileName.substring(dot + 1, fileName.length());
		}
		PathwayImporter importer = getPathwayImporter(ext);
		
		if(importer == null) throw new ConverterException( "No importer for '" + ext +  "' files" );
		
		Pathway _pathway = new Pathway();
		importer.doImport(file, _pathway);
		createVPathway(_pathway);
		fireApplicationEvent(new ApplicationEvent(_pathway, ApplicationEvent.PATHWAY_OPENED));
		if (vPathway != null)
		{
			fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.VPATHWAY_OPENED));
		}
	}
		
	/**
	 * Open a pathway from a gpml file
	 */
	public void openPathway(File pathwayFile) throws ConverterException
	{
		Pathway _pathway = null;		
		String pwf = pathwayFile.toString();
		
		// initialize new JDOM gpml representation and read the file
		_pathway = new Pathway();
		_pathway.readFromXml(new File(pwf), true);
		//Only set the pathway field after the data is loaded
		//(Exception thrown on error, this part will not be reached)
		createVPathway(_pathway);
		fireApplicationEvent(new ApplicationEvent(_pathway, ApplicationEvent.PATHWAY_OPENED));
		if (vPathway != null)
		{
			fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.VPATHWAY_OPENED));
		}
	}
	
	public File openPathway(URL url) throws ConverterException {
		String protocol = url.getProtocol();
		File f = null;
		if(protocol.equals("file")) {
			f = new File(url.getFile());
			openPathway(f);
		} else {
			try {
				f = File.createTempFile("urlPathway", "." + Engine.PATHWAY_FILE_EXTENSION);
				FileUtils.downloadFile(url, f);
				openPathway(f);
			} catch(Exception e) {
				throw new ConverterException(e);
			}
		}
		return f;
	}
	
	/**
	 * Save the pathway
	 * @param p	The pathway to save
	 * @param toFile The file to save to
	 * @throws ConverterException
	 */
	public void savePathway(Pathway p, File toFile) throws ConverterException {
		// make sure there are no problems with references.
		p.fixReferences();
		p.writeToXml(toFile, true);
	}
	
	/**
	 * Save the currently active pathway
	 * @param toFile	The file to save to
	 * @throws ConverterException
	 */
	public void savePathway(File toFile) throws ConverterException
	{
		savePathway(getActivePathway(), toFile);
	}

	/**
	   Try to make a vpathway,
	   replacing pathway with a new one.
	 */
	public void createVPathway(Pathway p)
	{
		if (wrapper == null)
		{
			standalone = p;
		}
		else
		{
			double zoom = 100;
			if(hasVPathway()) zoom = getActiveVPathway().getPctZoom();
			
			vPathway = wrapper.createVPathway();
			vPathway.fromModel(p);
			
			vPathway.setPctZoom(zoom);
			fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.VPATHWAY_CREATED));
		}
	}

	/**
	   used by undo manager
	 */
	public void replacePathway (Pathway p)
	{
		vPathway.replacePathway (p);		
		fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.VPATHWAY_CREATED));
	}
	
	/**
	 * Create a new pathway and view (Pathay and VPathway)
	 */
	public void newPathway() {
		Pathway pathway = new Pathway();
		pathway.initMappInfo();
		
		createVPathway(pathway);	
		fireApplicationEvent(new ApplicationEvent(pathway, ApplicationEvent.PATHWAY_NEW));
		if (vPathway != null)
		{
			fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.VPATHWAY_NEW));
		}
	}

	/**
	 * Find out whether a drawing is currently open or not
	 * @return true if a drawing is open, false if not
	 * @deprecated use {@link #hasVPathway}
	 */
	public boolean isDrawingOpen() { return vPathway != null; }
	
	/**
	 * Find out whether a VPathway is currently available or not
	 * @return true if a VPathway is currently available, false if not
	 */
	public boolean hasVPathway() { return vPathway != null; }

	private HashMap<String, PathwayExporter> exporters = new HashMap<String, PathwayExporter>();
	private HashMap<String, PathwayImporter> importers = new HashMap<String, PathwayImporter>();
	/**
	 * Add a {@link PathwayExporter} that handles export of GPML to another file format
	 * @param export
	 */
	public void addPathwayExporter(PathwayExporter export) {
		for(String ext : export.getExtensions()) {
			exporters.put(ext, export);
		}
	}

	/**
	 * Add a {@link PathwayImporter} that handles imoprt of GPML to another file format
	 * @param export
	 */
	public void addPathwayImporter(PathwayImporter importer) {
		for(String ext : importer.getExtensions()) {
			importers.put(ext, importer);
		}
	}
	
	public PathwayExporter getPathwayExporter(String ext) {
		return exporters.get(ext);
	}

	public PathwayImporter getPathwayImporter(String ext) {
		return importers.get(ext);
	}
	
	public HashMap<String, PathwayExporter> getPathwayExporters() {
		return exporters;
	}
		
	public HashMap<String, PathwayImporter> getPathwayImporters() {
		return importers;
	}
		
	private List<ApplicationEventListener> applicationEventListeners  = new ArrayList<ApplicationEventListener>();
	
	/**
	 * Add an {@link ApplicationEventListener}, that will be notified if a
	 * property changes that has an effect throughout the program (e.g. opening a pathway)
	 * @param l The {@link ApplicationEventListener} to add
	 */
	public void addApplicationEventListener(ApplicationEventListener l) 
	{
		if (l == null) throw new NullPointerException();
		applicationEventListeners.add(l);
	}
	
	public void removeApplicationEventListener(ApplicationEventListener l) {
		applicationEventListeners.remove(l);
	}
	
	/**
	 * Fire a {@link ApplicationEvent} to notify all {@link ApplicationEventListener}s registered
	 * to this class
	 * @param e
	 */
	private void fireApplicationEvent(ApplicationEvent e) {
		for(ApplicationEventListener l : applicationEventListeners) l.applicationEvent(e);
	}
	
	public interface ApplicationEventListener {
		public void applicationEvent(ApplicationEvent e);
	}

	String appName = "Application name undefined";
	/**
	 * Return full application name, including version No.
	 */
	public String getApplicationName()
	{
		return appName; 
	}
	
	public void setApplicationName (String value)
	{
		appName = value;
	}
	
	/**
	 * Fire a close event
	 * TODO: move APPLICATION_CLOSE to other place
	 */
	public void close()
	{
		ApplicationEvent e = new ApplicationEvent(this, ApplicationEvent.APPLICATION_CLOSE);
		fireApplicationEvent(e);
	}
	
	//TODO:
	// Constants for swing version.
	//public static final String APPLICATION_VERSION_NAME = "PathVisio (swing version)";
//	public static final boolean fUseExperimentalFeatures = false;
//	public static final boolean IS_APPLET = false;

	// Constants for swt version, v1
	//public static final String APPLICATION_VERSION_NAME = "PathVisio 1.1 (trunk)";
//	public static final boolean fUseExperimentalFeatures = false;
//	public static final String HELP_URL = "http://wiki.bigcat.unimaas.nl/pathvisio/Help";

	// Constants for applet:
//	public static final String APPLICATION_VERSION_NAME = "PathVisio.WikiPathways";
//	public static final String HELP_URL = "http://wiki.bigcat.unimaas.nl/pathvisio/Help";
//	public static final String SERVER_NAME = "WikiPathways.org";
	// for inclusion in certain error messages.
//	public static final String DEVELOPER_EMAIL = "thomas.kelder@bigcat.unimaas.nl";
//	public static final boolean IS_APPLET = true;

}