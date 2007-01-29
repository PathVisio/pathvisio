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

import java.awt.Rectangle;
import java.awt.Shape;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import data.GmmlDataObject;

public class GmmlInfoBox extends GmmlGraphics {
	
	//Elements not stored in gpml
	String fontName			= "Times New Roman";
	String fontWeight		= "regular";
	static final double M_INITIAL_FONTSIZE	= 10.0;
	
	int sizeX = 1;
	int sizeY = 1; //Real size is calculated on first call to draw()
	
	public GmmlInfoBox (GmmlDrawing canvas, GmmlDataObject o) {
		super(canvas, o);
		canvas.setMappInfo(this);
		drawingOrder = GmmlDrawing.DRAW_ORDER_MAPPINFO;		
	}
	
	public Point getBoardSize() { return new Point((int)gdata.getMBoardWidth(), (int)gdata.getMBoardHeight()); }
	
	int getVFontSize()
	{
		return (int)(vFromM(M_INITIAL_FONTSIZE));
	}
			
	protected void vMoveBy(double dx, double dy)
	{
		markDirty();
		gdata.setMapInfoTop (gdata.getMapInfoTop()  + (int)dy);
		gdata.setMapInfoLeft (gdata.getMapInfoLeft() + (int)dx);
		markDirty();
	}
	
	public void draw(PaintEvent e) 
	{
		draw(e, e.gc);
	}
	
	public void draw(PaintEvent e, GC buffer) 
	{		
		sizeX = 1; //Reset sizeX
		
		Font fBold = new Font(e.display, fontName, getVFontSize(), SWT.BOLD);
		Font fNormal = new Font(e.display, fontName, getVFontSize(), SWT.NONE);
		
		if (isSelected())
		{
			buffer.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
		}
		else 
		{
			buffer.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
		}
				
		//Draw Name, Organism, Data-Source, Version, Author, Maintained-by, Email, Availability and last modified
		String[][] text = new String[][] {
				{"Name: ", gdata.getMapInfoName()},
				{"Maintained by: ", gdata.getMaintainedBy()},
				{"Email: ", gdata.getEmail()},
				{"Availability: ", gdata.getAvailability()},
				{"Last modified: ", gdata.getLastModified()},
				{"Organism: ", gdata.getOrganism()},
				{"Data Source: ", gdata.getDataSource()}};
		int shift = 0;
		int mapInfoLeft = gdata.getMapInfoLeft();
		int mapInfoTop = gdata.getMapInfoTop();
		for(String[] s : text)
		{
			if(s[1] == null || s[1].equals("")) continue; //Skip empty labels
			buffer.setFont(fBold);
			Point labelSize = buffer.textExtent(s[0], SWT.DRAW_TRANSPARENT);
			buffer.drawString(s[0], mapInfoLeft, mapInfoTop + shift, true);
			buffer.setFont(fNormal);
			Point infoSize = buffer.textExtent(s[1], SWT.DRAW_TRANSPARENT);
			buffer.drawString(s[1], mapInfoLeft + labelSize.x, mapInfoTop + shift, true);
			shift += Math.max(infoSize.y, labelSize.y);
			sizeX = Math.max(sizeX, infoSize.x + labelSize.x);
		}
		sizeY = shift;
		
		fBold.dispose();
		fNormal.dispose();
	}

	protected Shape getVOutline() {
		return new Rectangle(gdata.getMapInfoLeft(), gdata.getMapInfoTop(), sizeX, sizeY);
	}

}
 