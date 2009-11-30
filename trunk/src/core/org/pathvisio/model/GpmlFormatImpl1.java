// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import org.bridgedb.DataSource;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.SAXOutputter;
import org.jdom.output.XMLOutputter;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;
import org.xml.sax.SAXException;

/**
 * class responsible for interaction with Gpml format.
 * Contains all gpml-specific constants,
 * and should be the only class (apart from svgFormat)
 * that needs to import jdom
 */
public class GpmlFormatImpl1
{

	public static final GpmlFormatImpl1 GPML_2007 = new GpmlFormatImpl1 (
			"GPML2007.xsd", Namespace.getNamespace("http://genmapp.org/GPML/2007")
		);
	public static final GpmlFormatImpl1 GPML_2008A = new GpmlFormatImpl1 (
			"GPML.xsd", Namespace.getNamespace("http://genmapp.org/GPML/2008a")
		);

	private GpmlFormatImpl1 (String xsdFile, Namespace nsGPML)
	{
		this.xsdFile = xsdFile;
		this.nsGPML = nsGPML;
	}

	private Namespace nsGPML;
	private String xsdFile;

	public Namespace getGpmlNamespace () { return nsGPML; }
	/**
	 * The factor that is used to convert pixel coordinates
	 * to the GPML model coordinates. E.g. if you want to convert the
	 * width from pixels to GPML model coordinates you use:
	 *
	 * double mWidth = width * PIXEL_TO_MODEL;
	 */
	public static final double PIXEL_TO_MODEL = 15;

	/**
	 * name of resource containing the gpml schema definition
	 */

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

