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

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JToolTip;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.Gex;
import org.pathvisio.data.Gex.ExpressionDataEvent;
import org.pathvisio.data.Gex.ExpressionDataListener;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swt.SwtEngine;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.SelectionBox;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.SelectionBox.SelectionListener;
import org.pathvisio.view.swing.ToolTipProvider;
import org.pathvisio.view.swing.VPathwaySwing;

/**
 * Manages visualizations.
 *
 * There is always one and exactly one global VisualizationManger present.
 * This can be obtained with getCurrent()
 *
 * The VisualizationManger maintains a list of Visualizations that can be
 * obtained with getVisualizations(). 
 *
 * The VisualizationManager is also responsible for saving / loading the configuration
 * for generic visualizations, owning the visualization sidePanel and
 * owning the Visualizations drop-down box.
 */
public class VisualizationManager implements ApplicationEventListener, ExpressionDataListener, ToolTipProvider {	
	static {
		VisualizationManager vm = new VisualizationManager();
		Engine.getCurrent().addApplicationEventListener(vm);
		Gex.addListener(vm);
	}
	
	/**
	   Interface for objects that want to listen to VisualizationEvents
	*/
	public interface VisualizationListener {
		public void visualizationEvent(VisualizationEvent e);
	}

	/**
	   name of the top-level xml element
	 */
	public static final String XML_ELEMENT = "visualizations";
		
	private static final String FILENAME_GENERIC = "visualizations.xml";
	
	private static final int CURRENT_NONE = -1;
	
	/**
	   List of all available Visualizations
	 */
	private static List<Visualization> visualizations = new ArrayList<Visualization>();
	private static int current = -1;
		
	/**
	   Obtain the currently active visualization. This is the visualization shown
	   in the open pathway.
	 */
	public static Visualization getCurrent() {
		if(current < 0 || current >= visualizations.size()) return null;
		return visualizations.get(current);
	}

	/**
	   Set which visualization will be active, by index
	 */
	public static void setCurrent(int index) {
		current = index;
		fireVisualizationEvent(
				new VisualizationEvent(
					VisualizationManager.class,
					VisualizationEvent.VISUALIZATION_SELECTED));
	}

	/**
	   Set which visualization will be active, by Object	   
	 */
	public static void setCurrent(Visualization v) {
		int index = getVisualizations().indexOf(v);
		if(index > -1) setCurrent(index);
	}

	/**
	   get a List of all visualizations
	 */
	public static List<Visualization> getVisualizations() {
		return visualizations;
	}

	/**
	   Get a List of all visualizations that are generic, i. e. do not
	   depend on data being loaded
	 */
	public static List<Visualization> getGeneric() {
		List<Visualization> generic = new ArrayList<Visualization>();
		for(Visualization v : visualizations) if(v.isGeneric()) generic.add(v);
		return generic;
	}

	/**
	   Get a list of visualizations that is not generic, i.e. depends
	   on data being loaded.
	*/
	public static List<Visualization> getNonGeneric() {
		List<Visualization> nongeneric = new ArrayList<Visualization>();
		for(Visualization v : visualizations) if(!v.isGeneric()) nongeneric.add(v);
		return nongeneric;
	}

	/**
	   get a list of names of all visualizations as an array.
	 */
	public static String[] getNames() {
		String[] names = new String[visualizations.size()];
		for(int i = 0; i < names.length; i++) 
			names[i] = visualizations.get(i).getName();
		return names;
	}

	/**
	   add a new visualization
	 */
	public static void addVisualization(Visualization v) {
		visualizations.add(v);
		fireVisualizationEvent(
				new VisualizationEvent(
					VisualizationManager.class,
					VisualizationEvent.VISUALIZATION_ADDED));
	}

