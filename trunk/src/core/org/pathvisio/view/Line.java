// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pathvisio.model.ConnectorShape;
import org.pathvisio.model.ConnectorShapeFactory;
import org.pathvisio.model.ConnectorType;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.MLine;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.ConnectorShape.Segment;
import org.pathvisio.model.ConnectorShape.WayPoint;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;
 
/**
 * This class represents a line on the pathway.
 * The actual implementation of the path is done by implementations
 * of the {@link ConnectorShape} interface.
 * @see ConnectorShape
 * @see ConnectorShapeFactory
 */
public class Line extends Graphics
{	
	private static final long serialVersionUID = 1L;
	
	private List<VPoint> points;
	private Map<MAnchor, VAnchor> anchors = new HashMap<MAnchor, VAnchor>();
	
	ArrayList<Handle> segmentHandles = new ArrayList<Handle>();

	ConnectorShape shape;
	
	/**
	 * Constructor for this class
	 * @param canvas - the VPathway this line will be part of
	 */
	public Line(VPathway canvas, PathwayElement o)
	{
		super(canvas, o);
		
		points = new ArrayList<VPoint>();
		addPoint(o.getMStart());
		addPoint(o.getMEnd());
		setAnchors();
		getConnectorShape().recalculateShape(getMLine());
		updateSegmentHandles();
	}
	
	private void addPoint(MPoint mp) {
		VPoint vp = canvas.getPoint(mp);
		points.add(vp);
		vp.addLine(this);
		vp.setHandleLocation();
	}
	
	private MLine getMLine() {
		return (MLine)gdata;
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
		ConnectorShape cs = getConnectorShape();
		WayPoint[] waypoints = cs.getWayPoints();
		
		//Destroy and recreate the handles if the number
		//doesn't match the waypoints number
		if(waypoints.length != segmentHandles.size()) {
			//Destroy the old handles
			for(Handle h : segmentHandles) h.destroy();
			segmentHandles.clear();
			
			//Create the new handles
			for(int i = 0; i < waypoints.length; i++) {
				Handle h = new Handle(Handle.DIRECTION_FREE, this, this.canvas);
				h.setStyle(Handle.STYLE_SEGMENT);
				segmentHandles.add(h);
			}
		}
		//Put the handles in the right place
		for(int i = 0; i < waypoints.length; i++) {
			Handle h = segmentHandles.get(i);
			h.setMLocation(waypoints[i].getX(), waypoints[i].getY());
		}
	}

