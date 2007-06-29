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

import java.awt.Color;
import java.awt.Graphics2D;

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
		if(isSelected()) {
			linecolor = selectColor;
		} else if (isHighlighted()) {
			linecolor = highlightColor;
		}
		
		int x = getVLeft();
		int y = getVTop();
		int w = getVWidth();
		int h = getVHeight();
		int cx = getVCenterX();
		int cy = getVCenterY();
						
		g.rotate(gdata.getRotation(), cx, cy);
		
		switch(gdata.getShapeType()) {
		case OVAL:
			if(!gdata.isTransparent()) {
				g.setColor(fillcolor);
				g.fillOval(x, y, w, h);
			}
			g.setColor(linecolor);
			g.drawOval(x, y, w, h);
			break;
		case ARC:
			g.setColor(linecolor);
			g.drawArc(x, y, w, h, 0, -180);
			break;
		case BRACE:
			g.setColor(linecolor);
			g.drawLine (cx + h/2, cy, cx + w/2 - h/2, cy); //line on the right
			g.drawLine (cx - h/2, cy, cx - w/2 + h/2, cy); //line on the left
			g.drawArc (cx - w/2, cy, h, h, -180, -90); //arc on the left
			g.drawArc (cx - h, cy - h,	h, h, -90, 90); //left arc in the middle
			g.drawArc (cx, cy - h, h, h, -90, -90); //right arc in the middle
			g.drawArc (cx + w/2 - h, cy, h, h, 0, 90); //arc on the right
			break;
		default:
			if(!gdata.isTransparent()) {
				g.setColor(fillcolor);
				g.fillRect(x, y, w, h);
			}
			g.setColor(linecolor);
			g.drawRect(x, y, w, h);
			break;
		}
	}	
}