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

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MPoint;
import org.pathvisio.view.LinAlg.Point;

/**
 * This is an {@link Graphics} class representing shapelike forms,
 * and provides implementation for containing 8 handles placed in a 
 * (rotated) rectangle around the shape and a rotation handle
 */
public abstract class GraphicsShape extends Graphics {

	private static final double M_ROTATION_HANDLE_POSITION = 20.0 * 15;

	//Side handles
	Handle handleN;
	Handle handleE;
	Handle handleS;
	Handle handleW;
	//Corner handles
	Handle handleNE;
	Handle handleSE;
	Handle handleSW;
	Handle handleNW;
	//Rotation handle
	Handle handleR;
		
	final Handle[][] handleMatrix; //Used to get opposite handles
	
	public GraphicsShape(VPathway canvas, PathwayElement o) {
		super(canvas, o);
		
		handleN	= new Handle(Handle.DIRECTION_Y, this, canvas);
		handleE	= new Handle(Handle.DIRECTION_X, this, canvas);
		handleS	= new Handle(Handle.DIRECTION_Y, this, canvas);
		handleW	= new Handle(Handle.DIRECTION_X, this, canvas);
				
		handleNE = new Handle(Handle.DIRECTION_FREE, this, canvas);
		handleSE = new Handle(Handle.DIRECTION_FREE, this, canvas);
		handleSW = new Handle(Handle.DIRECTION_FREE, this, canvas);
		handleNW = new Handle(Handle.DIRECTION_FREE, this, canvas);
		
		handleR = new Handle(Handle.DIRECTION_ROT, this, canvas);
		
		handleMatrix = new Handle[][] {
				{ handleNW, 	handleNE },
				{ handleSW, 	handleSE }};
	}
	
	
	/**
	 * Adjust model to changes in the shape, 
	 * and at the same time calculates the new position 
	 * in gpml coordinates (so without zoom factor)
	 */
	private void setVShape(double vleft, double vtop, double vwidth, double vheight) 
	{
//		gdata.dontFireEvents(3);
		gdata.setMWidth(mFromV(vwidth));
		gdata.setMHeight(mFromV(vheight));
		gdata.setMLeft(mFromV(vleft));
		gdata.setMTop(mFromV(vtop));
	}
	
	protected void vMoveBy(double vdx, double vdy)
	{
		gdata.setMLeft(gdata.getMLeft()  + mFromV(vdx));
		gdata.setMTop(gdata.getMTop() + mFromV(vdy));
		//Move graphRefs
		//GraphLink.moveRefsBy(gdata, mFromV(vdx), mFromV(vdy));
		Set<VPoint> toMove = new HashSet<VPoint>();
		for(GraphRefContainer ref : gdata.getReferences()) {
			if(ref instanceof MPoint) {
				toMove.add(canvas.getPoint((MPoint)ref));
			}
		}
		for(VPoint p : toMove) p.vMoveBy(vdx, vdy);
	}

