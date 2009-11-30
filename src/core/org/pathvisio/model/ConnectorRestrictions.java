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

import java.awt.Shape;
import java.awt.geom.Point2D;

import org.pathvisio.model.ConnectorShape.WayPoint;

/**
 * Methods to provide restrictions for the connector path
 */
public interface ConnectorRestrictions {

	/** line is connected to a PathwayElement on it's NORTH side. */
	public final static int SIDE_NORTH = 0;
	/** line is connected to a PathwayElement on it's EAST side. */
	public final static int SIDE_EAST = 1;
	/** line is connected to a PathwayElement on it's SOUTH side. */
	public final static int SIDE_SOUTH = 2;
	/** line is connected to a PathwayElement on it's WEST side. */
	public final static int SIDE_WEST = 3;

	/**
	 * Check if the connector may cross this point
	 * Optionally, returns a shape that defines the boundaries of the area  around this
	 * point that the connector may not cross.
	 * This method can be used for advanced connectors that route along other objects
	 * on the drawing
	 * @return A shape that defines the boundaries of the area around this point that
	 * the connector may not cross. Returning null is allowed for implementing classes.
	 */
	Shape mayCross(Point2D point);

	/**
	 * Get the side of the object to which the start of the connector connects
	 * @return The side, one of the SIDE_* constants
	 */
	int getStartSide();

	/**
	 * Get the side of the object to which the end of the connector connects
	 * @return The side, one of the SIDE_* constants
	 */
	int getEndSide();

	/**
	 * Get the start point to which the connector must connect
	 * @return
	 */
	Point2D getStartPoint();

	/**
	 * Get the end point to which the connector must connect
	 * @return
	 */
	Point2D getEndPoint();

	/**
	 * Get the preferred waypoints, to which the connector must draw
	 * it's path. The waypoints returned by this method are preferences
	 * and the connector shape may decide not to use them if they are invalid.
	 */
	WayPoint[] getWayPointPreferences();
}
