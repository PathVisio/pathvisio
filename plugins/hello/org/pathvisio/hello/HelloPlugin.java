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
package org.pathvisio.hello;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

/**
 * A tutorial implementation of a PathVisio plug-in
 */
public class HelloPlugin implements Plugin
{
	private PvDesktop desktop;

	public void init(PvDesktop desktop)
	{
		// save the desktop reference so we can use it later
		this.desktop = desktop;

		// register our action in the "Help" menu.
		desktop.registerMenuAction ("Help", helloAction);
	}

	public void done() {}

	private final HelloAction helloAction = new HelloAction();

	/**
	 * Display a welcome message when this action is triggered.
	 */
	private class HelloAction extends AbstractAction
	{
		HelloAction()
		{
			// The NAME property of an action is used as
			// the label of the menu item
			putValue (NAME, "Welcome message");
		}

		/**
		 *  called when the user selects the menu item
		 */
		public void actionPerformed(ActionEvent arg0)
		{
			JOptionPane.showMessageDialog(
					desktop.getFrame(),
					"Hello World");
		}
	}
}
