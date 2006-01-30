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

import org.jdom.JDOMException;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.output.XMLOutputter;
import org.jdom.*;
import org.jdom.Element;
import org.jdom.Attribute;
import java.io.IOException;
import java.util.*;
import java.io.FileWriter;
import java.io.File;

/**
 * The GmmlWriter class build a jdom document from a given pathway. It also include methods to dump a document to an output.
 */
 
public class GmmlWriter {

	GmmlPathway pathway;
	Document doc;
	Element root;
	
	/**
	  * The constructor will load the pathway and automaticly build a document of it.
	  */
	public GmmlWriter(GmmlPathway pathway) {
		this.pathway = pathway;
		buildDoc();
	}
	
	/**
	  * This method will dump the document to the console as valid xml using the jdom package
	  */
	public void dumpToScreen() {
		try {
			//Get the XML code
			XMLOutputter screendump = new XMLOutputter(Format.getPrettyFormat());
			//Dump the XML code to the screen
			screendump.output(doc, System.out);
		}
		catch (IOException e) {
		System.err.println(e);
		}
	}
   
	/**
	 * This method will write the document to the given file using the jdom package.
	 */
     
	public void writeToFile(String filename) {
		try 
		{
			//Get the XML code
			XMLOutputter screendump = new XMLOutputter(Format.getPrettyFormat());
			//Get the filename
			File file = new File(filename);
			//Open a filewriter
			FileWriter writer = new FileWriter(file);
			//Send XML code to the filewriter
	     	screendump.output(doc, writer);
		}
		catch (IOException e) 
		{
			System.err.println(e);
		}
	} 

	/**
	 * This method builds a document from the pathay, used only internally. Maybe make this private? Or make a public overloaded version that requires a pathway input
	 */
	public void buildDoc() {
		System.out.println("Building document...");
		root = new Element("Pathway"); //Create the root element, this is for GMML Always the pathway
	   //Add all the attributes to the pathway
	   for (int i = 0; i < pathway.attributes.length; i++) {
	   	root.setAttribute(pathway.attributes[i][0], pathway.attributes[i][1]);
	   }
	   //Create the graphics element
	   Element graphics = new Element("Graphics");
	   //Set the attributes for the graphics element
	   graphics.setAttribute("BoardHeight",Integer.toString(pathway.size[1]));
	   graphics.setAttribute("BoardWidth",Integer.toString(pathway.size[0]));
	   //Add the graphics element to the root element
	   root.addContent(graphics);
	   //Call the method that adds all other elements to the root element
	   fillRootElement();
	   //Create the document out of the root element
	   doc = new Document(root);
	}
	
	private void fillRootElement () 
	{
	//This calls the functions for adding all kinds of elements supported by the writer
		addGeneProducts();
		addLines();
		addLineShapes();
		addArcs();
		addLabels();
		addShapes();
		addBraces();
	}
	
	private void addGeneProducts() {
		for (int i = 0; i < pathway.geneProducts.length; i++) {
			//Create a new geneproduct element
			Element geneproduct = new Element("GeneProduct");
			
			//Set the geneproduct attributes
			geneproduct.setAttribute("GeneID",pathway.geneProducts[i].geneID);
			
			//Create a new graphics element
			Element graphics = new Element("Graphics");
			
			double w, h, x, y, cx, cy;
			
			w = pathway.geneProducts[i].width;
			h = pathway.geneProducts[i].height;
			x = pathway.geneProducts[i].x;
			y = pathway.geneProducts[i].y;
			//Calculate the centers from the x, y, width, height
			cx = x + w/2;
			cy = y + h/2; 
	   
			//Add the attributes to the graphics element
			graphics.setAttribute("CenterX",Integer.toString((int)cx));
			graphics.setAttribute("CenterY",Integer.toString((int)cy));
			graphics.setAttribute("Width",Integer.toString((int)w));
			graphics.setAttribute("Height",Integer.toString((int)h));
			
			//Add the graphics element to the geneproduct
			geneproduct.addContent(graphics);
			//Add the geneproduct to the root element (the pathway)
			root.addContent(geneproduct);
		}
	}
	
	private void addLines() {
		for (int i = 0; i < pathway.lines.length; i++) {
			Element line = new Element("Line");

			String type, style;

			type = "Line"; //Default is line
			
			if (pathway.lines[i].type==1) {
				type = "Arrow";
			}
			
			style = "Solid"; //Default is solid
			
			if (pathway.lines[i].style==1) {
				style = "Broken";
			}	
			
			line.setAttribute("Type",type);
			line.setAttribute("Style",style);
			
			Element graphics = new Element("Graphics"); 
			
			graphics.setAttribute("StartX",Integer.toString((int)pathway.lines[i].startx));
			graphics.setAttribute("StartY",Integer.toString((int)pathway.lines[i].starty));
			graphics.setAttribute("EndX",Integer.toString((int)pathway.lines[i].endx));
			graphics.setAttribute("EndY",Integer.toString((int)pathway.lines[i].endy));
			graphics.setAttribute("Color",GmmlColor.convertColorToString(pathway.lines[i].color));
			
			line.addContent(graphics);
			root.addContent(line);
		}
	}
	
