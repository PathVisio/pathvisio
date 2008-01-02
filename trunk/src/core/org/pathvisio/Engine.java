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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pathvisio.data.BackpageTextProvider;
import org.pathvisio.data.DBConnector;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayExporter;
import org.pathvisio.model.PathwayImporter;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceCollection;
import org.pathvisio.util.FileUtils;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayWrapper;

import sun.swing.BakedArrayList;

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
		
	/**
	 * the transparent color used in the icons for visualization of protein/mrna data
	 */
	public static final Color TRANSPARENT_COLOR = new Color(255, 0, 255);
	
	private static Engine currentEngine;
	
	/**
	 * Get the current instance of Engine
	 * @return
	 */
	public static Engine getCurrent() {
		if(currentEngine == null) currentEngine = new Engine();
		return currentEngine;
	}
	
	/**
	 * Set the current Engine
	 * Any previous Engine will be lost
	 * @param e
	 */
	public static void setCurrent(Engine e) {
		currentEngine = e;
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
	 * Get the {@link URL} for the resource stored in a jar file in the classpath
	 * @param name	the filename of the resource
	 * @return the URL pointing to the resource
	 */
	public URL getResourceURL(String name) {
		URL url = Engine.class.getClassLoader().getResource(name);
		if(url == null) Logger.log.error("Couldn't load resource '" + name + "'");
		return url;
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
	
	PreferenceCollection preferences;
	
	public void savePreferences() {
		if(preferences != null) {
			try {
				preferences.save();
			} catch(IOException e) {
				Logger.log.error("Unable to save preferences", e);
			}
		}
	}
	
	public void setPreferenceCollection(PreferenceCollection pc)
	{
		preferences = pc;
	}
	
	public PreferenceCollection getPreferenceCollection() {
		return preferences;
	}

	public void exportPathway(File file) throws ConverterException {
		Logger.log.trace("Exporting pathway to " + file);
		String fileName = file.toString();

		int dot = fileName.lastIndexOf('.');
		String ext = null;
		if(dot >= 0) {
			ext = fileName.substring(dot + 1, fileName.length());
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
		
	public void savePathway(File toFile) throws ConverterException
	{
		getActivePathway().writeToXml(toFile, true);
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
			vPathway.fromGmmlData(p);
			
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
	
	private BackpageTextProvider backpageTextProvider;
	
	/**
	 * Get the backpage text provider for this Engine.
	 * @return the backpage text provider
	 * @see BackpageTextProvider
	 */
	public BackpageTextProvider getBackpageTextProvider() {
		if(backpageTextProvider == null) {
			backpageTextProvider = new BackpageTextProvider(this);
		}
		return backpageTextProvider;
	}
	
	private HashMap<Integer, DBConnector> connectors = new HashMap<Integer, DBConnector>();
	
	public DBConnector getDbConnector(int type) throws ClassNotFoundException, InstantiationException, IllegalAccessException 
	{
		//Try to get the DBConnector from the hashmap first
		DBConnector connector = connectors.get(type);
		if(connector != null) return connector;
		
		//Else load it from the preferences
		String className = null;
		switch(type) {
		case DBConnector.TYPE_GDB:
			className = GlobalPreference.DB_ENGINE_GDB.getValue();
			break;
		case DBConnector.TYPE_GEX:
			className = GlobalPreference.DB_ENGINE_GEX.getValue();
			break;
		}
		if(className == null) return null;
		
		Class<?> dbc = Class.forName(className);
		Object o = dbc.newInstance();
		if(o instanceof DBConnector) {
			connector = (DBConnector)dbc.newInstance();
			connector.setDbType(type);
		}
		
		return connector;
	}
	
	/**
	 * Set the DBConnector for the given database type. 
	 * Overrides seting in GlobalPreference.DB_ENGINE_*
	 * @param connector
	 * @param type
	 */
	public void setDBConnector(DBConnector connector, int type) {
		connectors.put(type, connector);
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
	public void fireApplicationEvent(ApplicationEvent e) {
		for(ApplicationEventListener l : applicationEventListeners) l.applicationEvent(e);
	}
	
	public interface ApplicationEventListener {
		public void applicationEvent(ApplicationEvent e);
	}
}