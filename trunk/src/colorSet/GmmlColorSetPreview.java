package colorSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

public class GmmlColorSetPreview extends Canvas implements PaintListener {
	Vector colorSetObjects;
	Vector dataColumns;
	
	public GmmlColorSetPreview(Composite parent, int style)
	{
		super(parent, style);
		addPaintListener(this);	
		colorSetObjects = new Vector();
	}
	
	public void setColorSetObjects(Vector colorSetObjects) 
	{
		this.colorSetObjects = colorSetObjects;
	}
	
	public void setColorSetObjects(GmmlColorSetObject colorSetObject)
	{
		colorSetObjects = new Vector();
		colorSetObjects.add(colorSetObject);
	}
	
	Point size;
	public void paintControl (PaintEvent e)
	{	
		setExtremeValues();

		size = getSize();

		ListIterator it = colorSetObjects.listIterator(colorSetObjects.size());
		while(it.hasPrevious())
		{
			GmmlColorSetObject cs = (GmmlColorSetObject)it.previous();
			if(cs instanceof GmmlColorGradient) {
				GmmlColorGradient cg = (GmmlColorGradient)cs;
				drawColorGradient(e, cg);
			}
		}
	}
	
	final static int LABEL_HEIGHT = 20;
	final static int SIDE_WIDTH = 30;
	public void drawColorGradient(PaintEvent e, GmmlColorGradient cg)
	{
		System.out.println("drawing " + cg);
		Color c = getBackground();
		RGB oldBackground = c.getRGB();
		
		double min = extremes[0];
		double max = extremes[1];
		
		if((float)max == (float)min) {
			return;
		}
		
		// Get region to draw
		int barHeight = size.y - LABEL_HEIGHT;
		int start = (int)(((cg.valueStart - min) / (max - min)) * size.x);
		int end = (int)(((cg.valueEnd - min) / (max - min)) * size.x);

		if(cg.valueStart == (float)min) {
			start = SIDE_WIDTH;
		}
		if(cg.valueEnd == (float)max) {
			end = end - SIDE_WIDTH;
		}
		
		int n = end - start;
		
		// Fill squares with color cg.getColor()
		for(int i = start; i < end; i++) {
			double colorValue = cg.valueStart + (i-start) * (cg.valueEnd - cg.valueStart) / n;
			RGB rgb = cg.getColor(colorValue);
			if(rgb != null) {
				c = new Color(getShell().getDisplay(), rgb);
				e.gc.setBackground(c);
				e.gc.fillRectangle(i, 0, 1, barHeight);
			}
		}
		e.gc.drawLine(start, barHeight - 5, start, barHeight + 5);
		e.gc.drawLine(end, barHeight - 5, end, barHeight + 5);
		e.gc.setBackground(new Color(getShell().getDisplay(), oldBackground));
		e.gc.drawString(Double.toString(cg.valueStart), start, size.y - (int)(LABEL_HEIGHT / 1.5));
		e.gc.drawString(Double.toString(cg.valueEnd), end, size.y - (int)(LABEL_HEIGHT / 1.5));
		
		c.dispose();
	}
	
	double[] extremes;
	private void setExtremeValues()
	{
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		
		Iterator it = colorSetObjects.iterator();
		while(it.hasNext())
		{
			GmmlColorSetObject cs = (GmmlColorSetObject)it.next();
			if(cs instanceof GmmlColorGradient)
			{
				GmmlColorGradient cg = (GmmlColorGradient)cs;
				max = Math.max(cg.valueEnd, max);
				min = Math.min(cg.valueStart, min);
			}
		}
		extremes =  new double[] { min, max };
	}
}
