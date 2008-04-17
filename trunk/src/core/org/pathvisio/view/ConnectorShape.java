package org.pathvisio.view;

import java.awt.geom.Point2D;

import org.pathvisio.model.PathwayElement;

/**
 * Implement this to provide a line shape for connectors
 * @author thomas
 *
 */
public interface ConnectorShape {
	
	/**
	 * Get the shape that represents the connector path
	 * @param restrictions The restrictions to be used when creating the path
	 */
	public java.awt.Shape getShape(ConnectorRestrictions restrictions);
	
	/**
	 * Get the individual segments of the path
	 */
	public Segment[] getSegments(ConnectorRestrictions restrictions);
	
	/**
	 * Checks whether the preferred segments are valid and are used
	 * to draw the connector path
	 * @return true if the preferences are used, false if not
	 */
	public boolean isUsePreferredSegments(ConnectorRestrictions restrictions);
	
	/**
	 * A single segment of the connector path.
	 * @author thomas
	 */
	public class Segment {
		public static final int AXIS_X = PathwayElement.MSegment.HORIZONTAL;
		public static final int AXIS_Y = PathwayElement.MSegment.VERTICAL;
		private Point2D start, end;
		
		protected Segment(Point2D start, Point2D end) {
			this.start = start;
			this.end = end;
		}
		
		public Point2D getVEnd() {
			return end;
		}
		
		public Point2D getVStart() {
			return start;
		}
		
		public void setVEnd(Point2D end) {
			this.end = end;
		}
		
		public void setVStart(Point2D start) {
			this.start = start;
		}
		
		public Point2D getVCenter() {
			return new Point2D.Double(
					start.getX() + (end.getX() - start.getX()) / 2,
					start.getY() + (end.getY() - start.getY()) / 2
			);
		}
		
		public double getVLength() {
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
}