	public void setVScaleRectangle(Rectangle2D r) {
		setVShape(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	
	protected Rectangle2D getVScaleRectangle() {
		return new Rectangle2D.Double(getVLeftDouble(), getVTopDouble(), getVWidthDouble(), getVHeightDouble());
	}
	
	public Handle[] getHandles()
	{
		if( this instanceof SelectionBox) {
			// Only corner handles
			return new Handle[] {
					handleNE, handleSE,
					handleSW, handleNW
			};
		}
		if(	this instanceof GeneProduct || 
			this instanceof Label) {
			// No rotation handle for these objects
			return new Handle[] {
					handleN, handleNE, handleE, handleSE,
					handleS, handleSW, handleW,	handleNW,
			};
		}
		return new Handle[] {
				handleN, handleNE, handleE, handleSE,
				handleS, handleSW, handleW,	handleNW,
				handleR
		};
	}
	
	/**
	 * Translate the given point to internal coordinate system
	 * (origin in center and axis direction rotated with this objects rotation
	 * @param MPoint p
	 */
	private Point mToInternal(Point p) {
		Point pt = mRelativeToCenter(p);
		Point pr = LinAlg.rotate(pt, gdata.getRotation());
		return pr;
	}

	/**
	 * Translate the given point to external coordinate system (of the
	 * drawing canvas)
	 * @param MPoint p
	 */
	private Point mToExternal(Point p) {
		Point pr = LinAlg.rotate(p, -gdata.getRotation());
		Point pt = mRelativeToCanvas(pr);
		return pt;
	}

	/**
	 * Translate the given coordinates to external coordinate system (of the
	 * drawing canvas)
	 * @param x
	 * @param y
	 */
	private Point mToExternal(double x, double y) {
		return mToExternal(new Point(x, y));
	}

	/**
	 * Get the coordinates of the given point relative
	 * to this object's center
	 * @param p
	 */
	private Point mRelativeToCenter(Point p) {
		return p.subtract(getMCenter());
	}

	/**
	 * Get the coordinates of the given point relative
	 * to the canvas' origin
	 * @param p
	 */
	private Point vRelativeToCanvas(Point p) {
		return p.add(getVCenter());
	}

	private Point mRelativeToCanvas(Point p) {
		return p.add(getMCenter());
	}

	/**
	 * Get the center point of this object
	 */
	public Point getVCenter() {
		return new Point(getVCenterX(), getVCenterY());
	}

	/**
	 * Get the center point of this object
	 */
	public Point getMCenter() {
		return new Point(gdata.getMCenterX(), gdata.getMCenterY());
	}

	/**
	 * Set the center point of this object
	 * @param cn
	 */
	public void setMCenter(Point mcn) {
//		gdata.dontFireEvents(1);
		gdata.setMCenterX(mcn.x);
		gdata.setMCenterY(mcn.y);
	}

	public void setVCenter(Point vcn) {
//		gdata.dontFireEvents(1);
		gdata.setMCenterX(mFromV(vcn.x));
		gdata.setMCenterY(mFromV(vcn.y));
	}

	/**
	 * Calculate a new center point given the new width and height, in a
	 * way that the center moves over the rotated axis of this object
	 * @param mWidthNew
	 * @param mHeightNew
	 */
	public Point mCalcNewCenter(double mWidthNew, double mHeightNew) {
		Point mcn = new Point((mWidthNew - gdata.getMWidth())/2, (mHeightNew - gdata.getMHeight())/2);
		Point mcr = LinAlg.rotate(mcn, -gdata.getRotation());
		return mRelativeToCanvas(mcr);
	}

	public Point vCalcNewCenter(double vWidthNew, double vHeightNew) {
		Point vcn = new Point((vWidthNew - getVWidth())/2, (vHeightNew - getVHeight())/2);
		Point vcr = LinAlg.rotate(vcn, -gdata.getRotation());
		return vRelativeToCanvas(vcr);
	}

	/**
	 * Set the rotation of this object
	 * @param angle angle of rotation in radians
	 */
	public void setRotation(double angle) {
		if(angle < 0) gdata.setRotation(angle + Math.PI*2);
		else if(angle > Math.PI*2) gdata.setRotation (angle - Math.PI*2);
		else gdata.setRotation(angle);
	}
	
//	/**
//	 * Rotates the {@link GC} around the objects center
//	 * @param gc	the {@link GC} to rotate
//	 * @param tr	a {@link Transform} that can be used for rotation
//	 */
//	protected void rotateGC(GC gc, Transform tr) {		
//		SwtUtils.rotateGC(gc, tr, (float)Math.toDegrees(gdata.getRotation()), 
//				getVCenterX(), getVCenterY());
//	}
	
	public void adjustToHandle(Handle h) {
		//Rotation
		if 	(h == handleR) {
			Point def = mRelativeToCenter(getMHandleLocation(h));
			Point cur = mRelativeToCenter(new Point(h.mCenterx, h.mCentery));
			
			setRotation(gdata.getRotation() + LinAlg.angle(def, cur));
			
			return;
		}
					
		// Transformation
		Point mih = mToInternal(new Point(h.mCenterx, h.mCentery));
		
		double mdx = 0;
		double mdy = 0;
		double mdw = 0;
		double mdh = 0;
			
		if	(h == handleN || h == handleNE || h == handleNW) {
			mdy = -(mih.y + gdata.getMHeight()/2);
			mdh = -mdy;
		}
		if	(h == handleS || h == handleSE || h == handleSW ) {
			mdy = mih.y - gdata.getMHeight()/2;
			mdh = mdy;
		}
		if	(h == handleE || h == handleNE || h == handleSE) {
			mdx = mih.x - gdata.getMWidth()/2;
			mdw = mdx;
		}
		if	(h == handleW || h == handleNW || h== handleSW) {
			mdx = -(mih.x + gdata.getMWidth()/2);
			mdw = -mdx;
		};
		
		Point mnc = mCalcNewCenter(gdata.getMWidth() + mdw, gdata.getMHeight() + mdh);
//		gdata.dontFireEvents(1);
		gdata.setMHeight(gdata.getMHeight() + mdy);
		gdata.setMWidth(gdata.getMWidth() + mdx);
		setMCenter(mnc);		
	
		//In case object had zero width, switch handles
		if(gdata.getMWidth() < 0) {
			negativeWidth(h);
		}
		if(gdata.getMHeight() < 0) {
			negativeHeight(h);
		}
	}
	
	/**
	 * This method implements actions performed when the width of
	 * the object becomes negative after adjusting to a handle
	 * @param h	The handle this object adjusted to
	 */
	public void negativeWidth(Handle h) {
		if(h.getDirection() == Handle.DIRECTION_FREE)  {
			h = getOppositeHandle(h, Handle.DIRECTION_X);
		} else {
			h = getOppositeHandle(h, Handle.DIRECTION_XY);
		}
		double mw = -gdata.getMWidth();
		double msx = gdata.getMLeft() - mw;
//		gdata.dontFireEvents(1);
		gdata.setMWidth (mw);
		gdata.setMLeft(msx);
		canvas.setPressedObject(h);
	}
	
	/**
	 * This method implements actions performed when the height of
	 * the object becomes negative after adjusting to a handle
	 * @param h	The handle this object adjusted to
	 */
	public void negativeHeight(Handle h) {
		if(h.getDirection() == Handle.DIRECTION_FREE)  {
			h = getOppositeHandle(h, Handle.DIRECTION_Y);
		} else {
			h = getOppositeHandle(h, Handle.DIRECTION_XY);
		}
		double ht = -gdata.getMHeight();
		double sy = gdata.getMTop() - ht;
//		gdata.dontFireEvents(1);
		gdata.setMHeight(ht);
		gdata.setMTop(sy);
		canvas.setPressedObject(h);
	}
	
	/**
	 * Sets the handles at the correct location;
	 * @param ignore the position of this handle will not be adjusted
	 */
	private void setHandleLocation(Handle ignore)
	{
		Point p;
		p = getMHandleLocation(handleN);
		if(ignore != handleN) handleN.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleE);
		if(ignore != handleE) handleE.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleS);
		if(ignore != handleS) handleS.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleW);
		if(ignore != handleW) handleW.setMLocation(p.x, p.y);
		
