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
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

/**
 * Example plugin, adds action to toolbar
 * Toolbar action is diabled (greyed out) when no Pathway is opened
 */
public class ExToolbar implements Plugin, Engine.ApplicationEventListener
{
	private PvDesktop desktop;

	public void init(PvDesktop desktop) {
		this.desktop = desktop;

		// add our action (defined below) to the toolbar
		desktop.getSwingEngine().getApplicationPanel().addToToolbar(toolbarAction);

		// register a lister so we get notified when a pathway is opened
		desktop.getSwingEngine().getEngine().addApplicationEventListener(this);

		// set the initial enabled / disabled state of the action
		updateState();
	}

	/**
	 * Checks if a pathway is open or not. If there is no open pathway,
	 * the toolbar button is greyed out.
	 */
	public void updateState()
	{
		toolbarAction.setEnabled(desktop.getSwingEngine().getEngine().hasVPathway());
	}

	private final MyToolbarAction toolbarAction = new MyToolbarAction();

	private class MyToolbarAction extends AbstractAction
	{
		private static final String ICON_PATH = "org/pathvisio/example/example-icon.gif";

		MyToolbarAction()
		{
			// Short description will be the mouse tooltip label
			putValue(SHORT_DESCRIPTION, "My Toolbar Action");

			// icon in the toolbar. Use a 16x16 gif or png image.
			// The resource should be in the class path
			URL url = ExToolbar.class.getClassLoader().getResource(ICON_PATH);
			if (url == null) throw new IllegalStateException("Could not load resource " +
					ICON_PATH + ", please check that it is in the class-path");
			putValue(SMALL_ICON, new ImageIcon(url));
		}

		public void actionPerformed(ActionEvent arg0)
		{
			JOptionPane.showMessageDialog(desktop.getFrame(), "Hello World");
		}
	};

	public void done() {}

	/**
	 * This is called when a Pathway is opened or closed.
	 */
	public void applicationEvent(ApplicationEvent e)
	{
		updateState();
	}
}
