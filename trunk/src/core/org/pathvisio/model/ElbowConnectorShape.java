package org.pathvisio.model;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.pathvisio.debug.Logger;
import org.pathvisio.view.LinAlg;
import org.pathvisio.view.LinAlg.Point;

/**
 * ConnectorShape implementation for the elbow connector
 * @author thomas
 *
 */
public class ElbowConnectorShape extends AbstractConnector {
	private final static double SEGMENT_OFFSET = 20 * 15;

	public void recalculateShape(ConnectorRestrictions restrictions) {
		setSegments(calculateSegments(restrictions));
		setShape(calculateShape(getSegments()));
	}
	
	public boolean hasValidWaypoints(ConnectorRestrictions restrictions) {
		return false;
	}
	
	public Shape calculateShape(Segment[] segments) {
		GeneralPath path = new GeneralPath();
		int i = 0;
		for(Segment s : segments) {
			i++;
			if(s == null) { //Ignore null segments
				Logger.log.error("Null segment in connector!");
				continue;
			}
			path.moveTo((float)s.getMStart().getX(), (float)s.getMStart().getY());
			path.lineTo((float)s.getMEnd().getX(), (float)s.getMEnd().getY());
		}
		return path;
	}
		
	public Segment[] calculateSegments(ConnectorRestrictions restrictions) {
		//Ok, we want to get the exact segments that form the connector
		//while trying to honor the preferred segments
		
		int nrSegments = getNrSegments(restrictions);
		
		//Otherwise, calculate new segments
		Segment[] segments = new Segment[nrSegments];
			
		//Start with the first segment
		int axis = getSegmentAxis(restrictions.getStartSide());
		Segment first = new Segment(
				restrictions.getStartPoint(),
				movePoint(
						restrictions.getStartPoint(), 
						axis, 
						SEGMENT_OFFSET * getSegmentDirection(restrictions.getStartSide())
				)
		);
		
		segments[0] = first;
		
		//And the default last segment
		axis = getSegmentAxis(restrictions.getEndSide());
		Segment last = new Segment(
				movePoint(
						restrictions.getEndPoint(), 
						axis, 
						SEGMENT_OFFSET * getSegmentDirection(restrictions.getEndSide())
				), 
				restrictions.getEndPoint()
		);
		
		segments[segments.length - 1] = last;
		
		if(nrSegments - 2 == 1) {
			/*
			 * [S]---
			 * 		|
			 * 		---[S]
			 */
			//Move from the end of the first segment
			axis = getOppositeAxis(first.getAxis());
			segments[1] = new Segment(
					first.getMEnd(),
					movePoint(
							first.getMEnd(),
							axis,
							calculateSegmentLength(first.getMEnd(), last.getMStart(), axis)
					)
			);
						
			//Extend the last segment to connect with the end of the middle segment
			last.setMStart((Point2D)segments[1].getMEnd().clone());
		} else if(nrSegments - 2 == 2) {
//			/*
//			 * [S]---
//			 * 		| [S]
//			 * 		|  |
//			 * 		|---
//			 */
			axis = getOppositeAxis(last.getAxis());
			segments[2] = new Segment(
					movePoint(
							last.getMStart(),
							axis,
							- calculateSegmentLength(first.getMEnd(), last.getMStart(), axis)
					),
					last.getMStart()
			);
			axis = getOppositeAxis(first.getAxis());
			segments[1] = new Segment(
					first.getMEnd(),
					movePoint(
								first.getMEnd(), 
								axis,
								calculateSegmentLength(first.getMEnd(), segments[2].getMStart(), axis) 
					)
			);
		} else if(nrSegments - 2 == 3) {
//			/*  ----- 
//			 *  |   |
//			 * [S]  | [S]
//			 *      |  |
//			 *      |---
//			 */
			//Calculate middle segment
			Point2D start = restrictions.getStartPoint();
			Point2D end = restrictions.getEndPoint();
			Point2D middle = new Point2D.Double(
					start.getX() + (end.getX() - start.getX()) / 2,
					start.getY() + (end.getY() - start.getY()) / 2
			);
			axis = first.getAxis();
			double length = calculateSegmentLength(first.getMEnd(), last.getMStart(), axis);
			segments[2] = new Segment(
					movePoint(middle, axis, -length/2),
					movePoint(middle, axis, length/2)
			);
			segments[1] = new Segment(first.getMEnd(), segments[2].getMStart());
			segments[3] = new Segment(segments[2].getMEnd(), last.getMStart());
		} else {
			/* [S]----
			 *       |
			 *      [S]
			 */
			first.setMEnd(
					movePoint(
							first.getMEnd(),
							first.getAxis(),
							calculateSegmentLength(first.getMEnd(), last.getMStart(), first.getAxis())
					)
			);
			last.setMStart((Point2D)first.getMEnd().clone());			
		}
		
		return segments;
	}
	
	private double calculateSegmentLength(Point2D from, Point2D to, int axis) {
		double length;
		if(axis == Segment.AXIS_X) {
			length = to.getX() - from.getX();
		} else {
			length = to.getY() - from.getY();
		}
		return length;
	}
	
	private int getOppositeAxis(int axis) {
		return axis == Segment.AXIS_X ? Segment.AXIS_Y : Segment.AXIS_X;
	}
	
	private Point2D movePoint(Point2D point, int axis, double length) {
		if(axis == Segment.AXIS_X) 
			return new Point2D.Double(point.getX() + length, point.getY());
		else 
			return new Point2D.Double(point.getX(), point.getY() + length);
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
			return Segment.AXIS_X;
		case ConnectorRestrictions.SIDE_NORTH:
		case ConnectorRestrictions.SIDE_SOUTH:
			return Segment.AXIS_Y;
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
	int getDirectionY(Point2D start, Point2D end) {
		return (int)Math.signum(end.getY() - start.getY());
	}
	
	int getNrSegments(ConnectorRestrictions restrictions) {
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

	public Point2D fromLineCoordinate(double l) {
		//Calculate the total segment length
		Segment[] segments = getSegments();
		double length = 0;
		for(Segment s : segments) {
			length += Math.abs(s.getMLength());
		}
		
		//Find the right segment
		double end = 0;
		Segment segment = null;
		int i = 0;
		for(Segment s : segments) {
			double slength = Math.abs(s.getMLength());
			end += slength;
			double ls = (end - slength) / length;
			double le = end / length;
			if(l >= ls && l <= le) {
				segment = s;
				break;
			}
			i++;
		}
		if(segment == null) segment = segments[segments.length - 1];
		
		//Find the location on the segment
		double slength = Math.abs(segment.getMLength());
		double leftover = (l - (end - slength) / length) * length;
		double relative = leftover / slength;
		Point2D position = null;
		if(segment.getAxis() == Segment.AXIS_X) {
			position = new Point2D.Double(
				segment.getMStart().getX() + segment.getMLength() * relative,
				segment.getMStart().getY()
			);
		} else {
			position = new Point2D.Double(
				segment.getMStart().getX(),
				segment.getMStart().getY() + segment.getMLength() * relative
			);
		}
		return position;
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
