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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

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
	
	protected void draw(PaintEvent e, GC buffer)
	{
		Color c = new Color(e.display, GmmlGeneProduct.INITIAL_FILL_COLOR);
		RGB rgb = null;
		System.out.println(canvas.editMode);
		if(canvas.colorSetIndex > -1 && !canvas.editMode)
		{
			RefData refData = gmmlGex.data.get(parent.name);
			HashMap<Integer, Object> data = refData.getAvgSampleData();
			
			GmmlColorSet cs = (GmmlColorSet)gmmlGex.colorSets.get(canvas.colorSetIndex);
			c = new Color(e.display, cs.color_gene_not_found);
			
			rgb = cs.getColor(data);
			if(rgb != null)
			{
				c = new Color(e.display, rgb);
			}
			buffer.setBackground(c);
			Rectangle r = parent.getBounds();
			buffer.fillRectangle(r.x, r.y, r.width, r.height);
			
			if(refData.isAveraged())
			{
				buffer.setForeground(new Color(e.display, new RGB(255, 0, 0)));
				int oldLineWidth = buffer.getLineWidth();
				buffer.setLineWidth(2);
				buffer.drawRectangle(r.x, r.y, r.width, r.height);
				buffer.setLineWidth(oldLineWidth);
			}
		} else {
			buffer.setBackground(c);
			Rectangle r = parent.getBounds();
			buffer.fillRectangle(r.x, r.y, r.width, r.height);
		}

		c.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	

}
