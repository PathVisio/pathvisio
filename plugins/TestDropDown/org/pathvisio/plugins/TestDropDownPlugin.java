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

import javax.swing.Action;

import org.pathvisio.core.Engine;
import org.pathvisio.core.model.ConnectorType;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.view.DefaultTemplates;
import org.pathvisio.gui.swing.CommonActions;
import org.pathvisio.gui.swing.GraphicsChoiceButton;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.plugin.Plugin;


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
		GraphicsChoiceButton lineButton = desktop.getSwingEngine().getApplicationPanel().getItemsDropDown();

		// add buttons to the drop-down menu
		lineButton.addButtons("User Definied", aa);
		
		// add buttons to the objects tab
		desktop.getSwingEngine().getApplicationPanel().getObjectsPane().addButtons(aa, "User Defined", 10);
	}

	public void done() {}	
}
