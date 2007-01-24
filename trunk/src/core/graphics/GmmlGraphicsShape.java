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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;

import util.LinAlg;
import util.SwtUtils;
import util.LinAlg.Point;
import data.GmmlDataObject;
import data.GmmlEvent;
import data.ObjectType;

/**
 * This is an {@link GmmlGraphics} class representing shapelike forms,
 * and provides implementation for containing 8 handles placed in a 
 * (rotated) rectangle around the shape and a rotation handle
 */
public abstract class GmmlGraphicsShape extends GmmlGraphics {
	
	//Side handles
	GmmlHandle handleN;
	GmmlHandle handleE;
	GmmlHandle handleS;
	GmmlHandle handleW;
	//Corner handles
	GmmlHandle handleNE;
	GmmlHandle handleSE;
	GmmlHandle handleSW;
	GmmlHandle handleNW;
	//Rotation handle
	GmmlHandle handleR;
		
	final GmmlHandle[][] handleMatrix; //Used to get opposite handles
	
	public GmmlGraphicsShape(GmmlDrawing canvas, GmmlDataObject o) {
		super(canvas, o);
		
		handleN	= new GmmlHandle(GmmlHandle.DIRECTION_Y, this, canvas);
		handleE	= new GmmlHandle(GmmlHandle.DIRECTION_X, this, canvas);
		handleS	= new GmmlHandle(GmmlHandle.DIRECTION_Y, this, canvas);
		handleW	= new GmmlHandle(GmmlHandle.DIRECTION_X, this, canvas);
				
		handleNE = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		handleSE = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		handleSW = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		handleNW = new GmmlHandle(GmmlHandle.DIRECTION_FREE, this, canvas);
		
		handleR = new GmmlHandle(GmmlHandle.DIRECTION_ROT, this, canvas);
		
		handleMatrix = new GmmlHandle[][] {
				{ handleNW, 	handleNE },
				{ handleSW, 	handleSE }};
	}
		
	/**
	 * Get the x-coordinate of the center point of this object
	 * @return the center x-coordinate as integer
	 */
	protected int getCenterX() { return (int)gdata.getCenterX(); }

	/**
	 * Get the y-coordinate of the center point of this object
	 * @return the center y-coordinate as integer
	 */
	protected int getCenterY() { return (int)gdata.getCenterY(); }
	
	public void moveBy(double dx, double dy)
	{
//		gdata.dontFireEvents(1);
		gdata.setLeft(gdata.getLeft() + dx); 
		gdata.setTop(gdata.getTop() + dy);
		
		if(gdata.getParent() == null) return; //NOTE: Quick fix for GmmlSelectionBox -> shouldn't extend GmmlGraphics
		String id = gdata.getGraphId();
		List<GmmlDataObject> refs = gdata.getParent().getReferringObjects(id);
		
		if (refs != null && !id.equals("")) 
		{
			for (GmmlDataObject o : refs)
			{
				if (o.getObjectType() == ObjectType.LINE)
				{
					String startRef = o.getStartGraphRef();
					if (startRef != null && startRef.equals (id))
					{
//						o.dontFireEvents(1);
						o.setStartX(o.getStartX() + dx);
						o.setStartY(o.getStartY() + dy);
					}
					String endRef = o.getEndGraphRef();
					if (endRef != null && o.getEndGraphRef().equals (id))
					{
//						o.dontFireEvents(1);
						o.setEndX(o.getEndX() + dx);
						o.setEndY(o.getEndY() + dy);
					}
				}
			}
		}
	}
	
	private void setShape(double left, double top, double width, double height) {
//		gdata.dontFireEvents(3);
		gdata.setWidth(width);
		gdata.setHeight(height);
		gdata.setLeft(left);
		gdata.setTop(top);
	}
	
	public void setScaleRectangle(Rectangle2D.Double r) {
		setShape(r.x, r.y, r.width, r.height);
	}

	protected void adjustToZoom(double factor)
	{
		setShape(gdata.getLeft() * factor, 
				gdata.getTop() * factor, 
				gdata.getWidth() * factor, 
				gdata.getHeight() * factor);
	}
	
	protected Rectangle2D.Double getScaleRectangle() {
		return new Rectangle2D.Double(gdata.getLeft(), gdata.getTop(), gdata.getWidth(), gdata.getHeight());
	}
	
	public GmmlHandle[] getHandles()
	{
		if( this instanceof GmmlSelectionBox) {
			// Only corner handles
			return new GmmlHandle[] {
					handleNE, handleSE,
					handleSW, handleNW
			};
		}
		if(	this instanceof GmmlGeneProduct || 
			this instanceof GmmlLabel) {
			// No rotation handle for these objects
			return new GmmlHandle[] {
					handleN, handleNE, handleE, handleSE,
					handleS, handleSW, handleW,	handleNW,
			};
		}
		return new GmmlHandle[] {
				handleN, handleNE, handleE, handleSE,
				handleS, handleSW, handleW,	handleNW,
				handleR
		};
	}
	
