// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;

/**
 * A single graphical element in the view of a pathway. Handles bounds checking and dirty rectangles.
 * NB: this does not necessarily correspond directly with a org.pathvisio.model.PathwayElement.
 *
 * VPathwayElement includes purely visual helpers that do not have direct correspondence in the model,
 * such as handles and a selection box.
 * TODO naming - better named VElement, because there is no connection to PathwayElement.
 */
public abstract class VPathwayElement implements Comparable<VPathwayElement>
{
	protected static final BasicStroke DEFAULT_STROKE = new BasicStroke();

	protected VPathway canvas;

	VPathwayElement(VPathway canvas)
	{
		this.canvas = canvas;
		canvas.addObject(this);
	}

	public static Color selectColor = PreferenceManager.getCurrent().getColor(GlobalPreference.COLOR_SELECTED);
	public static final float HIGHLIGHT_STROKE_WIDTH = 5.0f;

	private Rectangle2D oldrect = null;

	private boolean isSelected;

	/**
	 * Will be called by VPathway whenever the zoom factor
	 * is changed. Default implementation refreshes the cache
	 * of VBounds and VOutline.
	 */
	void zoomChanged() {
		resetShapeCache();
	}

	private Shape vOutlineCache;
	private Rectangle2D vBoundsCache;

	/**
	 * Resets cache for VOutline and VBounds so that
	 * they will be recalculated on the next call to
	 * getVOutline or getVBounds.
	 */
	protected void resetShapeCache() {
		vOutlineCache = null;
		vBoundsCache = null;
	}

	public final void draw(Graphics2D g2d)
	{
		//Create a copy to ensure that the state of this Graphics2D will be intact
		//see: http://java.sun.com/docs/books/tutorial/uiswing/painting/concepts2.html

		Graphics2D g = (Graphics2D)g2d.create();

		//Prevent element from drawing outside its bounds
		g.clip(getVBounds());
		g.setStroke(DEFAULT_STROKE);
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
		resetShapeCache();
		Rectangle2D newrect = getVBounds();
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
		highlight (PreferenceManager.getCurrent().getColor(GlobalPreference.COLOR_HIGHLIGHTED));
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
		// first use getVBounds as a rough approximation
		if (getVBounds().intersects(r))
		{
			// Yes, the vbounds intersects, now try to be more precise
			return getVOutline().intersects(r);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Determines whether a Graphics object contains
	 * the point specified
	 * @param point - the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected boolean vContains(Point2D point)
	{
		// first use getVBounds as a rough approximation
		if (getVBounds().contains(point))
		{
			// Yes, the vbounds contains, now try to be more precise
			return getVOutline().contains(point);
		}
		else
		{
			return false;
		}
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
			destroyHandles();
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
			createHandles();
			isSelected = true;
			markDirty();
		}
	}

	protected void createHandles() {}

	/**
	 * Moves this object by specified increments
	 * @param dx - the value of x-increment
	 * @param dy - the value of y-increment
	 */
	protected void vMoveBy(double vdx, double vdy)
	{
	}

	/**
	 * Gets the cached rectangular bounds of this object
	 * This method is equivalent to {@link #getVOutline()}.getBounds2D()
	 * @return
	 */
	public final Rectangle2D getVBounds()
	{
		if(vBoundsCache == null) {
			vBoundsCache = calculateVBounds();
		}
		return vBoundsCache;
	}

	/**
	 * Calculates the rectangular bounds of this object
	 * This method is equivalent to {@link #getVOutline()}.getBounds2D()
	 * @return
	 */
	public Rectangle2D calculateVBounds()
	{
		return getVOutline().getBounds2D();
	}

	/**
	 * Get the cached outline of this element. The outline is used to check
	 * whether a point is contained in this element or not and includes the stroke
	 * and takes into account rotation.
	 * Because it includes the stroke, it is not a direct model to view mapping of
	 * the model outline!
	 * @return the outline of this element
	 */
	protected final Shape getVOutline() {
		if(vOutlineCache == null) {
			vOutlineCache = calculateVOutline();
		}
		return vOutlineCache;
	}

	/**
	 * Calculate the outline of this element. The outline is used to check
	 * whether a point is contained in this element or not and includes the stroke
	 * and takes into account rotation.
	 * Because it includes the stroke, it is not a direct model to view mapping of
	 * the model outline!
	 * @return the outline of this element
	 */
	abstract protected Shape calculateVOutline();

	/**
	 * Scales the object to the given rectangle
	 * @param r
	 */
	protected void setVScaleRectangle(Rectangle2D r) { }

	/**
	 * Gets the rectangle used to scale the object
	 */
	protected Rectangle2D getVScaleRectangle() { return new Rectangle2D.Double(); }

	/**
	 * returns the z-order that defines in what order to draw the element.
	 */
	protected abstract int getZOrder();

	/**
	 * Orders VPathwayElements
	 *
	 * non-Graphics objects always sort above graphics objects
	 * selected Graphics objects sort above non-selected graphics objects
	 * finally, non-selected graphics objects are sorted by their z-order.
	 * The z-order is determined by the type of object by default,
	 * but can be overridden by the user.
	 *
	 * The comparison is consistent with "equals", i.e. it doesn't return 0 if
	 * the objects are different, even if their drawing order is the same.
	 *
	 * @param d VPathwayElement that this is compared to.
	 */
	public int compareTo(VPathwayElement d)
	{
		// same object? easy...
		if (d == this)
			return 0;

		int a, b;

		a = getZOrder();
		b = d.getZOrder();

		// if sorting order is equal, use hash code
		if (b == a)
		{
			return hashCode() - d.hashCode();
		}
		else
			// not simply "a - b" because of the risk of integer overflows
			return a < b ? -1 : 1;
	}

	/**
	 * helper method to convert view coordinates to model coordinates
	 * */
	protected double mFromV(double v) { return canvas.mFromV(v); }

	/**
	 * helper method to convert view coordinates to model coordinates
	 * */
	protected double vFromM(double m) { return canvas.vFromM(m); }

	/**
	 * called automatically by #destroy(), and also by #deselect()
	 * This should be overridden if you create any Handles in createHandles()
	 */
	protected void destroyHandles() {}

	private boolean removeMe = false;
	boolean toBeRemoved() { return removeMe; }

	protected void destroy() {
		//Remove from canvas
		removeMe = true;
		markDirty();
		destroyHandles();
	}

}
