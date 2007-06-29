package org.pathvisio;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import org.pathvisio.data.DBConnector;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayExporter;
import org.pathvisio.model.PathwayImporter;
import org.pathvisio.preferences.swt.SwtPreferences.SwtPreference;
import org.pathvisio.util.Utils;
import org.pathvisio.view.VPathway;

public class Engine {
	private static Pathway pathway;
	private static VPathway vPathway;
		
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
	
	public final static Logger log = new Logger();
			
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
	public static VPathway getActiveVPathway() {
		return vPathway;
	}

	/**
	 * Sets the currently active Pathway and its VPathway 
	 * (Used by SwtEngine, will be removed after finishing Graphics2D transision)
	 * @param vpw
	 */
	@Deprecated
	public static void setActiveVPathway(Pathway pw, VPathway vpw) {
		pathway = pw;
		vPathway = vpw;
	}
	
	/**
	 * Returns the currently open Pathway
	 */
	public static Pathway getActivePathway() {
		return pathway;
	}
	
	/**
	 * application global clipboard.
	 */
	public static List<PathwayElement> clipboard = null;
	
	public static void importPathway(File file) throws ConverterException {
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
		pathway = _pathway;
		fireApplicationEvent(new ApplicationEvent(pathway, ApplicationEvent.PATHWAY_OPENED));
	}
	
	public static void openPathway(String fileName) throws ConverterException {
		openPathway(new File(fileName));
	}
	
	/**
	 * Open a pathway from a gpml file
	 */
	public static void openPathway(File pathwayFile) throws ConverterException
	{
		Pathway _pathway = null;		
		String pwf = pathwayFile.toString();
		
		// initialize new JDOM gpml representation and read the file
		_pathway = new Pathway();
		if (pwf.endsWith(".mapp"))
		{
			_pathway.readFromMapp(new File(pwf));
		}
		else
		{
			_pathway.readFromXml(new File(pwf), true);
		}
		if(_pathway != null) //Only continue if the data is correctly loaded
		{
			pathway = _pathway;
			fireApplicationEvent(new ApplicationEvent(pathway, ApplicationEvent.PATHWAY_OPENED));
		}
		
	}
	
	public static void savePathway() throws ConverterException {
		savePathway(pathway.getSourceFile());
	}
	
	public static void savePathway(File toFile) throws ConverterException {
		pathway.writeToXml(toFile, true);
	}
	
	private static void createVPathway(Pathway p) {
		vPathway.fromGmmlData(pathway);
		fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.VPATHWAY_CREATED));
	}
		
	/**
	 * Create a new pathway and view (Pathay and VPathway)
	 */
	public static void newPathway() {
		pathway = new Pathway();
		pathway.initMappInfo();
		
		fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.PATHWAY_NEW));
	}
	
	public static void newVPathway(Pathway pathway) {
		vPathway.fromGmmlData(pathway);
	}
	
	/**
	 * Find out whether a drawing is currently open or not
	 * @return true if a drawing is open, false if not
	 */
	public static boolean isDrawingOpen() { return vPathway != null; }


	private static HashMap<String, PathwayExporter> exporters = new HashMap<String, PathwayExporter>();
	private static HashMap<String, PathwayImporter> importers = new HashMap<String, PathwayImporter>();
	/**
	 * Add a {@link PathwayExporter} that handles export of GPML to another file format
	 * @param export
	 */
	public static void addPathwayExporter(PathwayExporter export) {
		for(String ext : export.getExtensions()) {
			exporters.put(ext, export);
		}
	}

	/**
	 * Add a {@link PathwayImporter} that handles imoprt of GPML to another file format
	 * @param export
	 */
	public static void addPathwayImporter(PathwayImporter importer) {
		for(String ext : importer.getExtensions()) {
			importers.put(ext, importer);
		}
	}
	
	public static PathwayExporter getPathwayExporter(String ext) {
		return exporters.get(ext);
	}

	public static PathwayImporter getPathwayImporter(String ext) {
		return importers.get(ext);
	}
	
	public static HashMap<String, PathwayExporter> getPathwayExporters() {
		return exporters;
	}
		
	public static HashMap<String, PathwayImporter> getPathwayImporters() {
		return importers;
	}
	
	public static DBConnector getDbConnector(int type) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		DBConnector connector = null;
		String className = null;
		switch(type) {
		case DBConnector.TYPE_GDB:
			className = SwtPreference.SWT_DB_ENGINE_GDB.getValue();
			break;
		case DBConnector.TYPE_GEX:
			className = SwtPreference.SWT_DB_ENGINE_GDB.getValue();
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
		
	private static List<ApplicationEventListener> applicationEventListeners  = new ArrayList<ApplicationEventListener>();
	
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
		public static final int PATHWAY_OPENED = 1;
		public static final int PATHWAY_NEW = 2;
		public static final int APPLICATION_CLOSE = 3;
		public static final int VPATHWAY_CREATED = 4;

		public Object source;
		public int type;
		
		public ApplicationEvent(Object source, int type) {
			super(source);
			this.source = source;
			this.type = type;
		}
	}
}