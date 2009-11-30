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
package org.pathvisio.model;

import java.awt.geom.Point2D;

import org.pathvisio.view.LinAlg;
import org.pathvisio.view.LinAlg.Point;


/**
 * Implement this to provide a line shape for connectors.
 *
 * Different implementations may draw a Line in different ways, for example with
 * curved or straight lines.
 */
public interface ConnectorShape {

	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;

	/**
	 * Force the connector to redraw it's path. The cache for segments,
	 * waypoints and shape.
	 * @param restrictions The ConnectorRestrictions that provides the start, end and
	 * preferred waypoints
	 */
	public void recalculateShape(ConnectorRestrictions restrictions);

	/**
	 * Get the Shape that represents the connector path
	 */
	public java.awt.Shape getShape();



	 /**
	  *  Calculate shape from the width of the line endings
	  *  This gets
	  */
	 public java.awt.Shape calculateAdjustedShape(double startLineEndingWidth, double endLineEndingWidth);


	/**
	 * Get the individual segments of the path
	 */
	public Segment[] getSegments();


	/**
	 * Get the waypoints through which the connector passes
	 */
	public WayPoint[] getWayPoints();

	/**
	 * Checks whether the waypoints as provided by the ConnectorRestrictions
	 * are valid and will be used to draw the connector path
	 * @return true if the waypoints are used, false if not
	 */
	public boolean hasValidWaypoints(ConnectorRestrictions restrictions);

	/**
	 * A waypoint, a point through which the connector passes. Each waypoint
	 * will have a handle in the view, so the user can modify it's position.
	 */
	public class WayPoint extends Point2D.Double {
		public WayPoint(Point2D position) {
			super(position.getX(), position.getY());
		}

		public WayPoint(double x, double y) {
			super(x, y);
		}
	}

	/**
	 * A single segment of the connector path.
	 * This is simply a combination of a start and end Point2D.
	 */
	public class Segment {
		private Point2D start, end;

		protected Segment(Point2D start, Point2D end) {
			this.start = start;
			this.end = end;
		}

		public Point2D getMEnd() {
			return end;
		}

		public Point2D getMStart() {
			return start;
		}

		public void setMEnd(Point2D end) {
			this.end = end;
		}

		public void setMStart(Point2D start) {
			this.start = start;
		}

		/** the center of the bounding box around start, end */
		public Point2D getMCenter() {
			return new Point2D.Double(
					start.getX() + (end.getX() - start.getX()) / 2,
					start.getY() + (end.getY() - start.getY()) / 2
			);
		}

		public double getMLength() {
			return LinAlg.distance(new Point(start), new Point(end));
		}
		/** The co-ordinates of the new end point
		 * if the length of the seqment is reduced by
		 * the parameter.
		 * */
		public Point2D calculateNewEndPoint(double reduceBy) {
			double xs = this.start.getX();
			double xe = this.end.getX();
			double ys = this.start.getY();
			double ye = this.end.getY();

			double theta = Math.atan2(ye - ys, xe - xs); //angle in polar coordinates
			double r = reduceBy;	//radius in polar coordinates

			double xshift = r*Math.cos(theta);
			double yshift = r*Math.sin(theta);

			xe = xe - xshift;
			ye = ye - yshift;

			return new Point2D.Double(xe,ye);
		}


		/** The co-ordinates of the new start point
		 * if the length of the seqment is reduced by
		 * the parameter. Easier to use the NewEndPoint
		 * method than rewrite it
		 * CAUTION:: may need examining with curved lines, The Math may
		 * not hold
		 * Also caution for elbow lines where the last line
		 * may be smaller than the width of the arrow shape
		 * We may have to put this function in each individual
		 * Connector Shape implementation
		 * */
		public Point2D calculateNewStartPoint(double reduceBy) {
			return calculateNewEndPoint(this.getMLength() - reduceBy);
			//OK for now but CAUTION for curved lines & elbow lines
			//
		}


		public String toString() {
			return start + ", " + end;
		}
	}

	/**
	 * Translates a 1-dimensional line coordinate to a 2-dimensional
	 * view coordinate.
	 * The 1-dimensional line coordinate is position objects that are attached
	 * to the line.
	 */
	public Point2D fromLineCoordinate(double l);

	/**
	 * Translates a 2-dimensional view coordinate to a 1-dimensional
	 * line coordinate.
	 */
	public double toLineCoordinate(Point2D v);
}