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
		
	public int getDrawingOrder() {
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
		case RECTANGLE:
		case OVAL:
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
			s = new Ellipse2D.Double(x, y, w, h);
			break;
		case ARC:;
			s = new Arc2D.Double (x, y, w, h, 0, -180, Arc2D.OPEN);
			break;
		case BRACE:
			GeneralPath p = new GeneralPath();
            p.moveTo(x, y + h);
            p.quadTo(x, y + h/2, x + h/2, y + h/2);
            p.lineTo(cx - h/2, y + h/2);
            p.quadTo(cx, y + h/2, cx, y);
            p.quadTo(cx, y + h/2, cx + h/2, y + h/2);
            p.lineTo(x + w - h/2, y + h/2);
            p.quadTo(x + w, y + h/2, x + w, y + h);
            s = p;
			break;
		default:
			s = new Rectangle(x, y, w, h);
			break;
		}
		AffineTransform t = new AffineTransform();
		t.rotate(gdata.getRotation(), cx, cy);
		return t.createTransformedShape(s);
	}
}