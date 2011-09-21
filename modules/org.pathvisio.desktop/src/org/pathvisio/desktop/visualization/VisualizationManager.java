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
package org.pathvisio.desktop.visualization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.core.view.VPathwayEvent;
import org.pathvisio.core.view.VPathwayEvent.VPathwayEventType;
import org.pathvisio.core.view.VPathwayListener;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.GexManager.GexManagerEvent;
import org.pathvisio.desktop.gex.GexManager.GexManagerListener;

/**
 * Maintains the visualizations
 */
public class VisualizationManager implements GexManagerListener, VPathwayListener, ApplicationEventListener
{
	/** Exceptions for the Visualization Manager,
	 * such as failure while loading stored visualization xml */
	public static class VisualizationException extends Exception
	{
		public VisualizationException (String msg) { super (msg); }
		public VisualizationException (Throwable t) { super (t); }
	}

	/**
	   name of the top-level xml element
	 */
	public static final String XML_ELEMENT = "visualizations";

	private final ColorSetManager colorSetMgr;
	private final Engine engine;
	private final GexManager gexManager;
	private final VisualizationMethodRegistry methodRegistry;
	
	public VisualizationManager(Engine engine, GexManager gexManager) {
		colorSetMgr = new ColorSetManager();
		this.engine = engine;
		this.gexManager = gexManager;
		this.methodRegistry = new VisualizationMethodRegistry();
		gexManager.addListener(this);
		engine.addApplicationEventListener(this);
		VPathway vp = engine.getActiveVPathway();
		if(vp != null) {
			vp.addVPathwayListener(this);
		}
		if(gexManager.isConnected()) {
			loadXML();
		}
	}

	public VisualizationMethodRegistry getVisualizationMethodRegistry()
	{
		return methodRegistry;
	}
	
	public Engine getEngine() {
		return engine;
	}

	public ColorSetManager getColorSetManager() {
		return colorSetMgr;
	}

	/**
	   Interface for objects that want to listen to VisualizationEvents
	*/
	public interface VisualizationListener {
		public void visualizationEvent(VisualizationEvent e);
	}

	/**
	   List of all available Visualizations
	 */
	private List<Visualization> visualizations = new ArrayList<Visualization>();
	private int active = -1;

	/**
	   Obtain the currently active visualization. This is the visualization shown
	   in the open pathway.
	 */
	public Visualization getActiveVisualization() {
		if(active < 0 || active >= visualizations.size()) return null;
		return visualizations.get(active);
	}

	/**
	   Set which visualization will be active, by index
	 */
	public void setActiveVisualization(int index) {
		active = index;
		fireVisualizationEvent(
				new VisualizationEvent(
					VisualizationManager.class,
					VisualizationEvent.VISUALIZATION_SELECTED));
	}

	/**
	   Set which visualization will be active, by Object
	 */
	public void setActiveVisualization(Visualization v) {
		int index = getVisualizations().indexOf(v);
		if(index > -1) setActiveVisualization(index);
	}

	/**
	   get a List of all visualizations
	 */
	public List<Visualization> getVisualizations() {
		return visualizations;
	}

	/**
	   get a list of names of all visualizations as an array.
	 */
	public String[] getNames() {
		String[] names = new String[visualizations.size()];
		for(int i = 0; i < names.length; i++)
			names[i] = visualizations.get(i).getName();
		return names;
	}

	/**
	   add a new visualization
	 */
	public  void addVisualization(Visualization v) {
		visualizations.add(v);
		v.setVisualizationMgr(this);
		fireVisualizationEvent(
				new VisualizationEvent(
					VisualizationManager.class,
					VisualizationEvent.VISUALIZATION_ADDED));
	}

	/**
	   remove a visualization (by index)
	 */
	public  void removeVisualization(int index) {
		if(index < 0 || index >= visualizations.size()) return; //Ignore wrong index
		visualizations.remove(index);
		fireVisualizationEvent(
				new VisualizationEvent(
					VisualizationManager.class,
					VisualizationEvent.VISUALIZATION_REMOVED));
	}

	/**
	   remove a visualization (by object)
	 */
	public  void removeVisualization(Visualization v) {
		removeVisualization(visualizations.indexOf(v));
	}

	/**
	   Remove all visualizations, in response to unloading
	   expression data
	 */
	private  void clearVisualizations() {
		List<Visualization> toRemove = new ArrayList<Visualization>();
		toRemove.addAll(visualizations);
		for(Visualization v : toRemove) removeVisualization(v);
	}

	/**
	   get a new name for a visualization, that is guaranteed to be unique
	 */
	public  String getNewName() {
		String prefix = "visualization";
		int i = 1;
		String name = prefix;
		while(nameExists(name)) name = prefix + "-" + i++;
		return name;
	}

	/**
	   check if a name already exists.
	 */
	public  boolean nameExists(String name) {
		for(Visualization v : visualizations)
			if(v.getName().equalsIgnoreCase(name)) return true;
		return false;
	}

	/**
	   List of listeners
	 */
	private  Set<VisualizationListener> listeners = new HashSet<VisualizationListener>();

	/**
	 * Add a {@link VisualizationListener}, that will be notified if an
	 * event related to visualizations occurs
	 */
	public  void addListener(VisualizationListener l)
	{
		listeners.add(l);
	}