	/**
	   remove a visualization (by index)
	 */
	public static void removeVisualization(int index) {
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
	public static void removeVisualization(Visualization v) {
		removeVisualization(visualizations.indexOf(v));
	}

	/**
	   get a new name for a visualization, that is guaranteed to be unique
	 */
	public static String getNewName() {
		String prefix = "visualization";
		int i = 1;
		String name = prefix;
		while(nameExists(name)) name = prefix + "-" + i++;
		return name;
	}

	/**
	   check if a name already exists.
	 */
	public static boolean nameExists(String name) {
		for(Visualization v : visualizations) 
			if(v.getName().equalsIgnoreCase(name)) return true;
		return false;
	}

	/**
	   save configuration for all generic visualizatons.
	 */
	public void saveGeneric() {
		Document xmlDoc = new Document();
		Element root = new Element(XML_ELEMENT);

		for(Visualization v : visualizations) {
			if(v.isGeneric()) root.addContent(v.toXML());
		}
		xmlDoc.addContent(root);
		
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		try {
			FileWriter fw = new FileWriter(getGenericFile());
			out.output(xmlDoc, fw);
			fw.close();
		} catch(IOException e) {
			Logger.log.error("Unable to save visualization settings", e);
		}
	}

	/**
	   load configuration for generic visualizations
	 */
	public static void loadGeneric() {
		if(!getGenericFile().exists()) return; //No generic visualizations saved yet
		SAXBuilder parser = new SAXBuilder();
		try {
			Document doc = parser.build(getGenericFile());
			Element root = doc.getRootElement();
			for(Object o : root.getChildren(Visualization.XML_ELEMENT)) {
				visualizations.add(Visualization.fromXML((Element) o));				
			}
		} catch(Exception e) {
			Logger.log.error("Unable to load visualization settinsg", e);
		}
	}

	/**
	   create a new jdom Element that represents the configuration for
	   all non-generic visualizations.
	 */
	public static Element getNonGenericXML() {
		Element xml = new Element(XML_ELEMENT);
		
		for(Visualization v : getNonGeneric()) xml.addContent(v.toXML());
		
		return xml;
	}

	/**
	   use a jdom Element to initialize the VisualizationManger
	 */
	public static void loadNonGenericXML(Element xml) {		
		if(xml == null) return;
		
		for(Object o : xml.getChildren(Visualization.XML_ELEMENT)) {
			Visualization vis = Visualization.fromXML((Element) o);
			if(!visualizations.contains(vis)) addVisualization(vis);				
		}
	}

	/**
	   Remove all non-generic visualizations, in response to unloading
	   expression data
	 */
	private static void removeNonGeneric() {
		List<Visualization> toRemove = new ArrayList<Visualization>();
		for(Visualization v : getVisualizations()) {
			if(!v.isGeneric()) toRemove.add(v);
		}
		for(Visualization v : toRemove) removeVisualization(v);
	}

	/**
	   obtain the File that stores the generic visualizations. 
	 */
	private static File getGenericFile() {
		return new File(SwtEngine.getCurrent().getApplicationDir(), FILENAME_GENERIC);
	}
	
	/**
	   The one and only visualization combobox.
	 */
	private static VisComboItem visComboItem = new VisComboItem("VisualizationCombo");

	/**
	   Obtain the one and only visualization combobox.
	 */
	public static ContributionItem getComboItem() {
		return visComboItem;
	}

	/**
	   The one and only visualization sidePanel
	 */
	private static VisualizationPanel sidePanel;

	/**
	   get the one and only visualization sidePanel.
	 */
	public static Composite getSidePanel() {
		return sidePanel;
	}

	/**
	   initialize the visualization sidePanel
	 */
	public static Composite createSidePanel(Composite parent) {
		if(sidePanel != null && !sidePanel.isDisposed()) sidePanel.dispose();
		sidePanel = new VisualizationPanel(parent, SWT.NULL);
		return sidePanel;
	}

	/**
	   represents a drop-down list with all visualizations.
	   It refreshes itself automatically in response to visualization events.
	 */
	private static class VisComboItem extends ControlContribution
		implements VisualizationListener
	{
		final String NONE = "no visualization";
		Combo visCombo;
		
		public VisComboItem(String id) {
			super(id);
			addListener(this);
		}

		protected Control createControl(Composite parent) {
			Composite control = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = layout.marginWidth = 1;
			control.setLayout(layout);
			
			Label label = new Label(control, SWT.CENTER);
			label.setText("Visualization: ");
			visCombo = new Combo(control, SWT.DROP_DOWN | SWT.READ_ONLY);
			visCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			visCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setCurrent(visCombo.getSelectionIndex() - 1);
				}
			});
			visCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setCurrent(visCombo.getSelectionIndex() - 1);
				}
			});
			update();
			return control;
		}
		
		public void update() {
			if(visCombo == null) return;
			
			String[] visnames = getNames();
			String[] items = new String[visnames.length + 1];
			items[0] = NONE;
			for(int i = 1; i < items.length; i++) items[i] = visnames[i-1];
			visCombo.setItems(items);
			visCombo.select(current + 1);
		}

		public void visualizationEvent(VisualizationEvent e) {
			switch(e.getType()) {
			case(VisualizationEvent.VISUALIZATION_ADDED):
			case(VisualizationEvent.VISUALIZATION_REMOVED):
			case(VisualizationEvent.VISUALIZATION_MODIFIED):
				visCombo.getDisplay().syncExec(new Runnable() {
					public void run() {
						update();
					}
				});
			}
			
		}
	}
	
	
	public void applicationEvent(ApplicationEvent e) {
		if(e.getType() == ApplicationEvent.APPLICATION_CLOSE) {
			saveGeneric();
		} else if (e.getType() == ApplicationEvent.VPATHWAY_CREATED) {
			VPathway vp = (VPathway)e.getSource();
			if(vp.getWrapper() instanceof VPathwaySwing) {
				((VPathwaySwing)vp.getWrapper()).addToolTipProvider(this);
			}
		}
	}

	/**
	   List of listeners
	 */
	private static List<VisualizationListener> listeners;

	/**
	 * Add a {@link VisualizationListener}, that will be notified if an
	 * event related to visualizations occurs
	 */
	public static void addListener(VisualizationListener l)
	{
		if(listeners == null)
			listeners = new ArrayList<VisualizationListener>();
		listeners.add(l);
	}

	public static void removeListener (VisualizationListener l)
	{
		if (listeners == null)
			return;
		listeners.remove (l);
	}

	/**
	 * Fire a {@link VisualizationEvent} to notify all {@link VisualizationListener}s registered
	 * to this class
	 */
	public static void fireVisualizationEvent(VisualizationEvent e) {
		for(VisualizationListener l : listeners) {
			l.visualizationEvent(e);
		}
	}

	public void expressionDataEvent(ExpressionDataEvent e) {
		if(e.type == ExpressionDataEvent.CONNECTION_CLOSED) {
			removeNonGeneric();
		}
	}

	public Component createToolTipComponent(JToolTip parent, Collection<VPathwayElement> elements) {
		if(getCurrent() == null) return null; //No tooltip if no visualization
		
		GeneProduct gp = null;
		for(VPathwayElement elm : elements) {
			if(elm instanceof GeneProduct) {
				gp = (GeneProduct)elm;
				break;
			}
		}
		
		if(gp == null) return null; //Only tooltip for gene product
		
		return getCurrent().createToolTipComponent(gp);
	}	

}
