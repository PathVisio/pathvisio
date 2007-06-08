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
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.pathvisio.view.LinAlg.Point;

/**
 * This class implements and handles handles for 
 * objects on the drawing which are used to 
 * resize them or change their location.
 */
class Handle extends VPathwayElement
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
	
	VPathwayElement parent;
	
	double mCenterx;
	double mCentery;
	
	double rotation;
	
	boolean visible;
	
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
	}

	public int getDrawingOrder() {
		return VPathway.DRAW_ORDER_HANDLE;
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
	public void draw(PaintEvent e, GC buffer)
	{
		if (!isVisible) return;
		double vCenterx = vFromM (mCenterx);
		double vCentery = vFromM (mCentery);
		
		if(direction == DIRECTION_ROT) {
			buffer.setLineWidth (1);
			buffer.setLineStyle(SWT.LINE_SOLID);
			buffer.setBackground (e.display.getSystemColor (SWT.COLOR_GREEN));
			buffer.setForeground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.fillOval(
					(int)(vCenterx - WIDTH/2), 
					(int)(vCentery - HEIGHT/2), 
					(int)WIDTH, 
					(int)HEIGHT);
			buffer.drawOval(
					(int)(vCenterx - WIDTH/2), 
					(int)(vCentery - HEIGHT/2), 
					(int)WIDTH, 
					(int)HEIGHT);
		} else {			
			buffer.setLineWidth (1);
			buffer.setLineStyle(SWT.LINE_SOLID);
			buffer.setBackground (e.display.getSystemColor (SWT.COLOR_YELLOW));
			buffer.setForeground (e.display.getSystemColor (SWT.COLOR_BLACK));
			buffer.fillRectangle (
					(int)(vCenterx - WIDTH/2), 
					(int)(vCentery - HEIGHT/2), 
					(int)WIDTH, 
					(int)HEIGHT);	
			buffer.drawRectangle (
					(int)(vCenterx - WIDTH/2), 
					(int)(vCentery - HEIGHT/2), 
					(int)WIDTH, 
					(int)HEIGHT);	
		}
		
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
		
	/**
	 * Moves this handle by the specified increments and
	 * adjusts the {@link VPathwayElement} to the new position
	 */
	public void vMoveBy(double vdx, double vdy)
	{	
		markDirty();

		if(direction != DIRECTION_FREE && direction != DIRECTION_ROT) {
			Point v = new Point(0,0);
			double xtraRot = 0;
			if		(direction == DIRECTION_X) {
				v = new Point(1,0);
			}
			else if	(direction == DIRECTION_Y) {
				v = new Point(0,1);
			}
			else if (direction == DIRECTION_XY) {
				Rectangle b = parent.getVBounds();
				v = new Point(b.width + 1, b.height + 1);
			}
			else if (direction == DIRECTION_MINXY) {
				xtraRot = Math.PI/2;
				Rectangle b = parent.getVBounds();
				v = new Point(b.height + 1, b.width + 1);
			}
			Point yr = LinAlg.rotate(v, -rotation + xtraRot);
			Point prj = LinAlg.project(new Point(vdx, vdy), yr);
			vdx = prj.x; vdy= prj.y;
		}
		
		mCenterx += mFromV(vdx);
		mCentery += mFromV(vdy);
		
		parent.adjustToHandle(this);
		markDirty();
	}
			
	public Shape getVOutline() {
		return new Rectangle2D.Double(vFromM(mCenterx) - WIDTH/2, vFromM(mCentery) - HEIGHT/2, 
				WIDTH, HEIGHT);
	}
		
	public String toString() { 
		return 	"Handle with parent: " + parent.toString() +
		" and direction " + direction; 
	}
			
} // end of class


