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
import java.awt.geom.Point2D;

import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * VAnchor is the view representation of {@link MAnchor}.
 * @author thomas
 *
 */
public class VAnchor extends VPathwayElement implements LinkProvider {
	private MAnchor mAnchor;
	private Line line;
	private Handle handle;

	private double mx = Double.NaN;
	private double my = Double.NaN;
	
	public VAnchor(MAnchor mAnchor, Line parent) {
		super(parent.getDrawing());
		this.mAnchor = mAnchor;
		this.line = parent;

		linkAnchor = new LinkAnchor(canvas, mAnchor, 0, 0);
		
		handle = new Handle(Handle.DIRECTION_FREE, this, getDrawing());
		updatePosition();
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
		
		double lc = mAnchor.getPosition();
			
		Point2D position = line.vFromL(lc);
		handle.setVLocation(position.getX(), position.getY());
		
		mx = mFromV(position.getX());
		my = mFromV(position.getY());
		
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
			double position = line.lFromV(new Point2D.Double(vx, vy));
			mAnchor.setPosition(position);
		}
	}

	private AnchorShape getAnchorShape() {
		AnchorShape shape = ShapeRegistry.getAnchor(
				mAnchor.getShape().getName());

		if(shape != null)
		{
			AffineTransform f = new AffineTransform();
			double scaleFactor = vFromM (1.0);
			f.translate (getVx(), getVy());
			f.scale (scaleFactor, scaleFactor);		   
			Shape sh = f.createTransformedShape(shape.getShape());
			shape = new AnchorShape (sh);
		}
		return shape;
	}
	
	private Shape getShape() {
		AnchorShape shape = getAnchorShape();
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
		
		AnchorShape arrowShape = getAnchorShape();
		if(arrowShape != null)
		{
			g.setStroke(new BasicStroke());
				g.setPaint (c);
				g.fill (arrowShape.getShape());		
				g.draw(arrowShape.getShape());
		}
		
		if(showLinkAnchors) {
			linkAnchor.draw(g);
		}
		if(isHighlighted()) {
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke (HIGHLIGHT_STROKE_WIDTH));
			g.draw (getShape());
		}
	}
	
	protected Shape calculateVOutline() {
		return getShape();
	}

	LinkAnchor linkAnchor;
	boolean showLinkAnchors = false;
	
	public LinkAnchor getLinkAnchorAt(Point2D p) {
		if(linkAnchor.getMatchArea().contains(p)) {
			return linkAnchor;
		}
		return null;
	}

	public void hideLinkAnchors() {
		showLinkAnchors = false;
		markDirty();
	}

	public void showLinkAnchors() {
		showLinkAnchors = true;
		markDirty();
	}
}
