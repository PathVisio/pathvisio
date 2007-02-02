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

import java.util.*;

import org.jdom.Document;
import org.jdom.Element;

import util.ColorConverter;

/**
 * class responsible for interaction with Gpml format.
 * Contains all gpml-specific constants,
 * and should be the only class (apart from svgFormat)
 * that needs to import jdom
 *  
 * @author Martijn
 *
 */
public class GmmlFormat 
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
		result.put("Pathway.Graphics@BoardWidth", new AttributeInfo ("Dimension", null, "required"));
		result.put("Pathway.Graphics@BoardHeight", new AttributeInfo ("Dimension", null, "required"));
		result.put("Pathway.Graphics@WindowWidth", new AttributeInfo ("Dimension", "18000", "optional"));
		result.put("Pathway.Graphics@WindowHeight", new AttributeInfo ("Dimension", "12000", "optional"));
		result.put("Pathway.Graphics@MapInfoLeft", new AttributeInfo ("xsd:float", "0", "optional"));
		result.put("Pathway.Graphics@MapInfoTop", new AttributeInfo ("xsd:float", "0", "optional"));
		result.put("Pathway@Name", new AttributeInfo ("NameType", null, "required"));
		result.put("Pathway@Organism", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Data-Source", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Version", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Author", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Maintained-By", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Email", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Availability", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Last-Modified", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("GeneProduct.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("GeneProduct.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("GeneProduct.Graphics@Width", new AttributeInfo ("Dimension", "600", "optional"));
		result.put("GeneProduct.Graphics@Height", new AttributeInfo ("Dimension", "300", "optional"));
		result.put("GeneProduct.Graphics@Color", new AttributeInfo ("ColorType", null, "optional"));
		result.put("GeneProduct@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("GeneProduct@Name", new AttributeInfo ("NameType", null, "required"));
		result.put("GeneProduct@GeneProduct-Data-Source", new AttributeInfo ("GeneProductDataSourceType", null, "required"));
		result.put("GeneProduct@GeneID", new AttributeInfo ("NameType", null, "optional"));
		result.put("GeneProduct@BackpageHead", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("GeneProduct@Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("GeneProduct@Type", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Line.Graphics.Point@x", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@y", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@GraphRef", new AttributeInfo ("xsd:IDREF", null, "optional"));
		result.put("Line.Graphics.Point@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Line.Graphics@Color", new AttributeInfo ("ColorType", "Black", "optional"));
		result.put("Line@Type", new AttributeInfo ("xsd:string", "Line", "optional"));
		result.put("Line@Style", new AttributeInfo ("xsd:string", "Solid", "optional"));
		result.put("Label.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Label.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Label.Graphics@Width", new AttributeInfo ("Dimension", null, "required"));
		result.put("Label.Graphics@Height", new AttributeInfo ("Dimension", null, "required"));
		result.put("Label.Graphics@Color", new AttributeInfo ("ColorType", null, "optional"));
		result.put("Label.Graphics@FontName", new AttributeInfo ("xsd:string", "Arial", "optional"));
		result.put("Label.Graphics@FontStyle", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontDecoration", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontStrikethru", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontWeight", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontSize", new AttributeInfo ("xsd:nonNegativeInteger", "12", "optional"));
		result.put("Label@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Label@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Label@Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@Width", new AttributeInfo ("Dimension", null, "required"));
		result.put("Shape.Graphics@Height", new AttributeInfo ("Dimension", null, "required"));
		result.put("Shape.Graphics@Color", new AttributeInfo ("ColorType", "Black", "optional"));
		result.put("Shape.Graphics@Rotation", new AttributeInfo ("xsd:float", "0.0", "optional"));
		result.put("Shape.Graphics@FillColor", new AttributeInfo ("ColorType", "Transparent", "optional"));
		result.put("Shape@Type", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Shape@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Brace.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Brace.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Brace.Graphics@Width", new AttributeInfo ("Dimension", null, "required"));
		result.put("Brace.Graphics@PicPointOffset", new AttributeInfo ("Dimension", null, "optional"));
		result.put("Brace.Graphics@Color", new AttributeInfo ("ColorType", "Black", "optional"));
		result.put("Brace.Graphics@Orientation", new AttributeInfo ("xsd:string", "top", "optional"));
		result.put("Brace@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("FixedShape.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("FixedShape.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("FixedShape@Type", new AttributeInfo ("xsd:string", null, "required"));
		result.put("FixedShape@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("ComplexShape.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("ComplexShape.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("ComplexShape.Graphics@Width", new AttributeInfo ("Dimension", null, "required"));
		result.put("ComplexShape.Graphics@Rotation", new AttributeInfo ("xsd:float", "0", "optional"));
		result.put("ComplexShape@Type", new AttributeInfo ("xsd:string", null, "required"));
		result.put("ComplexShape@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
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
	private static void setAttribute (String tag, String name, Element el, String value) throws ConverterException
	{
		String key = tag + "@" + name;
		if (!attributeInfo.containsKey(key)) 
			throw new ConverterException ("Trying to set invalid attribute " + key);
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
	private static String getAttribute (String tag, String name, Element el) throws ConverterException
	{
		String key = tag + "@" + name;
		if (!attributeInfo.containsKey(key)) 
			throw new ConverterException ("Trying to get invalid attribute " + key);
		return el.getAttributeValue(name);		
	}
	
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
				setAttribute("Pathway", "Name", root, o.getMapInfoName());
				setAttribute("Pathway", "Data-Source", root, "GenMAPP 2.0");
				setAttribute("Pathway", "Version", root, o.getVersion());
				setAttribute("Pathway", "Author", root, o.getAuthor());
				setAttribute("Pathway", "Maintained-By", root, o.getMaintainedBy());
				setAttribute("Pathway", "Email", root, o.getEmail());
				setAttribute("Pathway", "Availability", root, o.getAvailability());
				setAttribute("Pathway", "Last-Modified", root, o.getLastModified());

				Element notes = new Element("Notes");
				notes.addContent(o.getNotes());
				root.addContent(notes);

				Element comments = new Element("Comment");
				comments.addContent(o.getComment());
				root.addContent(comments);
				
				Element graphics = new Element("Graphics");
				root.addContent(graphics);
				
				setAttribute("Pathway.Graphics", "BoardWidth", graphics, "" + o.getMBoardWidth()* GmmlData.OLD_GMMLZOOM);
				setAttribute("Pathway.Graphics", "BoardHeight", graphics, "" + o.getMBoardHeight()* GmmlData.OLD_GMMLZOOM);
				setAttribute("Pathway.Graphics", "WindowWidth", graphics, "" + o.getWindowWidth()* GmmlData.OLD_GMMLZOOM);
				setAttribute("Pathway.Graphics", "WindowHeight", graphics, "" + o.getWindowHeight()* GmmlData.OLD_GMMLZOOM);				
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
    	
    	o.setMStartX (Double.parseDouble(p1.getAttributeValue("x")) / GmmlData.OLD_GMMLZOOM);
    	o.setMStartY (Double.parseDouble(p1.getAttributeValue("y")) / GmmlData.OLD_GMMLZOOM);
    	
    	String ref1 = p1.getAttributeValue("GraphRef");
    	if (ref1 == null) ref1 = "";
    	o.setStartGraphRef (ref1);

    	o.setMEndX (Double.parseDouble(p2.getAttributeValue("x")) / GmmlData.OLD_GMMLZOOM);
    	o.setMEndY (Double.parseDouble(p2.getAttributeValue("y")) / GmmlData.OLD_GMMLZOOM); 
    	
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
			p1.setAttribute("x", Double.toString(o.getMStartX() * GmmlData.OLD_GMMLZOOM));
			p1.setAttribute("y", Double.toString(o.getMStartY() * GmmlData.OLD_GMMLZOOM));
			if (o.getStartGraphRef() != null)
			{
				p1.setAttribute("GraphRef", o.getStartGraphRef());
			}
			Element p2 = new Element("Point");
			jdomGraphics.addContent(p2);
			p2.setAttribute("x", Double.toString(o.getMEndX() * GmmlData.OLD_GMMLZOOM));
			p2.setAttribute("y", Double.toString(o.getMEndY() * GmmlData.OLD_GMMLZOOM));
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
    	if (scol != null) 
    	{
    		o.setFillColor (ColorConverter.gmmlString2Color(scol));
    	}
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
			if (!o.getNotes().equals(""))
			{
				Element n = new Element("Notes");
				n.setText(o.getNotes());
				e.addContent(n);
			}
			
			if (!o.getComment().equals(""))
			{
				Element c = new Element ("Comment");
				c.setText(o.getComment());
				e.addContent(c);
			}
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
    	o.setMCenterX (Double.parseDouble(graphics.getAttributeValue("CenterX")) / GmmlData.OLD_GMMLZOOM); 
    	o.setMCenterY (Double.parseDouble(graphics.getAttributeValue("CenterY")) / GmmlData.OLD_GMMLZOOM);	
	}
	
	private static void updateCenter(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("CenterX", Double.toString(o.getMCenterX() * GmmlData.OLD_GMMLZOOM));
				jdomGraphics.setAttribute("CenterY", Double.toString(o.getMCenterY() * GmmlData.OLD_GMMLZOOM));
			}
		}		
	}

	private static void mapWidth(GmmlDataObject o, Element e)
	{
    	Element graphics = e.getChild("Graphics");
    	o.setMWidth (Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.OLD_GMMLZOOM);
	}
	
	private static void updateWidth(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				jdomGraphics.setAttribute("Width", Double.toString(o.getMWidth() * GmmlData.OLD_GMMLZOOM));
			}
		}		
	}

	private static void mapSimpleCenter(GmmlDataObject o, Element e)
	{
		o.setMCenterX (Double.parseDouble(e.getAttributeValue("CenterX")) / GmmlData.OLD_GMMLZOOM); 
		o.setMCenterY (Double.parseDouble(e.getAttributeValue("CenterY")) / GmmlData.OLD_GMMLZOOM);	
	}
	
	private static void updateSimpleCenter(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			e.setAttribute("CenterX", Double.toString(o.getMCenterX() * GmmlData.OLD_GMMLZOOM));
			e.setAttribute("CenterY", Double.toString(o.getMCenterY() * GmmlData.OLD_GMMLZOOM));			
		}		
	}

	private static void mapShapeData(GmmlDataObject o, Element e)
	{
    	mapCenter(o, e);
		Element graphics = e.getChild("Graphics");
		o.setMWidth (Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.OLD_GMMLZOOM); 
		o.setMHeight (Double.parseDouble(graphics.getAttributeValue("Height")) / GmmlData.OLD_GMMLZOOM);
	}
	
	private static void updateShapeData(GmmlDataObject o, Element e)
	{
		if(e != null) 
		{
			Element jdomGraphics = e.getChild("Graphics");
			if(jdomGraphics !=null) 
			{
				updateCenter(o, e);
				jdomGraphics.setAttribute("Width", Double.toString(o.getMWidth() * GmmlData.OLD_GMMLZOOM));
				jdomGraphics.setAttribute("Height", Double.toString(o.getMHeight() * GmmlData.OLD_GMMLZOOM));
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
		o.setMWidth (Double.parseDouble(graphics.getAttributeValue("Width")) / GmmlData.OLD_GMMLZOOM); 
		o.setMHeight (Double.parseDouble(graphics.getAttributeValue("PicPointOffset")) / GmmlData.OLD_GMMLZOOM);
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
				jdomGraphics.setAttribute("Width", Double.toString(o.getMWidth() * GmmlData.OLD_GMMLZOOM));
				jdomGraphics.setAttribute("PicPointOffset", Double.toString(o.getMHeight() * GmmlData.OLD_GMMLZOOM));
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
    	
    	o.setMFontSize (Integer.parseInt(graphics.getAttributeValue("FontSize")));
    	
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
				graphics.setAttribute("FontSize", Integer.toString((int)o.getMFontSize()));
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
		o.setMBoardWidth (Double.parseDouble(g.getAttributeValue("BoardWidth")) / GmmlData.OLD_GMMLZOOM);
		o.setMBoardHeight (Double.parseDouble(g.getAttributeValue("BoardHeight"))/ GmmlData.OLD_GMMLZOOM);
		o.setWindowWidth (Double.parseDouble(g.getAttributeValue("WindowWidth")) / GmmlData.OLD_GMMLZOOM);
		o.setWindowHeight (Double.parseDouble(g.getAttributeValue("WindowHeight"))/ GmmlData.OLD_GMMLZOOM);
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
