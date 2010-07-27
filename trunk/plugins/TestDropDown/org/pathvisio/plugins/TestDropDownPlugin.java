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
package org.pathvisio.plugins;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

//import org.pathvisio.gui.swing.PvDesktop;
//import org.pathvisio.plugin.Plugin;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;

import javax.swing.Action;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.CommonActions;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.gui.swing.CommonActions.NewElementAction;
import org.pathvisio.model.ConnectorType;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.ShapeType;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.util.Resources;
import org.pathvisio.view.DefaultTemplates;
import org.pathvisio.view.MIMShapes;
import org.pathvisio.view.Template;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;

import com.mammothsoftware.frwk.ddb.DropDownButton;

/**
 * A example of adding user-defined shapes (in MyShapes.java) to the drop-down menu.
 */
public class TestDropDownPlugin implements Plugin
{
	private PvDesktop desktop;

	public void init(PvDesktop desktop)
	{
		this.desktop = desktop;
		
		// register user defined shapes
		MyShapes.registerShapes();
		
		// define actions for the user-defined shapes
		Engine e = desktop.getSwingEngine().getEngine();
		Action[] aa = new Action[] {
				new CommonActions.NewElementAction(e, new DefaultTemplates.LineTemplate(
						"My Line", LineStyle.SOLID, LineType.LINE, MyShapes.MY_LINE, ConnectorType.STRAIGHT)
				),
				new CommonActions.NewElementAction(e, new DefaultTemplates.ShapeTemplate(MyShapes.MY_SHAPE)),
		};		
		
		// the drop-down memu
		DropDownButton lineButton = desktop.getSwingEngine().getApplicationPanel().getItemsDropDown();

		// add buttons to the drop-down menu
		desktop.getSwingEngine().getApplicationPanel().addButtons(aa, lineButton, 6, "Test");
	}

	public void done() {}	
}
