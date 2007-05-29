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
package org.pathvisio.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
   A PathwayExporter for exporting pathways to Scalable Vector Graphics (SVG) format
*/
public class SvgFormat implements PathwayExporter
{
	static final Namespace nsSVG = Namespace.getNamespace("http://www.w3.org/2000/svg");
		
	static Element defs;
	static Set<String> markers;
	
	static Document createJdom (Pathway data) throws ConverterException
	{
		Document doc = new Document();		
		
		defs = new Element("defs", nsSVG);
		markers = new HashSet<String>();
		
		Element root = new Element("svg");
		root.setNamespace(nsSVG);
		doc.setRootElement(root);
		DocType dt = new DocType("svg", "-//W3C//DTD SVG 1.1//EN", "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd");
    	doc.setDocType(dt);
    	
		root.addContent(defs);
		List<PathwayElement> objects = data.getDataObjects();
		Collections.sort(objects, new SvgComparator());
		for (PathwayElement o : data.getDataObjects())
		{
				addElement(root, o);
		}		
		return doc;
	}

	private static class SvgComparator implements Comparator {		
		List<Integer> order = Arrays.asList(
			ObjectType.INFOBOX,
			ObjectType.LEGEND,
			ObjectType.DATANODE,
			ObjectType.LABEL,
			ObjectType.SHAPE,
			ObjectType.LINE
		);
		List<ShapeType> shapeOrder = Arrays.asList(
			ShapeType.BRACE //Everything not specified will be on top
		);
		public int compare(Object o1, Object o2) {
			PathwayElement d1 = (PathwayElement)o1;
			PathwayElement d2 = (PathwayElement)o2;
			int ot1 = d1.getObjectType();
			int ot2 = d2.getObjectType();
			if(ot1 == ObjectType.SHAPE && ot2 == ObjectType.SHAPE) {
				return shapeOrder.indexOf(d1.getShapeType()) - shapeOrder.indexOf(d2.getShapeType());
			}
			int i1 = order.indexOf(ot1);
			int i2 = order.indexOf(ot2);
			return i2- i1;
		}
	}
	
	static public void addElement (Element root, PathwayElement o) throws ConverterException 
	{		
		switch (o.getObjectType())
		{
			case ObjectType.SHAPE:
				mapShape(root, o);
				break;
			case ObjectType.DATANODE:
				mapDataNode(root, o);
				break;
			case ObjectType.LINE:
				mapLine(root, o);
				break;
			case ObjectType.LABEL:
				mapLabel(root, o);
				break;
			case ObjectType.MAPPINFO:
				mapInfo(root, o);
				break;
		}
	}
	
	static void mapInfo(Element root, PathwayElement o) {
		root.setAttribute("width", "" + toPixel(o.getMBoardWidth()));
		root.setAttribute("height", "" + toPixel(o.getMBoardHeight()));
		String[][] text = new String[][] {
				{"Name: ", o.getMapInfoName()},
				{"Maintained by: ", o.getMaintainer()},
				{"Email: ", o.getEmail()},
				{"Availability: ", o.getCopyright()},
				{"Last modified: ", o.getLastModified()},
				{"Organism: ", o.getOrganism()},
				{"Data Source: ", o.getDataSource()}};
		
		double fsize = toPixel(o.getMFontSize()) + 2;//TODO: find out why smaller in SVG
		Element e = new Element("text", nsSVG);
		e.setAttribute("x", "" + toPixel(o.getMLeft()));
		e.setAttribute("y", "" + toPixel(o.getMTop()));
		e.setAttribute("font-size", "" + fsize);
		e.setAttribute("font-family", "times new roman");
		for(int i = 0; i < text.length; i++) {
			if(text[i][1] == null || text[i][1].equals("")) continue;
			
			Element l = new Element("tspan", nsSVG);
			l.setAttribute("x", "" + toPixel(o.getMLeft()));
			l.setAttribute("dy", fsize + "pt");
			l.setAttribute("font-weight", "bold");
			l.addContent(text[i][0]);
			Element v = new Element("tspan", nsSVG);
			v.addContent(text[i][1]);
			e.addContent(l);
			e.addContent(v);
		}
		root.addContent(e);
	}
	
