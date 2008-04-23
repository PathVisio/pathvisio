package org.pathvisio.model;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class CurvedConnectorShape extends ElbowConnectorShape {

	public Shape calculateShape(Segment[] segments) {
		GeneralPath path = new GeneralPath();
		Segment first = segments[0];
		Segment last = segments[segments.length - 1];
		path.moveTo(
				(float)first.getMStart().getX(), 
				(float)first.getMStart().getY()
		);
		
		for(int i = 1; i < segments.length - 1; i++) {
			Segment s = segments[i];
			Point2D center = s.getMCenter();
			Point2D start = s.getMStart();
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
				(float)last.getMStart().getX(),
				(float)last.getMStart().getY(),
				(float)last.getMStart().getX(),
				(float)last.getMStart().getY(),
				(float)last.getMEnd().getX(),
				(float)last.getMEnd().getY()
		);
		return path;
	}
}
