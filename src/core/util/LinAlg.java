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
package util;

public class LinAlg {
	
	public static double angle(Point p1, Point p2) {
		//Angle:
		//					p1.p2	
        //cos(angle) = --------------
        //          	||p1||*||p2||
		double cos = dot(p1,p2) / (p1.len() * p2.len());
		return direction(p1,p2) * Math.acos(cos);
	}
		
	/**
	 * negative: ccw positive: cw
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double direction(Point p1, Point p2) {
		return Math.signum(p1.x * p2.y - p1.y * p2.x);
	}
	
	public static double dot(Point v1, Point v2) {
		double[] d1 = v1.asArray();
		double[] d2 = v2.asArray();
		double sum = 0;
		for(int i = 0; i < Math.min(d1.length, d2.length); i++) sum += d1[i]*d2[i];
		return sum;
	}
	
	public static Point project(Point p1, Point p2) {
		//Projection of p1 on p2:
		// p1.p2
		// ----- . p2
		// p2.p2
		double c = dot(p1, p2) / dot(p2, p2);
		return new Point(p2.x * c, p2.y * c);
	}
	
	public static Point rotate(Point p, double angle) {
		Point pr = new Point(0,0);
		pr.x = p.x * Math.cos(angle) + p.y * Math.sin(angle);
		pr.y = -p.x * Math.sin(angle) + p.y * Math.cos(angle);
		return pr;
	}
	
	public static class Point {
		public double x, y;
		public Point(double x, double y) { this.x = x; this.y = y;	}
		
		public int[] asIntArray() { return new int[] { (int)x, (int)y }; }
		
		public double[] asArray() { return new double[] { x, y }; }
		
		public Point norm() {
			double l = len();
			return new Point(x / l, y / l);
		}
		public double len() {
			return Math.sqrt(dot(this, this));
		}
		
		public Point add(Point p) { return new Point(x + p.x, y + p.y); }
		public Point subtract(Point p) { return new Point(x - p.x, y - p.y); }
		public Point multiply(double d) { return new Point(x *= d, y *= d); }
				
		public Point clone() { return new Point(x, y); }
		public String toString() { return "Point: " + x + ", " + y; }
	}
}
