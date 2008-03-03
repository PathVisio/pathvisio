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

import java.awt.Component;
import java.awt.Frame;
import java.util.HashMap;

import javax.swing.JTabbedPane;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.panels.CommentPanel;
import org.pathvisio.gui.swing.panels.LitReferencePanel;
import org.pathvisio.gui.swing.panels.PathwayElementPanel;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyType;
import org.pathvisio.view.VPathway;

/**
 * Dialog that allows you to display and edit properties of a PathwayElement
 * @author thomas
 *
 */
public class PathwayElementDialog extends OkCancelDialog {
	private static final long serialVersionUID = 1L;

	public static final String TAB_COMMENTS = "Comments";
	public static final String TAB_LITERATURE = "Literature";
	
	/**
	 * Create a dialog for the given pathway element.
	 * @param e The pathway element
	 * @param readonly Whether the dialog should be read-only or not
	 * @return An instance of a subclass of PathwayElementDialog (depends on the
	 * type attribute of the given PathwayElement, e.g. type DATANODE returns a DataNodeDialog
	 */
	public static PathwayElementDialog getInstance(PathwayElement e, boolean readonly) {
		return getInstance(e, readonly, null, null);
	}
	
	public static PathwayElementDialog getInstance(PathwayElement e, boolean readonly, Frame frame, Component locationComp) {
		switch(e.getObjectType()) {
		case ObjectType.LABEL:
			return new LabelDialog(e, readonly, frame, locationComp);
		case ObjectType.DATANODE:
			return new DataNodeDialog(e, readonly, frame, locationComp);
		case ObjectType.INFOBOX:
			return new PathwayElementDialog(e.getParent().getMappInfo(), readonly, frame, "Pathway properties", locationComp);
		default:
			return new PathwayElementDialog(e, readonly, frame, "Element properties", locationComp);
		}
	}
	
	PathwayElement input;
	private JTabbedPane dialogPane;
	private HashMap<String, PathwayElementPanel> panels;
	private HashMap<PropertyType, Object> state = new HashMap<PropertyType, Object>();
		
	protected boolean readonly;
	
	protected PathwayElementDialog(PathwayElement e, boolean readonly, Frame frame, String title, Component locationComp) {
		super(frame, title, locationComp, true);
		this.readonly = readonly;
		panels = new HashMap<String, PathwayElementPanel>();
		createTabs();
		setInput(e);
		setSize(450, 300);
	}

	protected Component createDialogPane() {
		dialogPane = new JTabbedPane();
		return dialogPane;
	}
	
	/**
	 * Get the pathway element for this dialog
	 */
	protected PathwayElement getInput() {
		return input;
	}
	
	/**
	 * Set the pathway element for this dialog
	 */
	public void setInput(PathwayElement e) {
		input = e;
		storeState();
		refresh();
	}
	
	/**
	 * Refresh the GUI components to reflect the current pathway element's properties. This
	 * method automatically refreshes all registered PathwayElementPanels.
	 * Subclasses may override this to update their own GUI components that are not added 
	 * as PathwayElementPanel.
	 */
	protected void refresh() {
		for(PathwayElementPanel p : panels.values()) {
			p.setInput(input);
		}
	}
	
	/**
	 * Store the current state of the pathway element. This is used to cancel
	 * the modifications made in the dialog.
	 */
	protected void storeState() {
		PathwayElement e = getInput();
		for(PropertyType t : e.getAttributes()) {
			state.put(t, e.getProperty(t));
		}
	}
	
	/**
	 * Restore the original state of the pathway element. This is called when the
	 * cancel button is pressed.
	 */
	protected void restoreState() {
		PathwayElement e = getInput();
		for(PropertyType t : state.keySet()) {
			e.setProperty(t, state.get(t));
		}
	}
	
	private void createTabs() {
		addPathwayElementPanel(TAB_COMMENTS, new CommentPanel());
		addPathwayElementPanel(TAB_LITERATURE, new LitReferencePanel());
		addCustomTabs(dialogPane);
	}
	
	/**
	 * 
	 * @param tabLabel
	 * @param p
	 */
	protected void addPathwayElementPanel(String tabLabel, PathwayElementPanel p) {
		p.setReadOnly(readonly);
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
	 * {@link #addPathwayElementPanel(String, PathwayElementPanel)} to add a PathwayElementPanel, or
	 * use {@link JTabbedPane#add(Component)}.
	 * @param parent
	 */
	protected void addCustomTabs(JTabbedPane parent) {
		//To be implemented by subclasses
	}
	
	/**
	 * Called when the OK button is pressed. Will close the dialog amd register an undo event.
	 */
	protected void okPressed() {
		VPathway p = Engine.getCurrent().getActiveVPathway();
		if(p != null) p.redraw();
		setVisible(false);
	}
	
	/**
	 * Called when the Cancel button is pressed. Will close the dialog and revert the
	 * pathway element to it's original state.
	 */
	protected void cancelPressed() {
		restoreState();
		setVisible(false);
	}
}