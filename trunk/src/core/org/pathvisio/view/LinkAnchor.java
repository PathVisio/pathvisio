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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.GraphLink.GraphRefContainer;

/**
 * A LinkAnchor is a small round target on a Shape or Line that appears
 * only when you drag a line end around. If the line end is near a LinkAnchor,
 * the line end "connects" to the Shape or Line.
 */
public class LinkAnchor {
	static final double DRAW_RADIUS = 5 * 15;
	static final double MATCH_RADIUS = DRAW_RADIUS + 5 * 15;
	static final int HINT_STROKE_SIZE = 10;

	double relX, relY;
	GraphIdContainer idContainer;
	VPathway canvas;

	public LinkAnchor(VPathway canvas, GraphIdContainer idc, double relX, double relY) {
		this.relX = relX;
		this.relY = relY;
		this.idContainer = idc;
		this.canvas = canvas;
	}

	public Shape getMatchArea() {
		Point2D abs = idContainer.toAbsoluteCoordinate(
				new Point2D.Double(relX, relY)
		);
		return canvas.vFromM(new Ellipse2D.Double(
				abs.getX() - MATCH_RADIUS,
				abs.getY() - MATCH_RADIUS,
				MATCH_RADIUS * 2,
				MATCH_RADIUS * 2
		));
	}

	public Point2D getPosition() {
		return new Point2D.Double(relX, relY);
	}

	private Shape getShape(boolean includeHighlight) {
		Point2D abs = idContainer.toAbsoluteCoordinate(getPosition());
		Shape s = canvas.vFromM(new Ellipse2D.Double(
				abs.getX() - DRAW_RADIUS,
				abs.getY() -DRAW_RADIUS,
				DRAW_RADIUS * 2,
				DRAW_RADIUS * 2
		));
		if(drawHighlight && includeHighlight) {
			return new BasicStroke(HINT_STROKE_SIZE).createStrokedShape(s);
		} else {
			return s;
		}
	}

	public Shape getShape() {
		return getShape(true);
	}

	public void draw(Graphics2D g2d) {
		if(drawHighlight) {
			g2d.setColor(new Color(0, 255, 0, 128));
			g2d.fill(getShape());
		}

		//Draw a bulls eye
		Shape shape = getShape(false);
		Rectangle2D bounds = shape.getBounds2D();
		double r = bounds.getWidth() / 2;
		double cx = bounds.getCenterX();
		double cy = bounds.getCenterY();

		Ellipse2D outer = new Ellipse2D.Double(
				cx - r,
				cy -r,
				r * 2,
				r * 2
		);
		Ellipse2D white = new Ellipse2D.Double(
				cx - r * 2 / 3,
				cy - r * 2 / 3,
				4 * r / 3,
				4 * r / 3
		);
		Ellipse2D inner = new Ellipse2D.Double(
				cx - r / 3,
				cy - r / 3,
				2 * r / 3,
				2 * r / 3
		);

		Color fill = new Color(255, 0, 0, 255);

		g2d.setColor(fill);
		g2d.fill(outer);
		g2d.setColor(Color.WHITE);
		g2d.fill(white);
		g2d.setColor(fill);
		g2d.fill(inner);

		g2d.setColor(Color.BLACK);
		g2d.draw(shape);
	}

	public GraphIdContainer getGraphIdContainer() {
		return idContainer;
	}

	public void link(GraphRefContainer ref) {
		ref.linkTo(idContainer, relX, relY);
	}

	private boolean drawHighlight;

	/**
	 * Display a visual hint to show that this is the anchor that is
	 * being linked to.
	 */
	public void highlight() {
		drawHighlight = true;
	}

	public void unhighlight() {
		drawHighlight = false;
	}
}
