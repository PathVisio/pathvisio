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

/**
 * This class implements a brace and provides 
 * methods to resize and draw it
 */
public class GmmlBrace extends GmmlGraphicsShape
{
	private static final long serialVersionUID = 1L;
	
	public static final int INITIAL_PPO = 10;

	/**
	 * Constructor for mapping a JDOM Element.
	 * @param o	- the GPML element which will be loaded as a GmmlBrace
	 * @param canvas - the GmmlDrawing this GmmlBrace will be part of
	 */
	public GmmlBrace(GmmlDrawing canvas, GmmlDataObject o) {
		super(canvas, o);
		drawingOrder = GmmlDrawing.DRAW_ORDER_BRACE;
		setHandleLocation();
	}
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	public void draw(PaintEvent e, GC buffer)
	{		
		Color c = null;
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
		buffer.setLineWidth (2);
		
		Transform tr = new Transform(e.display);
		rotateGC(buffer, tr);
		
		int cx = (int)gdata.getCenterX();
		int cy = (int)gdata.getCenterY();
		int w = (int)gdata.getWidth();
		int d = (int)gdata.getHeight();
		
		buffer.drawLine (cx + d/2, cy, cx + w/2 - d/2, cy); //line on the right
		buffer.drawLine (cx - d/2, cy, cx - w/2 + d/2, cy); //line on the left
		buffer.drawArc (cx - w/2, cy, d, d, -180, -90); //arc on the left
		buffer.drawArc (cx - d, cy - d,	d, d, -90, 90); //left arc in the middle
		buffer.drawArc (cx, cy - d, d, d, -90, -90); //right arc in the middle
		buffer.drawArc (cx + w/2 - d, cy, d, d, 0, 90); //arc on the right
		
		buffer.setTransform(null);
		
		c.dispose();
		tr.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
}