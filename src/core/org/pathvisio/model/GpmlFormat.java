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

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.SAXOutputter;
import org.jdom.output.XMLOutputter;
import org.pathvisio.debug.Logger;
import org.xml.sax.SAXException;

/**
 * class responsible for interaction with Gpml format.
 * Contains all gpml-specific constants,
 * and should be the only class (apart from svgFormat)
 * that needs to import jdom
 *  
 * @author Martijn
 *
 */
public class GpmlFormat implements PathwayImporter, PathwayExporter
{
	public static final Namespace GPML = Namespace.getNamespace("http://genmapp.org/GPML/2007");
	public static final Namespace RDF = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	public static final Namespace RDFS = Namespace.getNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	public static final Namespace BIOPAX = Namespace.getNamespace("bp", "http://www.biopax.org/release/biopax-level2.owl#");
	public static final Namespace OWL = Namespace.getNamespace("owl", "http://www.w3.org/2002/07/owl#");

	/**
	 * name of resource containing the gpml schema definition
	 */
	final private static String xsdFile = "GPML.xsd";
	
	private static class AttributeInfo
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
		result.put("PublicationXref@ID", new AttributeInfo ("xsd:string", null, "required"));
		result.put("PublicationXref@Database", new AttributeInfo ("xsd:string", null, "required"));
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
		result.put("DataNode.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("DataNode.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("DataNode.Graphics@Color", new AttributeInfo ("gpml:ColorType", null, "optional"));
		result.put("DataNode.Xref@Database", new AttributeInfo ("gpml:DatabaseType", null, "required"));
		result.put("DataNode.Xref@ID", new AttributeInfo ("gpml:NameType", null, "required"));
		result.put("DataNode@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("DataNode@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("DataNode@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("DataNode@BackpageHead", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@GenMAPP-Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@Type", new AttributeInfo ("gpml:DataNodeType", "Unknown", "optional"));
		result.put("Line.Graphics.Point@x", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@y", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@GraphRef", new AttributeInfo ("xsd:IDREF", null, "optional"));
		result.put("Line.Graphics.Point@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Line.Graphics.Point@Head", new AttributeInfo ("xsd:string", "Line", "optional"));
		result.put("Line.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
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
		result.put("Label@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("Label@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Label@Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@GenMAPP-Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Link.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Link.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Link.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Link.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Link.Graphics@Color", new AttributeInfo ("gpml:ColorType", null, "optional"));
		result.put("Link.Graphics@FontName", new AttributeInfo ("xsd:string", "Arial", "optional"));
		result.put("Link.Graphics@FontStyle", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Link.Graphics@FontDecoration", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Link.Graphics@FontStrikethru", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Link.Graphics@FontWeight", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Link.Graphics@FontSize", new AttributeInfo ("xsd:nonNegativeInteger", "12", "optional"));
		result.put("Link@Href", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Link@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Link@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Link@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("Link@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Link@GenMAPP-Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Shape.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Shape.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("Shape.Graphics@Rotation", new AttributeInfo ("gpml:RotationType", "Top", "optional"));
		result.put("Shape.Graphics@FillColor", new AttributeInfo ("gpml:ColorType", "Transparent", "optional"));
		result.put("Shape@Type", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Shape@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Shape@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("Group@GroupId", new AttributeInfo ("xsd:ID", null, "required"));
		result.put("Group@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Group@TextLabel", new AttributeInfo("xsd:string", null, "optional"));
		result.put("Group@Style", new AttributeInfo ("gpml:GroupStyleType", "None", "optional"));
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
			"Comment", "BiopaxRef", "Graphics", "DataNode", "Line", "Label",
			"Shape", "Group", "InfoBox", "Legend", "Biopax"
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

		Namespace ns = GPML;

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

				updateComments(o, root);
				updateBiopax(o, root);
				updateBiopaxRef(o, root);
				
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

	public static PathwayElement mapElement(Element e) throws ConverterException
	{
		return mapElement (e, null);
	}
	
	/**
	   Create a single PathwayElement based on a piece of Jdom tree. Used also by Patch utility
	   Pathway p may be null
	 */
	public static PathwayElement mapElement(Element e, Pathway p) throws ConverterException
	{
		String tag = e.getName();
		int ot = ObjectType.getTagMapping(tag);
		if (ot == -1)
		{
			// do nothing. This could be caused by
			// tags <comment> or <graphics> that appear
			// as subtags of <pathway>
			return null;
		}
		
		PathwayElement o = new PathwayElement(ot);
		if (p != null)
		{
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
				mapGroupRef(o, e);
				mapBiopaxRef(o, e);
				break;
			case ObjectType.LABEL:
				mapShapeData(o, e, "Label");
				mapColor(o, e);
				mapLabelData(o, e);
				mapComments(o, e);
				mapGraphId(o, e);
				mapGroupRef(o, e);
				mapBiopaxRef(o, e);
				break;
			case ObjectType.LINE:
				mapLineData(o, e);
				mapColor(o, e);
				mapComments(o, e);
				mapGroupRef(o, e);
				mapBiopaxRef(o, e);
				break;
			case ObjectType.MAPPINFO:
				mapMappInfoData(o, e);
				mapBiopaxRef(o, e);
				mapComments(o, e);
				break;
			case ObjectType.SHAPE:
				mapShapeData(o, e, "Shape");
				mapShapeColor (o, e);
				mapColor(o, e);
				mapComments(o, e);
				mapShapeType(o, e);
				mapGraphId(o, e);
				mapGroupRef(o, e);
				mapBiopaxRef(o, e);
				break;
			case ObjectType.LEGEND:
				mapSimpleCenter(o, e);
				break;
			case ObjectType.INFOBOX:
				mapSimpleCenter (o, e);
				break;
			case ObjectType.GROUP:
				mapGroupRef(o, e);
				mapGroup (o, e);
				mapComments(o, e);
				mapBiopaxRef(o, e);
				break;
			case ObjectType.BIOPAX:
				mapBiopax(o, e);
				break;
			default:
				throw new ConverterException("Invalid ObjectType'" + tag + "'");
		}
		return o;
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
	
	private static void mapColor(PathwayElement o, Element e) throws ConverterException
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	String scol = getAttribute(e.getName() + ".Graphics", "Color", graphics);
    	o.setColor (gmmlString2Color(scol));
	}

	private static void mapShapeColor(PathwayElement o, Element e) throws ConverterException
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	String scol = getAttribute("Shape.Graphics", "FillColor", graphics);
    	if(scol.equals("Transparent")) {
    		o.setTransparent (true);
    	} else {
    		o.setTransparent (false);
    		o.setFillColor (gmmlString2Color(scol));
    	}
	}

	private static void updateColor(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			if(jdomGraphics != null) 
			{
				setAttribute(e.getName() + ".Graphics", "Color", jdomGraphics, color2HexBin(o.getColor()));
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
			}			
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
				f.setText (c.getComment());
				setAttribute("Comment", "Source", f, c.getSource());
				e.addContent(f);
			}
		}
	}
	
	private static void mapGraphId (PathwayElement o, Element e)
	{
		String id = e.getAttributeValue("GraphId");
		if((id == null || id.equals("")) && o.getParent() != null) {
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
		
	private static void mapGroupRef (PathwayElement o, Element e) 
	{
		String id = e.getAttributeValue("GroupRef");
		if(id != null && !id.equals("")) {
			o.setGroupRef (id);
		}
		
	}

	private static void updateGroupRef (PathwayElement o, Element e) 
	{
		String id = o.getGroupRef();
		if (id != null && !id.equals(""))
		{
			e.setAttribute("GroupRef", o.getGroupRef());
		} 
	}
	
	private static void mapGroup (PathwayElement o, Element e) throws ConverterException
	{
		//ID
		String id = e.getAttributeValue("GroupId");
		if((id == null || id.equals("")) && o.getParent() != null) 
			{id = o.getParent().getUniqueId();}
		o.setGroupId (id);
		//Style
		o.setGroupStyle(GroupStyle.fromGpmlName(getAttribute("Group", "Style", e)));
		//Label
		o.setTextLabel (getAttribute("Group", "TextLabel", e));
	}
	
	private static void updateGroup (PathwayElement o, Element e) throws ConverterException
	{
		//ID
		String id = o.createGroupId();
		if (id != null && !id.equals(""))
			{e.setAttribute("GroupId", o.createGroupId());}
		//Style
		setAttribute("Group", "Style", e, GroupStyle.toGpmlName(o.getGroupStyle()));
		//Label
		setAttribute ("Group", "TextLabel", e, o.getTextLabel());
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
    	
    	String rotation = getAttribute("Shape.Graphics", "Rotation", graphics);
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
	}
		
	private static void mapBiopax(PathwayElement o, Element e) throws ConverterException
	{
		//this method clones all content, 
		//getContent will leave them attached to the parent, which we don't want
		//We can safely remove them, since the JDOM element isn't used anymore after this method
		Element root = new Element("RDF", RDF);
		root.addNamespaceDeclaration(RDFS);
		root.addNamespaceDeclaration(RDF);
		root.addNamespaceDeclaration(OWL);
		root.addNamespaceDeclaration(BIOPAX);
		root.setAttribute(new Attribute("base", GPML.getURI() + "#", Namespace.XML_NAMESPACE));
		//Element owl = new Element("Ontology", OWL);
		//owl.setAttribute(new Attribute("about", "", RDF));
		//Element imp = new Element("imports", OWL);
		//imp.setAttribute(new Attribute("resource", BIOPAX.getURI(), RDF));
		//owl.addContent(imp);
		//root.addContent(owl);
		
		root.addContent(e.cloneContent());
		Document bp = new Document(root);
				
		o.setBiopax(bp);
	}
	
	private static void updateBiopax(PathwayElement o, Element e) throws ConverterException
	{
		Document bp = o.getBiopax();
		if(e != null && bp != null) {
			List<Content> content = bp.getRootElement().cloneContent();
			for(Content c : content) {
				if(c instanceof Element) {
					Element elm = (Element)c;
					if(elm.getNamespace().equals(BIOPAX)) {
						e.addContent(c);
					} else if(elm.getName().equals("RDF") && elm.getNamespace().equals(RDF)) {
						for(Object ce : elm.getChildren()) {
							if(((Element)ce).getNamespace().equals(BIOPAX)) {
								e.addContent((Element)ce);
							}
						}
					} else {
						Logger.log.info("Skipped non-biopax element" + c);
					}
				}
			}
		}
	}
	
	private static void mapBiopaxRef(PathwayElement o, Element e) throws ConverterException
	{
		for (Object f : e.getChildren("BiopaxRef", e.getNamespace()))
		{
			o.addBiopaxRef(((Element)f).getText());
		}  
	}
	
	private static void updateBiopaxRef(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) 
		{
			for (String ref : o.getBiopaxRefs())
			{
				Element f = new Element ("BiopaxRef", e.getNamespace());
				f.setText (ref);
				e.addContent(f);
			}
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
				updateBiopaxRef(o, e);
				e.addContent(new Element("Graphics", ns));			
				e.addContent(new Element("Xref", ns));			
				updateDataNode(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "DataNode");
				updateGraphId(o, e);				
				updateGroupRef(o, e);
				break;
			case ObjectType.SHAPE:
				e = new Element ("Shape", ns);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				e.addContent(new Element("Graphics", ns));
				updateShapeColor(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "Shape");
				updateShapeType(o, e);
				updateGraphId(o, e);
				updateGroupRef(o, e);
				break;
			case ObjectType.LINE:
				e = new Element("Line", ns);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				e.addContent(new Element("Graphics", ns));				
				updateLineData(o, e);
				updateColor(o, e);
				updateGroupRef(o, e);
				break;
			case ObjectType.LABEL:
				e = new Element("Label", ns);
				updateComments(o, e);			
				updateBiopaxRef(o, e);
				e.addContent(new Element("Graphics", ns));					
				updateLabelData(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "Label");
				updateGraphId(o, e);
				updateGroupRef(o, e);
				break;
			case ObjectType.LEGEND:
				e = new Element ("Legend", ns);
				updateSimpleCenter (o, e);
				break;
			case ObjectType.INFOBOX:
				e = new Element ("InfoBox", ns);
				updateSimpleCenter (o, e);
				break;
			case ObjectType.GROUP:
				e = new Element ("Group", ns);
				updateGroup (o, e);
				updateGroupRef(o, e);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				break;
			case ObjectType.BIOPAX:
				e = new Element ("Biopax", ns);
				updateBiopax(o, e);
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
    			Logger.log.error("while converting color: " +
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
		String red = padding(Integer.toBinaryString(color.getRed()), 8, '0');
		String green = padding(Integer.toBinaryString(color.getGreen()), 8, '0');
		String blue = padding(Integer.toBinaryString(color.getBlue()), 8, '0');
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

	public void doImport(File file, Pathway pathway) throws ConverterException
	{
		readFromXml(pathway, file, true);
	}

	public void doExport(File file, Pathway pathway) throws ConverterException
	{
		writeToXml(pathway, file, true);
	}
	
	public String[] getExtensions() {
		return new String[] { "gpml", "xml" };
	}

	public String getName() {
		return "GPML file";
	}
	
	/**
	 * Writes the JDOM document to the file specified
	 * @param file	the file to which the JDOM document should be saved
	 * @param validate if true, validate the dom structure before writing to file. If there is a validation error, 
	 * 		or the xsd is not in the classpath, an exception will be thrown. 
	 */
	static public void writeToXml(Pathway pwy, File file, boolean validate) throws ConverterException 
	{
		Document doc = createJdom(pwy);
		
		//Validate the JDOM document
		if (validate) validateDocument(doc);
		//			Get the XML code
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
	
	static public void readFromXml(Pathway pwy, File file, boolean validate) throws ConverterException
	{		
		FileReader inf;
		try	
		{
			inf = new FileReader (file);
		}
		catch (FileNotFoundException e)
		{
			throw new ConverterException (e);
		}
		readFromXml (pwy, inf, validate);
	}
	
	static public void readFromXml(Pathway pwy, Reader in, boolean validate) throws ConverterException
	{
		// Start XML processing
		
		SAXBuilder builder  = new SAXBuilder(false); // no validation when reading the xml file
		// try to read the file; if an error occurs, catch the exception and print feedback
		try
		{
			Logger.log.trace ("Build JDOM tree");
			// build JDOM tree
			Document doc = builder.build(in);

			Logger.log.trace ("Start Validation");
			if (validate) validateDocument(doc);
			
			// Copy the pathway information to a VPathway
			Element root = doc.getRootElement();
			
			Logger.log.trace ("Copy map elements");
			mapElement(root, pwy); // MappInfo
			
			// Iterate over direct children of the root element
			for (Object e : root.getChildren())
			{
				mapElement((Element)e, pwy);
			}			
			Logger.log.trace ("End copying map elements");
		}
		catch(JDOMParseException pe) 
		{
			 throw new ConverterException (pe);
		}
		catch(JDOMException e)
		{
			throw new ConverterException (e);
		}
		catch(IOException e)
		{
			throw new ConverterException (e);
		}
		catch(NullPointerException e)
		{
			throw new ConverterException (e);
		}
	}

	/**
	 * validates a JDOM document against the xml-schema definition specified by 'xsdFile'
	 * @param doc the document to validate
	 */
	public static void validateDocument(Document doc) throws ConverterException
	{	
		ClassLoader cl = Pathway.class.getClassLoader();
		InputStream is = cl.getResourceAsStream(xsdFile);
		if(is != null) {	
			Schema schema;
			try {
				SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				StreamSource ss = new StreamSource (is);
				schema = factory.newSchema(ss);
				ValidatorHandler vh =  schema.newValidatorHandler();
				SAXOutputter so = new SAXOutputter(vh);
				so.output(doc);
				// If no errors occur, the file is valid according to the gpml xml schema definition
				//TODO: open dialog to report error
				Logger.log.info("Document is valid according to the xml schema definition '" + 
						xsdFile.toString() + "'");
			} catch (SAXException se) {
				Logger.log.error("Could not parse the xml-schema definition", se);
				throw new ConverterException (se);
			} catch (JDOMException je) {
				Logger.log.error("Document is invalid according to the xml-schema definition!: " + 
						je.getMessage(), je);
				XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
				
				Logger.log.error("The invalid XML code:\n" + xmlcode.outputString(doc));
				throw new ConverterException (je);
			}
		} else {
			Logger.log.error("Document is not validated because the xml schema definition '" + 
					xsdFile + "' could not be found in classpath");
			throw new ConverterException ("Document is not validated because the xml schema definition '" + 
					xsdFile + "' could not be found in classpath");
		}
	}

}