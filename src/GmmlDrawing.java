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
import java.applet.Applet;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.Graphics2D.*;
import javax.swing.JPanel;

public class GmmlDrawing extends JPanel implements MouseListener, MouseMotionListener
{
	double zf = 15; //zoomfactor
	GmmlPathway pathway;
	GmmlConnection connection;
	BufferedImage bi;
	Graphics2D big;
	
	//Some variables for interactivity
	boolean mousePressedOnObject;
	int clickedObjectNumber;
	int clickedObjectType;
	
	boolean nonSelected = true;
	int selectedObjectNumber;
	int selectedObjectType;
	
	Rectangle[] helpers = new Rectangle[0];
	boolean clickedHelper = false;
	int selectedHelper;
	
	int slastx;
	int slasty;
	//End of the interactivity rewrite variables

	
	boolean firstTime = true;
	TexturePaint fillColor;
	Rectangle area; //area in which the rectangles are plotted.
	
	static protected Label label;
	
	GmmlDrawing(GmmlPathway pathway) {
		this.pathway = pathway;
		
		connection = new GmmlConnection(pathway);
					
		setBackground(Color.white);
		addMouseMotionListener(this);
		addMouseListener(this);
		
		setPreferredSize(new Dimension((int)(pathway.size[0]/zf),(int)(pathway.size[1]/zf)));
		setSize(new Dimension((int)(pathway.size[0]/zf),(int)(pathway.size[1]/zf)));
	} //end of GmmlDrawing(inputpathway)

	//init is used to form the JPanel later in the program.
	public void init(){
		//Dump the stored attributes to the screen.		
		System.out.println("Checking for stored attributes - number: "+pathway.attributes.length);
		for(int i=0; i<pathway.attributes.length; i++) {
			System.out.println("Attribute name: "+pathway.attributes[i][0]+ "value : "+pathway.attributes[i][1]);
		}
		
		//Initialize the layout.
		setLayout(new BorderLayout());
		
		//This label is used when the applet is just started
		label = new Label("Drag rectangles around within the area");
		//add("South", label); //South: in the lowest part of the frame.
	} //end of init
	
	/*When the mouse is pressed, there is checked with a for-loop if one clicked inside of a rectangle.
	 *If that is not the case, pressOut is true. If one clicks in a rectangle, the mouseEvent and the
	 *number of the rectangle are being sent to updateLocation.
	 *in rectClickArray, the rects in which there was clicked are true.
	 */
	 
	boolean pressOut = false; //true when one pressed or dragged or released outside the rectangles, false otherwise.		
	
	public void mousePressed(MouseEvent e){
		if(!nonSelected) {
			for(int i=helpers.length-1; i>=0; i--) {
				if(helpers[i].contains(e.getX(), e.getY())) {
					slastx = (int) (helpers[i].x - e.getX());
					slasty = (int) (helpers[i].y - e.getY());
					clickedHelper = true;
					selectedHelper = i;
					updateHelper(e);
					return;
				}
			}
		}
		
		clickedHelper = false;
		nonSelected = true;
		int CS = 0;
		
		for (int i=pathway.shapes.length-1; i>=0; i--) {
			if(pathway.shapes[i].contains(e.getX()*zf, e.getY()*zf)){ //if the user presses the mouses on a coordinate which is contained by rect
				
				mousePressedOnObject = true;
				clickedObjectNumber = i;		
				clickedObjectType = 7;
							
				slastx = (int) (pathway.shapes[i].x - e.getX()*zf); //lastx = position pathway.rects[i] - position mouse when pressed
				slasty = (int) (pathway.shapes[i].y - e.getY()*zf);
				
				updateLocation(e);
				
				//Test code for a possible new interactive way to handle objects, this should be closer to genmapp
				nonSelected = false;
				selectedObjectNumber = i;		
				selectedObjectType = 7;
				
				break;
			}
		}
		for (int i=pathway.arcs.length-1; i>=0; i--) {
			if(pathway.arcs[i].contains(e.getX()*zf, e.getY()*zf)){ //if the user presses the mouses on a coordinate which is contained by rect
				
				mousePressedOnObject = true;
				clickedObjectNumber = i;		
				clickedObjectType = 6;
							
				slastx = (int) (pathway.arcs[i].x - e.getX()*zf); //lastx = position pathway.rects[i] - position mouse when pressed
				slasty = (int) (pathway.arcs[i].y - e.getY()*zf);
				
				updateLocation(e);
				
				//Test code for a possible new interactive way to handle objects, this should be closer to genmapp
				nonSelected = false;
				selectedObjectNumber = i;		
				selectedObjectType = 6;
				
				break;
			}
		}
		for (int i=pathway.braces.length-1; i>=0; i--) {
			if(pathway.braces[i].contains(e.getX()*zf, e.getY()*zf)){ //if the user presses the mouses on a coordinate which is contained by rect
				
				mousePressedOnObject = true;
				clickedObjectNumber = i;		
				clickedObjectType = 5;
							
				slastx = (int) (pathway.braces[i].cX - e.getX()*zf); //lastx = position pathway.rects[i] - position mouse when pressed
				slasty = (int) (pathway.braces[i].cY - e.getY()*zf);
				
				updateLocation(e);
			
				//Test code for a possible new interactive way to handle objects, this should be closer to genmapp
				nonSelected = false;
				selectedObjectNumber = i;		
				selectedObjectType = 5;
				
				break;
			}
		}
		for (int i=pathway.lines.length-1; i>=0; i--) {
			if(pathway.lines[i].contains(e.getX()*zf, e.getY()*zf)){ //if the user presses the mouses on a coordinate which is contained by rect
				
				mousePressedOnObject = true;
				clickedObjectNumber = i;		
				clickedObjectType = 4;
							
				slastx = (int) (pathway.lines[i].startx - e.getX()*zf); //lastx = position pathway.rects[i] - position mouse when pressed
				slasty = (int) (pathway.lines[i].starty - e.getY()*zf);
				
				updateLocation(e);
				
				//Test code for a possible new interactive way to handle objects, this should be closer to genmapp
				nonSelected = false;
				selectedObjectNumber = i;		
				selectedObjectType = 4;
				
				break;
			}
		}

		for (int i=pathway.lineshapes.length-1; i>=0; i--) {
			if(pathway.lineshapes[i].contains(e.getX()*zf, e.getY()*zf)){ //if the user presses the mouses on a coordinate which is contained by rect
				
				mousePressedOnObject = true;
				clickedObjectNumber = i;		
				clickedObjectType = 3;
							
				slastx = (int) (pathway.lineshapes[i].startx - e.getX()*zf); //lastx = position pathway.rects[i] - position mouse when pressed
				slasty = (int) (pathway.lineshapes[i].starty - e.getY()*zf);
				
				updateLocation(e);
				
				//Test code for a possible new interactive way to handle objects, this should be closer to genmapp
				nonSelected = false;
				selectedObjectNumber = i;		
				selectedObjectType = 3;
				
				break;
			}
		}
		for (int i=pathway.geneProducts.length-1; i>=0; i--) {
			if(pathway.geneProducts[i].contains(e.getX()*zf, e.getY()*zf)){ //if the user presses the mouses on a coordinate which is contained by rect
				
				mousePressedOnObject = true;
				clickedObjectNumber = i;		
				clickedObjectType = 2;
							
				slastx = (int) (pathway.geneProducts[i].x - e.getX()*zf); //lastx = position pathway.rects[i] - position mouse when pressed
				slasty = (int) (pathway.geneProducts[i].y - e.getY()*zf);
				
				updateLocation(e);
				
				//Test code for a possible new interactive way to handle objects, this should be closer to genmapp
				nonSelected = false;
				selectedObjectNumber = i;		
				selectedObjectType = 2;
				
				break;
			}
		}
		for (int i=pathway.labels.length-1; i>=0; i--) {
			if(pathway.labels[i].contains(e.getX()*zf, e.getY()*zf)){ //if the user presses the mouses on a coordinate which is contained by rect
				
				mousePressedOnObject = true;
				clickedObjectNumber = i;		
				clickedObjectType = 1;
							
				slastx = (int) (pathway.labels[i].x - e.getX()*zf); //lastx = position pathway.rects[i] - position mouse when pressed
				slasty = (int) (pathway.labels[i].y - e.getY()*zf);
				
				updateLocation(e);
				
				//Test code for a possible new interactive way to handle objects, this should be closer to genmapp
				nonSelected = false;
				selectedObjectNumber = i;		
				selectedObjectType = 1;
				
				break;
			}
		}
		repaint();	
	} //end of mousePressed
	
