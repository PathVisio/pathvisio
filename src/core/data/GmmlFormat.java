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
package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import util.ColorConverter;

/**
 * class responsible for interaction with Gpml format.
 * Contains all gpml-specific constants,
 * and should be the only class that needs to import jdom
 *  
 * @author Martijn
 *
 */
public class GmmlFormat {

	/**
	 * The GPML xsd implies a certain ordering for children of the pathway element.
	 * (e.g. GeneProduct always comes before LineShape, etc.)
	 * 
	 * This Comparator can sort jdom Elements so that they are in the correct order
	 * for the xsd.
	 *  
	 * @author Martijn.vanIersel
	 */
	private static class ByElementName implements Comparator<Element>
	{
		// hashmap for quick lookups during sorting
		private HashMap<String, Integer> elementOrdering;
				
		// correctly ordered list of tag names, which are loaded into the hashmap in
		// the constructor.
		private final String[] elements = new String[] {
			"Notes", "Comment", "Graphics", "GeneProduct", "Line", "Label",
			"Shape", "Brace", "FixedShape", "ComplexShape", "InfoBox", "Legend"
		};
		
		/*
		 * Constructor
		 */
		public ByElementName()
		{
			elementOrdering = new HashMap<String, Integer>();
			for (int i = 0; i < elements.length; ++i)
			{
				elementOrdering.put (elements[i], new Integer(i));
			}			
		}
		
		/*
		 * As a comparison measure, returns difference of index of element names of a and b 
		 * in elements array. E.g:
		 * Comment -> index 1 in elements array
		 * Graphics -> index 2 in elements array.
		 * If a.getName() is Comment and b.getName() is Graphics, returns 1-2 -> -1
		 */
		public int compare(Element a, Element b) {
			return ((Integer)elementOrdering.get(a.getName())).intValue() - 
				((Integer)elementOrdering.get(b.getName())).intValue();
		}
		
	}
	
	public static Document createJdom(GmmlData data) throws ConverterException
	{
		Document doc = new Document();
		
		Element root = new Element("Pathway");
		doc.setRootElement(root);

		List<Element> elementList = new ArrayList<Element>();
    	
		for (GmmlDataObject o : data.getDataObjects())
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
			{
				root.setAttribute("Name", o.getMapInfoName());
				root.setAttribute("Data-Source", "GenMAPP 2.0");
				root.setAttribute("Version", o.getVersion());
				root.setAttribute("Author", o.getAuthor());
				root.setAttribute("Maintained-By", o.getMaintainedBy());
				root.setAttribute("Email", o.getEmail());
				root.setAttribute("Availability", o.getAvailability());
				root.setAttribute("Last-Modified", o.getLastModified());

				Element notes = new Element("Notes");
				notes.addContent(o.getNotes());
				root.addContent(notes);

				Element comments = new Element("Comment");
				comments.addContent(o.getComment());
				root.addContent(comments);
				
				Element graphics = new Element("Graphics");
				root.addContent(graphics);
				
				graphics.setAttribute("BoardWidth", "" + o.getBoardWidth()* GmmlData.GMMLZOOM);
				graphics.setAttribute("BoardHeight", "" + o.getBoardHeight()* GmmlData.GMMLZOOM);
				graphics.setAttribute("WindowWidth", "" + o.getWindowWidth()* GmmlData.GMMLZOOM);
				graphics.setAttribute("WindowHeight", "" + o.getWindowHeight()* GmmlData.GMMLZOOM);
				
			}
			else
			{
				Element e = createJdomElement(o);
				if (e != null)
					elementList.add(e);
			}
		}
		
    	// now sort the generated elements in the order defined by the xsd
		Collections.sort(elementList, new ByElementName());
		for (Element e : elementList)
		{			
			root.addContent(e);
		}
		