	/**
	 * Translate the given point to internal coordinate system
	 * (origin in center and axis direction rotated with this objects rotation
	 * @param Point p
	 */
	private Point toInternal(Point p) {
		Point pt = relativeToCenter(p);
		Point pr = LinAlg.rotate(pt, gdata.getRotation());
		return pr;
	}
	
	/**
	 * Translate the given point to external coordinate system (of the
	 * drawing canvas)
	 * @param Point p
	 */
	private Point toExternal(Point p) {
		Point pr = LinAlg.rotate(p, -gdata.getRotation());
		Point pt = relativeToCanvas(pr);
		return pt;
	}
	
	/**
	 * Translate the given co�rdinates to external coordinate system (of the
	 * drawing canvas)
	 * @param x
	 * @param y
	 */
	private Point toExternal(double x, double y) {
		return toExternal(new Point(x, y));
	}
				
	/**
	 * Get the coordinates of the given point relative
	 * to this object's center
	 * @param p
	 */
	private Point relativeToCenter(Point p) {
		return p.subtract(getCenter());
	}
	
	/**
	 * Get the coordinates of the given point relative
	 * to the canvas' origin
	 * @param p
	 */
	private Point relativeToCanvas(Point p) {
		return p.add(getCenter());
	}
	
	/**
	 * Get the center point of this object
	 */
	public Point getCenter() {
		return new Point(gdata.getCenterX(), gdata.getCenterY());
	}
	
	/**
	 * Set the center point of this object
	 * @param cn
	 */
	public void setCenter(Point cn) {
//		gdata.dontFireEvents(1);
		gdata.setCenterX(cn.x);
		gdata.setCenterY(cn.y);
	}
	
	/**
	 * Calculate a new center point given the new width and height, in a
	 * way that the center moves over the rotated axis of this object
	 * @param newWidth
	 * @param newHeight
	 */
	public Point calcNewCenter(double newWidth, double newHeight) {
		Point cn = new Point((newWidth - gdata.getWidth())/2, (newHeight - gdata.getHeight())/2);
		Point cr = LinAlg.rotate(cn, -gdata.getRotation());
		return relativeToCanvas(cr);
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
	
	/**
	 * Rotates the {@link GC} around the objects center
	 * @param gc	the {@link GC} to rotate
	 * @param tr	a {@link Transform} that can be used for rotation
	 */
	protected void rotateGC(GC gc, Transform tr) {		
		SwtUtils.rotateGC(gc, tr, (float)Math.toDegrees(gdata.getRotation()), 
				getCenterX(), getCenterY());
	}
	
	public void adjustToHandle(GmmlHandle h) {
		//Rotation
		if 	(h == handleR) {
			Point def = relativeToCenter(getHandleLocation(h));
			Point cur = relativeToCenter(new Point(h.centerx, h.centery));
			
			setRotation(gdata.getRotation() + LinAlg.angle(def, cur));
			
			return;
		}
					
		// Transformation
		Point hi = toInternal(new Point(h.centerx, h.centery));
		
		double dx = 0;
		double dy = 0;
		double dw = 0;
		double dh = 0;
			
		if	(h == handleN || h == handleNE || h == handleNW) {
			dy = -(hi.y + gdata.getHeight()/2);
			dh = -dy;
		}
		if	(h == handleS || h == handleSE || h == handleSW ) {
			dy = hi.y - gdata.getHeight()/2;
			dh = dy;
		}
		if	(h == handleE || h == handleNE || h == handleSE) {
			dx = hi.x - gdata.getWidth()/2;
			dw = dx;
		}
		if	(h == handleW || h == handleNW || h== handleSW) {
			dx = -(hi.x + gdata.getWidth()/2);
			dw = -dx;
		};
		
		Point nc = calcNewCenter(gdata.getWidth() + dw, gdata.getHeight() + dh);
//		gdata.dontFireEvents(1);
		gdata.setHeight(gdata.getHeight() + dy);
		gdata.setWidth(gdata.getWidth() + dx);
		setCenter(nc);		
	
		//In case object had zero width, switch handles
		if(gdata.getWidth() < 0) {
			negativeWidth(h);
		}
		if(gdata.getHeight() < 0) {
			negativeHeight(h);
		}
	}
	
	/**
	 * This method implements actions performed when the width of
	 * the object becomes negative after adjusting to a handle
	 * @param h	The handle this object adjusted to
	 */
	public void negativeWidth(GmmlHandle h) {
		if(h.getDirection() == GmmlHandle.DIRECTION_FREE)  {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_X);
		} else {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_XY);
		}
		double w = -gdata.getWidth();
		double sx = gdata.getLeft() - w;
//		gdata.dontFireEvents(1);
		gdata.setWidth (w);
		gdata.setLeft(sx);
		canvas.setPressedObject(h);
	}
	
	/**
	 * This method implements actions performed when the height of
	 * the object becomes negative after adjusting to a handle
	 * @param h	The handle this object adjusted to
	 */
	public void negativeHeight(GmmlHandle h) {
		if(h.getDirection() == GmmlHandle.DIRECTION_FREE)  {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_Y);
		} else {
			h = getOppositeHandle(h, GmmlHandle.DIRECTION_XY);
		}
		double ht = -gdata.getHeight();
		double sy = gdata.getTop() - ht;
