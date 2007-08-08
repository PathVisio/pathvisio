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

import java.util.Map;
import java.util.HashMap;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.AffineTransform;
import org.pathvisio.model.ShapeType;

class ShapeRegistry
{
	static Map <String, Shape> registry = null;
		
	static
	{
		registry = new HashMap <String, Shape>();
		registry.put ("Pentagon", getRegularPolygon (5, 10, 10) );
		registry.put ("Hexagon", getRegularPolygon (6, 10, 10) );
		registry.put ("Triangle", getRegularPolygon (3, 10, 10) );
		registry.put ("OrganA", getPluggableShape (ShapeType.ORGANA));
		registry.put ("OrganB", getPluggableShape (ShapeType.ORGANB));
		registry.put ("OrganC", getPluggableShape (ShapeType.ORGANC));
		registry.put ("CellA", getPluggableShape (ShapeType.CELLA));
		registry.put ("Ribosome", getPluggableShape (ShapeType.RIBOSOME));
		registry.put ("ProteinB", getPluggableShape (ShapeType.PROTEINB));
		registry.put ("Vesicle", getPluggableShape (ShapeType.VESICLE));
	}

	public static Shape getShape (String name, double x, double y, double w, double h)
	{
		Shape sh = registry.get (name);
		if (sh != null)
		{
			// now scale the path so it has proper w and h.
			Rectangle r = sh.getBounds();
			AffineTransform at = AffineTransform.getScaleInstance (w / r.width, h / r.height);
			sh = at.createTransformedShape (sh);
			at = AffineTransform.getTranslateInstance (x - r.x, y - r.y);
			return at.createTransformedShape (sh);
		}
		return null;
	}

