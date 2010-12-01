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
package org.pathvisio.model;

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

import org.pathvisio.view.GenMAPPShapes;
import org.pathvisio.view.GenMAPPShapes.Internal;
import org.pathvisio.view.ShapeRegistry;

/**
   Extensible enum
 */
public class ShapeType implements IShape
{
	private static List<ShapeType> values = new ArrayList<ShapeType>();

	public static final ShapeType NONE = new ShapeType (null, "None", "None");
	public static final ShapeType RECTANGLE = new ShapeType (new Rectangle (0, 0, 10, 10), "Rectangle");
	public static final ShapeType ROUNDED_RECTANGLE = new ShapeType (null, "RoundedRectangle")
	{
		public Shape getShape (double mw, double mh)
		{
			return new RoundRectangle2D.Double (0, 0, mw, mh, 20, 20);
		}	
	};
	
	public static final ShapeType OVAL = new ShapeType (new Ellipse2D.Double (0, 0, 10, 10), "Oval");
	public static final ShapeType ARC = new ShapeType (new Arc2D.Double (0, 0, 10, 10, 0, -180, Arc2D.OPEN), "Arc");
	public static final ShapeType TRIANGLE = new ShapeType (GenMAPPShapes.getRegularPolygon (3, 10, 10), "Triangle", "Poly"); // poly ;in MAPP
	public static final ShapeType PENTAGON = new ShapeType (GenMAPPShapes.getRegularPolygon (5, 10, 10), "Pentagon", "Poly"); // poly in MAPP
	public static final ShapeType HEXAGON = new ShapeType (GenMAPPShapes.getRegularPolygon (6, 10, 10), "Hexagon", "Poly"); // poly in MAPP
	public static final ShapeType BRACE = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.BRACE), "Brace", "Brace");	
	public static final ShapeType MITOCHONDRIA = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.MITOCHONDRIA), "Mitochondria", null);
	public static final ShapeType SARCOPLASMICRETICULUM = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.SARCOPLASMICRETICULUM), "Sarcoplasmic Reticulum", null);
	public static final ShapeType ENDOPLASMICRETICULUM = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.ENDOPLASMICRETICULUM), "Endoplasmic Reticulum", null);
	public static final ShapeType GOLGIAPPARATUS = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.GOLGIAPPARATUS), "Golgi Apparatus", null);
	
	@Deprecated
	public static final ShapeType CELL = new ShapeType (GenMAPPShapes.getCombinedShape (Internal.CELL), "Cell", null);
	@Deprecated
	public static final ShapeType NUCLEUS = new ShapeType (GenMAPPShapes.getCombinedShape (Internal.NUCLEUS), "Nucleus", null);
	@Deprecated
	public static final ShapeType ORGANELLE = new ShapeType (GenMAPPShapes.getCombinedShape (Internal.ORGANELLE), "Organelle", null);
	@Deprecated
	public static final ShapeType VESICLE = new ShapeType (GenMAPPShapes.getCombinedShape (Internal.VESICLE), "Vesicle", "Vesicle");
	@Deprecated
	public static final ShapeType MEMBRANE = new ShapeType (null, "Membrane", "Membrane");
	@Deprecated
	public static final ShapeType CELLA = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.CELLA), "CellA", "CellA");
	@Deprecated
	public static final ShapeType RIBOSOME = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.RIBOSOME), "Ribosome", "Ribosome");
	@Deprecated
	public static final ShapeType ORGANA = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.ORGANA), "OrganA", "OrganA");
	@Deprecated
	public static final ShapeType ORGANB = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.ORGANB), "OrganB", "OrganB");
	@Deprecated
	public static final ShapeType ORGANC = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.ORGANA), "OrganC", "OrganC");
	@Deprecated
	public static final ShapeType PROTEINB = new ShapeType (GenMAPPShapes.getPluggableShape (Internal.PROTEINB), "ProteinComplex", "ProteinB");

	
	//This map is used to track deprecated shapetypes for conversion and exclusion from gui
	public static final Map<ShapeType, ShapeType> deprecatedMap = new HashMap<ShapeType, ShapeType>();
	static { 
		deprecatedMap.put(CELL, ROUNDED_RECTANGLE);
		deprecatedMap.put(ORGANELLE, ROUNDED_RECTANGLE);
		deprecatedMap.put(MEMBRANE, ROUNDED_RECTANGLE);
		deprecatedMap.put(CELLA, OVAL);
		deprecatedMap.put(NUCLEUS, OVAL);
		deprecatedMap.put(ORGANA, OVAL);
		deprecatedMap.put(ORGANB, OVAL);
		deprecatedMap.put(ORGANC, OVAL);
		deprecatedMap.put(VESICLE, OVAL);   	
		deprecatedMap.put(PROTEINB, HEXAGON);
		deprecatedMap.put(RIBOSOME, HEXAGON);
		// exclude from list for gui
		pruneValues();
	}
	
	/**
	 * Prunes values list for deprecated shapes
	 */
	private static void pruneValues() {
		List<ShapeType> list = new ArrayList<ShapeType>();
		for (int i = 0; i < values.size(); ++i)
		{
			ShapeType s = values.get(i);
			if (!deprecatedMap.containsKey(s)){
				list.add(s);
			}
		}
		values = list;
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

		// add it to the array list.
		values.add (this);
		
		ShapeRegistry.registerShape(this);
	}

	public ShapeType(Shape shape, String name)
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

	/**
	   Returns the names of all registered Shape types, in such a way that the index
	   is equal to it's ordinal value.

	   i.e. ShapeType.fromName(ShapeType.getNames[n]).getOrdinal() == n
	 */
	static public String[] getNames()
	{
		String[] result = new String[values.size()];

		for (int i = 0; i < values.size(); ++i)
		{
			result[i] = values.get(i).getName();
		}
		return result;
	}

	static public ShapeType[] getValues()
	{
		return values.toArray(new ShapeType[0]);
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

	@Override
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