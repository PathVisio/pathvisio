package org.pathvisio.model;

import java.awt.Shape;

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
