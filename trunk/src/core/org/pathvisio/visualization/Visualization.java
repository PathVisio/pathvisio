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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Element;

import org.pathvisio.util.Utils;
import org.pathvisio.visualization.VisualizationManager.VisualizationEvent;
import org.pathvisio.visualization.VisualizationManager.VisualizationListener;
import org.pathvisio.visualization.plugins.PluginManager;
import org.pathvisio.visualization.plugins.VisualizationPlugin;
import org.pathvisio.data.Gex;
import org.pathvisio.data.Gex.ExpressionDataEvent;
import org.pathvisio.data.Gex.ExpressionDataListener;
import org.pathvisio.gui.swt.Engine;
import org.pathvisio.view.Graphics;

/**
 * Represents a set of configured visualization plugins
 * @author thomas
 */
public class Visualization implements ExpressionDataListener, VisualizationListener {
	public static final String XML_ELEMENT = "visualization";
	public static final String XML_ATTR_NAME = "name";
	
	String name;
	HashMap<Class, PluginSet> plugins;
	List<PluginSet> pluginPlacement;
	
	Composite sidePanel;
	
	/**
	 * Constructor for this class. Creates an instance of {@link Visualization} with the
	 * given name
	 * @param name The name of this {@link Visualization}
	 */
	public Visualization(String name) {
		initPlugins();
		this.name = name;
		Gex.addListener(this);
		VisualizationManager.addListener(this);
	}
	
	/**
	 * Create initial instances of {@link PluginSet} for each visualization
	 * plugin class
	 */
	void initPlugins() {
		plugins = new HashMap<Class, PluginSet>();
		pluginPlacement = new ArrayList<PluginSet>();
		for(Class c : PluginManager.getPlugins()) {
			addPluginClass(c);
		}
	}
	
	/**
	 * Refresh the available subclasses of {@link VisualizationPlugin}.
	 */
	void refreshPluginClasses() {
		for(Class c : PluginManager.getPlugins()) {
			if(!plugins.containsKey(c)) {
				addPluginClass(c);
			}
		}
	}
	
	/**
	 * Add a subclass of {@link VisualizationPlugin} to the available
	 * visualization plugin classes
	 * @param c	The class of the visualization plugin to add
	 */
	void addPluginClass(Class c) {
		try {
			PluginSet pr = new PluginSet(c, this);
			plugins.put(c, pr);
			pluginPlacement.add(pr);
		} catch(Throwable e) {
			Engine.log.error("Unable to create instance of plugin " + c, e);
		}
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
		fireVisualizationEvent(VisualizationEvent.VISUALIZATION_MODIFIED);
	}
	
	/**
	 * Fire an {@link VisualizationEvent} for this visualization and the given type
	 * @param type The type of the {@link VisualizationEvent} to fire
	 */
	private final void fireVisualizationEvent(int type) {
		VisualizationManager.fireVisualizationEvent(
				new VisualizationEvent(this, type));
	}
	
	/**
	 * Returnts whether this visualization is generic or not. A visualization is
	 * generic only if all of its active plugins are generic.
	 * @see VisualizationPlugin#isGeneric()
	 * @return true if this visualization is generic, false otherwise
	 */
	public boolean isGeneric() {
		for(PluginSet pr : getPluginSets())
			if(pr.isActive() && !pr.isGeneric()) return false; //One or more active non-generic plugins, so not generic
		return true;
	}
	
	/**
	 * Get the {@link PluginSet}s of this visualization
	 * @return An ordered list of {@link PluginSet}s
	 */
	public List<PluginSet> getPluginSets() {
		return pluginPlacement;
	}
	
	/**
	 * Get the {@link PluginSet} for the given plugin class
	 */
	public PluginSet getPluginSet(Class c) {
		return plugins.get(c);
	}
	
	/**
	 * Set the {@link PluginSet} for the given subclass of {@link VisualizationPlugin}
	 * @param pluginClass	The class to set the given {@link PluginSet} for
	 * @param ps			The {@link PluginSet} to set
	 */
	private void setPluginSet(Class pluginClass, PluginSet ps) {
		pluginPlacement.remove(ps);
		plugins.put(pluginClass, ps);
		pluginPlacement.add(ps);
		fireVisualizationEvent(VisualizationEvent.VISUALIZATION_MODIFIED);
	}
	
