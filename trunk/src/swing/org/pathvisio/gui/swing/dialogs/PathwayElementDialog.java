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
package org.pathvisio.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.panels.CommentPanel;
import org.pathvisio.gui.swing.panels.PathwayElementPanel;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.view.VPathway;

public class PathwayElementDialog extends JDialog implements ActionListener {
	static final String OK = "Ok";
	static final String CANCEL = "Cancel";
	
	public static final String TAB_COMMENTS = "Comments";
	
	public static PathwayElementDialog getInstance(PathwayElement e) {
		return getInstance(e, null, null);
	}
	
	public static PathwayElementDialog getInstance(PathwayElement e, Frame frame, Component locationComp) {
		switch(e.getObjectType()) {
		case ObjectType.DATANODE:
			return new DataNodeDialog(e, frame, locationComp);
		default:
			return new PathwayElementDialog(e, frame, "Properties", locationComp);
		}
	}
	
	PathwayElement input;
	private JTabbedPane dialogPane;
	private HashMap<String, PathwayElementPanel> panels;
	private HashMap<PropertyType, Object> state = new HashMap<PropertyType, Object>();
		
	public PathwayElementDialog(PathwayElement e, Frame frame, String title, Component locationComp) {
		super(frame, "Element properties", true);
		
		panels = new HashMap<String, PathwayElementPanel>();
		
		JButton cancelButton = new JButton(CANCEL);
		cancelButton.addActionListener(this);

		final JButton setButton = new JButton(OK);
		setButton.setActionCommand(OK);
		setButton.addActionListener(this);
		getRootPane().setDefaultButton(setButton);
		
		dialogPane = new JTabbedPane();
		createTabs();
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(setButton);

		Container contentPane = getContentPane();
		contentPane.add(dialogPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		pack();
		setLocationRelativeTo(locationComp);
		
		setInput(e);
		}

	protected PathwayElement getInput() {
		return input;
	}
	
	public void setInput(PathwayElement e) {
		input = e;
		storeState();
		refresh();
	}
	
	protected void refresh() {
		for(PathwayElementPanel p : panels.values()) {
			p.setInput(input);
		}
	}
	
	protected void storeState() {
		PathwayElement e = getInput();
		for(PropertyType t : e.getAttributes()) {
			state.put(t, e.getProperty(t));
		}
	}
	
	protected void restoreState() {
		PathwayElement e = getInput();
		for(PropertyType t : state.keySet()) {
			e.setProperty(t, state.get(t));
		}
	}
	
	private void createTabs() {
		addPathwayElementPanel(TAB_COMMENTS, new CommentPanel());
		addCustomTabs(dialogPane);
	}
		
	protected void addPathwayElementPanel(String tabLabel, PathwayElementPanel p) {
		dialogPane.add(tabLabel, p);
		panels.put(tabLabel, p);
	}
	
	protected void removePathwayElementPanel(String tabLabel) {
		PathwayElementPanel panel = panels.get(tabLabel);
		if(panel != null) {
			dialogPane.remove(panel);
			panels.remove(panel);
		}
	}
	
	public void selectPathwayElementPanel(String tabLabel) {
		PathwayElementPanel panel = panels.get(tabLabel);
		if(panel != null) {
			dialogPane.setSelectedComponent(panel);
		}
	}
	
	/**
	 * Override in subclass and use 
	 * {@link #addPathwayElementPanel(String, PathwayElementPanel)} to add custom panels
	 * @param parent
	 */
	protected void addCustomTabs(JTabbedPane parent) {
		//To be implemented by subclasses
	}
	
	protected void okPressed() {
		VPathway p = Engine.getCurrent().getActiveVPathway();
		if(p != null) p.redraw();
		setVisible(false);
	}
	
	protected void cancelPressed() {
		restoreState();
		setVisible(false);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (OK.equals(e.getActionCommand())) {
			okPressed();
		}
		if(CANCEL.equals(e.getActionCommand())) {
			cancelPressed();
		}
	}
}