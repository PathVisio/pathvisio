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

import java.awt.Color;
import java.net.URL;

import javax.swing.Action;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.ShapeType;
import org.pathvisio.core.view.Template;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.CommonActions;

/**
 * Example plugin, adds action to toolbar
 * Toolbar action is diabled (greyed out) when no Pathway is opened
 */
public class ExTemplate implements Plugin, Engine.ApplicationEventListener
{
	private PvDesktop desktop;

	public void init(PvDesktop desktop) {
		this.desktop = desktop;

		toolbarAction = new CommonActions.NewElementAction(desktop.getSwingEngine().getEngine(), template);

		// add our action (defined below) to the toolbar
		desktop.getSwingEngine().getApplicationPanel().addToToolbar(toolbarAction);

		// register a listener so we get notified when a pathway is opened
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

	static Template template = new Template()
	{
		@Override
		public PathwayElement[] addElements(Pathway p, double mx, double my)
		{
			PathwayElement e = PathwayElement.createPathwayElement(ObjectType.SHAPE);
			e.setShapeType(ShapeType.OVAL);
			e.setMCenterX(mx);
			e.setMCenterY(my);
			e.setMWidth(300.0);
			e.setMHeight(300.0);
			e.setRotation(0);
			e.setFillColor(Color.BLACK);
			e.setTransparent(false);

			e.setGraphId(p.getUniqueGraphId());
			p.add(e);

			return new PathwayElement[] { e };
		}

		@Override
		public String getName()
		{
			return "Restriction";
		}

		@Override
		public String getDescription()
		{
			return "New Restriction";
		}

		@Override
		public VPathwayElement getDragElement(VPathway vp)
		{
			// normally we would return a Handle of the newly
			// created VPathwayElement here.
			//
			// by returning null, we prevent the user from
			// dragging the handle and customizing the
			// size of the new object
			return null;
		}

		private static final String ICON_PATH = "org/pathvisio/example/example-icon.gif";

		@Override
		public URL getIconLocation()
		{
			return ExTemplate.class.getClassLoader().getResource(ICON_PATH);
		}

		@Override
		public void postInsert(PathwayElement[] newElements)
		{
			// TODO Auto-generated method stub
			
		}
	};

	private Action toolbarAction;

	public void done() {}

	/**
	 * This is called when a Pathway is opened or closed.
	 */
	public void applicationEvent(ApplicationEvent e)
	{
		updateState();
	}
}
