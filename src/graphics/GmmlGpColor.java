package graphics;


import gmmlVision.GmmlVision;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

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

import preferences.GmmlPreferences;

import util.SwtUtils;
import colorSet.GmmlColorSet;
import data.GmmlGdb;
import data.GmmlGex;
import data.GmmlGex.CachedData.Data;

public class GmmlGpColor {
	public static RGB color_ambigious = GmmlPreferences.getColorProperty("colors.ambigious_reporter");
	
	GmmlGeneProduct parent;
	
	public ArrayList ensIds;
	public ArrayList refIds;
	
	Vector sampleData;
	private GmmlDrawing canvas;
	
	public GmmlGpColor(GmmlGeneProduct parent)
	{
		this.parent = parent;
		canvas = parent.canvas;
	}
	
	public static final double COLOR_AREA_RATIO = 0.5;
//	public static final int MAX_COLOR_SIZE = 20;
	Color c;
	Font f;
	PaintEvent e;
	GC buffer;
	
	private void drawLabel(Rectangle colorArea)
	{
		Point textSize = null;
		Rectangle r = parent.getBounds();
		r.width -= colorArea.width;

		int fontSize = (int)(parent.fontSize * (double)r.width / parent.getBounds().width);
//		 TODO: find optimal fontsize
		fontSize += 2;
		f = SwtUtils.changeFont(f, new FontData("Arial narrow", fontSize, SWT.NONE), e.display);
		buffer.setFont(f);
		textSize = buffer.textExtent (parent.getName());
		c = SwtUtils.changeColor(c, parent.gdata.getColor(), e.display);
		buffer.setForeground(c);
		buffer.drawString (parent.getName(), 
			r.x + (int)(r.width / 2) - (int)(textSize.x / 2),
			r.y + (int)(r.height / 2) - (int)(textSize.y / 2), true);
		
	}
	
	private void colorAsNotFound(Rectangle colorArea, RGB rgb)
	{
		c = SwtUtils.changeColor(c, rgb, e.display);
		
		buffer.setBackground(c);
		buffer.fillRectangle(colorArea.x, colorArea.y, colorArea.width, colorArea.height);
	}
	