	static void mapLine(Element parent, PathwayElement o) {
		Element e = new Element("line", nsSVG);
		e.setAttribute("x1", "" + toPixel(o.getMStartX()));
		e.setAttribute("y1", "" + toPixel(o.getMStartY()));
		e.setAttribute("x2", "" + toPixel(o.getMEndX()));
		e.setAttribute("y2", "" + toPixel(o.getMEndY()));
		e.setAttribute("stroke", rgb2String(o.getColor()));
		if(o.getLineStyle() == LineStyle.DASHED) {
			e.setAttribute("stroke-dasharray", "5,2");
		}
		
		LineType type = o.getLineType();
		String id = getColordMarker(type, o.getColor(), markers, defs);
		if(type != LineType.LINE) {
			e.setAttribute("marker-end", "url(#" + id + ")");
		}
		parent.addContent(e);
	}
	
	static void mapDataNode(Element parent, PathwayElement o) {
		Element e = new Element("rect", nsSVG);
		e.setAttribute("x", "" + toPixel(o.getMLeft()));
		e.setAttribute("y", "" + toPixel(o.getMTop()));
		e.setAttribute("width", "" + toPixel(o.getMWidth()));
		e.setAttribute("height", "" + toPixel(o.getMHeight()));
		mapColor(e, o);
		parent.addContent(e);
		e = createTextElement(o);
		e.addContent(o.getTextLabel());
		parent.addContent(e);
	}
	
	static void mapLabel(Element parent, PathwayElement o) {
		Element e = createTextElement(o);
		e.addContent(o.getTextLabel());
		parent.addContent(e);
	}
	
	static void mapShape(Element parent, PathwayElement o) {
		double cx = toPixel(o.getMCenterX());
		double cy = toPixel(o.getMCenterY());
		double w = toPixel(o.getMWidth());
		double h = toPixel(o.getMHeight());
		
		double r = o.getRotation() * 180.0/Math.PI;
		
		Element tr = new Element("g", nsSVG);		
		tr.setAttribute("transform", "translate(" + cx + ", " + cy + ")");
		Element rot = new Element("g", nsSVG);
		rot.setAttribute("transform", "rotate(" + r + ")");
		Element e = null;	
		
		switch (o.getShapeType())
		{
			case OVAL:
				e = new Element("ellipse", nsSVG);
				e.setAttribute("cx", "0");
				e.setAttribute("cy", "0");
				e.setAttribute("rx", "" + toPixel(o.getMWidth()/2));
				e.setAttribute("ry", "" + toPixel(o.getMHeight()/2));
				break;
			case ARC:
				e = new Element("path", nsSVG);
				e.setAttribute("d", "M " + -w/2 + " 0 " + " a " + w/2 + " " + h/2 + " 0 0 0 " + w + " 0");
				break;
			case BRACE:
				e = new Element("path", nsSVG);
				e.setAttribute(
						"d", "M " + -w/2 + " " + h/2 + " q 0 " + -h/2 + " " + h/2 + " " + -h/2 + " " +
						"L " + -h/2 + " 0 " +
						"Q 0 0 0 " + -h/2 + " " +
						"Q 0 0 " + h/2 + " 0 " + 
						"L " + (w/2 - h/2) + " 0 " +
						"q " + h/2 + " 0 " + h/2 + " " + h/2
				);
				break;
			default:
				e = new Element("rect", nsSVG);
				e.setAttribute("x", "" + -w/2);
				e.setAttribute("y", "" + -h/2);
				e.setAttribute("width", "" + toPixel(o.getMWidth()));
				e.setAttribute("height", "" + toPixel(o.getMHeight()));
				break;
		}
		mapColor(e, o);
		rot.addContent(e);
		tr.addContent(rot);
		parent.addContent(tr);
	}
	
