// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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
package org.pathvisio.view;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;

public abstract class VPathwayElement implements Comparable<VPathwayElement>
{	
	protected VPathway canvas;
	
	VPathwayElement(VPathway canvas) {
		this.canvas = canvas;
		canvas.addObject(this);
	}
	
	private boolean isHighlighted;
	private Rectangle oldrect = null;
	
	private boolean isSelected;
		
	protected abstract void draw(PaintEvent e);
	
	/**
	 * Draws the VPathwayElement object on the VPathway
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
		Rectangle newrect = getVBounds();
		canvas.addDirtyRect(newrect);
		oldrect = newrect;
	}

	/**
	 * Get the drawing this object belongs to
	 */
	public VPathway getDrawing() {
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
	 * Determines whether a Graphics object intersects 
	 * the rectangle specified
	 * @param r - the rectangle to check
	 * @return True if the object intersects the rectangle, false otherwise
	 */
	protected boolean vIntersects(Rectangle2D.Double r)
	{
		return getVOutline().intersects(r);
	}
	
	/**
	 * Determines wheter a Graphics object contains
	 * the point specified
	 * @param point - the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected boolean vContains(Point2D point)
	{
		return getVOutline().contains(point);
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
	 * @param h	The Handle to adjust to
	 */
	protected void adjustToHandle(Handle h) {}

	/**
	 * Get all the handles belonging to this object
	 * @return an array of GmmlHandles, an empty array if the object
	 * has no handles
	 */
	protected Handle[] getHandles() { return new Handle[] {}; }
	
	/**
	 * Moves this object by specified increments
	 * @param dx - the value of x-increment
	 * @param dy - the value of y-increment
	 */
	// TODO: should really be mMoveBy, using model coords,
	// because implementations do a lot of conversions anyway
	// perhaps could even be partially implemented in PathwayElement 
	protected void vMoveBy(double dx, double dy) { }
	
	/**
	 * Get the rectangular boundary of this object
	 */
	protected final Rectangle getVBounds()
	{
		return getVOutline().getBounds();
	}
	
	abstract protected Shape getVOutline();


	/**
	 * Scales the object to the given rectangle
	 * @param r
	 */
	protected void setVScaleRectangle(Rectangle2D.Double r) { }
	
	/**
	 * Gets the rectangle used to scale the object
	 */
	protected Rectangle2D.Double getVScaleRectangle() { return new Rectangle2D.Double(); }

	public int getDrawingOrder() {
		return VPathway.DRAW_ORDER_DEFAULT;
	}
	
	/**
	 * Orders GmmlDrawingObjects by their drawingOrder.
	 * The comparison is consistent with "equals", i.e. it doesn't return 0 if
	 * the objects are different, even if their drawing order is the same.
	 * 
	 * @param d
	 * @see #getDrawingOrder()
	 */
	public int compareTo(VPathwayElement d)
	{
		// same object? easy...
		if (d == this)
			return 0;
		
		int az, bz;
		az = getDrawingOrder();
		bz = d.getDrawingOrder();
		
		if(isSelected() && d.isSelected()) {
			; //objects are both selected, keep original sort order
		}
		else if(isSelected() || isHighlighted())
		{
			az = VPathway.DRAW_ORDER_SELECTED;
		}
		else if(d.isSelected() || d.isHighlighted())
		{
			bz = VPathway.DRAW_ORDER_SELECTED;
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
	
	/** 
	 * helper method to convert view coordinates to model coordinates 
	 * */
	protected double mFromV(double v) { return canvas.mFromV(v); }

	/** 
	 * helper method to convert view coordinates to model coordinates 
	 * */
	protected double vFromM(double m) { return canvas.vFromM(m); } 
	
	protected void destroyHandles() {
		for(Handle h : getHandles()) {
			h.destroy();
		}
	}
	
	protected void destroy() { 
		//Remove from canvas
		canvas.getDrawingObjects().remove(this);
		destroyHandles();
	}

}
