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

import org.pathvisio.model.PathwayElement;

/**
 * This class represents a GMMLShape, which can be a 
 * rectangle or ellips, depending of its type.
 */
public class Shape extends GraphicsShape
{
	private static final long serialVersionUID = 1L;
			
	/**
	 * Constructor for this class
	 * @param canvas - the VPathway this Shape will be part of
	 */
	public Shape(VPathway canvas, PathwayElement o)
	{
		super(canvas, o);
		setHandleLocation();
	}
		
	public int getNaturalOrder() {
		switch(gdata.getShapeType()) {
		case BRACE:
			return VPathway.DRAW_ORDER_BRACE;
		default:
			return VPathway.DRAW_ORDER_SHAPE;
		}
	}

	public void doDraw(Graphics2D g)
	{					
		Color fillcolor = gdata.getFillColor();
		Color linecolor = gdata.getColor();
		if(isSelected())
		{
			linecolor = selectColor;
		} 

		java.awt.Shape shape = getFillShape();
		
		g.setColor(linecolor);
		g.draw(shape);
		
		switch(gdata.getShapeType())
		{		
		case ARC:
		case BRACE:
			// don't fill arcs or braces
			// TODO: this exception should disappear in the future,
			// when we've made sure all pathways on wikipathways have
			// transparent arcs and braces
			break;			
		default:
			// fill the rest
			if(!gdata.isTransparent()) {
				g.setColor(fillcolor);
				g.fill(shape);
			}
			break;
		}
		
		if (isHighlighted())
		{
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke (HIGHLIGHT_STROKE_WIDTH));
			g.draw (shape);
		}
	}	

		
	protected java.awt.Shape getFillShape(float sw) {
		int x = getVLeft();
		int y = getVTop();
		int w = getVWidth() + (int)sw;
		int h = getVHeight() + (int)sw;
		int cx = getVCenterX();
		int cy = getVCenterY();
		
		java.awt.Shape s = null;
		
		switch(gdata.getShapeType()) {
		case OVAL:
			s = ShapeRegistry.getShape ("Oval", x, y, w, h);
			break;
		case ARC:
			s = ShapeRegistry.getShape ("Arc", x, y, w, h);
			break;
		case BRACE:
			s = ShapeRegistry.getShape ("Brace", x, y, w, h);
			break;
		case RECTANGLE:
			s = ShapeRegistry.getShape ("Rectangle", x, y, w, h);
			break;
		case PENTAGON:
			s = ShapeRegistry.getShape ("Pentagon", x, y, w, h);
			break;
		case HEXAGON:
			s = ShapeRegistry.getShape ("Hexagon", x, y, w, h);
			break;
		case TRIANGLE:
			s = ShapeRegistry.getShape ("Triangle", x, y, w, h);
			break;
		case CELLA:
			s = ShapeRegistry.getShape ("CellA", x, y, w, h);
			break;
		case ORGANA:
			s = ShapeRegistry.getShape ("OrganA", x, y, w, h);
			break;
		case ORGANB:
			s = ShapeRegistry.getShape ("OrganB", x, y, w, h);
			break;
		case ORGANC:
			s = ShapeRegistry.getShape ("OrganC", x, y, w, h);
			break;
		case RIBOSOME:
			s = ShapeRegistry.getShape ("Ribosome", x, y, w, h);
			break;
		case PROTEINB:
			s = ShapeRegistry.getShape ("ProteinB", x, y, w, h);
			break;
		case VESICLE:
			s = ShapeRegistry.getShape ("Vesicle", x, y, w, h);
			break;
		default:
			s = ShapeRegistry.getShape ("Default", x, y, w, h);
			break;
		}
		AffineTransform t = new AffineTransform();
		t.rotate(gdata.getRotation(), cx, cy);
		return t.createTransformedShape(s);
	}
}