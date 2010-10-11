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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import org.pathvisio.model.LineStyle;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.ShapeType;

/**
 * This class represents a GMMLShape, which can be a
 * rectangle or ellips, depending of its type.
 * //TODO: rename this class to something other than Shape,
 * because it is confusing with java.awt.Shape
 */
public class Shape extends GraphicsShape
{
	static final int FUZZY_STROKE_WIDTH = 7;

	/**
	 * Constructor for this class
	 * @param canvas - the VPathway this Shape will be part of
	 */
	public Shape(VPathway canvas, PathwayElement o)
	{
		super(canvas, o);
	}
	
	public void doDraw(Graphics2D g)
	{
		g.setColor(getLineColor());
		setLineStyle(g);
		drawShape(g);
		
		g.setFont(getVFont());
		drawTextLabel(g);
		
		if (isHighlighted())
		{
			java.awt.Shape shape = getShape(true, false);
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke (HIGHLIGHT_STROKE_WIDTH));
			g.draw (shape);
		}
		super.doDraw((Graphics2D)g.create());
	}

	protected java.awt.Shape calculateVOutline() {
		Area a = new Area(super.calculateVOutline());
		a.add(new Area(getShape(true, FUZZY_STROKE_WIDTH))); //fuzzy matching for outline
		if(!gdata.isTransparent()) {
			//Also include the filled area when not transparent
			java.awt.Shape fill = getShape(true, false);
			a.add(new Area(fill));
		}
		return a;
	}

	protected java.awt.Shape getShape(boolean rotate, float sw) {
		double x = getVLeft();
		double y = getVTop();
		double w = getVWidth();
		double h = getVHeight();
		double cx = getVCenterX();
		double cy = getVCenterY();

		java.awt.Shape s = null;

		if (gdata.getShapeType() == null || gdata.getShapeType() == ShapeType.NONE)
		{
			s = ShapeRegistry.getShape ("Default", x, y, w, h);
		}
		else
		{
			s = ShapeRegistry.getShape (
					gdata.getShapeType().getName(),
					x, y, w, h);
		}

		if(rotate) {
			AffineTransform t = new AffineTransform();
			t.rotate(gdata.getRotation(), cx, cy);
			s = t.createTransformedShape(s);
		}

		if(sw > 0) {
			if (gdata.getLineStyle() == LineStyle.DOUBLE){
				// correction factor for composite stroke
				sw = (float) (gdata.getLineThickness() * 4); 
			}
			Stroke stroke = new BasicStroke(sw);
			s = stroke.createStrokedShape(s);
		}
		return s;
	}
}