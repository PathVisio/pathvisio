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
import java.util.EventObject;
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
 * Manages visualizations
 * @author thomas
 *
 */
public class VisualizationManager implements ApplicationEventListener, ExpressionDataListener, ToolTipProvider {	
	static {
		VisualizationManager vm = new VisualizationManager();
		Engine.getCurrent().addApplicationEventListener(vm);
		Gex.addListener(vm);
	}
	
	public static final String XML_ELEMENT = "visualizations";
		
	static final String FILENAME_GENERIC = "visualizations.xml";
	
	static final int CURRENT_NONE = -1;
	
	static List<Visualization> visualizations = new ArrayList<Visualization>();
	static int current = -1;
		
	public static Visualization getCurrent() {
		if(current < 0 || current >= visualizations.size()) return null;
		return visualizations.get(current);
	}
	
	public static void setCurrent(int index) {
		current = index;
		fireVisualizationEvent(
				new VisualizationEvent(null, VisualizationEvent.VISUALIZATION_SELECTED));
	}
	
	public static void setCurrent(Visualization v) {
		int index = getVisualizations().indexOf(v);
		if(index > -1) setCurrent(index);
	}
	
	public static List<Visualization> getVisualizations() {
		return visualizations;
	}
	
	public static List<Visualization> getGeneric() {
		List<Visualization> generic = new ArrayList<Visualization>();
		for(Visualization v : visualizations) if(v.isGeneric()) generic.add(v);
		return generic;
	}
	
	public static List<Visualization> getNonGeneric() {
		List<Visualization> nongeneric = new ArrayList<Visualization>();
		for(Visualization v : visualizations) if(!v.isGeneric()) nongeneric.add(v);
		return nongeneric;
	}
	
	public static String[] getNames() {
		String[] names = new String[visualizations.size()];
		for(int i = 0; i < names.length; i++) 
			names[i] = visualizations.get(i).getName();
		return names;
	}
	
	public static void addVisualization(Visualization v) {
		visualizations.add(v);
		fireVisualizationEvent(
				new VisualizationEvent(null, VisualizationEvent.VISUALIZATION_ADDED));
	}
	
	public static void removeVisualization(int index) {
		if(index < 0 || index >= visualizations.size()) return; //Ignore wrong index
		visualizations.remove(index);
		fireVisualizationEvent(
				new VisualizationEvent(null, VisualizationEvent.VISUALIZATION_REMOVED));
	}
	
	public static void removeVisualization(Visualization v) {
		removeVisualization(visualizations.indexOf(v));
	}
	
	public static String getNewName() {
		String prefix = "visualization";
		int i = 1;
		String name = prefix;
		while(nameExists(name)) name = prefix + "-" + i++;
		return name;
	}
	
	public static boolean nameExists(String name) {
		for(Visualization v : visualizations) 
			if(v.getName().equalsIgnoreCase(name)) return true;
		return false;
	}
	
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
	
	public static Element getNonGenericXML() {
		Element xml = new Element(XML_ELEMENT);
		
		for(Visualization v : getNonGeneric()) xml.addContent(v.toXML());
		
		return xml;
	}
	
	public static void loadNonGenericXML(Element xml) {		
		if(xml == null) return;
		
		for(Object o : xml.getChildren(Visualization.XML_ELEMENT)) {
			Visualization vis = Visualization.fromXML((Element) o);
			if(!visualizations.contains(vis)) addVisualization(vis);				
		}
	}
	
	static void removeNonGeneric() {
		List<Visualization> toRemove = new ArrayList<Visualization>();
		for(Visualization v : getVisualizations()) {
			if(!v.isGeneric()) toRemove.add(v);
		}
		for(Visualization v : toRemove) removeVisualization(v);
	}
	
	static File getGenericFile() {
		return new File(SwtEngine.getCurrent().getApplicationDir(), FILENAME_GENERIC);
	}
	
	static VisComboItem visComboItem = new VisComboItem("VisualizationCombo");
	public static ContributionItem getComboItem() {
		return visComboItem;
	}
	
	static VisualizationPanel sidePanel;
		
	public static Composite getSidePanel() {
		return sidePanel;
	}
	public static Composite createSidePanel(Composite parent) {
		if(sidePanel != null && !sidePanel.isDisposed()) sidePanel.dispose();
		sidePanel = new VisualizationPanel(parent, SWT.NULL);
		return sidePanel;
	}
	
