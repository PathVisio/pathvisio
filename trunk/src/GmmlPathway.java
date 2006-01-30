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
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.JApplet;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D.*;
import java.awt.geom.AffineTransform;

/** GmmlPathway creates the objects geneProducts, lines, lineshapes, labels, arcs, braces and shapes.
  * Also the strings with the attributes, notes and comment are created.
  */
public class GmmlPathway {
	//Pathway
	int[] size = new int[2];
	
	//Geneproduct
	GmmlGeneProduct[] geneProducts = new GmmlGeneProduct[0];
		
	//Lines
	GmmlLine[] lines = new GmmlLine[0];
	
	//Lineshapes
	GmmlLineShape[] lineshapes = new GmmlLineShape[0];

	//Label
	GmmlLabel[] labels = new GmmlLabel[0];
	
	//Arc
	GmmlArc[] arcs = new GmmlArc[0];

	//Brace
	GmmlBrace[] braces = new GmmlBrace[0];

	//Shape
	GmmlShape[] shapes = new GmmlShape[0];
		
	//Attributes + notes element + comment element
	String[][] attributes = new String[0][2];
	String notes = new String();
	String comment = new String();

	/** setNotes sets the attribute notes of a specified object.
	  */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/** setComment sets the attribute Comment of a specified object.
	  */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/** addAttribute adds an attribute to the array of strings attributes.
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>attribute - type of the attribute
	  * <DD>value - value of the attribute
	  * </DL>
	  */
	public void addAttribute(String attribute, String value) {
		int length = attributes.length;
		
		//RESIZE PART
		attributes = (String[][]) resizeArray(attributes, (length+1));
		// new array is [length+1][2 or Null]
  		for (int i=0; i<attributes.length; i++) {
			if (attributes[i] == null) {
     			attributes[i] = new String[2];
			}
		}
		
		attributes[length][0] = attribute;
		attributes[length][1] = value;
	}

	/** addGeneProduct adds a geneproduct to the array of geneproducts.
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>x - upper left x-coordinate of the rectangle
	  * <DD>y - upper left y-coordinate of the rectangle
	  * <DD>width - width of the rectangle
	  * <DD>heigth - heigth of the rectangle
	  * <DD>geneID - ID of the gene
	  * <DD>ref - the reference of the gene
	  * </DL>
	  */	
	public void addGeneProduct(int x, int y, int width, int height, String geneID, String ref) {
		int length = geneProducts.length;
				
		//Resize part
		geneProducts = (GmmlGeneProduct[]) resizeArray(geneProducts, (length+1));
		GmmlGeneProduct temp = new GmmlGeneProduct(x,y,width,height,geneID,ref);
		geneProducts[length]=temp;
	}

	/** addLine adds a line to the array of lines.
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>startx - starting x-coordinate of the line
	  * <DD>starty - starting y-coordinate of the line
	  * <DD>endx - ending x-coordinate of the line
	  * <DD>endy - ending y-coordinate of the line
	  * <DD>type - type of the line; 0: line, 1: arrow
	  * <DD>style - style of the line: 0: solid line, 1: broken line
	  * <DD>colorstring - the color of the line
	  * </DL>
	  */		
	public void addLine(double startx, double starty, double endx, double endy, int type, int style, String colorstring) {
		int length = lines.length;
		
		//RESIZE PART
		lines = (GmmlLine[]) resizeArray(lines, (length+1));
		Color color = GmmlColor.convertStringToColor(colorstring);
		lines[length] = new GmmlLine(startx, starty, endx, endy, style, type, color);
	}

	/** addLabel adds a label to the array of labels.
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>x - upper left x-coordinate of the text
	  * <DD>y - upper left y-coordinate of the text
	  * <DD>width - width of the text
	  * <DD>heigth - heigth of the text
	  * <DD>text - the text
	  * <DD>color - the color of the text
	  * <DD>font - the font of the text
	  * <DD>weight - the weight of the text (bold or not)
	  * <DD>style - style of the font (italic or underlined)
	  * <DD>fontsize - size of the font
	  * </DL>
	  */		
	public void addLabel(int x, int y, int width, int height, String text, String color, String font, String weight, String style, int fontsize) {
		int length = labels.length;	

		//RESIZE PART
		labels = (GmmlLabel[]) resizeArray(labels, (length+1));

		labels[length] = new GmmlLabel(x, y, width, height, text, font, weight, style, fontsize, GmmlColor.convertStringToColor(color));
	}
	
