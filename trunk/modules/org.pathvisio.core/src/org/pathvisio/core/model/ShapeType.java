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
package org.pathvisio.core.model;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.core.view.GenMAPPShapes;
import org.pathvisio.core.view.ShapeRegistry;
import org.pathvisio.core.view.GenMAPPShapes.Internal;

/**
   Extensible enum
 */
public enum ShapeType implements IShape
{
	NONE(null, "None", "None"),
	RECTANGLE(new Rectangle (0, 0, 10, 10), "Rectangle"),
	ROUNDED_RECTANGLE(null, "RoundedRectangle")
	{
		public Shape getShape (double mw, double mh)
		{
			return new RoundRectangle2D.Double (0, 0, mw, mh, 20, 20);
		}	
	},
	
	OVAL(new Ellipse2D.Double (0, 0, 10, 10), "Oval"),
	ARC(new Arc2D.Double (0, 0, 10, 10, 0, -180, Arc2D.OPEN), "Arc"),
	TRIANGLE(GenMAPPShapes.getRegularPolygon (3, 10, 10), "Triangle", "Poly"), // poly ;in MAPP
	PENTAGON(GenMAPPShapes.getRegularPolygon (5, 10, 10), "Pentagon", "Poly"), // poly in MAPP
	HEXAGON(GenMAPPShapes.getRegularPolygon (6, 10, 10), "Hexagon", "Poly"), // poly in MAPP
	BRACE(GenMAPPShapes.getPluggableShape (Internal.BRACE), "Brace", "Brace"),	
	MITOCHONDRIA(GenMAPPShapes.getPluggableShape (Internal.MITOCHONDRIA), "Mitochondria", null),
	SARCOPLASMICRETICULUM(GenMAPPShapes.getPluggableShape (Internal.SARCOPLASMICRETICULUM), "Sarcoplasmic Reticulum", null),
	ENDOPLASMICRETICULUM(GenMAPPShapes.getPluggableShape (Internal.ENDOPLASMICRETICULUM), "Endoplasmic Reticulum", null),
	GOLGIAPPARATUS(GenMAPPShapes.getPluggableShape (Internal.GOLGIAPPARATUS), "Golgi Apparatus", null),
	
	@Deprecated
	CELL(GenMAPPShapes.getCombinedShape (Internal.CELL), "Cell", null),
	@Deprecated
	NUCLEUS(GenMAPPShapes.getCombinedShape (Internal.NUCLEUS), "Nucleus", null),
	@Deprecated
	ORGANELLE(GenMAPPShapes.getCombinedShape (Internal.ORGANELLE), "Organelle", null),
	@Deprecated
	VESICLE(GenMAPPShapes.getCombinedShape (Internal.VESICLE), "Vesicle", "Vesicle"),
	@Deprecated
	MEMBRANE(null, "Membrane", "Membrane"),
	@Deprecated
	CELLA(GenMAPPShapes.getPluggableShape (Internal.CELLA), "CellA", "CellA"),
	@Deprecated
	RIBOSOME(GenMAPPShapes.getPluggableShape (Internal.RIBOSOME), "Ribosome", "Ribosome"),
	@Deprecated
	ORGANA(GenMAPPShapes.getPluggableShape (Internal.ORGANA), "OrganA", "OrganA"),
	@Deprecated
	ORGANB(GenMAPPShapes.getPluggableShape (Internal.ORGANB), "OrganB", "OrganB"),
	@Deprecated
	ORGANC(GenMAPPShapes.getPluggableShape (Internal.ORGANA), "OrganC", "OrganC"),
	@Deprecated
	PROTEINB(GenMAPPShapes.getPluggableShape (Internal.PROTEINB), "ProteinComplex", "ProteinB"),

	;

	//This map is used to track deprecated shapetypes for conversion and exclusion from gui
	public static final Map<ShapeType, ShapeType> DEPRECATED_MAP = new HashMap<ShapeType, ShapeType>();
	private static final List<ShapeType> VISIBLE_VALUES = new ArrayList<ShapeType>();
	static { 
		DEPRECATED_MAP.put(CELL, ROUNDED_RECTANGLE);
		DEPRECATED_MAP.put(ORGANELLE, ROUNDED_RECTANGLE);
		DEPRECATED_MAP.put(MEMBRANE, ROUNDED_RECTANGLE);
		DEPRECATED_MAP.put(CELLA, OVAL);
		DEPRECATED_MAP.put(NUCLEUS, OVAL);
		DEPRECATED_MAP.put(ORGANA, OVAL);
		DEPRECATED_MAP.put(ORGANB, OVAL);
		DEPRECATED_MAP.put(ORGANC, OVAL);
		DEPRECATED_MAP.put(VESICLE, OVAL);   	
		DEPRECATED_MAP.put(PROTEINB, HEXAGON);
		DEPRECATED_MAP.put(RIBOSOME, HEXAGON);
		
		// prune from list for gui
		for (ShapeType s : values())
		{
			if (!DEPRECATED_MAP.containsKey(s)){
				VISIBLE_VALUES.add(s);
			}
		}
	}
	
	private final String name;
	private final String mappName;
	private final boolean isResizeable;
	private final boolean isRotatable;
	private final Shape sh;

	/**
	   The constructor is private so we have to use the "create"
	   method to add new ShapeTypes. In the create method we make sure
	   that the same object can't get added twice.

	   Note that mappName may be null for Shapes that are not supported by GenMAPP.
	 */
	private ShapeType(Shape shape, String name, String mappName)
	{
		this(shape, name, mappName, true, true);
	}

	private ShapeType (Shape shape, String name, String mappName, boolean isResizeable, boolean isRotatable)
	{
		if (name == null) { throw new NullPointerException(); }
		this.isResizeable = isResizeable;
		this.isRotatable = isRotatable;
		this.mappName = mappName;
		this.name  = name;
		this.sh = shape;
		
		ShapeRegistry.registerShape(this);
	}

	private ShapeType(Shape shape, String name)
	{
		this (shape, name, name, true, true);
	}

	public String getMappName()
	{
		return mappName;
	}

	/**
	   Stable identifier for this ShapeType.
	 */
	public String getName ()
	{
		return name;
	}

	static public String[] getVisibleNames()
	{
		String[] result = new String[VISIBLE_VALUES.size()];

		for (int i = 0; i < VISIBLE_VALUES.size(); ++i)
		{
			result[i] = VISIBLE_VALUES.get(i).getName();
		}
		return result;
	}

	static public ShapeType[] getVisibleValues()
	{
		return VISIBLE_VALUES.toArray(new ShapeType[0]);
	}

	public String toString()
	{
		return name;
	}

	public boolean isResizeable()
	{
		return isResizeable;
	}

	public boolean isRotatable()
	{
		return isRotatable;
	}

	public Shape getShape(double mw, double mh)
	{
		// now scale the path so it has proper w and h.
		Rectangle r = sh.getBounds();
		AffineTransform at = new AffineTransform();
		at.translate ( - r.x,  - r.y);
		at.scale (mw / r.width, mh / r.height);
		return at.createTransformedShape (sh);
	}

}