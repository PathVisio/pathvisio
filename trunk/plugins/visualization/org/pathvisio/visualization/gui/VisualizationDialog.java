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
package org.pathvisio.visualization.gui;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JScrollPane;

import org.pathvisio.gui.swing.dialogs.OkCancelDialog;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationManager;

/**
 * Dialog for editing dataset visualization options.
 */
public class VisualizationDialog extends OkCancelDialog
{

	VisualizationPanel visPanel;
	VisualizationManager visMgr;

	public VisualizationDialog(VisualizationManager visMgr, Frame frame, Component locationComp) {
		super(frame, "Visualization options", locationComp, true, false);

		this.visMgr = visMgr;

		setDialogComponent(new JScrollPane (createDialogPane()));

		//If there is no visualization yet, create one
		if(visMgr.getVisualizations().size() == 0) {
			visMgr.addVisualization(new Visualization("untitled"));
		}
		if(visMgr.getActiveVisualization() == null) {
			visMgr.setActiveVisualization(0);
		}
		visPanel.setVisualizationManager(visMgr);

		pack();
		// we have to do this again after pack():
		setLocationRelativeTo(locationComp);
	}

	protected Component createDialogPane() {
		visPanel = new VisualizationPanel();
		return visPanel;
	}

	protected void okPressed() {
		visMgr.saveXML(); //Save the settings
		super.okPressed();
	}
}
