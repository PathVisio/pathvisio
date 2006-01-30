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
import java.awt.Color;
/**
  *This class contains the gene products. It contains a constructor, and the methods contains, setLocation and getHelpers
  */

public class GmmlGeneProduct {
	int x, y, width, height;
	String geneID, ref;
	
	/**
	  *Constructor GmmlGeneProduct has 4 ints for the coordinates, a string for the geneID, and a string for the reference as input. This input is assigned to the object geneproduct, but no real rectangle object is constructed.
	  */
	public GmmlGeneProduct(int x, int y, int width, int height, String geneID, String ref) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.geneID = geneID;
		this.ref = ref;
	} //end of constructor
	
	/**
	  *Method contains uses the coordinates of a specific point (pointx, pointy) 
	  *to determine whether a geneproduct contains this point. 
	  *To do this, there is checked whether the point is in the area of the geneproduct.
	  */	
	public boolean contains(double pointx, double pointy) {
		if (x<=pointx && pointx<=x+width && y<=pointy && pointy<=y+height) {
			return true;
		}
		else {
			return false;
		}
	} //end of contains

	/**
	  *Method setLocation changes the int x and int y coordinate to the x and y that are arguments for this method
	  */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	  *Method getHelpers returns an array of rectangles on the geneproduct, which are used to drag and transform the geneproduct. The rectangles are in the middle of the geneproduct, in the middle of the upper line and in the middle of the right line of the geneproduct.
	  */
	public Rectangle[] getHelpers(double zf) {
		Rectangle[] helpers = new Rectangle[3];
		
		helpers[0] = new Rectangle( (int)((x/zf) + (0.5*width/zf)) - 2, (int)((y/zf) + (0.5*height/zf)) - 2, 5, 5);
		helpers[1] = new Rectangle( (int)((x/zf) + (0.5*width/zf)) - 2, (int)(y/zf) - 2, 5, 5);
		helpers[2] = new Rectangle( (int)((x/zf) + (width/zf)) - 2, (int)((y/zf) + (0.5*height/zf)) - 2, 5, 5);
		
		return helpers;
	}
	
} //end of GmmlGeneProduct