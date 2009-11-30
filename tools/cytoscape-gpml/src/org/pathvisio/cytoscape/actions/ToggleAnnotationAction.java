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
package org.pathvisio.cytoscape.actions;

import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;

import giny.view.GraphView;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;

import org.pathvisio.cytoscape.GpmlHandler;
import org.pathvisio.debug.Logger;

/**
 * Toggle visibility of graphical annotations (Shape, Label, unconnected Line).
 */
public class ToggleAnnotationAction extends CytoscapeAction {
	GpmlHandler gpmlHandler;

	boolean checked = true;

	public ToggleAnnotationAction(GpmlHandler gpmlHandler) {
		this.gpmlHandler = gpmlHandler;
	}

	protected void initialize() {
		putValue(NAME, "Toggle GPML annotations");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl H"));
		super.initialize();
	}

	public String getPreferredMenu() {
		return "View";
	}

	public boolean isInMenuBar() {
		return true;
	}

	public boolean isInToolBar() {
		return false;
	}

	public void menuSelected(MenuEvent e) {
		Logger.log.trace(getClass() + ": menuSelected()");
	}

	public void actionPerformed(ActionEvent e) {
		checked = !checked;
		GraphView view = Cytoscape.getCurrentNetworkView();
		if(view != null) {
			gpmlHandler.showAnnotations(view, checked);
		}
	}
}
