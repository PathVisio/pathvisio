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
package org.pathvisio.visualization;

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
import org.pathvisio.data.GexManager;
import org.pathvisio.data.GexManager.GexManagerEvent;
import org.pathvisio.data.GexManager.GexManagerListener;
import org.pathvisio.debug.Logger;
import org.pathvisio.visualization.colorset.ColorSetManager;

/**
 * Maintains the visualizations
 */
public class VisualizationManager implements GexManagerListener {
	private static VisualizationManager current;
	
	public static VisualizationManager getCurrent() {
		if(current == null) {
			current = new VisualizationManager(
					VisualizationMethodRegistry.getCurrent(),
					new ColorSetManager()
			);
		}
		return current;
	}
	/**
	   name of the top-level xml element
	 */
	public static final String XML_ELEMENT = "visualizations";
	
	ColorSetManager colorSetMgr;
	VisualizationMethodRegistry methodRegistry;
	
	public VisualizationManager(VisualizationMethodRegistry methodRegistry, 
			ColorSetManager colorSetMgr) {
		this.colorSetMgr = colorSetMgr;
		this.methodRegistry = methodRegistry;
		GexManager.addListener(this);
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
	public void setCurrent(int index) {
		active = index;
		fireVisualizationEvent(
				new VisualizationEvent(
					VisualizationManager.class,
					VisualizationEvent.VISUALIZATION_SELECTED));
	}

	/**
	   Set which visualization will be active, by Object	   
	 */
	public void setCurrent(Visualization v) {
		int index = getVisualizations().indexOf(v);
		if(index > -1) setCurrent(index);
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
	private  Set<VisualizationListener> listeners;

	/**
	 * Add a {@link VisualizationListener}, that will be notified if an
	 * event related to visualizations occurs
	 */
	public  void addListener(VisualizationListener l)
	{
		if(listeners == null)
			listeners = new HashSet<VisualizationListener>();
		listeners.add(l);
	}

	public  void removeListener (VisualizationListener l)
	{
		if (listeners == null)
			return;
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

	public  final String ROOT_XML_ELEMENT = "expression-data-visualizations";

	public  InputStream getXmlInput()
	{
		File xmlFile = new File(GexManager.getCurrentGex().getDbName() + ".xml");
		try {
			if(!xmlFile.exists()) xmlFile.createNewFile();
			InputStream in = new FileInputStream(xmlFile);
			return in;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public  OutputStream getXmlOutput() {
		try {
			File f = new File(GexManager.getCurrentGex().getDbName() + ".xml");
			OutputStream out = new FileOutputStream(f);
			return out;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public  void saveXML() {
		if(!GexManager.isConnected()) return;
		
		OutputStream out = getXmlOutput();
		
		Document xmlDoc = new Document();
		Element root = new Element(ROOT_XML_ELEMENT);
		xmlDoc.setRootElement(root);
		
		root.addContent(colorSetMgr.getXML());
		
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		try {
			xmlOut.output(xmlDoc, out);
			out.close();
		} catch(IOException e) {
			Logger.log.error("Unable to save visualization settings", e);
		}
	}
	

	/**
	   use a jdom Element to initialize the VisualizationManger
	 */
	public void loadXML(Element xml) {		
		if(xml == null) return;
		
		for(Object o : xml.getChildren(Visualization.XML_ELEMENT)) {
			Visualization vis = Visualization.fromXML((Element) o, methodRegistry);
			if(!visualizations.contains(vis)) addVisualization(vis);				
		}
	}
	
	public  void loadXML() {
		Document doc = getXML();
		Element root = doc.getRootElement();
		Element vis = root.getChild(VisualizationManager.XML_ELEMENT);
		loadXML(vis);
		Element cs = root.getChild(ColorSetManager.XML_ELEMENT);
		colorSetMgr.fromXML(cs);
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
	
}
