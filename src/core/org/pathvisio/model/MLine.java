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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.model.ConnectorShape.WayPoint;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.util.Utils;

/**
 * MLine - basically a PathwayElement, but overrides some methods
 * to calculate coordinates dynamically. For example getMCenterX().
 *
 * For other shapes, the centerX coordinate is stored in GPML. Lines however
 * store the end-points, the center can be calculated based on that.
 */
public class MLine extends PathwayElement implements ConnectorRestrictions {
	ConnectorShape shape;

	public MLine() {
		super(ObjectType.LINE);
	}

	/**
	 * the Connector Shape for this line - the connector shape
	 * can calculate a Shape based on the connector type (straight, elbow or curved)
	 * and possibly way points
	 */
	public ConnectorShape getConnectorShape() {
		String type = getConnectorType().getName();

		//Recreate the ConnectorShape when it's null or when the type
		//doesn't match the implementing class
		if(shape == null || !shape.getClass().equals(ConnectorShapeFactory.getImplementingClass(type))) {
			shape = ConnectorShapeFactory.createConnectorShape(getConnectorType().getName());
			shape.recalculateShape(this);
		}
		return shape;
	}

	/**
	 * returns the center x coordinate of the bounding box around (start, end)
	 */
	public double getMCenterX()
	{
		double start = getMStart().getX();
		double end = getMEnd().getX();
		return start + (end - start) / 2;
	}

	/**
	 * returns the center y coordinate of the bounding box around (start, end)
	 */
	public double getMCenterY()
	{
		double start = getMStart().getY();
		double end = getMEnd().getY();
		return start + (end - start) / 2;
	}

	/**
	 * returns the left x coordinate of the bounding box around (start, end)
	 */
	public double getMLeft()
	{
		double start = getMStart().getX();
		double end = getMEnd().getX();
		return Math.min(start, end);
	}

	/**
	 * returns the width of the bounding box around (start, end)
	 */
	public double getMWidth()
	{
		double start = getMStart().getX();
		double end = getMEnd().getX();
		return Math.abs(start-end);
	}

	/**
	 * returns the height of the bounding box around (start, end)
	 */
	public double getMHeight()
	{
		double start = getMStart().getY();
		double end = getMEnd().getY();
		return Math.abs(start-end);
	}

	/**
	 * returns the top y coordinate of the bounding box around (start, end)
	 */
	public double getMTop()
	{
		double start = getMStart().getY();
		double end = getMEnd().getY();
		return Math.min(start, end);
	}

	/**
	 * Sets the position of the top side
	 * of the rectangular bounds of the line
	 */
	public void setMTop(double v) {
		if(getDirectionY() > 0) {
			setMStartY(v);
		} else {
			setMEndY(v);
		}
	}

	/**
	 * Sets the position of the left side
	 * of the rectangular bounds of the line
	 */
	public void setMLeft(double v) {
		if(getDirectionX() > 0) {
			setMStartX(v);
		} else {
			setMEndX(v);
		}
	}

	/**
	 * Sets the x position of the center of the line. This makes
	 * the line move as a whole
	 */
	public void setMCenterX(double v) {
		double dx = v - getMCenterX();
		setMStartX(getMStartX() + dx);
		setMEndX(getMEndX() + dx);
	}

	/**
	 * Sets the y position of the center of the line. This makes the line
	 * move as a whole.
	 */
	public void setMCenterY(double v) {
		double dy = v - getMCenterY();
		setMStartY(getMStartY() + dy);
		setMEndY(getMEndY() + dy);
	}


	/** returns the sign of end.x - start.x */
	private int getDirectionX() {
		return (int)Math.signum(getMEndX() - getMStartX());
	}

	/** returns the sign of end.y - start.y */
	private int getDirectionY() {
		return (int)Math.signum(getMEndY() - getMStartY());
	}

	/** converts end point from MPoint to Point2D */
	public Point2D getEndPoint() {
		return getMEnd().toPoint2D();
	}

	/** converts start point from MPoint to Point2D */
	public Point2D getStartPoint() {
		return getMStart().toPoint2D();
	}

	/** converts all points from MPoint to Point2D */
	public List<Point2D> getPoints() {
		List<Point2D> pts = new ArrayList<Point2D>();
		for(MPoint p : getMPoints()) {
			pts.add(p.toPoint2D());
		}
		return pts;
	}

