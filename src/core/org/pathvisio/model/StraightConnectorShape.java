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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.pathvisio.model.ConnectorShape.Segment;
import org.pathvisio.view.LinAlg;
import org.pathvisio.view.LinAlg.Point;

/**
 * Implements a straight connector Shape, i.e. a Connector with
 * only 90-degree angles.
 */
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



	/**
	 *  Calculate shape from the width of the line endings
	 *  This gets
	 */
	 protected java.awt.Shape calculateShape(Segment[] segments)
	 {
		 Point2D start = segments[0].getMStart();
		 Point2D end = segments[segments.length - 1].getMEnd();
		 return (new Line2D.Double(start,end));
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