		p = getMHandleLocation(handleNE);
		if(ignore != handleNE) handleNE.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleSE);
		if(ignore != handleSE) handleSE.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleSW);
		if(ignore != handleSW) handleSW.setMLocation(p.x, p.y);
		p = getMHandleLocation(handleNW);
		if(ignore != handleNW) handleNW.setMLocation(p.x, p.y);

		p = getMHandleLocation(handleR);
		if(ignore != handleR) handleR.setMLocation(p.x, p.y);
		
		for(Handle h : getHandles()) h.rotation = gdata.getRotation();
	}
	
	/**
	 * Sets the handles at the correct location
	 */
	public void setHandleLocation()
	{
		setHandleLocation(null);
	}
	
	/**
	 * Get the default location of the given handle 
	 * (in coordinates relative to the canvas)
	 * @param h
	 */
	protected Point getVHandleLocation(Handle h) 
	{
		Point mp = getMHandleLocation (h);
		if (mp != null)			
			return new Point (vFromM(mp.x), vFromM(mp.y));
		else return null;
	}

	protected Point getMHandleLocation(Handle h) {
		if(h == handleN) return mToExternal(0, -gdata.getMHeight()/2);
		if(h == handleE) return mToExternal(gdata.getMWidth()/2, 0);
		if(h == handleS) return mToExternal(0,  gdata.getMHeight()/2);
		if(h == handleW) return mToExternal(-gdata.getMWidth()/2, 0);
		
		if(h == handleNE) return mToExternal(gdata.getMWidth()/2, -gdata.getMHeight()/2);
		if(h == handleSE) return mToExternal(gdata.getMWidth()/2, gdata.getMHeight()/2);
		if(h == handleSW) return mToExternal(-gdata.getMWidth()/2, gdata.getMHeight()/2);
		if(h == handleNW) return mToExternal(-gdata.getMWidth()/2, -gdata.getMHeight()/2);

		if(h == handleR) return mToExternal(gdata.getMWidth()/2 + M_ROTATION_HANDLE_POSITION, 0);
		return null;
	}
	
	/**
	 * Gets the handle opposite to the given handle.
	 * For directions N, E, S and W this is always their complement,
	 * for directions NE, NW, SE, SW, you can constraint the direction, e.g.:
	 * if direction is X, the opposite of NE will be NW instead of SW
	 * @param h	The handle to find the opposite for
	 * @param direction	Constraints on the direction, one of {@link Handle}#DIRECTION_*.
	 * Will be ignored for N, E, S and W handles
	 * @return	The opposite handle
	 */
	Handle getOppositeHandle(Handle h, int direction) {
		//Ignore direction for N, E, S and W
		if(h == handleN) return handleS;
		if(h == handleE) return handleW;
		if(h == handleS) return handleN;
		if(h == handleW) return handleE;
				
		int[] pos = handleFromMatrix(h);
		switch(direction) {
		case Handle.DIRECTION_XY:
		case Handle.DIRECTION_MINXY:
		case Handle.DIRECTION_FREE:
			return handleMatrix[ Math.abs(pos[0] - 1)][ Math.abs(pos[1] - 1)];
		case Handle.DIRECTION_Y:
			return handleMatrix[ Math.abs(pos[0] - 1)][pos[1]];
		case Handle.DIRECTION_X:
			return handleMatrix[ pos[0]][ Math.abs(pos[1] - 1)];
		default:
			return null;
		}
	}
	
	int[] handleFromMatrix(Handle h) {
		for(int x = 0; x < 2; x++) {
			for(int y = 0; y < 2; y++) {
				if(handleMatrix[x][y] == h) return new int[] {x,y};
			}
		}
		return null;
	}
	
	/**
	 * Creates a shape of the outline of this object
	 */
	protected Shape getVOutline()
	{
		return getShape();
	}
		
	protected Shape getShape() {
		return getFillShape(defaultStroke.getLineWidth());
	}
	protected Shape getFillShape() {
		return getFillShape(0);
	}
	
	protected Shape getFillShape(float sw) {
		int x = getVLeft();
		int y = getVTop();
		int w = getVWidth();
		int h = getVHeight();
		int cx = getVCenterX();
		int cy = getVCenterY();

		Shape s = new Rectangle2D.Double(x, y, w + sw, h + sw);
		AffineTransform t = new AffineTransform();
		t.rotate(gdata.getRotation(), cx, cy);
		return s;
	}
	
	public void gmmlObjectModified(PathwayEvent e) {		
		markDirty(); // mark everything dirty
		setHandleLocation();
	}
	
}
