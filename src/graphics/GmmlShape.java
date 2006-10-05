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
		
	protected void draw(PaintEvent e, GC buffer)
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
				buffer.drawRectangle (
					startX,
					startY,
					width,
					height
				);
				break;
			case ShapeType.OVAL:
				
				buffer.setLineWidth (1);
				buffer.drawOval (
					startX, 
					startY,
					width, 
					height
				);
				break;
			case ShapeType.ARC:
				buffer.setLineWidth (1);
				buffer.drawArc(
						startX, 
						startY,
						width, 
						height,
					 0, 180
				);
				break;
		}

		buffer.setTransform(null);
		
		c.dispose();
		tr.dispose();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
}