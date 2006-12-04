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
package visualization;

import gmmlVision.GmmlVision;
import graphics.GmmlGraphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Element;

import util.Utils;
import visualization.VisualizationManager.VisualizationEvent;
import visualization.VisualizationManager.VisualizationListener;
import visualization.plugins.PluginManager;
import visualization.plugins.VisualizationPlugin;
import data.GmmlGex;
import data.GmmlGex.ExpressionDataEvent;
import data.GmmlGex.ExpressionDataListener;

/**
 * Represents a set of configured visualization plugins
 * @author thomas
 *
 */
public class Visualization implements ExpressionDataListener, VisualizationListener {
	public static final String XML_ELEMENT = "visualization";
	public static final String XML_ATTR_NAME = "name";
	
	String name;
	HashMap<Class, PluginSet> plugins;
	List<PluginSet> drawingOrder;
	
	Composite sidePanel;
	
	public Visualization(String name) {
		initPlugins();
		this.name = name;
		GmmlGex.addListener(this);
		VisualizationManager.addListener(this);
	}
	
	void initPlugins() {
		plugins = new HashMap<Class, PluginSet>();
		drawingOrder = new ArrayList<PluginSet>();
		for(Class c : PluginManager.getPlugins()) {
			addPlugin(c);
		}
	}
	
	void refreshPlugins() {
		for(Class c : PluginManager.getPlugins()) {
			if(!plugins.containsKey(c)) {
				addPlugin(c);
			}
		}
	}
	
	void addPlugin(Class c) {
		try {
			PluginSet pr = new PluginSet(c, this);
			plugins.put(c, pr);
			drawingOrder.add(pr);
		} catch(Throwable e) {
			GmmlVision.log.error("Unable to create instance of plugin " + c, e);
		}
	}

	void updateAvailablePlugins() {
		for(Class pc : PluginManager.getPlugins()) {
			if(!plugins.containsKey(pc)) {
					addPlugin(pc);
			}
		}
	}

	public String getName() { return name; }
	public void setName(String name) { 
		this.name = name;
		fireVisualizationEvent(VisualizationEvent.VISUALIZATION_MODIFIED);
	}
	
	public final void fireVisualizationEvent(int type) {
		VisualizationManager.fireVisualizationEvent(
				new VisualizationEvent(this, type));
	}
	
	public boolean isGeneric() {
		for(PluginSet pr : getPlugins())
			if(pr.isActive() && !pr.isGeneric()) return false; //One or more active non-generic plugins, so not generic
		return true;
	}
		
	public List<PluginSet> getPlugins() {
		return drawingOrder;
	}
		
	public void setRepresentation(Class pluginClass, PluginSet pr) {
		drawingOrder.remove(pr);
		plugins.put(pluginClass, pr);
		drawingOrder.add(pr);
		fireVisualizationEvent(VisualizationEvent.VISUALIZATION_MODIFIED);
	}
			
	public void drawToObject(GmmlGraphics g, PaintEvent e, GC buffer) {
		for(PluginSet pr : getPlugins()) {
			if(pr.isDrawing()) pr.getDrawingPlugin().draw(g, e, buffer);
		}
	}
	
