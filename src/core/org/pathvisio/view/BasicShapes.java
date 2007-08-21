// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.Shape;
import java.awt.Polygon;

class BasicShapes
{
	static void registerShapes()
	{
		ShapeRegistry.registerShape (
			"Arc", new Arc2D.Double (0, 0, 10, 10, 0, -180, Arc2D.OPEN));

		GeneralPath p = new GeneralPath();
		p.moveTo(0, 4);
		p.quadTo(0, 2, 3, 2);
		p.quadTo(6, 2, 6, 0);
		p.quadTo(6, 2, 9, 2);
		p.quadTo(12, 2, 12, 4);		
		ShapeRegistry.registerShape (
			"Brace", p);

		ShapeRegistry.registerShape (
			"Oval", new Ellipse2D.Double (0, 0, 10, 10));
		ShapeRegistry.registerShape (
			"Rectangle", new Rectangle (0, 0, 10, 10));

		ShapeRegistry.registerArrow ("Arrow", getArrowHead(), ArrowShape.CLOSED);
		ShapeRegistry.registerArrow ("TBar", getTBar(), ArrowShape.CLOSED);
		ShapeRegistry.registerArrow ("LigandRound", getLRound(), ArrowShape.CLOSED);
		ShapeRegistry.registerArrow ("ReceptorRound", getRRound(), ArrowShape.WIRE);
		ShapeRegistry.registerArrow ("Receptor", getReceptor(), ArrowShape.WIRE);
		ShapeRegistry.registerArrow ("ReceptorSquare", getReceptorSquare(), ArrowShape.WIRE);
		ShapeRegistry.registerArrow ("LigandSquare", getLigand(), ArrowShape.CLOSED);		
	}

	/**
	   These are all model coordinates:
	 */
	private static final int ARROWHEIGHT = 65;
	private static final int ARROWWIDTH = 140;
	private static final int TBARHEIGHT = 225;
	private static final int TBARWIDTH = 15;
	private static final int LRDIAM = 175;
	private static final int RRDIAM = LRDIAM + 50;
	private static final int LIGANDWIDTH = 125;
	private static final int LIGANDHEIGHT = 175;
	private static final int RECEPWIDTH = LIGANDWIDTH + 30;
	private static final int RECEPHEIGHT = LIGANDHEIGHT + 30;

	private static Shape getArrowHead()
	{
		int[] xpoints = new int[] { 0, -ARROWWIDTH, -ARROWWIDTH };
		int[] ypoints = new int[] { 0, -ARROWHEIGHT, ARROWHEIGHT };	   
		return new Polygon(xpoints, ypoints, 3);
	}
	
	private static Shape getTBar()
	{
		return new Rectangle2D.Double(
			-TBARWIDTH, -TBARHEIGHT / 2,
			TBARWIDTH, TBARHEIGHT
			);
	}
		
	private static Shape getLRound()
	{	
		return new Ellipse2D.Double(-LRDIAM / 2, -LRDIAM / 2, LRDIAM, LRDIAM);
	}
		
	private static Shape getRRound()
	{
		return new Arc2D.Double (0, - RRDIAM / 2, RRDIAM, RRDIAM, 90, 180, Arc2D.OPEN);
	}
		
	private static Shape getReceptorSquare()
	{					
		GeneralPath rec = new GeneralPath();
		rec.moveTo(RECEPWIDTH, RECEPHEIGHT / 2);
		rec.lineTo(0, RECEPHEIGHT / 2);
		rec.lineTo(0, -RECEPHEIGHT / 2);
		rec.lineTo(RECEPWIDTH, - RECEPHEIGHT / 2);
		return rec;
	}

	private static Shape getReceptor()
	{
		GeneralPath rec = new GeneralPath();
		rec.moveTo(RECEPWIDTH, RECEPHEIGHT / 2);
		rec.lineTo(0, 0);
		rec.lineTo(RECEPWIDTH, - RECEPHEIGHT / 2);
		return rec;		
	}
	
	private static Shape getLigand()
	{
		return new Rectangle2D.Double(
			-LIGANDWIDTH, -LIGANDHEIGHT / 2,
			LIGANDWIDTH, LIGANDHEIGHT
			);
	}

}