	/**
	   Internal, 
	   Only for general shape types that can be described as a path.
	   The shapes are constructed as a general path with arbitrary size
	   and then resized to fit w and h parameters.
	 */
	static private Shape getPluggableShape (ShapeType st)
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
			path.moveTo (45, 23);
			path.curveTo (45, 34, 36, 43, 24, 43);
			path.curveTo (13, 43, 3, 34, 3, 23);
			path.curveTo (3, 12, 13, 3, 24, 3);
			path.curveTo (36, 3, 45, 12, 45, 23);
			path.closePath();
			path.moveTo (48, 23);
			path.curveTo (48, 36, 37, 46, 24, 46);
			path.curveTo (11, 46, 1, 36, 1, 23);
			path.curveTo (1, 11, 11, 0, 24, 0);
			path.curveTo (37, 0, 48, 11, 48, 23);
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
			path.moveTo (36, 0);
			path.curveTo (28, 1, 22, 5, 19, 11);
			path.curveTo (9, 13, 1, 22, 1, 33);
			path.curveTo (1, 43, 8, 52, 18, 55);
			path.curveTo (21, 61, 28, 66, 36, 66);
			path.curveTo (44, 66, 51, 61, 55, 55);
			path.curveTo (65, 53, 73, 44, 73, 33);
			path.curveTo (73, 22, 65, 13, 55, 11);
			path.curveTo (51, 5, 45, 0, 37, 0);
			path.curveTo (37, 0, 36, 0, 36, 0);
			path.closePath();
			path.moveTo (36, 4);
			path.curveTo (36, 4, 36, 4, 37, 4);
			path.curveTo (44, 4, 50, 8, 53, 14);
			path.curveTo (53, 15, 53, 16, 54, 17);
			path.curveTo (54, 17, 54, 18, 54, 18);
			path.curveTo (54, 19, 55, 21, 55, 22);
			path.curveTo (55, 24, 54, 26, 54, 28);
			path.curveTo (54, 29, 53, 29, 53, 30);
			path.curveTo (52, 29, 52, 29, 51, 28);
			path.curveTo (51, 28, 51, 28, 51, 28);
			path.curveTo (50, 27, 49, 26, 47, 25);
			path.curveTo (47, 25, 47, 24, 46, 24);
			path.curveTo (45, 24, 44, 23, 43, 23);
			path.curveTo (42, 23, 42, 22, 42, 22);
			path.curveTo (40, 22, 38, 21, 36, 21);
			path.curveTo (34, 21, 33, 22, 31, 22);
			path.curveTo (29, 23, 28, 23, 26, 24);
			path.curveTo (26, 24, 26, 24, 26, 24);
			path.curveTo (24, 26, 22, 27, 20, 29);
			path.curveTo (20, 28, 20, 28, 20, 27);
			path.curveTo (19, 26, 19, 24, 19, 22);
			path.curveTo (19, 22, 19, 22, 19, 22);
			path.curveTo (19, 22, 19, 22, 19, 22);
			path.curveTo (19, 21, 19, 19, 20, 18);
			path.curveTo (20, 18, 20, 17, 20, 17);
			path.curveTo (20, 16, 21, 15, 21, 14);
			path.curveTo (24, 8, 29, 4, 36, 4);
			path.closePath();
			path.moveTo (17, 15);
			path.curveTo (17, 15, 17, 15, 17, 15);
			path.curveTo (17, 15, 17, 16, 17, 16);
			path.curveTo (16, 16, 16, 17, 16, 17);
			path.curveTo (16, 19, 16, 21, 16, 22);
			path.curveTo (16, 22, 16, 22, 16, 22);
			path.curveTo (16, 22, 16, 22, 16, 22);
			path.curveTo (16, 24, 16, 26, 16, 28);
			path.curveTo (16, 28, 16, 28, 16, 28);
			path.curveTo (17, 30, 17, 31, 18, 32);
			path.curveTo (17, 34, 17, 35, 16, 37);
			path.curveTo (16, 37, 16, 37, 16, 38);
			path.curveTo (15, 40, 15, 42, 15, 44);
			path.curveTo (15, 46, 15, 47, 16, 49);
			path.curveTo (16, 49, 16, 50, 16, 50);
			path.curveTo (9, 48, 4, 41, 4, 33);
			path.curveTo (4, 24, 10, 17, 17, 15);
			path.closePath();
			path.moveTo (57, 15);
			path.curveTo (64, 17, 69, 24, 69, 33);
			path.curveTo (69, 41, 64, 49, 56, 51);
			path.curveTo (57, 50, 57, 49, 57, 48);
			path.curveTo (57, 48, 57, 48, 57, 48);
			path.curveTo (57, 48, 57, 48, 57, 48);
			path.curveTo (57, 46, 58, 45, 58, 44);
			path.curveTo (58, 43, 57, 42, 57, 41);
			path.curveTo (57, 41, 57, 40, 57, 39);
			path.curveTo (57, 38, 56, 37, 56, 37);
			path.curveTo (56, 36, 56, 35, 55, 34);
			path.curveTo (55, 34, 55, 34, 55, 34);
			path.curveTo (56, 32, 57, 31, 57, 29);
			path.curveTo (58, 27, 58, 25, 58, 22);
			path.curveTo (58, 21, 58, 19, 57, 17);
			path.curveTo (57, 17, 57, 16, 57, 16);
			path.curveTo (57, 15, 57, 15, 57, 15);
			path.closePath();
			path.moveTo (36, 25);
			path.curveTo (38, 25, 39, 25, 41, 26);
			path.curveTo (41, 26, 41, 26, 41, 26);
			path.curveTo (43, 26, 44, 27, 45, 27);
			path.curveTo (45, 28, 45, 28, 45, 28);
			path.curveTo (47, 29, 48, 29, 49, 31);
			path.curveTo (50, 32, 51, 33, 51, 34);
			path.curveTo (52, 34, 52, 35, 52, 35);
			path.curveTo (52, 36, 53, 37, 53, 38);
			path.curveTo (53, 38, 53, 39, 54, 40);
			path.curveTo (54, 40, 54, 41, 54, 41);
			path.curveTo (54, 42, 54, 43, 54, 44);
			path.curveTo (54, 45, 54, 46, 54, 48);
			path.curveTo (53, 49, 53, 50, 53, 50);
			path.curveTo (53, 51, 53, 51, 52, 51);
			path.curveTo (50, 58, 43, 63, 36, 63);
			path.curveTo (29, 63, 24, 58, 21, 53);
			path.curveTo (21, 52, 21, 52, 21, 52);
			path.curveTo (20, 51, 19, 50, 19, 48);
			path.curveTo (19, 47, 19, 45, 19, 44);
			path.curveTo (19, 42, 19, 40, 19, 38);
			path.curveTo (20, 37, 20, 35, 21, 34);
			path.curveTo (21, 34, 21, 34, 21, 34);
			path.curveTo (21, 34, 21, 34, 21, 34);
			path.curveTo (23, 31, 25, 29, 28, 27);
			path.curveTo (30, 26, 33, 25, 36, 25);
			path.closePath();
			path.moveTo (36, 0);
			path.curveTo (28, 1, 22, 5, 19, 11);
			path.curveTo (9, 13, 1, 22, 1, 33);
			path.curveTo (1, 43, 8, 52, 18, 55);
			path.curveTo (21, 61, 28, 66, 36, 66);
			path.curveTo (44, 66, 51, 61, 55, 55);
			path.curveTo (65, 53, 73, 44, 73, 33);
			path.curveTo (73, 22, 65, 13, 55, 11);
			path.curveTo (51, 5, 45, 0, 37, 0);
			path.curveTo (37, 0, 36, 0, 36, 0);
			path.closePath();
			path.moveTo (36, 4);
			path.curveTo (36, 4, 36, 4, 37, 4);
			path.curveTo (44, 4, 50, 8, 53, 14);
			path.curveTo (53, 15, 53, 16, 54, 17);
			path.curveTo (54, 17, 54, 18, 54, 18);
			path.curveTo (54, 19, 55, 21, 55, 22);
			path.curveTo (55, 24, 54, 26, 54, 28);
			path.curveTo (54, 29, 53, 29, 53, 30);
			path.curveTo (52, 29, 52, 29, 51, 28);
			path.curveTo (51, 28, 51, 28, 51, 28);
			path.curveTo (50, 27, 49, 26, 47, 25);
			path.curveTo (47, 25, 47, 24, 46, 24);
			path.curveTo (45, 24, 44, 23, 43, 23);
			path.curveTo (42, 23, 42, 22, 42, 22);
			path.curveTo (40, 22, 38, 21, 36, 21);
			path.curveTo (34, 21, 33, 22, 31, 22);
			path.curveTo (29, 23, 28, 23, 26, 24);
			path.curveTo (26, 24, 26, 24, 26, 24);
			path.curveTo (24, 26, 22, 27, 20, 29);
			path.curveTo (20, 28, 20, 28, 20, 27);
			path.curveTo (19, 26, 19, 24, 19, 22);
			path.curveTo (19, 22, 19, 22, 19, 22);
			path.curveTo (19, 22, 19, 22, 19, 22);
			path.curveTo (19, 21, 19, 19, 20, 18);
			path.curveTo (20, 18, 20, 17, 20, 17);
			path.curveTo (20, 16, 21, 15, 21, 14);
			path.curveTo (24, 8, 29, 4, 36, 4);
			path.closePath();
			path.moveTo (17, 15);
			path.curveTo (17, 15, 17, 15, 17, 15);
			path.curveTo (17, 15, 17, 16, 17, 16);
			path.curveTo (16, 16, 16, 17, 16, 17);
			path.curveTo (16, 19, 16, 21, 16, 22);
			path.curveTo (16, 22, 16, 22, 16, 22);
			path.curveTo (16, 22, 16, 22, 16, 22);
			path.curveTo (16, 24, 16, 26, 16, 28);
			path.curveTo (16, 28, 16, 28, 16, 28);
			path.curveTo (17, 30, 17, 31, 18, 32);
			path.curveTo (17, 34, 17, 35, 16, 37);
			path.curveTo (16, 37, 16, 37, 16, 38);
			path.curveTo (15, 40, 15, 42, 15, 44);
			path.curveTo (15, 46, 15, 47, 16, 49);
			path.curveTo (16, 49, 16, 50, 16, 50);
			path.curveTo (9, 48, 4, 41, 4, 33);
			path.curveTo (4, 24, 10, 17, 17, 15);
			path.closePath();
			path.moveTo (57, 15);
			path.curveTo (64, 17, 69, 24, 69, 33);
			path.curveTo (69, 41, 64, 49, 56, 51);
			path.curveTo (57, 50, 57, 49, 57, 48);
			path.curveTo (57, 48, 57, 48, 57, 48);
			path.curveTo (57, 48, 57, 48, 57, 48);
			path.curveTo (57, 46, 58, 45, 58, 44);
			path.curveTo (58, 43, 57, 42, 57, 41);
			path.curveTo (57, 41, 57, 40, 57, 39);
			path.curveTo (57, 38, 56, 37, 56, 37);
			path.curveTo (56, 36, 56, 35, 55, 34);
			path.curveTo (55, 34, 55, 34, 55, 34);
			path.curveTo (56, 32, 57, 31, 57, 29);
			path.curveTo (58, 27, 58, 25, 58, 22);
			path.curveTo (58, 21, 58, 19, 57, 17);
			path.curveTo (57, 17, 57, 16, 57, 16);
			path.curveTo (57, 15, 57, 15, 57, 15);
			path.closePath();
			path.moveTo (36, 25);
			path.curveTo (38, 25, 39, 25, 41, 26);
			path.curveTo (41, 26, 41, 26, 41, 26);
			path.curveTo (43, 26, 44, 27, 45, 27);
			path.curveTo (45, 28, 45, 28, 45, 28);
			path.curveTo (47, 29, 48, 29, 49, 31);
			path.curveTo (50, 32, 51, 33, 51, 34);
			path.curveTo (52, 34, 52, 35, 52, 35);
			path.curveTo (52, 36, 53, 37, 53, 38);
			path.curveTo (53, 38, 53, 39, 54, 40);
			path.curveTo (54, 40, 54, 41, 54, 41);
			path.curveTo (54, 42, 54, 43, 54, 44);
			path.curveTo (54, 45, 54, 46, 54, 48);
			path.curveTo (53, 49, 53, 50, 53, 50);
			path.curveTo (53, 51, 53, 51, 52, 51);
			path.curveTo (50, 58, 43, 63, 36, 63);
			path.curveTo (29, 63, 24, 58, 21, 53);
			path.curveTo (21, 52, 21, 52, 21, 52);
			path.curveTo (20, 51, 19, 50, 19, 48);
			path.curveTo (19, 47, 19, 45, 19, 44);
			path.curveTo (19, 42, 19, 40, 19, 38);
			path.curveTo (20, 37, 20, 35, 21, 34);
			path.curveTo (21, 34, 21, 34, 21, 34);
			path.curveTo (21, 34, 21, 34, 21, 34);
			path.curveTo (23, 31, 25, 29, 28, 27);
			path.curveTo (30, 26, 33, 25, 36, 25);
			path.closePath();			
			break;
		case VESICLE:
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
		}
		return path;
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