	/**
	 * Updates the segment preferences to the new handle position
	 */
	protected void adjustToHandle(Handle h, double vx, double vy) {
		WayPoint[] waypoints = getConnectorShape().getWayPoints();
		int index = segmentHandles.indexOf(h);
		if(index > -1) {
			List<MPoint> points = gdata.getMPoints();
			if(points.size() - 2 != (waypoints.length)) {
				//Recreate points from segments
				points = new ArrayList<MPoint>();
				points.add(gdata.getMStart());
				for(int i = 0; i < waypoints.length; i++) {
					MPoint p = gdata.new MPoint(waypoints[i].getX(), waypoints[i].getY());
					points.add(p);
				}
				points.add(gdata.getMEnd());
				gdata.dontFireEvents(1);
				gdata.setMPoints(points);
			}
			points.get(index + 1).moveTo(mFromV(vx), mFromV(vy));
		}
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
	
	private ConnectorShape getConnectorShape() {
		String type = gdata.getConnectorType().getName();
		
		//Recreate the ConnectorShape when it's null or when the type
		//doesn't match the implementing class
		if(shape == null || !shape.getClass().equals(ConnectorShapeFactory.getImplementingClass(type))) {
			System.out.println("Creating connector " + type);
			shape = ConnectorShapeFactory.createConnectorShape(gdata.getConnectorType().getName());
			shape.recalculateShape(getMLine());
		}
		return shape;
	}

	/**
	 * Get the connector shape translated to view coordinates
	 */
	private Shape getVConnector() {
		Shape s = getConnectorShape().getShape();
		AffineTransform t = new AffineTransform();
		double scale = vFromM(1);
		t.setToScale(scale, scale);
		return t.createTransformedShape(s);
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

		Shape l = getVConnector();

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

	/**
	 * Be careful to prevent infinite recursion when
	 * Line.getVOutline triggers recalculation of a connector.
	 * 
	 * For now, only check crossing of geneproducts and shapes.
	 */
	public Shape mayCross(Point2D point) 
	{
		Shape shape = null;
		for (VPathwayElement o : canvas.getDrawingObjects())
		{
			if (o instanceof GeneProduct ||
					o instanceof Shape)
				if (o.vContains(point))
				{
					shape = o.getVOutline();
				}
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
		Segment[] segments = getConnectorShape().getSegments();
		
		ArrowShape he = getVHead(
				segments[segments.length - 1].getMStart(), 
				segments[segments.length - 1].getMEnd(),
				gdata.getEndLineType()
		);
		ArrowShape hs = getVHead(
				segments[0].getMEnd(),
				segments[0].getMStart(),
				gdata.getStartLineType()
		);
		return new ArrowShape[] { hs, he };
	}
	
	protected Shape getVShape(boolean rotate) {
		Shape l = getVConnector();

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
	private void setAnchors() {
		//Check for new anchors
		List<MAnchor> manchors = gdata.getMAnchors();
		for(MAnchor ma : manchors) {
			if(!anchors.containsKey(ma)) {
				anchors.put(ma, new VAnchor(ma, this));
			}
		}
		//Check for deleted anchors
		for(MAnchor ma : anchors.keySet()) {
			if(!manchors.contains(ma)) {
				anchors.get(ma).destroy();
			}
		}
	}
	
	void removeVAnchor(VAnchor va) {
		anchors.remove(va.getMAnchor());
		gdata.removeMAnchor(va.getMAnchor());
	}
	
	private void updateAnchorPositions() {
		for(VAnchor va : anchors.values()) {
			va.updatePosition();
		}
	}
	
	protected void swapPoint(VPoint pOld, VPoint pNew) 
	{
		int i = points.indexOf(pOld);
		if(i > -1) {
			points.remove(pOld);
			points.add(i, pNew);
		}
	}
	
	protected void drawHead(Graphics2D g, ArrowShape head, Color c)
	{
		if(head != null)
		{
			g.setStroke(new BasicStroke());
			switch (head.getFillType())
			{
			case ArrowShape.OPEN:
				g.setPaint (Color.WHITE);
				g.fill (head.getShape());				
				g.setColor (c);
				g.draw (head.getShape());
				break;
			case ArrowShape.CLOSED:
				g.setPaint (c);
				g.fill (head.getShape());				
				break;
			case ArrowShape.WIRE:
				g.setColor (c);
				g.draw (head.getShape());
				break;
			default:
				assert (false);
			}
		}
	}

	/**
	   Will return the arrowhead suitable for an arrow pointing from
	   p1 to p2 (so the tip of the arrowhead will be at p2)
	 */
	protected ArrowShape getVHead(Point2D p1, Point2D p2, LineType type)
	{
		double xs = p1.getX();
		double ys = p1.getY();
		double xe = p2.getX();
		double ye = p2.getY();

		ArrowShape h;
		if (type == null)
		{
			h = ShapeRegistry.getArrow ("Default");
		}
		else if (type.getName().equals ("Line"))
		{
			h = null;
		}
		else
		{			
			h = ShapeRegistry.getArrow (type.getName());
		}
		
		if(h != null)
		{
			AffineTransform f = new AffineTransform();
			double scaleFactor = vFromM (1.0);
			f.rotate(Math.atan2 (ye - ys, xe - xs), xe, ye);
			f.translate (xe, ye);
			f.scale (scaleFactor, scaleFactor);		   
			Shape sh = f.createTransformedShape(h.getShape());
			h = new ArrowShape (sh, h.getFillType());
		}
		return h;
	}
		
	/**
	 * Constructs the line for the coordinates stored in this class
	 */
	public Line2D getVLine()
	{
		return new Line2D.Double(getVStartX(), getVStartY(), getVEndX(), getVEndY());
	}
	
	/**
	 * Sets the line start and end to the coordinates specified
	 * <DL><B>Parameters</B>
	 * <DD>Double x1	- new startx 
	 * <DD>Double y1	- new starty
	 * <DD>Double x2	- new endx
	 * <DD>Double y2	- new endy
	 */
	public void setVLine(double vx1, double vy1, double vx2, double vy2)
	{
		getStart().setVLocation(vx1, vy1);
		getEnd().setVLocation(vx2, vy2);
	}
		
	protected void setVScaleRectangle(Rectangle2D r) {
		setVLine(r.getX(), r.getY(), r.getX() + r.getWidth(), r.getY() + r.getHeight());
	}
	
	public Handle[] getHandles()
	{
		ArrayList<Handle> handles = new ArrayList<Handle>();
		for(VPoint p : points) {
			handles.add(p.getHandle());
		}
		for(Handle h : segmentHandles) {
			handles.add(h);
		}
		return handles.toArray(new Handle[handles.size()]);
	}
		
	public List<VPoint> getPoints() { return points; }
	
	public VPoint getStart() {
		return points.get(0);
	}
	
	public VPoint getEnd() {
		return points.get(points.size() - 1);
	}
	
	public double getVCenterX()
	{
		return vFromM(gdata.getMCenterX());
	}
	
	public double getVCenterY()
	{
		return vFromM(gdata.getMCenterY());
	}
	
	public double getVLeft()
	{
		return vFromM(gdata.getMLeft());
	}
	
	public double getVWidth()
	{
		return vFromM(gdata.getMWidth());
	}
	
	public double getVHeight()
	{
		return vFromM(gdata.getMHeight());
	}	
	
	public double getVTop()
	{
		return vFromM(gdata.getMTop());
	}
	
	protected void vMoveBy(double vdx, double vdy)
	{
		for(VPoint p : points) {
			p.vMoveBy(vdx, vdy);
		}
		//Move graphRefs
		Set<VPoint> toMove = new HashSet<VPoint>();
		for(GraphRefContainer ref : gdata.getReferences()) {
			if(ref instanceof MPoint) {
				toMove.add(canvas.getPoint((MPoint)ref));
			}
		}
		toMove.removeAll(points);
		for(VPoint p : toMove) p.vMoveBy(vdx, vdy);
	}
	
	public void gmmlObjectModified(PathwayEvent e) {		
		getConnectorShape().recalculateShape(getMLine());
		
		WayPoint[] wps = getConnectorShape().getWayPoints();
		List<MPoint> mps = gdata.getMPoints();
		if(wps.length == mps.size() - 2 && getConnectorShape().hasValidWaypoints(getMLine())) {
			getMLine().adjustWayPointPreferences(wps);
		} else {
			getMLine().resetWayPointPreferences();
		}

		updateSegmentHandles();
		markDirty();
		for(VPoint p : points) {
			p.markDirty();
			p.setHandleLocation();
		}
		if(gdata.getMAnchors().size() != anchors.size()) {
			setAnchors();
		}
		updateAnchorPositions();
	}
	
	protected void destroyHandles() { 
		//Point handles will be destroyed by VPoints
		
		for(Handle h : getSegmentHandles()) {
			h.destroy();
		}
	}
	
	protected void destroy() {
		super.destroy();
		
		for(VPoint p : points) {
			p.removeLine(this);
		}
		for(MPoint p : gdata.getMPoints()) {
			canvas.pointsMtoV.remove(p);
		}
		for(VAnchor a : anchors.values()) {
			a.destroy();
		}
	}
	
	/**
	 * Returns the x-coordinate of the start point of this line, adjusted to the
	 * current zoom factor
	 * @return
	 */
	protected double getVStartX() { return (int)(vFromM(gdata.getMStartX())); }
	
	/**
	 * Returns the y-coordinate of the start point of this line, adjusted to the
	 * current zoom factor
	 * @return
	 */
	protected double getVStartY() { return (int)(vFromM(gdata.getMStartY())); }
	
	/**
	 * Returns the x-coordinate of the end point of this line, adjusted to the
	 * current zoom factor
	 * @return
	 */
	protected double getVEndX() { return (int)(vFromM(gdata.getMEndX())); }
	
	/**
	 * Returns the y-coordinate of the end point of this line, adjusted to the
	 * current zoom factor
	 * @return
	 */
	protected double getVEndY() { return (int)(vFromM(gdata.getMEndY())); }

	/**
	 * Translate a line coordinate (1-dimensional) to
	 * a view coordinate
	 * @param l The line coordinate
	 */
	public Point2D vFromL(double l) {
		return getConnectorShape().fromLineCoordinate(l);
	}
	
	/**
	 * Translate a view coordinate (2-dimensional) to
	 * a line coordinate (1-dimensional)
	 */
	public double lFromV(Point2D v) {
		return getConnectorShape().toLineCoordinate(v);
	}
	
	/**
	 * Get the segment on which the given line coordinate
	 * lies
	 */
	public Segment getSegment(double lc) {
		Segment[] segments = getConnectorShape().getSegments();
		double length = 0;
		for(Segment s : segments) {
			length += s.getMLength();
		}
		double end = 0;
		for(Segment s : segments) {
			end += s.getMLength();
			if(lc <= end) {
				return s;
			}
		}
		return segments[segments.length];
	}
}