	public  void removeListener (VisualizationListener l)
	{
		listeners.remove (l);
	}

	/**
	 * Fire a {@link VisualizationEvent} to notify all {@link VisualizationListener}s registered
	 * to this class
	 */
	public  void fireVisualizationEvent(VisualizationEvent e) {
		for(VisualizationListener l : listeners) {
			l.visualizationEvent(e);
		}
	}

	/**
	 * Refreshes the vpathway and fires visualization events after
	 * a visualization has been modified.
	 * @param v The visualization that has been modified.
	 */
	protected void visualizationModified(Visualization v) {
		VPathway vp = engine.getActiveVPathway();
		if(vp != null) {
			vp.redraw();
		}
		fireVisualizationEvent(new VisualizationEvent(
			this, VisualizationEvent.VISUALIZATION_MODIFIED
		));
	}

	public void gexManagerEvent(GexManagerEvent e)
	{
		switch (e.getType())
		{
		case GexManagerEvent.CONNECTION_OPENED:
			loadXML();
			break;
		case GexManagerEvent.CONNECTION_CLOSED:
			clearVisualizations();
			break;
		default:
			assert (false); // Shouldn't occur.
		}
	}

	public static final String ROOT_XML_ELEMENT = "expression-data-visualizations";

	private InputStream getXmlInput()
	{
		File xmlFile = new File(gexManager.getCurrentGex().getDbName() + ".xml");
		Logger.log.trace("Getting visualizations xml: " + xmlFile);
		try {
			if(!xmlFile.exists()) xmlFile.createNewFile();
			InputStream in = new FileInputStream(xmlFile);
			return in;
		} catch(Exception e) {
			Logger.log.error("Unable to find visualization settings file!");
			return null;
		}
	}

	public void saveXML() throws IOException
	{
		if(!gexManager.isConnected()) return;

		// write to a temporary file, rename to final file after write was successful.
		File finalFile = new File(gexManager.getCurrentGex().getDbName() + ".xml");
		File tempFile = File.createTempFile(finalFile.getName(), ".tmp", finalFile.getParentFile());
		OutputStream out = new FileOutputStream(tempFile);

		Logger.log.trace("Saving visualizations and color sets to xml: " + out);
		Document xmlDoc = new Document();
		Element root = new Element(ROOT_XML_ELEMENT);
		xmlDoc.setRootElement(root);

		root.addContent(colorSetMgr.getXML());

		Element vis = new Element(XML_ELEMENT);
		for(Visualization v : getVisualizations()) {
			vis.addContent(v.toXML());
		}
		root.addContent(vis);

		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		xmlOut.output(xmlDoc, out);
		out.close();

		if (finalFile.exists()) finalFile.delete();
		if (!tempFile.renameTo(finalFile)) throw new IOException ("Couldn't rename temporary file " + tempFile);
	}


	/**
	   use a jdom Element to initialize the VisualizationManger
	 */
	public void loadXML(Element xml) {
		if(xml == null) return;

		Visualization last = null;
		for(Object o : xml.getChildren(Visualization.XML_ELEMENT)) {
			last = Visualization.fromXML((Element) o, methodRegistry, this);
		}
		if (last != null) this.setActiveVisualization(last);
	}

	public  void loadXML() {
		Logger.log.trace("Loading xml for visualization settings");
		Document doc = getXML();
		Element root = doc.getRootElement();
		//Load the colorsets first
		Element cs = root.getChild(ColorSetManager.XML_ELEMENT);
		colorSetMgr.fromXML(cs);
		//Load the visualizations
		Element vis = root.getChild(VisualizationManager.XML_ELEMENT);
		loadXML(vis);
		Logger.log.trace("Finished loading xml for visualization settings");
	}

	public  Document getXML() {
		InputStream in = getXmlInput();
		Document doc;
		Element root;
		try {
			SAXBuilder parser = new SAXBuilder();
			doc = parser.build(in);
			in.close();
			root = doc.getRootElement();
		} catch(Exception e) {
			doc = new Document();
			root = new Element(ROOT_XML_ELEMENT);
			doc.setRootElement(root);
		}
		return doc;
	}


	public void vPathwayEvent(VPathwayEvent e) {
		if(e.getType() == VPathwayEventType.ELEMENT_DRAWN) {
			Visualization v = getActiveVisualization();
			VPathwayElement elm = e.getAffectedElement();
			if(v != null && elm instanceof Graphics) {
				v.visualizeDrawing((Graphics)elm, e.getGraphics2D());
			}
		}
	}

	public void applicationEvent(ApplicationEvent e) {
		switch (e.getType())
		{
		case ApplicationEvent.VPATHWAY_CREATED:
			((VPathway)e.getSource()).addVPathwayListener(this);
			break;
		case ApplicationEvent.VPATHWAY_DISPOSED:
			((VPathway)e.getSource()).removeVPathwayListener(this);
			break;
		}
	}

	private boolean disposed = false;
	/**
	 * free all resources (such as listeners) held by this class.
	 * Owners of this class must explicitly dispose of it to clean up.
	 */
	public void dispose()
	{
		assert (!disposed);
		gexManager.removeListener(this);
		engine.removeApplicationEventListener(this);
		VPathway vpwy = engine.getActiveVPathway();
		if (vpwy != null)
		{
			vpwy.removeVPathwayListener(this);
		}
		disposed = true;
	}
}
