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
import org.eclipse.swt.graphics.Transform;

import util.SwtUtils;
import data.GmmlDataObject;
import data.ShapeType;

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
		drawingOrder = GmmlDrawing.DRAW_ORDER_SHAPE;
		setHandleLocation();
	}
		
	public void draw(PaintEvent e, GC buffer)
	{	
		Color c = null;
		Color b = null;
		if (isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
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
		
		int startX = (int)gdata.getLeft();
		int startY = (int)gdata.getTop();
		int width = (int)gdata.getWidth();
		int height = (int)gdata.getHeight();
		
		switch (gdata.getShapeType())
		{
			case ShapeType.RECTANGLE: 
				buffer.setLineWidth (1);
				if (!gdata.isTransparent())
					buffer.fillRectangle (
						startX,	startY,	width, height);
				buffer.drawRectangle (
					startX,	startY,	width, height);				
				break;
			case ShapeType.OVAL:				
				buffer.setLineWidth (1);
				if (!gdata.isTransparent())
					buffer.fillOval (
						startX, startY,	width, height);
				buffer.drawOval (
					startX, startY,	width, height);
				break;
			case ShapeType.ARC:
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
						startX, startY,	width, height, 0, 180);
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