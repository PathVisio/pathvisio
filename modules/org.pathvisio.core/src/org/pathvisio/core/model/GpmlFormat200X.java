// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bridgedb.DataSource;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.pathvisio.core.biopax.BiopaxElement;
import org.pathvisio.core.model.PathwayElement.MAnchor;
import org.pathvisio.core.model.PathwayElement.MPoint;
import org.pathvisio.core.view.ShapeRegistry;

/**
 * GpmlFormat reader / writer for version 2007 and 2008A.
 * Code shared with other versions is in GpmlFormatAbstract.`
 */
class GpmlFormat200X extends GpmlFormatAbstract implements GpmlFormatReader
{
	public static final GpmlFormatReader GPML_2007 = new GpmlFormat200X (
		"GPML2007.xsd", Namespace.getNamespace("http://genmapp.org/GPML/2007")
	);
	public static final GpmlFormatReader GPML_2008A = new GpmlFormat200X (
		"GPML2008a.xsd", Namespace.getNamespace("http://genmapp.org/GPML/2008a")
	);

	public GpmlFormat200X(String xsdFile, Namespace ns) {
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
	}

	@Override
	protected Map<String, AttributeInfo> getAttributeInfo() 
	{
		return ATTRIBUTE_INFO;
	};

	private static final int CONVERSION = 15;
	
	private void mapDeprecatedAttribute(PathwayElement o, Element el, String key, String tag, String name) 
		throws ConverterException
	{
		String infoKey = tag + "@" + name;
		if (!getAttributeInfo().containsKey(infoKey))
				throw new ConverterException("Trying to get invalid attribute " + infoKey);
		String result = ((el == null) ? null : el.getAttributeValue(name));
		if (result != null) o.setDynamicProperty(key, result);
	}
	
	protected void updateMappInfoVariable(Element root, PathwayElement o) throws ConverterException
	{
		setAttribute("Pathway", "Copyright", root, o.getCopyright());
	}

