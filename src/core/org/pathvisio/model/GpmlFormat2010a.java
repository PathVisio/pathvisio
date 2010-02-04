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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bridgedb.DataSource;
import org.jdom.Element;
import org.jdom.Namespace;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;

class GpmlFormat2010a extends GpmlFormatAbstract 
{
	public static final GpmlFormat2010a GPML_2010A = new GpmlFormat2010a (
			"GPML.xsd", Namespace.getNamespace("http://genmapp.org/GPML/2010a")
		);

	public GpmlFormat2010a(String xsdFile, Namespace ns) {
		super (xsdFile, ns);
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
		result.put("Pathway@Name", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Pathway@Organism", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Data-Source", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Version", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Author", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Maintainer", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Email", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@License", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@Last-Modified", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Pathway@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("DataNode.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("DataNode.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("DataNode.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("DataNode.Graphics@FontName", new AttributeInfo ("xsd:string", "Arial", "optional"));
		result.put("DataNode.Graphics@FontStyle", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("DataNode.Graphics@FontDecoration", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("DataNode.Graphics@FontStrikethru", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("DataNode.Graphics@FontWeight", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("DataNode.Graphics@FontSize", new AttributeInfo ("xsd:nonNegativeInteger", "12", "optional"));
		result.put("DataNode.Graphics@Align", new AttributeInfo ("xsd:string", "Center", "optional"));
		result.put("DataNode.Graphics@Valign", new AttributeInfo ("xsd:string", "Top", "optional"));
		result.put("DataNode.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("DataNode.Graphics@LineStyle", new AttributeInfo ("gpml:StyleType", "Solid", "optional"));
		result.put("DataNode.Graphics@LineThickness", new AttributeInfo ("xsd:float", "1.0", "optional"));
		result.put("DataNode.Graphics@FillColor", new AttributeInfo ("gpml:ColorType", "White", "optional"));
		result.put("DataNode.Graphics@ShapeType", new AttributeInfo ("xsd:string", "Rectangle", "optional"));
		result.put("DataNode.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("DataNode.Xref@Database", new AttributeInfo ("xsd:string", null, "required"));
		result.put("DataNode.Xref@ID", new AttributeInfo ("xsd:string", null, "required"));
		result.put("DataNode@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("DataNode@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("DataNode@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("DataNode@Type", new AttributeInfo ("xsd:string", "Unknown", "optional"));
		result.put("State.Graphics@relX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("State.Graphics@relY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("State.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("State.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("State.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("State.Graphics@LineStyle", new AttributeInfo ("gpml:StyleType", "Solid", "optional"));
		result.put("State.Graphics@LineThickness", new AttributeInfo ("xsd:float", "1.0", "optional"));
		result.put("State.Graphics@FillColor", new AttributeInfo ("gpml:ColorType", "White", "optional"));
		result.put("State.Graphics@ShapeType", new AttributeInfo ("xsd:string", "Rectangle", "optional"));
		result.put("State.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("State.Xref@Database", new AttributeInfo ("xsd:string", null, "required"));
		result.put("State.Xref@ID", new AttributeInfo ("xsd:string", null, "required"));
		result.put("State@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("State@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("State@GraphRef", new AttributeInfo ("xsd:IDREF", null, "optional"));
		result.put("State@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("State@StateType", new AttributeInfo ("xsd:string", "Unknown", "optional"));
		result.put("Line.Graphics.Point@x", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@y", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Point@relX", new AttributeInfo ("xsd:float", null, "optional"));
		result.put("Line.Graphics.Point@relY", new AttributeInfo ("xsd:float", null, "optional"));
		result.put("Line.Graphics.Point@GraphRef", new AttributeInfo ("xsd:IDREF", null, "optional"));
		result.put("Line.Graphics.Point@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Line.Graphics.Point@ArrowHead", new AttributeInfo ("xsd:string", "Line", "optional"));
		result.put("Line.Graphics.Anchor@position", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Line.Graphics.Anchor@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Line.Graphics.Anchor@Shape", new AttributeInfo ("xsd:string", "ReceptorRound", "optional"));
		result.put("Line.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("Line.Graphics@LineThickness", new AttributeInfo ("xsd:float", null, "optional"));
		result.put("Line.Graphics@LineStyle", new AttributeInfo ("gpml:StyleType", "Solid", "optional"));
		result.put("Line.Graphics@ConnectorType", new AttributeInfo ("xsd:string", "Straight", "optional"));
		result.put("Line.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("Line@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Line@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Line@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Label.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Label.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Label.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Label.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Label.Graphics@FontName", new AttributeInfo ("xsd:string", "Arial", "optional"));
		result.put("Label.Graphics@FontStyle", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontDecoration", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontStrikethru", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontWeight", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Label.Graphics@FontSize", new AttributeInfo ("xsd:nonNegativeInteger", "12", "optional"));
		result.put("Label.Graphics@Align", new AttributeInfo ("xsd:string", "Center", "optional"));
		result.put("Label.Graphics@Valign", new AttributeInfo ("xsd:string", "Top", "optional"));
		result.put("Label.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("Label.Graphics@LineStyle", new AttributeInfo ("gpml:StyleType", "Solid", "optional"));
		result.put("Label.Graphics@LineThickness", new AttributeInfo ("xsd:float", "1.0", "optional"));
		result.put("Label.Graphics@FillColor", new AttributeInfo ("gpml:ColorType", "Transparent", "optional"));
		result.put("Label.Graphics@ShapeType", new AttributeInfo ("xsd:string", "None", "optional"));
		result.put("Label.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("Label@Href", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@PathwayRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Label@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Label@TextLabel", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Shape.Graphics@CenterX", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@CenterY", new AttributeInfo ("xsd:float", null, "required"));
		result.put("Shape.Graphics@Width", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Shape.Graphics@Height", new AttributeInfo ("gpml:Dimension", null, "required"));
		result.put("Shape.Graphics@FontName", new AttributeInfo ("xsd:string", "Arial", "optional"));
		result.put("Shape.Graphics@FontStyle", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Shape.Graphics@FontDecoration", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Shape.Graphics@FontStrikethru", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Shape.Graphics@FontWeight", new AttributeInfo ("xsd:string", "Normal", "optional"));
		result.put("Shape.Graphics@FontSize", new AttributeInfo ("xsd:nonNegativeInteger", "12", "optional"));
		result.put("Shape.Graphics@Align", new AttributeInfo ("xsd:string", "Center", "optional"));
		result.put("Shape.Graphics@Valign", new AttributeInfo ("xsd:string", "Top", "optional"));
		result.put("Shape.Graphics@Color", new AttributeInfo ("gpml:ColorType", "Black", "optional"));
		result.put("Shape.Graphics@LineStyle", new AttributeInfo ("gpml:StyleType", "Solid", "optional"));
		result.put("Shape.Graphics@LineThickness", new AttributeInfo ("xsd:float", "1.0", "optional"));
		result.put("Shape.Graphics@FillColor", new AttributeInfo ("gpml:ColorType", "Transparent", "optional"));
		result.put("Shape.Graphics@ShapeType", new AttributeInfo ("xsd:string", null, "required"));
		result.put("Shape.Graphics@ZOrder", new AttributeInfo ("xsd:integer", null, "optional"));
		result.put("Shape.Graphics@Rotation", new AttributeInfo ("gpml:RotationType", "Top", "optional"));
		result.put("Shape@BiopaxRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape@GraphId", new AttributeInfo ("xsd:ID", null, "optional"));
		result.put("Shape@GroupRef", new AttributeInfo ("xsd:string", null, "optional"));
		result.put("Shape@TextLabel", new AttributeInfo ("xsd:string", null, "optional"));
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
	}

	@Override
	protected Map<String, AttributeInfo> getAttributeInfo() 
	{
		return ATTRIBUTE_INFO;
	}

	@Override
	protected void mapMappInfoDataVariable(PathwayElement o, Element e)
			throws ConverterException {
		o.setCopyright (getAttribute("Pathway", "License", e));
	}

	@Override
	protected void updateMappInfoVariable(Element root, PathwayElement o)
			throws ConverterException {
		setAttribute("Pathway", "License", root, o.getCopyright());
	}

	public Element createJdomElement(PathwayElement o) throws ConverterException
	{
		Element e = null;
		switch (o.getObjectType())
		{
			case DATANODE:
				e = new Element("DataNode", getGpmlNamespace());
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", getGpmlNamespace()));
				e.addContent(new Element("Xref", getGpmlNamespace()));
				updateShapeColor(o, e);
				updateColor(o, e);
				updateDataNode(o, e);
				updateShapeData(o, e, "DataNode");
				updateGraphId(o, e);
				updateGroupRef(o, e);
				break;
			case STATE:
				e = new Element("State", getGpmlNamespace());
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", getGpmlNamespace()));
				updateShapeColor(o, e);
				updateColor(o, e);
				//TODO: Xref?
				updateStateData(o, e);
				updateGraphId(o, e);
				break;
			case SHAPE:
				e = new Element ("Shape", getGpmlNamespace());
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", getGpmlNamespace()));
				updateShapeColor(o, e);
				updateColor(o, e);
				updateShapeData(o, e, "Shape");
				updateLabelData(o, e, "Label");
				updateShapeType(o, e);
				updateGraphId(o, e);
				updateGroupRef(o, e);
				break;
			case LINE:
				e = new Element("Line", getGpmlNamespace());
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", getGpmlNamespace()));
				updateLineData(o, e);
				updateGraphId(o, e);
				updateColor(o, e);
				updateGroupRef(o, e);
				break;
			case LABEL:
				e = new Element("Label", getGpmlNamespace());
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				e.addContent(new Element("Graphics", getGpmlNamespace()));
				updateShapeColor(o, e);
				updateColor(o, e);
				updateLabelData(o, e, "Label");
				updateShapeData(o, e, "Label");
				updateGraphId(o, e);
				updateGroupRef(o, e);
				break;
			case LEGEND:
				e = new Element ("Legend", getGpmlNamespace());
				updateSimpleCenter (o, e);
				break;
			case INFOBOX:
				e = new Element ("InfoBox", getGpmlNamespace());
				updateSimpleCenter (o, e);
				break;
			case GROUP:
				e = new Element ("Group", getGpmlNamespace());
				updateGroup (o, e);
				updateGroupRef(o, e);
				updateComments(o, e);
				updateBiopaxRef(o, e);
				updateAttributes(o, e);
				break;
			case BIOPAX:
				e = new Element ("Biopax", getGpmlNamespace());
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

	protected void mapShapeType(PathwayElement o, Element e) throws ConverterException
	{
    	Element graphics = e.getChild("Graphics", e.getNamespace());

		String style = getAttribute ("Shape.Graphics", "LineStyle", graphics);
    	o.setLineStyle ((style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED);
		o.setShapeType (ShapeType.fromGpmlName(getAttribute("Shape.Graphics", "ShapeType", graphics)));
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

	protected void updateShapeType(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null)
		{
			Element jdomGraphics = e.getChild("Graphics", e.getNamespace());
			if(jdomGraphics !=null)
			{
				String shapeName = o.getShapeType() == null ? "None" : o.getShapeType().getName();
				setAttribute("Shape.Graphics", "ShapeType", jdomGraphics, shapeName);
				setAttribute("Shape.Graphics", "LineStyle", jdomGraphics, o.getLineStyle() == LineStyle.SOLID ? "Solid" : "Broken");
				setAttribute("Shape.Graphics", "Rotation", jdomGraphics, Double.toString(o.getRotation()));
			}
		}
	}

	protected void mapLabelData(PathwayElement o, Element e) throws ConverterException
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
    	
		setAttribute("Label.Graphics", "ShapeType", graphics, o.getShapeType().getName());

    	o.setFontName (getAttribute("Label.Graphics", "FontName", graphics));
	}
	
	protected void updateLabelData(PathwayElement o, Element e, String base) throws ConverterException
	{
		if(e != null)
		{
			setAttribute(base, "TextLabel", e, o.getTextLabel());
			Element graphics = e.getChild("Graphics", e.getNamespace());
			if(graphics !=null)
			{
				setAttribute(base + ".Graphics", "FontName", graphics, o.getFontName() == null ? "" : o.getFontName());
				setAttribute(base + ".Graphics", "FontWeight", graphics, o.isBold() ? "Bold" : "Normal");
				setAttribute(base + ".Graphics", "FontStyle", graphics, o.isItalic() ? "Italic" : "Normal");
				setAttribute(base + ".Graphics", "FontDecoration", graphics, o.isUnderline() ? "Underline" : "Normal");
				setAttribute(base + ".Graphics", "FontStrikethru", graphics, o.isStrikethru() ? "Strikethru" : "Normal");
				setAttribute(base + ".Graphics", "FontSize", graphics, Integer.toString((int)o.getMFontSize()));
		    	String shapeType = getAttribute(base + ".Graphics", "ShapeType", graphics);
				o.setShapeType(ShapeType.fromGpmlName(shapeType));
			}
		}
	}

	protected void mapShapeData(PathwayElement o, Element e, String base) throws ConverterException
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

	protected void updateShapeData(PathwayElement o, Element e, String base) throws ConverterException
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

	protected void mapDataNode(PathwayElement o, Element e) throws ConverterException
	{
		o.setTextLabel    (getAttribute("DataNode", "TextLabel", e));
		o.setDataNodeType (getAttribute("DataNode", "Type", e));
		Element xref = e.getChild ("Xref", e.getNamespace());
		o.setGeneID (getAttribute("DataNode.Xref", "ID", xref));
		o.setDataSource (DataSource.getByFullName (getAttribute("DataNode.Xref", "Database", xref)));
	}

	protected void updateDataNode(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) {
			setAttribute ("DataNode", "TextLabel", e, o.getTextLabel());
			setAttribute ("DataNode", "Type", e, o.getDataNodeType());
			Element xref = e.getChild("Xref", e.getNamespace());
			String database = o.getDataSource() == null ? "" : o.getDataSource().getFullName();
			setAttribute ("DataNode.Xref", "Database", xref, database == null ? "" : database);
			setAttribute ("DataNode.Xref", "ID", xref, o.getGeneID());
        }
	}

	protected void mapStateData(PathwayElement o, Element e) throws ConverterException
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

	protected static void updateStateData(PathwayElement o, Element e) throws ConverterException
	{
		//TODO
	}
	
	protected void mapLineData(PathwayElement o, Element e) throws ConverterException
	{
    	
    	Element graphics = e.getChild("Graphics", e.getNamespace());

    	String style = getAttribute("Line.Graphics", "LineStyle", graphics);
    	o.setLineStyle ((style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED);

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
        	} else if(i == pointElements.size() - 1) {
    			endType = getAttribute("Line.Graphics.Point", "ArrowHead", pe);
        	}
    	}

    	o.setMPoints(mPoints);
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
	
	protected void updateLineData(PathwayElement o, Element e) throws ConverterException
	{
		if(e != null) {
	    	Element graphics = e.getChild("Graphics", e.getNamespace());
			setAttribute("Line.Graphics", "LineStyle", graphics, o.getLineStyle() == LineStyle.SOLID ? "Solid" : "Broken");

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

}