	private void colorByData(Rectangle colorArea)
	{
		Data mappIdData = GmmlGex.getCachedData(parent.getID(), parent.getSystemCode());
		
		GmmlColorSet cs = (GmmlColorSet)GmmlGex.getColorSets().get(GmmlGex.getColorSetIndex());
		
		int nr = cs.useSamples.size();
		int width = nr > 0 ? colorArea.width / nr : colorArea.width;
		for(int i = 0; i < nr; i++)
		{
			int idSample = cs.useSamples.get(i).idSample; // The sample to visualize
			
			// Get sub-rectangle
			int x = colorArea.x + width * i;
			Rectangle r = new Rectangle(x,
					colorArea.y, width, colorArea.height);
						
			// Visualize according sample type
			int sampleType = cs.sampleTypes.get(i);
			switch(sampleType)
			{
			case GmmlColorSet.SAMPLE_TYPE_PROT:
			case GmmlColorSet.SAMPLE_TYPE_TRANS:
				setBackgroundColor(cs, mappIdData.getAverageSampleData(), idSample);
				drawAsImage(sampleType, r);
				break;
			case GmmlColorSet.SAMPLE_TYPE_UNDEF:
				switch(cs.getMultipleDataDisplay()) {
				case GmmlColorSet.MULT_DATA_AVG:
					setBackgroundColor(cs, mappIdData.getAverageSampleData(), idSample);
					buffer.fillRectangle(r.x, r.y, r.width, r.height);
					buffer.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
					buffer.drawRectangle(r.x, r.y, r.width, r.height);
					break;
				case GmmlColorSet.MULT_DATA_DIV:
					drawAsHorizontalBars(cs, mappIdData, idSample, r);
				}
			}
		}
		
		Rectangle r = parent.getBounds();
		
		//Mark ambigious reporters
		if(mappIdData.hasMultipleData())
		{
			org.eclipse.swt.graphics.Rectangle clip = buffer.getClipping();
			Region noClipping = null;
			buffer.setClipping(noClipping);
			
			Color c = new Color(e.display, color_ambigious);
			buffer.setForeground(c);
			int oldLineWidth = buffer.getLineWidth();
			buffer.setLineWidth(1);
			buffer.drawRectangle(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
			buffer.setLineWidth(oldLineWidth);
			
			buffer.setClipping(clip);
			
			c.dispose();
		}
	}
	
	private void setBackgroundColor(GmmlColorSet cs, HashMap<Integer, Object> data, int idSample)
	{
		c = SwtUtils.changeColor(c, cs.color_no_data_found, e.display);
		RGB rgb = null;
		if((rgb = cs.getColor(data, idSample)) != null)
			c = SwtUtils.changeColor(c, rgb, e.display);
		buffer.setBackground(c);
	}
	
	private void drawAsHorizontalBars(GmmlColorSet cs, Data mappIdData, 
			int idSample, Rectangle r)
	{
		//TODO: divide in horizontal bars and color
	}
	
	private void drawAsImage(int sampleType, Rectangle r)
	{
		Image image = null;
		switch(sampleType)
		{
		case GmmlColorSet.SAMPLE_TYPE_PROT:
			image = GmmlVision.getImageRegistry().get("data.protein"); break;
		case GmmlColorSet.SAMPLE_TYPE_TRANS:
			image = GmmlVision.getImageRegistry().get("data.mRNA"); break;
		}
		
		if(image != null)
		{
			ImageData imgData = image.getImageData();
			int imageHeight = r.width;
			
			buffer.fillRectangle(r.x, r.y + r.height / 2 - imageHeight / 2, r.width, imageHeight);
			
			buffer.drawImage(image, 0, 0, imgData.width, imgData.height, 
					r.x, r.y + r.height / 2 - imageHeight / 2, r.width, imageHeight);
		} else {
			buffer.fillRectangle(r.x, r.y, r.width, r.height);
		}
	}

	protected void draw(PaintEvent e, GC buffer)
	{
		int colorSetIndex = GmmlGex.getColorSetIndex();
		c = new Color(e.display, GmmlGeneProduct.INITIAL_FILL_COLOR);
		f = new Font(e.display, "ARIAL", parent.fontSize, SWT.NONE);
		this.e = e;
		this.buffer = buffer;
		
		Rectangle r = parent.getBounds();
		buffer.setBackground(c);
		buffer.fillRectangle(r.x, r.y, r.width, r.height);
		if(GmmlGex.isConnected() && colorSetIndex > -1 && 
				!canvas.isEditMode() && GmmlGex.getColorSets().size() > 0)
		{
			// Get visualization area
			Rectangle colorArea = parent.getBounds();
			
			// Adjust width to enable to divide into nrSamples equal rectangles
			GmmlColorSet cs = (GmmlColorSet)GmmlGex.getColorSets().get(colorSetIndex);
			colorArea.width = (int)(COLOR_AREA_RATIO * colorArea.width);
			if(cs.useSamples.size() > 0) {
				colorArea.width += cs.useSamples.size() - colorArea.width % cs.useSamples.size();
			}
			// Get x position
			colorArea.x = colorArea.x + (parent.getBounds().width - colorArea.width);
			
			if(GmmlGex.hasData(parent.getID(), parent.getSystemCode())) //Check if data is available
			{
				colorByData(colorArea);
			}
			else if(GmmlGdb.hasGene(parent.getID(), parent.getSystemCode())) //Check if gene exists in gdb
			{		
				colorAsNotFound(colorArea, cs.color_no_data_found);
			}
			else {
				colorAsNotFound(colorArea, cs.color_no_gene_found);
			}
			drawLabel(colorArea);
			
		} else {			
			buffer.setFont (f);
			String label = parent.gdata.getGeneID();
			Point textSize = buffer.textExtent (label);
			c = SwtUtils.changeColor(c, parent.gdata.getColor(), e.display);
			buffer.setForeground(c);
			int x = (int) parent.getCenterX() - (textSize.x / 2);
			int y = (int) parent.getCenterY() - (textSize.y / 2);
//			RGB rgb = c.getRGB();
			buffer.drawString (label, x , y, true);
		}
		
		c.dispose();
		f.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	

}
