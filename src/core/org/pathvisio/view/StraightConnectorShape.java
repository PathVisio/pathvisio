package org.pathvisio.view;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.pathvisio.view.LinAlg.Point;

public class StraightConnectorShape implements ConnectorShape {

	public java.awt.Shape getShape(ConnectorRestrictions restrictions) {
		Line2D line = new Line2D.Double(
				restrictions.getStartPoint(),
				restrictions.getEndPoint()
		);
		return line;
	}
	
	public Segment[] getSegments(ConnectorRestrictions restrictions) {
		return new Segment[] {
				new Segment(restrictions.getStartPoint(), restrictions.getEndPoint())
		};
	}

	public boolean isUsePreferredSegments(ConnectorRestrictions restrictions) {
		return false;
	}

	public Point2D fromLineCoordinate(ConnectorRestrictions restrictions, double l) {
		Point2D start = restrictions.getStartPoint();
		Point2D end = restrictions.getEndPoint();

		double vsx = start.getX();
		double vsy = start.getY();
		double vex = end.getX();
		double vey = end.getY();
		
		int dirx = vsx > vex ? -1 : 1;
		int diry = vsy > vey ? -1 : 1;

		return new Point2D.Double(
			vsx + dirx * Math.abs(vsx - vex) * l,
			vsy + diry * Math.abs(vsy - vey) * l
		);
	}

	public double toLineCoordinate(ConnectorRestrictions restrictions,
			Point2D v) {
		return LinAlg.toLineCoordinates(
				new Point(restrictions.getStartPoint()),
				new Point(restrictions.getEndPoint()),
				new Point(v)
		);
	}
}
