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

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

/**
 * This defines and registers some
 * Special Shapes that are very specific to GenMAPP,
 * such as the GenMAPP ProteinComplex, Vesicle and Ribosome.
 *
 * Shapes are defined and registered in the static section of this class.
 */
class GenMAPPShapes
{
	static void registerShapes()
	{
		ShapeRegistry.registerShape ("Pentagon", getRegularPolygon (5, 10, 10) );
		ShapeRegistry.registerShape ("Hexagon", getRegularPolygon (6, 10, 10) );
		ShapeRegistry.registerShape ("Triangle", getRegularPolygon (3, 10, 10) );
	    ShapeRegistry.registerShape ("ComplexMembrane", getPluggableShape (Internal.COMPLEX_OVAL));

		ShapeRegistry.registerShape ("OrganA", getPluggableShape (Internal.ORGANA));
		ShapeRegistry.registerShape ("OrganB", getPluggableShape (Internal.ORGANB));
		ShapeRegistry.registerShape ("OrganC", getPluggableShape (Internal.ORGANC));
		ShapeRegistry.registerShape ("CellA", getPluggableShape (Internal.CELLA));
		ShapeRegistry.registerShape ("Ribosome", getPluggableShape (Internal.RIBOSOME));
		ShapeRegistry.registerShape ("ProteinComplex", getPluggableShape (Internal.PROTEINB));
	    ShapeRegistry.registerShape ("Cell", getCombinedShape (Internal.CELL));
	    ShapeRegistry.registerShape ("Nucleus", getCombinedShape (Internal.NUCLEUS));
	    ShapeRegistry.registerShape ("Mitochondria", getCombinedShape (Internal.MITOCHONDRIA));
	    ShapeRegistry.registerShape ("Organelle", getCombinedShape (Internal.ORGANELLE));
	    ShapeRegistry.registerShape ("Vesicle", getCombinedShape (Internal.VESICLE));
	}

	/**
	   these constants are internal, only for the switch statement below.
	   There is no relation with the constants defined in ShapeType.
	 */
	private enum Internal
	{
		COMPLEX_OVAL,
		