	/**
	 * Returns the element that the start of this line is connected to. Returns null if there isn't any.
	 */
	private GraphIdContainer getStartElement() {
		Pathway parent = getParent();
		if(parent != null) {
			return parent.getGraphIdContainer(getStartGraphRef());
		}
		return null;
	}

	/**
	 * Returns the element that the end of this line is connected to. Returns null if there isn't any.
	 */
	private GraphIdContainer getEndElement() {
		Pathway parent = getParent();
		if(parent != null) {
			return parent.getGraphIdContainer(getEndGraphRef());
		}
		return null;
	}

	/**
	 * Calculate on which side of a PathwayElement (SIDE_NORTH, SIDE_EAST, SIDE_SOUTH or SIDE_WEST)
	 * the start of this line is connected to.
	 *
	 * If the start is not connected to anything, returns SIDE_WEST
	 */
	public int getStartSide() {
		int side = SIDE_WEST;

		GraphIdContainer e = getStartElement();
		if(e != null) {
			if(e instanceof PathwayElement) {
				side = getSide(getMStart().getRelX(), getMStart().getRelY());
			} else if(e instanceof MAnchor) {
                side= getAttachedLineDirection((MAnchor)e);
            }
		}
		return side;
	}

	/**
	 * Calculate on which side of a PathwayElement (SIDE_NORTH, SIDE_EAST, SIDE_SOUTH or SIDE_WEST)
	 * the end of this line is connected to.
	 *
	 * If the end is not connected to anything, returns SIDE_EAST
	 */
	public int getEndSide() {
		int side = SIDE_EAST;

		GraphIdContainer e = getEndElement();
		if(e != null) {
			if(e instanceof PathwayElement) {
				side = getSide(getMEnd().getRelX(), getMEnd().getRelY());
			} else if(e instanceof MAnchor) {
                side= getAttachedLineDirection((MAnchor)e);
            }
		}
		return side;
	}

    private int getAttachedLineDirection(MAnchor anchor) {
        int side;
        double pos = anchor.getPosition();
        MLine attLine = ((MLine)anchor.getParent());
        if (attLine.getConnectorShape() instanceof ElbowConnectorShape) {
            ConnectorShape.Segment attSeg = findAnchorSegment(attLine, pos);
            int orientationX = Utils.getDirectionX(attSeg.getMStart(), attSeg.getMEnd());
            int orientationY = Utils.getDirectionY(attSeg.getMStart(), attSeg.getMEnd());
            side = getSide(orientationY, orientationX);
        } else {
            side = getOppositeSide(getSide(getMEndX(), getMEndY(), getMStartX(), getMStartY()));
            if (attLine.almostPerfectAlignment(side)) {
                side = getClockwisePerpendicularSide(side);
            }
        }
        return side;
    }

    private ConnectorShape.Segment findAnchorSegment(MLine attLine, double pos) {
        ConnectorShape.Segment[] segments = attLine.getConnectorShape().getSegments();
        Double totLength = 0.0;
        ConnectorShape.Segment attSeg = null;
        for (ConnectorShape.Segment segment:segments) {
            totLength = totLength + segment.getMLength();
        }
        Double currPos;
        Double segSum = 0.0;
        for (ConnectorShape.Segment segment:segments) {
            segSum = segSum + segment.getMLength();
            currPos = segSum / totLength;
            attSeg = segment;
            if (currPos > pos) {
                break;
            }
        }
        return attSeg;
    }

    /**
     * Check if either the line segment has less than or equal to
     * 10 degree alignment with the side passed
     * @param startLine
     * @param endLine
     * @return true if <= 10 degree alignment
     * else false
     */
    private boolean almostPerfectAlignment(int side){
        int MAXOFFSET = 30; /* cutoff point where we see a shallow
        	angle still as either horizontal or vertical */
                // X axis
        if ((side == SIDE_EAST) || (side == SIDE_WEST)) {
            double angleDegree = (180/Math.PI)*Math.atan2(Math.abs(getStartPoint().getY() - getEndPoint().getY()), Math.abs(getStartPoint().getX() - getEndPoint().getX()));
            if (angleDegree <= MAXOFFSET)
                return true;
        } else {//north south or Y axis
            double angleDegree = (180/Math.PI)*Math.atan2(Math.abs(getStartPoint().getX() - getEndPoint().getX()), Math.abs(getStartPoint().getY() - getEndPoint().getY()));
            if (angleDegree <= MAXOFFSET)
                return true;
        }
        return false;
    }

