// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.view.Graphics;

/**
 * Represents a set of configured visualization plugins
 * @author thomas
 */
public class Visualization
{
	public static final String XML_ELEMENT = "visualization";
	public static final String XML_ATTR_NAME = "name";

	String name;
	Map<String, VisualizationMethod> methods = new HashMap<String, VisualizationMethod>();

	/**
	 * The visualization manager that will be used to fire
	 * events.
	 * May be null!
	 */
	VisualizationManager visMgr;

	/**
	 * Constructor for this class. Creates an instance of {@link Visualization} with the
	 * given name
	 * @param name The name of this {@link Visualization}
	 */
	public Visualization(String name) {
		this.name = name;
	}

	/** Internal method. Should only be called by VisualizationManager.addVisualization() 
	 * and by fromXml() */
	void setVisualizationMgr(VisualizationManager visMgr) {
		this.visMgr = visMgr;
	}

	public void addMethod(VisualizationMethod m)
	{
		if (m.getVisualization() != null) { throw new IllegalArgumentException(
			"Trying to add Method that is already part of a Visualization"); }
		methods.put(m.getClass().toString(), m);
		m.setVisualization(this);
		modified();
	}
	
	public void removeMethod(VisualizationMethod m)
	{
		if (m.getVisualization() != this) { throw new IllegalArgumentException(
				"Trying to remove Method from Visualization that is not its parent"); }
		methods.remove(m.getClass().toString());
		m.setVisualization(null);
		modified();
	}

	/**
	 * Get the name of this {@link Visualization}
	 * @return the name
	 */
	public String getName() { return name; }

	/**
	 * Set the name of this {@link Visualization}
	 * @param name the name for this visualization
	 */
	public void setName(String name) {
		this.name = name;
		modified();
	}

	public VisualizationManager getManager() {
		return visMgr;
	}

	public Collection<VisualizationMethod> getMethods() {
		return methods.values();
	}
	
	public VisualizationMethod getMethod(String name)
	{
		return methods.get(name);
	}

	/**
	 * Call this method when the visualization or one of the visualization
	 * methods have been modified. It will notify the manager, which will
	 * refresh the vpathway and send out the necessary events.
	 */
	protected final void modified() {
		if(visMgr != null) {
			visMgr.visualizationModified(this);
		}
	}

	/**
	 * Draw this visualization to the pathway drawing for the given {@link Graphics} object.
	 * @see VisualizationMethod#visualizeOnDrawing(Graphics, Graphics2D)
	 * @param g	The {@link Graphics} object the visualization applies to
	 * @param g2d Graphical context on which drawing operations can be performed
	 */
	public void visualizeDrawing(Graphics g, Graphics2D g2d)
	{
		// get a list of visualization methods,
		// sort on default drawing order.
		List<VisualizationMethod> methods = new ArrayList<VisualizationMethod>();
		methods.addAll (getMethods());
		Collections.sort (methods, new Comparator<VisualizationMethod>()
		{

			public int compare(VisualizationMethod arg0,
					VisualizationMethod arg1) {
				return arg0.defaultDrawingOrder() - arg1.defaultDrawingOrder();
			}

		});
		for(VisualizationMethod m : methods)
		{
			m.visualizeOnDrawing(g, g2d);
		}
	}

	/**
	 * Provide an drawing area on the given Graphics for the given VisualizationMethod (only
	 * when {@link VisualizationMethod#isUseProvidedArea()})
	 * @param m the VisualizationMethod to provide the area for
	 * @param g the Graphics on which the area is created
	 * @return A {@link Shape} object that contains the area in which the
	 * VisualizationPlugin can draw its visualization
	 */
	public Area provideDrawArea(VisualizationMethod m, Graphics g) {
		if(!m.isUseProvidedArea())
			throw new IllegalArgumentException("useProvidedArea set to false for this plug-in");

		//Determine number of active plugins that to reserve a region
		int nrRes = 0;
		int index = 0;
		for(VisualizationMethod vm : getMethods()) {
			nrRes += (vm.isUseProvidedArea()) ? 1 : 0;
		}
		Area area = g.createVisualizationRegion();
		//Distribute space over plugins
		Rectangle bounds = area.getBounds();

		if(nrRes == 0) {
			return area;
		}

		//Adjust width so we can divide into equal rectangles
		bounds.width += bounds.width % nrRes;
		int w = bounds.width / nrRes;
		bounds.x += w * index;
		bounds.width = w;
		bounds.height -= 1;

		Area barea = new Area(bounds);
		area.intersect(barea);

		return area;
	}


	/**
	 * Save the information to re-build this visualization to an
	 * XML element
	 * @return The XML element containing the information to re-build this visualization
	 */
	public Element toXML() {
		Element vis = new Element(XML_ELEMENT);
		vis.setAttribute(XML_ATTR_NAME, getName());
		for(VisualizationMethod m : getMethods()) {
			vis.addContent(m.toXML());
		}
		return vis;
	}

	/**
	 * Re-build a visualization based on the information in the given XML element
	 * @param xml The XML element that contains the information to re-build the visualization
	 * @return The visualization that is re-build based on the information in the XML element
	 */
	public static Visualization fromXML(Element xml, VisualizationMethodRegistry methodFactory, VisualizationManager visMgr) {
		String name = xml.getAttributeValue(XML_ATTR_NAME);

		Visualization v = new Visualization(name);
		visMgr.addVisualization(v);
		for(Object o : xml.getChildren(VisualizationMethod.XML_ELEMENT)) {
			try {
				String methodName = ((Element)o).getAttributeValue(VisualizationMethod.XML_ATTR_NAME);
				VisualizationMethod m = methodFactory.createVisualizationMethod(methodName);
				v.addMethod (m);
				m.loadXML((Element)o);
			} catch(Throwable e) {
				Logger.log.error("Unable to load plugin", e);
			}
		}
		return v;
	}

	public boolean equals(Object o) {
		if(o instanceof Visualization) return ((Visualization)o).getName().equals(name);
		return false;
	}

	@Override public int hashCode()
	{
		// equals based on getName(), so hashCode as well.
		return name.hashCode();
	}

	public String toString() {
		return name;
	}

	private boolean showLegend;
	public boolean isShowLegend() { return showLegend; }
	public void setShowLegend (boolean value) 
	{ 
		showLegend = value;
		modified();
	}

}
