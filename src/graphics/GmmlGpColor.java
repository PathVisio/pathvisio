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
	
	protected void draw(PaintEvent e)
	{
		Color c = new Color(e.display, GmmlGeneProduct.INITIAL_FILL_COLOR);
		RGB rgb = null;
			
		if(canvas.colorSetIndex > -1)
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
			e.gc.setBackground(c);
			Rectangle r = parent.getBounds();
			e.gc.fillRectangle(r.x, r.y, r.width, r.height);
			
			if(refData.isAveraged())
			{
				e.gc.setForeground(new Color(e.display, new RGB(255, 0, 0)));
				int oldLineWidth = e.gc.getLineWidth();
				e.gc.setLineWidth(2);
				e.gc.drawRectangle(r.x, r.y, r.width, r.height);
				e.gc.setLineWidth(oldLineWidth);
			}
		} else {
			e.gc.setBackground(c);
			Rectangle r = parent.getBounds();
			e.gc.fillRectangle(r.x, r.y, r.width, r.height);
		}

		c.dispose();
	}
	

}