	/**
	 * Draw this visualization to the pathway drawing for the given {@link Graphics} object.
	 * @see VisualizationPlugin#visualizeOnDrawing(Graphics, PaintEvent, GC)
	 * @param g	The {@link Graphics} object the visualization applies to
	 * @param e	{@link PaintEvent} containing information about the paint
	 * @param gc Graphical context on which drawing operations can be performed
	 */
	public void visualizeDrawing(Graphics g, PaintEvent e, GC gc) {
		for(PluginSet pr : getPluginSetsDrawingOrder()) {
			if(pr.isDrawing()) pr.getDrawingPlugin().visualizeOnDrawing(g, e, gc);
		}
	}
	
	/**
	 * Provide an drawing area on the given Graphics for the given VisualizationPlugin (only
	 * when {@link VisualizationPlugin#isUseProvidedArea()})
	 * @param p the VisualizationPlugin to provide the area for
	 * @param g the Graphics on which the area is created
	 * @return An {@link Region} object that contains the area in which the
	 * VisualizationPlugin can draw its visualization
	 */
	public Region provideDrawArea(VisualizationPlugin p, Graphics g) {
		if(!p.isUseProvidedArea()) 
			throw new IllegalArgumentException("useProvidedArea set to false for this plug-in");
		
		//Determine number of active plugins that to reserve a region
		int nrRes = 0;
		int index = 0;
		for(PluginSet pr : getPluginSetsDrawingOrder()) {
			if(pr.getDrawingPlugin() == p) index = nrRes;
			nrRes += (pr.getDrawingPlugin().isActive() && pr.getDrawingPlugin().isUseProvidedArea()) ? 1 : 0;
		}
		
		Region region = g.createVisualizationRegion();
		//Distribute space over plugins
		Rectangle bounds = region.getBounds();
		
		//Adjust width so we can divide into equal rectangles
		bounds.width += bounds.width % nrRes;
		int w = bounds.width / nrRes;
		bounds.x += w * index;
		bounds.width = w;
		
		
		region.intersect(bounds);
		return region;
	}
	
	/**
	 * Set the display order of the given plugin-set. {@link Utils#ORDER_FIRST} makes the
	 * plug-in to be drawn last (so displayed on top of the visualization).
	 * @param pr The plugin-set to set the drawing order for
	 * @param order The order constant (as specified in {@link Utils#changeOrder(List, Object, int)}
	 * @see Utils#changeOrder(List, Object, int)
	 */
	public void setDisplayOrder(PluginSet pr, int order) {
		Utils.changeOrder(pluginPlacement, pr, order);
		fireVisualizationEvent(VisualizationEvent.VISUALIZATION_MODIFIED);
	}
	
	/**
	 * Get the plugin-sets in the order of which they are to be drawn
	 * @return a list of plugin-sets in the order of which they are to be drawn
	 */
	public List<PluginSet> getPluginSetsDrawingOrder() {
		List<PluginSet> sorted = new ArrayList<PluginSet>(pluginPlacement);
		Collections.reverse(sorted);
		return sorted;
	}
	
	/**
	 * Update the side-panel for all plug-ins that are activated
	 * in the side-panel to visualize the given pathway elements
	 * @param objects The pathway elements to visualize
	 */
	void visualizeSidePanel(Collection<Graphics> objects) {
		for(PluginSet pr : getPluginSetsDrawingOrder()) {
			if(pr.isSidePanel())
				pr.getSidePanelPlugin().visualizeOnSidePanel(objects);
		}
	}
	
