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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import org.pathvisio.gui.Engine;
import org.pathvisio.model.Pathway.Color;

/**
 * class responsible for interaction with Gpml format.
 * Contains all gpml-specific constants,
 * and should be the only class (apart from svgFormat)
 * that needs to import jdom
 *  
 * @author Martijn
 *
 */
public class GpmlFormat 
{
	static class AttributeInfo
	{
		/**
		 * xsd validated type. Note that in the current implementation
		 * we don't do anything with restrictions, only with the
		 * base type.
		 */
		public String schemaType;
		
		/**
		 * default value for the attribute
		 */
		public String def; // default
		
		/**
		 * use of the attribute: can be "required" or "optional"
		 */
		public String use;
		
		AttributeInfo (String _schemaType, String _def, String _use)
		{
			schemaType = _schemaType;
			def = _def;
			use = _use;
		}
	}

	static final Map<String, AttributeInfo> attributeInfo = initAttributeInfo();
	
	static Map<String, AttributeInfo> initAttributeInfo()
	{
		Map<String, AttributeInfo> result = new HashMap<String, AttributeInfo>();
		// IMPORTANT: this array has been generated from the xsd with 
		// an automated perl script. Don't edit this directly, use the perl script instead.
		/* START OF AUTO-GENERATED CONTENT */
		result.put("Comment@Source", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway.Graphics@BoardWidth", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Pathway.Graphics@BoardHeight", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Pathway.Graphics@WindowWidth", new AttributeInfo ("gpml:Dimension", "18000", "optional"));
		result.put("Pathway.Graphics@WindowHeight", new AttributeInfo ("gpml:Dimension", "12000", "optional"));
		result.put("Pathway@Name", new AttributeInfo ("gpml:NameType", null, "required"));
		result.put("Pathway@Organism", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Data-Source", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Version", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Author", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Maintainer", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Email", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Copyright", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Last-Modified", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("DataNode.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("DataNode.Graphics@Width", new AttributeInfo ("gpml:Dimension", "600", "optional"));
		result.put("DataNode.Graphics@Height", new AttributeInfo ("gpml:Dimension", "300", "optional"));
		result.put("DataNode.Graphics@Color", new AttributeInfo ("gpml:ColorType", null, "optional"));
		result.put("DataNode.Xref@Database", new AttributeInfo ("gpml:DatabaseType", null, "required"));
		result.put("DataNode.Xref@ID", new AttributeInfo ("gpml:NameType", null, "required"));
		result.put("DataNode@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("DataNode@GroupId", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@GroupStyle", new AttributeInfo ("gpml:GroupStyleType", "Stack", "optional"));
		result.put("DataNode@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("DataNode@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("DataNode@BackpageHead", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@GenMAPP-Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@Type", new AttributeInfo ("gpml:DataNodeType", "Unknown", "optional"));
		result.put("Line.Graphics.Point@x", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@y", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@GraphRef", new AttributeInfo ("xsd:IDREF", null, "optional"));
		result.put("Line.Graphics.Point@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Line.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("Line.Graphics.Point@Head", new AttributeInfo ("xsd:string", "Line", "optional"));
		result.put("Line@Style", new AttributeInfo ("xsd:string", "Solid", "optional"));
		result.put("Label.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Label.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Label.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Label.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Label.Graphics@Color", new AttributeInfo ("gpml:ColorType", null, "optional"));
		result.put("Label.Graphics@FontName", new AttributeInfo ("xsd:string", "Arial", "optional"));
		result.put("Label.Graphics@FontStyle", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontDecoration", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontStrikethru", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontWeight", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontSize", new AttributeInfo ("xsd:nonNegativeInteger", "12", "optional"));
		result.put("Label@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Label@GroupId", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@GroupStyle", new AttributeInfo ("gpml:GroupStyleType", "Stack", "optional"));
		result.put("Label@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("Label@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Label@Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Shape.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Shape.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("Shape.Graphics@Rotation", new AttributeInfo ("gpml:RotationType", "Top", "optional"));
		result.put("Shape.Graphics@FillColor", new AttributeInfo ("gpml:ColorType", "Transparent", "optional"));
		result.put("Shape@Type", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Shape@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Shape@GroupId", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape@GroupStyle", new AttributeInfo ("gpml:GroupStyleType", "Stack", "optional"));
		result.put("Shape@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("InfoBox@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("InfoBox@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Legend@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Legend@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		/* END OF AUTO-GENERATED CONTENT */
		
		return result;
	};
	
	/**
	 * Sets a certain attribute value, 
	 * Does a basic check for some types,
	 * throws an exception when you're trying to set an invalid value
	 * If you're trying to set a default value, or an optional value to null,
	 * the attribute is omitted,
	 * leading to a leaner xml output. 
	 * 
	 * @param tag used for lookup in the defaults table
	 * @param name used for lookup in the defaults table
	 * @param el jdom element where this attribute belongs in
	 * @param value value you wan't to check and set
	 */
	private static void setAttribute(String tag, String name, Element el,
			String value) throws ConverterException {
		String key = tag + "@" + name;
		if (!attributeInfo.containsKey(key))
			throw new ConverterException("Trying to set invalid attribute "
					+ key);
		AttributeInfo aInfo = attributeInfo.get(key);
		boolean isDefault = false;
		// here we start seeing if the attribute is equal to the
		// default value
		// if so, we can leave out the attribute from the jdom
		// altogether
		if (aInfo.use.equals("optional")) {
			if (aInfo.schemaType.equals("xsd:string")
					|| aInfo.schemaType.equals("xsd:ID")) {
				if ((aInfo.def == null && value == null) ||
						(aInfo.def != null && aInfo.def.equals(value)) ||
						(aInfo.def == null && value != null && value.equals("")))
					isDefault = true;
			} else if (aInfo.schemaType.equals("xsd:float")
					|| aInfo.schemaType.equals("Dimension")) {
				Double x = Double.parseDouble(aInfo.def);
				Double y = Double.parseDouble(value);
				if (Math.abs(x - y) < 1e-6)
					isDefault = true;
			}
		}
		if (!isDefault)
			el.setAttribute(name, value);
	}

	/**
	 * Gets a certain attribute value,
	 * replaces it with a suitable default under certain conditions.
	 * 
	 * @param tag used for lookup in the defaults table
	 * @param name used for lookup in the defaults table
	 * @param el jdom element to get the attribute from
	 * @throws ConverterException
	 */
	private static String getAttribute(String tag, String name, Element el) throws ConverterException 
	{
		String key = tag + "@" + name;
		if (!attributeInfo.containsKey(key))
				throw new ConverterException("Trying to get invalid attribute " + key);
		AttributeInfo aInfo = attributeInfo.get(key);
		String result = el.getAttributeValue(name, aInfo.def);
		return result;
	}
	
	/**
	 * The GPML xsd implies a certain ordering for children of the pathway element.
	 * (e.g. DataNode always comes before LineShape, etc.)
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
			"Comment", "Graphics", "DataNode", "Line", "Label",
			"Shape", "InfoBox", "Legend"
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
	
	public static Document createJdom(Pathway data) throws ConverterException
	{
		Document doc = new Document();

		Namespace ns = Namespace.getNamespace("http://genmapp.org/GPML/2007");

		Element root = new Element("Pathway", ns);
		doc.setRootElement(root);

		List<Element> elementList = new ArrayList<Element>();
    	
		for (PathwayElement o : data.getDataObjects())
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
			{
				setAttribute("Pathway", "Name", root, o.getMapInfoName());
				setAttribute("Pathway", "Data-Source", root, "GenMAPP 2.0");
				setAttribute("Pathway", "Version", root, o.getVersion());
				setAttribute("Pathway", "Author", root, o.getAuthor());
				setAttribute("Pathway", "Maintainer", root, o.getMaintainer());
				setAttribute("Pathway", "Email", root, o.getEmail());
				setAttribute("Pathway", "Copyright", root, o.getCopyright());
				setAttribute("Pathway", "Last-Modified", root, o.getLastModified());
				setAttribute("Pathway", "Organism", root, o.getOrganism());

				for (PathwayElement.Comment c : o.getComments())
				{
					Element f = new Element ("Comment", ns);
					f.setText (c.comment);
					setAttribute("Comment", "Source", f, c.source);
					root.addContent(f);
				}				
				
				Element graphics = new Element("Graphics", ns);
				root.addContent(graphics);
				
				setAttribute("Pathway.Graphics", "BoardWidth", graphics, "" + o.getMBoardWidth());
				setAttribute("Pathway.Graphics", "BoardHeight", graphics, "" + o.getMBoardHeight());
				setAttribute("Pathway.Graphics", "WindowWidth", graphics, "" + o.getWindowWidth());
				setAttribute("Pathway.Graphics", "WindowHeight", graphics, "" + o.getWindowHeight());				
			}
			else
			{
				Element e = createJdomElement(o, ns);
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
	
	public static void mapElement(Element e, Pathway p) throws ConverterException
	{
		String tag = e.getName();
		int ot = ObjectType.getTagMapping(tag);
		if (ot == -1)
		{
			// do nothing. This could be caused by
			// tags <comment> or <graphics> that appear
			// as subtags of <pathway>
			return;
		}
		
		PathwayElement o;
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
			o = new PathwayElement(ot);
			p.add (o);
		}
		
		switch (o.getObjectType())
		{
			case ObjectType.DATANODE:
				mapShapeData(o, e, "DataNode");
				mapColor(o, e);
				mapComments(o, e);
				mapDataNode(o, e);
				mapGraphId(o, e);
				break;
			case ObjectType.LABEL:
				mapShapeData(o, e, "Label");
				mapColor(o, e);
				mapLabelData(o, e);
				mapComments(o, e);
				mapGraphId(o, e);
				break;
			case ObjectType.LINE:
				mapLineData(o, e);
				mapColor(o, e);
				mapComments(o, e);
				break;
			case ObjectType.MAPPINFO:
				mapMappInfoData(o, e);
				break;
			case ObjectType.SHAPE:
				mapShapeData(o, e, "Shape");
				mapShapeColor (o, e);
				mapComments(o, e);
				mapShapeType(o, e);
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
	
	private static void mapLineData(PathwayElement o, Element e) throws ConverterException
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	
    	Element p1 = (Element)graphics.getChildren().get(0);
    	Element p2 = (Element)graphics.getChildren().get(1);
    	
    	o.setMStartX (Double.parseDouble(getAttribute("Line.Graphics.Point", "x", p1)));
    	o.setMStartY (Double.parseDouble(getAttribute("Line.Graphics.Point", "y", p1)));
    	
    	String ref1 = getAttribute("Line.Graphics.Point", "GraphRef", p1);
    	if (ref1 == null) ref1 = "";
    	o.setStartGraphRef (ref1);

    	o.setMEndX (Double.parseDouble(getAttribute("Line.Graphics.Point", "x", p2)));
    	o.setMEndY (Double.parseDouble(getAttribute("Line.Graphics.Point", "y", p2))); 
    	
    	String ref2 = getAttribute("Line.Graphics.Point", "GraphRef", p2);
    	if (ref2 == null) ref2 = "";
    	o.setEndGraphRef (ref2);

    	String style = getAttribute("Line", "Style", e);
    	String type = getAttribute("Line.Graphics.Point", "Head", p1);
    	
    	o.setLineStyle ((style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED);
    	o.setLineType (LineType.getByGpmlName(type));
	}
	
	private static void updateLineData(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) {
			setAttribute("Line", "Style", e, o.getLineStyle() == LineStyle.SOLID ? "Solid" : "Broken");
			
			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			Element p1 = new Element("Point", e.getNamespace());
			jdomGraphics.addContent(p1);
			setAttribute("Line.Graphics.Point", "x", p1, Double.toString(o.getMStartX()));
			setAttribute("Line.Graphics.Point", "y", p1, Double.toString(o.getMStartY()));
			setAttribute("Line.Graphics.Point", "Head", p1, o.getLineType().getGpmlName());
			if (o.getStartGraphRef() != null && !o.getStartGraphRef().equals(""))
			{
				setAttribute("Line.Graphics.Point", "GraphRef", p1, o.getStartGraphRef());
			}
			Element p2 = new Element("Point", e.getNamespace());
			jdomGraphics.addContent(p2);
			setAttribute("Line.Graphics.Point", "x", p2, Double.toString(o.getMEndX()));
			setAttribute("Line.Graphics.Point", "y", p2, Double.toString(o.getMEndY()));
			if (o.getEndGraphRef() != null && !o.getEndGraphRef().equals(""))
			{
				setAttribute("Line.Graphics.Point", "GraphRef", p2, o.getEndGraphRef());
			}
		}
	}
	
	private static void mapColor(PathwayElement o, Element e)
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	String scol = graphics.getAttributeValue("Color");
    	o.setColor (gmmlString2Color(scol));
    	o.setTransparent(scol == null || scol.equals("Transparent"));
	}

	private static void mapShapeColor(PathwayElement o, Element e)
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	String scol = graphics.getAttributeValue("FillColor");
    	if (scol != null) 
    	{
    		o.setFillColor (gmmlString2Color(scol));
    	}
    	o.setTransparent (scol == null || scol.equals("Transparent"));
    	scol = graphics.getAttributeValue("Color");
    	o.setColor (gmmlString2Color(scol));
	}

	private static void updateColor(PathwayElement o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			if(jdomGraphics != null) 
			{
				if (o.isTransparent())
					jdomGraphics.setAttribute("Color", "Transparent");
				else
					jdomGraphics.setAttribute("Color", color2HexBin(o.getColor()));
			}
		}
	}
		
	private static void updateShapeColor(PathwayElement o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			if(jdomGraphics != null) 
			{
				if (o.isTransparent())
					jdomGraphics.setAttribute("FillColor", "Transparent");
				else
					jdomGraphics.setAttribute("FillColor", color2HexBin(o.getFillColor()));
				jdomGraphics.setAttribute("Color", color2HexBin(o.getColor()));			}
		}
	}

	private static void mapComments(PathwayElement o, Element e) throws ConverterException
	{
		for (Object f : e.getChildren("Comment", e.getNamespace()))
		{
			o.addComment(((Element)f).getText(), getAttribute("Comment", "Source", (Element)f));
		}    	
	}
	
	private static void updateComments(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) 
		{
			for (PathwayElement.Comment c : o.getComments())
			{
				Element f = new Element ("Comment", e.getNamespace());
				f.setText (c.comment);
				setAttribute("Comment", "Source", f, c.source);
				e.addContent(f);
			}
		}
	}
	
	private static void mapGraphId (PathwayElement o, Element e)
	{
		String id = e.getAttributeValue("GraphId");
		if(id == null || id.equals("")) {
			id = o.getParent().getUniqueId();
		}
		o.setGraphId (id);
	}
	
	private static void updateGraphId (PathwayElement o, Element e)
	{
		String id = o.getGraphId();
		// id has to be unique!
		if (id != null && !id.equals(""))
		{
			e.setAttribute("GraphId", o.getGraphId());
		} 
	}
	
	private static void mapDataNode(PathwayElement o, Element e) throws ConverterException
	{
		o.setTextLabel    (getAttribute("DataNode", "TextLabel", e));
		o.setXref         (getAttribute("DataNode", "GenMAPP-Xref", e));
		o.setDataNodeType (getAttribute("DataNode", "Type", e));
		o.setBackpageHead (getAttribute("DataNode", "BackpageHead", e));
		Element xref = e.getChild ("Xref", e.getNamespace());
		o.setGeneID (getAttribute("DataNode.Xref", "ID", xref));
		o.setDataSource (getAttribute("DataNode.Xref", "Database", xref));
	}

	private static void updateDataNode(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) {
			setAttribute ("DataNode", "TextLabel", e, o.getTextLabel());
			setAttribute ("DataNode", "GenMAPP-Xref", e, o.getXref());
			setAttribute ("DataNode", "Type", e, o.getDataNodeType());
			setAttribute ("DataNode", "BackpageHead", e, o.getBackpageHead());
			Element xref = e.getChild("Xref", e.getNamespace());
			setAttribute ("DataNode.Xref", "Database", xref, o.getDataSource());
			setAttribute ("DataNode.Xref", "ID", xref, o.getGeneID());			
		}
	}

	private static void mapSimpleCenter(PathwayElement o, Element e)
	{
		o.setMCenterX (Double.parseDouble(e.getAttributeValue("CenterX"))); 
		o.setMCenterY (Double.parseDouble(e.getAttributeValue("CenterY")));	
	}
	
	private static void updateSimpleCenter(PathwayElement o, Element e)
	{
		if(e != null) 
		{
			e.setAttribute("CenterX", Double.toString(o.getMCenterX()));
			e.setAttribute("CenterY", Double.toString(o.getMCenterY()));			
		}		
	}

	private static void mapShapeData(PathwayElement o, Element e, String base) throws ConverterException
	{
		Element graphics = e.getChild("Graphics", e.getNamespace());
    	o.setMCenterX (Double.parseDouble(getAttribute(base + ".Graphics", "CenterX", graphics))); 
    	o.setMCenterY (Double.parseDouble(getAttribute(base + ".Graphics", "CenterY", graphics)));	
		o.setMWidth (Double.parseDouble(getAttribute(base + ".Graphics", "Width", graphics))); 
		o.setMHeight (Double.parseDouble(getAttribute(base + ".Graphics", "Height", graphics)));
	}
	
	private static void updateShapeData(PathwayElement o, Element e, String base) throws ConverterException
	{
		if(e != null) 
		{
			Element graphics = e.getChild("Graphics", e.getNamespace());
			if(graphics !=null) 
			{
				setAttribute(base + ".Graphics", "CenterX", graphics, "" + o.getMCenterX());
				setAttribute(base + ".Graphics", "CenterY", graphics, "" + o.getMCenterY());
				setAttribute(base + ".Graphics", "Width", graphics, "" + o.getMWidth());
				setAttribute(base + ".Graphics", "Height", graphics, "" + o.getMHeight());
			}
		}
	}
	
	private static void mapShapeType(PathwayElement o, Element e) throws ConverterException
	{
		o.setShapeType (ShapeType.fromGpmlName(getAttribute("Shape", "Type", e)));
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	
    	String rotation = graphics.getAttributeValue("Rotation");
    	double result;
    	if (rotation.equals("Top"))
    	{
    		result = 0.0;
    	}
    	else if (rotation.equals("Right"))
		{
    		result = 0.5 * Math.PI;
		}
    	else if (rotation.equals("Bottom"))
    	{
    		result = Math.PI;
    	}
    	else if (rotation.equals("Left"))
    	{
    		result = 1.5 * Math.PI;
    	}
    	else
    	{
    		result = Double.parseDouble(rotation);
    	}
    	o.setRotation (result); 
	}
	
	private static void updateShapeType(PathwayElement o, Element e)
	{
		if(e != null) 
		{
			e.setAttribute("Type", ShapeType.toGpmlName(o.getShapeType()));
			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("Rotation", Double.toString(o.getRotation()));
			}
		}
	}
	
	private static void mapLabelData(PathwayElement o, Element e) throws ConverterException
	{
		o.setTextLabel (getAttribute("Label", "TextLabel", e));
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	
    	o.setMFontSize (Integer.parseInt(graphics.getAttributeValue("FontSize")));
    	
    	String fontWeight = getAttribute("Label.Graphics", "FontWeight", graphics);
    	String fontStyle = getAttribute("Label.Graphics", "FontStyle", graphics);
    	String fontDecoration = getAttribute("Label.Graphics", "FontDecoration", graphics);
    	String fontStrikethru = getAttribute("Label.Graphics", "FontStrikethru", graphics);
    	
    	o.setBold (fontWeight != null && fontWeight.equals("Bold"));   	
    	o.setItalic (fontStyle != null && fontStyle.equals("Italic"));    	
    	o.setUnderline (fontDecoration != null && fontDecoration.equals("Underline"));    	
    	o.setStrikethru (fontStrikethru != null && fontStrikethru.equals("Strikethru"));
    	
    	o.setFontName (getAttribute("Label.Graphics", "FontName", graphics));
    	
    	String xref = getAttribute("Label", "Xref", e);
    	if (xref == null) xref = "";
    	o.setXref(xref);
	}
	
	private static void updateLabelData(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) 
		{
			setAttribute("Label", "TextLabel", e, o.getTextLabel());
			setAttribute("Label", "Xref", e, o.getXref() == null ? "" : o.getXref());
			Element graphics = e.getChild("Graphics", e.getNamespace());
			if(graphics !=null) 
			{
				setAttribute("Label.Graphics", "FontName", graphics, o.getFontName() == null ? "" : o.getFontName());			
				setAttribute("Label.Graphics", "FontWeight", graphics, o.isBold() ? "Bold" : "Normal");
				setAttribute("Label.Graphics", "FontStyle", graphics, o.isItalic() ? "Italic" : "Normal");
				setAttribute("Label.Graphics", "FontDecoration", graphics, o.isUnderline() ? "Underline" : "Normal");
				setAttribute("Label.Graphics", "FontStrikethru", graphics, o.isStrikethru() ? "Strikethru" : "Normal");
				setAttribute("Label.Graphics", "FontSize", graphics, Integer.toString((int)o.getMFontSize()));
			}
		}
	}
	
	private static void mapMappInfoData(PathwayElement o, Element e) throws ConverterException
	{
		o.setMapInfoName (getAttribute("Pathway", "Name", e));
		o.setOrganism (getAttribute("Pathway", "Organism", e));	
		o.setMapInfoDataSource (getAttribute("Pathway", "Data-Source", e));
		o.setVersion (getAttribute("Pathway", "Version", e));
		o.setAuthor (getAttribute("Pathway", "Author", e));
		o.setMaintainer (getAttribute("Pathway", "Maintainer", e));
		o.setEmail (getAttribute("Pathway", "Email", e));
		o.setLastModified (getAttribute("Pathway", "Last-Modified", e));
		o.setCopyright (getAttribute("Pathway", "Copyright", e));
		
		Element g = e.getChild("Graphics", e.getNamespace());
		o.setMBoardWidth (Double.parseDouble(getAttribute("Pathway.Graphics", "BoardWidth", g)));
		o.setMBoardHeight (Double.parseDouble(getAttribute("Pathway.Graphics", "BoardHeight", g)));
		o.setWindowWidth (Double.parseDouble(getAttribute("Pathway.Graphics", "WindowWidth", g)));
		o.setWindowHeight (Double.parseDouble(getAttribute("Pathway.Graphics", "WindowHeight", g)));
		
		for (Object f : e.getChildren("Comment", e.getNamespace()))
		{
			o.addComment(((Element)f).getText(), getAttribute("Comment", "Source", (Element)f));
		}		
	}
	
	static public Element createJdomElement(PathwayElement o, Namespace ns) throws ConverterException 
	{		
		Element e = null;
		
		switch (o.getObjectType())
		{
			case ObjectType.DATANODE:
				e = new Element("DataNode", ns);
				updateComments(o, e);
				e.addContent(new Element("Graphics", ns));			
				e.addContent(new Element("Xref", ns));			
				updateDataNode(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "DataNode");
				updateGraphId(o, e);
				break;
			case ObjectType.SHAPE:
				e = new Element ("Shape", ns);
				updateComments(o, e);
				e.addContent(new Element("Graphics", ns));
				updateShapeColor(o, e);
				updateShapeData(o, e, "Shape");
				updateShapeType(o, e);
				updateGraphId(o, e);
				break;
			case ObjectType.LINE:
				e = new Element("Line", ns);
				updateComments(o, e);
				e.addContent(new Element("Graphics", ns));				
				updateLineData(o, e);
				updateColor(o, e);
				break;
			case ObjectType.LABEL:
				e = new Element("Label", ns);
				updateComments(o, e);			
				e.addContent(new Element("Graphics", ns));					
				updateLabelData(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "Label");
				updateGraphId(o, e);
				break;
			case ObjectType.LEGEND:
				e = new Element ("Legend", ns);
				updateSimpleCenter (o, e);
				break;
			case ObjectType.INFOBOX:
				e = new Element ("InfoBox", ns);
				updateSimpleCenter (o, e);
				break;
		}
		if (e == null)
		{
			throw new ConverterException ("Error creating jdom element with objectType " + o.getObjectType());
		}
		return e;
	}

	/**
	 * Converts a string containing either a named color (as specified in gpml) or a hexbinary number
	 * to an {@link Color} object
	 * @param strColor
	 */
    public static Color gmmlString2Color(String strColor)
    {
    	if(colorMappings.contains(strColor))
    	{
    		double[] color = (double[])rgbMappings.get(colorMappings.indexOf(strColor));
    		return new Color((int)(255*color[0]),(int)(255*color[1]),(int)(255*color[2]));
    	}
    	else
    	{
    		try
    		{
    			strColor = padding(strColor, 6, '0');
        		int red = Integer.valueOf(strColor.substring(0,2),16);
        		int green = Integer.valueOf(strColor.substring(2,4),16);
        		int blue = Integer.valueOf(strColor.substring(4,6),16);
        		return new Color(red,green,blue);
    		}
    		catch (Exception e)
    		{
    			Engine.log.error("while converting color: " +
    					"Color " + strColor + " is not valid, element color is set to black", e);
    		}
    	}
    	return new Color(0,0,0);
    }
    
	/**
	 * Converts an {@link Color} object to a hexbinary string
	 * @param color
	 */
	public static String color2HexBin(Color color)
	{
		String red = padding(Integer.toBinaryString(color.red), 8, '0');
		String green = padding(Integer.toBinaryString(color.green), 8, '0');
		String blue = padding(Integer.toBinaryString(color.blue), 8, '0');
		String hexBinary = Integer.toHexString(Integer.valueOf(red + green + blue, 2));
		return padding(hexBinary, 6, '0');
	}
	
    /**
     * Prepends character c x-times to the input string to make it length n
     * @param s	String to pad
     * @param n	Number of characters of the resulting string
     * @param c	character to append
     * @return	string of length n or larger (if given string s > n)
     */
    public static String padding(String s, int n, char c)
    {
    	while(s.length() < n)
    	{
    		s = c + s;
    	}
    	return s;
    }
    
	public static final List rgbMappings = Arrays.asList(new double[][] {
			{0, 1, 1},		// aqua 
			{0, 0, 0},	 	// black
			{0, 0, 1}, 		// blue
			{1, 0, 1},		// fuchsia
			{.5, .5, .5,},	// gray
			{0, .5, 0}, 	// green
			{0, 1, 0},		// lime
			{.5, 0, 0},		// maroon
			{0, 0, .5},		// navy
			{.5, .5, 0},	// olive
			{.5, 0, .5},	// purple
			{1, 0, 0}, 		// red
			{.75, .75, .75},// silver
			{0, .5, .5}, 	// teal
			{1, 1, 1},		// white
			{1, 1, 0},		// yellow
			{0, 0, 0}		// transparent (actually irrelevant)
		});
	
	public static final List colorMappings = Arrays.asList(new String[]{
			"Aqua", "Black", "Blue", "Fuchsia", "Gray", "Green", "Lime",
			"Maroon", "Navy", "Olive", "Purple", "Red", "Silver", "Teal",
			"White", "Yellow", "Transparent"
		});
}
