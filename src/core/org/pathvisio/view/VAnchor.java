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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * VAnchor is the view representation of {@link MAnchor}.
 *
 * It is stuck to a Line and can move one-dimensionally across it. It has a handle
 * so the user can drag it.
 */
public class VAnchor extends VPathwayElement implements LinkProvider, Adjustable {
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

	@Override protected void destroyHandles()
	{
		if (handle != null)
		{
			handle.destroy();
			handle = null;
		}
	}

	protected void createHandles()
	{
		handle = new Handle(Handle.Freedom.FREE, this, this);
		double lc = mAnchor.getPosition();
		Point2D position = line.vFromL(lc);
		handle.setVLocation(position.getX(), position.getY());
	}

	void updatePosition() {
		double lc = mAnchor.getPosition();

		Point2D position = line.vFromL(lc);
		if (handle != null) handle.setVLocation(position.getX(), position.getY());

		mx = mFromV(position.getX());
		my = mFromV(position.getY());

		//Redraw graphRefs
		for(GraphRefContainer ref : mAnchor.getReferences()) {
			if(ref instanceof MPoint) {
				VPoint vp = canvas.getPoint((MPoint)ref);
				if(vp != null && vp.getLine() != line) {
					vp.getLine().recalculateConnector();
				}
			}
		}
	}

	public void adjustToHandle(Handle h, double vx, double vy)
	{
		double position = line.lFromV(new Point2D.Double(vx, vy));
		mAnchor.setPosition(position);
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
			linkAnchor.draw((Graphics2D)g.create());
		}
		if(isHighlighted()) {
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke (HIGHLIGHT_STROKE_WIDTH));
			g.draw (getShape());
		}
	}

	//Minimum outline diameter of 15px
	static final double MIN_OUTLINE = 15;

	protected Shape calculateVOutline() {
		Shape s = getShape();
		Rectangle b = s.getBounds();
		//Create a larger shape if the given shape
		//is smaller than the minimum
		if(b.getWidth() < MIN_OUTLINE ||
			b.getHeight() < MIN_OUTLINE) {
			s = new Ellipse2D.Double(
					getVx() - MIN_OUTLINE / 2, getVy() - MIN_OUTLINE / 2,
					MIN_OUTLINE, MIN_OUTLINE
			);
		}
		if(showLinkAnchors) {
			Area a = new Area(s);
			a.add(new Area(linkAnchor.getShape()));
			return a;
		} else {
			return s;
		}
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

	/**
	 * Returns the zorder of the parent line
	 */
	protected int getZOrder() {
		return line.getPathwayElement().getZOrder() + 1;
	}
}
