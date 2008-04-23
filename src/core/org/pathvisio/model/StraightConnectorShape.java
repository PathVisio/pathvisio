package org.pathvisio.model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.pathvisio.view.LinAlg;
import org.pathvisio.view.LinAlg.Point;

public class StraightConnectorShape extends AbstractConnector {

	public void recalculateShape(ConnectorRestrictions restrictions) {
		setSegments(new Segment[] {
				new Segment(restrictions.getStartPoint(), restrictions.getEndPoint())
		});
		setShape(new Line2D.Double(
				restrictions.getStartPoint(),
				restrictions.getEndPoint()
		));
		setWayPoints(new WayPoint[0]);
	}
	
	public boolean hasValidWaypoints(ConnectorRestrictions restrictions) {
		return false;
	}
	
	public Point2D fromLineCoordinate(double l) {
		Segment[] segments = getSegments();
		Point2D start = segments[0].getMStart();
		Point2D end = segments[segments.length - 1].getMEnd();

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

	public double toLineCoordinate(Point2D v) {
		Segment[] segments = getSegments();
		return LinAlg.toLineCoordinates(
				new Point(segments[0].getMStart()),
				new Point(segments[segments.length - 1].getMEnd()),
				new Point(v)
		);
	}
}
