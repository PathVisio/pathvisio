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
import java.awt.Rectangle;
/**
  *This class contains the braces. It contains a constructor, and the methods contains, setLocation and getHelpers
  */

public class GmmlBrace
{
	double cX, cY, w, ppo;
	int or; //or is the orientation: 0=top, 1=right, 2=bottom, 3=left
	Color color;
	
	/**
	  *Constructor GmmlBrace has 4 doubles for the coordinates, an int for the orientation and a string for the color as input. Width is the longest side of the brace, ppo the shortest side. This input is assigned to the object brace, but no real brace is constructed. Orientation is 0 for top, 1 for right, 2 for bottom or 3 for left.  
	  */
	public GmmlBrace(double centerX, double centerY, double width, double ppo, int orientation, String color)
	{
		cX=centerX;
		cY=centerY;
		w=width;
		this.ppo=ppo;
		or=orientation;
		this.color=GmmlColor.convertStringToColor(color);
		
	} //end constructor GmmlBrace

	/**
	  *Method contains uses the coordinates of a specific point (pointx, pointy) to determine whether a brace contains this point. 
	  *To do this, there is checked whether this point is in a certain rectangle.
	  */	 
	public boolean contains(double pointx, double pointy)
	{
		if (or==0 || or==2)
		{
			if (cX-0.5*w<=pointx&& pointx<=cX+0.5*w && cY-0.5*ppo<=pointy && pointy<=cY+0.5*ppo)
			{
				return true;
			}
			else
			{
				return false;
			}
		} //end if orientation
		else
		{
			if (cY-0.5*w<=pointy && pointy<=cY+0.5*w && cX-0.5*ppo<=pointx && pointx<=cX+0.5*ppo)
			{
				return true;
			}
			else
			{
				return false;
			}
		} // end else orientation
	} //end of contains
	
	/**
	  *Method setLocation changes the double centerX and centerY coordinate to the centerX and centerY that are arguments for this method
	  */
	public void setLocation(double centerX, double centerY)
	{
		cX = centerX;
		cY = centerY;
	}
	
	/**
	  *Method getHelpers returns an array of rectangles on the brace, which are used to drag and transform the brace.
	  */
	public Rectangle[] getHelpers(double zf)
	{
		Rectangle[] helpers = new Rectangle[2];
		helpers[0] = new Rectangle((int)(cX/zf) - 2 ,(int)(cY/zf) - 2, 5, 5);
		helpers[1] = new Rectangle();
		switch (or) {
			case 0:
				helpers[1].setBounds((int)((cX + (0.5*w))/zf) - 2 ,(int)(cY/zf) - 2, 5, 5);
				break;
			case 1: 
				helpers[1].setBounds((int)(cX/zf) - 2 ,(int)((cY + (0.5*w))/zf) - 2, 5, 5);
				break;
			case 2:
				helpers[1].setBounds((int)((cX - (0.5*w))/zf) - 2 ,(int)(cY/zf) - 2, 5, 5);
				break;
			case 3:
				helpers[1].setBounds((int)(cX/zf) - 2 ,(int)((cY - (0.5*w))/zf) - 2, 5, 5);
				break;
		}
		return helpers;
	}

} //end of GmmlBrace