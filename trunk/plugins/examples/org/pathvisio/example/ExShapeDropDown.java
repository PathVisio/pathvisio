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

import java.awt.geom.GeneralPath;

import javax.swing.Action;

import org.pathvisio.core.Engine;
import org.pathvisio.core.model.AbstractShape;
import org.pathvisio.core.model.ConnectorType;
import org.pathvisio.core.model.IShape;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.view.ArrowShape;
import org.pathvisio.core.view.DefaultTemplates;
import org.pathvisio.core.view.ShapeRegistry;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.CommonActions;
import org.pathvisio.gui.GraphicsChoiceButton;


/**
 * A example of adding user-defined shapes to the drop-down menu.
 */
public class ExShapeDropDown implements Plugin
{
	/**
	 * User-defined shapes example
	 */
	public static class MyShapes
	{
		public static final LineType MY_LINE = LineType.create ("my-line", "Arrow");
		public static final IShape MY_SHAPE = new AbstractShape (getMyShape(), "my-shape");
		
	    public static void registerShapes()
		{
			ShapeRegistry.registerArrow (MY_LINE.getName(), getMyLine(), ArrowShape.FillType.OPEN, 9);
		}
	    
	    static private java.awt.Shape getMyLine ()
	    {
	    	GeneralPath path = new GeneralPath();
			path.moveTo (0, 0);
			path.lineTo (15, -10);
			path.lineTo (30, 0);
			path.lineTo (15, 10);
			path.closePath();
	    	return path;
	    	
	    }

		static private java.awt.Shape getMyShape ()
		{
			GeneralPath path = new GeneralPath();
			path.moveTo(30, 0);
			path.lineTo(50, 60);
			path.lineTo(0, 20);
			path.lineTo(60, 20);
			path.lineTo(10, 60);
			path.closePath();
			return path;
		}
	}

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
