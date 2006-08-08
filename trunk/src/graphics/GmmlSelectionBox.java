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
	
//	Side handles
	GmmlHandle handleN;
	GmmlHandle handleE;
	GmmlHandle handleS;
	GmmlHandle handleW;
	//Corner handles
	GmmlHandle handleNE;
	GmmlHandle handleSE;
	GmmlHandle handleSW;
	GmmlHandle handleNW;
	
	/**
	 * Constructor for this class
	 * @param canvas - the GmmlDrawing this selectionbox will be part of
	 */
	public GmmlSelectionBox(GmmlDrawing canvas)
	{
		super(canvas);
		drawingOrder = GmmlDrawing.DRAW_ORDER_SELECTIONBOX;

		handleN	= new GmmlHandle(GmmlHandle.DIRECTION_Y, this, canvas);
		handleE	= new GmmlHandle(GmmlHandle.DIRECTION_X, this, canvas);
		handleS	= new GmmlHandle(GmmlHandle.DIRECTION_Y, this, canvas);
		handleW	= new GmmlHandle(GmmlHandle.DIRECTION_X, this, canvas);
		
		handleNE	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
		handleSE	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
		handleSW	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
		handleNW	= new GmmlHandle(GmmlHandle.DIRECTION_XY, this, canvas);
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
	
	public GmmlHandle[] getHandles()
	{
		return new GmmlHandle[] {
				handleN, handleNE, handleE, handleSE,
				handleS, handleSW, handleW,	handleNW,
		};
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
		for (GmmlHandle h : getHandles())
		{
			h.show();
		}
	}
	
	public void deselect()
	{
		super.deselect();
		for (GmmlHandle h : getHandles())
		{
			h.hide();
		}
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
	
	public void adjustToHandle(GmmlHandle h) {
		double dx = 0;
		double dy = 0;
		
		if	(h == handleN || h == handleNE || h == handleNW) {
			dy = h.centery - y1;
		}
		if	(h == handleS || h == handleSE || h == handleSW ) {
			dy = h.centery - y2;
		}
		if	(h == handleE || h == handleNE || h == handleSE) {
			dx = h.centerx - x1;
		}
		if	(h == handleW || h == handleNW || h== handleSW) {
			dx = h.centerx - x2;
		}
		resizeX(dx);
		resizeY(dy);
		
		setHandleLocation(h);
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
					g.moveBy(dx / 2, 0); 
					g.resizeX(dx / 2); 
				} else break; //Sorted, so all selected GmmlGraphics at end
			}
		}
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
					g.moveBy(0, dy / 2); 
					g.resizeY(dy / 2); 
				} else break; //Sorted, so all selected GmmlGraphics at end
			}
		}
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
	 * Sets the handles at the correct location;
	 * left border.
	 */
	private void setHandleLocation()
	{
		setHandleLocation(null);
	}
	
	/**
	 * Sets the handles at the correct location;
	 * @param ignore the position of this handle will not be adjusted
	 */
	private void setHandleLocation(GmmlHandle ignore)
	{
		Rectangle2D.Double r = getRectangle();
		
		if(ignore != handleN) handleN.setLocation(r.x + r.width/2, r.y);
		if(ignore != handleE) handleE.setLocation(r.x + r.width, r.y + r.height/2);
		if(ignore != handleS) handleS.setLocation(r.x + r.width/2, r.y + r.height);
		if(ignore != handleW) handleW.setLocation(r.x, r.y + r.width/2);
		
		if(ignore != handleNE) handleNE.setLocation(r.x + r.width, r.y);
		if(ignore != handleSE) handleSE.setLocation(r.x + r.width, r.y + r.height);
		if(ignore != handleSW) handleSW.setLocation(r.x, r.y + r.height);
		if(ignore != handleNW) handleNW.setLocation(r.x, r.y);
	}
}