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
  *This class contains the labels. It contains a constructor, and the methods contains, setLocation and getHelpers
  */

public class GmmlLabel {
	String text, font, fontWeight, fontStyle;
	int x, y, width, height, fontSize;
	Color color;
	
	/**
	  *Constructor GmmlLabel has 4 doubles for the coordinates, 4 Strings for the text, the font, the font weight and the font style, an int for the font size and a color object for the color as input.
	  */
	public GmmlLabel (int x, int y, int width, int height, String text, String font, String fontWeight, String fontStyle, int fontSize, Color color) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
		this.font = font;
		this.fontWeight = fontWeight;
		this.fontStyle = fontStyle;
		this.fontSize = fontSize;
		this.color = color;
	}
	
	/**
	  *Method contains uses the coordinates of a specific point (pointx, pointy) 
	  *to determine whether a label contains this point. 
	  *To do this, a 'real' rectangle object is formed, on which the normal contains method is used.
	  */	
	public boolean contains (double pointx, double pointy) {
		Rectangle rect = new Rectangle(x, y, width, height);
		boolean contains = rect.contains(pointx, pointy);
		return contains;
	}
	
	/**
	  *Method setLocation changes the int x and y coordinate to the x and y that are arguments for this method
	  */	
	public void setLocation(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	  *Method getHelpers returns an array of rectangles on the label, which are used to drag and transform the label.
	  */
	public Rectangle[] getHelpers(double zf) {
		Rectangle[] helpers = new Rectangle[1];
		
		helpers[0] = new Rectangle( (int)((x/zf) + (0.5*width/zf)) - 2, (int)((y/zf) + (0.5*height/zf)) - 2, 5, 5);
		
		return helpers;
	}

}
