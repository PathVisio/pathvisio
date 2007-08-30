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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
			
	public GraphicsShape(VPathway canvas, PathwayElement o)
	{
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

	public void setVScaleRectangle(Rectangle2D r)
	{
		setVShape(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	
	protected Rectangle2D getVScaleRectangle()
	{
		return new Rectangle2D.Double(getVLeftDouble(), getVTopDouble(), getVWidthDouble(), getVHeightDouble());
	}
	
	public Handle[] getHandles()
	{
		if(	this instanceof GeneProduct || 
			this instanceof Label)
		{
			// No rotation handle for these objects
			return new Handle[]
			{
					handleN, handleNE, handleE, handleSE,
					handleS, handleSW, handleW,	handleNW,
			};
		}
		return new Handle[]
		{
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
	private Point mToInternal(Point p)
	{
		Point pt = mRelativeToCenter(p);
		Point pr = LinAlg.rotate(pt, gdata.getRotation());
		return pr;
	}

	/**
	 * Translate the given coordinates to external coordinate system (of the
	 * drawing canvas)
	 * @param x
	 * @param y
	 */
	private Point mToExternal(double x, double y)
	{
		Point p = new Point(x, y);
		Point pr = LinAlg.rotate(p, -gdata.getRotation());
		pr.x += gdata.getMCenterX();
		pr.y += gdata.getMCenterY();
		return pr;
	}

	/**
	 * Get the coordinates of the given point relative
	 * to this object's center
	 * @param p
	 */
	private Point mRelativeToCenter(Point p)
	{
		return p.subtract(new Point(gdata.getMCenterX(), gdata.getMCenterY()));
	}

	/**
	 * Get the center point of this object
	 */
	private Point getMCenter()
	{
		return new Point(gdata.getMCenterX(), gdata.getMCenterY());
	}

	/**
	 * Set the rotation of this object
	 * @param angle angle of rotation in radians
	 */
	public void setRotation(double angle)
	{
		if(angle < 0) gdata.setRotation(angle + Math.PI*2);
		else if(angle > Math.PI*2) gdata.setRotation (angle - Math.PI*2);
		else gdata.setRotation(angle);
	}
	
	public void adjustToHandle(Handle h, double vnewx, double vnewy)
	{
		//Rotation
		if 	(h == handleR)
		{
			Point cur = mRelativeToCenter(new Point(mFromV(vnewx), mFromV(vnewy)));			
			setRotation (Math.atan2(cur.y, cur.x));				
			return;
		}
					
		// Transformation
		Point iPos = mToInternal(new Point(mFromV(vnewx), mFromV(vnewy)));
		
		double idx = 0;
		double idy = 0;
		double idw = 0;
		double idh = 0;
		double halfh = gdata.getMHeight () / 2;
		double halfw = gdata.getMWidth () / 2;

		if	(h == handleN || h == handleNE || h == handleNW)
		{
			idh = -(iPos.y + halfh);
			idy = -idh / 2;
		}
		if	(h == handleS || h == handleSE || h == handleSW )
		{
			idh = (iPos.y - halfh);
			idy = idh / 2;
		}
		if	(h == handleE || h == handleNE || h == handleSE) {
			idw = (iPos.x - halfw);
			idx = idw / 2;
		}
		if	(h == handleW || h == handleNW || h== handleSW) {
			idw = -(iPos.x + halfw);
			idx = -idw / 2;
		};

		double neww = gdata.getMWidth() + idw;
		double newh = gdata.getMHeight() + idh;

		//In case object had negative width, switch handles
		if(neww < 0)
		{
			setHorizontalOppositeHandle(h);
			neww = -neww;
		}
		if(newh < 0)
		{
			setVerticalOppositeHandle(h);
			newh = -newh;			
		}

		gdata.setMWidth(neww);
		gdata.setMHeight(newh);		
		Point vcr = LinAlg.rotate(new Point (idx, idy), -gdata.getRotation());
		gdata.setMCenterX (gdata.getMCenterX() + vcr.x);
		gdata.setMCenterY (gdata.getMCenterY() + vcr.y);
						  
	}

	private void setHorizontalOppositeHandle(Handle h)
	{
		Handle opposite = null;
		if(h == handleE) opposite = handleW;
		else if(h == handleW) opposite = handleE;
		else if(h == handleNE) opposite = handleNW;
		else if(h == handleSE) opposite = handleSW;
		else if(h == handleNW) opposite = handleNE;
		else if(h == handleSW) opposite = handleSE;
		else opposite = h;
		canvas.setPressedObject(opposite);
	}

	private void setVerticalOppositeHandle(Handle h)
	{
		Handle opposite = null;
		if(h == handleN) opposite = handleS;
		else if(h == handleS) opposite = handleN;
		else if(h == handleNE) opposite = handleSE;
		else if(h == handleSE) opposite = handleNE;
		else if(h == handleNW) opposite = handleSW;
		else if(h == handleSW) opposite = handleNW;
		else opposite = h;
		canvas.setPressedObject(opposite);
	}									   
	
	/**
	 * Sets the handles at the correct location;
	 * @param ignore the position of this handle will not be adjusted
	 */
	protected void setHandleLocation()
	{
		Point p;
		p = mToExternal(0, -gdata.getMHeight()/2);
		handleN.setMLocation(p.x, p.y);
		p = mToExternal(gdata.getMWidth()/2, 0);
		handleE.setMLocation(p.x, p.y);
		p = mToExternal(0,  gdata.getMHeight()/2);
		handleS.setMLocation(p.x, p.y);
		p = mToExternal(-gdata.getMWidth()/2, 0);
		handleW.setMLocation(p.x, p.y);
		
		p = mToExternal(gdata.getMWidth()/2, -gdata.getMHeight()/2);
		handleNE.setMLocation(p.x, p.y);
		p = mToExternal(gdata.getMWidth()/2, gdata.getMHeight()/2);
		handleSE.setMLocation(p.x, p.y);
		p = mToExternal(-gdata.getMWidth()/2, gdata.getMHeight()/2);
		handleSW.setMLocation(p.x, p.y);
		p = mToExternal(-gdata.getMWidth()/2, -gdata.getMHeight()/2);
		handleNW.setMLocation(p.x, p.y);

		p = mToExternal(gdata.getMWidth()/2 + M_ROTATION_HANDLE_POSITION, 0);
		handleR.setMLocation(p.x, p.y);
		
		for(Handle h : getHandles()) h.rotation = gdata.getRotation();
	}
	
	/**
	 * Creates a shape of the outline of this object
	 */
	protected Shape getVOutline()
	{
		return getShape();
	}
		
	protected Shape getShape()
	{
		return getFillShape(defaultStroke.getLineWidth());
	}
	
	protected Shape getFillShape()
	{
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
	
	public void gmmlObjectModified(PathwayEvent e)
	{
		markDirty(); // mark everything dirty
		setHandleLocation();
	}
	
}