	/** addArc adds an arc to the array of arcs.
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>x - starting x-coordinate of the arc
	  * <DD>y - starting y-coordinate of the arc
	  * <DD>width - width of the arc
	  * <DD>heigth - heigth of the arc
	  * <DD>color - color of the arc
	  * <DD>rotation - rotation of the arc in radians
	  * </DL>
	  */		
	public void addArc(double x, double y, double width, double height, String color, double rotation) {
		int length = arcs.length;
		
		//RESIZE PART
		arcs = (GmmlArc[]) resizeArray(arcs, (length+1));
		GmmlArc temp = new GmmlArc(x, y, width, height, color, rotation);
		arcs[length]=temp;
	}
	
	/** addLineShape adds a lineshape to the array of lineshapes.
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>startx - starting x-coordinate of the line
	  * <DD>starty - starting y-coordinate of the line
	  * <DD>endx - ending x-coordinate of the line
	  * <DD>endy - ending y-coordinate of the line
	  * <DD>scolor - the color of the line
	  * <DD>type - type of the end of the line; 0: tbar, 1: ReceptorRound, 2: LigandRound, 3: ReceptorSquare, 4: LigandSquare.
	  * </DL>
	  */	
	public void addLineShape(double startx, double starty, double endx, double endy, String scolor, int type) {
		int length = lineshapes.length;
		
		//RESIZE PART
		lineshapes = (GmmlLineShape[]) resizeArray(lineshapes, (length+1));
		Color color = GmmlColor.convertStringToColor(scolor);
		lineshapes[length] = new GmmlLineShape(startx, starty, endx, endy, type, color);
	}
	
	/** addBrace adds a brace to the array of braces.
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>centerX - center x-coordinate of the brace
	  * <DD>centerY - center y-coordinate of the brace
	  * <DD>width - width of the brace
	  * <DD>ppo - the PicPointOffset of the braces
	  * <DD>orientation - orientation of the brace; 0 refers to top, 1 refers to right, 2 refers to bottom, 3 refers to left
	  * <DD>color - the color of the brace
	  * </DL>
	  */		
	public void addBrace(double centerX, double centerY, double width, double ppo, int orientation, String color) {
		int length = braces.length;
				
		//Resize part
		braces = (GmmlBrace[]) resizeArray(braces, (length+1));
		braces[length] = new GmmlBrace(centerX,centerY,width,ppo,orientation,color);

	}

	public void addCellShape(double x, double y, double width, double height, double rotation) {
		//to do: make addCellShape
	}
	
	public void addCellComponent(double centerX, double centerY, int type) {
		//to do: make addCellComponent
	}
	
	public void addProteinComplex(double centerX, double centerY, int type) {
		//to do: make addCellComponent
	}		
	
	/** addGeneProduct adds a geneproduct to the array of geneproducts.
	  * <BR>
	  * <DL><B>Parameters</B>
	  * <DD>x - upper left x-coordinate of the shape
	  * <DD>y - upper left y-coordinate of the shape
	  * <DD>width - width of the shape
	  * <DD>heigth - heigth of the shape
	  * <DD>type - type of the shape; 0: rectangle, 1: ellips
	  * <DD>color - color of the shape
	  * <DD>rotation - rotation of the shape
	  * </DL>
	  */		
	public void addShape(double x, double y, double width, double height, int type, String color, double rotation) {
		int length = shapes.length;
		
		//RESIZE PART
		shapes = (GmmlShape[]) resizeArray(shapes, (length+1));
		shapes[length] = new GmmlShape(x, y, width, height, type, color, rotation);
	}
	
	public void setSize(int width, int height) {
		size[0] = width;
		size[1] = height;
	}
	
	/** echoAtt checks for stored attributes and prints those
	  */
	public void echoAtt() {
		System.out.println("Checking for stored attributes - number: "+attributes.length);
		for(int i=0; i<attributes.length; i++)
		{
			System.out.println("Attribute name: "+attributes[i][0]+ "value : "+attributes[i][1]);
		}
	}

	private static Object resizeArray (Object oldArray, int newSize)
	{
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType,newSize);
		int preserveLength = Math.min(oldSize,newSize);
		if (preserveLength > 0)
			System.arraycopy (oldArray,0,newArray,0,preserveLength);
		return newArray; 
	}
	

	
} //end of GmmlPathway
