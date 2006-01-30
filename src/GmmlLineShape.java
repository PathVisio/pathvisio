/*
Copyright 2005 H.C. Achterberg, R.M.H. Besseling, I.Kaashoek, 
M.M.Palm, E.D Pelgrim, BiGCaT (http://www.BiGCaT.unimaas.nl/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and 
limitations under the License.
*/

import java.awt.*;
import java.awt.geom.Line2D;
/**
  *This class contains the lineshapes. It contains a constructor, and the methods contains, setLocation and getHelpers
  */

public class GmmlLineShape {
	double startx, starty, endx, endy;
	int type;
	Color color;
	
	/**
	  *Constructor GmmlLineShape has 4 doubles for the coordinates, an int for the type and a color object for the color as input.
	  */
	public GmmlLineShape (double startx, double starty, double endx, double endy, int type, Color color) {
		this.startx = startx;
		this.starty = starty;
		this.endx = endx;
		this.endy = endy;
		this.type = type;
		this.color = color;
	}
	
	/**
	  *Method contains uses the coordinates of a specific point (pointx, pointy) 
	  *to determine whether a lineshape contains this point. 
	  *To do this, a polygon is created, on which the normal contains method is used. 
	  *This polygon is created to enlarge the line, because it is rather difficult to click a line.
	  */
	public boolean contains (double pointx, double pointy) {
		double s  = Math.sqrt(((endx-startx)*(endx-startx)) + ((endy-starty)*(endy-starty))) / 60;
		int[] x = new int[4];
		int[] y = new int[4];
			
		x[0] = (int) (((-endy + starty)/s) + endx);
		y[0] = (int) ((( endx - startx)/s) + endy);
		x[1] = (int) ((( endy - starty)/s) + endx);
		y[1] = (int) (((-endx + startx)/s) + endy);
		x[2] = (int) ((( endy - starty)/s) + startx);
		y[2] = (int) (((-endx + startx)/s) + starty);
		x[3] = (int) (((-endy + starty)/s) + startx);
		y[3] = (int) ((( endx - startx)/s) + starty);
			
		Polygon temp = new Polygon(x,y,4);
				
		if (temp.contains(pointx, pointy)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	  *Method setLocation changes the int x and y coordinate to the x and y that are arguments for this method
	  */	
	public void setLocation(double startx, double starty){
		double diffx = startx - this.startx;
		double diffy = starty - this.starty;
		this.startx = startx;
		this.starty = starty;
		endx = endx + diffx;
		endy = endy + diffy;
	}
	
	/**
	  *Method getHelpers returns an array of rectangles on the lineshape, which are used to drag and transform the lineshape.
	  */
	public Rectangle[] getHelpers(double zf) {
		Rectangle helpers[] = new Rectangle[2];
		helpers[0] = new Rectangle((int)(startx/zf) - 2 ,(int)(starty/zf) - 2, 5, 5);
		helpers[1] = new Rectangle((int)(endx/zf) - 2 ,(int)(endy/zf) - 2, 5, 5);
		
		return helpers;
	}
}