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

import java.awt.geom.GeneralPath;

import org.pathvisio.core.model.AbstractShape;
import org.pathvisio.core.model.IShape;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.view.ArrowShape;
import org.pathvisio.core.view.ShapeRegistry;

/**
 * User-defined shapes example
 */
public class MyShapes
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
