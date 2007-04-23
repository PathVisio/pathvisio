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
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import org.pathvisio.preferences.GmmlPreferences;
import org.pathvisio.util.SwtUtils;
import org.pathvisio.model.GmmlDataObject;
import org.pathvisio.model.GmmlEvent;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.GmmlDataObject.MPoint;
import org.pathvisio.model.GraphLink.GraphRefContainer;
 
/**
 * This class implements and handles a line
 */
public class Line extends Graphics
{

	private static final long serialVersionUID = 1L;
	
	private List<VPoint> points;
	
	/**
	 * Constructor for this class
	 * @param canvas - the Pathway this line will be part of
	 */
	public Line(Pathway canvas, GmmlDataObject o)
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
	
	public int getDrawingOrder() {
		return Pathway.DRAW_ORDER_LINE;
	}
	
	protected void swapPoint(VPoint pOld, VPoint pNew) {
		int i = points.indexOf(pOld);
		if(i > -1) {
			points.remove(pOld);
			points.add(i, pNew);
		}
	}
			
	public void draw(PaintEvent e, GC buffer)
	{
		double vEndx = getVEndX();
		double vEndy = getVEndY();
		double vStartx = getVStartX();
		double vStarty = getVStartY();

		Color c = null;
		if (isSelected())
		{
			c = SwtUtils.changeColor(c, selectColor, e.display);
		}
		else if (isHighlighted())
		{
			RGB rgb = GmmlPreferences.getColorProperty(GmmlPreferences.PREF_COL_HIGHLIGHTED);
			c = SwtUtils.changeColor(c, rgb, e.display);
		}
		else 
		{
			c = SwtUtils.changeColor(c, gdata.getColor(), e.display);
		}
		buffer.setForeground (c);
		buffer.setBackground (c);
		
		buffer.setLineWidth (1);
		int ls = gdata.getLineStyle();
		if (ls == LineStyle.SOLID)
		{
			buffer.setLineStyle (SWT.LINE_SOLID);
		}
		else if (ls == LineStyle.DASHED)
		{ 
			// TODO: This works well on windows. I wonder if this is the same on all platforms
			buffer.setLineDash (new int[] {4, 4});
		}			

		double s = Math.sqrt(((vEndx-vStartx)*(vEndx-vStartx)) + ((vEndy - vStarty)*(vEndy - vStarty)));
		
		switch (gdata.getLineType())
		{
		
			case LINE:
				buffer.drawLine ((int)vStartx, (int)vStarty, (int)vEndx, (int)vEndy);
				break;
			case ARROW:				
				buffer.drawLine ((int)vStartx, (int)vStarty, (int)vEndx, (int)vEndy);
				drawArrowhead(buffer);
				break;
			case TBAR:
			{
				s /= 8;
	
				double capx1 = ((-vEndy + vStarty)/s) + vEndx;
				double capy1 = (( vEndx - vStartx)/s) + vEndy;
				double capx2 = (( vEndy - vStarty)/s) + vEndx;
				double capy2 = ((-vEndx + vStartx)/s) + vEndy;
	
				buffer.drawLine ((int)vStartx, (int)vStarty, (int)vEndx, (int)vEndy);
				buffer.drawLine ((int)capx1, (int)capy1, (int)capx2, (int)capy2);
			}
				break;
			case LIGAND_ROUND:
			{
				if (vEndx != vStartx || vEndy != vStarty)
				{
					double dx = (vEndx - vStartx)/s;
					double dy = (vEndy - vStarty)/s;
								
					buffer.drawLine ((int)vStartx, (int)vStarty, (int)(vEndx - 6 * dx), (int)(vEndy - 6 * dy));
					buffer.drawOval ((int)vEndx - 5, (int)vEndy - 5, 10, 10);
					buffer.fillOval ((int)vEndx - 5, (int)vEndy - 5, 10, 10);
				}
			}
				break;
			case RECEPTOR_ROUND:
			{
				if (vEndx != vStartx || vEndy != vStarty)
				{
					double theta 	= Math.toDegrees(Math.atan2((vEndx - vStartx),(vEndy - vStarty)));
					double dx 		= (vEndx - vStartx)/s;
					double dy 		= (vEndy - vStarty)/s;	
					
					buffer.drawLine ((int)vStartx, (int)vStarty, (int)(vEndx - (8*dx)), (int)(vEndy - (8*dy)));
					buffer.drawArc ((int)vEndx - 8, (int)vEndy - 8, 16, 16, (int)theta + 180, -180);
				}
			}
				break;
			case RECEPTOR: //TODO: implement receptor
			case RECEPTOR_SQUARE:
			{
				if (vEndx != vStartx || vEndy != vStarty)
				{
					s /= 8;
					
					double x3 		= vEndx - ((vEndx - vStartx)/s);
					double y3 		= vEndy - ((vEndy - vStarty)/s);
					double capx1 	= ((-vEndy + vStarty)/s) + x3;
					double capy1 	= (( vEndx - vStartx)/s) + y3;
					double capx2 	= (( vEndy - vStarty)/s) + x3;
					double capy2 	= ((-vEndx + vStartx)/s) + y3;			
					double rx1		= capx1 + 1.5*(vEndx - vStartx)/s;
					double ry1 		= capy1 + 1.5*(vEndy - vStarty)/s;
					double rx2 		= capx2 + 1.5*(vEndx - vStartx)/s;
					double ry2 		= capy2 + 1.5*(vEndy - vStarty)/s;
				
					buffer.drawLine ((int)vStartx, (int)vStarty, (int)x3, (int)y3);
					buffer.drawLine ((int)capx1, (int)capy1, (int)capx2, (int)capy2);
					buffer.drawLine ((int)capx1, (int)capy1, (int)rx1, (int)ry1);
					buffer.drawLine ((int)capx2, (int)capy2, (int)rx2, (int)ry2);
				}
			}
				break;
			case LIGAND_SQUARE:
			{
				if (vEndx != vStartx || vEndy != vStarty)
				{
					s /= 6;
					double x3 		= vEndx - ((vEndx - vStartx)/s);
					double y3 		= vEndy - ((vEndy - vStarty)/s);
		
					int[] points = new int[4 * 2];
					
					points[0] = (int) (((-vEndy + vStarty)/s) + x3);
					points[1] = (int) ((( vEndx - vStartx)/s) + y3);
					points[2] = (int) ((( vEndy - vStarty)/s) + x3);
					points[3] = (int) (((-vEndx + vStartx)/s) + y3);
		
					points[4] = (int) (points[2] + 1.5*(vEndx - vStartx)/s);
					points[5] = (int) (points[3] + 1.5*(vEndy - vStarty)/s);
					points[6] = (int) (points[0] + 1.5*(vEndx - vStartx)/s);
					points[7] = (int) (points[1] + 1.5*(vEndy - vStarty)/s);
					
					buffer.drawLine ((int)vStartx, (int)vStarty, (int)x3, (int)y3);
					buffer.drawPolygon(points);
					buffer.fillPolygon(points);
				}
			}
				break;
		}
		
		c.dispose();

		
	}
	
