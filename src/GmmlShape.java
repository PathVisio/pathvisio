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

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;

/**
  *This class contains the shapes. It contains a constructor, and the methods contains, setLocation and getHelpers
  */
public class GmmlShape {

double x,y,width,height,rotation;
int type;
Color color;
	
	/**
	  *Constructor GmmlShape has 4 doubles for the coordinates, an int for the type, a double for the rotation and a color object for the color as input.
	  */
	public GmmlShape(double x, double y, double width, double height, int type, String color, double rotation) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = GmmlColor.convertStringToColor(color);
		this.type = type;
		this.rotation = rotation;			
	} //end of GmmlShape constructor
	
	/**
	  *Method contains uses the coordinates of a specific point (pointx, pointy) 
	  *to determine whether a shape contains this point. 
	  *To do this, a polygon is created, on which the normal contains method is used.
	  */
	public boolean contains(double pointx, double pointy)
	{
		if (type==0) {				
			double theta = Math.toRadians(rotation);
			double[] rot = new double[2];
				
			rot[0] = Math.cos(theta);
			rot[1] = Math.sin(theta);
		
			int[] xs = new int[4];
			int[] ys = new int[4];
			
			xs[0]= (int)(( 0.5*width*rot[0]-0.5*height*rot[1])+x+width); //upper right
			xs[1]= (int)(( 0.5*width*rot[0]+0.5*height*rot[1])+x+width); //lower right
			xs[2]= (int)((-0.5*width*rot[0]+0.5*height*rot[1])+x+width); //lower left
			xs[3]= (int)((-0.5*width*rot[0]-0.5*height*rot[1])+x+width); //upper left
			
			ys[0]= (int)(( 0.5*width*rot[1]+0.5*height*rot[0])+y+height); //upper right
			ys[1]= (int)(( 0.5*width*rot[1]-0.5*height*rot[0])+y+height); //lower right
			ys[2]= (int)((-0.5*width*rot[1]-0.5*height*rot[0])+y+height); //lower left
			ys[3]= (int)((-0.5*width*rot[1]+0.5*height*rot[0])+y+height); //upper left
				
			Polygon temp = new Polygon(xs,ys,4);
			
			if (temp.contains(pointx, pointy)) {
				return true;
			}
			else {
				return false;
			}
				
		}
		else {
			double theta = Math.toRadians(rotation);
			double[] rot = new double[2];
				
			rot[0] = Math.cos(theta);
			rot[1] = Math.sin(theta);
		
			int[] xs = new int[4];
			int[] ys = new int[4];
			
			xs[0]= (int)(( width*rot[0]-height*rot[1])+x+width); //upper right
			xs[1]= (int)(( width*rot[0]+height*rot[1])+x+width); //lower right
			xs[2]= (int)((-width*rot[0]+height*rot[1])+x+width); //lower left
			xs[3]= (int)((-width*rot[0]-height*rot[1])+x+width); //upper left
			
			ys[0]= (int)(( width*rot[1]+height*rot[0])+y+height); //upper right
			ys[1]= (int)(( width*rot[1]-height*rot[0])+y+height); //lower right
			ys[2]= (int)((-width*rot[1]-height*rot[0])+y+height); //lower left
			ys[3]= (int)((-width*rot[1]+height*rot[0])+y+height); //upper left
				
			Polygon temp = new Polygon(xs,ys,4);
			
			if (temp.contains(pointx, pointy)) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	/**
	  *Method setLocation changes the int x and y coordinate to the x and y that are arguments for this method
	  */
	public void setLocation(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	/**
	  *Method getHelpers returns an array of rectangles on the shape, which are used to drag and transform the shape.
	  */
	public Rectangle[] getHelpers(double zf) {
		double theta = Math.toRadians(rotation);
		double[] rot = new double[2];
		
		rot[0] = Math.cos(theta);
		rot[1] = Math.sin(theta);
		
		Rectangle[] helpers = new Rectangle[3];
		
//		new Rectangle((int)(shape.x/zf + shape.width/(2*zf)),(int)(shape.y/zf + shape.height/(2*zf)),(int)(shape.width/zf),(int)(shape.height/zf))
		if (type == 0) {
			helpers[0] = new Rectangle( (int)((x/zf) + (width/zf)) - 2, (int)((y/zf) + (height/zf)) - 2, 5, 5);
			helpers[1] = new Rectangle( (int)((x/zf) + (width/zf)) - 2, (int)((y/zf) + (0.5*height/zf))- 2, 5, 5);
			helpers[2] = new Rectangle( (int)((x/zf) + (1.5*width/zf)) - 2, (int)((y/zf) + (height/zf)) - 2, 5, 5);
		}
		if (type == 1) {
			helpers[0] = new Rectangle( (int)((x/zf) + (width/zf)) - 2, (int)((y/zf) + (height/zf)) - 2, 5, 5);
			helpers[1] = new Rectangle( (int)((x/zf) + (width/zf)) - 2, (int)(y/zf)- 2, 5, 5);
			helpers[2] = new Rectangle( (int)((x/zf) + (2*width/zf)) - 2, (int)((y/zf) + (height/zf)) - 2, 5, 5);
		}
		
		return helpers;
	}
	

} //end of GmmlShape