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
package org.pathvisio.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.preferences.GlobalPreference;

public abstract class VPathwayElement implements Comparable<VPathwayElement>
{	
	protected BasicStroke defaultStroke = new BasicStroke();
	
	protected VPathway canvas;
	
	VPathwayElement(VPathway canvas)
	{
		this.canvas = canvas;
		canvas.addObject(this);
	}
	
	public static Color selectColor = GlobalPreference.getValueColor(GlobalPreference.COLOR_SELECTED);
	public static final float HIGHLIGHT_STROKE_WIDTH = 5.0f;

	private Rectangle oldrect = null;
	
	private boolean isSelected;
		
	public final void draw(Graphics2D g2d)
	{
		//Create a copy to ensure that the state of this Graphics2D will be intact
		//see: http://java.sun.com/docs/books/tutorial/uiswing/painting/concepts2.html
		
		Graphics2D g = (Graphics2D)g2d.create();
				
		//Prevent element from drawing outside its bounds
		g.clip(getVBounds());
		g.setStroke(defaultStroke);
		//Perform the drawing
		doDraw(g);
		
		//Free resources from the copied Graphics2D
		g.dispose();
	}
		
	protected abstract void doDraw(Graphics2D g2d);
	
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
	public VPathway getDrawing()
	{
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
			highlightColor = null;
			markDirty();
		}
	}

	private Color highlightColor;
	public Color getHighlightColor()
	{
		return highlightColor;
	}
	
	private boolean isHighlighted;

	/**
	 Besides setting isHighlighted, this accomplishes this:
	 - marking the area dirty, so the object has a chance to redraw itself in highlighted state

	 @param c this will highlight in a particular color. See
	 highlight() without parameter if you just want to highlight with
	 the default color
	 */
	public void highlight(Color c)
	{
		if(!(isHighlighted && highlightColor == c))
		{
			isHighlighted = true;
			highlightColor = c;
			markDirty();
		}
	}

	/**
	   highlight this element with the default highlight color
	 */
	public void highlight()
	{
		highlight (GlobalPreference.getValueColor(GlobalPreference.COLOR_HIGHLIGHTED));
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
	protected boolean vIntersects(Rectangle2D r)
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
	 * passed on by the given handle
	 * @param h	The Handle to adjust to
	 */
	protected void adjustToHandle(Handle h, double vx, double vy) {}

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
	protected void vMoveBy(double vdx, double vdy)
	{
	}
	
	/**
	 * Get the rectangular boundary of this object
	 */
	public Rectangle getVBounds()
	{
		return getVOutline().getBounds();
	}
	
	public double getVLeftRot(){
		return this.getVBounds().getMinX();
	}
	
	public void setVLeft(){
		System.out.println("This is in VPathwayElement");
	}
	
	public double getVTopRot(){
		return this.getVBounds().getMinY();
	}
	
	public double getVWidthRot(){
		return (this.getVBounds().getMaxX() - this.getVBounds().getMinX());
	}
	
	public double getVHeightRot(){
		return (this.getVBounds().getMaxY() - this.getVBounds().getMinY());
	}
	
	/**
	 * Get the outline of this element. The outline is used to check 
	 * whether a point is contained in this element or not
	 * @return the outline of this element
	 */
	abstract protected Shape getVOutline();

	/**
	 * Scales the object to the given rectangle
	 * @param r
	 */
	protected void setVScaleRectangle(Rectangle2D r) { }
	
	/**
	 * Gets the rectangle used to scale the object
	 */
	protected Rectangle2D getVScaleRectangle() { return new Rectangle2D.Double(); }

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
		
		int a, b, an, bn, az, bz;
		a = getDrawingOrder();
		b = d.getDrawingOrder();
		//Natural order in first bits
		an = a & 0xFF00;
		bn = b & 0xFF00;
		//z-order in last bits
		az = a & 0x00FF;
		bz = b & 0x00FF;
		
		if(isSelected() && d.isSelected()) {
			; //objects are both selected, keep original sort order
		}
		else if(isSelected() || isHighlighted())
		{
			an = VPathway.DRAW_ORDER_SELECTED;
		}
		else if(d.isSelected() || d.isHighlighted())
		{
			bn = VPathway.DRAW_ORDER_SELECTED;
		}
		
		// note, if the drawing order is equal, that doesn't mean the objects are equal
		// the construct with hashcodes give objects a defined sort order, even if their
		// drawing orders are equal.
		// The z-ordering takes care of the order of objects of the same type
		// note that the when the z-order is higher, the object is drawn later and should be
		// sorted below. Therefore the bz and az are switched.
		if (an == bn)
		{
			an = bz;
			bn = az;		
		}
		// there is still a remote possibility that although the objects are not the same,
		// the hashcode is the same. Even still, we shouldn't return 0.
		if (an != bn) 
			return bn - an; 
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
