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

/**
 * ConnectorShape implementation for the elbow connector
 * @author thomas
 *
 */
public class ElbowConnectorShape extends SegmentedConnector {
	private final static double SEGMENT_OFFSET = 20;

	public void recalculateShape(ConnectorRestrictions restrictions) {

        WayPoint[] wps = calculateWayPoints(restrictions);
		setSegments(calculateSegments(restrictions, wps));
		setWayPoints(wayPointsToCenter(wps, getSegments()));
		setShape(calculateShape());
	}

	public boolean hasValidWaypoints(ConnectorRestrictions restrictions) {
		//Only check if number of waypoints matches number of segments
        return restrictions.getWayPointPreferences().length == getNrSegments(restrictions) - 2;
	}

	protected WayPoint[] wayPointsToCenter(WayPoint[] waypoints, Segment[] segments) {
		//Set all waypoints to the center of the segments
		for(int i = 1; i < segments.length - 1; i++) {
			waypoints[i - 1].setLocation(segments[i].getMCenter());
		}
		return waypoints;
	}

	protected Shape calculateShape() {
		return calculateShape(getSegments());
	}

	protected WayPoint[] calculateWayPoints(ConnectorRestrictions restrictions) {
		int nrSegments = getNrSegments(restrictions);
		WayPoint[] waypoints = restrictions.getWayPointPreferences();

		//Use the preferred waypoint if it's possible to draw
		//a valid path through them
		if(hasValidWaypoints(restrictions)) {
			return waypoints;
		}

		//Else, calculate the default waypoints
		waypoints = new WayPoint[nrSegments - 2];

		int startSide = restrictions.getStartSide();
		int startAxis = getSegmentAxis(startSide);
		int startDirection = getSegmentDirection(startSide);
		int endSide = restrictions.getEndSide();
		int endAxis = getSegmentAxis(endSide);
		int endDirection = getSegmentDirection(endSide);

		Point2D start = restrictions.getStartPoint();
		Point2D end = restrictions.getEndPoint();

		if(nrSegments - 2 == 1) {
			/*
			 * [S]---
			 * 		|
			 * 		---[S]
			 */
			waypoints[0] = calculateWayPoint(start, end, startAxis, startDirection);
		} else if(nrSegments - 2 == 2) {
			/*
			* [S]---
			* 		| [S]
			* 		|  |
			* 		|---
			*/
			waypoints[0] = calculateWayPoint(start, new Point2D.Double(
					end.getX() + SEGMENT_OFFSET * endDirection, end.getY() + SEGMENT_OFFSET * endDirection
													), startAxis, startDirection);

			waypoints[1] = calculateWayPoint(end, waypoints[0], endAxis, endDirection);
		} else if(nrSegments - 2 == 3) {
			/*  -----
			 *  |   |
			 * [S]  | [S]
			 *      |  |
			 *      |---
			 */
			//Start with middle waypoint
			waypoints[1] = new WayPoint(new Point2D.Double(
					start.getX() + (end.getX() - start.getX()) / 2,
					start.getY() + (end.getY() - start.getY()) / 2
			));
			waypoints[0] = calculateWayPoint(start, waypoints[1], startAxis, startDirection);
			waypoints[2] = calculateWayPoint(end, waypoints[1], endAxis, endDirection);
		}
		return waypoints;
	}

	protected WayPoint calculateWayPoint(Point2D start, Point2D end, int axis, int direction) {
		double x,y = 0;
		if(axis == AXIS_Y) {
			x = start.getX() + (end.getX() - start.getX()) / 2;
			y = start.getY() + SEGMENT_OFFSET * direction;
		} else {
			x = start.getX() + SEGMENT_OFFSET * direction;
			y = start.getY() + (end.getY() - start.getY()) / 2;
		}
		return new WayPoint(x, y);
	}

	protected Segment[] calculateSegments(ConnectorRestrictions restrictions, WayPoint[] waypoints) {
		int nrSegments = getNrSegments(restrictions);
		Segment[] segments = new Segment[nrSegments];

		Point2D start = restrictions.getStartPoint();
		Point2D end = restrictions.getEndPoint();
		int startAxis = getSegmentAxis(restrictions.getStartSide());
		if(nrSegments == 2) { //No waypoints
			segments[0] = createStraightSegment(start, end, startAxis);
			segments[1] = createStraightSegment(segments[0].getMEnd(), end, getOppositeAxis(startAxis));
		} else {
			segments[0] = createStraightSegment(
					restrictions.getStartPoint(),
					waypoints[0],
					startAxis
			);
			int axis = getOppositeAxis(startAxis);
			for(int i = 0; i < waypoints.length - 1; i++) {
				segments[i + 1] = createStraightSegment(
						segments[i].getMEnd(),
						waypoints[i + 1],
						axis
				);
				axis = getOppositeAxis(axis);
			}
			segments[segments.length - 2] = createStraightSegment(
					segments[segments.length -3].getMEnd(),
					end,
					axis
			);
			segments[segments.length - 1] = createStraightSegment(
					segments[segments.length - 2].getMEnd(),
					end,
					getSegmentAxis(restrictions.getEndSide())
			);
		}
		setWayPoints(waypoints);
		return segments;
	}