//		gdata.dontFireEvents(1);
		gdata.setHeight(ht);
		gdata.setTop(sy);
		canvas.setPressedObject(h);
	}
	
	/**
	 * Sets the handles at the correct location;
	 * @param ignore the position of this handle will not be adjusted
	 */
	private void setHandleLocation(GmmlHandle ignore)
	{
		Point p;
		p = getHandleLocation(handleN);
		if(ignore != handleN) handleN.setLocation(p.x, p.y);
		p = getHandleLocation(handleE);
		if(ignore != handleE) handleE.setLocation(p.x, p.y);
		p = getHandleLocation(handleS);
		if(ignore != handleS) handleS.setLocation(p.x, p.y);
		p = getHandleLocation(handleW);
		if(ignore != handleW) handleW.setLocation(p.x, p.y);
		
		p = getHandleLocation(handleNE);
		if(ignore != handleNE) handleNE.setLocation(p.x, p.y);
		p = getHandleLocation(handleSE);
		if(ignore != handleSE) handleSE.setLocation(p.x, p.y);
		p = getHandleLocation(handleSW);
		if(ignore != handleSW) handleSW.setLocation(p.x, p.y);
		p = getHandleLocation(handleNW);
		if(ignore != handleNW) handleNW.setLocation(p.x, p.y);

		p = getHandleLocation(handleR);
		if(ignore != handleR) handleR.setLocation(p.x, p.y);
		
		for(GmmlHandle h : getHandles()) h.rotation = gdata.getRotation();
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
	 * (in co�rdinates relative to the canvas)
	 * @param h
	 */
	protected Point getHandleLocation(GmmlHandle h) {
		if(h == handleN) return toExternal(0, -gdata.getHeight()/2);
		if(h == handleE) return toExternal(gdata.getWidth()/2, 0);
		if(h == handleS) return toExternal(0,  gdata.getHeight()/2);
		if(h == handleW) return toExternal(-gdata.getWidth()/2, 0);
		
		if(h == handleNE) return toExternal(gdata.getWidth()/2, -gdata.getHeight()/2);
		if(h == handleSE) return toExternal(gdata.getWidth()/2, gdata.getHeight()/2);
		if(h == handleSW) return toExternal(-gdata.getWidth()/2, gdata.getHeight()/2);
		if(h == handleNW) return toExternal(-gdata.getWidth()/2, -gdata.getHeight()/2);

		if(h == handleR) return toExternal(gdata.getWidth()/2 + (30*getDrawing().getZoomFactor()), 0);
		return null;
	}
	
	/**
	 * Gets the handle opposite to the given handle.
	 * For directions N, E, S and W this is always their complement,
	 * for directions NE, NW, SE, SW, you can constraint the direction, e.g.:
	 * if direction is X, the opposite of NE will be NW instead of SW
	 * @param h	The handle to find the opposite for
	 * @param direction	Constraints on the direction, one of {@link GmmlHandle}#DIRECTION_*.
	 * Will be ignored for N, E, S and W handles
	 * @return	The opposite handle
	 */
	GmmlHandle getOppositeHandle(GmmlHandle h, int direction) {
		//Ignore direction for N, E, S and W
		if(h == handleN) return handleS;
		if(h == handleE) return handleW;
		if(h == handleS) return handleN;
		if(h == handleW) return handleE;
				
		int[] pos = handleFromMatrix(h);
		switch(direction) {
		case GmmlHandle.DIRECTION_XY:
		case GmmlHandle.DIRECTION_MINXY:
		case GmmlHandle.DIRECTION_FREE:
			return handleMatrix[ Math.abs(pos[0] - 1)][ Math.abs(pos[1] - 1)];
		case GmmlHandle.DIRECTION_Y:
			return handleMatrix[ Math.abs(pos[0] - 1)][pos[1]];
		case GmmlHandle.DIRECTION_X:
			return handleMatrix[ pos[0]][ Math.abs(pos[1] - 1)];
		default:
			return null;
		}
	}
	
	int[] handleFromMatrix(GmmlHandle h) {
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
	protected Shape getOutline()
	{
		int[] x = new int[4];
		int[] y = new int[4];
		
		int[] p = getHandleLocation(handleNE).asIntArray();
		x[0] = p[0]; y[0] = p[1];
		p = getHandleLocation(handleSE).asIntArray();
		x[1] = p[0]; y[1] = p[1];
		p = getHandleLocation(handleSW).asIntArray();
		x[2] = p[0]; y[2] = p[1];
		p = getHandleLocation(handleNW).asIntArray();
		x[3] = p[0]; y[3] = p[1];
		
		Polygon pol = new Polygon(x, y, 4);
		return pol;
	}
			
	public void gmmlObjectModified(GmmlEvent e) {		
		markDirty(); // mark everything dirty
		setHandleLocation();
	}
}
