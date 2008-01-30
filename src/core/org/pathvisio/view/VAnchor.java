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
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;
import org.pathvisio.view.LinAlg.Point;

/**
 * VAnchor is the view representation of {@link MAnchor}.
 * @author thomas
 *
 */
public class VAnchor extends VPathwayElement {
	private MAnchor mAnchor;
	private Line line;
	private Handle handle;

	private double mx = Double.NaN;
	private double my = Double.NaN;
	
	public VAnchor(MAnchor mAnchor, Line parent) {
		super(parent.getDrawing());
		this.mAnchor = mAnchor;
		this.line = parent;

		handle = new Handle(Handle.DIRECTION_FREE, this, getDrawing());
		updatePosition();
	}

	public int getDrawingOrder() {
		return VPathway.DRAW_ORDER_ANCHOR;
	}
	
	public double getVx() {
		return vFromM(mx);
	}
	
	public double getVy() {
		return vFromM(my);
	}
	
	public Handle getHandle() {
		return handle;
	}

	public MAnchor getMAnchor() {
		return mAnchor;
	}
	
	protected void destroy() {
		super.destroy();
		line.removeVAnchor(this);
	}
	
	protected Handle[] getHandles() {
		return new Handle[] { handle };
	}

	public void select() {
		handle.show();
		super.select();
	}
	
	public void deselect() {
		handle.hide();
		super.deselect();
	}
	
	void updatePosition() {
		double prevX = mx;
		double prevY = my;
		
		double position = mAnchor.getPosition();
		double vsx = line.getVStartX();
		double vsy = line.getVStartY();
		double vex = line.getVEndX();
		double vey = line.getVEndY();

		int dirx = vsx > vex ? -1 : 1;
		int diry = vsy > vey ? -1 : 1;

		mx = mFromV(vsx + dirx * Math.abs(vsx - vex) * position);
		my = mFromV(vsy + diry * Math.abs(vsy - vey) * position);
		handle.setMLocation(mx, my);
		
		//Move graphRefs
		if(!Double.isNaN(prevX) && !Double.isNaN(prevY)) {
			for(GraphRefContainer ref : mAnchor.getReferences()) {
				if(ref instanceof MPoint) {
					ref.moveBy(mx - prevX, my - prevY);
				}
			}
		}
	}
	
	protected void adjustToHandle(Handle h, double vx, double vy) {
		if(h == handle) {
			//Project handle position on line and calculate relative position
			Point start = new Point(line.getVStartX(), line.getVStartY());
			Point end = new Point(line.getVEndX(), line.getVEndY());
			Point direction = start.subtract(end);
			Point projection = LinAlg.project(start, new Point(vx, vy), direction);
			double lineLength = LinAlg.distance(start, end);
			double anchorLength = LinAlg.distance(start, projection);
			double position = anchorLength / lineLength;
			if(position > 1) position = 1;
			if(position < 0) position = 0;
			mAnchor.setPosition(position);
		}
	}

	private ArrowShape getArrowShape() {
		ArrowShape shape = ShapeRegistry.getArrow(
				mAnchor.getShape().getName());

		if(shape != null)
		{
			AffineTransform f = new AffineTransform();
			double scaleFactor = vFromM (1.0);
			f.translate (getVx(), getVy());
			f.scale (scaleFactor, scaleFactor);		   
			Shape sh = f.createTransformedShape(shape.getShape());
			shape = new ArrowShape (sh, shape.getFillType());
		}
		return shape;
	}
	
	private Shape getShape() {
		ArrowShape shape = getArrowShape();
		return shape != null ? shape.getShape() : handle.getVOutline();
	}
	
	protected void doDraw(Graphics2D g) {
		Color c;
		
		if(isSelected()) {
			c = selectColor;
		}
		else {
			c = line.getPathwayElement().getColor(); 
		}
		
		ArrowShape arrowShape = getArrowShape();
		if(arrowShape != null)
		{
			g.setStroke(new BasicStroke());
			switch (arrowShape.getFillType())
			{
			case ArrowShape.OPEN:
				g.setPaint (Color.WHITE);
				g.fill (arrowShape.getShape());				
				g.setColor (c);
				g.draw (arrowShape.getShape());
				break;
			case ArrowShape.CLOSED:
				g.setPaint (c);
				g.fill (arrowShape.getShape());				
				break;
			case ArrowShape.WIRE:
				g.setColor (c);
				g.draw (arrowShape.getShape());
				break;
			default:
				assert (false);
			}
		}
		
		if(isHighlighted()) {
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke (HIGHLIGHT_STROKE_WIDTH));
			g.draw (getShape());
		}
	}
	
	protected Shape getVOutline() {
		return getShape();
	}
}
