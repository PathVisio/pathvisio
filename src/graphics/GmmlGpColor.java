package graphics;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import colorSet.*;
import data.GmmlGex.ConvertThread;

public class GmmlGpColor {
	// Pre-cached data / ensIds
	//	-> reload on loading new gex
	// Parent
	//	-> GmmlGeneProduct
	// 
	
	GmmlDrawing canvas;
	GmmlGeneProduct parent;
	
	// Cach some data on loading mapp
	HashMap data;
	ArrayList ensIds;
	ArrayList refIds;
	
	public GmmlGpColor(GmmlGeneProduct parent)
	{
		this.parent = parent;
		canvas = parent.canvas;
		
		
	}
	
	public void setCache()
	{
		refIds = new ArrayList();
		ensIds = canvas.gmmlVision.gmmlGdb.ref2EnsIds(parent.name);
		Iterator it = ensIds.iterator();
		while(it.hasNext())
		{
			ArrayList refs = canvas.gmmlVision.gmmlGdb.ensId2Refs((String)it.next());
			if(refs != null)
			{
				refIds.addAll(refs);
			}
		}
		
		data = canvas.gmmlVision.gmmlGex.getDataHash(refIds);
		
	}
		
	protected void draw(PaintEvent e)
	{
		Color cFill = new Color(e.display, GmmlGeneProduct.INITIAL_FILL_COLOR);
		RGB rgb = null;
		if(canvas.colorSetIndex > -1)
		{
			GmmlColorSet cs = (GmmlColorSet)canvas.gmmlVision.gmmlGex.colorSets.get(canvas.colorSetIndex);
			rgb = cs.getColor(data);
			if(rgb != null)
			{
//				System.out.println("Drawing in color: " + rgb);
				cFill = new Color(e.display, rgb);
			}
		}
		
		e.gc.setBackground(cFill);
		Rectangle r = parent.getBounds();
		e.gc.fillRectangle(r.x, r.y, r.width, r.height);
	}
}
