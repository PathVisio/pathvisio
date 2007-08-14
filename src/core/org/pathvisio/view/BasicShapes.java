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
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

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
	}
}