	protected Segment createStraightSegment(Point2D start, Point2D end, int axis) {
		double ex = end.getX();
		double ey = end.getY();
		if(axis == AXIS_X) {
			ey = start.getY();
		} else {
			ex = start.getX();
		}
		return new Segment(start, new Point2D.Double(ex, ey));
	}

	private int getOppositeAxis(int axis) {
		return axis == ConnectorShape.AXIS_X ? AXIS_Y : AXIS_X;
	}

	private int getSegmentDirection(int side) {
		switch(side) {
		case ConnectorRestrictions.SIDE_EAST:
		case ConnectorRestrictions.SIDE_SOUTH:
			return 1;
		case ConnectorRestrictions.SIDE_NORTH:
		case ConnectorRestrictions.SIDE_WEST:
			return -1;
		}
		return 0;
	}

	private int getSegmentAxis(int side) {
		switch(side) {
		case ConnectorRestrictions.SIDE_EAST:
		case ConnectorRestrictions.SIDE_WEST:
			return AXIS_X;
		case ConnectorRestrictions.SIDE_NORTH:
		case ConnectorRestrictions.SIDE_SOUTH:
			return AXIS_Y;
		}
		return 0;
	}

	/* The number of connector for each side and relative position
		RN	RE	RS	RW
BLN		1	2	1	0
TLN		1	2	3	2

BLE		3	1	0	1
TLE		0	1	2	1

BLS		3	2	1	2
TLS		1	2	1	0

BLW		2	3	2	1
TLW		2	3	2	1
	There should be some logic behind this, but hey, it's Friday...
	(so we just hard code the array)
	 */
	private int[][][] waypointNumbers;

	private int getNrWaypoints(int x, int y, int z) {
//		if(waypointNumbers == null) {
		waypointNumbers = new int[][][] {
				new int[][] {
						new int[] { 1, 1 },
						new int[] { 2, 2 },
						new int[] { 1, 3 },
						new int[] { 0, 2 }
				},
				new int[][] {
						new int[] { 2, 0 },
						new int[] { 1, 1 },
						new int[] { 0, 2 },
						new int[] { 1, 1 },
				},
				new int[][] {
						new int[] { 3, 1 },
						new int[] { 2, 2 },
						new int[] { 1, 1 },
						new int[] { 2, 0 },
				},
				new int[][] {
						new int[] { 2, 2 },
						new int[] { 3, 3 },
						new int[] { 2, 2 },
						new int[] { 1, 1 },
				}
		};
//		}
		return waypointNumbers[x][y][z];
	}

	/**
	 * Get the direction of the line on the x axis
	 * @param start The start point of the line
	 * @param end The end point of the line
	 * @return 1 if the direction is positive (from left to right),
	 * -1 if the direction is negative (from right to left)
	 */
	int getDirectionX(Point2D start, Point2D end) {
		return (int)Math.signum(end.getX() - start.getX());
	}

	/**
	 * Get the direction of the line on the y axis
	 * @param start The start point of the line
	 * @param end The end point of the line
	 * @return 1 if the direction is positive (from top to bottom),
	 * -1 if the direction is negative (from bottom to top)
	 */
	protected int getDirectionY(Point2D start, Point2D end) {
		return (int)Math.signum(end.getY() - start.getY());
	}

	protected int getNrSegments(ConnectorRestrictions restrictions) {

        Point2D start = restrictions.getStartPoint();
		Point2D end = restrictions.getEndPoint();

		boolean leftToRight = getDirectionX(start, end) > 0;

		Point2D left = leftToRight ? start : end;
		Point2D right = leftToRight ? end : start;
		boolean leftBottom = getDirectionY(left, right) < 0;

		int z = leftBottom ? 0 : 1;
		int x = leftToRight ? restrictions.getStartSide() : restrictions.getEndSide();
		int y = leftToRight ? restrictions.getEndSide() : restrictions.getStartSide();
		return getNrWaypoints(x, y, z) + 2;
	}

    private boolean isPointOnSegment(Double p1, Double p2, Double point) {
        double offset = 50.0;
        if (p1 < p2 && (p1 - offset) < point && (point < (p2 + offset))) {
            return true;
        } else if (p2 < p1 && (p2 - offset) < point && (point < (p1 + offset))) {
            return true;
        }
        return false;
    }
}
