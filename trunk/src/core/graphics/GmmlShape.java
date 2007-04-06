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
package graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;

import preferences.GmmlPreferences;
import util.SwtUtils;
import data.GmmlDataObject;

/**
 * This class represents a GMMLShape, which can be a 
 * rectangle or ellips, depending of its type.
 */
public class GmmlShape extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;
			
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this GmmlShape will be part of
	 */
	public GmmlShape(GmmlDrawing canvas, GmmlDataObject o)
	{
		super(canvas, o);
		setHandleLocation();
	}
		
	public int getDrawingOrder() {
		switch(gdata.getShapeType()) {
		case BRACE:
			return GmmlDrawing.DRAW_ORDER_BRACE;
		default:
			return GmmlDrawing.DRAW_ORDER_SHAPE;
		}
	}
	
	public void draw(PaintEvent e, GC buffer)
	{	
		Color c = null;
		Color b = null;
		if (isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
		}
		else if (isHighlighted())
		{
			RGB rgb = GmmlPreferences.getColorProperty(GmmlPreferences.PREF_COL_HIGHLIGHTED);
			c = SwtUtils.changeColor(c, rgb, e.display);
		}
		else 
		{
			c = SwtUtils.changeColor(c, gdata.getColor(), e.display);
		}
		buffer.setForeground (c);
		buffer.setLineStyle (SWT.LINE_SOLID);
		b = SwtUtils.changeColor(c, gdata.getFillColor(), e.display);
		buffer.setBackground (b);
		
		Transform tr = new Transform(e.display);
		rotateGC(buffer, tr);
		
		int vStartX = getVLeft();
		int vStartY = getVTop();
		int vWidth = getVWidth();
		int vHeight = getVHeight();
		
		switch (gdata.getShapeType())
		{
			case RECTANGLE: 
				buffer.setLineWidth (1);
				if (!gdata.isTransparent())
					buffer.fillRectangle (
						vStartX,	vStartY,	vWidth, vHeight);
				buffer.drawRectangle (
					vStartX,	vStartY,	vWidth, vHeight);				
				break;
			case OVAL:				
				buffer.setLineWidth (1);
				if (!gdata.isTransparent())
					buffer.fillOval (
						vStartX, vStartY,	vWidth, vHeight);
				buffer.drawOval (
					vStartX, vStartY,	vWidth, vHeight);
				break;
			case ARC:
				buffer.setLineWidth (1);
				/**
				 * Arcs are different from Oval and Rect, in that
				 * they are not filled in GenMAPP, and that the
				 * color column is used for the line color.
				 * Likewise, we don't fill them in PathVisio.
				 */
//				if (!gdata.isTransparent())
//					buffer.fillArc(
//							startX, startY,	width, height, 0, 180);					
				buffer.drawArc(
						vStartX, vStartY,	vWidth, vHeight, 0, -180);
				break;
			case BRACE:
				buffer.setLineWidth (2);
								
				int cx = getVCenterX();
				int cy = getVCenterY();
				int w = getVWidth();
				int d = getVHeight();
				
				buffer.drawLine (cx + d/2, cy, cx + w/2 - d/2, cy); //line on the right
				buffer.drawLine (cx - d/2, cy, cx - w/2 + d/2, cy); //line on the left
				buffer.drawArc (cx - w/2, cy, d, d, -180, -90); //arc on the left
				buffer.drawArc (cx - d, cy - d,	d, d, -90, 90); //left arc in the middle
				buffer.drawArc (cx, cy - d, d, d, -90, -90); //right arc in the middle
				buffer.drawArc (cx + w/2 - d, cy, d, d, 0, 90); //arc on the right
				break;
		}

		buffer.setTransform(null);
		
		c.dispose();
		b.dispose();
		tr.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
}