		@Deprecated ORGANA,
		@Deprecated ORGANB,
		@Deprecated ORGANC,
		@Deprecated CELLA,
		@Deprecated RIBOSOME,
		@Deprecated	PROTEINB,
		@Deprecated	CELL,
		@Deprecated	NUCLEUS,
		@Deprecated	MITOCHONDRIA,
		@Deprecated	ORGANELLE,
		@Deprecated	VESICLE;
	}
	/**
	   Internal,
	   Only for general shape types that can be described as a path.
	   The shapes are constructed as a general path with arbitrary size
	   and then resized to fit w and h parameters.
	 */
	static private java.awt.Shape getPluggableShape (Internal st)
	{
		GeneralPath path = new GeneralPath();
		switch (st)
		{
		case ORGANA:
			path.moveTo (33, 30);
			path.curveTo (33, 46, 26, 60, 17, 60);
			path.curveTo (8, 60, 0, 46, 0, 30);
			path.curveTo (0, 14, 8, 0, 17, 0);
			path.curveTo (26, 0, 33, 14, 33, 30);
			path.closePath();
			break;
		case CELLA:
			path.moveTo (44, 140);
			path.curveTo (38, 158, 28, 169, 21, 165);
			path.curveTo (15, 161, 14, 143, 20, 126);
			path.curveTo (26, 108, 36, 97, 43, 101);
			path.curveTo (49, 105, 50, 123, 44, 140);
			path.closePath();
			path.moveTo (64, 109);
			path.curveTo (49, 162, 27, 202, 13, 198);
			path.curveTo (0, 193, 1, 147, 16, 93);
			path.curveTo (31, 40, 54, 0, 67, 5);
			path.curveTo (80, 9, 79, 56, 64, 109);
			path.closePath();
			break;
		case ORGANC:
			path.moveTo (105.00f, 0.44f);
			path.curveTo (47.56f, 0.44f, 0.34f, 44.59f, 0.34f, 99.38f);
			path.curveTo (0.34f, 154.16f, 47.56f, 198.28f, 105.00f, 198.28f);
			path.curveTo (162.44f, 198.28f, 209.66f, 154.16f, 209.66f, 99.38f);
			path.curveTo (209.66f, 44.59f, 162.44f, 0.44f, 105.00f, 0.44f);
			path.closePath();
			path.moveTo (105.00f, 15.44f);
			path.curveTo (154.80f, 15.44f, 194.66f, 53.22f, 194.66f, 99.38f);
			path.curveTo (194.66f, 145.53f, 154.80f, 183.28f, 105.00f, 183.28f);
			path.curveTo (55.20f, 183.28f, 15.34f, 145.53f, 15.34f, 99.38f);
			path.curveTo (15.34f, 53.22f, 55.20f, 15.44f, 105.00f, 15.44f);
			path.closePath();
			break;
		case ORGANB:
			path.moveTo (15, 281);
			path.curveTo (6, 254, 0, 199, 0, 156);
			path.curveTo (0, 113, 6, 49, 15, 21);
			path.curveTo (15, 12, 26, 1, 38, 1);
			path.curveTo (49, 1, 60, 11, 60, 23);
			path.curveTo (59, 36, 50, 46, 32, 44);
			path.curveTo (23, 71, 23, 102, 23, 144);
			path.curveTo (23, 188, 23, 227, 32, 254);
			path.curveTo (50, 254, 60, 265, 60, 278);
			path.curveTo (60, 290, 46, 300, 36, 300);
			path.curveTo (27, 300, 15, 289, 15, 281);
			path.closePath();
			break;
		case RIBOSOME:
			path.moveTo (23.97f, 0.47f);
			path.curveTo (19.30f, 0.47f, 15.22f, 5.18f, 13.03f, 12.16f);
			path.curveTo (11.68f, 10.52f, 10.06f, 9.53f, 8.28f, 9.53f);
			path.curveTo (3.71f, 9.53f, -0.00f, 15.90f, 0.00f, 23.75f);
			path.curveTo (0.00f, 31.60f, 3.71f, 37.97f, 8.28f, 37.97f);
			path.curveTo (10.02f, 37.97f, 11.64f, 37.04f, 12.97f, 35.47f);
			path.curveTo (15.14f, 42.57f, 19.25f, 47.38f, 23.97f, 47.38f);
			path.curveTo (30.95f, 47.38f, 36.63f, 36.85f, 36.63f, 23.91f);
			path.curveTo (36.63f, 10.96f, 30.95f, 0.47f, 23.97f, 0.47f);
			path.closePath();
			break;
		case PROTEINB:
			path.moveTo (35.22f, 1.03f);
			path.curveTo (28.17f, 1.34f, 21.64f, 5.70f, 18.19f, 11.78f);
			path.curveTo (7.92f, 13.45f, 0.25f, 23.46f, 0.47f, 33.72f);
			path.curveTo (0.27f, 43.64f, 7.43f, 53.33f, 17.25f, 55.40f);
			path.curveTo (21.43f, 63.78f, 31.55f, 68.86f, 40.71f, 66.31f);
			path.curveTo (46.39f, 64.88f, 51.27f, 60.86f, 54.06f, 55.75f);
			path.curveTo (64.33f, 54.31f, 72.18f, 44.49f, 72.18f, 34.27f);
			path.curveTo (72.63f, 24.01f, 65.17f, 13.84f, 54.94f, 11.93f);
			path.curveTo (52.33f, 8.95f, 49.65f, 5.12f, 45.70f, 3.35f);
			path.curveTo (42.49f, 1.64f, 38.84f, 0.89f, 35.22f, 1.03f);
			path.closePath();
			path.moveTo (35.41f, 4.53f);
			path.curveTo (43.73f, 3.99f, 51.43f, 10.33f, 53.37f, 18.29f);
			path.curveTo (54.52f, 22.42f, 54.39f, 27.08f, 52.34f, 30.90f);
			path.curveTo (47.43f, 24.06f, 37.85f, 20.57f, 29.78f, 23.34f);
			path.curveTo (25.94f, 24.54f, 22.47f, 26.87f, 19.87f, 29.94f);
			path.curveTo (15.56f, 19.96f, 22.24f, 6.85f, 33.04f, 4.83f);
			path.curveTo (33.82f, 4.67f, 34.61f, 4.57f, 35.41f, 4.53f);
			path.closePath();
			path.moveTo (16.34f, 15.78f);
			path.curveTo (14.45f, 21.42f, 14.57f, 28.02f, 17.50f, 33.37f);
			path.curveTo (14.54f, 38.79f, 13.51f, 45.40f, 15.56f, 51.31f);
			path.curveTo (6.44f, 47.89f, 1.80f, 36.75f, 4.90f, 27.69f);
			path.curveTo (6.60f, 22.24f, 10.89f, 17.59f, 16.34f, 15.78f);
			path.closePath();
			path.moveTo (56.28f, 15.81f);
			path.curveTo (65.68f, 18.80f, 70.76f, 29.93f, 67.94f, 39.17f);
			path.curveTo (66.32f, 45.03f, 61.68f, 50.04f, 55.81f, 51.78f);
			path.curveTo (57.00f, 48.33f, 57.35f, 44.62f, 56.61f, 41.03f);
			path.curveTo (56.22f, 37.40f, 53.29f, 34.25f, 56.26f, 30.98f);
			path.curveTo (58.07f, 26.12f, 57.96f, 20.69f, 56.28f, 15.81f);
			path.closePath();
			path.moveTo (35.81f, 25.90f);
			path.curveTo (43.76f, 25.85f, 50.93f, 31.93f, 52.77f, 39.57f);
			path.curveTo (55.25f, 48.14f, 51.03f, 58.27f, 42.73f, 61.92f);
			path.curveTo (35.28f, 65.52f, 25.73f, 62.48f, 21.37f, 55.55f);
			path.curveTo (15.48f, 47.23f, 17.70f, 34.43f, 26.28f, 28.84f);
			path.curveTo (29.08f, 26.94f, 32.44f, 25.90f, 35.81f, 25.90f);
			path.closePath();
			break;
		case VESICLE:
			path.moveTo (33, 30);
			path.curveTo (33, 46, 26, 60, 17, 60);
			path.curveTo (8, 60, 0, 46, 0, 30);
			path.curveTo (0, 14, 8, 0, 17, 0);
			path.curveTo (26, 0, 33, 14, 33, 30);
			path.closePath();
			break;
		case COMPLEX_OVAL:
			path.moveTo (72.81f, 85.70f);
			path.curveTo (97.59f, 83.01f, 94.55f, 147.38f, 119.28f, 144.29f);
			path.curveTo (166.27f, 144.40f, 136.22f, 42.38f, 175.51f, 41.70f);
			path.curveTo (215.08f, 41.02f, 188.27f, 150.12f, 227.79f, 148.28f);
			path.curveTo (271.14f, 146.25f, 230.67f, 29.04f, 274.00f, 26.55f);
			path.curveTo (317.72f, 24.05f, 290.58f, 142.55f, 334.36f, 143.22f);
			path.curveTo (371.55f, 143.80f, 351.55f, 43.14f, 388.66f, 45.75f);
			path.curveTo (429.51f, 48.62f, 392.43f, 153.80f, 432.85f, 160.40f);
			path.curveTo (459.82f, 164.80f, 457.96f, 94.30f, 485.13f, 97.26f);
			path.curveTo (548.33f, 124.69f, 534.13f, 233.75f, 472.75f, 258.89f);
			path.curveTo (454.92f, 261.42f, 450.22f, 220.87f, 432.35f, 223.03f);
			path.curveTo (400.60f, 226.86f, 409.73f, 303.71f, 377.80f, 301.95f);
			path.curveTo (348.05f, 300.30f, 365.16f, 223.61f, 335.37f, 223.28f);
			path.curveTo (295.83f, 222.85f, 316.30f, 327.99f, 276.78f, 326.44f);
			path.curveTo (241.90f, 325.08f, 266.95f, 236.11f, 232.34f, 231.61f);
			path.curveTo (200.07f, 227.42f, 201.79f, 311.88f, 169.71f, 306.49f);
			path.curveTo (134.22f, 300.53f, 167.04f, 209.92f, 131.32f, 205.60f);
			path.curveTo (110.14f, 203.04f, 116.28f, 257.74f, 94.95f, 258.26f);
			path.curveTo (15.35f, 236.77f, 5.51f, 114.51f, 72.81f, 85.70f);
			path.closePath();
			path.moveTo (272.82f, 0.84f);
			path.curveTo (378.97f, 1.13f, 542.51f, 62.39f, 543.54f, 168.53f);
			path.curveTo (544.58f, 275.18f, 381.50f, 342.19f, 274.84f, 342.28f);
			path.curveTo (166.69f, 342.36f, 0.84f, 274.66f, 2.10f, 166.51f);
			path.curveTo (3.33f, 60.72f, 167.03f, 0.56f, 272.82f, 0.84f);
			path.closePath();
			break;
		}
		return path;
	}
	
