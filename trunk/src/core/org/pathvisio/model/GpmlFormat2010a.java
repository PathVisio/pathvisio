package org.pathvisio.model;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Namespace;

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

	
}
