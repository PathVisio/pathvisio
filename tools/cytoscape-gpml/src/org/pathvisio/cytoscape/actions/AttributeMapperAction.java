// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
import cytoscape.view.CytoscapeDesktop;

import java.awt.event.ActionEvent;

import javax.swing.JTabbedPane;

import org.pathvisio.cytoscape.AttributeDefaultsPanel;
import org.pathvisio.cytoscape.AttributeMapperPanel;
import org.pathvisio.cytoscape.GpmlPlugin;
import org.pathvisio.gui.dialogs.OkCancelDialog;

/**
 * Action to show AttributeMapperPanel, where
 * mappings between Cytoscape attributes and GPML Properties can be
 * configured.
 */
public class AttributeMapperAction extends CytoscapeAction {
	GpmlPlugin plugin;

	public AttributeMapperAction(GpmlPlugin plugin) {
		this.plugin = plugin;
	}

	protected void initialize() {
		super.initialize();
		putValue(NAME, "Configure attribute mappings");
	}

	public String getPreferredMenu() {
		return "Plugins";
	}

	public boolean isInMenuBar() {
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		OkCancelDialog dialog = new OkCancelDialog(
			CytoscapeDesktop.getFrames()[0], "GPML attribute mappings", null,
			true, false
		);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Node attributes", new AttributeMapperPanel(
				plugin.getGpmlHandler().getAttributeMapper(), Cytoscape.getNodeAttributes()
		));
		tabs.addTab("Edge attributes", new AttributeMapperPanel(
				plugin.getGpmlHandler().getAttributeMapper(), Cytoscape.getEdgeAttributes()
		));
		tabs.addTab("Default values", new AttributeDefaultsPanel(
				plugin.getGpmlHandler().getAttributeMapper()
		));
		dialog.setDialogComponent(tabs);
		dialog.pack();
		dialog.setVisible(true);
	}
}