	/**
	 * Internal,
	 * For shape types composed of multiple basic shapes.
	 * 
	 * NOTE: These are all being deprecated. They should be 
	 * automatically converted to semantic-free shapes.
	 */
	static private java.awt.Shape getCombinedShape (Internal st)
	{
		Area area = new Area();
		
		switch (st)
		{
		case CELL:
			RoundRectangle2D.Double c1 = new RoundRectangle2D.Double(0,0,600,600,100, 100);
			RoundRectangle2D.Double c2 = new RoundRectangle2D.Double(11,11,578,578,100, 100);
			area.add(new Area(c1));
			area.exclusiveOr(new Area(c2));
			break;
		case NUCLEUS:
			Ellipse2D.Double n1 = new Ellipse2D.Double (0, 0, 300, 200);
			Ellipse2D.Double n2 = new Ellipse2D.Double (8, 8, 284, 184);
			area.add(new Area(n1));
			area.exclusiveOr(new Area(n2));
			break;
		case MITOCHONDRIA:
			RoundRectangle2D.Double m1 = new RoundRectangle2D.Double (0, 0, 200, 100, 40, 60);
			Ellipse2D.Double m2 = new Ellipse2D.Double (4, 4, 192, 92);
			area.add(new Area(m1));
			area.exclusiveOr(new Area(m2));
			break;
		case ORGANELLE:
			RoundRectangle2D.Double g1 = new RoundRectangle2D.Double(0,0,200,100,40, 60);
			RoundRectangle2D.Double g2 = new RoundRectangle2D.Double(8,8,184,84,40, 60);
			area.add(new Area(g1));
			area.exclusiveOr(new Area(g2));
			break;
		case VESICLE:
			Ellipse2D.Double v1 = new Ellipse2D.Double (0, 0, 100, 100);
			area.add(new Area(v1));
			break;
			
		
		}
		return area;
	}
	
	static private java.awt.Shape getRegularPolygon (int sides, double w, double h)
	{
		GeneralPath path = new GeneralPath();
		for (int i = 0; i < sides; ++i)
		{
			double angle = Math.PI * 2 * i / sides;
			double x = (w/2) * (1 + Math.cos (angle));
			double y = (h/2) * (1 + Math.sin (angle));
			if (i == 0)
			{
				path.moveTo ((float)x, (float)y);
			}
			else
			{
				path.lineTo ((float)x, (float)y);
			}
		}
		path.closePath();
		return path;
	}

}