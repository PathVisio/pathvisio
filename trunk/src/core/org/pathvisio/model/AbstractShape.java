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

import org.pathvisio.view.ShapeRegistry;

public class AbstractShape implements IShape
{
	private String name;
	private String mappName;
	private boolean isResizeable;
	private boolean isRotatable;
	private Shape sh;
	
	public AbstractShape (Shape sh, String name, String mappName, boolean isResizeable, boolean isRotatable)
	{
		this.name = name;
		this.sh = sh;
		this.mappName = mappName;
		this.isRotatable = isRotatable;
		this.isResizeable = isResizeable;
		ShapeRegistry.registerShape(this);
	}

	public AbstractShape (Shape sh, String name)
	{
		this (sh, name, name, true, true);
	}

	@Override
	public String getMappName()
	{
		return mappName;
	}

	@Override
	public String getName()
	{
		return name;
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

	@Override
	public boolean isResizeable()
	{
		return isResizeable;
	}

	@Override
	public boolean isRotatable()
	{
		return isRotatable;
	}
	
	public String toString()
	{
		return name;
	}

}
