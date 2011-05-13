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

import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.PathwayElementMenuListener.PathwayElementMenuHook;

/**
 * This plugin adds the ability to attach a phone number to each
 * element in a pathway. This is a completely artificial example of course,
 * but it demonstrates how dynamic properties (aka arbitrary attributes)
 * can be used within a plugin.
 * <p>
 * A right-click menu item is used to expose
 * the dynamic property to the user-interface. This is not the only way
 * to do it. You could also do that through a toolbar button or menu item,
 * or you could even use dynamic properties to create hidden values
 * that are not exposed to the UI at all.
 */
public class ExDynamic implements Plugin
{
	private PvDesktop desktop;

	@Override
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
				// only insert menu item if e is instanceof Graphics,
				// i.e. if it has an attached model object.
				if (e instanceof Graphics)
				{
					// We instantiate an Action
					EditAttributeAction editPhoneAction = new EditAttributeAction();

					// pass the clicked element to the action object
					editPhoneAction.setElement((Graphics)e);

					// Insert action into the menu.
					menu.add (editPhoneAction);
				}
			}
		}
		);

	}

	/**
	 * A right-click menu action
	 */
	public class EditAttributeAction extends AbstractAction
	{
		private Graphics elt;

		public EditAttributeAction()
		{
			putValue (NAME, "Add / Edit phone number");
		}

		/**
		 * This should be called before the action is triggered
		 */
		public void setElement (Graphics anElt)
		{
			elt = anElt;
		}

		// Here we define the key of our dynamic property.
		// it's a good idea to prefix the key with the
		// package name of your plugin,
		// to prevent name clashes between plugins.
		static final String KEY = "org.pathvisio.example.phone";

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			String originalVal = elt.getPathwayElement().getDynamicProperty(KEY);

			String inputVal = JOptionPane.showInputDialog(desktop.getFrame(),
					"Enter a phone number here", originalVal);

			// TODO: check that this is an integer value
			elt.getPathwayElement().setDynamicProperty(KEY, inputVal);
		}

	}

	@Override
	public void done() {}

}
