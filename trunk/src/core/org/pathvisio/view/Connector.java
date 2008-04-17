package org.pathvisio.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.model.ConnectorType;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.PathwayElement.MSegment;
import org.pathvisio.view.ConnectorShape.Segment;

/**
 * A connector connects two pathway elements by drawing a path between the
 * elements. The actual implementation of the paths is done by implementations
 * of the {@link ConnectorShape} interface.
 * @see ConnectorShape
 * @see ConnectorShapeRegistry
 * @author thomas
 *
 */
public class Connector extends Line implements ConnectorRestrictions {
	Graphics startGraphics; //graphics to which the start connects, may be null
	Graphics endGraphics; //graphics to which the end connects, may be null

	ArrayList<Handle> segmentHandles = new ArrayList<Handle>();

	public Connector(VPathway canvas, PathwayElement o) {
		super(canvas, o);
		findConnectingGraphics();
	}

	/**
	 * Find the pathway elements that Sthis connector 
	 * connects to by using the GraphId, GraphRef attributes
	 */
	private void findConnectingGraphics() {
		String startRef = gdata.getStartGraphRef();
		String endRef = gdata.getEndGraphRef();
		if(startRef != null) {
			PathwayElement ms = gdata.getParent().getElementById(startRef);
			setStartGraphics(canvas.getPathwayElementView(ms));
		}
		if(endRef != null) {
			PathwayElement me = gdata.getParent().getElementById(endRef);
			setEndGraphics(canvas.getPathwayElementView(me));
		}
	}

	/**
	 * Set the pathway element that connects to the start
	 * of this connector
	 * @param g
	 */
	private void setStartGraphics(Graphics g) {
		startGraphics = g;
	}

	/**
	 * Set the pathway element that connects to the end
	 * of this connector
	 * @param g
	 */
	private void setEndGraphics(Graphics g) {
		endGraphics = g;
	}

	public SegmentPreference[] getSegmentPreferences() {
		SegmentPreference[] restrictions = null;
		MSegment[] mSegments = gdata.getMSegments();
		if(mSegments != null) {
			restrictions = new SegmentPreference[mSegments.length];
			for(int i = 0; i < mSegments.length; i++) {
				restrictions[i] = new SegmentPreference(
						mSegments[i].getDirection(), vFromM(mSegments[i].getMLength())
				);
			}
		}
		return restrictions;
	}

	/**
	 * Get the side of the given pathway element to which
	 * the x and y coordinates connect
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param g The graphics to find the side of
	 * @return One of the SIDE_* constants
	 */
	private static int getSide(double x, double y, Graphics g) {
		int direction = 0;

		if(g != null) {
			double relX = x - g.getVCenterX();
			double relY = y - g.getVCenterY();
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
		}
		return direction;
	}

	public int getStartSide() {
		if(startGraphics != null) {
			return getSide(getVStartX(), getVStartY(), startGraphics);
		} else {
			return SIDE_EAST;
		}
	}

	public int getEndSide() {
		if(endGraphics != null) {
			return getSide(getVEndX(), getVEndY(), endGraphics);
		} else {
			return SIDE_WEST;
		}
	}

	private String getConnectorType() {
		ConnectorType type = gdata.getConnectorType();
		if(type == null) {
			type = ConnectorType.STRAIGHT;
		}
		return type.getName();
	}
	
	/**
	 * Update the segment handles to be placed on the current
	 * connector segments
	 */
	private void updateSegmentHandles() {
		ConnectorShape cs = ConnectorShapeRegistry.getShape(getConnectorType());
		Segment[] segments = cs.getSegments(this);
		//Destroy and recreate the handles if the number
		//doesn't match the segments number (minus 2, because we don't
		//have handles for the start and end segment
		if(segments.length - 2 != segmentHandles.size()) {
			System.out.println("\tRecreating handles");
			//Destroy the old handles
			for(Handle h : segmentHandles) h.destroy();
			segmentHandles.clear();
			
			//Create the new handles
			if(segments.length > 2) {
				for(int i = 1; i < segments.length - 1; i++) {
					int direction = segments[i].getAxis() == Segment.AXIS_X ? 
							Handle.DIRECTION_SEGMENT_HORIZONTAL : Handle.DIRECTION_SEGMENT_VERTICAL;
					segmentHandles.add(new Handle(direction, this, this.canvas));
				}
			}
		}
		//Put the handles in the right place
		for(int i = 1; i < segments.length - 1; i++) {
			Handle h = segmentHandles.get(i - 1);
			Point2D center = segments[i].getVCenter();
			h.setVLocation(center.getX(), center.getY());
		}
	}

