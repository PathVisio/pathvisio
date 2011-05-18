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
package org.pathvisio.core.model;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Implements a curved connector that draws curved lines between
 * the segments.
 * @author thomas
 *
 */
public class CurvedConnectorShape extends ElbowConnectorShape {
	Segment[] elbowSegments; //The original segments of the elbow connector
	CurvedSegment[] curvedSegments; //The elbow segment with bezier points
	//Higher resolution approximation of the curve
	//Used for calculating the anchor position
	Segment[] curveHigh;
	//Lower resolution approximation of the curve
	//Used for calculating the arrow heads
	Segment[] curveLow;

	protected Shape calculateShape() {
		return calculateShape(elbowSegments);
	}

	public Shape calculateShape(Segment[] segments) {
		GeneralPath path = new GeneralPath();

		CurvedSegment[] curvedSegments = calculateCurvedSegments(segments);

		path.moveTo(
			(float)curvedSegments[0].getMStart().getX(),
			(float)curvedSegments[0].getMStart().getY()
		);

		Segment first = segments[0];
		path.moveTo(
				(float)first.getMStart().getX(),
				(float)first.getMStart().getY()
		);

		for(int i = 0; i < curvedSegments.length; i++) {
			CurvedSegment cs = curvedSegments[i];
			path.curveTo(
					(float)cs.getC1().getX(),
					(float)cs.getC1().getY(),
					(float)cs.getC2().getX(),
					(float)cs.getC2().getY(),
					(float)cs.getMEnd().getX(),
					(float)cs.getMEnd().getY()
			);
		}

//		//Custom curve calculation (for testing)
//		Segment[] curve = calculateCurve(NRSTEP_LOW);
//		path.moveTo(curve[0].getMStart().getX(), curve[0].getMStart().getY());
//		for(int i = 0; i < curve.length; i++) {
//			path.lineTo(curve[i].getMEnd().getX(), curve[i].getMEnd().getY());
//		}

		return path;
	}

	/**
	 * Also calculates curvedSegments and curve
	 * @see calculateCurvedSegments
	 * @see calculateCurve
	 */
	protected Segment[] calculateSegments(ConnectorRestrictions restrictions,
			WayPoint[] waypoints) {
		elbowSegments = super.calculateSegments(restrictions, waypoints);

		//Also calculate curved segments
		curvedSegments = calculateCurvedSegments(elbowSegments);
		curveHigh = calculateCurve(NRSTEP_HIGH);
		curveLow = calculateCurve(NRSTEP_LOW);

		return curveLow;
	}

	/**
	 * Based on the given elbow segments, calculate a new segment
	 * and control points for each bezier curve.
	 */
	protected CurvedSegment[] calculateCurvedSegments(Segment[] segments) {
		CurvedSegment[] curvedSegments = new CurvedSegment[segments.length - 1];

		Segment first = segments[0];
		Segment last = segments[segments.length - 1];

		Point2D prev = first.getMStart();

		for(int i = 1; i < segments.length - 1; i++) {
			Segment s = segments[i];
			Point2D center = s.getMCenter();
			Point2D start = s.getMStart();
			curvedSegments[i - 1] = new CurvedSegment(
				prev,
				center,
				prev,
				start
			);
			prev = s.getMCenter();
		}

		curvedSegments[curvedSegments.length - 1] = new CurvedSegment(
			prev,
			last.getMEnd(),
			last.getMStart(),
			last.getMEnd()
		);
		return curvedSegments;
	}

	static final int NRSTEP_LOW = 3; //Number of steps for lowres curve
	static final int NRSTEP_HIGH = 20; //Number of steps for highres curve

	/**
	 * Calculates the bezier curve, using NRSTEP segments for each
	 * curvedSegment.
	 * @see calculateCurvedSegments
	 * @return An array with the curve broken down into small segments
	 */
	protected Segment[] calculateCurve(int nrStep) {
		Segment[] curve = new Segment[nrStep * curvedSegments.length];

		for(int i = 0; i < curvedSegments.length; i++) {
			CurvedSegment cs = curvedSegments[i];
			Point2D prev = cs.getMStart();

			for(int j = 0; j < nrStep; j++) {
				double t = (j + 1) * 1.0/nrStep;
				double xe = bezier(
						cs.getMStart().getX(),
						cs.getC1().getX(),
						cs.getC2().getX(),
						cs.getMEnd().getX(),
						t
				);
				double ye = bezier(
						cs.getMStart().getY(),
						cs.getC1().getY(),
						cs.getC2().getY(),
						cs.getMEnd().getY(),
						t
				);
				curve[i * nrStep + j] =
					new Segment(prev, new Point2D.Double(xe, ye));
				prev = new Point2D.Double(xe, ye);
			}
		}
		return curve;
	}

	/**
	 * Function for the cubic bezier curve
	 * @param p0 The start coordinate
	 * @param p1 The first helper coordinate
	 * @param p2 The second helper coordinate
	 * @param p3 The end coordinate
	 * @param t The relative position in the curve (0 <= t <= 1)
	 * @return The coordinate on the curve for t
	 */
	private double bezier(double p0, double p1, double p2, double p3, double t) {
		return 	(pow((1-t), 3))*p0 + 3*t*pow((1-t),2)*p1 +
				3*pow(t, 2)*(1-t)*p2 + pow(t, 3)*p3;
	}

	private double pow(double a, double b) {
		return Math.pow(a, b);
	}

	/**
	 * Segment for curved connector, also stores bezier control points
	 * @author thomas
	 */
	private class CurvedSegment extends Segment {
		private Point2D c1;
		private Point2D c2;

		public CurvedSegment(Point2D start, Point2D end, Point2D c1, Point2D c2) {
			super(start, end);
			this.c1 = c1;
			this.c2 = c2;
		}

		public Point2D getC1() {
			return c1;
		}

		public Point2D getC2() {
			return c2;
		}
	}

	public Point2D fromLineCoordinate(double l) {
		return super.fromLineCoordinate(l, curveHigh);
	}

	protected WayPoint[] wayPointsToCenter(WayPoint[] waypoints,
			Segment[] segments) {
		return super.wayPointsToCenter(waypoints, elbowSegments);
	}
}
