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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pathvisio.model.LineStyle;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayEvent;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.model.PathwayElement.MPoint;
 
/**
 * This class implements and handles a line
 */
public class Line extends Graphics
{
	private static final int ARROWHEIGHT = 65;
	private static final int ARROWWIDTH = 140;
	private static final int TBARHEIGHT = 225;
	private static final int TBARWIDTH = 15;
	private static final int LRDIAM = 175;
	private static final int RRDIAM = LRDIAM + 50;
	private static final int LIGANDWIDTH = 125;
	private static final int LIGANDHEIGHT = 175;
	private static final int RECEPWIDTH = LIGANDWIDTH + 30;
	private static final int RECEPHEIGHT = LIGANDHEIGHT + 30;
	
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
	
	public int getDrawingOrder() 
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
		Color c = gdata.getColor();
		if(isSelected()) {
			c = selectColor;
		} else if (isHighlighted()) {
			c = highlightColor;
		}
		g.setColor(c);
				
		int ls = gdata.getLineStyle();
		if (ls == LineStyle.SOLID) {
			g.setStroke(new BasicStroke());
		}
		else if (ls == LineStyle.DASHED) { 
			g.setStroke(new BasicStroke(1, 
									BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 
									10, new float[] {4, 4}, 0));
		}			

		Line2D l = getVLine();
		Point2D start = l.getP1();
		Point2D end = l.getP2();
		
		g.draw(l);
		
		double xs = start.getX();
		double ys = start.getY();
		double xe = end.getX();
		double ye = end.getY();
		
