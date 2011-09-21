// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.view;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;

import org.pathvisio.core.model.AbstractShape;
import org.pathvisio.core.model.IShape;

/**
   The Shape registry stores all arrow heads and shapes

   at this moment the shape registry initializes itself,
   by calling  registerShape() on BasicShapes, GenMAPPShapes and MIMShapes.
 */

public class ShapeRegistry
{
	private static Shape defaultShape = null;
	public static final IShape DEFAULT_SHAPE;
	private static ArrowShape defaultArrow = null;
	private static AnchorShape defaultAnchor = null;

	private static Map <String, IShape> shapeMap = new HashMap <String, IShape>();
	private static Map <String, ArrowShape> arrowMap = new HashMap <String, ArrowShape>();
	private static Map <String, AnchorShape> anchorMap = new HashMap <String, AnchorShape>();
	private static Map<String, IShape> mappMappings = new HashMap<String, IShape>();

	static
	{
		GeneralPath temp = new GeneralPath();
		temp.moveTo (-50,-50);
		temp.lineTo (50,-50);
		temp.lineTo (50,50);
		temp.lineTo (-50,50);
		temp.closePath ();
		temp.moveTo (-30,-30);
		temp.lineTo (30,30);
		temp.moveTo (-30,30);
		temp.lineTo (30,-30);
		defaultArrow = new ArrowShape (temp, ArrowShape.FillType.OPEN);

		temp = new GeneralPath();
		temp.moveTo (0,0);
		temp.lineTo (10,0);
		temp.lineTo (10,10);
		temp.lineTo (0,10);
		temp.closePath ();
		temp.moveTo (2,2);
		temp.lineTo (8,8);
		temp.moveTo (2,8);
		temp.lineTo (8,2);
		defaultShape = temp;
		DEFAULT_SHAPE = new AbstractShape(defaultShape, "default");

		BasicShapes.registerShapes();
		GenMAPPShapes.registerShapes();
	}

	/**
	   looks up the ShapeType corresponding to that name.
	 */
	public static IShape fromName (String value)
	{
		return shapeMap.get(value);
	}

    /*
	 * Warning when using fromMappName: in case value == Poly, 
	 * this will return Triangle. The caller needs to check for
	 * this special
	 * case.
	 */
	public static IShape fromMappName (String value)
	{
		return mappMappings.get(value);
	}

	public static void registerShape(IShape ish)
	{
		shapeMap.put(ish.getName(), ish);
		if (ish.getMappName() != null)
		{
			mappMappings.put (ish.getMappName(), ish);
		}
	}

	/**
	 * Register an arrow shape
	 * @param key The key used to identify the arrow shape
	 * @param sh The shape used to draw the stroke
	 * @param fillType The fill type, see {@link ArrowShape}
	 * @param lineEndingLength The line ending width
	 */
	static public void registerArrow (String key, Shape sh, ArrowShape.FillType fillType, int lineEndingLength) {
		//pass in zero as the gap between line line ending and anchor
		arrowMap.put(key, new ArrowShape (sh, fillType, lineEndingLength));
	}

	/**
	 * Register an arrow shape
	 * @param key The key used to identify the arrow shape
	 * @param sh The shape used to draw the stroke and fill (in case fillType is open or closed)
	 * @param fillType The fill type, see {@link ArrowShape}
	 */
	static public void registerArrow (String key, Shape sh, ArrowShape.FillType fillType)
	{
		arrowMap.put (key, new ArrowShape (sh, fillType));
	}

	static public void registerAnchor (String key, Shape sh)
	{
		anchorMap.put (key, new AnchorShape (sh));
	}

	/**
	   Returns a named arrow head. The shape is normalized so that it
	   fits with a line that goes along the positive x-axis.  The tip
	   of the arrow head is in 0,0.
	 */
	public static ArrowShape getArrow(String name)
	{
		ArrowShape sh = arrowMap.get (name);
		if (sh == null)
		{
			sh = defaultArrow;
		}
		return sh;
		// TODO: here we return a reference to the object on the
		// registry itself we should really return a clone, although
		// in practice this is not a problem since we do a affine
		// transform immediately after.
	}

	/**
	 * Returns an anchor shape
	 */
	public static AnchorShape getAnchor(String name) {
		AnchorShape sh = anchorMap.get (name);
		if (sh == null)
		{
			sh = defaultAnchor;
		}
		return sh;
	}

}