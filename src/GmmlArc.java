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

import java.awt.geom.Arc2D;
import java.awt.Color;
import java.awt.Rectangle;

/**
  *This class contains the arcs. It contains a constructor, and the methods contains, setLocation and getHelpers
  */
public class GmmlArc {

double x, y, width, height, rotation;
Color color;

	/**
	  *Constructor GmmlArc has 4 doubles for the coordinates and a string for the color as input. This input is assigned to the object arc, but no real arc is constructed.
	  */
	public GmmlArc(double x, double y, double width, double height, String color,double rotation)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = GmmlColor.convertStringToColor(color);
		this.rotation = Math.toDegrees(rotation);
		
	} //end of constructor GmmlArc
	
	/**
	  *Method contains uses the coordinates of a specific point (pointx, pointy) to determine whether an arc contains this point. 
	  *To do this, a 'real' arc object is formed, on which the normal contains method is used.
	  */
	public boolean contains(double pointx, double pointy)
	{
		Arc2D.Double arc = new Arc2D.Double(x-width,y-height,2*width,2*height,180-rotation,180,0);
		
		if (arc.contains(pointx,pointy))
		{
			return true;
		}
		else {
			return false;
		}
	}
	/**
	  *Method setLocation changes the double x and y coordinate to the x and y that are arguments for this method
	  */
	public void setLocation(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	/**
	  *Method getHelpers returns an array of rectangles on the arc, which are used to drag and transform the arc.
	  *TO DO: the rotation should be implemented correctly with a rotation matrix, but to do that, other methods that use the helpers should be changed too. (For example the size of the arc should be calculated correctly)
	  */
	public Rectangle[] getHelpers(double zf)
	{
		Rectangle[] helpers = new Rectangle[3];
		
		if(rotation <90)
		{
			helpers[0] = new Rectangle( (int)(x/zf) - 2, (int)(y/zf) - 2, 5, 5);
			helpers[1] = new Rectangle( (int)(x/zf) - 2, (int)((y/zf) + (height/zf)) - 2, 5, 5);
			helpers[2] = new Rectangle( (int)((x/zf) + (width/zf)) - 2, (int)(y/zf) - 2, 5, 5);
			return helpers;
		}
		else
		{
			helpers[0] = new Rectangle( (int)(x/zf) - 2, (int)(y/zf) - 2, 5, 5);
			helpers[1] = new Rectangle( (int)(x/zf) - 2, (int)((y/zf) - (height/zf)) - 2, 5, 5);
			helpers[2] = new Rectangle( (int)((x/zf) + (width/zf)) - 2, (int)(y/zf) - 2, 5, 5);
			
			return helpers;
		}
	}
} //end of GmmlArc