		AttributeInfo (String aSchemaType, String aDef, String aUse)
		{
			schemaType = aSchemaType;
			def = aDef;
			use = aUse;
		}
	}

	private static final Map<String, AttributeInfo> ATTRIBUTE_INFO = initAttributeInfo();

	private static Map<String, AttributeInfo> initAttributeInfo()
	{
		Map<String, AttributeInfo> result = new HashMap<String, AttributeInfo>();
		// IMPORTANT: this array has been generated from the xsd with
		// an automated perl script. Don't edit this directly, use the perl script instead.
		/* START OF AUTO-GENERATED CONTENT */
		result.put("Comment@Source", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("PublicationXref@ID", new AttributeInfo ("xsd:string", null, "required"));
		result.put("PublicationXref@Database", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Attribute@Key", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Attribute@Value", new AttributeInfo ("xsd:string", null, "required"));
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
		result.put("Pathway@License", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Copyright", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Last-Modified", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("DataNode.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("DataNode.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("DataNode.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("DataNode.Graphics@Color", new AttributeInfo ("gpml:ColorType", null, "optional"));
		result.put("DataNode.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("DataNode.Xref@Database", new AttributeInfo ("gpml:DatabaseType", null, "required"));
		result.put("DataNode.Xref@ID", new AttributeInfo ("gpml:NameType", null, "required"));
		result.put("DataNode@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("DataNode@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("DataNode@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("DataNode@BackpageHead", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@GenMAPP-Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@Type", new AttributeInfo ("gpml:DataNodeType", "Unknown", "optional"));
		result.put("State.Graphics@relX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("State.Graphics@relY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("State.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("State.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("State.Graphics@Color", new AttributeInfo ("gpml:ColorType", null, "optional"));
		result.put("State.Graphics@FillColor", new AttributeInfo ("gpml:ColorType", null, "optional"));
		result.put("State.Xref@Database", new AttributeInfo ("gpml:DatabaseType", null, "required"));
		result.put("State.Xref@ID", new AttributeInfo ("gpml:NameType", null, "required"));
		result.put("State@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("State@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("State@GraphRef", new AttributeInfo ("xsd:IDREF", null, "optional"));
		result.put("State@Style", new AttributeInfo ("gpml:StyleType", "Solid", "optional"));
		result.put("State@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("State@StateType", new AttributeInfo ("xsd:string", "Unknown", "optional"));
		result.put("State@ShapeType", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Line.Graphics.Point@x", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@y", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@relX", new AttributeInfo ("xsd:float", null, "optional"));
		result.put("Line.Graphics.Point@relY", new AttributeInfo ("xsd:float", null, "optional"));
		result.put("Line.Graphics.Point@GraphRef", new AttributeInfo ("xsd:IDREF", null, "optional"));
		result.put("Line.Graphics.Point@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Line.Graphics.Point@ArrowHead", new AttributeInfo ("xsd:string", "Line", "optional"));
		result.put("Line.Graphics.Point@Head", new AttributeInfo ("xsd:string", "Line", "optional"));
		result.put("Line.Graphics.Anchor@position", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Anchor@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Line.Graphics.Anchor@Shape", new AttributeInfo ("xsd:string", "ReceptorRound", "optional"));
		result.put("Line.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("Line.Graphics@ConnectorType", new AttributeInfo ("xsd:string", "Straight", "optional"));
		result.put("Line.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("Line@Style", new AttributeInfo ("gpml:StyleType", "Solid", "optional"));
		result.put("Line@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Line@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Line@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
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
		result.put("Label.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("Label@Href", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@PathwayRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Label@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("Label@Outline", new AttributeInfo ("xsd:string", "None", "optional"));
		result.put("Label@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Label@Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@GenMAPP-Xref", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Shape.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Shape.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("Shape.Graphics@Rotation", new AttributeInfo ("gpml:RotationType", "Top", "optional"));
		result.put("Shape.Graphics@FillColor", new AttributeInfo ("gpml:ColorType", "Transparent", "optional"));
		result.put("Shape.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("Shape@Type", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Shape@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Shape@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape@ObjectType", new AttributeInfo ("gpml:ObjectType", "Annotation", "optional"));
		result.put("Shape@Style", new AttributeInfo ("gpml:StyleType", "Solid", "optional"));
		result.put("Group@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Group@GroupId", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Group@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Group@Style", new AttributeInfo ("xsd:string", "None", "optional"));
		result.put("Group@TextLabel", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Group@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
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
	private  void setAttribute(String tag, String name, Element el,
			String value) throws ConverterException {
		String key = tag + "@" + name;
		if (!ATTRIBUTE_INFO.containsKey(key))
			throw new ConverterException("Trying to set invalid attribute "
					+ key);
		AttributeInfo aInfo = ATTRIBUTE_INFO.get(key);
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
				if(aInfo.def != null && value != null) {
					Double x = Double.parseDouble(aInfo.def);
					Double y = Double.parseDouble(value);
					if (Math.abs(x - y) < 1e-6)
						isDefault = true;
				}
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
	private  String getAttribute(String tag, String name, Element el) throws ConverterException
	{
		String key = tag + "@" + name;
		if (!ATTRIBUTE_INFO.containsKey(key))
				throw new ConverterException("Trying to get invalid attribute " + key);
		AttributeInfo aInfo = ATTRIBUTE_INFO.get(key);
		String result = ((el == null) ? aInfo.def : el.getAttributeValue(name, aInfo.def));
		return result;
	}

	/**
	 * The GPML xsd implies a certain ordering for children of the pathway element.
	 * (e.g. DataNode always comes before LineShape, etc.)
	 *
	 * This Comparator can sort jdom Elements so that they are in the correct order
	 * for the xsd.
	 */
	private static class ByElementName implements Comparator<Element>
	{
		// hashmap for quick lookups during sorting
		private Map<String, Integer> elementOrdering;

		// correctly ordered list of tag names, which are loaded into the hashmap in
		// the constructor.
		private final String[] elements = new String[] {
				"Comment", "BiopaxRef", "Graphics", "DataNode", "State", "Line", "Label",
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

	public Document createJdom(Pathway data) throws ConverterException
	{
		Document doc = new Document();

		Namespace ns = nsGPML;

		Element root = new Element("Pathway", ns);
		doc.setRootElement(root);

		List<Element> elementList = new ArrayList<Element>();

		List<PathwayElement> pathwayElements = data.getDataObjects();
		Collections.sort(pathwayElements);
		for (PathwayElement o : pathwayElements)
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
			{
				setAttribute("Pathway", "Name", root, o.getMapInfoName());
				setAttribute("Pathway", "Data-Source", root, o.getMapInfoDataSource());
				setAttribute("Pathway", "Version", root, o.getVersion());
				setAttribute("Pathway", "Author", root, o.getAuthor());
				setAttribute("Pathway", "Maintainer", root, o.getMaintainer());
				setAttribute("Pathway", "Email", root, o.getEmail());
				setAttribute("Pathway", "Copyright", root, o.getCopyright());
				setAttribute("Pathway", "Last-Modified", root, o.getLastModified());
				setAttribute("Pathway", "Organism", root, o.getOrganism());

				updateComments(o, root);
				updateBiopaxRef(o, root);
				updateAttributes(o, root);

				Element graphics = new Element("Graphics", ns);
				root.addContent(graphics);

				double[] size = o.getMBoardSize();
				setAttribute("Pathway.Graphics", "BoardWidth", graphics, "" +size[0]);
				setAttribute("Pathway.Graphics", "BoardHeight", graphics, "" + size[1]);
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

	public PathwayElement mapElement(Element e) throws ConverterException
	{
		return mapElement (e, null);
	}

	/**
	   Create a single PathwayElement based on a piece of Jdom tree. Used also by Patch utility
	   Pathway p may be null
	 */
	public PathwayElement mapElement(Element e, Pathway p) throws ConverterException
	{
		String tag = e.getName();
		ObjectType ot = ObjectType.getTagMapping(tag);
		if (ot == null)
		{
			// do nothing. This could be caused by
			// tags <comment> or <graphics> that appear
			// as subtags of <pathway>
			return null;
		}

		PathwayElement o = PathwayElement.createPathwayElement(ot);
		if (p != null)
		{
			p.add (o);
		}

		switch (o.getObjectType())
		{
			case DATANODE:
				mapShapeData(o, e, "DataNode");
				mapColor(o, e);
				mapComments(o, e);
				mapDataNode(o, e);
				mapGraphId(o, e);
				mapGroupRef(o, e);
				mapBiopaxRef(o, e);
				mapAttributes(o, e);
				break;
			case STATE:
				mapShapeColor(o, e);
				mapStateData(o, e);
				mapColor(o, e);
				mapComments(o, e);
				mapGraphId(o, e);
				mapBiopaxRef(o, e);
				mapAttributes(o, e);
				break;
			case LABEL:
				mapShapeData(o, e, "Label");
				mapColor(o, e);
				mapLabelData(o, e);
				mapComments(o, e);
				mapGraphId(o, e);
				mapGroupRef(o, e);
				mapBiopaxRef(o, e);
				mapAttributes(o, e);
				break;
			case LINE:
				mapLineData(o, e);
				mapColor(o, e);
				mapGraphId(o, e);
				mapComments(o, e);
				mapGroupRef(o, e);
				mapBiopaxRef(o, e);
				mapAttributes(o, e);
				break;
			case MAPPINFO:
				mapMappInfoData(o, e);
				mapBiopaxRef(o, e);
				mapComments(o, e);
				mapAttributes(o, e);
				break;
			case SHAPE:
				mapShapeData(o, e, "Shape");
				mapShapeColor (o, e);
				mapColor(o, e);
				mapComments(o, e);
				mapShapeType(o, e);
				mapGraphId(o, e);
				mapGroupRef(o, e);
				mapBiopaxRef(o, e);
				mapAttributes(o, e);
				break;
			case LEGEND:
				mapSimpleCenter(o, e);
				break;
			case INFOBOX:
				mapSimpleCenter (o, e);
				break;
			case GROUP:
				mapGroupRef(o, e);
				mapGroup (o, e);
				mapComments(o, e);
				mapBiopaxRef(o, e);
				mapAttributes(o, e);
				break;
			case BIOPAX:
				mapBiopax(o, e);
				break;
			default:
				throw new ConverterException("Invalid ObjectType'" + tag + "'");
		}
		return o;
	}

	private void mapStateData(PathwayElement o, Element e) throws ConverterException
	{
    	String ref = getAttribute("State", "GraphRef", e);
    	if (ref != null) {
    		o.setGraphRef(ref);
    	}

    	Element graphics = e.getChild("Graphics", e.getNamespace());

    	o.setRelX(Double.parseDouble(getAttribute("State.Graphics", "relX", graphics)));
    	o.setRelY(Double.parseDouble(getAttribute("State.Graphics", "relY", graphics)));
		o.setMWidth (Double.parseDouble(getAttribute("State.Graphics", "Width", graphics)));
		o.setMHeight (Double.parseDouble(getAttribute("State.Graphics", "Height", graphics)));

		//TODO
		//StateType
		// ShapeType???
		// Line style???
		// Xref???
	}

	private static void updateStateData(PathwayElement o, Element e) throws ConverterException
	{
		//TODO
	}

	private void mapLineData(PathwayElement o, Element e) throws ConverterException
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());

    	List<MPoint> mPoints = new ArrayList<MPoint>();

    	String startType = null;
    	String endType = null;

    	List<Element> pointElements = graphics.getChildren("Point", e.getNamespace());
    	for(int i = 0; i < pointElements.size(); i++) {
    		Element pe = pointElements.get(i);
    		MPoint mp = o.new MPoint(
    		    	Double.parseDouble(getAttribute("Line.Graphics.Point", "x", pe)),
    		    	Double.parseDouble(getAttribute("Line.Graphics.Point", "y", pe))
    		);
    		mPoints.add(mp);
        	String ref = getAttribute("Line.Graphics.Point", "GraphRef", pe);
        	if (ref != null) {
        		mp.setGraphRef(ref);
        		String srx = pe.getAttributeValue("relX");
        		String sry = pe.getAttributeValue("relY");
        		if(srx != null && sry != null) {
        			mp.setRelativePosition(Double.parseDouble(srx), Double.parseDouble(sry));
        		}
        	}

        	if(i == 0) {
        		startType = getAttribute("Line.Graphics.Point", "ArrowHead", pe);
        		endType = getAttribute("Line.Graphics.Point", "Head", pe);
        	} else if(i == pointElements.size() - 1) {
        		/**
     		   	read deprecated Head attribute for backwards compatibility.
     		   	If an arrowhead attribute is present on the other point,
     		   	it overrides this one.
        		 */
        		if (pe.getAttributeValue("ArrowHead") != null)
        		{
        			endType = getAttribute("Line.Graphics.Point", "ArrowHead", pe);
        		}
        	}
    	}

    	o.setMPoints(mPoints);

    	String style = getAttribute("Line", "Style", e);

    	o.setLineStyle ((style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED);
		o.setStartLineType (LineType.fromName(startType));
    	o.setEndLineType (LineType.fromName(endType));

    	String connType = getAttribute("Line.Graphics", "ConnectorType", graphics);
    	o.setConnectorType(ConnectorType.fromName(connType));

    	String zorder = graphics.getAttributeValue("ZOrder");
		if (zorder != null)
			o.setZOrder(Integer.parseInt(zorder));

    	//Map anchors
    	List<Element> anchors = graphics.getChildren("Anchor", e.getNamespace());
    	for(Element ae : anchors) {
    		double position = Double.parseDouble(getAttribute("Line.Graphics.Anchor", "position", ae));
    		MAnchor anchor = o.addMAnchor(position);
    		mapGraphId(anchor, ae);
    		String shape = getAttribute("Line.Graphics.Anchor", "Shape", ae);
    		if(shape != null) {
    			anchor.setShape(AnchorType.fromName(shape));
    		}
    	}
	}

	private void updateLineData(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) {
			setAttribute("Line", "Style", e, o.getLineStyle() == LineStyle.SOLID ? "Solid" : "Broken");

			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			List<MPoint> mPoints = o.getMPoints();

			for(int i = 0; i < mPoints.size(); i++) {
				MPoint mp = mPoints.get(i);
				Element pe = new Element("Point", e.getNamespace());
				jdomGraphics.addContent(pe);
				setAttribute("Line.Graphics.Point", "x", pe, Double.toString(mp.getX()));
				setAttribute("Line.Graphics.Point", "y", pe, Double.toString(mp.getY()));
				if (mp.getGraphRef() != null && !mp.getGraphRef().equals(""))
				{
					setAttribute("Line.Graphics.Point", "GraphRef", pe, mp.getGraphRef());
					setAttribute("Line.Graphics.Point", "relX", pe, Double.toString(mp.getRelX()));
					setAttribute("Line.Graphics.Point", "relY", pe, Double.toString(mp.getRelY()));
				}
				if(i == 0) {
					setAttribute("Line.Graphics.Point", "ArrowHead", pe, o.getStartLineType().getName());
				} else if(i == mPoints.size() - 1) {
					setAttribute("Line.Graphics.Point", "ArrowHead", pe, o.getEndLineType().getName());
				}
			}

			for(MAnchor anchor : o.getMAnchors()) {
				Element ae = new Element("Anchor", e.getNamespace());
				setAttribute("Line.Graphics.Anchor", "position", ae, Double.toString(anchor.getPosition()));
				setAttribute("Line.Graphics.Anchor", "Shape", ae, anchor.getShape().getName());
				updateGraphId(anchor, ae);
				jdomGraphics.addContent(ae);
			}

			ConnectorType ctype = o.getConnectorType();
			setAttribute("Line.Graphics", "ConnectorType", jdomGraphics, ctype.getName());
			setAttribute("Line.Graphics", "ZOrder", jdomGraphics, "" + o.getZOrder());
		}
	}

	private void mapColor(PathwayElement o, Element e) throws ConverterException
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());
    	String scol = getAttribute(e.getName() + ".Graphics", "Color", graphics);
    	o.setColor (gmmlString2Color(scol));
	}

	private void mapShapeColor(PathwayElement o, Element e) throws ConverterException
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

	private void updateColor(PathwayElement o, Element e) throws ConverterException
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

	private void updateShapeColor(PathwayElement o, Element e)
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

	private void mapComments(PathwayElement o, Element e) throws ConverterException
	{
		for (Object f : e.getChildren("Comment", e.getNamespace()))
		{
			o.addComment(((Element)f).getText(), getAttribute("Comment", "Source", (Element)f));
		}
	}

	private void updateComments(PathwayElement o, Element e) throws ConverterException
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

	private void mapAttributes(PathwayElement o, Element e) throws ConverterException
	{
		for (Object f : e.getChildren("Attribute", e.getNamespace()))
		{
			o.setDynamicProperty(
					getAttribute("Attribute", "Key", (Element)f),
					getAttribute("Attribute", "Value", (Element)f));
		}
	}

	private void updateAttributes(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			for (String key : o.getDynamicPropertyKeys())
			{
				Element a = new Element ("Attribute", e.getNamespace());
				setAttribute ("Attribute", "Key", a, key);
				setAttribute ("Attribute", "Value", a, o.getDynamicProperty(key));
				e.addContent (a);
			}
		}
	}

	private void mapGraphId (GraphIdContainer o, Element e)
	{
		String id = e.getAttributeValue("GraphId");
		//Never add graphid until all elements are mapped, to prevent duplcate ids!
//		if((id == null || id.equals("")) && o.getGmmlData() != null) {
//			id = o.getGmmlData().getUniqueGraphId();
//		}
		if(id != null) {
			o.setGraphId (id);
		}
	}

	private void updateGraphId (GraphIdContainer o, Element e)
	{
		String id = o.getGraphId();
		// id has to be unique!
		if (id != null && !id.equals(""))
		{
			e.setAttribute("GraphId", o.getGraphId());
		}
	}

	private void mapGroupRef (PathwayElement o, Element e)
	{
		String id = e.getAttributeValue("GroupRef");
		if(id != null && !id.equals("")) {
			o.setGroupRef (id);
		}

	}

	private void updateGroupRef (PathwayElement o, Element e)
	{
		String id = o.getGroupRef();
		if (id != null && !id.equals(""))
		{
			e.setAttribute("GroupRef", o.getGroupRef());
		}
	}

	private void mapGroup (PathwayElement o, Element e) throws ConverterException
	{
		//ID
		String id = e.getAttributeValue("GroupId");
		if((id == null || id.equals("")) && o.getParent() != null)
			{id = o.getParent().getUniqueGroupId();}
		o.setGroupId (id);

		//GraphId
		mapGraphId(o, e);

		//Style
		o.setGroupStyle(GroupStyle.fromName(getAttribute("Group", "Style", e)));
		//Label
		String textLabel = getAttribute("Group", "TextLabel", e);
		if(textLabel != null) {
			o.setTextLabel (textLabel);
		}
	}

	private void updateGroup (PathwayElement o, Element e) throws ConverterException
	{
		//ID
		String id = o.createGroupId();
		if (id != null && !id.equals(""))
			{e.setAttribute("GroupId", o.createGroupId());}

		//GraphId
		updateGraphId(o, e);

		//Style
		setAttribute("Group", "Style", e, o.getGroupStyle().getName());
		//Label
		setAttribute ("Group", "TextLabel", e, o.getTextLabel());
	}

	private void mapDataNode(PathwayElement o, Element e) throws ConverterException
	{
		o.setTextLabel    (getAttribute("DataNode", "TextLabel", e));
		o.setGenMappXref         (getAttribute("DataNode", "GenMAPP-Xref", e));
		o.setDataNodeType (getAttribute("DataNode", "Type", e));
		o.setBackpageHead (getAttribute("DataNode", "BackpageHead", e));
		Element xref = e.getChild ("Xref", e.getNamespace());
		o.setGeneID (getAttribute("DataNode.Xref", "ID", xref));
		o.setDataSource (DataSource.getByFullName (getAttribute("DataNode.Xref", "Database", xref)));
	}

	private void updateDataNode(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) {
			setAttribute ("DataNode", "TextLabel", e, o.getTextLabel());
			setAttribute ("DataNode", "GenMAPP-Xref", e, o.getGenMappXref());
			setAttribute ("DataNode", "Type", e, o.getDataNodeType());
			setAttribute ("DataNode", "BackpageHead", e, o.getBackpageHead());
			Element xref = e.getChild("Xref", e.getNamespace());
			String database = o.getDataSource() == null ? "" : o.getDataSource().getFullName();
			setAttribute ("DataNode.Xref", "Database", xref, database == null ? "" : database);
			setAttribute ("DataNode.Xref", "ID", xref, o.getGeneID());
		}
	}

	private void mapSimpleCenter(PathwayElement o, Element e)
	{
		o.setMCenterX (Double.parseDouble(e.getAttributeValue("CenterX")));
		o.setMCenterY (Double.parseDouble(e.getAttributeValue("CenterY")));
	}

	private void updateSimpleCenter(PathwayElement o, Element e)
	{
		if(e != null)
		{
			e.setAttribute("CenterX", Double.toString(o.getMCenterX()));
			e.setAttribute("CenterY", Double.toString(o.getMCenterY()));
		}
	}

	private void mapShapeData(PathwayElement o, Element e, String base) throws ConverterException
	{
		Element graphics = e.getChild("Graphics", e.getNamespace());
    	o.setMCenterX (Double.parseDouble(getAttribute(base + ".Graphics", "CenterX", graphics)));
    	o.setMCenterY (Double.parseDouble(getAttribute(base + ".Graphics", "CenterY", graphics)));
		o.setMWidth (Double.parseDouble(getAttribute(base + ".Graphics", "Width", graphics)));
		o.setMHeight (Double.parseDouble(getAttribute(base + ".Graphics", "Height", graphics)));
		String zorder = graphics.getAttributeValue("ZOrder");
		if (zorder != null)
			o.setZOrder(Integer.parseInt(zorder));
	}

	private void updateShapeData(PathwayElement o, Element e, String base) throws ConverterException
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
				setAttribute(base + ".Graphics", "ZOrder", graphics, "" + o.getZOrder());
			}
		}
	}

	private void mapShapeType(PathwayElement o, Element e) throws ConverterException
	{
		o.setShapeType (ShapeType.fromGpmlName(getAttribute("Shape", "Type", e)));
		String style = getAttribute ("Shape", "Style", e);
    	o.setLineStyle ((style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED);
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

	private void updateShapeType(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			e.setAttribute("Type", o.getShapeType().getName());
			setAttribute("Line", "Style", e, o.getLineStyle() == LineStyle.SOLID ? "Solid" : "Broken");

			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			if(jdomGraphics !=null)
			{
				jdomGraphics.setAttribute("Rotation", Double.toString(o.getRotation()));
			}
		}
	}

	private void mapLabelData(PathwayElement o, Element e) throws ConverterException
	{
		o.setTextLabel (getAttribute("Label", "TextLabel", e));
    	Element graphics = e.getChild("Graphics", e.getNamespace());

    	String fontSizeString = getAttribute("Label.Graphics", "FontSize", graphics);
    	o.setMFontSize (Integer.parseInt(fontSizeString));

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
    	o.setGenMappXref(xref);
    	String outline = getAttribute("Label", "Outline", e);
		o.setOutline (OutlineType.fromTag (outline));
	}

	private void updateLabelData(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			setAttribute("Label", "TextLabel", e, o.getTextLabel());
			setAttribute("Label", "Xref", e, o.getGenMappXref() == null ? "" : o.getGenMappXref());
			setAttribute("Label", "Outline", e, o.getOutline().getTag());
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

	private void mapMappInfoData(PathwayElement o, Element e) throws ConverterException
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

		//Board size will be calculated
//		o.setMBoardWidth (Double.parseDouble(getAttribute("Pathway.Graphics", "BoardWidth", g)));
//		o.setMBoardHeight (Double.parseDouble(getAttribute("Pathway.Graphics", "BoardHeight", g)));
		o.setWindowWidth (Double.parseDouble(getAttribute("Pathway.Graphics", "WindowWidth", g)));
		o.setWindowHeight (Double.parseDouble(getAttribute("Pathway.Graphics", "WindowHeight", g)));
	}

	private void mapBiopax(PathwayElement o, Element e) throws ConverterException
	{
		//this method clones all content,
		//getContent will leave them attached to the parent, which we don't want
		//We can safely remove them, since the JDOM element isn't used anymore after this method
		Element root = new Element("RDF", GpmlFormat.RDF);
		root.addNamespaceDeclaration(GpmlFormat.RDFS);
		root.addNamespaceDeclaration(GpmlFormat.RDF);
		root.addNamespaceDeclaration(GpmlFormat.OWL);
		root.addNamespaceDeclaration(GpmlFormat.BIOPAX);
		root.setAttribute(new Attribute("base", nsGPML.getURI() + "#", Namespace.XML_NAMESPACE));
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

	private void updateBiopax(PathwayElement o, Element e) throws ConverterException
	{
		Document bp = o.getBiopax();
		if(e != null && bp != null) {
			List<Content> content = bp.getRootElement().cloneContent();
			for(Content c : content) {
				if(c instanceof Element) {
					Element elm = (Element)c;
					if(elm.getNamespace().equals(GpmlFormat.BIOPAX)) {
						e.addContent(c);
					} else if(elm.getName().equals("RDF") && elm.getNamespace().equals(GpmlFormat.RDF)) {
						for(Object ce : elm.getChildren()) {
							if(((Element)ce).getNamespace().equals(GpmlFormat.BIOPAX)) {
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

	private void mapBiopaxRef(PathwayElement o, Element e) throws ConverterException
	{
		for (Object f : e.getChildren("BiopaxRef", e.getNamespace()))
		{
			o.addBiopaxRef(((Element)f).getText());
		}
	}

	private void updateBiopaxRef(PathwayElement o, Element e) throws ConverterException
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

	public Element createJdomElement(PathwayElement o, Namespace ns) throws ConverterException
	{
		Element e = null;
		switch (o.getObjectType())
		{
			case DATANODE:
				e = new Element("DataNode", ns);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", ns));
				e.addContent(new Element("Xref", ns));
				updateDataNode(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "DataNode");
				updateGraphId(o, e);
				updateGroupRef(o, e);
				break;
			case STATE:
				e = new Element("State", ns);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", ns));
				//TODO: Xref?
				updateStateData(o, e);
				updateColor(o, e);
				updateShapeColor(o, e);
				updateGraphId(o, e);
				break;
			case SHAPE:
				e = new Element ("Shape", ns);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", ns));
				updateShapeColor(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "Shape");
				updateShapeType(o, e);
				updateGraphId(o, e);
				updateGroupRef(o, e);
				break;
			case LINE:
				e = new Element("Line", ns);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", ns));
				updateLineData(o, e);
				updateGraphId(o, e);
				updateColor(o, e);
				updateGroupRef(o, e);
				break;
			case LABEL:
				e = new Element("Label", ns);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", ns));
				updateLabelData(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "Label");
				updateGraphId(o, e);
				updateGroupRef(o, e);
				break;
			case LEGEND:
				e = new Element ("Legend", ns);
				updateSimpleCenter (o, e);
				break;
			case INFOBOX:
				e = new Element ("InfoBox", ns);
				updateSimpleCenter (o, e);
				break;
			case GROUP:
				e = new Element ("Group", ns);
				updateGroup (o, e);
				updateGroupRef(o, e);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				break;
			case BIOPAX:
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
    	if(COLOR_MAPPINGS.contains(strColor))
    	{
    		double[] color = (double[])RGB_MAPPINGS.get(COLOR_MAPPINGS.indexOf(strColor));
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

	public static final List<double[]> RGB_MAPPINGS = Arrays.asList(new double[][] {
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

	public static final List<String> COLOR_MAPPINGS = Arrays.asList(new String[]{
			"Aqua", "Black", "Blue", "Fuchsia", "Gray", "Green", "Lime",
			"Maroon", "Navy", "Olive", "Purple", "Red", "Silver", "Teal",
			"White", "Yellow", "Transparent"
		});

	/**
	 * Writes the JDOM document to the file specified
	 * @param file	the file to which the JDOM document should be saved
	 * @param validate if true, validate the dom structure before writing to file. If there is a validation error,
	 * 		or the xsd is not in the classpath, an exception will be thrown.
	 */
	public void writeToXml(Pathway pwy, File file, boolean validate) throws ConverterException
	{
		OutputStream out;
		try
		{
			out = new FileOutputStream(file);
		}
		catch (IOException ex)
		{
			throw new ConverterException (ex);
		}
		writeToXml (pwy, out, validate);
	}

	/**
	 * Writes the JDOM document to the outputstream specified
	 * @param out	the outputstream to which the JDOM document should be writed
	 * @param validate if true, validate the dom structure before writing. If there is a validation error,
	 * 		or the xsd is not in the classpath, an exception will be thrown.
	 * @throws ConverterException
	 */
	public void writeToXml(Pathway pwy, OutputStream out, boolean validate) throws ConverterException {
		Document doc = createJdom(pwy);

		//Validate the JDOM document
		if (validate) validateDocument(doc);
		//			Get the XML code
		XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
		Format f = xmlcode.getFormat();
		f.setEncoding("UTF-8");
		f.setTextMode(Format.TextMode.PRESERVE);
		xmlcode.setFormat(f);

		try
		{
			//Send XML code to the outputstream
			xmlcode.output(doc, out);
		}
		catch (IOException ie)
		{
			throw new ConverterException(ie);
		}
	}

	public void readFromRoot(Element root, Pathway pwy) throws ConverterException
	{
		mapElement(root, pwy); // MappInfo

		// Iterate over direct children of the root element
		for (Object e : root.getChildren())
		{
			mapElement((Element)e, pwy);
		}
		Logger.log.trace ("End copying map elements");

		//Add graphIds for objects that don't have one
		addGraphIds(pwy);

		//Convert absolute point coordinates of linked points to
		//relative coordinates
		convertPointCoordinates(pwy);
	}

	private static void addGraphIds(Pathway pathway) throws ConverterException {
		for(PathwayElement pe : pathway.getDataObjects()) {
			String id = pe.getGraphId();
			if(id == null || "".equals(id))
			{
				if (pe.getObjectType() == ObjectType.LINE)
				{
					// because we forgot to write out graphId's on Lines on older pathways
					// generate a graphId based on hash of coordinates
					// so that pathways with branching history still have the same id.
					// This part may be removed for future versions of GPML (2010+)

					StringBuilder builder = new StringBuilder();
					builder.append(pe.getMStartX());
					builder.append(pe.getMStartY());
					builder.append(pe.getMEndX());
					builder.append(pe.getMEndY());
					builder.append(pe.getStartLineType());
					builder.append(pe.getEndLineType());

					String newId;
					int i = 1;
					do
					{
						newId = "id" + Integer.toHexString((builder.toString() + ("_" + i)).hashCode());
						i++;
					}
					while (pathway.getGraphIds().contains(newId));
					pe.setGraphId(newId);
				}
			}
		}
	}

	private static void convertPointCoordinates(Pathway pathway) throws ConverterException
	{
		for(PathwayElement pe : pathway.getDataObjects()) {
			if(pe.getObjectType() == ObjectType.LINE) {
				String sr = pe.getStartGraphRef();
				String er = pe.getEndGraphRef();
				if(sr != null && !"".equals(sr) && !pe.getMStart().relativeSet()) {
					GraphIdContainer idc = pathway.getGraphIdContainer(sr);
					Point2D relative = idc.toRelativeCoordinate(
							new Point2D.Double(
								pe.getMStart().getRawX(),
								pe.getMStart().getRawY()
							)
					);
					pe.getMStart().setRelativePosition(relative.getX(), relative.getY());
				}
				if(er != null && !"".equals(er) && !pe.getMEnd().relativeSet()) {
					GraphIdContainer idc = pathway.getGraphIdContainer(er);
					Point2D relative = idc.toRelativeCoordinate(
							new Point2D.Double(
								pe.getMEnd().getRawX(),
								pe.getMEnd().getRawY()
							)
					);
					pe.getMEnd().setRelativePosition(relative.getX(), relative.getY());
				}
				((MLine)pe).getConnectorShape().recalculateShape(((MLine)pe));
			}
		}
	}

	/**
	 * validates a JDOM document against the xml-schema definition specified by 'xsdFile'
	 * @param doc the document to validate
	 */
	public void validateDocument(Document doc) throws ConverterException
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