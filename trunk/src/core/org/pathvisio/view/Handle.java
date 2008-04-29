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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.view.LinAlg.Point;

/**
 * This class implements and handles handles for 
 * objects on the drawing which are used to 
 * resize them or change their location.
 */
public class Handle extends VPathwayElement
{
	private static final long serialVersionUID = 1L;
	
	/** 
	 * because isSelected really doesn't make sense for GmmlHandles, 
	 * I added this variable isVisible. It should be set automatically by its parent
	 * through calls of show() and hide()
	 */
	private boolean isVisible = false;
	
	//The direction this handle is allowed to move in
	int direction;
	public static final int DIRECTION_FREE = 0;
	public static final int DIRECTION_X	 = 1;
	public static final int DIRECTION_Y  = 2; 
	public static final int DIRECTION_ROT = 3;
	public static final int DIRECTION_XY = 4;
	public static final int DIRECTION_MINXY = 5;
	
	public static final int WIDTH 	= 8;
	public static final int HEIGHT	= 8;
	
	public static final int STYLE_DEFAULT = 0;
	public static final int STYLE_SEGMENT = 1;
	public static final int STYLE_ROTATE = 2;
	
	VPathwayElement parent;
	
	double mCenterx;
	double mCentery;
	
	double rotation;
	
	boolean visible;
	
	int style = STYLE_DEFAULT;
	
	/**
	 * Constructor for this class, creates a handle given the parent, direction and canvas
	 * @param direction	Direction this handle can be moved in (one of DIRECTION_*)
	 * @param parent	The object this handle belongs to
	 * @param canvas	The {@link VPathway} to draw this handle on
	 */
	public Handle(int direction, VPathwayElement parent, VPathway canvas)
	{
		super(canvas);		
		this.direction = direction;
		this.parent = parent;
		if(direction == DIRECTION_ROT) {
			setStyle(STYLE_ROTATE);
		}
	}

	/**
	 * Set the appearance style of the handle.
	 * @param style One of the STYLE_* constants
	 */
	public void setStyle(int style) {
		this.style = style;
	}
	
	public VPathwayElement getParent() {
		return parent;
	}
		
	/**
	 * Get the direction this handle is allowed to move in
	 * @return one of DIRECTION_*
	 */
	public int getDirection() { return direction; }
	
	public void setDirection(int direction) { this.direction = direction; }
	
	public void setVLocation(double vx, double vy)
	{
		markDirty();
		mCenterx = mFromV(vx);
		mCentery = mFromV(vy);
		markDirty();
	}

	public void setMLocation(double mx, double my)
	{
		markDirty();
		mCenterx = mx;
		mCentery = my;
		markDirty();
	}
	
	public double getVCenterX() {
		return vFromM(mCenterx);
	}
	
	public double getVCenterY() {
		return vFromM(mCentery);
	}
	
	/**
	 * returns the visibility of this handle
	 * @see hide(), show()
	 */
	public boolean isVisible()
	{
		return isVisible;
	}
	
	/**
	 * call show() to cause this handle to show up and mark its area dirty 
	 * A handle should show itself only if it's parent object is active / selected
	 * @see hide(), isvisible()
	 */
	public void show()
	{
		if (!isVisible)
		{
			isVisible = true;
			markDirty();
		}
	}
	
	/**
	 * hide handle, and also mark its area dirty
	 * @see show(), isvisible()
	 */
	public void hide()
	{
		if (isVisible)
		{
			isVisible = false;
			markDirty();
		}
	}
	
	/**
	 * draws itself, but only if isVisible() is true, there is 
	 * no need for a check for isVisible() before calling draw().
	 */
	public void doDraw(Graphics2D g)
	{
		if(!isVisible) return;
		
		Shape fillShape = getFillShape();
		
		switch(style) {
		case STYLE_ROTATE:
			g.setColor(Color.GREEN);
			break;
		case STYLE_SEGMENT:
			g.setColor(new Color(0, 128, 255));
			break;
		default:
			g.setColor(Color.YELLOW);
			break;
		}
		
		g.fill(fillShape);
		
		g.setColor(Color.BLACK);
		
		g.draw(fillShape);		
	}
		
	/**
	   Note: don't use Handle.vMoveBy, use vMoveTo instead.
	   it's impossible to handle snap-to-grid correctly if you only have the delta information.
	 */
	public void vMoveBy(double vdx, double vdy)
	{
		assert (false);
		// You shouldn't call vMoveBy on a handle! use vMoveTo instead
	}
	
	/**
	   Called when a mouse event forces the handle to move.
	   Note: this doesn't cause the handle itself to move,
	   rather, it passes the information to the underlying object.
	 */
	public void vMoveTo (double vnx, double vny)
	{
		markDirty();

		if(direction != DIRECTION_FREE && direction != DIRECTION_ROT) {
			Point v = new Point(0,0);
			Rectangle2D b = parent.getVBounds();
			Point base = new Point (b.getCenterX(), b.getCenterY());
			if (direction == DIRECTION_X)
			{
				v = new Point (1, 0);
			}
			else if	(direction == DIRECTION_Y)
			{
				v = new Point (0, 1);
			}
			else if (direction == DIRECTION_XY)
			{
				v = new Point (b.getWidth(), b.getHeight());
			}
			else if (direction == DIRECTION_MINXY)
			{
				v = new Point (b.getHeight(), -b.getWidth());
			}
			Point yr = LinAlg.rotate(v, -rotation);
			Point prj = LinAlg.project(base, new Point(vnx, vny), yr);
			vnx = prj.x; vny = prj.y;
		}

		parent.adjustToHandle(this, vnx, vny);
		markDirty();
	}
			
	public Shape calculateVOutline() {
		return getFillShape((int)Math.ceil(defaultStroke.getLineWidth())).getBounds();
	}
		
	private Shape getFillShape() {
		return getFillShape(0);
	}
	
	private Shape getFillShape(int sw) {
		Shape s = null;
		switch(style) {
		case STYLE_ROTATE:
			s = new Ellipse2D.Double(getVCenterX() - WIDTH/2, getVCenterY() - HEIGHT/2, 
					WIDTH + sw, HEIGHT + sw);
			break;
		case STYLE_SEGMENT:
			s = new Rectangle2D.Double(getVCenterX() - WIDTH/2, getVCenterY() - HEIGHT/2, 
					WIDTH + sw, HEIGHT + sw);
			
			s = AffineTransform.getRotateInstance(
					Math.PI / 4, getVCenterX(), getVCenterY()
			).createTransformedShape(s);
			break;
		default:
			s = new Rectangle2D.Double(getVCenterX() - WIDTH/2, getVCenterY() - HEIGHT/2, 
					WIDTH + sw, HEIGHT + sw);
			break;
		}
		return s;
	}
	
	public String toString() { 
		return 	"Handle with parent: " + parent.toString() +
		" and direction " + direction; 
	}
			
} // end of class


