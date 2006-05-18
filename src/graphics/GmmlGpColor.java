package graphics;


import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Region;

import colorSet.*;
import data.*;
import data.GmmlGex.RefData;

public class GmmlGpColor {
	// Pre-cached data / ensIds
	//	-> reload on loading new gex
	// Parent
	//	-> GmmlGeneProduct
	// 
	
	GmmlGdb gmmlGdb;
	GmmlGex gmmlGex;
	GmmlDrawing canvas;
	GmmlGeneProduct parent;
	
	// Cache some data on loading mapp
	Hashtable dataHash;
		// {refId : ArrayList data}
	public ArrayList ensIds;
	public ArrayList refIds;
	
	Vector sampleData;
	
	public GmmlGpColor(GmmlGeneProduct parent)
	{
		this.parent = parent;
		canvas = parent.canvas;
		gmmlGdb = canvas.gmmlVision.gmmlGdb;
		gmmlGex = canvas.gmmlVision.gmmlGex;
	}
	
	private static final double COLOR_AREA_RATIO = 0.5;
	protected void draw(PaintEvent e, GC buffer)
	{
		Color c = new Color(e.display, GmmlGeneProduct.INITIAL_FILL_COLOR);
		Font f = new Font(e.display, "ARIAL", parent.fontSize, SWT.NONE);
		
		Rectangle r = parent.getBounds();
		buffer.setBackground(c);
		buffer.fillRectangle(r.x, r.y, r.width, r.height);
		if(canvas.colorSetIndex > -1 && !canvas.editMode)
		{
			// Get visualization area
			Rectangle colorArea = parent.getBounds();
			colorArea.width = (int)(COLOR_AREA_RATIO * colorArea.width);
//			colorArea.width = (int)(colorArea.width / 2);
			colorArea.x = colorArea.x + colorArea.width;
			
			if(gmmlGex.data.containsKey(parent.name))
			{
				colorByData(e, buffer, c, colorArea);
			}
			else
			{				
				colorByGeneNotFound(e, buffer, c, colorArea);
			}
			drawLabel(e, buffer, c, f);
			
		} else {			
			buffer.setFont (f);
			Point textSize = buffer.textExtent (parent.geneID);
			
			c = new Color(e.display, parent.color);
			buffer.setForeground(c);
			buffer.drawString (parent.geneID, 
				(int) parent.centerx - (textSize.x / 2) , 
				(int) parent.centery - (textSize.y / 2), true);
		}
		
		c.dispose();
		f.dispose();
	}
	
	private void drawLabel(PaintEvent e, GC buffer, Color c, Font f)
	{
		Point textSize = null;
		Rectangle r = parent.getBounds();
		r.width = (int)((1 - COLOR_AREA_RATIO) * r.width);

		f = new Font(e.display, "ARIAL", (int)((1 - COLOR_AREA_RATIO) * parent.fontSize), SWT.NONE);
		buffer.setFont (f);
		textSize = buffer.textExtent (parent.geneID);
		
		c = new Color(e.display, parent.color);
		buffer.setForeground(c);
		buffer.drawString (parent.geneID, 
			r.x + (int)(r.width / 2) - (int)(textSize.x / 2),
			r.y + (int)(r.height / 2) - (int)(textSize.y / 2), true);
	}
	
	private void colorByGeneNotFound(PaintEvent e, GC buffer, Color c, Rectangle colorArea)
	{
		GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(canvas.colorSetIndex);
		
		c = new Color(e.display, cs.color_gene_not_found);
		
		buffer.setBackground(c);
		buffer.fillRectangle(colorArea.x, colorArea.y, colorArea.width, colorArea.height);
	}
	
	private void colorByData(PaintEvent e, GC buffer, Color c, Rectangle colorArea)
	{
		RGB rgb = null;
		RefData refData = gmmlGex.data.get(parent.name);
		HashMap<Integer, Object> data = refData.getAvgSampleData();
		
		GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(canvas.colorSetIndex);
		
		int nr = cs.useSamples.size();
		for(int i = 0; i < nr; i++)
		{
			// Get sub-rectangle
			Rectangle r = new Rectangle(colorArea.x + colorArea.width * i / nr,
					colorArea.y, colorArea.width / nr, colorArea.height);
			// Get the color
			c = new Color(e.display, cs.color_gene_not_found);
			rgb = cs.getColor(data, cs.useSamples.get(i).idSample);
			if(rgb != null)
			{
				c = new Color(e.display, rgb);
			}
			buffer.setBackground(c);
			
			// Visualize according column type
//			switch(cs.sampleTypes.get(i))
//			{
//			case GmmlColorSet.SAMPLE_TYPE_PROT:
//			case GmmlColorSet.SAMPLE_TYPE_TRANS:
//			case GmmlColorSet.SAMPLE_TYPE_PVALUE:
//			case GmmlColorSet.SAMPLE_TYPE_UNDEF:
//			r.grow(1,1);
			buffer.fillRectangle(r.x, r.y, r.width, r.height);
			buffer.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
			buffer.drawRectangle(r.x, r.y, r.width, r.height);
//			}
		}
		
		Rectangle r = parent.getBounds();
		
		if(refData.isAveraged())
		{
			org.eclipse.swt.graphics.Rectangle clip = buffer.getClipping();
			Region noClipping = null;
			buffer.setClipping(noClipping);
			
			buffer.setForeground(new Color(e.display, new RGB(255, 0, 0)));
			int oldLineWidth = buffer.getLineWidth();
			buffer.setLineWidth(2);
			buffer.drawRectangle(r.x + 1, r.y + 1, r.width - 1, r.height - 1);
			buffer.setLineWidth(oldLineWidth);
			
			buffer.setClipping(clip);
		}
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	

}