	/**
	 * Updates the segment preferences to the new handle position
	 */
	protected void adjustToHandle(Handle h, double vx, double vy) {
		int index = segmentHandles.indexOf(h) + 1;
		ConnectorShape shape = ConnectorShapeRegistry.getShape(getConnectorType());
		Segment[] segments = shape.getSegments(this);
		
		if(index > -1) {
			MSegment[] mSegments = gdata.getMSegments();
			if(mSegments == null || segments.length != mSegments.length) {
				//Build MSegments from Segments
				mSegments = new MSegment[segments.length];
				for(int i = 0; i < segments.length; i++) {
					mSegments[i] = gdata.new MSegment(
							segments[i].getAxis(), 
							mFromV(segments[i].getVLength())
					);
				}
				gdata.dontFireEvents(1);
				gdata.setMSegments(mSegments);
			}
			
			MSegment currSeg = mSegments[index];
			MSegment prevSeg = mSegments[index - 1];
			MSegment nextSeg = mSegments[index + 1];
			
			//Segment moving vertically
			gdata.dontFireEvents(1);
			if(currSeg.getDirection() == MSegment.HORIZONTAL) {
				double dl = vy - h.getVCenterY();
				prevSeg.setMLength(mFromV(vFromM(prevSeg.getMLength()) + dl));
				nextSeg.setMLength(mFromV(vFromM(nextSeg.getMLength()) - dl));
			} else {
				double dl = vx - h.getVCenterX();
				prevSeg.setMLength(mFromV(vFromM(prevSeg.getMLength()) + dl));
				nextSeg.setMLength(mFromV(vFromM(nextSeg.getMLength()) - dl));
			}
			
			//Reset the segment preferences in the model if they are invalid
			if(!shape.isUsePreferredSegments(this)) {
				gdata.setMSegments(null);
			}
		} else {
			gdata.setMSegments(null);
		}
	}

	public void gmmlObjectModified(PathwayEvent e) {
		super.gmmlObjectModified(e);
		
		findConnectingGraphics();
		updateSegmentHandles();
	}
	
	public void select() {
		super.select();
		if(isSelected()) {
			updateSegmentHandles();
		}
	}
	
	private List<Handle> getSegmentHandles() {
		return segmentHandles;
	}
	
	public Handle[] getHandles() {
		Handle[] lineHandles = super.getHandles();
		ArrayList<Handle> handles = new ArrayList<Handle>();
		for(Handle h : lineHandles) handles.add(h);
		for(Handle h : segmentHandles) handles.add(h);
		return handles.toArray(new Handle[lineHandles.length + segmentHandles.size()]);
	}

	protected void destroyHandles() {
		super.destroyHandles();
		for(Handle h : getSegmentHandles()) {
			h.destroy();
		}
	}
	
	private ConnectorShape getConnectorShape() {
		return ConnectorShapeRegistry.getShape(getConnectorType());
	}

	public void doDraw(Graphics2D g) {
		Color c;
		
		if(isSelected())
		{
			c = selectColor;
		}
		else
		{
			c = gdata.getColor(); 
		}
		g.setColor(c);

		int ls = gdata.getLineStyle();
		if (ls == LineStyle.SOLID) {
			g.setStroke(new BasicStroke());
		}
		else if (ls == LineStyle.DASHED)
		{ 
			g.setStroke	(new BasicStroke (
				  1, 
				  BasicStroke.CAP_SQUARE,
				  BasicStroke.JOIN_MITER, 
				  10, new float[] {4, 4}, 0));
		}			

		Shape l = getConnectorShape().getShape(this);

		ArrowShape[] heads = getVHeads();
		ArrowShape hs = heads[0];
		ArrowShape he = heads[1];

		g.draw(l);
		drawHead(g, he, c);
		drawHead(g, hs, c);
		if (isHighlighted())
		{
			Color hc = getHighlightColor();
			g.setColor(new Color (hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke (new BasicStroke (HIGHLIGHT_STROKE_WIDTH));
			g.draw(l);
			if (he != null) g.draw(he.getShape());
			if (hs != null) g.draw(hs.getShape());
		}
	}

	public Shape mayCross(Point2D point) {
		VPathwayElement elm = canvas.getObjectAt(point);
		Shape shape = null;
		if(elm != null && elm != this) {
			if(elm instanceof Handle) {
				elm = ((Handle)elm).getParent();
			}
			shape = elm.getVOutline();
		}
		return shape;
	}

	public Point2D getStartPoint() {
		return new Point2D.Double(getVStartX(), getVStartY());
	}

	public Point2D getEndPoint() {
		return new Point2D.Double(getVEndX(), getVEndY());
	}

	protected Shape getVOutline() {
		return new BasicStroke(5).createStrokedShape(getVShape(true));
	}
	
	/**
	 * Returns the properly sized and rotated arrowheads
	 * @return An array with two arrowheads, for the start and end respectively
	 */
	protected ArrowShape[] getVHeads() {
		Segment[] segments = getConnectorShape().getSegments(this);
		
		ArrowShape he = getVHead(
				segments[segments.length - 1].getVStart(), 
				segments[segments.length - 1].getVEnd(),
				gdata.getEndLineType()
		);
		ArrowShape hs = getVHead(
				segments[0].getVEnd(),
				segments[0].getVStart(),
				gdata.getStartLineType()
		);
		return new ArrowShape[] { hs, he };
	}
	protected Shape getVShape(boolean rotate) {
		Shape l = getConnectorShape().getShape(this);

		ArrowShape[] heads = getVHeads();
		ArrowShape hs = heads[0];
		ArrowShape he = heads[1];
		
		Area total = new Area(new BasicStroke(1).createStrokedShape(l));
		if(hs != null) {
			total.add(new Area(hs.getShape()));
		}
		if(he != null) {
			total.add(new Area(he.getShape()));
		}
		return total;
	}
}