	/**
	 * Returns the Perpendicular for a SIDE_* constant (e.g. SIDE_EAST <-> SIDE_WEST)
	 */
	private int getClockwisePerpendicularSide(int side) {
	    switch(side) {
	    case SIDE_EAST:
	        return SIDE_SOUTH;
	    case SIDE_WEST:
	        return SIDE_NORTH;
	    case SIDE_NORTH:
	        return SIDE_EAST;
	    case SIDE_SOUTH:
	        return SIDE_WEST;
	    }
	    return -1;
	}


    public void adjustWayPointPreferences(WayPoint[] waypoints) {
		List<MPoint> mpoints = getMPoints();
		for(int i = 0; i < waypoints.length; i++) {
			WayPoint wp = waypoints[i];
			MPoint mp = mpoints.get(i + 1);
			if(mp.getX() != wp.getX() || mp.getY() != wp.getY()) {
				dontFireEvents(1);
				mp.moveTo(wp.getX(), wp.getY());
			}
		}
	}

	public void resetWayPointPreferences() {
		List<MPoint> mps = getMPoints();
		while(mps.size() > 2) {
			mps.remove(mps.size() - 2);
		}
	}

	/**
	 * Get the preferred waypoints, to which the connector must draw
	 * it's path. The waypoints returned by this method are preferences
	 * and the connector shape may decide not to use them if they are invalid.
	 */
	public WayPoint[] getWayPointPreferences() {
		List<MPoint> pts = getMPoints();
		WayPoint[] wps = new WayPoint[pts.size() - 2];
		for(int i = 0; i < wps.length; i++) {
			wps[i] = new WayPoint(pts.get(i + 1).toPoint2D());
		}
		return wps;
	}

	/**
	 * Get the side of the given pathway element to which
	 * the x and y coordinates connect
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param e The element to find the side of
	 * @return One of the SIDE_* constants
	 */
	private static int getSide(double x, double y, double cx, double cy) {
		return getSide(x - cx, y - cy);
	}

	private static int getSide(double relX, double relY) {
		int direction = 0;
		if(Math.abs(relX) > Math.abs(relY)) {
			if(relX > 0) {
				direction = SIDE_EAST;
			} else {
				direction = SIDE_WEST;
			}
		} else {
			if(relY > 0) {
				direction = SIDE_SOUTH;
			} else {
				direction = SIDE_NORTH;
			}
		}
		return direction;
	}

	/**
	 * Returns the opposite for a SIDE_* constant (e.g. SIDE_EAST <-> SIDE_WEST)
	 */
	private int getOppositeSide(int side) {
		switch(side) {
		case SIDE_EAST:
			return SIDE_WEST;
		case SIDE_WEST:
			return SIDE_EAST;
		case SIDE_NORTH:
			return SIDE_SOUTH;
		case SIDE_SOUTH:
			return SIDE_NORTH;
		}
		return -1;
	}

	/**
	 * Check if the connector may cross this point
	 * Optionally, returns a shape that defines the boundaries of the area  around this
	 * point that the connector may not cross.
	 * This method can be used for advanced connectors that route along other objects
	 * on the drawing
	 * @return A shape that defines the boundaries of the area around this point that
	 * the connector may not cross. Returning null is allowed for implementing classes.
	 */
	public Shape mayCross(Point2D point) {
		Pathway parent = getParent();
		Rectangle2D rect = null;
		if(parent != null) {
			for(PathwayElement e : parent.getDataObjects()) {
				ObjectType ot = e.getObjectType();
				if(ot == ObjectType.SHAPE || ot == ObjectType.DATANODE || ot == ObjectType.LABEL) {
					Rectangle2D b = e.getMBounds();
					if(b.contains(point)) {
						if(rect == null) rect = b;
						else rect.add(b);
					}
				}
			}
		}
		return rect;
	}
}