	private void addLineShapes() {
		for (int i = 0; i < pathway.lineshapes.length; i++) {
			Element lineshape = new Element("LineShape");

			String type, style;

			type = "TBar"; //Default is tbar
			
			if (pathway.lineshapes[i].type==1) {
				type = "ReceptorRound";
			}
			if (pathway.lineshapes[i].type==2) {
				type = "LigandRound";
			}	
			if (pathway.lineshapes[i].type==3) {
				type = "ReceptorSquare";
			}
			if (pathway.lineshapes[i].type==4) {
				type = "LigandSquare";
			}
			
			lineshape.setAttribute("Type",type);
			Element graphics = new Element("Graphics"); 

			graphics.setAttribute("StartX",Integer.toString((int)pathway.lineshapes[i].startx));
			graphics.setAttribute("StartY",Integer.toString((int)pathway.lineshapes[i].starty));
			graphics.setAttribute("EndX",Integer.toString((int)pathway.lineshapes[i].endx));
			graphics.setAttribute("EndY",Integer.toString((int)pathway.lineshapes[i].endy));
			graphics.setAttribute("Color",GmmlColor.convertColorToString(pathway.lineshapes[i].color));
			
			lineshape.addContent(graphics);
			root.addContent(lineshape);
		}
	}

	private void addArcs() {
		for (int i = 0; i<pathway.arcs.length; i++) {
			Element arc = new Element("Arc");
			
			Element graphics = new Element("Graphics"); 
			
			graphics.setAttribute("StartX",Integer.toString((int)pathway.arcs[i].x));
			graphics.setAttribute("StartY",Integer.toString((int)pathway.arcs[i].y));
			graphics.setAttribute("Width",Integer.toString((int)pathway.arcs[i].width));
			graphics.setAttribute("Height",Integer.toString((int)pathway.arcs[i].height));
			graphics.setAttribute("Color",GmmlColor.convertColorToString(pathway.arcs[i].color));
			graphics.setAttribute("Rotation",Double.toString(pathway.arcs[i].rotation));
			
			arc.addContent(graphics);
			root.addContent(arc);
		}
	}
	
	private void addLabels() {
		for (int i = 0; i < pathway.labels.length; i++) {
			Element label = new Element("Label");
		
			label.setAttribute("TextLabel",pathway.labels[i].text);
		
			Element graphics = new Element("Graphics");
		
			double w, h, x, y, cx, cy;
			
			w = pathway.labels[i].width;
			h = pathway.labels[i].height;
			x = pathway.labels[i].x;
			y = pathway.labels[i].y;
			cx = x + w/2;
			cy = y + h/2;
			
			String fontWeight = pathway.labels[i].fontWeight;
			
			if (fontWeight=="") {
				fontWeight = "Regular";
			}
			
			String fontStyle = pathway.labels[i].fontStyle;
			
			if (fontStyle=="") {
				fontStyle = "Normal";
			}
			
			graphics.setAttribute("CenterX",Integer.toString((int)cx));
			graphics.setAttribute("CenterY",Integer.toString((int)cy));
			graphics.setAttribute("Width",Integer.toString((int)w));
			graphics.setAttribute("Height",Integer.toString((int)h));
			graphics.setAttribute("Color",GmmlColor.convertColorToString(pathway.labels[i].color));
			graphics.setAttribute("FontName",pathway.labels[i].font);
			graphics.setAttribute("FontSize",Integer.toString(pathway.labels[i].fontSize));
			graphics.setAttribute("FontWeight",fontWeight);
			graphics.setAttribute("FontStyle",fontStyle);
			
			label.addContent(graphics);
			root.addContent(label);
		}

	}
	
	private void addShapes()
	{
		for (int i = 0; i < pathway.shapes.length; i++)
		{
			Element shape = new Element("Shape");
			
			String type = "Rectangle"; //Default is rectangle

			if (pathway.shapes[i].type==1) {
			type = "Oval";
			}	

			shape.setAttribute("Type",type);
		
			Element graphics = new Element("Graphics");
		
			double w, h, x, y, cx, cy;
			
			w = pathway.shapes[i].width;
			h = pathway.shapes[i].height;
			x = pathway.shapes[i].x;
			y = pathway.shapes[i].y;
			cx = x + w;
			cy = y + h;
			
			graphics.setAttribute("CenterX",Integer.toString((int)cx));
			graphics.setAttribute("CenterY",Integer.toString((int)cy));
			graphics.setAttribute("Width",Integer.toString((int)w));
			graphics.setAttribute("Height",Integer.toString((int)h));
			graphics.setAttribute("Color",GmmlColor.convertColorToString(pathway.shapes[i].color));
			graphics.setAttribute("Rotation",Integer.toString((int)pathway.shapes[i].rotation));
			
			shape.addContent(graphics);
			root.addContent(shape);
		}

	}
	
	private void addBraces()
	{
		for (int i = 0; i<pathway.braces.length; i++)
		{
			Element brace = new Element("Brace");
			
			Element graphics = new Element("Graphics"); 

			String orientation = "top";
			if(pathway.braces[i].or==1) {
				orientation = "right";
			} else if(pathway.braces[i].or==2) {
				orientation = "bottom";
			}
			else if(pathway.braces[i].or==3) {
				orientation = "left";
			}

			graphics.setAttribute("CenterX",Integer.toString((int)pathway.braces[i].cX));
			graphics.setAttribute("CenterY",Integer.toString((int)pathway.braces[i].cY));
			graphics.setAttribute("Width",Integer.toString((int)pathway.braces[i].w));
			graphics.setAttribute("PicPointOffset",Integer.toString((int)pathway.braces[i].ppo));
			graphics.setAttribute("Color",GmmlColor.convertColorToString(pathway.braces[i].color));
			graphics.setAttribute("Orientation", orientation);

			brace.addContent(graphics);
			root.addContent(brace);
		}
	}
}