	static class VisComboItem extends ControlContribution implements VisualizationListener {
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
			switch(e.type) {
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
	
	static class VisualizationPanel extends ScrolledComposite implements SelectionListener, 
									VisualizationListener, 
									ApplicationEventListener {
		Visualization vis;
		Composite contents;
		Set<Graphics> input;
		
		public VisualizationPanel(Composite parent, int style) {
			super(parent, style);
			createContents();
			
			Engine.getCurrent().addApplicationEventListener(this);
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) {
				vp.addSelectionListener(this);
			}
			
			VisualizationManager.addListener(this);
			input = new LinkedHashSet<Graphics>();
		}
		
		void createContents() {
			contents = new Composite(this, SWT.NULL);
			contents.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			setContent(contents);
			setExpandHorizontal(true);
			setExpandVertical(true);
			contents.setLayout(new FillLayout());
			setMinSize(contents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		
		void fillContents() {
			if(vis != null) {
				vis.disposeSidePanel();
				vis.createSideSidePanel(contents);
			}
		}
		
		public void setVisualization(Visualization v) {
			if(vis != null) vis.disposeSidePanel();
			vis = v;
			fillContents();
		}
		
		void addInput(Graphics g) {
			input.add(g);
			refresh();
		}
		
		void removeInput(Graphics g) {
			input.remove(g);
			refresh();
		}
		
		void clearInput() {
			input.clear();
			refresh();
		}
		
		void refresh() {
			if(vis != null) vis.visualizeSidePanel(input);
			layout(true, true);
		}

		public void selectionEvent(SelectionBox.SelectionEvent e) {
			switch(e.type) {
			case SelectionBox.SelectionEvent.OBJECT_ADDED:
				if(e.affectedObject instanceof Graphics) 
					addInput((Graphics)e.affectedObject);
				break;
			case SelectionBox.SelectionEvent.OBJECT_REMOVED:
				if(e.affectedObject instanceof Graphics) 
					removeInput((Graphics)e.affectedObject);
				break;
			case SelectionBox.SelectionEvent.SELECTION_CLEARED:
				clearInput();
			}
		}

		public void visualizationEvent(VisualizationEvent e) {
			switch(e.type) {
			case VisualizationEvent.VISUALIZATION_SELECTED:
				setVisualization(getCurrent());
			case VisualizationEvent.PLUGIN_SIDEPANEL_ACTIVATED:
				fillContents();
			}
			
		}

		public void applicationEvent(ApplicationEvent e) {
			if(e.type == ApplicationEvent.VPATHWAY_CREATED) {
				VPathway vp = (VPathway)e.source;
				vp.addSelectionListener(this);
			}
		}		
	}
	
	public void applicationEvent(ApplicationEvent e) {
		if(e.type == ApplicationEvent.APPLICATION_CLOSE) {
			saveGeneric();
		} else if (e.type == ApplicationEvent.VPATHWAY_CREATED) {
			VPathway vp = (VPathway)e.source;
			if(vp.getWrapper() instanceof VPathwaySwing) {
				((VPathwaySwing)vp.getWrapper()).addToolTipProvider(this);
			}
		}
	}
	
	static List<VisualizationListener> listeners;

	/**
	 * Add a {@link ExpressionDataListener}, that will be notified if an
	 * event related to visualizations occurs
	 * @param l The {@link ExpressionDataListener} to add
	 */
	public static void addListener(VisualizationListener l) {
		if(listeners == null)
			listeners = new ArrayList<VisualizationListener>();
		listeners.add(l);
	}

	/**
	 * Fire a {@link VisualizationEvent} to notify all {@link VisualizationListener}s registered
	 * to this class
	 * @param e
	 */
	public static void fireVisualizationEvent(VisualizationEvent e) {
		for(VisualizationListener l : listeners) {
			l.visualizationEvent(e);
		}
	}

	public interface VisualizationListener {
		public void visualizationEvent(VisualizationEvent e);
	}

	public static class VisualizationEvent extends EventObject {
		private static final long serialVersionUID = 1L;
		public static final int COLORSET_ADDED = 0;
		public static final int COLORSET_REMOVED = 1;
		public static final int COLORSET_MODIFIED = 2;
		public static final int VISUALIZATION_ADDED = 3;
		public static final int VISUALIZATION_REMOVED = 4;
		public static final int VISUALIZATION_MODIFIED = 5;
		public static final int VISUALIZATION_SELECTED = 6;
		public static final int PLUGIN_MODIFIED = 7;
		public static final int PLUGIN_ADDED = 8;
		public static final int PLUGIN_SIDEPANEL_ACTIVATED = 9;

		public Object source;
		public int type;

		public VisualizationEvent(Object source, int type) {
			super(source == null ? VisualizationManager.class : source);
			this.source = source;
			this.type = type;
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
