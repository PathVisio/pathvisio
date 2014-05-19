// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2014 BiGCaT Bioinformatics
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
package org.pathvisio.inforegistry.impl;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.pathvisio.core.view.SelectionBox.SelectionEvent;
import org.pathvisio.core.view.SelectionBox.SelectionListener;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.inforegistry.InfoRegistry;

/**
 * 
 * currently implemented as a plugin (but not shown in plugin manager)
 * creates a side tab und implements the selection listener to update the
 * drop down box with information providers for the selected data node type.
 * 
 * @author mkutmon
 *
 */
public class InfoRegistryPlugin implements Plugin, SelectionListener {

	private InfoRegistry registry;
	private PvDesktop desktop;
	private JPanel sidePanel;
	
	@Override
	public void init(PvDesktop desktop) {
		registry = InfoRegistry.getInfoRegistry();
		this.desktop = desktop;
		
		addSidePanel();
	}
	
	private void addSidePanel() {
		sidePanel = new JPanel ();
		sidePanel.setLayout (new BorderLayout());
		sidePanel.add (new JLabel ("Under Development"), BorderLayout.NORTH);
        
        // get a reference to the sidebar
        JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
        
        // add or panel with a given Title
        sidebarTabbedPane.add("Info", sidePanel);
	}

	@Override
	public void done() {
		desktop.getSideBarTabbedPane().remove(sidePanel);
		// TODO Auto-generated method stub
	}

	@Override
	public void selectionEvent(SelectionEvent e) {
		// TODO Auto-generated method stub
	}
}