	static void mapColor(Element e, PathwayElement o) {
		e.setAttribute("stroke", rgb2String(o.getColor()));
		if(o.isTransparent() && o.getObjectType() != ObjectType.DATANODE) {
			e.setAttribute("fill", "none");
		} else {
			e.setAttribute("fill", rgb2String(o.getFillColor()));
			//Override for some shape types (TODO: make handling of colors consistent!)
			if(o.getObjectType() == ObjectType.DATANODE) {
				e.setAttribute("fill", "white");
			} else if(o.getObjectType() == ObjectType.SHAPE){
				//Fill/transparency in braces/arcs is not stored properly
				if(o.getShapeType() == ShapeType.ARC || o.getShapeType() == ShapeType.BRACE)
					e.setAttribute("fill", "none");
			}
		}
	}
	
	static String rgb2String(Color rgb) {
		return "rgb(" + rgb.red + "," + rgb.green + "," + rgb.blue + ")";
	}
	
	static int toPixel(double coordinate) {
		return (int)(coordinate * 1/15);
	}
	
	static Element createTextElement(PathwayElement o) {
		Element e = new Element("text", nsSVG);
		e.setAttribute("x", "" + toPixel(o.getMCenterX()));
		e.setAttribute("y", "" + (toPixel(o.getMCenterY()) + toPixel(o.getMFontSize())));
		e.setAttribute("font-family", o.getFontName()); 
		e.setAttribute("font-size",toPixel(o.getMFontSize()) + "pt");
		e.setAttribute("text-anchor", "middle");
		//e.setAttribute("alignment-baseline", "middle"); //Not supported by firefox
		e.setAttribute("dy", "-" + toPixel((1.0/3) * o.getMFontSize()) + "pt"); //Instead of alignment-baseline
		if(o.isBold()) e.setAttribute("font-weight", "bold");
		if(o.isItalic()) e.setAttribute("font-style", "italic");
		if(o.isStrikethru()) e.setAttribute("text-decoration", "line-through");
		if(o.isUnderline()) e.setAttribute("text-decoration", "underline");
		e.setAttribute("fill", rgb2String(o.getColor()));
		return e;
	}
	