		switch (gdata.getLineType()) {
			case ARROW:				
				paintArrowHead(g, xs, ys, xe, ye, vFromM(ARROWWIDTH), vFromM(ARROWHEIGHT));
				break;
			case TBAR:	
				paintTBar(g, xs, ys, xe, ye, vFromM(TBARWIDTH), vFromM(TBARHEIGHT));
				break;
			case LIGAND_ROUND:	
				paintLRound(g, xe, ye, vFromM(LRDIAM));
				break;
			case RECEPTOR_ROUND:
				paintRRound(g, xs, ys, xe, ye, vFromM(RRDIAM));
				break;
			case RECEPTOR: //TODO: implement receptor
			case RECEPTOR_SQUARE:
				paintReceptor(g, xs, ys, xe, ye, vFromM(RECEPWIDTH), vFromM(RECEPHEIGHT));
				break;
			case LIGAND_SQUARE:
			{
				paintLigand(g, xs, ys, xe, ye, vFromM(LIGANDWIDTH), vFromM(LIGANDHEIGHT));
			}
			break;
	}
	}
	
	private double getAngle(double xs, double ys, double xe, double ye) {
		if(xs == xe && ys == ye) return 0; //Unable to determine direction
		Point2D ps = new Point2D.Double(xe - xs, ye - ys);
		Point2D pe = new Point2D.Double(1, 0);
		
		return LinAlg.angle(new LinAlg.Point(ps.getX(), ps.getY()), new LinAlg.Point(pe.getX(), pe.getY()));
	}
	
	private void paintArrowHead(Graphics2D g2d, double xs, double ys, double xe, double ye, double w, double h) {			
		double angle = getAngle(xs, ys, xe, ye);
		int[] xpoints = new int[] { (int)xe, (int)(xe - w), (int)(xe - w) };
		int[] ypoints = new int[] { (int)ye, (int)(ye - h), (int)(ye + h) };
		
		Polygon arrow = new Polygon(xpoints, ypoints, 3);
		AffineTransform f = new AffineTransform();
		f.rotate(-angle, xe, ye);
		Shape rotArrow = f.createTransformedShape(arrow);
		g2d.fill(rotArrow);
	}
	
	private void paintTBar(Graphics2D g2d, double xs, double ys, double xe, double ye, double w, double h) {
		double angle = getAngle(xs, ys, xe, ye);
	
		Rectangle2D bar = new Rectangle2D.Double(xe - w, ye - h/2, w, h);
		AffineTransform f = new AffineTransform();
		f.rotate(-angle, xe, ye);
		Shape rotBar = f.createTransformedShape(bar);
		g2d.fill(rotBar);
	}
	
	private void paintLRound(Graphics2D g2d, double xe, double ye, double d) {	
		g2d.fillOval ((int)(xe - d/2), (int)(ye - d/2), (int)d, (int)d);
	}
	
	private void paintRRound(Graphics2D g2d, double xs, double ys, double xe, double ye, double d) {
		double angle = getAngle(xs, ys, xe, ye);
		AffineTransform f = new AffineTransform();
		f.rotate(-angle, xe, ye);
		
		Rectangle2D hideRect = new Rectangle2D.Double(xe - d/2, ye - 2, d/2 + 1, 4);
		Shape hide = f.createTransformedShape(hideRect);
		
		Arc2D arc = new Arc2D.Double((int)(xe - d/2), (int)(ye - d/2), d, d, 90, 180, Arc2D.OPEN);
		Shape rotArc = f.createTransformedShape(arc);
		
		//Hide top of line
		Color cOld = g2d.getColor();
		g2d.setColor(Color.WHITE);
		g2d.fill(hide);
		g2d.setColor(cOld);
		g2d.draw(rotArc);		
	}
	
	private void paintReceptor(Graphics2D g2d, double xs, double ys, double xe, double ye, double w, double h) {					
		/* Path2D Only in Java 1.6....
		Path2D rec = new Path2D.Double();
		rec.moveTo(xe + w, ye + h/2);
		rec.lineTo(xe, ye + h/2);
		rec.lineTo(xe, ye - h/2);
		rec.lineTo(xe + w, ye - h/2);
		AffineTransform f = new AffineTransform();
		f.rotate(-angle, xe, ye);
		Shape rotRec = f.createTransformedShape(rec);
		g2d.draw(rotRec);
		*/
		
		double angle = getAngle(xs, ys, xe, ye);
		AffineTransform f = new AffineTransform();
		f.rotate(-angle, xe, ye);
		
		Rectangle2D hideRect = new Rectangle2D.Double(xe - w, ye - 2, w + 1, 4);
		Shape hide = f.createTransformedShape(hideRect);
		
		//Hide top of line
		Color cOld = g2d.getColor();
		g2d.setColor(Color.WHITE);
		g2d.fill(hide);
		g2d.setColor(cOld);
		
		Shape l = new Line2D.Double(xe, ye + h/2, xe - w, ye + h/2);
		Shape r = f.createTransformedShape(l);
		g2d.draw(r);
		l = new Line2D.Double(xe - w, ye + h/2, xe - w, ye - h/2);
		r = f.createTransformedShape(l);
		g2d.draw(r);
		l = new Line2D.Double(xe - w, ye - h/2, xe, ye - h/2);
		r = f.createTransformedShape(l);
		g2d.draw(r);
	}
	
	private void paintLigand(Graphics2D g2d, double xs, double ys, double xe, double ye, double w, double h) {
		double angle = getAngle(xs, ys, xe, ye);
	
		Rectangle2D bar = new Rectangle2D.Double(xe - w, ye - h/2, w, h);
		AffineTransform f = new AffineTransform();
		f.rotate(-angle, xe, ye);
		Shape rotBar = f.createTransformedShape(bar);
		g2d.fill(rotBar);
		g2d.draw(rotBar);
	}
	
	protected Shape getVOutline()
	{
		//TODO: bigger than necessary, just to include the arrowhead / shape at the end
		BasicStroke stroke = new BasicStroke(20);
		Shape outline = stroke.createStrokedShape(getVLine());
		return outline;
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

	public void setVScaleRectangle(Rectangle2D r) {
		setVLine(r.getX(), r.getY(), r.getX() + r.getWidth(), r.getY() + r.getHeight());
	}
	
	protected Rectangle2D getVScaleRectangle() {
		return new Rectangle2D.Double(getVStartXDouble(), getVStartYDouble(), getVEndXDouble()
				- getVStartXDouble(), getVEndYDouble() - getVStartYDouble());
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
	
	public int getVCenterX() {
		// TODO Auto-generated method stub
		double start = gdata.getMStart().getX();
		double end = gdata.getMEnd().getX();
		return (int)vFromM(start + (end - start) / 2);
	}
	
	public int getVCenterY() {
		// TODO Auto-generated method stub
		double start = gdata.getMStart().getY();
		double end = gdata.getMEnd().getY();
		return (int)vFromM(start + (end - start) / 2);
	}
	
	public int getVLeft() {
		// TODO Auto-generated method stub
		double start = gdata.getMStart().getX();
		double end = gdata.getMEnd().getX();
		return (int)vFromM(Math.min(start, end));
	}
	
	public int getVWidth() {
		// TODO Auto-generated method stub
		double start = gdata.getMStart().getX();
		double end = gdata.getMEnd().getX();
		return (int)vFromM(Math.abs(start-end));
	}
	
	public int getVHeight() {
		// TODO Auto-generated method stub
		double start = gdata.getMStart().getY();
		double end = gdata.getMEnd().getY();
		return (int)vFromM(Math.abs(start-end));
	}	
	
	public int getVTop() {
		// TODO Auto-generated method stub
		double start = gdata.getMStart().getY();
		double end = gdata.getMEnd().getY();
		return (int)vFromM(Math.min(start, end));
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
	
	protected int getVStartX() { return (int)(vFromM(gdata.getMStartX())); }
	protected int getVStartY() { return (int)(vFromM(gdata.getMStartY())); }
	protected int getVEndX() { return (int)(vFromM(gdata.getMEndX())); }
	protected int getVEndY() { return (int)(vFromM(gdata.getMEndY())); }

	protected double getVStartXDouble() { return vFromM(gdata.getMStartX()); }
	protected double getVStartYDouble() { return vFromM(gdata.getMStartY()); }
	protected double getVEndXDouble() { return vFromM(gdata.getMEndX()); }
	protected double getVEndYDouble() { return vFromM(gdata.getMEndY()); }

}
