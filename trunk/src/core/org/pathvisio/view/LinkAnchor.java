package org.pathvisio.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.GraphLink.GraphRefContainer;

public class LinkAnchor {
	static final double DRAW_RADIUS = 5 * 15;
	static final double MATCH_RADIUS = DRAW_RADIUS + 5 * 15;
	
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
	
	public Shape getShape() {
		Point2D abs = idContainer.toAbsoluteCoordinate(getPosition());
		return canvas.vFromM(new Ellipse2D.Double(
				abs.getX() - DRAW_RADIUS, 
				abs.getY() -DRAW_RADIUS, 
				DRAW_RADIUS * 2, 
				DRAW_RADIUS * 2
		));
	}
	
	public void draw(Graphics2D g2d) {
		g2d.setColor(new Color(255, 0, 0, 128));
		Shape shape = getShape();

		g2d.fill(shape);
		
		g2d.setColor(Color.BLACK);
		
		g2d.draw(shape);		
	}

	public GraphIdContainer getGraphIdContainer() {
		return idContainer;
	}
	
	public void link(GraphRefContainer ref) {
		ref.linkTo(idContainer, relX, relY);
	}
}
