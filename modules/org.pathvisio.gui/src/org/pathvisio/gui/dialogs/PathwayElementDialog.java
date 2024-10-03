/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.gui.dialogs;

import java.awt.Component;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.StaticProperty;
import org.pathvisio.core.view.UndoAction;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.panels.CommentPanel;
import org.pathvisio.gui.panels.LitReferencePanel;
import org.pathvisio.gui.panels.PathwayElementPanel;

/**
 * Dialog that allows you to display and edit properties of a PathwayElement
 * @author thomas
 *
 */
public class PathwayElementDialog extends OkCancelDialog {

	public static final String TAB_COMMENTS = "Comments";
	public static final String TAB_LITERATURE = "Literature";

	PathwayElement input;
	private JTabbedPane dialogPane;
	private Map<String, PathwayElementPanel> panels;
	private Map<StaticProperty, Object> state = new HashMap<StaticProperty, Object>();
	private Pathway originalPathway; //Used for undo event

	protected boolean readonly;
	protected SwingEngine swingEngine;

	protected PathwayElementDialog(SwingEngine swingEngine, PathwayElement e, boolean readonly, Frame frame, String title, Component locationComp) {
		super(frame, title, locationComp, true);
		this.readonly = readonly;
		this.swingEngine = swingEngine;
		setDialogComponent(createDialogPane());
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
		originalPathway = (Pathway) e.getParent().clone();
		for(StaticProperty t : e.getStaticPropertyKeys()) {
			state.put(t, e.getStaticProperty(t));
		}
	}

	/**
	 * Restore the original state of the pathway element. This is called when the
	 * cancel button is pressed.
	 */
	protected void restoreState() {
		PathwayElement e = getInput();
		for(StaticProperty t : state.keySet()) {
			e.setStaticProperty(t, state.get(t));
		}
	}

	private void createTabs() {
		addPathwayElementPanel(TAB_COMMENTS, new CommentPanel());
		addPathwayElementPanel(TAB_LITERATURE, new LitReferencePanel(swingEngine));
		addCustomTabs(dialogPane);
	}

	/**
	 *
	 * @param tabLabel
	 * @param p
	 */
	public void addPathwayElementPanel(String tabLabel, PathwayElementPanel p) {
		p.setReadOnly(readonly);
		dialogPane.add(tabLabel, p);
		panels.put(tabLabel, p);
	}

	public void removePathwayElementPanel(String tabLabel) {
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
		boolean done = true;
		if(this instanceof DataNodeDialog || this instanceof LineDialog) {
			if(!input.getElementID().equals("") && input.getDataSource() == null) {
				done = false;
				JOptionPane.showMessageDialog(this,
						"You annotated this pathway element with an identifier but no database.\n Please specify a database system.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} else if (input.getElementID().equals("") && input.getDataSource() != null) {
				done = false;
				JOptionPane.showMessageDialog(this,
						"You annotated this pathway element with a database but no identifier.\n Please specify an identifier.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		if(done) {
			VPathway p = swingEngine.getEngine().getActiveVPathway();
			p.getUndoManager().newAction(
					new UndoAction("Modified element properties", originalPathway)
			);
			if(p != null) p.redraw();
			setVisible(false);
		}
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
