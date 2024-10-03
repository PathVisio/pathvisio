/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.core.view;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.pathvisio.core.model.AnchorType;

/**
 * Defines and registers all basic shapes and arrowheads, such Oval,
 * Rectangle, Arrow, TBar.
 *
 * Note that this does not include all shapes used in GenMAPP, See GenMAPPShapes for more
 * Shapes defined by GenMAPP.
 *
 * Shapes are defined and registered in the static section of this class.
 */
class BasicShapes
{
	static void registerShapes()
	{
		ShapeRegistry.registerArrow ("Arrow", getArrowHead(), ArrowShape.FillType.CLOSED);
		ShapeRegistry.registerArrow ("TBar", getTBar(), ArrowShape.FillType.OPEN, TBARWIDTH + TBAR_GAP);
		ShapeRegistry.registerArrow ("LigandRound", getLRound(), ArrowShape.FillType.CLOSED);
		ShapeRegistry.registerArrow ("ReceptorRound", getRRound(), ArrowShape.FillType.WIRE);
		ShapeRegistry.registerArrow ("Receptor", getReceptor(), ArrowShape.FillType.WIRE);
		ShapeRegistry.registerArrow ("ReceptorSquare", getReceptorSquare(), ArrowShape.FillType.WIRE);
		ShapeRegistry.registerArrow ("LigandSquare", getLigand(), ArrowShape.FillType.CLOSED);

		ShapeRegistry.registerAnchor (AnchorType.NONE.getName(), getAnchorDefault());
		ShapeRegistry.registerAnchor (AnchorType.CIRCLE.getName(), getAnchorCircle());
	}

	/**
	   These are all model coordinates:
	 */
	private static final int ARROWHEIGHT = 4;
	private static final int ARROWWIDTH = 9;
	private static final int TBARHEIGHT = 15;
	private static final int TBARWIDTH = 1;
	private static final int TBAR_GAP = 6;
	private static final int LRDIAM = 11;
	private static final int RRDIAM = LRDIAM + 3;
	private static final int LIGANDWIDTH = 8;
	private static final int LIGANDHEIGHT = 11;
	private static final int RECEPWIDTH = LIGANDWIDTH + 2;
	private static final int RECEPHEIGHT = LIGANDHEIGHT + 2;

	private static final int ANCHOR_DEFAULT_SIZE = 3;
	private static final int ANCHOR_CIRCLE_SIZE = 8;


	private static Shape getArrowHead()
	{
		int[] xpoints = new int[] { 0, -ARROWWIDTH, -ARROWWIDTH };
		int[] ypoints = new int[] { 0, -ARROWHEIGHT, ARROWHEIGHT };
		return new Polygon(xpoints, ypoints, 3);
	}

	private static Shape getTBar()
	{
		return new Rectangle2D.Double(
			0, -TBARHEIGHT / 2,
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

	private static Shape getAnchorDefault() {
		return new Rectangle2D.Double(
				-ANCHOR_DEFAULT_SIZE / 2, -ANCHOR_DEFAULT_SIZE / 2,
				ANCHOR_DEFAULT_SIZE, ANCHOR_DEFAULT_SIZE
			);
	}

	private static Shape getAnchorCircle() {
		return new Ellipse2D.Double(
				-ANCHOR_CIRCLE_SIZE / 2, -ANCHOR_CIRCLE_SIZE / 2,
				ANCHOR_CIRCLE_SIZE, ANCHOR_CIRCLE_SIZE
			);
	}

}