	static String getColordMarker(LineType type, Color color, Set markers, Element defs) {
		Element marker = null;
		String id = type.getGpmlName() + color.toString().hashCode();
		
		if(markers.contains(id)) return id;
		
		String c = rgb2String(color);
		switch(type) {
		case ARROW:
			marker = new Element("marker", nsSVG);
			marker.setAttribute("id", id);
			marker.setAttribute("viewBox", "0 0 10 10");
			marker.setAttribute("orient", "auto");
			marker.setAttribute("refX", "10");
			marker.setAttribute("refY", "5");
			marker.setAttribute("markerWidth", "10");
			marker.setAttribute("markerHeight", "10");
			Element e = new Element("path", nsSVG);
			e.setAttribute("d", "M 0 0 L 10 5 L 0 10 z");
			e.setAttribute("stroke", c);
			e.setAttribute("fill", c);
			marker.addContent(e);
			break;
		case TBAR:
			marker = new Element("marker", nsSVG);
			marker.setAttribute("id", id);
			marker.setAttribute("viewBox", "0 0 1 15");
			marker.setAttribute("orient", "auto");
			marker.setAttribute("refX", "1");
			marker.setAttribute("refY", "8");
			marker.setAttribute("markerWidth", "2");
			marker.setAttribute("markerHeight", "20");
			e = new Element("rect", nsSVG);
			e.setAttribute("x", "1");
			e.setAttribute("y", "1");
			e.setAttribute("width", "1");
			e.setAttribute("height", "15");
			e.setAttribute("stroke", c);
			e.setAttribute("fill", c);
			marker.addContent(e);
			break;
		case LIGAND_ROUND:
			marker = new Element("marker", nsSVG);
			marker.setAttribute("id", id);
			marker.setAttribute("viewBox", "0 0 10 10");
			marker.setAttribute("orient", "auto");
			marker.setAttribute("refX", "10");
			marker.setAttribute("refY", "5");
			marker.setAttribute("markerWidth", "10");
			marker.setAttribute("markerHeight", "10");
			e = new Element("ellipse", nsSVG);
			e.setAttribute("cx", "5");
			e.setAttribute("cy", "5");
			e.setAttribute("rx", "5");
			e.setAttribute("ry", "5");
			e.setAttribute("stroke", c);
			e.setAttribute("fill", c);
			marker.addContent(e);
			break;
		case RECEPTOR_ROUND:
			marker = new Element("marker", nsSVG);
			marker.setAttribute("id", id);
			marker.setAttribute("viewBox", "0 0 10 10");
			marker.setAttribute("orient", "auto");
			marker.setAttribute("refX", "5");
			marker.setAttribute("refY", "5");
			marker.setAttribute("markerWidth", "15");
			marker.setAttribute("markerHeight", "15");
			e = new Element("path", nsSVG);
			e.setAttribute("d", "M 10 0 A 5 5 0 0 0 10 10");
			e.setAttribute("stroke", c);
			e.setAttribute("fill", "none");
			marker.addContent(e);
			break;
		case RECEPTOR_SQUARE:
			marker = new Element("marker", nsSVG);
			marker.setAttribute("id", id);
			marker.setAttribute("viewBox", "0 0 10 15");
			marker.setAttribute("orient", "auto");
			marker.setAttribute("refX", "1");
			marker.setAttribute("refY", "7.5");
			marker.setAttribute("markerWidth", "15");
			marker.setAttribute("markerHeight", "15");
			e = new Element("path", nsSVG);
			e.setAttribute("d", "M 10 0 L 0 0  L 0 15 L 10 15");
			e.setAttribute("stroke", c);
			e.setAttribute("fill", "none");
			marker.addContent(e);
			break;
		case LIGAND_SQUARE:
			marker = new Element("marker", nsSVG);
			marker.setAttribute("id", id);
			marker.setAttribute("viewBox", "0 0 10 15");
			marker.setAttribute("orient", "auto");
			marker.setAttribute("refX", "10");
			marker.setAttribute("refY", "7.5");
			marker.setAttribute("markerWidth", "10");
			marker.setAttribute("markerHeight", "10");
			e = new Element("rect", nsSVG);
			e.setAttribute("x", "1");
			e.setAttribute("y", "1");
			e.setAttribute("width", "10");
			e.setAttribute("height", "15");
			e.setAttribute("stroke", c);
			e.setAttribute("fill", c);
			marker.addContent(e);
			break;
		}
		
		if(marker != null) {
			defs.addContent(marker);
			markers.add(id);
			return id;
		} else {
			return null;
		}
	}
	
	private String[] extensions = new String[] { "svg" };

	public String getName() {
		return "Scalable Vector Graphics (SVG)";
	}
	
	public String[] getExtensions() {
		return extensions;
	}
	
	public void doExport(File file, Pathway pathway) throws ConverterException {
		Document doc = SvgFormat.createJdom(pathway);
		
		XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
		Format f = xmlcode.getFormat();
		f.setEncoding("ISO-8859-1");
		f.setTextMode(Format.TextMode.PRESERVE);
		xmlcode.setFormat(f);
		
		//Open a filewriter
		try
		{
			FileWriter writer = new FileWriter(file);
			//Send XML code to the filewriter
			xmlcode.output(doc, writer);
		}
		catch (IOException ie)
		{
			throw new ConverterException(ie);
		}
	}
}
