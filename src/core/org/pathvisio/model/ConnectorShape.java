package org.pathvisio.model;

import java.awt.geom.Point2D;


/**
 * Implement this to provide a line shape for connectors
 * @author thomas
 *
 */
public interface ConnectorShape {
	
	public void recalculateShape(ConnectorRestrictions restrictions);
	
	/**
	 * Get the shape that represents the connector path
	 */
	public java.awt.Shape getShape();
	
	/**
	 * Get the individual segments of the path
	 */
	public Segment[] getSegments();
	
	/**
	 * Checks whether the waypoints as provided by the ConnectorRestrictions
	 * are valid and will be used to draw the connector path
	 * @return true if the waypoints are used, false if not
	 */
	public boolean hasValidWaypoints(ConnectorRestrictions restrictions);
	
	/**
	 * A single segment of the connector path.
	 * @author thomas
	 */
	public class Segment {
		public static final int AXIS_X = 0;
		public static final int AXIS_Y = 1;
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
		
		public Point2D getMCenter() {
			return new Point2D.Double(
					start.getX() + (end.getX() - start.getX()) / 2,
					start.getY() + (end.getY() - start.getY()) / 2
			);
		}
		
		public double getMLength() {
			if(getAxis() == AXIS_X) {
				return end.getX() - start.getX();
			} else {
				return end.getY() - start.getY();
			}
		}
		
		public int getAxis() {
			if(start.getX() != end.getX()) 
				return Segment.AXIS_X;
			else 
				return Segment.AXIS_Y;
		}
	}

	/**
	 * Translates a 1-dimensional line coordinate to a 2-dimensional
	 * view coordinate.
	 */
	public Point2D fromLineCoordinate(double l);
	
	/**
	 * Translates a 2-dimensional view coordinate to a 1-dimensional
	 * line coordinate.
	 */
	public double toLineCoordinate(Point2D v);
}