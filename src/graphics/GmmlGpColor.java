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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Region;

import util.SwtUtils;

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
	GmmlGeneProduct parent;
	
	// Cache some data on loading mapp
	Hashtable dataHash;
		// {refId : ArrayList data}
	public ArrayList ensIds;
	public ArrayList refIds;
	
	Vector sampleData;
	private GmmlDrawing canvas;
	
	public GmmlGpColor(GmmlGeneProduct parent)
	{
		this.parent = parent;
		canvas = parent.canvas;
		gmmlGdb = canvas.gmmlVision.gmmlGdb;
		gmmlGex = canvas.gmmlVision.gmmlGex;
	}
	
	public static final double COLOR_AREA_RATIO = 0.5;
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
			colorArea.width = (int)Math.ceil(COLOR_AREA_RATIO * colorArea.width);
			// Adjust width to enable to divide into nrSamples equal rectangles
			GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(canvas.colorSetIndex);
			colorArea.width += cs.useSamples.size() - colorArea.width % cs.useSamples.size();
			// Get x position
			colorArea.x = colorArea.x + (parent.getBounds().width - colorArea.width);
			
			if(gmmlGex.data.containsKey(parent.name))
			{
				colorByData(e, buffer, c, colorArea);
			}
			else
			{				
				colorByGeneNotFound(e, buffer, c, colorArea);
			}
			drawLabel(e, buffer, c, f, colorArea);
			
		} else {			
			buffer.setFont (f);
			Point textSize = buffer.textExtent (parent.geneID);
			
			c = SwtUtils.changeColor(c, parent.color, e.display);
			buffer.setForeground(c);
			buffer.drawString (parent.geneID, 
				(int) parent.centerx - (textSize.x / 2) , 
				(int) parent.centery - (textSize.y / 2), true);
		}
		
		c.dispose();
		f.dispose();
	}
	
	private void drawLabel(PaintEvent e, GC buffer, Color c, Font f, Rectangle colorArea)
	{
		Point textSize = null;
		Rectangle r = parent.getBounds();
		r.width -= colorArea.width;

		int fontSize = (int)(parent.fontSize * (double)r.width / parent.getBounds().width);
//		 TODO: find optimal fontsize
		fontSize += 2;
		f = new Font(e.display, "Arial narrow", fontSize, SWT.NONE);
		buffer.setFont(f);
		textSize = buffer.textExtent (parent.geneID);

		c = SwtUtils.changeColor(c, parent.color, e.display);
		buffer.setForeground(c);
		buffer.drawString (parent.geneID, 
			r.x + (int)(r.width / 2) - (int)(textSize.x / 2),
			r.y + (int)(r.height / 2) - (int)(textSize.y / 2), true);
	}
	
	private void colorByGeneNotFound(PaintEvent e, GC buffer, Color c, Rectangle colorArea)
	{
		GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(canvas.colorSetIndex);
		
		c = SwtUtils.changeColor(c, cs.color_gene_not_found, e.display);
		
		buffer.setBackground(c);
		buffer.fillRectangle(colorArea.x, colorArea.y, colorArea.width, colorArea.height);
	}
	
	public static int SAMPLE_TYPE_UNDEF = 0;
	public static int SAMPLE_TYPE_TRANS = 1;
	public static int SAMPLE_TYPE_PROT	= 2;
	public static int SAMPLE_TYPE_PVALUE= 3;
	private void colorByData(PaintEvent e, GC buffer, Color c, Rectangle colorArea)
	{
		RGB rgb = null;
		RefData refData = gmmlGex.data.get(parent.name);
		HashMap<Integer, Object> data = refData.getAvgSampleData();
		
		GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(canvas.colorSetIndex);
		
		int nr = cs.useSamples.size();
		int width = colorArea.width / nr;
		for(int i = 0; i < nr; i++)
		{
			// Get sub-rectangle
			int x = colorArea.x + width * i;
			Rectangle r = new Rectangle(x,
					colorArea.y, width, colorArea.height);
			// Get the color
			c = new Color(e.display, cs.color_gene_not_found);
			rgb = cs.getColor(data, cs.useSamples.get(i).idSample);
			if(rgb != null)
			{
				c = new Color(e.display, rgb);
			}
			buffer.setBackground(c);
			
			// Visualize according column type
			
			switch(cs.sampleTypes.get(i))
			{
			case GmmlColorSet.SAMPLE_TYPE_PROT:
				Image image = parent.canvas.gmmlVision.imageRegistry.get("data.protein");
				drawDataTypeImage(e, buffer, image, r);
				break;
			case GmmlColorSet.SAMPLE_TYPE_TRANS:
				image = parent.canvas.gmmlVision.imageRegistry.get("data.mRNA");
				drawDataTypeImage(e, buffer, image, r);
				break;
			case GmmlColorSet.SAMPLE_TYPE_PVALUE:
			case GmmlColorSet.SAMPLE_TYPE_UNDEF:
				buffer.fillRectangle(r.x, r.y, r.width, r.height);
				buffer.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				buffer.drawRectangle(r.x, r.y, r.width, r.height);
			}
		}
		
		Rectangle r = parent.getBounds();
		
		if(refData.isAveraged())
		{
			org.eclipse.swt.graphics.Rectangle clip = buffer.getClipping();
			Region noClipping = null;
			buffer.setClipping(noClipping);
			
			buffer.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
			int oldLineWidth = buffer.getLineWidth();
			buffer.setLineWidth(2);
			buffer.drawRectangle(r.x + 1, r.y + 1, r.width - 1, r.height - 1);
			buffer.setLineWidth(oldLineWidth);
			
			buffer.setClipping(clip);
		}
	}
	
	protected void drawDataTypeImage(PaintEvent e, GC buffer, Image image, Rectangle r)
	{
		buffer.fillRectangle(r.x, r.y, r.width, r.height);
		if(image != null)
		{
			ImageData imgData = image.getImageData();
			
			buffer.drawImage(image, 0, 0, imgData.width, imgData.height, 
					r.x, r.y, r.width, r.height);
		}
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	

}