	protected void draw(PaintEvent e)
	{
		draw(e, e.gc);
	}
		
	protected Shape getVOutline()
	{
		//TODO: bigger than necessary, just to include the arrowhead / shape at the end
		BasicStroke stroke = new BasicStroke(20);
		Shape outline = stroke.createStrokedShape(getVLine());
		return outline;
	}	
	
	/**
	 * If the line type is arrow, this method draws the arrowhead
	 */
	private void drawArrowhead(GC buffer) //TODO! clean up this mess.....
	{
		double angle = 25.0;
		double theta = Math.toRadians(180 - angle);
		double[] rot = new double[2];
		double[] p = new double[2];
		double[] q = new double[2];
		double a, b, norm;
		
		rot[0] = Math.cos(theta);
		rot[1] = Math.sin(theta);
		
		buffer.setLineStyle (SWT.LINE_SOLID);
		
		double vEndx = getVEndX();
		double vEndy = getVEndY();
		double vStartx = getVStartX();
		double vStarty = getVStartY();
		
		if(vStartx == vEndx && vStarty == vEndy) return; //Unable to determine direction
		
		a = vEndx-vStartx;
		b = vEndy-vStarty;
		norm = 8/(Math.sqrt((a*a)+(b*b)));				
		p[0] = ( a*rot[0] + b*rot[1] ) * norm + vEndx;
		p[1] = (-a*rot[1] + b*rot[0] ) * norm + vEndy;
		q[0] = ( a*rot[0] - b*rot[1] ) * norm + vEndx;
		q[1] = ( a*rot[1] + b*rot[0] ) * norm + vEndy;
		int[] points = {
			(int)vEndx, (int)vEndy,
			(int)(p[0]), (int)(p[1]),
			(int)(q[0]), (int)(q[1])
		};
		
		buffer.drawPolygon (points);
		buffer.fillPolygon (points);
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

	public void setVScaleRectangle(Rectangle2D.Double r) {
		setVLine(r.x, r.y, r.x + r.width, r.y + r.height);
	}
	
	protected Rectangle2D.Double getVScaleRectangle() {
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
	
	public void gmmlObjectModified(GmmlEvent e) {		
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
