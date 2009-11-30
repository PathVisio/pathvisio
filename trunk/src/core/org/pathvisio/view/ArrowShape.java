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
   ArrowShapes determine how the ending of a line can be drawn. These
   are arrows in a broad sense - they could be T-bars for example.
   <p>
   ArrowShapes have a fill type. FillType can be OPEN, CLOSED or WIRE.
   The fillType determines whether the body of the arrow head is filled
   with the foreground color or with the canvas color.
   <p>
   For the outline, the shape returned by getShape() will be used.
   getFillShape() optionally defines a different shape for the body. If there
   is no separate fillShape defined, the same shape is used for the outline
   and for the body.

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
	/**
	 * Enumerates possible ways to combine the outline and body.
	 */
	public enum FillType
	{
		/**
		 * Open fill-type, where the outline is colored with the foreground color and the
		 * body is colored with the canvas color.
		 */
		OPEN,

		/**
		 * Closed fill-type, where both the outline and the body are
		 * colored with the line color.
		 */
		CLOSED,

		/**
		 * Wire fill-type, there is only an outline.
		 */
		WIRE
	}

	/**
	 * Normally, this constructor is not called directly.
	 * Use {@link ShapeRegistry.registerShape} instead to define a new ArrowShape.
	 */
	public ArrowShape (Shape shape, FillType fillType, int gap) {
		this.shape = shape;
		this.fillType = fillType;
		this.gap = gap;

	}

	/**
	 * Normally, this constructor is not called directly.
	 * Use {@link ShapeRegistry.registerShape} instead to define a new ArrowShape.
	 */
	public ArrowShape (Shape shape, FillType fillType) {
		this.shape = shape;
		this.fillType = fillType;
	}

	/**
	 * @return one of {@link FillType.OPEN}, {@link FillType.CLOSED} or {@link FillType.WIRE}
	 */
	public FillType getFillType () { return fillType; }

	/**
	 * @return the outline for this arrow type.
	 */
	public Shape getShape() { return shape; }


	/**
	 * @return the gap at the end of the line, for arrow shapes
	 * 	that should not overlap the thing the line is connected with.
	 */
	public double getGap()
	{
		return (double)gap;
	}

	private Shape shape;
	private FillType fillType;
	private int gap = 0;
}