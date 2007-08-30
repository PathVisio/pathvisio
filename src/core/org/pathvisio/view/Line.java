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
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MPoint;
 
/**
 * This class implements and handles a line
 */
public class Line extends Graphics
{	
	private static final long serialVersionUID = 1L;
	
	private List<VPoint> points;
	
	/**
	 * Constructor for this class
	 * @param canvas - the VPathway this line will be part of
	 */
	public Line(VPathway canvas, PathwayElement o)
	{
		super(canvas, o);
		
		points = new ArrayList<VPoint>();
		for(MPoint mp : o.getMPoints()) {
			VPoint vp = canvas.getPoint(mp);
			points.add(vp);
			vp.addLine(this);
			vp.setHandleLocation();
		}
	}
	
	public int getNaturalOrder() 
	{
		return VPathway.DRAW_ORDER_LINE;
	}
	
	protected void swapPoint(VPoint pOld, VPoint pNew) 
	{
		int i = points.indexOf(pOld);
		if(i > -1) {
			points.remove(pOld);
			points.add(i, pNew);
		}
	}

	public void doDraw(Graphics2D g)
	{
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

		Line2D l = getVLine();
		Point2D start = l.getP1();
		Point2D end = l.getP2();
		
		ArrowShape he = getVHead(start, end, gdata.getEndLineType());
		ArrowShape hs = getVHead(end, start, gdata.getStartLineType());
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
	
	private void drawHead(Graphics2D g, ArrowShape head, Color c)
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

	/*
	private Shape getArrowHead(double xs, double ys, double xe, double ye, double w, double h) {
		int[] xpoints = new int[] { (int)xe, (int)(xe - w), (int)(xe - w) };
		int[] ypoints = new int[] { (int)ye, (int)(ye - h), (int)(ye + h) };
		
		return new Polygon(xpoints, ypoints, 3);
	}
	
	private Shape getTBar(double xs, double ys, double xe, double ye, double w, double h) {
		return new Rectangle2D.Double(xe - w, ye - h/2, w, h);
	}
		
	private Shape getLRound(double xe, double ye, double d) {	
		return new Ellipse2D.Double(xe - d/2, ye - d/2, d, d);
	}
		
	private Shape getRRound(double xs, double ys, double xe, double ye, double d) {
		return new Arc2D.Double((int)xe, (int)(ye - d/2), d, d, 90, 180, Arc2D.OPEN);
	}
		
	private Shape getReceptor(double xs, double ys, double xe, double ye, double w, double h) {					
		GeneralPath rec = new GeneralPath();
		rec.moveTo((int)(xe + w), (int)(ye + h/2));
		rec.lineTo((int)xe, (int)(ye + h/2));
		rec.lineTo((int)xe, (int)(ye - h/2));
		rec.lineTo((int)(xe + w), (int)(ye - h/2));
		return rec;
	}
	
	private Shape getLigand(double xs, double ys, double xe, double ye, double w, double h) {
		return new Rectangle2D.Double(xe - w, ye - h/2, w, h);
	}
	*/
	
//	TODO: create the real outline, by creating a shape that
//  represents the whole line...use getArrow() etc.
	protected Shape getVOutline()
	{
		Line2D l = getVLine();
		Point2D start = l.getP1();
		Point2D end = l.getP2();
		
		//TODO: take start arrowHead into account too.
		//Wider stroke for line, for 'fuzzy' matching
		Area line = new Area (new BasicStroke(5).createStrokedShape(l));
		ArrowShape he = getVHead(start, end, gdata.getEndLineType());
		if(he != null)
		{
			line.add(new Area(he.getShape()));
		}
		ArrowShape hs = getVHead(end, start, gdata.getStartLineType());
		if(hs != null)
		{
			line.add(new Area(hs.getShape()));
		}
		return line;
	}
	
//	/**
//	 * If the line type is arrow, this method draws the arrowhead
//	 */
//	private void drawArrowhead(GC buffer) //TODO! clean up this mess.....
//	{
//		double angle = 25.0;
//		double theta = Math.toRadians(180 - angle);
//		double[] rot = new double[2];
//		double[] p = new double[2];
//		double[] q = new double[2];
//		double a, b, norm;
//		
//		rot[0] = Math.cos(theta);
//		rot[1] = Math.sin(theta);
//		
//		buffer.setLineStyle (SWT.LINE_SOLID);
//		
//		double vEndx = getVEndX();
//		double vEndy = getVEndY();
//		double vStartx = getVStartX();
//		double vStarty = getVStartY();
//		
//		if(vStartx == vEndx && vStarty == vEndy) return; //Unable to determine direction
//		
//		a = vEndx-vStartx;
//		b = vEndy-vStarty;
//		norm = 8/(Math.sqrt((a*a)+(b*b)));				
//		p[0] = ( a*rot[0] + b*rot[1] ) * norm + vEndx;
//		p[1] = (-a*rot[1] + b*rot[0] ) * norm + vEndy;
//		q[0] = ( a*rot[0] - b*rot[1] ) * norm + vEndx;
//		q[1] = ( a*rot[1] + b*rot[0] ) * norm + vEndy;
//		int[] points = {
//			(int)vEndx, (int)vEndy,
//			(int)(p[0]), (int)(p[1]),
//			(int)(q[0]), (int)(q[1])
//		};
//		
//		buffer.drawPolygon (points);
//		buffer.fillPolygon (points);
//	}

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
		Handle[] handles = new Handle[points.size()];
		for(int i = 0; i < handles.length; i++) {
			handles[i] = points.get(i).getHandle();
		}
		return handles;
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
		double start = gdata.getMStart().getX();
		double end = gdata.getMEnd().getX();
		return vFromM(start + (end - start) / 2);
	}
	
	public double getVCenterY()
	{
		double start = gdata.getMStart().getY();
		double end = gdata.getMEnd().getY();
		return vFromM(start + (end - start) / 2);
	}
	
	public double getVLeft()
	{
		double start = gdata.getMStart().getX();
		double end = gdata.getMEnd().getX();
		return vFromM(Math.min(start, end));
	}
	
	public double getVWidth()
	{
		double start = gdata.getMStart().getX();
		double end = gdata.getMEnd().getX();
		return vFromM(Math.abs(start-end));
	}
	
	public double getVHeight()
	{
		double start = gdata.getMStart().getY();
		double end = gdata.getMEnd().getY();
		return vFromM(Math.abs(start-end));
	}	
	
	public double getVTop()
	{
		double start = gdata.getMStart().getY();
		double end = gdata.getMEnd().getY();
		return vFromM(Math.min(start, end));
	}
	
	protected Shape getVShape(boolean rotate) {
		return new Rectangle2D.Double(getVLeft(), getVTop(), getVWidth(), getVHeight());
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
		markDirty();
		for(VPoint p : points) {
			p.markDirty();
			p.setHandleLocation();
		}
	}
	
	protected void destroyHandles() { 
		//Do nothing, handles will be destroyed by VPoints
	}
	
	protected void destroy() {
		//don't call super.destroy(), this will destroy handles of VPoints
		//which may be used by other lines
		super.destroy();
		
		for(VPoint p : points) {
			p.removeLine(this);
		}
		for(MPoint p : gdata.getMPoints()) {
			canvas.pointsMtoV.remove(p);
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
}
