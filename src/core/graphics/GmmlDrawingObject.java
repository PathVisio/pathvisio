// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;

public abstract class GmmlDrawingObject implements Comparable<GmmlDrawingObject>
{	
	protected GmmlDrawing canvas;
	
	GmmlDrawingObject(GmmlDrawing canvas) {
		this.canvas = canvas;
		canvas.addObject(this);
	}
	
	int drawingOrder = GmmlDrawing.DRAW_ORDER_DEFAULT;
	private boolean isHighlighted;
	private Rectangle oldrect = null;
	
	private boolean isSelected;
	
	/**
	 * Adjusts this object to the zoom
	 * specified in the drawing it is part of
	 * @param factor - the factor to scale the objects coordinates and measures with
	 */
	abstract void adjustToZoom(double factor);
	
	protected abstract void draw(PaintEvent e);
	
	/**
	 * Draws the GmmlDrawingObject object on the GmmlDrawing
	 * it is part of
	 */
	public abstract void draw(PaintEvent e, GC buffer);
	
	/** 
	 * mark both the area currently and previously occupied by this object for redraw 
	 */
	protected void markDirty()
	{
		if (oldrect != null)
		{
			canvas.addDirtyRect(oldrect);
		}
		Rectangle newrect = getBounds();
		canvas.addDirtyRect(newrect);
		oldrect = newrect;
	}

	/**
	 * Get the drawing this object belongs to
	 */
	public GmmlDrawing getDrawing() {
		return canvas;
	}
	
	/**
	 * Besides resetting isHighlighted, this accomplishes this:
	 * - marking the area dirty, so the object has a chance to redraw itself in unhighlighted state
	 */
	public void unhighlight()
	{
		if(isHighlighted)
		{
			isHighlighted = false;
			markDirty();
		}
	}

	/**
	 * Besides setting isHighlighted, this accomplishes this:
	 * - marking the area dirty, so the object has a chance to redraw itself in highlighted state
	 */
	public void highlight()
	{
		if(!isHighlighted)
		{
			isHighlighted = true;
			markDirty();
		}
	}
	
	/**
	 * Returns true if this object is highlighted, false otherwise
	 */
	public boolean isHighlighted()
	{
		return isHighlighted;
	}

	/**
	 * Determines whether a GmmlGraphics object intersects 
	 * the rectangle specified
	 * @param r - the rectangle to check
	 * @return True if the object intersects the rectangle, false otherwise
	 */
	protected final boolean intersects(Rectangle2D.Double r)
	{
		return getOutline().intersects(r);
	}
	
	/**
	 * Determines wheter a GmmlGraphics object contains
	 * the point specified
	 * @param point - the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected final boolean isContain(Point2D point)
	{
		return getOutline().contains(point);
	}	


	public boolean isSelected()
	{
		return isSelected;
	}
	
	/**
	 * Besides resetting isSelected, this accomplishes this:
	 * - marking the area dirty, so the object has a chance to redraw itself in unselected state
	 */
	public void deselect()
	{
		if (isSelected)
		{
			isSelected = false;
			markDirty();			
		}
	}

	/**
	 * Besides setting isSelected, this accomplishes this:
	 * - marking the area dirty, so the object has a chance to redraw itself in selected state
	 */
	public void select()
	{
		if (!isSelected)
		{
			isSelected = true;
			markDirty();			
		}
	}

	/**
	 * Transforms this object to fit to the coordinates
	 * of the given handle
	 * @param h	The GmmlHandle to adjust to
	 */
	protected void adjustToHandle(GmmlHandle h) {}

	/**
	 * Get all the handles belonging to this object
	 * @return an array of GmmlHandles, an empty array if the object
	 * has no handles
	 */
	protected GmmlHandle[] getHandles() { return new GmmlHandle[] {}; }
	
	/**
	 * Moves this object by specified increments
	 * @param dx - the value of x-increment
	 * @param dy - the value of y-increment
	 */
	protected void moveBy(double dx, double dy) { }
	
	/**
	 * Get the rectangular boundary of this object
	 */
	protected final Rectangle getBounds()
	{
		return getOutline().getBounds();
	}
	
	abstract protected Shape getOutline();


	/**
	 * Scales the object to the given rectangle
	 * @param r
	 */
	protected void setScaleRectangle(Rectangle2D.Double r) { }
	
	/**
	 * Gets the rectangle used to scale the object
	 */
	protected Rectangle2D.Double getScaleRectangle() { return new Rectangle2D.Double(); }

	/**
	 * Orders GmmlDrawingObjects by their drawingOrder.
	 * The comparison is consistent with "equals", i.e. it doesn't return 0 if
	 * the objects are different, even if their drawing order is the same.
	 * 
	 * @param d
	 */
	public int compareTo(GmmlDrawingObject d)
	{
		// same object? easy...
		if (d == this)
			return 0;
		
		int az, bz;
		az = drawingOrder;
		bz = d.drawingOrder;
		
		if(isSelected() && d.isSelected()) {
			; //objects are both selected, keep original sort order
		}
		else if(isSelected() || isHighlighted())
		{
			az = GmmlDrawing.DRAW_ORDER_SELECTED;
		}
		else if(d.isSelected() || d.isHighlighted())
		{
			bz = GmmlDrawing.DRAW_ORDER_SELECTED;
		}
		
		// note, if the drawing order is equal, that doesn't mean the objects are equal
		// the construct with hashcodes give objects a defined sort order, even if their
		// drawing orders are equal.		
		if (az == bz)
		{
			az = hashCode();
			bz = d.hashCode();		
		}
		// there is still a remote possibility that although the objects are not the same,
		// the hashcode is the same. Even still, we shouldn't return 0.
		if (az != bz) 
			return bz - az; 
		else
			return -1;
	}
}