		return doc;
	}
	
	public static void mapElement(Element e, GmmlData p) throws ConverterException
	{
		String tag = e.getName();
		int ot = ObjectType.getTagMapping(tag);
		if (ot == -1)
		{
			// do nothing. This could be caused by
			// tags <notes> or <comment> that appear
			// as subtags of <pathway>
			return;
		}
		
		GmmlDataObject o;
		if (ot == ObjectType.MAPPINFO)
		{
			o = p.getMappInfo();
		}
		else if (ot == ObjectType.INFOBOX)
		{
			o = p.getInfoBox();
		}
		else
		{
			o = new GmmlDataObject(ot);
			p.add (o);
		}
		
		switch (o.getObjectType())
		{
			case ObjectType.BRACE: // brace
				mapNotesAndComment(o, e);
				mapColor(o, e);
				mapBraceData(o, e);
				mapGraphId(o, e);
				break;
			case ObjectType.GENEPRODUCT:
				mapShapeData(o, e);
				mapColor(o, e);
				mapNotesAndComment(o, e);
				mapGeneProductData(o, e);
				mapGraphId(o, e);
				break;
			case ObjectType.LABEL:
				mapShapeData(o, e);
				mapColor(o, e);
				mapLabelData(o, e);
				mapNotesAndComment(o, e);
				mapGraphId(o, e);
				break;
			case ObjectType.LINE:
				mapLineData(o, e);
				mapColor(o, e);
				mapNotesAndComment(o, e);
				break;
			case ObjectType.MAPPINFO:
				mapMappInfoData(o, e);
				break;
			case ObjectType.SHAPE:
				mapShapeData(o, e);
				mapShapeColor (o, e);
				mapNotesAndComment(o, e);
				mapShapeType(o, e);
				mapRotation(o, e);
				mapGraphId(o, e);
				break;
			case ObjectType.FIXEDSHAPE:
				mapCenter(o, e);
				mapNotesAndComment(o, e);
				mapShapeType(o, e);
				mapGraphId(o, e);
				break;
			case ObjectType.COMPLEXSHAPE:
				mapCenter(o, e);
				mapWidth(o, e);
				mapNotesAndComment(o, e);
				mapShapeType(o, e);
				mapRotation(o, e);
				mapGraphId(o, e);
				break;
			case ObjectType.LEGEND:
				mapSimpleCenter(o, e);
				break;
			case ObjectType.INFOBOX:
				mapSimpleCenter (o, e);
				break;
			default:
				throw new ConverterException("Invalid ObjectType'" + tag + "'");
		}
	}
	
	public static final List<String> gmmlLineTypes = Arrays.asList(new String[] {
			"Line", "Arrow", "TBar", "Receptor", "LigandSquare", 
			"ReceptorSquare", "LigandRound", "ReceptorRound"});

	private static void mapLineData(GmmlDataObject o, Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	
    	Element p1 = (Element)graphics.getChildren().get(0);
    	Element p2 = (Element)graphics.getChildren().get(1);
    	
    	o.setStartX (Double.parseDouble(p1.getAttributeValue("x")) / GmmlData.GMMLZOOM);
    	o.setStartY (Double.parseDouble(p1.getAttributeValue("y")) / GmmlData.GMMLZOOM);
    	
    	String ref1 = p1.getAttributeValue("GraphRef");
    	if (ref1 == null) ref1 = "";
    	o.setStartGraphRef (ref1);

    	o.setEndX (Double.parseDouble(p2.getAttributeValue("x")) / GmmlData.GMMLZOOM);
    	o.setEndY (Double.parseDouble(p2.getAttributeValue("y")) / GmmlData.GMMLZOOM); 
    	
    	String ref2 = p2.getAttributeValue("GraphRef");
    	if (ref2 == null) ref2 = "";
    	o.setEndGraphRef (ref2);

    	String style = e.getAttributeValue("Style");
    	String type = e.getAttributeValue("Type");
    	
    	o.setLineStyle ((style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED);
    	o.setLineType (gmmlLineTypes.indexOf(type));
	}
	
	private static void updateLineData(GmmlDataObject o, Element e)
	{
		if(e != null) {
			e.setAttribute("Type", gmmlLineTypes.get(o.getLineType()));
			e.setAttribute("Style", o.getLineStyle() == LineStyle.SOLID ? "Solid" : "Broken");
			
			Element jdomGraphics = e.getChild("Graphics");
			Element p1 = new Element("Point");
			jdomGraphics.addContent(p1);
			p1.setAttribute("x", Double.toString(o.getStartX() * GmmlData.GMMLZOOM));
			p1.setAttribute("y", Double.toString(o.getStartY() * GmmlData.GMMLZOOM));
			if (o.getStartGraphRef() != null)
			{
				p1.setAttribute("GraphRef", o.getStartGraphRef());
			}
			Element p2 = new Element("Point");
			jdomGraphics.addContent(p2);
			p2.setAttribute("x", Double.toString(o.getEndX() * GmmlData.GMMLZOOM));
			p2.setAttribute("y", Double.toString(o.getEndY() * GmmlData.GMMLZOOM));
			if (o.getEndGraphRef() != null)
			{
				p2.setAttribute("GraphRef", o.getEndGraphRef());
			}
		}
	}
	
	private static void mapColor(GmmlDataObject o, Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	String scol = graphics.getAttributeValue("Color");
    	o.setColor (ColorConverter.gmmlString2Color(scol));
    	o.setTransparent(scol == null || scol.equals("Transparent"));
	}

	private static void mapShapeColor(GmmlDataObject o, Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	String scol = graphics.getAttributeValue("FillColor");
    	o.setFillColor (ColorConverter.gmmlString2Color(scol));
    	o.setTransparent (scol == null || scol.equals("Transparent"));
    	scol = graphics.getAttributeValue("Color");
    	o.setColor (ColorConverter.gmmlString2Color(scol));
	}

	private static void updateColor(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics != null) 
			{
				if (o.isTransparent())
					jdomGraphics.setAttribute("Color", "Transparent");
				else
					jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(o.getColor()));
			}
		}
	}
		
	private static void updateShapeColor(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics != null) 
			{
				if (o.isTransparent())
					jdomGraphics.setAttribute("FillColor", "Transparent");
				else
					jdomGraphics.setAttribute("FillColor", ColorConverter.color2HexBin(o.getFillColor()));
				jdomGraphics.setAttribute("Color", ColorConverter.color2HexBin(o.getColor()));			}
		}
	}

	private static void mapNotesAndComment(GmmlDataObject o, Element e)
	{
		String notes = e.getChildText("Notes");
    	if (notes == null) notes = "";
    	o.setNotes(notes);
    	
    	String comment = e.getChildText("Comment");
    	if (comment == null) comment = "";
    	o.setComment(comment);
	}
	
	private static void updateNotesAndComment(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element n = new Element("Notes");
			n.setText(o.getNotes());
			e.addContent(n);
			
			Element c = new Element ("Comment");
			c.setText(o.getComment());
			e.addContent(c);
		}
	}
	
	private static void mapGraphId (GmmlDataObject o, Element e)
	{
		String id = e.getAttributeValue("GraphId");
		o.setGraphId (id);
	}
	
	private static void updateGraphId (GmmlDataObject o, Element e)
	{
		String id = o.getGraphId();
		// id has to be unique!
		if (id != null && !id.equals(""))
		{
			e.setAttribute("GraphId", o.getGraphId());
		}
	}
	
	private static void mapGeneProductData(GmmlDataObject o, Element e)
	{
		o.setGeneID (e.getAttributeValue("GeneID"));
		String xref = e.getAttributeValue ("Xref");
		if (xref == null) xref = "";
		o.setXref(xref);
		o.setGeneProductType (e.getAttributeValue("Type"));
		o.setGeneProductName (e.getAttributeValue("Name"));
		o.setBackpageHead (e.getAttributeValue("BackpageHead"));
		o.setDataSource (e.getAttributeValue("GeneProduct-Data-Source"));
	}

	private static void updateGeneProductData(GmmlDataObject o, Element e)
	{
		if(e != null) {
			e.setAttribute("GeneID", o.getGeneID());
			e.setAttribute("Xref", o.getXref());
			e.setAttribute("Type", o.getGeneProductType());
			e.setAttribute("Name", o.getGeneProductName());
			e.setAttribute("BackpageHead", o.getBackpageHead());
			e.setAttribute("GeneProduct-Data-Source", o.getDataSource());
		}
	}
	 	
	// internal helper routine
	private static void mapCenter(GmmlDataObject o, Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	o.setCenterX (Double.parseDouble(graphics.getAttributeValue("CenterX")) / GmmlData.GMMLZOOM); 
    	o.setCenterY (Double.parseDouble(graphics.getAttributeValue("CenterY")) / GmmlData.GMMLZOOM);	
	}
	
	private static void updateCenter(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("CenterX", Double.toString(o.getCenterX() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Double.toString(o.getCenterY() * GmmlData.GMMLZOOM));
			}
		}		
	}

	private static void mapWidth(GmmlDataObject o, Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	o.setWidth (Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.GMMLZOOM);
	}
	
	private static void updateWidth(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("Width", Double.toString(o.getWidth() * GmmlData.GMMLZOOM));
			}
		}		
	}

	private static void mapSimpleCenter(GmmlDataObject o, Element e)
	{
		o.setCenterX (Double.parseDouble(e.getAttributeValue("CenterX")) / GmmlData.GMMLZOOM); 
		o.setCenterY (Double.parseDouble(e.getAttributeValue("CenterY")) / GmmlData.GMMLZOOM);	
	}
	
	private static void updateSimpleCenter(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			e.setAttribute("CenterX", Double.toString(o.getCenterX() * GmmlData.GMMLZOOM));
			e.setAttribute("CenterY", Double.toString(o.getCenterY() * GmmlData.GMMLZOOM));			
		}		
	}

	private static void mapShapeData(GmmlDataObject o, Element e)
	{
    	mapCenter(o, e);
		Element graphics = e.getChild("Graphics");
		o.setWidth (Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.GMMLZOOM); 
		o.setHeight (Double.parseDouble(graphics.getAttributeValue("Height")) / GmmlData.GMMLZOOM);
	}
	
	private static void updateShapeData(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				updateCenter(o, e);
				jdomGraphics.setAttribute("Width", Double.toString(o.getWidth() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Height", Double.toString(o.getHeight() * GmmlData.GMMLZOOM));
			}
		}
	}
	
	private static void mapShapeType(GmmlDataObject o, Element e)
	{
		o.setShapeType (ShapeType.fromGmmlName(e.getAttributeValue("Type")));
	}
	
	private static void updateShapeType(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			e.setAttribute("Type", ShapeType.toGmmlName(o.getShapeType()));
		}
	}
	
	private static void mapBraceData(GmmlDataObject o, Element e)
	{
    	mapCenter(o, e);
		Element graphics = e.getChild("Graphics");
		o.setWidth (Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.GMMLZOOM); 
		o.setHeight (Double.parseDouble(graphics.getAttributeValue("PicPointOffset")) / GmmlData.GMMLZOOM);
		int orientation = OrientationType.getMapping(graphics.getAttributeValue("Orientation"));
		if(orientation > -1)
			o.setOrientation(orientation);
	}
	
	private static void updateBraceData(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				updateCenter(o, e);
				jdomGraphics.setAttribute("Width", Double.toString(o.getWidth() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("PicPointOffset", Double.toString(o.getHeight() * GmmlData.GMMLZOOM));
				jdomGraphics.setAttribute("Orientation", OrientationType.getMapping(o.getOrientation()));
			}
		}
	}

	private static void mapRotation(GmmlDataObject o, Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	o.setRotation (Double.parseDouble(graphics.getAttributeValue("Rotation"))); 
	}

	private static void updateRotation(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("Rotation", Double.toString(o.getRotation()));
			}
		}	
	}
	
	private static void mapLabelData(GmmlDataObject o, Element e)
	{
		o.setLabelText (e.getAttributeValue("TextLabel"));
    	Element graphics = e.getChild("Graphics");
    	
    	o.setFontSize (Integer.parseInt(graphics.getAttributeValue("FontSize")));
    	
    	String fontWeight = graphics.getAttributeValue("FontWeight");
    	String fontStyle = graphics.getAttributeValue("FontStyle");
    	String fontDecoration = graphics.getAttributeValue ("FontDecoration");
    	String fontStrikethru = graphics.getAttributeValue ("FontStrikethru");
    	
    	o.setBold (fontWeight != null && fontWeight.equals("Bold"));   	
    	o.setItalic (fontStyle != null && fontStyle.equals("Italic"));    	
    	o.setUnderline (fontDecoration != null && fontDecoration.equals("Underline"));    	
    	o.setStrikethru (fontStrikethru != null && fontStrikethru.equals("Strikethru"));
    	
    	o.setFontName (graphics.getAttributeValue("FontName"));
    	
    	String xref = e.getAttributeValue("Xref");
    	if (xref == null) xref = "";
    	o.setXref(xref);
	}
	
	private static void updateLabelData(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			e.setAttribute("TextLabel", o.getLabelText());
			e.setAttribute("Xref", o.getXref() == null ? "" : o.getXref());
			Element graphics = e.getChild("Graphics");
			if(graphics !=null) 
			{
				graphics.setAttribute("FontName", o.getFontName() == null ? "" : o.getFontName());			
				graphics.setAttribute("FontWeight", o.isBold() ? "Bold" : "Normal");
				graphics.setAttribute("FontStyle", o.isItalic() ? "Italic" : "Normal");
				graphics.setAttribute("FontDecoration", o.isUnderline() ? "Underline" : "Normal");
				graphics.setAttribute("FontStrikethru", o.isStrikethru() ? "Strikethru" : "Normal");
				graphics.setAttribute("FontSize", Integer.toString((int)o.getFontSize()));
			}
		}
	}
	
	private static void mapMappInfoData(GmmlDataObject o, Element e)
	{
		o.setMapInfoName (e.getAttributeValue("Name"));
		String organism = e.getAttributeValue("Organism"); 
		if (organism == null) organism = "";
		o.setOrganism (organism);
		
		// TODO: should this safety check for organism be done for all properties?
		
		o.setMapInfoDataSource (e.getAttributeValue("Data-Source"));
		o.setVersion (e.getAttributeValue("Version"));
		o.setAuthor (e.getAttributeValue("Author"));
		o.setMaintainedBy (e.getAttributeValue("Maintained-By"));
		o.setEmail (e.getAttributeValue("Email"));
		o.setLastModified (e.getAttributeValue("Last-Modified"));
		o.setAvailability (e.getAttributeValue("Availability"));
		
		Element g = e.getChild("Graphics");
		o.setBoardWidth (Double.parseDouble(g.getAttributeValue("BoardWidth")) / GmmlData.GMMLZOOM);
		o.setBoardHeight (Double.parseDouble(g.getAttributeValue("BoardHeight"))/ GmmlData.GMMLZOOM);
		o.setWindowWidth (Double.parseDouble(g.getAttributeValue("WindowWidth")) / GmmlData.GMMLZOOM);
		o.setWindowHeight (Double.parseDouble(g.getAttributeValue("WindowHeight"))/ GmmlData.GMMLZOOM);
		o.setMapInfoLeft (0);//Integer.parseInt(g.getAttributeValue("MapInfoLeft")) / GmmlData.GMMLZOOM;		
		o.setMapInfoTop (0);//Integer.parseInt(g.getAttributeValue("MapInfoTop")) / GmmlData.GMMLZOOM;
		
		o.setNotes (e.getChildText("Notes"));
		o.setComment (e.getChildText("Comment"));
	}
	
	static public Element createJdomElement(GmmlDataObject o) throws ConverterException 
	{		
		Element e = null;
		
		switch (o.getObjectType())
		{
			case ObjectType.GENEPRODUCT:
				e = new Element("GeneProduct");
				updateNotesAndComment(o, e);
				e.addContent(new Element("Graphics"));			
				updateGeneProductData(o, e);
				updateColor(o, e);
				updateShapeData(o, e);
				updateGraphId(o, e);
				break;
			case ObjectType.SHAPE:
				e = new Element ("Shape");		
				updateNotesAndComment(o, e);
				e.addContent(new Element("Graphics"));
				updateShapeColor(o, e);
				updateRotation(o, e);
				updateShapeData(o, e);
				updateShapeType(o, e);
				updateGraphId(o, e);
				break;
			case ObjectType.FIXEDSHAPE:
				e = new Element ("FixedShape");		
				updateNotesAndComment(o, e);
				e.addContent(new Element("Graphics"));					
				updateCenter(o, e);
				updateShapeType(o, e);
				updateGraphId(o, e);
				break;
			case ObjectType.COMPLEXSHAPE:
				e = new Element ("ComplexShape");		
				updateNotesAndComment(o, e);
				e.addContent(new Element("Graphics"));					
				updateRotation(o, e);
				updateCenter(o, e);
				updateWidth(o, e);
				updateShapeType(o, e);
				updateGraphId(o, e);
				break;
			case ObjectType.BRACE:
				e = new Element("Brace");
				updateNotesAndComment(o, e);
				e.addContent(new Element("Graphics"));
					
				updateColor(o, e);
				updateBraceData(o, e);
				updateGraphId(o, e);
				break;
			case ObjectType.LINE:
				e = new Element("Line");
				updateNotesAndComment(o, e);
				e.addContent(new Element("Graphics"));				
				updateLineData(o, e);
				updateColor(o, e);
				break;
			case ObjectType.LABEL:
				e = new Element("Label");
				updateNotesAndComment(o, e);			
				e.addContent(new Element("Graphics"));					
				updateLabelData(o, e);
				updateColor(o, e);
				updateShapeData(o, e);
				updateGraphId(o, e);
				break;
			case ObjectType.LEGEND:
				e = new Element ("Legend");
				updateSimpleCenter (o, e);
				break;
			case ObjectType.INFOBOX:
				e = new Element ("InfoBox");
				updateSimpleCenter (o, e);
				break;
		}
		if (e == null)
		{
			throw new ConverterException ("Error creating jdom element with objectType " + o.getObjectType());
		}
		return e;
	}

}
