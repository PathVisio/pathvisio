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
package org.pathvisio.example;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.gui.swing.PathwayElementMenuListener.PathwayElementMenuHook;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.plugin.Plugin;

/**
 * Example on how you can modify the context menu of the Pathway view.
 * Activate the menu by right-clicking on a Pathway element
 */
public class ExContextMenu implements Plugin
{
	private PvDesktop desktop;

	public void init(PvDesktop desktop)
	{
		// save a reference to the application context
		this.desktop = desktop;

		// register a hook so we can modify the right-click menu
		desktop.addPathwayElementMenuHook(new PathwayElementMenuHook()
		{
			/**
			 * This method is called whenever the user right-clicks
			 * on an element in the Pathway.
			 * VPathwayElement contains the object that was clicked on.
			 */
			public void pathwayElementMenuHook(VPathwayElement e, JPopupMenu menu)
			{
				// We instantiate an Action
				ShowInfoAction showInfoAction = new ShowInfoAction();

				// pass the clicked element to the action object
				showInfoAction.setElement(e);

				// Insert action into the menu.
				// NB: this is optional, we can choose e.g.
				// to insert only when the clicked element is a certain type.
				menu.add (showInfoAction);
			}
		}
		);
	}

	public void done() {}

	private class ShowInfoAction extends AbstractAction
	{
		private VPathwayElement elt;

		public ShowInfoAction()
		{
			// This will be the label of the pop up menu item.
			putValue (NAME, "What class is this?");
		}

		/**
		 * This should be called before the action is triggered
		 */
		public void setElement (VPathwayElement anElt)
		{
			elt = anElt;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			// Display a message with the actual class
			JOptionPane.showMessageDialog(desktop.getFrame(),
					"You clicked a " + elt.getClass(), "Class Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
