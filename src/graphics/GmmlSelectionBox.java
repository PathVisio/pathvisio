package graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;

/**
 * This class implements a selectionbox 
 */ 
class GmmlSelectionBox extends GmmlDrawingObject
{
	private static final long serialVersionUID = 1L;
	
	int x1, y1, x2, y2;
	
	GmmlHandle handlex;
	GmmlHandle handley;
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this selectionbox will be part of
	 */
	public GmmlSelectionBox(GmmlDrawing canvas)
	{
		drawingOrder = GmmlDrawing.DRAW_ORDER_SELECTIONBOX;
		
		this.canvas = canvas;
		canvas.addElement(this);
		
		handlex		= new GmmlHandle(GmmlHandle.HANDLETYPE_WIDTH, this, canvas);
		handley		= new GmmlHandle(GmmlHandle.HANDLETYPE_HEIGHT, this, canvas);
		
		canvas.addElement(handlex);
		canvas.addElement(handley);
		
		resetRectangle();
	}	
	
	/**
	 * resets the selectionbox rectangle position to the upper 
	 * left corner of the screen
	 */
	public void resetRectangle()
	{
		x1 = 0;
		y1 = 0;
		x2 = 0;
		y2 = 0;
	}
	
	public void setCorner(int x, int y) {
		markDirty();
		x2 = x;
		y2 = y;
		setHandleLocation();
		markDirty();
	}
	
	public void setRectangle(Rectangle r) {
		markDirty();
		x1 = r.x;
		x2 = r.x + r.width;
		y1 = r.y;
		y2 = r.y + r.height;
		setHandleLocation();
		markDirty();
	}
	
	public Rectangle2D.Double getRectangle() {
		double width = x2-x1;
		double height = y2-y1;
		double x = x1;
		double y = y1;
		
		if(width < 0)
		{
			width = -width;
			x = x - width;
		}
		if(height < 0)
		{
			height = -height;
			y = y - height;
		}
		return new Rectangle2D.Double(x,y,width,height);
	}
	
	/*
	 * (non-Javadoc)
	 * @see GmmlGraphics#draw(java.awt.Graphics)
	 */
	protected void draw(PaintEvent e, GC buffer)
	{
		if(canvas.isSelecting || isSelected())
		{
			buffer.setForeground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.setBackground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.setLineStyle (SWT.LINE_DOT);
			buffer.setLineWidth (1);
			buffer.drawRectangle (x1, y1, x2-x1, y2-y1);
		}
	}
	
	public void select()
	{
		super.select();
		hideCanvasHandles();
		fitToSelection();
		handlex.show();
		handley.show();
	}
	
	public void deselect()
	{
		super.deselect();
		handlex.hide();
		handley.hide();
	}
	
	private void hideCanvasHandles() {
		ArrayList<GmmlDrawingObject> drw = canvas.getDrawingObjects();
		Collections.sort(drw);
		for(int i = drw.size(); i > 0; i--) {
			GmmlDrawingObject o = drw.get(i - 1);
			if(o instanceof GmmlGraphics) {
				if(o.isSelected())
					for(GmmlHandle h : ((GmmlGraphics)o).getHandles()) h.hide();
				else break; //Sorted, so all selected GmmlGraphics at end
			}
		}
	}
	
	public void fitToSelection() {
		Rectangle r = null;
		for(GmmlDrawingObject o : canvas.getDrawingObjects()) {
			if(o.isSelected() && !(o instanceof GmmlSelectionBox)) {
				if(r == null) r = (Rectangle)o.getBounds().clone();
				else r.add(o.getBounds());
			}
		}
		if(r != null) setRectangle(r);
	}
	
	public boolean hasMultipleSelection() {
		int ns = 0;
		ArrayList<GmmlDrawingObject> drw = canvas.getDrawingObjects();
		Collections.sort(drw);
		for(int i = drw.size(); i > 0; i--) {
			GmmlDrawingObject o = drw.get(i - 1);
			if(o instanceof GmmlGraphics) {
				if(o.isSelected()) ns++;
				else break; //Sorted, so all selected GmmlGraphics at end
			}
			if(ns > 1) return true;
		}
		return false;
	}
	
	protected void resizeX(double dx) {
		markDirty();
		x2 += dx;
		ArrayList<GmmlDrawingObject> drw = canvas.getDrawingObjects();
		Collections.sort(drw);
		for(int i = drw.size(); i > 0; i--) {
			GmmlDrawingObject o = drw.get(i - 1);
			if(o instanceof GmmlGraphics) {
				if(o.isSelected()) {
					GmmlGraphics g = (GmmlGraphics)o;
					g.moveLineStart(dx / 2, 0);
					g.moveBy(dx / 2, 0); 
					g.resizeX(dx / 2); 
				} else break; //Sorted, so all selected GmmlGraphics at end
			}
		}
		setHandleLocation();
		markDirty();
	}
	
	protected void resizeY(double dy) {
		markDirty();
		y1 += dy;
		ArrayList<GmmlDrawingObject> drw = canvas.getDrawingObjects();
		Collections.sort(drw);
		for(int i = drw.size(); i > 0; i--) {
			GmmlDrawingObject o = drw.get(i - 1);
			if(o instanceof GmmlGraphics) {
				if(o.isSelected()) {
					GmmlGraphics g = (GmmlGraphics)o;
					g.moveLineStart(0, dy / 2);
					g.moveBy(0, dy / 2); 
					g.resizeY(dy / 2); 
				} else break; //Sorted, so all selected GmmlGraphics at end
			}
		}
		setHandleLocation();
		markDirty();
	}
	
	protected void moveBy(double dx, double dy) {
		markDirty();
		x1 += dx; x2 += dx;
		y1 += dy; y2 += dy;
		setHandleLocation();
		markDirty();
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
	
	protected boolean intersects(Rectangle2D.Double r)
	{	
		ArrayList<GmmlDrawingObject> drw = canvas.getDrawingObjects();
		Collections.sort(drw);
		for(int i = drw.size(); i > 0; i--) {
			GmmlDrawingObject o = drw.get(i - 1);
			if(o instanceof GmmlGraphics) {
				if(o.isSelected()) {
					if(o.intersects(r)) return true;
				} else break; //Sorted, so all selected GmmlGraphics at end
			}
		}
		return false;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see GmmlDrawingObject#isContain(java.awt.geom.Point2D)
	 */
	protected boolean isContain(Point2D point)
	{
		return false;
	}
	
	protected Rectangle getBounds()
	{
		return getRectangle().getBounds();
	}
	
	/**
	 * Sets this class's handles at the correct location
	 */
	private void setHandleLocation()
	{
		Rectangle2D.Double r = getRectangle();
		handley.setLocation(r.x + r.width / 2, r.y);
		handlex.setLocation(r.x + r.width, r.y + r.height / 2);
	}
	
//	protected ArrayList getSideAreas()
//	{
//	int w = 4;
//	ArrayList rl = new ArrayList();
//	Rectangle r = getRectangle().getBounds();
//	rl.add(new Rectangle(r.x - w/2, r.y - w/2, r.width + w, w));
//	rl.add(new Rectangle(r.x + r.width - w/2, r.y - w/2, w, r.height + w/2));
//	rl.add(new Rectangle(r.x - w/2, r.y + r.height - w/2, r.width + w, w));
//	rl.add(new Rectangle(r.x - w/2, r.y + w/2, w, r.height + w));
//	return rl;
//	} 
} // end of class