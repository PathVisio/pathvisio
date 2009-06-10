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
package org.pathvisio.cytoscape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.ArrowShape;
import org.pathvisio.view.ShapeRegistry;

import ding.view.DGraphView;

public class Line extends Annotation {
	private static final int HEADSPACE = 30;
	
	public Line(PathwayElement e, DGraphView view) {
		super(e, view);
	}

	protected double getVStartX() { return GpmlPlugin.mToV((pwElm.getMStartX())); }
	protected double getVStartY() { return GpmlPlugin.mToV((pwElm.getMStartY())); }
	protected double getVEndX() { return GpmlPlugin.mToV((pwElm.getMEndX())); }
	protected double getVEndY() { return GpmlPlugin.mToV((pwElm.getMEndY())); }
	
	public Line2D getVLine() {
		return new Line2D.Double(getVStartX(), getVStartY(), getVEndX(), getVEndY());
	}
	
	public Shape getVOutline() {
		//TODO: bigger than necessary, just to include the arrowhead / shape at the end
		BasicStroke stroke = new BasicStroke(HEADSPACE);
		Shape outline = stroke.createStrokedShape(getVLine());
		return outline;
	}
	
	public void doPaint(Graphics2D g2d) {
		Color c = pwElm.getColor();
		g2d.setColor(c);
		
		Rectangle b = relativeToBounds(viewportTransform(getVLine())).getBounds();
		Line2D l = getVLine();
		Point2D start = l.getP1();
		Point2D end = l.getP2();
		
		double xdir = end.getX() - start.getX();
		double ydir = end.getY() - start.getY();
		
		double xs = xdir >= 0 ? b.x : b.x + b.width;
		double ys = ydir >= 0 ? b.y : b.y + b.height;
		double xe = xdir >= 0 ? xs + b.width : b.x;
		double ye = ydir >= 0 ? ys + b.height : b.y;

		l = new Line2D.Double(xs, ys, xe, ye);
		start = l.getP1();
		end = l.getP2();
		
		int ls = pwElm.getLineStyle();
		if (ls == LineStyle.SOLID) {
			g2d.setStroke(new BasicStroke());
		}
		else if (ls == LineStyle.DASHED)
		{ 
			g2d.setStroke	(new BasicStroke (
				  1, 
				  BasicStroke.CAP_SQUARE,
				  BasicStroke.JOIN_MITER, 
				  10, new float[] {4, 4}, 0));
		}			

		
		ArrowShape he = getVHead(start, end, pwElm.getEndLineType());
		ArrowShape hs = getVHead(end, start, pwElm.getStartLineType());
		g2d.draw(l);
		drawHead(g2d, he, c);
		drawHead(g2d, hs, c);
		
		g2d.dispose();
	}
	
	private void drawHead(Graphics2D g, ArrowShape head, Color c)
	{
		if(head != null)
		{
			g.setStroke(new BasicStroke());
			switch (head.getFillType())
			{
			case OPEN:
				g.setPaint (Color.WHITE);
				g.fill (head.getShape());				
				g.setColor (c);
				g.draw (head.getShape());
				break;
			case CLOSED:
				g.setPaint (c);
				g.fill (head.getShape());				
				break;
			case WIRE:
				g.setColor (c);
				g.draw (head.getShape());
				break;
			default:
				assert (false);
			}
		}
	}
	
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
			f.rotate(Math.atan2 (ye - ys, xe - xs), xe, ye);
			f.translate (xe, ye);
			double scaleFactor = GpmlPlugin.mToV(getCurrentScaleFactor());
			f.scale (scaleFactor, scaleFactor);	   
			Shape sh = f.createTransformedShape(h.getShape());
			h = new ArrowShape (sh, h.getFillType());
		}
		return h;
	}
	
}
