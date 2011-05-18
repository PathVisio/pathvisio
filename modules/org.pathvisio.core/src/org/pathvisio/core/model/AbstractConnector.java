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
import java.awt.geom.Point2D;

/**
 * Abstract connectorshape implementation that deals
 * with cached shapes, segments and waypoints.
 * ConnectorShapes may implement this class and use the
 * setShape, setSegments and setWayPoints to refresh the cached shape.
 * @author thomas
 */
public abstract class AbstractConnector implements ConnectorShape {
	private Shape shape;
	private Segment[] segments;
	private WayPoint[] waypoints;

	public Shape getShape() {
		return shape;
	}


	abstract protected Shape calculateShape(Segment[] segments);

	 /**
	  *  Calculate shape from the width of the line endings
	  *
	  */
	 public Shape calculateAdjustedShape(double startGap, double endGap)
	 {
		 //gets the segments to local array
		 Segment[] segments = getSegments();
		 int numSegments=segments.length;
		 Segment[] localsegments = new Segment[numSegments];

		 for (int i=0;i<segments.length;i++)
		 	localsegments[i]=new Segment(segments[i].getMStart(),segments[i].getMEnd());

		 //co-ordinate calculations on start and end segments
		 //make changes on the segment array downloaded
		 Point2D adjustedLineEnd = segments[segments.length - 1].calculateNewEndPoint(endGap);
		 localsegments[numSegments -1] = new Segment(segments[numSegments - 1].getMStart(),adjustedLineEnd);


		 //now for the first segment in the connector shape
		 Point2D adjustedLineStart = segments[0].calculateNewStartPoint(startGap);
		 localsegments[0] = new Segment(adjustedLineStart,localsegments[0].getMEnd());

		 Shape  adjustedShape = calculateShape(localsegments);

		 return adjustedShape;
	 }


	/**
	 * Set the shape cache that will be returned by {@link #getShape()}
	 */
	protected void setShape(Shape shape) {
		this.shape = shape;
	}

	public Segment[] getSegments() {
		return segments;
	}

	/**
	 * Set the segment cache that will be returned by {@link #getSegments()}
	 */
	protected void setSegments(Segment[] segments) {
		this.segments = segments;
	}

	public WayPoint[] getWayPoints() {
		return waypoints;
	}

	/**
	 * Set the waypoints cache that will be returned by {@link #getWayPoints()}
	 */
	public void setWayPoints(WayPoint[] waypoints) {
		this.waypoints = waypoints;
	}
}
