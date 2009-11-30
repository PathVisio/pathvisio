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

import cytoscape.util.CytoscapeAction;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import org.pathvisio.cytoscape.GpmlPlugin;

/**
 * Copy selected nodes in GPML format, so that it can be
 * pasted in PathVisio or Cytoscape.
 */
public class CopyAction extends CytoscapeAction {
	GpmlPlugin plugin;

	public CopyAction(GpmlPlugin plugin) {
		this.plugin = plugin;
	}

	protected void initialize() {
		super.initialize();
		putValue(NAME, "Copy GPML");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
	}

	public String getPreferredMenu() {
		return "Edit";
	}

	public boolean isInMenuBar() {
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		plugin.drag(Toolkit.getDefaultToolkit().getSystemClipboard());
	}
}
