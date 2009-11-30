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
package org.pathvisio.view;

import java.awt.geom.Point2D;

/**
 * Helper class for rotation calculations.
 *
 */
public class LinAlg
{

	/**
	 * Determines angle between two vectors defined by p1 and p2
	 * Both vectors start at 0.0
	 *
	 * @param p1 start point of vector
	 * @param p2 end point of vector
	 * @return angle in radians
	 */
	public static double angle(Point p1, Point p2)
	{
		//Angle:
		//					p1.p2
        //cos(angle) = --------------
        //          	||p1||*||p2||

		double cos = dot(p1,p2) / (p1.len() * p2.len());
		if(cos>1)
		{
			cos=1;
		}
		return direction(p1,p2) * Math.acos(cos);
	}

	/**
	 * negative: ccw positive: cw
	 */
	public static double direction(Point p1, Point p2)
	{

		return Math.signum(p1.x * p2.y - p1.y * p2.x);
	}

	private static double dot(Point v1, Point v2)
	{
		double[] d1 = v1.asArray();
		double[] d2 = v2.asArray();
		double sum = 0;
		for(int i = 0; i < Math.min(d1.length, d2.length); i++) sum += d1[i]*d2[i];
		return sum;
	}

	/**
	   Projection of point q on a line through p with direction vector v

	   If p is 0,0, it's the same as the two-argument function with the same name.
	 */
	public static Point project (Point p, Point q, Point v)
	{
		Point q2 = new Point (q.x - p.x, q.y - p.y);
		double vlen = dot (v, v);
		if (vlen == 0)
		{
			return p;
		}
		else
		{
			double c = dot (q2, v) / dot (v, v);
			return new Point (p.x + v.x * c, p.y + v.y * c);
		}
	}

	/**
	 * Convert a 2-D point to 1-D line coordinates (relative position on the line, range {0,1})
	 */
	public static double toLineCoordinates (Point start, Point end, Point p) {
		//Project v position on line and calculate relative position
		Point direction = start.subtract(end);
		Point projection = project(start, p, direction);
		double lineLength = distance(start, end);
		double anchorLength = distance(start, projection);
		double position = anchorLength / lineLength;

		double ldir = direction(start, end);
		double adir = direction(start, projection);
		if(adir != ldir) {
			position = 0;
		}
		if(position > 1) position = 1;
		if(position < 0) position = 0;
		if(Double.isNaN(position)) position = 0;
		return position;
	}
	/**
	  Projection of p1 on p2:

	   p1.p2
	   ----- . p2
	   p2.p2
	*/
	public static Point project(Point p1, Point p2)
	{
		double c = dot(p1, p2) / dot(p2, p2);
		return new Point(p2.x * c, p2.y * c);
	}

	public static double distance(Point p1, Point p2)
	{
		Point dp = p2.subtract(p1);
		return dp.len();
	}

	public static Point rotate(Point p, double angle)
	{
		Point pr = new Point(0,0);
		pr.x = p.x * Math.cos(angle) + p.y * Math.sin(angle);
		pr.y = -p.x * Math.sin(angle) + p.y * Math.cos(angle);
		return pr;
	}

	/**
	 * Point, pair of doubles. Supports adding, substracting,
	 * calculating the length of the vector, etc.
	 */
	public static class Point
	{
		public double x, y;

		public Point(double x, double y) { this.x = x; this.y = y;	}

		public Point(Point2D p2d) {
			this(p2d.getX(), p2d.getY());
		}

		public Point2D toPoint2D() {
			return new Point2D.Double(x, y);
		}

		public int[] asIntArray() { return new int[] { (int)x, (int)y }; }

		public double[] asArray() { return new double[] { x, y }; }

		public Point norm()
		{
			double l = len();
			return new Point(x / l, y / l);
		}
		public double len()
		{
			return Math.sqrt(dot(this, this));
		}

		public Point add(Point p) { return new Point(x + p.x, y + p.y); }
		public Point subtract(Point p) { return new Point(x - p.x, y - p.y); }
		public Point multiply(double d) { return new Point(x *= d, y *= d); }

		public Point clone() { return new Point(x, y); }
		public String toString() { return "Point: " + x + ", " + y; }
	}
}
