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

/**
   ArrowShapes are shapes with an "open" attribute.  If open is true,
   it should be filled with the background color and stroked with the
   line shape.  For filling, the shape returned by getFillShape() will be used,
   for stroking, the shape returned by getShape() will be used. If fillShape is
   not specified in the constructor, shape will be used.
   If open is false, it should be filled and stroked with
   the line color.

	   <pre>
          open             closed             wire
              |\               #\             \
	    ______| \        ______##\        _____\
			  | /              ##/             /
			  |/               #/             /
	   </pre>
*/
public class ArrowShape
{
	public static final int OPEN = 0;
	public static final int CLOSED = 1;
	public static final int WIRE = 2;
	
	public ArrowShape (Shape shape, Shape fillShape, int fillType) {
		this.shape = shape;
		this.fillType = fillType;
		this.fillShape = fillShape != null ? fillShape : shape;
	}
	
	public ArrowShape (Shape shape, int fillType)
	{
		this(shape, shape, fillType);
	}
	
	public int getFillType () { return fillType; }
	public Shape getShape() { return shape; }
	public Shape getFillShape() { return fillShape; }
	
	Shape shape;
	int fillType;
	Shape fillShape;
}