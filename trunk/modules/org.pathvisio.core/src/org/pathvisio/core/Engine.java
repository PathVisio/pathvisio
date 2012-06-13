// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayExporter;
import org.pathvisio.core.model.PathwayImporter;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.core.util.Utils;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayWrapper;

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

	/**
	 * the transparent color used in the icons for visualization of protein/mrna data
	 */
	public static final Color TRANSPARENT_COLOR = new Color(255, 0, 255);

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

	//TODO: No reason to keep this in engine, it doesn't act on active pathway
	/**
	 * Exports given pathway to file. This function doesn't act on the active pathway.
	 * @param pathway pathway to export
	 * @param file file to write to.
	 * @returns a list of warnings that occurred during export, or an empty list if there were none.
	 */
	public List<String> exportPathway(File file, Pathway pathway) throws ConverterException 
	{
		Logger.log.trace("Exporting pathway to " + file);
		
		Set<PathwayExporter> set = getPathwayExporters(file);
		if (set != null && set.size() == 1)
		{
			PathwayExporter exporter = Utils.oneOf(set);
			exporter.doExport(file, pathway);
			return exporter.getWarnings();
		}
		else
			throw new ConverterException( "Could not determine exporter for '" + FileUtils.getExtension(file.toString()) +  "' files" );
	}

	public void importPathway(File file) throws ConverterException
	{
		Logger.log.trace("Importing pathway from " + file);
		
		Set<PathwayImporter> set = getPathwayImporters(file);		
		if (set != null && set.size() == 1)
		{
			PathwayImporter importer = Utils.oneOf (set);
			Pathway pathway = importer.doImport(file);
			pathway.setSourceFile(file);
			newPathwayHelper (pathway);
		}
		else
			throw new ConverterException( "Could not determine importer for '" + FileUtils.getExtension(file.toString()) +  "' files" );
	}

	/**
	 * After loading a pathway from disk,
	 * run createVPathway on EDT thread to prevent concurrentModificationException
	 */
	private void newPathwayHelper(final Pathway pathway) throws ConverterException
	{
		try {
			// switch back to EDT
			SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					createVPathway(pathway);
					fireApplicationEvent(new ApplicationEvent(pathway, ApplicationEvent.Type.PATHWAY_OPENED));
					if (vPathway != null)
					{
						fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.Type.VPATHWAY_OPENED));
					}
				}
			});
		} catch (InterruptedException e) {
			throw new ConverterException (e);
		} catch (InvocationTargetException e) {
			throw new ConverterException (e);
		}
	}

	public void openPathwayFromMemory(Pathway pathway) throws ConverterException
	{
		newPathwayHelper(pathway);
	}
	
	/**
	 * Open a pathway from a gpml file
	 */
	public void openPathway(File pathwayFile) throws ConverterException
	{
		String pwf = pathwayFile.toString();

		// initialize new JDOM gpml representation and read the file
		final Pathway pathway = new Pathway();
		pathway.readFromXml(new File(pwf), true);
		//Only set the pathway field after the data is loaded
		//(Exception thrown on error, this part will not be reached)
		newPathwayHelper(pathway);
	}

	public File openPathway(URL url) throws ConverterException {
		//TODO insert in recent pathways
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
		fireApplicationEvent(new ApplicationEvent(p, ApplicationEvent.Type.PATHWAY_SAVE));
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
	 * opposite of createVPathway
	 */
	public void disposeVPathway()
	{
		assert (vPathway != null);
		// signal destruction of vPathway
		fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.Type.VPATHWAY_DISPOSED));
		vPathway.dispose();
		vPathway = null;
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
			if(hasVPathway())
			{
				// save zoom Level
				zoom = getActiveVPathway().getPctZoom();

				disposeVPathway();
			}

			vPathway = wrapper.createVPathway();
			vPathway.registerKeyboardActions(this);
			vPathway.activateUndoManager(this);
			vPathway.fromModel(p);

			vPathway.setPctZoom(zoom);
			fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.Type.VPATHWAY_CREATED));
		}
	}

	/**
	   used by undo manager
	 */
	public void replacePathway (Pathway p)
	{
		vPathway.replacePathway (p);
		fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.Type.VPATHWAY_CREATED));
	}

	/**
	 * Create a new pathway and view (Pathay and VPathway)
	 */
	public void newPathway() {
		Pathway pathway = new Pathway();
		pathway.initMappInfo();

		createVPathway(pathway);
		fireApplicationEvent(new ApplicationEvent(pathway, ApplicationEvent.Type.PATHWAY_NEW));
		if (vPathway != null)
		{
			fireApplicationEvent(new ApplicationEvent(vPathway, ApplicationEvent.Type.VPATHWAY_NEW));
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

	private Map<String, Set <PathwayExporter> > exporters = new HashMap<String, Set <PathwayExporter> >();
	private Map<String, Set <PathwayImporter> > importers = new HashMap<String, Set <PathwayImporter> >();
	/**
	 * Add a {@link PathwayExporter} that handles export of GPML to another file format
	 * @param export
	 */
	public void addPathwayExporter(PathwayExporter export) {
		for(String ext : export.getExtensions()) {
			Utils.multimapPut(exporters, ext.toLowerCase(), export);
		}
	}

	/**
	 * Add a {@link PathwayImporter} that handles imoprt of GPML to another file format
	 * @param export
	 */
	public void addPathwayImporter(PathwayImporter importer) {
		for(String ext : importer.getExtensions()) {
			Utils.multimapPut(importers, ext.toLowerCase(), importer);
		}
	}

	/**
	 * Find a suitable exporter for the given filename
	 * @returns null if no suitable exporter could be found
	 */
	public Set<PathwayExporter> getPathwayExporters(File f) 
	{
		return exporters.get(FileUtils.getExtension(f.toString()).toLowerCase());
	}

	/**
	 * Find exporters suitable for a given file.
	 * In case multiple importers match the file extension, the files may be inspected.
	 * @returns null if no suitable importer could be found
	 */
	public Set<PathwayImporter> getPathwayImporters(File f) 
	{
		Set<PathwayImporter> set = new HashSet<PathwayImporter>();
		
		// deep copy, so that we can safely modify our set 
		set.addAll(importers.get(FileUtils.getExtension(f.toString()).toLowerCase()));
		
		if (set != null && set.size() > 1)
		{
			Iterator<PathwayImporter> i = set.iterator();
			while (i.hasNext())
			{
				PathwayImporter j = i.next();
				if (!j.isCorrectType(f))
					i.remove();
			}
		}

		return set;
	}

	/**
	 * @returns all registered pathway exporters
	 */
	public Set<PathwayExporter> getPathwayExporters() 
	{
		return Utils.multimapValues(exporters);
	}

	/**
	 * @returns all registered pathway importers
	 */
	public Set<PathwayImporter> getPathwayImporters() 
	{
		return Utils.multimapValues(importers);
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

	/**
	 * Implement this if you want to receive events upon opening / closing
	 * pathways
	 */
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
		ApplicationEvent e = new ApplicationEvent(this, ApplicationEvent.Type.APPLICATION_CLOSE);
		fireApplicationEvent(e);
	}

	private boolean disposed = false;
	/**
	 * free all resources (such as listeners) held by this class.
	 * Owners of this class must explicitly dispose of it to clean up.
	 */
	public void dispose()
	{
		assert (!disposed);
		if (vPathway != null) disposeVPathway();
		applicationEventListeners.clear();
		disposed = true;
	}

	/** return the subversion revision at the time of building */
	public static String getRevision()
	{
		return Revision.REVISION;
	}

	/** The current PathVisio version */
	public static String getVersion()
	{
		return Revision.VERSION;
	}

}