	public void mouseDragged(MouseEvent e) {
		if (clickedHelper) {
			updateHelper(e);
		}
		else if (mousePressedOnObject) { //always mousePressed before mouseDragged -> pressOut true when start dragging outside of rect.
		 	updateLocation(e);
		} 
		else {  
			label.setText("First position the cursor on the rectangle and then drag.");
		}
	} //end of mouseDragged
	
	// Handles the event of a user releasing the mouse button. Sets pressOut back on false.
	public void mouseReleased(MouseEvent e){
		// Checks whether or not the cursor is inside of the rectangle when the user releases the mouse button.   
		mousePressedOnObject = false;
	} //end of mouseReleased
	
	// This method required by MouseListener, it does nothing
	public void mouseMoved(MouseEvent e){}

   // These methods are required by MouseMotionListener, they do nothing
	public void mouseClicked(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	
	//updateLocation
	private void updateLocation(MouseEvent e){
		updateLocation(slastx + e.getX()*zf, slasty + e.getY()*zf);
	}
	
	//updateLocation
	private void updateLocation(double new_x, double new_y) {
		double[] newCoords;
		double width, height, xcoord, ycoord;
		switch (clickedObjectType) {
			case 1: //label
				newCoords = checkRect(new_x, new_y, pathway.labels[clickedObjectNumber].width, pathway.labels[clickedObjectNumber].height);
				pathway.labels[clickedObjectNumber].setLocation((int)newCoords[0] , (int)newCoords[1]);
				break;
			case 2: //geneProduct
				newCoords = checkRect(new_x, new_y, pathway.geneProducts[clickedObjectNumber].width, pathway.geneProducts[clickedObjectNumber].height);
				pathway.geneProducts[clickedObjectNumber].setLocation((int)newCoords[0] , (int)newCoords[1]);
				break;
			case 3: //lineshape
				if(pathway.lineshapes[clickedObjectNumber].endx >= pathway.lineshapes[clickedObjectNumber].startx) {
					width = pathway.lineshapes[clickedObjectNumber].endx-pathway.lineshapes[clickedObjectNumber].startx;
					xcoord = new_x;
					if (pathway.lineshapes[clickedObjectNumber].endy >= pathway.lineshapes[clickedObjectNumber].starty) {
						height = pathway.lineshapes[clickedObjectNumber].endy-pathway.lineshapes[clickedObjectNumber].starty;
						ycoord = new_y;
						newCoords = checkRect(xcoord, ycoord , width, height);
						pathway.lineshapes[clickedObjectNumber].setLocation((int)newCoords[0] , (int)newCoords[1]);
					} else {
						height = pathway.lineshapes[clickedObjectNumber].starty-pathway.lineshapes[clickedObjectNumber].endy;
						ycoord = new_y - height;
						newCoords = checkRect(xcoord, ycoord , width, height);
						pathway.lineshapes[clickedObjectNumber].setLocation((int)newCoords[0] , (int)newCoords[1] + height);
					}
				} else {
					width = pathway.lineshapes[clickedObjectNumber].startx-pathway.lineshapes[clickedObjectNumber].endx;
					xcoord = new_x - width;
					if (pathway.lineshapes[clickedObjectNumber].endy >= pathway.lineshapes[clickedObjectNumber].starty) {
						height = pathway.lineshapes[clickedObjectNumber].endy-pathway.lineshapes[clickedObjectNumber].starty;
						ycoord = new_y;
						newCoords = checkRect(xcoord, ycoord , width, height);
						pathway.lineshapes[clickedObjectNumber].setLocation((int)newCoords[0] + width , (int)newCoords[1]);
					} else {
						height = pathway.lineshapes[clickedObjectNumber].starty-pathway.lineshapes[clickedObjectNumber].endy;
						ycoord = new_y - height;
						newCoords = checkRect(xcoord, ycoord , width, height);
						pathway.lineshapes[clickedObjectNumber].setLocation((int)newCoords[0] + width , (int)newCoords[1] + height);
					}
				}
				break;
			case 4: //line
				if(pathway.lines[clickedObjectNumber].endx >= pathway.lines[clickedObjectNumber].startx) {
					width = pathway.lines[clickedObjectNumber].endx-pathway.lines[clickedObjectNumber].startx;
					xcoord = new_x;
					if (pathway.lines[clickedObjectNumber].endy >= pathway.lines[clickedObjectNumber].starty) {
						height = pathway.lines[clickedObjectNumber].endy-pathway.lines[clickedObjectNumber].starty;
						ycoord = new_y;
						newCoords = checkRect(xcoord, ycoord , width, height);
						pathway.lines[clickedObjectNumber].setLocation((int)newCoords[0] , (int)newCoords[1]);
					} else {
						height = pathway.lines[clickedObjectNumber].starty-pathway.lines[clickedObjectNumber].endy;
						ycoord = new_y - height;
						newCoords = checkRect(xcoord, ycoord , width, height);
						pathway.lines[clickedObjectNumber].setLocation((int)newCoords[0] , (int)newCoords[1] + height);
					}
				} else {
					width = pathway.lines[clickedObjectNumber].startx-pathway.lines[clickedObjectNumber].endx;
					xcoord = new_x - width;
					if (pathway.lines[clickedObjectNumber].endy >= pathway.lines[clickedObjectNumber].starty) {
						height = pathway.lines[clickedObjectNumber].endy-pathway.lines[clickedObjectNumber].starty;
						ycoord = new_y;
						newCoords = checkRect(xcoord, ycoord , width, height);
						pathway.lines[clickedObjectNumber].setLocation((int)newCoords[0] + width , (int)newCoords[1]);
					} else {
						height = pathway.lines[clickedObjectNumber].starty-pathway.lines[clickedObjectNumber].endy;
						ycoord = new_y - height;
						newCoords = checkRect(xcoord, ycoord , width, height);
						pathway.lines[clickedObjectNumber].setLocation((int)newCoords[0] + width , (int)newCoords[1] + height);
					}
				}
				break;
			case 5: //brace
				if(pathway.braces[clickedObjectNumber].or==1 || pathway.braces[clickedObjectNumber].or==3) {
					newCoords = checkRect(new_x-3, new_y - 0.5*pathway.braces[clickedObjectNumber].w, 7, pathway.braces[clickedObjectNumber].w);
				pathway.braces[clickedObjectNumber].setLocation((int)newCoords[0]+3, (int)newCoords[1] + (0.5*pathway.braces[clickedObjectNumber].w));
				} else {
					newCoords = checkRect(new_x - 0.5*pathway.braces[clickedObjectNumber].w, new_y - 3, pathway.braces[clickedObjectNumber].w, 7);
					pathway.braces[clickedObjectNumber].setLocation((int)newCoords[0] + (0.5*pathway.braces[clickedObjectNumber].w), (int)newCoords[1] + 3);
				}
				break;
			case 6: //arc
				newCoords = checkRect(new_x - (pathway.arcs[clickedObjectNumber].width), new_y - (pathway.arcs[clickedObjectNumber].height) , 2*pathway.arcs[clickedObjectNumber].width, 1*pathway.arcs[clickedObjectNumber].height);
				pathway.arcs[clickedObjectNumber].setLocation((int)newCoords[0]+(pathway.arcs[clickedObjectNumber].width) , (int)newCoords[1]+(1*pathway.arcs[clickedObjectNumber].height));
				break;
			case 7: //shape
				if(pathway.shapes[clickedObjectNumber].type == 0) {
					newCoords = checkRect(new_x + (0.5*pathway.shapes[clickedObjectNumber].width), new_y + (0.5*pathway.shapes[clickedObjectNumber].height) , pathway.shapes[clickedObjectNumber].width, pathway.shapes[clickedObjectNumber].height);
					pathway.shapes[clickedObjectNumber].setLocation((int)newCoords[0]-(0.5*pathway.shapes[clickedObjectNumber].width) , (int)newCoords[1]-(0.5*pathway.shapes[clickedObjectNumber].height));
				} else {
					newCoords = checkRect(new_x, new_y, 2*pathway.shapes[clickedObjectNumber].width, 2*pathway.shapes[clickedObjectNumber].height);
					pathway.shapes[clickedObjectNumber].setLocation((int)newCoords[0] , (int)newCoords[1]);
				}
				break;
		}
		repaint(); //The component will be repainted after all of the currently pending events have been dispatched
	}
	
	private void updateHelper(MouseEvent e) {
		double[] newCoord;
		switch (selectedObjectType) {
			case 1:
				switch (selectedHelper) {
					case 0:
						helpers[0].setLocation((int)(slastx + e.getX()), (int)(slasty + e.getY()));
						updateLocation((int)((2+helpers[0].x)*zf) - (int)(0.5*pathway.labels[selectedObjectNumber].width),(int)((2+helpers[0].y)*zf) - (int)(0.5*pathway.labels[selectedObjectNumber].height));
						break;
				}
				break;
			case 2:
				switch (selectedHelper) {
					case 0:
						helpers[0].setLocation((int)(slastx + e.getX()), (int)(slasty + e.getY()));
						updateLocation((int)((2+helpers[0].x)*zf) - (int)(0.5*pathway.geneProducts[selectedObjectNumber].width),(int)((2+helpers[0].y)*zf) - (int)(0.5*pathway.geneProducts[selectedObjectNumber].height));
						break;
					case 1:
						int oldheight = pathway.geneProducts[selectedObjectNumber].height;
						if ((slasty + e.getY()) < helpers[0].y) {
							newCoord = checkPoint(helpers[1].x, (int)(slasty + e.getY()));
						} else {
							newCoord = checkPoint(helpers[1].x, helpers[0].y - 1);
						}
						helpers[1].setLocation((int)newCoord[0],(int)newCoord[1]);
						pathway.geneProducts[selectedObjectNumber].height = Math.abs((int)(2 * zf * (helpers[0].y - helpers[1].y)));
						pathway.geneProducts[selectedObjectNumber].y -= (int)(0.5 * (pathway.geneProducts[selectedObjectNumber].height - oldheight));
						break;
					case 2:
						int oldwidth = pathway.geneProducts[selectedObjectNumber].width;
						if ((slastx + e.getX()) > helpers[0].x) {
							newCoord = checkPoint(slastx + e.getX(), helpers[2].y);
						} else {
							newCoord = checkPoint(helpers[0].x + 1, helpers[2].y);
						}
						helpers[2].setLocation((int)newCoord[0],(int)newCoord[1]);
						pathway.geneProducts[selectedObjectNumber].width = Math.abs((int)(2 * zf * (helpers[2].x - helpers[0].x)));
						pathway.geneProducts[clickedObjectNumber].x -= (int)(0.5 * (pathway.geneProducts[selectedObjectNumber].width - oldwidth));
						break;
				}
				break;
			case 3:
				switch (selectedHelper) {
					case 0:
						newCoord = checkPoint(slastx + e.getX(), slasty + e.getY());
						helpers[0].setLocation((int) newCoord[0], (int)newCoord[1]);
						pathway.lineshapes[selectedObjectNumber].startx = (2+helpers[0].x)*zf;
						pathway.lineshapes[selectedObjectNumber].starty = (2+helpers[0].y)*zf;
						break;
					case 1:
						newCoord = checkPoint(slastx + e.getX(), slasty + e.getY());
						helpers[1].setLocation((int) newCoord[0], (int)newCoord[1]);
						pathway.lineshapes[selectedObjectNumber].endx = (2+helpers[1].x)*zf;
						pathway.lineshapes[selectedObjectNumber].endy = (2+helpers[1].y)*zf;
						break;
				}
				break;
			case 4:
				switch (selectedHelper) {
					case 0:
						newCoord = checkPoint(slastx + e.getX(), slasty + e.getY());
						helpers[0].setLocation((int) newCoord[0], (int)newCoord[1]);
						pathway.lines[selectedObjectNumber].startx = (2+helpers[0].x)*zf;
						pathway.lines[selectedObjectNumber].starty = (2+helpers[0].y)*zf;
						break;
					case 1:
						newCoord = checkPoint(slastx + e.getX(), slasty + e.getY());
						helpers[1].setLocation((int) newCoord[0], (int)newCoord[1]);
						pathway.lines[selectedObjectNumber].endx = (2+helpers[1].x)*zf;
						pathway.lines[selectedObjectNumber].endy = (2+helpers[1].y)*zf;
						break;
				}
				break;
			case 5:
				switch (selectedHelper) {
					case 0:
						helpers[0].setLocation((int)(slastx + e.getX()), (int)(slasty + e.getY()));
						updateLocation((2+helpers[0].x)*zf, (2+helpers[0].y)*zf);
						break;
					case 1:
						if(pathway.braces[selectedObjectNumber].or==0 || pathway.braces[selectedObjectNumber].or==2) {
							newCoord = checkPoint((int)(slastx + e.getX()), helpers[1].y);
							helpers[1].setLocation((int) newCoord[0], (int)newCoord[1]);
							pathway.braces[selectedObjectNumber].w = zf*2*Math.abs(helpers[0].x-helpers[1].x);
						} else if (pathway.braces[selectedObjectNumber].or==1 || pathway.braces[selectedObjectNumber].or==3) {
							newCoord = checkPoint(helpers[1].x, (int)(slasty + e.getY()));
							helpers[1].setLocation((int) newCoord[0], (int)newCoord[1]);
							pathway.braces[selectedObjectNumber].w = zf*2*Math.abs(helpers[0].y-helpers[1].y); 
						}
						break;
				}
				break;
			case 6:
				switch (selectedHelper) {
					case 0:
						helpers[0].setLocation((int)(slastx + e.getX()), (int)(slasty + e.getY()));
						updateLocation((int)((2+helpers[0].x)*zf),(int)((2+helpers[0].y)*zf));
						break;
					case 1:
						double oldheight = pathway.arcs[selectedObjectNumber].height;
						newCoord = checkPoint(helpers[1].x, (int)(slasty + e.getY()));
						helpers[1].setLocation((int) newCoord[0], (int)newCoord[1]);
						pathway.arcs[selectedObjectNumber].height = Math.abs((int)(zf * (helpers[0].y - helpers[1].y)));
						break;
					case 2:
						double oldwidth = pathway.arcs[selectedObjectNumber].width;
						newCoord = checkPoint((int)(slastx + e.getX()), helpers[2].y);
						helpers[2].setLocation((int) newCoord[0], (int)newCoord[1]);
						pathway.arcs[selectedObjectNumber].width = Math.abs((int)(zf * (helpers[2].x - helpers[0].x)));
						break;
				}
				break;
			case 7:
				switch (selectedHelper) {
					case 0:
						helpers[0].setLocation((int)(slastx + e.getX()), (int)(slasty + e.getY()));
						updateLocation((int)((2+helpers[0].x)*zf) - (int)(pathway.shapes[selectedObjectNumber].width),(int)((2+helpers[0].y)*zf) - (int)(pathway.shapes[selectedObjectNumber].height));
						break;
					case 1:
						double oldheight = pathway.shapes[selectedObjectNumber].height;
						newCoord = checkPoint(helpers[1].x, (int)(slasty + e.getY()));
						helpers[1].setLocation((int) newCoord[0], (int)newCoord[1]);
						if(pathway.shapes[selectedObjectNumber].type==0) {
							pathway.shapes[selectedObjectNumber].height = Math.abs((int)(2* zf * (helpers[0].y - helpers[1].y)));
						} else if(pathway.shapes[selectedObjectNumber].type==1) {
							pathway.shapes[selectedObjectNumber].height = Math.abs((int)(zf * (helpers[0].y - helpers[1].y)));
						}
						pathway.shapes[selectedObjectNumber].y -= (int)(pathway.shapes[selectedObjectNumber].height - oldheight);
						break;
					case 2:
						double oldwidth = pathway.shapes[selectedObjectNumber].width;
						newCoord = checkPoint((int)(slastx + e.getX()), helpers[2].y);
						helpers[2].setLocation((int) newCoord[0], (int)newCoord[1]);
						if(pathway.shapes[selectedObjectNumber].type==0) {
							pathway.shapes[selectedObjectNumber].width = Math.abs((int)(2 * zf * (helpers[2].x - helpers[0].x)));
						} else if(pathway.shapes[selectedObjectNumber].type==1) {
							pathway.shapes[selectedObjectNumber].width = Math.abs((int)(zf * (helpers[2].x - helpers[0].x)));
						}
						pathway.shapes[selectedObjectNumber].x -= (int)(pathway.shapes[selectedObjectNumber].width - oldwidth);
						break;
				}
				break;

		} 
		repaint(); //The component will be repainted after all of the currently pending events have been dispatched
	}
	public void paint(Graphics g){
		update(g);
	}

	public void update(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

			if(firstTime){
				Dimension dim = getSize(); //Size of frame f
				int w = dim.width;
				int h = dim.height;
				area = new Rectangle(dim);
				bi = (BufferedImage)createImage(w, h);
				big = bi.createGraphics();
				big.setBackground(Color.white);
				big.setStroke(new BasicStroke(8.0f));
				firstTime = false;
			} 

		//Clears the rectangle that was previously drawn.
		big.setColor(Color.white);
		big.clearRect(0, 0, area.width, area.height);

		//Draw shapes
		for(int i=0; i<pathway.shapes.length; i++) {
			drawShape(pathway.shapes[i]);
		}
		
		//Draw arcs
		for (int i=0; i<pathway.arcs.length; i++) {
			drawArc(pathway.arcs[i]);
		}
		
		//Draw braces
		for (int i=0; i<pathway.braces.length; i++) {
			drawBrace(pathway.braces[i]);
		}
		 
		//Draws lines
		for (int i=0; i<pathway.lines.length; i++) {
			drawLine(pathway.lines[i]);
		}
		
		//Draws lineshapes
		for (int i=0; i<pathway.lineshapes.length; i++) {
			drawLineShape(pathway.lineshapes[i]);
		}
		
		for (int i=0; i<connection.Connection.length; i++)  {
			big.setColor(Color.orange);
			big.setStroke(new BasicStroke(2.0f));
			double x1 = 0;
			double y1 = 0;
			double x2 = 0;
			double y2 = 0;
			switch (connection.Connection[i][4]) {
				case 1:
					x1 = pathway.labels[connection.Connection[i][2]].x + 0.5 * pathway.labels[connection.Connection[i][2]].width;
					y1 = pathway.labels[connection.Connection[i][2]].y + 0.5 * pathway.labels[connection.Connection[i][2]].height;
					break;
				case 2:
					x1 = pathway.geneProducts[connection.Connection[i][2]].x + 0.5 * pathway.geneProducts[connection.Connection[i][2]].width;
					y1 = pathway.geneProducts[connection.Connection[i][2]].y + 0.5 * pathway.geneProducts[connection.Connection[i][2]].height;
					break;
				case 3:
					System.out.println("Invalid connection type: 3");
					break;
				case 4:
					System.out.println("Invalid connection type: 4");
					break;
				case 5:
					x1 = pathway.braces[connection.Connection[i][2]].cX;
					y1 = pathway.braces[connection.Connection[i][2]].cY;
					break;
				case 6:
					x1 = pathway.arcs[connection.Connection[i][2]].x + 0.5 * pathway.arcs[connection.Connection[i][2]].width;
					y1 = pathway.arcs[connection.Connection[i][2]].y + 0.5 * pathway.arcs[connection.Connection[i][2]].height;
					break;
				case 7:
					x1 = pathway.shapes[connection.Connection[i][2]].x + 0.5 * pathway.shapes[connection.Connection[i][2]].width;
					y1 = pathway.shapes[connection.Connection[i][2]].y + 0.5 * pathway.shapes[connection.Connection[i][2]].height;
					break;
				case 8:
					if(connection.Connection[i][1]==3) {
						x1 = pathway.lineshapes[connection.Connection[i][0]].startx;
						y1 = pathway.lineshapes[connection.Connection[i][0]].starty;
					}
					if(connection.Connection[i][1]==4) {
						x1 = pathway.lines[connection.Connection[i][0]].startx;
						y1 = pathway.lines[connection.Connection[i][0]].starty;
					}

					break;
			}
			switch (connection.Connection[i][5]) {
				case 1:
					x2 = pathway.labels[connection.Connection[i][3]].x + 0.5 * pathway.labels[connection.Connection[i][3]].width;
					y2 = pathway.labels[connection.Connection[i][3]].y + 0.5 * pathway.labels[connection.Connection[i][3]].height;
					break;
				case 2:
					x2 = pathway.geneProducts[connection.Connection[i][3]].x + 0.5 * pathway.geneProducts[connection.Connection[i][3]].width;
					y2 = pathway.geneProducts[connection.Connection[i][3]].y + 0.5 * pathway.geneProducts[connection.Connection[i][3]].height;
					break;
				case 3:
					System.out.println("Invalid connection type: 3");
					break;
				case 4:
					System.out.println("Invalid connection type: 4");
					break;
				case 5:
					x2 = pathway.braces[connection.Connection[i][3]].cX;
					y2 = pathway.braces[connection.Connection[i][3]].cY;
					break;
				case 6:
					x2 = pathway.arcs[connection.Connection[i][3]].x + 0.5 * pathway.arcs[connection.Connection[i][3]].width;
					y2 = pathway.arcs[connection.Connection[i][3]].y + 0.5 * pathway.arcs[connection.Connection[i][3]].height;
					break;
				case 7:
					x2 = pathway.shapes[connection.Connection[i][3]].x + 0.5 * pathway.shapes[connection.Connection[i][3]].width;
					y2 = pathway.shapes[connection.Connection[i][3]].y + 0.5 * pathway.shapes[connection.Connection[i][3]].height;
					break;
				case 8:
					if(connection.Connection[i][1]==3) {
						x2 = pathway.lineshapes[connection.Connection[i][0]].endx;
						y2 = pathway.lineshapes[connection.Connection[i][0]].endy;
					}
					if(connection.Connection[i][1]==4) {
						x2 = pathway.lines[connection.Connection[i][0]].endx;
						y2 = pathway.lines[connection.Connection[i][0]].endy;
					}
					break;
			}
			
			if(connection.Connection[i][1]==3) {
				GmmlLineShape lineshape = new GmmlLineShape(x1, y1, x2, y2, pathway.lines[connection.Connection[i][0]].type, Color.red);;
				drawLineShape(lineshape);
			}
			if(connection.Connection[i][1]==4) {
				GmmlLine line = new GmmlLine(x1, y1, x2, y2, pathway.lines[connection.Connection[i][0]].type, pathway.lines[connection.Connection[i][0]].style, Color.red);
				drawLine(line);
			}
					
			/*if(x1==0 || y1==0 || x2==0 || y2==0) {
				System.out.println("x1: "+x1+" y1: "+y1+" x2: "+x2+" y2: "+y2);
				System.out.println("Type a: "+connection.Connection[i][4]+" Type a: "+connection.Connection[i][5]);
			}
			big.draw(new Line2D.Double(x1/zf,y1/zf,x2/zf,y2/zf)); */
		}
		
		//Draw geneproducts
		for (int i=0; i<pathway.geneProducts.length; i++) {
			drawGeneProduct(pathway.geneProducts[i]);
		}
		
		//Draw text labels
		for (int i=0; i<pathway.labels.length; i++) {
			drawLabel(pathway.labels[i]);
		}
		
		//Draw helpers for modifying the drawing	
		if(!nonSelected) {
			drawHelpers();
		}
		
		// Draws the buffered image to the screen.
		g2.drawImage(bi, 0, 0, this);
		
	} //end of update
	
	public void drawShape (GmmlShape shape) {
		big.setStroke(new BasicStroke(1.0f));
		big.setColor(shape.color);
		big.rotate(Math.toRadians(shape.rotation), (shape.x/zf + shape.width/zf), (shape.y/zf + shape.height/zf));
		if (shape.type == 0) {
			big.draw(new Rectangle((int)(shape.x/zf + shape.width/(2*zf)),(int)(shape.y/zf + shape.height/(2*zf)),(int)(shape.width/zf),(int)(shape.height/zf)));
		} else if (shape.type == 1) {
			big.draw(new Ellipse2D.Double(shape.x/zf,shape.y/zf,2*shape.width/zf,2*shape.height/zf));
		}
		big.rotate(-Math.toRadians(shape.rotation),  (shape.x/zf + shape.width/zf), (shape.y/zf + shape.height/zf));  //reset rotation
	}
	
	public void drawLine (GmmlLine line) {
		big.setColor(line.color);
		float[] dash = {3.0f};
		if (line.style==0) {
			big.setStroke(new BasicStroke(1.0f));
		}
		else if (line.style==1){ 
			big.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
		}
		Line2D.Double drawline = new Line2D.Double(line.startx/zf,line.starty/zf,line.endx/zf,line.endy/zf);
		big.draw(drawline);
		if (line.type==1) {
			drawArrowHead(line);
		}
	}
	
	public void drawArrowHead (GmmlLine line) {
		//Creates arrowhead
		big.setColor(line.color);
		double angle = 25.0;
		double theta = Math.toRadians(180 - angle);
		double[] rot = new double[2];
		double[] p = new double[2];
		double[] q = new double[2];
		double a, b, norm;
		
		rot[0] = Math.cos(theta);
		rot[1] = Math.sin(theta);
		
		big.setStroke(new BasicStroke(1.0f));

		a = line.endx-line.startx;
		b = line.endy-line.starty;
		norm = 8/(Math.sqrt((a*a)+(b*b)));				
		p[0] = ( a*rot[0] + b*rot[1] ) * norm + line.endx/zf;
		p[1] = (-a*rot[1] + b*rot[0] ) * norm + line.endy/zf;
		q[0] = ( a*rot[0] - b*rot[1] ) * norm + line.endx/zf;
		q[1] = ( a*rot[1] + b*rot[0] ) * norm + line.endy/zf;
		int[] x = {(int) (line.endx/zf),(int) (p[0]),(int) (q[0])};
		int[] y = {(int) (line.endy/zf),(int) (p[1]),(int) (q[1])};
		Polygon arrowhead = new Polygon(x,y,3);
		big.draw(arrowhead);
		big.fill(arrowhead);
	}
	
	public void drawLineShape (GmmlLineShape lineshape) {
		big.setColor(lineshape.color);
		big.setStroke(new BasicStroke(1.0f));
		
		//Types:
		// 0 - Tbar
		// 1 - Receptor round
		// 2 - Ligand round
		// 3 - Receptor square
		// 4 - Ligand square
		if (lineshape.type==0) {
			double x1 = lineshape.startx/zf;
			double x2 = lineshape.endx/zf;
			double y1 = lineshape.starty/zf;
			double y2 = lineshape.endy/zf;
			
			Line2D.Double drawline = new Line2D.Double(x1,y1,x2,y2);
			big.draw(drawline);
			
			double s  = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1))) / 8;
			
