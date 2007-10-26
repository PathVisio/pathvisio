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
import org.pathvisio.visualization.VisualizationManager.VisualizationListener;

/**
   Side Panel in the main window that can be used by Visualization Plugins to
   show more detailed information.
 */
class VisualizationPanel extends ScrolledComposite implements SelectionListener, 
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
		switch(e.getType())
		{
		case VisualizationEvent.VISUALIZATION_SELECTED:
			setVisualization(VisualizationManager.getCurrent());
		case VisualizationEvent.PLUGIN_SIDEPANEL_ACTIVATED:
			fillContents();
		}
			
	}

	public void applicationEvent(ApplicationEvent e) {
		if(e.getType() == ApplicationEvent.VPATHWAY_CREATED) {
			VPathway vp = (VPathway)e.getSource();
			vp.addSelectionListener(this);
		}
	}		
}
