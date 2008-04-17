package org.pathvisio.view;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class CurvedConnectorShape extends ElbowConnectorShape {

	public Shape getShape(ConnectorRestrictions restrictions) {
		GeneralPath path = new GeneralPath();
		Segment[] segments = getSegments(restrictions);
		Segment first = segments[0];
		Segment last = segments[segments.length - 1];
		path.moveTo(
				(float)first.getVStart().getX(), 
				(float)first.getVStart().getY()
		);
		
		for(int i = 1; i < segments.length - 1; i++) {
			Segment s = segments[i];
			Point2D center = s.getVCenter();
			Point2D start = s.getVStart();
			path.curveTo(
					(float)start.getX(),
					(float)start.getY(),
					(float)start.getX(),
					(float)start.getY(),
					(float)center.getX(),
					(float)center.getY()
			);
		}
		
		path.curveTo(
				(float)last.getVStart().getX(),
				(float)last.getVStart().getY(),
				(float)last.getVStart().getX(),
				(float)last.getVStart().getY(),
				(float)last.getVEnd().getX(),
				(float)last.getVEnd().getY()
		);
		return path;
	}
}