			double capx1 = ((-y2 + y1)/s) + x2;
			double capy1 = (( x2 - x1)/s) + y2;
			double capx2 = (( y2 - y1)/s) + x2;
			double capy2 = ((-x2 + x1)/s) + y2;
			
			Line2D.Double drawcap = new Line2D.Double(capx1,capy1,capx2,capy2);
			big.draw(drawcap);
		}
		if (lineshape.type==1) {
			double x1 = lineshape.startx/zf;
			double x2 = lineshape.endx/zf;
			double y1 = lineshape.starty/zf;
			double y2 = lineshape.endy/zf;
			
			double s  = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
						
			double dx = (x2-x1)/s;
			double dy = (y2-y1)/s;
						
			Line2D.Double drawline = new Line2D.Double(x1,y1,x2-(6*dx),y2-(6*dy));
			big.draw(drawline);
			
			Ellipse2D.Double ligandround = new Ellipse2D.Double(x2-5, y2-5, 10, 10);
			big.draw(ligandround);
			big.fill(ligandround);

		}
		if (lineshape.type==2) {
			double x1 = lineshape.startx/zf;
			double x2 = lineshape.endx/zf;
			double y1 = lineshape.starty/zf;
			double y2 = lineshape.endy/zf;
			
			double theta = Math.toDegrees(Math.atan((x2-x1)/(y2-y1)));
			
			double s  = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
			
			double dx = (x2-x1)/s;
			double dy = (y2-y1)/s;
						
			Line2D.Double drawline = new Line2D.Double(x1,y1,x2-(8*dx),y2-(8*dy));
			big.draw(drawline);
			
			Arc2D.Double receptorround = new Arc2D.Double(x2-8, y2-8, 16, 16, theta+180, -180, Arc2D.OPEN);
			big.draw(receptorround);
		}
		if (lineshape.type==3) {
			double x1 = lineshape.startx/zf;
			double x2 = lineshape.endx/zf;
			double y1 = lineshape.starty/zf;
			double y2 = lineshape.endy/zf;
			
			double s  = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1))) / 8;
			
			double x3 = x2 - ((x2-x1)/s);
			double y3 = y2 - ((y2-y1)/s);
			
			Line2D.Double drawline = new Line2D.Double(x1,y1,x3,y3);
			big.draw(drawline);
			
			double capx1 = ((-y2 + y1)/s) + x3;
			double capy1 = (( x2 - x1)/s) + y3;
			double capx2 = (( y2 - y1)/s) + x3;
			double capy2 = ((-x2 + x1)/s) + y3;
			
			Line2D.Double drawcap = new Line2D.Double(capx1,capy1,capx2,capy2);
			big.draw(drawcap);
			
			double rx1 = capx1 + 1.5*(x2-x1)/s;
			double ry1 = capy1 + 1.5*(y2-y1)/s;
			double rx2 = capx2 + 1.5*(x2-x1)/s;
			double ry2 = capy2 + 1.5*(y2-y1)/s;
			
			Line2D.Double receptor1 = new Line2D.Double(capx1,capy1,rx1,ry1);
			big.draw(receptor1);
			Line2D.Double receptor2 = new Line2D.Double(capx2,capy2,rx2,ry2);
			big.draw(receptor2);
		}
		if (lineshape.type==4) {
			double x1 = lineshape.startx/zf;
			double x2 = lineshape.endx/zf;
			double y1 = lineshape.starty/zf;
			double y2 = lineshape.endy/zf;
			
			double s  = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1))) / 6;
			
			double x3 = x2 - ((x2-x1)/s);
			double y3 = y2 - ((y2-y1)/s);
			
			Line2D.Double drawline = new Line2D.Double(x1,y1,x3,y3);
			big.draw(drawline);
			
			int[] polyx = new int[4];
			int[] polyy = new int[4];
			
			polyx[0] = (int) (((-y2 + y1)/s) + x3);
			polyy[0] = (int) ((( x2 - x1)/s) + y3);
			polyx[1] = (int) ((( y2 - y1)/s) + x3);
			polyy[1] = (int) (((-x2 + x1)/s) + y3);

			polyx[2] = (int) (polyx[1] + 1.5*(x2-x1)/s);
			polyy[2] = (int) (polyy[1] + 1.5*(y2-y1)/s);			
			polyx[3] = (int) (polyx[0] + 1.5*(x2-x1)/s);
			polyy[3] = (int) (polyy[0] + 1.5*(y2-y1)/s);
	
			Polygon ligandsquare  = new Polygon(polyx,polyy,4);
			big.draw(ligandsquare);
			big.fill(ligandsquare);
		}
		
	}
	
	// Draws the Geneproduct.
	public void drawGeneProduct (GmmlGeneProduct geneproduct) {
		Rectangle rect = new Rectangle((int)(geneproduct.x/zf),(int)(geneproduct.y/zf),(int)(geneproduct.width/zf),(int)(geneproduct.height/zf));
		
		big.setColor(Color.black);
		big.setStroke(new BasicStroke(2.0f));
		big.draw(rect);
		big.setColor(Color.white);
		big.fill(rect);
				
		// Draws text on the newly positioned rectangles.
		Font gpfont = new Font("Arial", Font.PLAIN, (int)(150/zf));
		big.setFont(gpfont);
		
		big.setColor(Color.black);
		big.setStroke(new BasicStroke(1.0f));

		FontMetrics fm = big.getFontMetrics();
		
		int rectWidth = geneproduct.width;
		int rectHeight = geneproduct.height;
		int textWidth = fm.stringWidth(geneproduct.geneID);
		int fHeight = fm.getHeight();
				
		int x = (int)(geneproduct.x + (rectWidth  - zf * textWidth) / 2);
		int y = (int)(geneproduct.y + (rectHeight + zf * fHeight  ) / 2);
		
		big.drawString(geneproduct.geneID,(int)(x/zf),(int)(y/zf));
	}
	
	//Draw a label
	public void drawLabel (GmmlLabel label) {		
		Font font = new Font(label.font, Font.PLAIN, (int) (label.fontSize*(15/zf)));
		if (label.fontWeight.equalsIgnoreCase("bold")) {
			if (label.fontStyle.equalsIgnoreCase("italic")) {
				font = font.deriveFont(Font.BOLD+Font.ITALIC);
			} else {
				font = font.deriveFont(Font.BOLD);
			}
		} else if (label.fontStyle.equalsIgnoreCase("italic")) {
			font = font.deriveFont(Font.ITALIC);
		} 
		
		big.setFont(font); 
		
		FontMetrics fm = big.getFontMetrics();
		int lfHeight = fm.getHeight();
		int textWidth = fm.stringWidth(label.text);
		
		//Make sure the text is written on a white background
		Rectangle labelback = new Rectangle((int)(label.x/zf) - 2, (int)(label.y/zf)+3, textWidth + 4, lfHeight); //The +3 in the y coord is for the letter as g, j, q and y who are partially written under the normal baseline, the x and with are for a tiny bit of extra whitespace for convience.
		big.setColor(Color.white);
		big.fill(labelback);
		
		big.setColor(label.color);
		big.drawString(label.text,(int)(label.x/zf), (int)(label.y/zf+lfHeight));
	}
	
	//Draw an arc
	public void drawArc (GmmlArc arc) {
		big.setColor(arc.color);
		big.setStroke(new BasicStroke(2.0f));
	
		Arc2D.Double temparc = new Arc2D.Double((arc.x - arc.width)/zf, (arc.y - arc.height)/zf, 2*arc.width/zf, 2*arc.height/zf, 180-arc.rotation, 180, 0);
		big.draw(temparc);
	}
	
	//Draw a brace
	public void drawBrace (GmmlBrace brace) {
		double cX  = brace.cX/zf;
		double cY  = brace.cY/zf;
		double w   = brace.w/zf;
		double ppo = brace.ppo/zf;
		int or = brace.or;
		
		Arc2D.Double[] arcsOfBrace = new Arc2D.Double[4]; //4 Arcs are used to create a brace
		Line2D.Double[] linesOfBrace = new Line2D.Double[2];; //2 Lines are used to creata a brace
		Line2D.Double[] lines = new Line2D.Double[2];
		
		linesOfBrace[0] = new Line2D.Double();
		linesOfBrace[1] = new Line2D.Double();
	
		for (int i=0; i<4; i++){
			arcsOfBrace[i] = new Arc2D.Double();
		}
		
		if (or==0) { //Orientation is top
			linesOfBrace[0].setLine(cX+0.5*ppo,cY,cX+0.5*w-0.5*ppo,cY); //line on the right
			linesOfBrace[1].setLine(cX-0.5*ppo,cY,cX-0.5*w+0.5*ppo,cY); //line on the left
			
			arcsOfBrace[0].setArc(cX-(0.5*w),cY,ppo,ppo,-180,-90,0); //arc on the left
			arcsOfBrace[1].setArc(cX-ppo,cY-ppo,ppo,ppo,-90,90,0); //left arc in the middle
			arcsOfBrace[2].setArc(cX,cY-ppo,ppo,ppo,-90,-90,0); //right arc in the middle
			arcsOfBrace[3].setArc(cX+(0.5*w)-ppo,cY,ppo,ppo,0,90,0); //arc on the right
		} // end of orientation is top
		
		else if (or==1) { //Orientation is right
			linesOfBrace[0].setLine(cX,cY+0.5*ppo,cX,cY+0.5*w-0.5*ppo); //line on the bottom
			linesOfBrace[1].setLine(cX,cY-0.5*ppo,cX,cY-0.5*w+0.5*ppo); //line on the top
			
			arcsOfBrace[0].setArc(cX-ppo,cY-(0.5*w),ppo,ppo,0,90,0); //arc on the top
			arcsOfBrace[1].setArc(cX,cY-ppo,ppo,ppo,-90,-90,0); //upper arc in the middle
			arcsOfBrace[2].setArc(cX,cY,ppo,ppo,90,90,0); //lower arc in the middle
			arcsOfBrace[3].setArc(cX-ppo,cY+(0.5*w)-ppo,ppo,ppo,0,-90,0); //arc on the bottom

		} // end of orientation is right
		
		else if (or==2) { //Orientation is bottom
			linesOfBrace[0].setLine(cX+0.5*ppo,cY,cX+0.5*w-0.5*ppo,cY); //line on the right
			linesOfBrace[1].setLine(cX-0.5*ppo,cY,cX-0.5*w+0.5*ppo,cY); //line on the left
			
			arcsOfBrace[0].setArc(cX-(0.5*w),cY-ppo,ppo,ppo,-180,90,0); //arc on the left
			arcsOfBrace[1].setArc(cX-ppo,cY,ppo,ppo,90,-90,0); //left arc in the middle
			arcsOfBrace[2].setArc(cX,cY,ppo,ppo,90,90,0); //right arc in the middle
			arcsOfBrace[3].setArc(cX+(0.5*w)-ppo,cY-ppo,ppo,ppo,0,-90,0); //arc on the right

		} // end of orientation is bottom
		
		else if (or==3) { //Orientation is left
			linesOfBrace[0].setLine(cX,cY+0.5*ppo,cX,cY+0.5*w-0.5*ppo); //line on the bottom
			linesOfBrace[1].setLine(cX,cY-0.5*ppo,cX,cY-0.5*w+0.5*ppo); //line on the top
			
			arcsOfBrace[0].setArc(cX,cY-(0.5*w),ppo,ppo,-180,-90,0); //arc on the top
			arcsOfBrace[1].setArc(cX-ppo,cY-ppo,ppo,ppo,-90,90,0); //upper arc in the middle
			arcsOfBrace[2].setArc(cX-ppo,cY,ppo,ppo,90,-90,0); //lower arc in the middle
			arcsOfBrace[3].setArc(cX,cY+(0.5*w)-ppo,ppo,ppo,-90,-90,0); //arc on the bottom

		} // end of orientation is left
		
		big.setColor(brace.color);
		big.setStroke(new BasicStroke(2.0f));
		
		big.draw(linesOfBrace[0]);
		big.draw(linesOfBrace[1]);
		big.draw(arcsOfBrace[0]);
		big.draw(arcsOfBrace[1]);
		big.draw(arcsOfBrace[2]);
		big.draw(arcsOfBrace[3]);
		
	} //end of drawBrace

	private void drawHelpers () {
		if(nonSelected) return;
		
		switch (selectedObjectType) {
			case 1: 
				helpers = pathway.labels[selectedObjectNumber].getHelpers(zf);
				break;
			case 2:
				helpers = pathway.geneProducts[selectedObjectNumber].getHelpers(zf);
				break;
			case 3:
				helpers = pathway.lineshapes[selectedObjectNumber].getHelpers(zf);
				break;
			case 4:
				helpers = pathway.lines[selectedObjectNumber].getHelpers(zf);
				break;
			case 5:
				helpers = pathway.braces[selectedObjectNumber].getHelpers(zf);
				break;
			case 6:
				helpers = pathway.arcs[selectedObjectNumber].getHelpers(zf);
				break;
			case 7:
				helpers = pathway.shapes[selectedObjectNumber].getHelpers(zf);
				break;
		}
		for (int i=0; i < helpers.length; i++) {
			big.setColor(Color.orange);
			big.fill(helpers[i]);
			big.setColor(Color.blue);
			big.draw(helpers[i]);
		}
	}
	
	/*
    * Checks if the rectangle is contained within the applet window.  If the rectangle
    * is not contained withing the applet window, it is redrawn so that it is adjacent
    * to the edge of the window and just inside the window.
	 */
	 
	double[] checkRect(double new_x, double new_y, double new_width, double new_height){
		if (area == null) {
			double[] checkedCoord = {0,0,0,0};
			
			return checkedCoord;
		}
		if(area.contains(new_x/zf, new_y/zf, new_width/zf, new_height/zf)){
			double[] checkedCoord = {new_x, new_y, new_width, new_height};
		
			return checkedCoord;
		}		
	
		if((new_x+new_width)/zf>area.width){
			new_x = (int)(area.width*zf-new_width+1);
		}
		if(new_x < 0){  
			new_x = -1;
		}
		if((new_y+new_height)/zf>area.height){
			new_y = (int)(area.height*zf-new_height+1); 
		}
		if(new_y < 0){  
			new_y = -1;
		}
		
		double[] checkedCoord = {new_x, new_y};
		
		return checkedCoord;
	}
	
	double[] checkPoint(double new_x, double new_y) {
		if (area == null) {
			double[] checkedCoord = {0,0};
			
			return checkedCoord;
		}
		
		if(new_x > (area.width-5)){
			new_x = (int)(area.width-5);
		}
		if(new_x < 0){  
			new_x = 0;
		}
		if(new_y > (area.height-5)){
			new_y = (int)(area.height-5); 
		}
		if(new_y < 0){  
			new_y = 0;
		}
		
		double[] checkedPoint = {new_x, new_y};
		
		return checkedPoint;
	}
	
	public void setZoom(int z) {
		zf = (int) (15.0/(z/100.0));
		setPreferredSize(new Dimension((int)(pathway.size[0]/zf), (int)(pathway.size[1]/zf)));
		repaint();
	}
		
} //end of DrawingCanvas
