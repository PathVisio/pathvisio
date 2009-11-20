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
package org.pathvisio.view;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.LineType;
import org.pathvisio.model.ShapeType;

/**
 * Collection of Shapes and ArrowHeads used in 
 * the Molecular Interaction Map - style of pathways
 */
public class MIMShapes
{
	public static void registerShapes()
	{
		ShapeRegistry.registerShape ("mim-phosphorylated", getPluggableShape (MIM_PHOSPHORYLATED));
		ShapeRegistry.registerShape ("mim-degradation", getPluggableShape (MIM_DEGRADATION));
		ShapeRegistry.registerShape ("mim-interaction", getPluggableShape (MIM_INTERACTION));
		ShapeRegistry.registerArrow ("mim-necessary-stimulation", getMIMNecessary(), ArrowShape.FillType.OPEN);
		ShapeRegistry.registerArrow ("mim-binding", getMIMBinding(), ArrowShape.FillType.CLOSED);
		ShapeRegistry.registerArrow ("mim-conversion", getMIMConversion(), ArrowShape.FillType.CLOSED);		
		ShapeRegistry.registerArrow ("mim-stimulation", getMIMStimulation(), ArrowShape.FillType.OPEN);
		ShapeRegistry.registerArrow ("mim-catalysis", getMIMCatalysis(), ArrowShape.FillType.OPEN);		
		ShapeRegistry.registerArrow ("mim-cleavage", getMIMCleavage(), ArrowShape.FillType.WIRE);
		ShapeRegistry.registerArrow ("mim-inhibition", getMIMInhibition(), ArrowShape.FillType.OPEN, TBARWIDTH + TBAR_GAP);
		
		ShapeType.create ("mim-phosphorylated", null);
		ShapeType.create ("mim-degradation", null);
		ShapeType.create ("mim-interaction", null);
		
		LineType.create ("mim-necessary-stimulation", "Arrow");
		LineType.create ("mim-binding", "Arrow");
		LineType.create ("mim-conversion", "Arrow");
		LineType.create ("mim-stimulation", "Arrow");
		LineType.create ("mim-catalysis", "Arrow");
		LineType.create ("mim-inhibition", "Arrow");
		LineType.create ("mim-cleavage", null);
	}

	private static final int MIM_PHOSPHORYLATED = 0;
	private static final int MIM_DEGRADATION = 1;
	private static final int MIM_INTERACTION = 2;

	static final int CLEAVAGE_FIRST = 100;
	static final int CLEAVAGE_SECOND = 500;

	static private java.awt.Shape getMIMCleavage ()
	{
		GeneralPath path = new GeneralPath();
		path.moveTo (0, 0);
		path.lineTo (-CLEAVAGE_FIRST, -CLEAVAGE_FIRST);
		path.lineTo (CLEAVAGE_SECOND, -CLEAVAGE_FIRST);
		return path;
	}

	static final int CATALISYS_DIAM = 175;
	
	static private java.awt.Shape getMIMCatalysis ()
	{
		return new Ellipse2D.Double	(
			-CATALISYS_DIAM / 2, -CATALISYS_DIAM / 2,
			CATALISYS_DIAM, CATALISYS_DIAM);
	}

	private static final int ARROWHEIGHT = 65;
	private static final int ARROWWIDTH = 140;
	private static final int ARROW_NECESSARY_CROSSBAR = 200;
	