	/**
	 * Create the side-panel composite for plug-ins that are activated
	 * in the side-panel.
	 * @param parent The parent Composite to create the new Composite on
	 * @return A Composite that displays the side-panel visualization for the plug-ins
	 * that are activated in the side-panel
	 */
	Composite createSideSidePanel(Composite parent) {
		sidePanel = new Composite(parent, SWT.NULL);
		sidePanel.setLayout(new FillLayout(SWT.VERTICAL));
		
		for(PluginSet pr : getPluginSetsDrawingOrder()) {
			if(pr.isSidePanel()) {
				Group group = new Group(sidePanel, SWT.NULL);
				group.setBackground(group.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				group.setLayout(new FillLayout());
				group.setText(pr.getSidePanelPlugin().getName());
				pr.getSidePanelPlugin().initSidePanel(group);
			}
		}
		return sidePanel;
	}
	
	/**
	 * Disposes the side-panel composite created with {@link #createSideSidePanel(Composite)}
	 */
	void disposeSidePanel() {
		if(sidePanel != null && !sidePanel.isDisposed())
			sidePanel.dispose();
	}
	
	/**
	 * Checks whether this visualization has one or more plug-ins activated on
	 * the tool-tip
	 * @return true if one or more plug-ins are activated on the tool-tip, false if not
	 */
	public boolean usesToolTip() {
		for(PluginSet pr : pluginPlacement) {
			if(pr.isToolTip()) return true;
		}
		return false;
	}
	
	/**
	 * Create a tool-tip for the given pathway element
	 * @param parent The parent shell
	 * @param control The control on which the tool-tip is created
	 * @param g The pathway element to create the tool-tip for
	 * @return A tool-tip that displays visualizations for the given gene-product 
	 * by the plug-ins activated on the tool-tip
	 */
	public Shell visualizeToolTip(Shell parent, Control control, Graphics g) {
		final Shell tip = new Shell(parent, SWT.ON_TOP | SWT.TOOL);  
		tip.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		tip.setLayout(new RowLayout(SWT.VERTICAL));
		
		Listener hideListener = new Listener() {
			public void handleEvent(Event event) {
	 			switch (event.type) {
	 			case SWT.MouseDown:
	 			case SWT.MouseExit:
	 			case SWT.MouseMove:
	 			case SWT.FocusOut:
	 				tip.dispose();
	 			}
			}
		};
		
		tip.addListener(SWT.MouseDown, hideListener);
		tip.addListener(SWT.MouseExit, hideListener);
		control.addListener(SWT.MouseMove, hideListener);
		parent.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				tip.dispose();	
			}
			public void shellDeactivated(ShellEvent e) {
				tip.dispose();
			}
		});
				
		boolean hasOne = false;
		for(PluginSet pr : getPluginSetsDrawingOrder()) {
			if(pr.isToolTip()) {
				Composite ttc = pr.getToolTipPlugin().visualizeOnToolTip(tip, g);
				if(ttc != null) hasOne = true;
			}
		}
		tip.pack();
		return hasOne ? tip : null;
	}
	
	/**
	 * Save the information to re-build this visualization to an
	 * XML element
	 * @return The XML element containing the information to re-build this visualization
	 */
	public Element toXML() {
		Element vis = new Element(XML_ELEMENT);
		vis.setAttribute(XML_ATTR_NAME, getName());
		for(PluginSet pr : pluginPlacement)
			if(pr.isActive()) 
				vis.addContent(pr.toXML());
		return vis;
	}
	
	/**
	 * Re-build a visualization based on the information in the given XML element
	 * @param xml The XML element that contains the information to re-build the visualization
	 * @return The visualization that is re-build based on the information in the XML element
	 */
	public static Visualization fromXML(Element xml) {
		String name = xml.getAttributeValue(XML_ATTR_NAME);
		if(name == null) name = VisualizationManager.getNewName();
		
		Visualization v = new Visualization(name);
		for(Object o : xml.getChildren(PluginSet.XML_ELEMENT)) {
			try {
				PluginSet pr = PluginSet.fromXML((Element)o, v);
				v.setPluginSet(pr.getClass(), pr);
			} catch(Throwable e) {
				Engine.log.error("Unable to load plugin", e);
			}
		}		
		return v;
	}
	
	public boolean equals(Object o) {
		if(o instanceof Visualization) return ((Visualization)o).getName().equals(name);
		return false;
	}

	public void expressionDataEvent(ExpressionDataEvent e) {
		switch(e.type) {
		case ExpressionDataEvent.CONNECTION_OPENED:
			refreshPluginClasses();
		}
		
	}

	public void visualizationEvent(VisualizationEvent e) {
		switch(e.type) {
		case VisualizationEvent.PLUGIN_ADDED: 
			refreshPluginClasses();
			break;
		}
	}
	
	/**
	 * Set of instances from subclass of {@link VisualizationPlugin} (all instances
	 * have the same class).
	 * The set contains one instance for each display option (drawing, side panel, tool tip)
	 * @author Thomas
	 */
	public static class PluginSet {
		static final int NR = 3; //Number of display options
		static final int TOOLTIP = 0;
		static final int DRAWING = 1;
		static final int SIDEPANEL = 2;
		
		Visualization vis;
		Class pluginClass;
		VisualizationPlugin[] reps;
		
		private PluginSet(Visualization v) {
			vis = v;
			reps = new VisualizationPlugin[NR];
		}
		
		/**
		 * Constructor for this class
		 * @param pluginClass The class of the plug-in this set will contain
		 * @param v The visualization this plugin-set belongs to
		 * @throws Throwable
		 */
		public PluginSet(Class pluginClass, Visualization v) throws Throwable {
			this(v);
			this.pluginClass = pluginClass;
			for(int i = 0; i < NR; i++) {
				reps[i] = PluginManager.getInstance(pluginClass, v);
			}
		}
		
		void setPluginClass(Class pluginClass) throws Throwable {
			this.pluginClass = pluginClass;
			for(int i = 0; i < reps.length; i++) {
				if(reps[i] == null || !pluginClass.isInstance(reps[i]))
					reps[i] = PluginManager.getInstance(pluginClass, vis);
			}
		}
		
		void setPlugin(VisualizationPlugin p, int representation) throws Throwable {
			p.setActive(true);
			reps[representation] = p;
			if(pluginClass == null || !pluginClass.equals(p.getClass())) 
				setPluginClass(p.getClass());
		}
		
		void checkIndex(int index) {
			if(index < 0 || index > reps.length) 
				throw new IllegalArgumentException("invalid representation index");
		}
		
		/**
		 * Get the plug-in instance that will be used for visualization on the pahtway drawing
		 * @return The plug-in instance for visualization on the drawing
		 */
		public VisualizationPlugin getDrawingPlugin() { return reps[DRAWING]; }
		/**
		 * Get the plug-in instance that will be used for visualization on the pahtway tool-tip
		 * @return The plug-in instance for visualization on the tool-tip
		 */
		public VisualizationPlugin getToolTipPlugin() { return reps[TOOLTIP]; }
		/**
		 * Get the plug-in instance that will be used for visualization on the pahtway side-panel
		 * @return The plug-in instance for visualization on the side-panel
		 */
		public VisualizationPlugin getSidePanelPlugin() { return reps[SIDEPANEL]; }
		
		/**
		 * Get the plug-in instance for the given representation
		 * @param representation one of {@link #TOOLTIP}, {@link #SIDEPANEL}, {@link #DRAWING})
		 * @return The instance of the plug-in for the given representation
		 */
		public VisualizationPlugin getPlugin(int representation) { 
			checkIndex(representation);
			return reps[representation]; 
		}
		
		/**
		 * Get an instance of the plugin-class this plugin-set contains.
		 * Convenience method to query information about the plug-in, don't use
		 * the instance for visualization!
		 * @return An instance of the plugin-class
		 */
		VisualizationPlugin getInstance() { return reps[0]; }
		
		/**
		 * Check whether one or more of the plug-ins in this set are activated
		 * @return true if one or more of the plug-ins are activated, false if not
		 * @see VisualizationPlugin#isActive()
		 */
		public boolean isActive() {
			for(VisualizationPlugin p : reps) 
				if(p.isActive()) return true;
			return false;
		}
		
		/**
		 * Check whether all plug-ins in this set are generic
		 * @return true if all plug-ins in this set are generic, false if not
		 * @see VisualizationPlugin#isGeneric()
		 */
		public boolean isGeneric() {
			for(VisualizationPlugin p : reps) 
				if(!p.isGeneric()) return false;
			return true;
		}
		
		/**
		 * Check whether the pathway drawing representation in this set is activated
		 * @return true if the drawing representation is active, false if not
		 */
		public boolean isDrawing() { return getDrawingPlugin().isActive(); }
		/**
		 * Check whether the side-panel representation in this set is activated
		 * @return true if the sice-panel representation is active, false if not
		 */
		public boolean isSidePanel() { return getSidePanelPlugin().isActive(); }
		/**
		 * Check whether the tool-tip representation in this set is activated
		 * @return true if the tool-tip representation is active, false if not
		 */
		public boolean isToolTip() { return getToolTipPlugin().isActive(); }

		/**
		 * Set the activation state of the instance of the plug-in for the given representaion
		 * @param representation The representation (one of {@link #DRAWING}, {@link #SIDEPANEL}, {@link #TOOLTIP})
		 * @param active true to activate, false to de-activate
		 */
		public void setActive(int representation, boolean active) {
			checkIndex(representation);
			reps[representation].setActive(active);
			if(representation == SIDEPANEL) VisualizationManager.fireVisualizationEvent(
					new VisualizationEvent(this, VisualizationEvent.PLUGIN_SIDEPANEL_ACTIVATED));
		}
		
		static final String XML_ELEMENT = "plugin-representations";
		static final String XML_ATTR_CLASS = "class";
		static final String XML_ELM_DRAWING = "drawing";
		static final String XML_ELM_TOOLTIP = "tooltip";
		static final String XML_ELM_SIDEPANEL = "sidepanel";
		
		/**
		 * Save the information to re-build this plugin-ste to an
		 * XML element
		 * @return The XML element containing the information to re-build this plugin-set
		 */
		public Element toXML() {
			Element e = new Element(XML_ELEMENT);
			e.setAttribute(XML_ATTR_CLASS, pluginClass.getCanonicalName());		 
			
			if(reps[DRAWING].isActive()) {
				Element dr = new Element(XML_ELM_DRAWING);
				dr.addContent(reps[DRAWING].toXML());
				e.addContent(dr);
			}
			if(reps[SIDEPANEL].isActive()) {
				Element sp = new Element(XML_ELM_SIDEPANEL);
				sp.addContent(reps[SIDEPANEL].toXML());
				e.addContent(sp);
			}
			if(reps[TOOLTIP].isActive()) { 
				Element tt = new Element(XML_ELM_TOOLTIP);
				tt.addContent(reps[TOOLTIP].toXML());
				e.addContent(tt);
			}
			return e;
		}
		
		/**
		 * Re-build a plugin-set based on the information in the given XML element
		 * @param xml The XML element that contains the information to re-build the plugin-set
		 * @return The plugin-set that is re-build based on the information in the XML element
		 */
		public static PluginSet fromXML(Element xml, Visualization v) throws Throwable {
			PluginSet pr = new PluginSet(v);
			
			Element drawing = xml.getChild(XML_ELM_DRAWING);
			Element tooltip = xml.getChild(XML_ELM_TOOLTIP);
			Element sidepanel = xml.getChild(XML_ELM_SIDEPANEL);
			if(drawing != null) 
				pr.setPlugin(PluginManager.instanceFromXML(
						drawing.getChild(VisualizationPlugin.XML_ELEMENT), v), DRAWING);
			if(tooltip != null)
				pr.setPlugin(PluginManager.instanceFromXML(
						tooltip.getChild(VisualizationPlugin.XML_ELEMENT), v), TOOLTIP);
			if(sidepanel != null)
				pr.setPlugin(PluginManager.instanceFromXML(
						sidepanel.getChild(VisualizationPlugin.XML_ELEMENT), v), SIDEPANEL);
			return pr;
		}
		
		public int hashCode() {
			return pluginClass.hashCode();
		}
		public boolean equals(Object obj) {
			if(obj instanceof PluginSet) 
				return pluginClass.equals(((PluginSet)obj).pluginClass);
			return false;
		}
	}
}