	protected void mapMappInfoDataVariable (PathwayElement o, Element e) throws ConverterException
	{
		o.setCopyright (getAttribute("Pathway", "Copyright", e));
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
		o.setShapeType (ShapeRegistry.fromName(getAttribute("Shape", "Type", e)));
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

	protected void mapLabelData(PathwayElement o, Element e) throws ConverterException
	{
		o.setTextLabel (getAttribute("Label", "TextLabel", e));
    	Element graphics = e.getChild("Graphics", e.getNamespace());

    	String fontSizeString = getAttribute("Label.Graphics", "FontSize", graphics);
    	o.setMFontSize (Integer.parseInt(fontSizeString) / CONVERSION);

    	String fontWeight = getAttribute("Label.Graphics", "FontWeight", graphics);
    	String fontStyle = getAttribute("Label.Graphics", "FontStyle", graphics);
    	String fontDecoration = getAttribute("Label.Graphics", "FontDecoration", graphics);
    	String fontStrikethru = getAttribute("Label.Graphics", "FontStrikethru", graphics);

    	o.setBold (fontWeight != null && fontWeight.equals("Bold"));
    	o.setItalic (fontStyle != null && fontStyle.equals("Italic"));
    	o.setUnderline (fontDecoration != null && fontDecoration.equals("Underline"));
    	o.setStrikethru (fontStrikethru != null && fontStrikethru.equals("Strikethru"));
    	
    	String outline = getAttribute("Label", "Outline", e);
		if ("None".equals(outline)) o.setShapeType(ShapeType.NONE);
		else if ("Rectangle".equals(outline)) o.setShapeType(ShapeType.RECTANGLE);
		else if ("RoundedRectangle".equals(outline)) o.setShapeType(ShapeType.ROUNDED_RECTANGLE);

    	o.setFontName (getAttribute("Label.Graphics", "FontName", graphics));
	}
	
	protected void mapShapeData(PathwayElement o, Element e, String base) throws ConverterException
	{
		Element graphics = e.getChild("Graphics", e.getNamespace());
    	o.setMCenterX (Double.parseDouble(getAttribute(base + ".Graphics", "CenterX", graphics)) / CONVERSION);
    	o.setMCenterY (Double.parseDouble(getAttribute(base + ".Graphics", "CenterY", graphics)) / CONVERSION);
		o.setMWidth (Double.parseDouble(getAttribute(base + ".Graphics", "Width", graphics)) / CONVERSION);
		o.setMHeight (Double.parseDouble(getAttribute(base + ".Graphics", "Height", graphics)) / CONVERSION);
		String zorder = graphics.getAttributeValue("ZOrder");
		if (zorder != null)
			o.setZOrder(Integer.parseInt(zorder));
	}

	protected void mapDataNode(PathwayElement o, Element e) throws ConverterException
	{
		o.setTextLabel    (getAttribute("DataNode", "TextLabel", e));
		mapDeprecatedAttribute(o, e, "org.pathvisio.model.BackpageHead", "DataNode", "BackpageHead");
		o.setDataNodeType (getAttribute("DataNode", "Type", e));
		Element xref = e.getChild ("Xref", e.getNamespace());
		o.setGeneID (getAttribute("DataNode.Xref", "ID", xref));
		o.setDataSource (DataSource.getByFullName (getAttribute("DataNode.Xref", "Database", xref)));
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
		o.setMWidth (Double.parseDouble(getAttribute("State.Graphics", "Width", graphics)) / CONVERSION);
		o.setMHeight (Double.parseDouble(getAttribute("State.Graphics", "Height", graphics)) / CONVERSION);

		//TODO
		//StateType
		// ShapeType???
		// Line style???
		// Xref???
	}

	protected void mapLineData(PathwayElement o, Element e) throws ConverterException
	{
    	String style = getAttribute("Line", "Style", e);
    	o.setLineStyle ((style.equals("Solid")) ? LineStyle.SOLID : LineStyle.DASHED);
		
    	Element graphics = e.getChild("Graphics", e.getNamespace());

    	List<MPoint> mPoints = new ArrayList<MPoint>();

    	String startType = null;
    	String endType = null;

    	List<Element> pointElements = graphics.getChildren("Point", e.getNamespace());
    	for(int i = 0; i < pointElements.size(); i++) {
    		Element pe = pointElements.get(i);
    		MPoint mp = o.new MPoint(
    		    	Double.parseDouble(getAttribute("Line.Graphics.Point", "x", pe)) / CONVERSION,
    		    	Double.parseDouble(getAttribute("Line.Graphics.Point", "y", pe)) / CONVERSION
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

	protected void mapSimpleCenter(PathwayElement o, Element e)
	{
		o.setMCenterX (Double.parseDouble(e.getAttributeValue("CenterX")) / CONVERSION);
		o.setMCenterY (Double.parseDouble(e.getAttributeValue("CenterY")) / CONVERSION);
	}

	protected void mapBiopax(PathwayElement o, Element e) throws ConverterException
	{
		//this method clones all content,
		//getContent will leave them attached to the parent, which we don't want
		//We can safely remove them, since the JDOM element isn't used anymore after this method
		Element root = new Element("RDF", GpmlFormat.RDF);
		root.addNamespaceDeclaration(GpmlFormat.RDFS);
		root.addNamespaceDeclaration(GpmlFormat.RDF);
		root.addNamespaceDeclaration(GpmlFormat.OWL);
		root.addNamespaceDeclaration(GpmlFormat.BIOPAX);
		root.setAttribute(new Attribute("base", getGpmlNamespace().getURI() + "#", Namespace.XML_NAMESPACE));
		//Element owl = new Element("Ontology", OWL);
		//owl.setAttribute(new Attribute("about", "", RDF));
		//Element imp = new Element("imports", OWL);
		//imp.setAttribute(new Attribute("resource", BIOPAX.getURI(), RDF));
		//owl.addContent(imp);
		//root.addContent(owl);

		root.addContent(e.cloneContent());
		Document bp = new Document(root);

		updateBiopaxNamespace(root);
		((BiopaxElement)o).setBiopax(bp);
	}

	private static final Namespace BP_LEV2 = Namespace.getNamespace("bp", "http://www.biopax.org/release/biopax-level2.owl#");
	
	/**
	 * Convert the namespace of biopax element from old level2 to new level3.
	 * Starting from GpmlFormat2010a, we only support level 3.
	 * <p>
	 * We ignore all other differences between level 2 and level 3 here. For
	 * PublicationXref, it doesn't matter.
	 */
	private void updateBiopaxNamespace(Element e)
	{
		if (e.getNamespace().equals(BP_LEV2))
		{
			e.setNamespace(GpmlFormat.BIOPAX);
		}
		for (Object f : e.getChildren())
		{
			updateBiopaxNamespace((Element)f);
		}
	}
	
}