	static private java.awt.Shape getMIMStimulation ()
	{
		GeneralPath path = new GeneralPath();
		path.moveTo (0, 0);
		path.lineTo (-ARROWWIDTH, -ARROWHEIGHT);
		path.lineTo (-ARROWWIDTH, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getMIMBinding ()
	{
		GeneralPath path = new GeneralPath();
		path.moveTo (0, 0);
		path.lineTo (-ARROWWIDTH, -ARROWHEIGHT);
		path.lineTo (-ARROWWIDTH / 2, 0);
		path.lineTo (-ARROWWIDTH, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getMIMConversion ()
	{
		GeneralPath path = new GeneralPath();
		path.moveTo (0, 0);
		path.lineTo (-ARROWWIDTH, -ARROWHEIGHT);
		path.lineTo (-ARROWWIDTH, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getMIMNecessary ()
	{
		GeneralPath path = new GeneralPath();
		path.moveTo (0, 0);
		path.lineTo (-ARROWWIDTH, -ARROWHEIGHT);
		path.lineTo (-ARROWWIDTH, ARROWHEIGHT);
		path.closePath();
		path.moveTo (-ARROW_NECESSARY_CROSSBAR, -ARROWHEIGHT);
		path.lineTo (-ARROW_NECESSARY_CROSSBAR, ARROWHEIGHT);
		return path;
	}

	/**
	   Internal, 
	   Only for general shape types that can be described as a path.
	   The shapes are constructed as a general path with arbitrary size
	   and then resized to fit w and h parameters.
	 */
	static private java.awt.Shape getPluggableShape (int st)
	{
		GeneralPath path = new GeneralPath();
		switch (st)
		{
		case MIM_DEGRADATION:
			path.moveTo (31.59f, 18.46f);
			path.curveTo (31.59f, 25.44f, 25.72f, 31.10f, 18.50f, 31.10f);
			path.curveTo (11.27f, 31.10f, 5.41f, 25.44f, 5.41f, 18.46f);
			path.curveTo (5.41f, 11.48f, 11.27f, 5.82f, 18.50f, 5.82f);
			path.curveTo (25.72f, 5.82f, 31.59f, 11.48f, 31.59f, 18.46f);
			path.closePath();
			path.moveTo (0.39f, 0.80f);
			path.curveTo (34.84f, 36.07f, 35.25f, 35.67f, 35.25f, 35.67f);
			break;
		case MIM_PHOSPHORYLATED:
			path.moveTo (5.79f, 4.72f);
			path.lineTo (5.79f, 18.18f);
			path.lineTo (13.05f, 18.18f);
			path.curveTo (15.74f, 18.18f, 17.81f, 17.60f, 19.28f, 16.43f);
			path.curveTo (20.75f, 15.26f, 21.48f, 13.60f, 21.48f, 11.44f);
			path.curveTo (21.48f, 9.29f, 20.75f, 7.64f, 19.28f, 6.47f);
			path.curveTo (17.81f, 5.30f, 15.74f, 4.72f, 13.05f, 4.72f);
			path.lineTo (5.79f, 4.72f);
			path.moveTo (0.02f, 0.73f);
			path.lineTo (13.05f, 0.73f);
			path.curveTo (17.83f, 0.73f, 21.44f, 1.65f, 23.88f, 3.47f);
			path.curveTo (26.34f, 5.28f, 27.57f, 7.93f, 27.57f, 11.44f);
			path.curveTo (27.57f, 14.98f, 26.34f, 17.65f, 23.88f, 19.46f);
			path.curveTo (21.44f, 21.26f, 17.83f, 22.17f, 13.05f, 22.17f);
			path.lineTo (5.79f, 22.17f);
			path.lineTo (5.79f, 36.57f);
			path.lineTo (0.02f, 36.57f);
			path.lineTo (0.02f, 0.73f);
			break;
		case MIM_INTERACTION:
			path.moveTo (30.90f, 15.20f);
			path.curveTo (30.90f, 23.18f, 24.02f, 29.65f, 15.55f, 29.65f);
			path.curveTo (7.08f, 29.65f, 0.20f, 23.18f, 0.20f, 15.20f);
			path.curveTo (0.20f, 7.23f, 7.08f, 0.76f, 15.55f, 0.76f);
			path.curveTo (24.02f, 0.76f, 30.90f, 7.23f, 30.90f, 15.20f);
			path.closePath();			
			break;
		default:
			assert (false);
		}
		return path;
	}

	// copied from BasicShapes
	private static final int TBARHEIGHT = 225;
	private static final int TBARMASKHEIGHT = 15 * 3;
	private static final int TBARWIDTH = 1;
	private static final int TBAROFFSET = 150;
	private static final int TBAR_GAP = 100;

	// copied from BasicShapes.getTBar()
	private static Shape getMIMInhibition()
	{
		return new Rectangle2D.Double(
			-TBAROFFSET, -TBARHEIGHT / 2,
			TBARWIDTH, TBARHEIGHT
			);
	}

	// copied from BasicShapes.getTBarFill
	private static Shape getMIMInhibitionFill() 
	{
		return new Rectangle2D.Double(
			-TBAROFFSET + TBARWIDTH, -TBARMASKHEIGHT / 2,
			TBAROFFSET * 2 - TBARWIDTH, TBARMASKHEIGHT
		);
	}	
}