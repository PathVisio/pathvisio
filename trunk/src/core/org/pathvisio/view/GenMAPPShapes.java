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
	    ShapeRegistry.registerShape ("Mitochondria", getPluggableShape (Internal.MITOCHONDRIA));
	    ShapeRegistry.registerShape ("Sarcoplasmic Reticulum", getPluggableShape (Internal.SARCOPLASMICRETICULUM));
	    ShapeRegistry.registerShape ("Endoplasmic Reticulum", getPluggableShape (Internal.ENDOPLASMICRETICULUM));
	    ShapeRegistry.registerShape ("Golgi Apparatus", getPluggableShape (Internal.GOLGIAPPARATUS));

		ShapeRegistry.registerShape ("OrganA", getPluggableShape (Internal.ORGANA));
		ShapeRegistry.registerShape ("OrganB", getPluggableShape (Internal.ORGANB));
		ShapeRegistry.registerShape ("OrganC", getPluggableShape (Internal.ORGANC));
		ShapeRegistry.registerShape ("CellA", getPluggableShape (Internal.CELLA));
		ShapeRegistry.registerShape ("Ribosome", getPluggableShape (Internal.RIBOSOME));
		ShapeRegistry.registerShape ("ProteinComplex", getPluggableShape (Internal.PROTEINB));
	    ShapeRegistry.registerShape ("Cell", getCombinedShape (Internal.CELL));
	    ShapeRegistry.registerShape ("Nucleus", getCombinedShape (Internal.NUCLEUS));
