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
package org.pathvisio.example;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

/**
 * Demonstres how a Plugin can add a new tab to the Sidepanel.
 */
public class ExSidepanel implements Plugin
{
	private PvDesktop desktop;

	public void init(PvDesktop desktop)
	{
		this.desktop = desktop;

		// create a new panel to show in the side bar
		JPanel mySideBarPanel = new JPanel ();
		mySideBarPanel.setLayout (new BorderLayout());
		mySideBarPanel.add (new JLabel ("Hello SideBar"), BorderLayout.CENTER);

		// get a reference to the sidebar
		JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();

		// add or panel with a given Title
		sidebarTabbedPane.add("Title", mySideBarPanel);
	}

	public void done() {}
}
