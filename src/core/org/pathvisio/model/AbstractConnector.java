package org.pathvisio.model;

import java.awt.Shape;

/**
 * Abstract connectorshape implementation that deals 
 * with cached shapes and segments.
 * ConnectorShapes may implement this class and use the
 * setShape and setSegments to refresh the cached shape.
 * @author thomas
 */
public abstract class AbstractConnector implements ConnectorShape {
	private Shape shape;
	private Segment[] segments;
	
	public Shape getShape() {
		return shape;
	}
	
	protected void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public Segment[] getSegments() {
		return segments;
	}
	
	protected void setSegments(Segment[] segments) {
		this.segments = segments;
	}
	
}