//	    ShapeRegistry.registerShape ("Mitochondria", getCombinedShape (Internal.MITOCHONDRIA));
	    ShapeRegistry.registerShape ("Organelle", getCombinedShape (Internal.ORGANELLE));
	    ShapeRegistry.registerShape ("Vesicle", getCombinedShape (Internal.VESICLE));
	}

	/**
	   these constants are internal, only for the switch statement below.
	   There is no relation with the constants defined in ShapeType.
	 */
	private enum Internal
	{
		MITOCHONDRIA,
		SARCOPLASMICRETICULUM,
		ENDOPLASMICRETICULUM,
		GOLGIAPPARATUS,
		
		@Deprecated ORGANA,
		@Deprecated ORGANB,
		@Deprecated ORGANC,
		@Deprecated CELLA,
		@Deprecated RIBOSOME,
		@Deprecated	PROTEINB,
		@Deprecated	CELL,
		@Deprecated	NUCLEUS,
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
		case MITOCHONDRIA:
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
		case SARCOPLASMICRETICULUM:
			path.moveTo (83.84f, 11.06f);
			path.curveTo (55.38f, 11.53f, 34.05f , 35.28f, 27.55f , 
			59.42f);
			path.curveTo (21.34f, 79.07f, 24.23f , 100.01f, 31.18f , 
			119.12f);
			path.curveTo (36.92f, 139.13f, 39.24f , 152.44f, 29.02f , 
			171.33f);
			path.curveTo (17.17f, 196.63f, 16.73f , 237.34f, 36.28f , 
			259.06f);
			path.curveTo (51.15f, 276.87f, 78.30f , 283.82f, 101.03f , 
			276.18f);
			path.curveTo (121.32f, 270.59f, 135.08f , 253.36f, 139.86f 
			, 234.78f);
			path.curveTo (144.60f, 217.86f, 141.76f , 191.06f, 133.71f 
			, 174.98f);
			path.curveTo (125.19f, 156.63f, 126.34f , 137.76f, 133.32f 
			, 118.69f);
			path.curveTo (142.15f, 94.73f, 147.49f , 74.12f, 138.15f , 
			49.55f);
			path.curveTo (131.47f, 29.83f, 112.90f , 13.08f, 90.17f , 
			11.34f);
			path.curveTo (88.07f, 11.13f, 85.95f , 11.04f, 83.84f , 
			11.06f);
			path.lineTo (83.84f, 11.06f);
			path.closePath();
			break;
		case ENDOPLASMICRETICULUM:
			path.moveTo (117.65f, 173.00f);
			path.curveTo (111.77f, 135.92f, 125.48f , 98.47f, 146.23f 
			, 66.69f);
			path.curveTo (151.89f, 55.92f, 160.75f , 45.46f, 160.84f , 
			33.16f);
			path.curveTo (160.30f, 18.31f, 138.48f , 9.17f, 125.67f , 
			19.03f);
			path.curveTo (114.97f, 28.67f, 106.07f , 40.84f, 102.50f , 
			54.17f);
			path.curveTo (96.72f, 85.23f, 99.58f , 117.17f, 92.40f , 
			148.05f);
			path.curveTo (91.84f, 159.08f, 80.85f , 155.60f, 80.24f , 
			146.57f);
			path.curveTo (77.74f, 135.64f, 78.88f , 124.33f, 78.47f , 
			113.28f);
			path.curveTo (80.31f, 94.13f, 96.96f , 76.32f, 89.59f , 
			56.67f);
			path.curveTo (87.26f, 44.36f, 70.19f , 43.28f, 62.75f , 
			52.40f);
			path.curveTo (50.92f, 61.79f, 46.77f , 75.95f, 46.31f , 
			89.59f);
			path.curveTo (45.11f, 108.24f, 46.91f , 127.30f, 54.97f , 
			144.71f);
			path.curveTo (76.04f, 186.21f, 51.55f , 219.27f, 44.39f , 
			192.05f);
			path.curveTo (37.12f, 175.84f, 53.90f , 158.19f, 43.13f , 
			142.63f);
			path.curveTo (33.49f, 136.38f, 21.53f , 143.47f, 17.78f , 
			152.08f);
			path.curveTo (10.80f, 165.14f, 12.97f , 180.15f, 11.50f , 
			194.16f);
			path.curveTo (10.56f, 215.22f, 14.93f , 236.91f, 27.06f , 
			255.12f);
			path.curveTo (31.46f, 261.52f, 43.73f , 274.00f, 51.82f , 
			262.00f);
			path.curveTo (56.61f, 253.46f, 46.74f , 235.42f, 57.95f , 
			232.67f);
			path.curveTo (69.97f, 234.28f, 64.92f , 252.09f, 68.86f , 
			261.37f);
			path.curveTo (72.48f, 273.75f, 76.44f , 286.90f, 85.75f , 
			296.88f);
			path.curveTo (93.26f, 304.63f, 109.74f , 303.34f, 112.06f 
			, 290.90f);
			path.curveTo (113.01f, 281.64f, 108.20f , 273.22f, 102.95f 
			, 264.08f);
			path.curveTo (99.06f, 255.85f, 95.56f , 246.93f, 94.32f , 
			237.86f);
			path.curveTo (90.75f, 223.98f, 82.55f , 201.60f, 95.29f , 
			195.34f);
			path.curveTo (109.69f, 194.21f, 112.63f , 209.12f, 111.48f 
			, 218.85f);
			path.curveTo (111.42f, 229.31f, 112.01f , 239.46f, 114.31f 
			, 250.45f);
			path.curveTo (115.12f, 263.89f, 119.84f , 278.60f, 133.43f 
			, 286.05f);
			path.curveTo (148.33f, 292.25f, 158.77f , 284.87f, 156.66f 
			, 273.73f);
			path.curveTo (154.44f, 264.05f, 148.78f , 255.16f, 144.56f 
			, 245.97f);
			path.curveTo (137.70f, 233.33f, 129.80f , 220.91f, 126.99f 
			, 206.95f);
			path.curveTo (123.04f, 195.67f, 116.81f , 184.58f, 117.50f 
			, 172.46f);
			path.lineTo (117.65f, 173.00f);
			path.closePath();
			break;
		case GOLGIAPPARATUS:
			path.moveTo (127.96f, 49.53f);
			path.curveTo (121.66f, 31.27f, 138.80f , 7.66f, 156.31f , 
			9.53f);
			path.curveTo (173.25f, 7.39f, 191.42f , 27.16f, 188.92f , 
			46.11f);
			path.curveTo (186.48f, 61.46f, 179.96f , 70.14f, 178.51f , 
			85.78f);
			path.curveTo (177.21f, 106.34f, 175.10f , 125.37f, 174.43f 
			, 146.77f);
			path.curveTo (174.05f, 177.43f, 171.66f , 209.04f, 176.42f 
			, 239.53f);
			path.curveTo (180.30f, 258.62f, 191.10f , 275.62f, 192.45f 
			, 295.81f);
			path.curveTo (192.54f, 311.66f, 182.11f , 325.50f, 166.59f 
			, 324.58f);
			path.curveTo (152.14f, 325.43f, 133.44f , 325.03f, 126.49f 
			, 309.03f);
			path.curveTo (121.02f, 295.74f, 125.62f , 281.07f, 131.05f 
			, 267.86f);
			path.curveTo (136.98f, 249.17f, 140.34f , 231.09f, 143.04f 
			, 212.10f);
			path.curveTo (146.20f, 178.60f, 144.97f , 149.47f, 144.04f 
			, 115.92f);
			path.curveTo (142.28f, 93.04f, 135.66f , 70.93f, 127.96f , 
			49.53f);
			path.lineTo (127.96f, 49.53f);
			path.closePath();
			path.moveTo (77.73f, 59.23f);
			path.curveTo (82.35f, 48.65f, 94.32f , 42.79f, 105.18f , 
			45.62f);
			path.curveTo (113.70f, 47.00f, 119.21f , 52.21f, 122.69f , 
			61.49f);
			path.curveTo (124.37f, 78.66f, 115.43f , 93.85f, 114.27f , 
			110.08f);
			path.curveTo (110.14f, 136.19f, 112.49f , 161.25f, 113.20f 
			, 185.45f);
			path.curveTo (115.01f, 204.12f, 116.77f , 217.45f, 122.31f 
			, 232.89f);
			path.curveTo (125.70f, 241.82f, 126.02f , 246.57f, 127.21f 
			, 254.37f);
			path.curveTo (127.05f, 264.38f, 124.03f , 270.98f, 115.53f 
			, 276.21f);
			path.curveTo (103.11f, 280.30f, 93.64f , 277.95f, 83.72f , 
			270.17f);
			path.curveTo (77.55f, 261.61f, 77.64f , 258.92f, 78.22f , 
			247.63f);
			path.curveTo (78.86f, 241.37f, 80.77f , 237.00f, 83.86f , 
			227.01f);
			path.curveTo (89.30f, 212.69f, 87.64f , 201.97f, 88.23f , 
			186.83f);
			path.curveTo (88.38f, 166.69f, 87.57f , 132.05f, 86.93f , 
			111.58f);
			path.curveTo (83.54f, 83.34f, 74.05f , 78.83f, 77.73f , 
			59.23f);
			path.lineTo (77.73f, 59.23f);
			path.closePath();
			path.moveTo (48.72f, 76.28f);
			path.curveTo (56.17f, 78.12f, 61.95f , 84.59f, 66.18f , 
			90.83f);
			path.curveTo (70.48f, 98.11f, 73.15f , 105.72f, 75.01f , 
			113.81f);
			path.curveTo (76.93f, 121.92f, 76.68f , 130.38f, 77.54f , 
			138.66f);
			path.curveTo (78.48f, 156.39f, 78.11f , 174.59f, 72.59f , 
			191.57f);
			path.curveTo (70.55f, 198.47f, 66.87f , 205.48f, 64.48f , 
			212.26f);
			path.curveTo (60.37f, 220.77f, 55.64f , 228.48f, 49.21f , 
			235.35f);
			path.curveTo (42.93f, 240.92f, 33.51f , 241.52f, 25.97f , 
			238.69f);
			path.curveTo (16.43f, 235.33f, 12.78f , 222.68f, 16.90f , 
			213.66f);
			path.curveTo (19.81f, 205.01f, 23.19f , 198.84f, 33.22f , 
			191.68f);
			path.curveTo (41.60f, 182.17f, 42.95f , 177.91f, 44.24f , 
			169.15f);
			path.curveTo (45.39f, 158.97f, 44.29f , 149.60f, 43.45f , 
			137.43f);
			path.curveTo (42.26f, 127.62f, 40.71f , 118.07f, 30.98f , 
			110.34f);
			path.curveTo (24.67f, 103.68f, 20.99f , 97.09f, 22.55f , 
			88.91f);
			path.curveTo (25.77f, 75.28f, 36.38f , 74.84f, 43.84f , 
			75.16f);
			path.curveTo (45.65f, 75.20f, 47.53f , 75.46f, 49.08f , 
			76.49f);
			path.lineTo (48.72f, 76.28f);
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
//		case MITOCHONDRIA:
//			RoundRectangle2D.Double m1 = new RoundRectangle2D.Double (0, 0, 200, 100, 40, 60);
//			Ellipse2D.Double m2 = new Ellipse2D.Double (4, 4, 192, 92);
//			area.add(new Area(m1));
//			area.exclusiveOr(new Area(m2));
//			break;
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