	public Region getReservedRegion(VisualizationPlugin p, GmmlGraphics g) {
		if(!p.isUseReservedRegion()) 
			throw new IllegalArgumentException("useProvidedArea set to false for this plug-in");
		
		//Determine number of active plugins that to reserve a region
		int nrRes = 0;
		int index = 0;
		for(PluginSet pr : getPlugins()) {
			if(pr.getDrawingPlugin() == p) index = nrRes;
			nrRes += (pr.getDrawingPlugin().isActive() && pr.getDrawingPlugin().isUseReservedRegion()) ? 1 : 0;
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
		
	public void setDrawingOrder(PluginSet pr, int order) {
		Utils.setDrawingOrder(drawingOrder, pr, order);
		fireVisualizationEvent(VisualizationEvent.VISUALIZATION_MODIFIED);
	}
	
	public List<PluginSet> getPluginsSorted() {
		return drawingOrder;
	}
	
	public void updateSidePanel(Collection<GmmlGraphics> objects) {
		for(PluginSet pr : drawingOrder) {
			if(pr.isSidePanel())
				pr.getSidePanelPlugin().updateSidePanel(objects);
		}
	}
	
	public Composite createSideSidePanel(Composite parent) {
		sidePanel = new Composite(parent, SWT.NULL);
		sidePanel.setLayout(new FillLayout(SWT.VERTICAL));
		
		for(PluginSet pr : drawingOrder) {
			if(pr.isSidePanel()) {
				Group group = new Group(sidePanel, SWT.NULL);
				group.setBackground(group.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				group.setLayout(new FillLayout());
				group.setText(pr.getSidePanelPlugin().getName());
				pr.getSidePanelPlugin().createSidePanelComposite(group);
			}
		}
		return sidePanel;
	}
	
	public Composite getSidePanel() {
		return sidePanel;
	}
	
	public void disposeSidePanel() {
		if(sidePanel != null && !sidePanel.isDisposed())
			sidePanel.dispose();
	}
	
	public boolean usesToolTip() {
		for(PluginSet pr : drawingOrder) {
			if(pr.isToolTip()) return true;
		}
		return false;
	}
	
	public Shell getToolTip(Display display, GmmlGraphics g) {
		Shell tip = new Shell(display, SWT.ON_TOP | SWT.TOOL);  
		tip.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		tip.setLayout(new RowLayout(SWT.VERTICAL));
		
		boolean hasOne = false;
		for(PluginSet pr : drawingOrder) {
			if(pr.isToolTip()) {
				Composite ttc = pr.getToolTipPlugin().getToolTipComposite(tip, g);
				if(ttc != null) hasOne = true;
			}
		}
		tip.pack();
		return hasOne ? tip : null;
	}
	
	public Element toXML() {
		Element vis = new Element(XML_ELEMENT);
		vis.setAttribute(XML_ATTR_NAME, getName());
		for(PluginSet pr : drawingOrder)
			if(pr.isActive()) 
				vis.addContent(pr.toXML());
		return vis;
	}
	
	public static Visualization fromXML(Element xml) {
		String name = xml.getAttributeValue(XML_ATTR_NAME);
		if(name == null) name = VisualizationManager.getNewName();
		
		Visualization v = new Visualization(name);
		for(Object o : xml.getChildren(PluginSet.XML_ELEMENT)) {
			try {
				PluginSet pr = PluginSet.fromXML((Element)o, v);
				v.setRepresentation(pr.getClass(), pr);
			} catch(Throwable e) {
				GmmlVision.log.error("Unable to load plugin", e);
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
			updateAvailablePlugins();
		}
		
	}

	public void visualizationEvent(VisualizationEvent e) {
		switch(e.type) {
		case VisualizationEvent.PLUGIN_ADDED: 
			refreshPlugins();
			break;
		}
	}
	
	public static class PluginSet {
		static final int NR = 3;
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
			if(pluginClass == null || pluginClass.equals(p.getClass())) 
				setPluginClass(p.getClass());
		}
		
		void checkIndex(int index) {
			if(index < 0 || index > reps.length) 
				throw new IllegalArgumentException("invalid representation index");
		}
		
		public VisualizationPlugin getDrawingPlugin() { return reps[DRAWING]; }
		public VisualizationPlugin getToolTipPlugin() { return reps[TOOLTIP]; }
		public VisualizationPlugin getSidePanelPlugin() { return reps[SIDEPANEL]; }
		public VisualizationPlugin getPlugin(int representation) { 
			checkIndex(representation);
			return reps[representation]; 
		}
		
		public VisualizationPlugin getInstance() { return reps[0]; }
		
		public boolean isActive() {
			for(VisualizationPlugin p : reps) 
				if(p.isActive()) return true;
			return false;
		}
		
		public boolean isGeneric() {
			for(VisualizationPlugin p : reps) 
				if(!p.isGeneric()) return false;
			return true;
		}
		
		public boolean isDrawing() { return getDrawingPlugin().isActive(); }
		public boolean isSidePanel() { return getSidePanelPlugin().isActive(); }
		public boolean isToolTip() { return getToolTipPlugin().isActive(); }

		public void setActive(int representation, boolean active) {
			checkIndex(representation);
			reps[representation].setActive(active);
			VisualizationManager.fireVisualizationEvent(
					new VisualizationEvent(this, VisualizationEvent.PLUGIN_SIDEPANEL_ACTIVATED));
		}
		
		static final String XML_ELEMENT = "plugin-representations";
		static final String XML_ATTR_CLASS = "class";
		static final String XML_ELM_DRAWING = "drawing";
		static final String XML_ELM_TOOLTIP = "tooltip";
		static final String XML_ELM_SIDEPANEL = "sidepanel";
		
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

//		public String toString() {
//			return 	"PluginSet:  " + pluginClass + "\n" +
//					" \tToolTip:" + isToolTip() +
//					" \tDrawing:" + isDrawing() +
//					" \tSidepanel:" + isSidePanel();
//		}
		
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
